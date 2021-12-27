#ifndef KMEANS_INDEX_H
#define KMEANS_INDEX_H
#include <algorithm>
#include <string>
#include <map>
#include <cassert>
#include <limits>
#include <cmath>
//#include <sqlite3.h>

#include <iostream>


#include "Setting.h"

//#include <opencv2/flann/any.h>
#include <opencv2/flann.hpp>

namespace my_cvflann
{
using namespace cvflann;

struct my_KMeansIndexParams : public IndexParams
{
    my_KMeansIndexParams(int branching = 32, int iterations = 11,
                      flann_centers_init_t centers_init = FLANN_CENTERS_RANDOM, float cb_index = 0.2 )
    {
        (*this)["algorithm"] = FLANN_INDEX_KMEANS;
        // branching factor
        (*this)["branching"] = branching;
        // max iterations to perform in one kmeans clustering (kmeans tree)
        (*this)["iterations"] = iterations;
        // algorithm used for picking the initial cluster centers for kmeans tree
        (*this)["centers_init"] = centers_init;
        // cluster boundary index. Used when searching the kmeans tree
        (*this)["cb_index"] = cb_index;
    }
};


/**
 * Hierarchical kmeans index
 *
 * Contains a tree constructed through a hierarchical kmeans clustering
 * and other information for indexing a set of points for nearest-neighbour matching.
 */
template <typename Distance>
class my_KMeansIndex : public NNIndex<Distance>
{
public:
    typedef typename Distance::ElementType ElementType;
    typedef typename Distance::ResultType DistanceType;



    typedef void (my_KMeansIndex::* centersAlgFunction)(int, int*, int, int*, int&);

    /**
     * The function used for choosing the cluster centers.
     */
    centersAlgFunction chooseCenters;



    /**
     * Chooses the initial centers in the k-means clustering in a random manner.
     *
     * Params:
     *     k = number of centers
     *     vecs = the dataset of points
     *     indices = indices in the dataset
     *     indices_length = length of indices vector
     *
     */
    void chooseCentersRandom(int k, int* indices, int indices_length, int* centers, int& centers_length)
    {
        UniqueRandom r(indices_length);

        int index;
        for (index=0; index<k; ++index) {
            bool duplicate = true;
            int rnd;
            while (duplicate) {
                duplicate = false;
                rnd = r.next();
                if (rnd<0) {
                    centers_length = index;
                    return;
                }

                centers[index] = indices[rnd];

                for (int j=0; j<index; ++j) {
                    DistanceType sq = distance_(dataset_[centers[index]], dataset_[centers[j]], dataset_.cols);
                    if (sq<1e-16) {
                        duplicate = true;
                    }
                }
            }
        }

        centers_length = index;
    }


    /**
     * Chooses the initial centers in the k-means using Gonzales' algorithm
     * so that the centers are spaced apart from each other.
     *
     * Params:
     *     k = number of centers
     *     vecs = the dataset of points
     *     indices = indices in the dataset
     * Returns:
     */
    void chooseCentersGonzales(int k, int* indices, int indices_length, int* centers, int& centers_length)
    {
        int n = indices_length;

        int rnd = rand_int(n);
        assert(rnd >=0 && rnd < n);

        centers[0] = indices[rnd];

        int index;
        for (index=1; index<k; ++index) {

            int best_index = -1;
            DistanceType best_val = 0;
            for (int j=0; j<n; ++j) {
                DistanceType dist = distance_(dataset_[centers[0]],dataset_[indices[j]],dataset_.cols);
                for (int i=1; i<index; ++i) {
                    DistanceType tmp_dist = distance_(dataset_[centers[i]],dataset_[indices[j]],dataset_.cols);
                    if (tmp_dist<dist) {
                        dist = tmp_dist;
                    }
                }
                if (dist>best_val) {
                    best_val = dist;
                    best_index = j;
                }
            }
            if (best_index!=-1) {
                centers[index] = indices[best_index];
            }
            else {
                break;
            }
        }
        centers_length = index;
    }


    /**
     * Chooses the initial centers in the k-means using the algorithm
     * proposed in the KMeans++ paper:
     * Arthur, David; Vassilvitskii, Sergei - k-means++: The Advantages of Careful Seeding
     *
     * Implementation of this function was converted from the one provided in Arthur's code.
     *
     * Params:
     *     k = number of centers
     *     vecs = the dataset of points
     *     indices = indices in the dataset
     * Returns:
     */
    void chooseCentersKMeanspp(int k, int* indices, int indices_length, int* centers, int& centers_length)
    {
        int n = indices_length;

        double currentPot = 0;
        DistanceType* closestDistSq = new DistanceType[n];

        // Choose one random center and set the closestDistSq values
        int index = rand_int(n);
        assert(index >=0 && index < n);
        centers[0] = indices[index];

        for (int i = 0; i < n; i++) {
            closestDistSq[i] = distance_(dataset_[indices[i]], dataset_[indices[index]], dataset_.cols);
            closestDistSq[i] = ensureSquareDistance<Distance>( closestDistSq[i] );
            currentPot += closestDistSq[i];
        }


        const int numLocalTries = 1;

        // Choose each center
        int centerCount;
        for (centerCount = 1; centerCount < k; centerCount++) {

            // Repeat several trials
            double bestNewPot = -1;
            int bestNewIndex = -1;
            for (int localTrial = 0; localTrial < numLocalTries; localTrial++) {

                // Choose our center - have to be slightly careful to return a valid answer even accounting
                // for possible rounding errors
                double randVal = rand_double(currentPot);
                for (index = 0; index < n-1; index++) {
                    if (randVal <= closestDistSq[index]) break;
                    else randVal -= closestDistSq[index];
                }

                // Compute the new potential
                double newPot = 0;
                for (int i = 0; i < n; i++) {
                    DistanceType dist = distance_(dataset_[indices[i]], dataset_[indices[index]], dataset_.cols);
                    newPot += std::min( ensureSquareDistance<Distance>(dist), closestDistSq[i] );
                }

                // Store the best result
                if ((bestNewPot < 0)||(newPot < bestNewPot)) {
                    bestNewPot = newPot;
                    bestNewIndex = index;
                }
            }

            // Add the appropriate center
            centers[centerCount] = indices[bestNewIndex];
            currentPot = bestNewPot;
            for (int i = 0; i < n; i++) {
                DistanceType dist = distance_(dataset_[indices[i]], dataset_[indices[bestNewIndex]], dataset_.cols);
                closestDistSq[i] = std::min( ensureSquareDistance<Distance>(dist), closestDistSq[i] );
            }
        }

        centers_length = centerCount;

        delete[] closestDistSq;
    }



public:

    flann_algorithm_t getType() const
    {
        return FLANN_INDEX_KMEANS;
    }

    /**
     * Index constructor
     *
     * Params:
     *          inputData = dataset with the input features
     *          params = parameters passed to the hierarchical k-means algorithm
     */
    my_KMeansIndex(const cv::Mat& inputData, const IndexParams& params = my_KMeansIndexParams(),
                Distance d = Distance())
        :dataset_((ElementType*)inputData.data, inputData.rows, inputData.cols),index_params_(params)
        , root_(NULL), indices_(NULL), distance_(d)/*,sqlite3_(NULL)*/ ,descriptorDate_(NULL)
    {
        memoryCounter_ = 0;
        

        size_ = dataset_.rows;
        veclen_ = dataset_.cols;
        printf("11size_=%d veclen_=%d\n ",size_,veclen_);

        branching_ = get_param(params,"branching",32);
        iterations_ = get_param(params,"iterations",11);
        if (iterations_<0) {
            iterations_ = (std::numeric_limits<int>::max)();
        }
        centers_init_  = get_param(params,"centers_init",FLANN_CENTERS_RANDOM);

        if (centers_init_==FLANN_CENTERS_RANDOM) {
            chooseCenters = &my_KMeansIndex::chooseCentersRandom;
        }
        else if (centers_init_==FLANN_CENTERS_GONZALES) {
            chooseCenters = &my_KMeansIndex::chooseCentersGonzales;
        }
        else if (centers_init_==FLANN_CENTERS_KMEANSPP) {
            chooseCenters = &my_KMeansIndex::chooseCentersKMeanspp;
        }
        else {
            throw FLANNException("Unknown algorithm for choosing initial centers.");
        }
        cb_index_ = 0.4f;
    }

    my_KMeansIndex(void * descriptorDate,const int &size, const IndexParams& params = my_KMeansIndexParams(),
                Distance d = Distance())
        :dataset_(),index_params_(params)
        , root_(NULL), indices_(NULL), distance_(d),descriptorDate_(descriptorDate)
    {
        memoryCounter_ = 0;

        size_ = size;
        veclen_ = 128;
        printf("22size_=%d veclen_=%d\n ",size_,veclen_);

        branching_ = get_param(params,"branching",32);
        iterations_ = get_param(params,"iterations",11);
        if (iterations_<0) {
            iterations_ = (std::numeric_limits<int>::max)();
        }
        centers_init_  = get_param(params,"centers_init",FLANN_CENTERS_RANDOM);

        if (centers_init_==FLANN_CENTERS_RANDOM) {
            chooseCenters = &my_KMeansIndex::chooseCentersRandom;
        }
        else if (centers_init_==FLANN_CENTERS_GONZALES) {
            chooseCenters = &my_KMeansIndex::chooseCentersGonzales;
        }
        else if (centers_init_==FLANN_CENTERS_KMEANSPP) {
            chooseCenters = &my_KMeansIndex::chooseCentersKMeanspp;
        }
        else {
            throw FLANNException("Unknown algorithm for choosing initial centers.");
        }
        cb_index_ = 0.4f;
    }


    my_KMeansIndex(const my_KMeansIndex&);
    my_KMeansIndex& operator=(const my_KMeansIndex&);


    /**
     * Index destructor.
     *
     * Release the memory used by the index.
     */
    virtual ~my_KMeansIndex()
    {
        if (root_ != NULL) {
            free_centers(root_);
        }
        if (indices_!=NULL) {
            delete[] indices_;
        }
    }

    /**
     *  Returns size of index.
     */
    size_t size() const
    {
        return size_;
    }

    /**
     * Returns the length of an index feature.
     */
    size_t veclen() const
    {
        return veclen_;
    }


    void set_cb_index( float index)
    {
        cb_index_ = index;
    }

    /**
     * Computes the inde memory usage
     * Returns: memory used by the index
     */
    int usedMemory() const
    {
        return pool_.usedMemory+pool_.wastedMemory+memoryCounter_;
    }

    /**
     * Dummy implementation for other algorithms of addable indexes after that.
     */
    void addIndex(const Matrix<ElementType>& /*wholeData*/, const Matrix<ElementType>& /*additionalData*/)
    {
    }

    /**
     * Builds the index
     */
    void buildIndex()
    {
        if (branching_<2) {
            throw FLANNException("Branching factor must be at least 2");
        }

        indices_ = new int[size_];
        for (size_t i=0; i<size_; ++i) {
            indices_[i] = int(i);
        }

        root_ = pool_.allocate<KMeansNode>();
        std::memset(root_, 0, sizeof(KMeansNode));

        computeNodeStatistics(root_, indices_, (int)size_);
        computeClustering(root_, indices_, (int)size_, branching_,0);
        printf("111 branching_=%d iterations_=%d memoryCounter_=%d cb_index_=%f size_=%d \n"
        ,branching_,iterations_,memoryCounter_,cb_index_,size_);
        printf("355size=%d \n",size_);
    }


    void saveIndex(FILE* stream)
    {
    printf("saveIndex111 \n");
        save_value(stream, branching_);
        save_value(stream, iterations_);
        save_value(stream, memoryCounter_);
        save_value(stream, cb_index_);
        save_value(stream, *indices_, (int)size_);

        printf("222 branching_=%d iterations_=%d memoryCounter_=%d \n"
        ,branching_,iterations_,memoryCounter_);
		printf("cb_index_=%f \n",cb_index_);

        printf("355size=%d \n",size_);

        save_tree(stream, root_);
    }


    void loadIndex(FILE* stream)
    {
    printf("000\n");
        load_value(stream, branching_);
        printf("1111\n");
        load_value(stream, iterations_);
        load_value(stream, memoryCounter_);
        load_value(stream, cb_index_);

        if (indices_!=NULL) {
            delete[] indices_;
        }
        indices_ = new int[size_];
        load_value(stream, *indices_, size_);
		printf("465 size=%d \n",size_);

        if (root_!=NULL) {
            free_centers(root_);
        }
		printf("355size=%d \n",size_);

        load_tree(stream, root_);
		printf("444size=%d \n",size_);

        index_params_["algorithm"] = getType();
        index_params_["branching"] = branching_;
        index_params_["iterations"] = iterations_;
        index_params_["centers_init"] = centers_init_;
        index_params_["cb_index"] = cb_index_;
        printf("555size=%d \n",size_);
        printf("333 branching_=%d iterations_=%d memoryCounter_=%d cb_index_=%f \n"
        ,branching_,iterations_,memoryCounter_,cb_index_);

        printf("355size=%d \n",size_);

    }

    void knnSearch(const cv::Mat& queries, cv::Mat& indices, cv::Mat& dists, int knn, const SearchParams& params = SearchParams())
        {
            assert(queries.cols == veclen());
            assert(indices.rows >= queries.rows);
            assert(dists.rows >= queries.rows);
            assert(int(indices.cols) >= knn);
            assert(int(dists.cols) >= knn);

            cvflann::Matrix<ElementType> _query((ElementType*)queries.data, queries.rows, queries.cols);
            cvflann::Matrix<int> _indices((int*)indices.data, indices.rows, indices.cols);
            cvflann::Matrix<DistanceType> _dists((DistanceType*)dists.data, dists.rows, dists.cols);
            KNNUniqueResultSet<DistanceType> resultSet(knn);
            for (size_t i = 0; i < _query.rows; i++) {
                resultSet.clear();
                findNeighbors(resultSet, _query[i], params);
                if (get_param(params,"sorted",true)) resultSet.sortAndCopy(_indices[i], _dists[i], knn);
                else resultSet.copy(_indices[i], _dists[i], knn);
            }
        }


    /**
     * Find set of nearest neighbors to vec. Their indices are stored inside
     * the result object.
     *
     * Params:
     *     result = the result object in which the indices of the nearest-neighbors are stored
     *     vec = the vector for which to search the nearest neighbors
     *     searchParams = parameters that influence the search algorithm (checks, cb_index)
     */
    void findNeighbors(ResultSet<DistanceType>& result, const ElementType* vec, const SearchParams& searchParams)
    {

        int maxChecks = get_param(searchParams,"checks",32);

        if (maxChecks==FLANN_CHECKS_UNLIMITED) {
            findExactNN(root_, result, vec);
        }
        else {
            // Priority queue storing intermediate branches in the best-bin-first search
            Heap<BranchSt>* heap = new Heap<BranchSt>((int)size_);

            int checks = 0;
            findNN(root_, result, vec, checks, maxChecks, heap);

            BranchSt branch;
            while (heap->popMin(branch) && (checks<maxChecks || !result.full())) {
                KMeansNodePtr node = branch.node;
                findNN(node, result, vec, checks, maxChecks, heap);
            }
            assert(result.full());

            delete heap;
        }

    }

    /**
     * Clustering function that takes a cut in the hierarchical k-means
     * tree and return the clusters centers of that clustering.
     * Params:
     *     numClusters = number of clusters to have in the clustering computed
     * Returns: number of cluster centers
     */
    int getClusterCenters(Matrix<DistanceType>& centers)
    {
        int numClusters = centers.rows;
        if (numClusters<1) {
            throw FLANNException("Number of clusters must be at least 1");
        }

        DistanceType variance;
        KMeansNodePtr* clusters = new KMeansNodePtr[numClusters];

        int clusterCount = getMinVarianceClusters(root_, clusters, numClusters, variance);

        Logger::info("Clusters requested: %d, returning %d\n",numClusters, clusterCount);

        for (int i=0; i<clusterCount; ++i) {
            DistanceType* center = clusters[i]->pivot;
            for (size_t j=0; j<veclen_; ++j) {
                centers[i][j] = center[j];
            }
        }
        delete[] clusters;

        return clusterCount;
    }

    IndexParams getParameters() const
    {
        return index_params_;
    }


private:
    /**
     * Struture representing a node in the hierarchical k-means tree.
     */
    struct KMeansNode
    {
        /**
         * The cluster center.
         */
        DistanceType* pivot;
        /**
         * The cluster radius.
         */
        DistanceType radius;
        /**
         * The cluster mean radius.
         */
        DistanceType mean_radius;
        /**
         * The cluster variance.
         */
        DistanceType variance;
        /**
         * The cluster size (number of points in the cluster)
         */
        int size;
        /**
         * Child nodes (only for non-terminal nodes)
         */
        KMeansNode** childs;
        /**
         * Node points (only for terminal nodes)
         */
        int* indices;
        /**
         * Level
         */
        int level;
    };
    typedef KMeansNode* KMeansNodePtr;

    /**
     * Alias definition for a nicer syntax.
     */
    typedef BranchStruct<KMeansNodePtr, DistanceType> BranchSt;

    Matrix<ElementType> getDateSet(const int &index)
    {
      int colSize = sizeof(ElementType) * veclen_;
      Matrix<ElementType> DateSet;
      if(NULL == descriptorDate_)
      {
        UDEBUG("descriptorDate_ is NULL ...\n");
        return DateSet;
      }
      ElementType *date = (ElementType*)(descriptorDate_+(index*colSize));
      DateSet = Matrix<ElementType>(date,1,veclen_);
      return DateSet;
    }

#if 0
    Matrix<ElementType> getDateSet(const int &index)
    {
      int result = 0;
      Matrix<ElementType> DateSet;
      sqlite3_stmt * stat = NULL;
      char arr[20];
      sprintf(arr, "%d ;", index);
      std::string select = "SELECT descriptor FROM descriptor_table WHERE ID = ";
      std::string sql =  select + arr;
      result = sqlite3_prepare(sqlite3_,sql.c_str(),-1, &stat, 0);
      if(result != SQLITE_OK)
      {
        return DateSet;
      }
      while(1)
      {
        result = sqlite3_step(stat);
        if(SQLITE_ROW == result)
        {
          ElementType *descriptorData =  (ElementType*)sqlite3_column_blob(stat,0);
          int descriptorDataLen = sqlite3_column_bytes(stat,0);
          descriptorDataLen /= sizeof(ElementType);
          DateSet = Matrix<ElementType>(descriptorData,1,descriptorDataLen);
          break;
        }
        else if(SQLITE_DONE == result)
        {
          break;
        }
      }
      sqlite3_finalize(stat);
      return DateSet;
    }
#endif
    void save_tree(FILE* stream, KMeansNodePtr node)
    {
        save_value(stream, *node);
        save_value(stream, *(node->pivot), (int)veclen_);
        if (node->childs==NULL) {
            int indices_offset = (int)(node->indices - indices_);
            save_value(stream, indices_offset);
        }
        else {
            for(int i=0; i<branching_; ++i) {
                save_tree(stream, node->childs[i]);
            }
        }
    }


    void load_tree(FILE* stream, KMeansNodePtr& node)
    {
        node = pool_.allocate<KMeansNode>();
        load_value(stream, *node);
        node->pivot = new DistanceType[veclen_];
        //printf(" load tree 111\n");
        load_value(stream, *(node->pivot), (int)veclen_);
        //printf(" load tree 222\n");
        if (node->childs==NULL) {
            int indices_offset;
            load_value(stream, indices_offset);
            node->indices = indices_ + indices_offset;
        }
        else {
            node->childs = pool_.allocate<KMeansNodePtr>(branching_);
            for(int i=0; i<branching_; ++i) {
                load_tree(stream, node->childs[i]);
            }
        }
    }


    /**
     * Helper function
     */
    void free_centers(KMeansNodePtr node)
    {
        delete[] node->pivot;
        if (node->childs!=NULL) {
            for (int k=0; k<branching_; ++k) {
                free_centers(node->childs[k]);
            }
        }
    }

    /**
     * Computes the statistics of a node (mean, radius, variance).
     *
     * Params:
     *     node = the node to use
     *     indices = the indices of the points belonging to the node
     */
    void computeNodeStatistics(KMeansNodePtr node, int* indices, int indices_length)
    {

        DistanceType radius = 0;
        DistanceType variance = 0;
        DistanceType* mean = new DistanceType[veclen_];
        memoryCounter_ += int(veclen_*sizeof(DistanceType));

        memset(mean,0,veclen_*sizeof(DistanceType));

        for (size_t i=0; i<size_; ++i) {
            ElementType* vec = dataset_[indices[i]];
            for (size_t j=0; j<veclen_; ++j) {
                mean[j] += vec[j];
            }
            variance += distance_(vec, ZeroIterator<ElementType>(), veclen_);
        }
        for (size_t j=0; j<veclen_; ++j) {
            mean[j] /= size_;
        }
        variance /= size_;
        variance -= distance_(mean, ZeroIterator<ElementType>(), veclen_);

        DistanceType tmp = 0;
        for (int i=0; i<indices_length; ++i) {
            tmp = distance_(mean, dataset_[indices[i]], veclen_);
            if (tmp>radius) {
                radius = tmp;
            }
        }

        node->variance = variance;
        node->radius = radius;
        node->pivot = mean;
    }


    /**
     * The method responsible with actually doing the recursive hierarchical
     * clustering
     *
     * Params:
     *     node = the node to cluster
     *     indices = indices of the points belonging to the current node
     *     branching = the branching factor to use in the clustering
     *
     * TODO: for 1-sized clusters don't store a cluster center (it's the same as the single cluster point)
     */
    void computeClustering(KMeansNodePtr node, int* indices, int indices_length, int branching, int level)
    {
        node->size = indices_length;
        node->level = level;

        if (indices_length < branching) {
            node->indices = indices;
            std::sort(node->indices,node->indices+indices_length);
            node->childs = NULL;
            return;
        }

        int* centers_idx = new int[branching];
        int centers_length;
        (this->*chooseCenters)(branching, indices, indices_length, centers_idx, centers_length);

        if (centers_length<branching) {
            node->indices = indices;
            std::sort(node->indices,node->indices+indices_length);
            node->childs = NULL;
            delete [] centers_idx;
            return;
        }


        Matrix<double> dcenters(new double[branching*veclen_],branching,veclen_);
        for (int i=0; i<centers_length; ++i) {
            ElementType* vec = dataset_[centers_idx[i]];
            for (size_t k=0; k<veclen_; ++k) {
                dcenters[i][k] = double(vec[k]);
            }
        }
        delete[] centers_idx;

        std::vector<DistanceType> radiuses(branching);
        int* count = new int[branching];
        for (int i=0; i<branching; ++i) {
            radiuses[i] = 0;
            count[i] = 0;
        }

        //	assign points to clusters
        int* belongs_to = new int[indices_length];
        for (int i=0; i<indices_length; ++i) {

            DistanceType sq_dist = distance_(dataset_[indices[i]], dcenters[0], veclen_);
            belongs_to[i] = 0;
            for (int j=1; j<branching; ++j) {
                DistanceType new_sq_dist = distance_(dataset_[indices[i]], dcenters[j], veclen_);
                if (sq_dist>new_sq_dist) {
                    belongs_to[i] = j;
                    sq_dist = new_sq_dist;
                }
            }
            if (sq_dist>radiuses[belongs_to[i]]) {
                radiuses[belongs_to[i]] = sq_dist;
            }
            count[belongs_to[i]]++;
        }

        bool converged = false;
        int iteration = 0;
        while (!converged && iteration<iterations_) {
            converged = true;
            iteration++;

            // compute the new cluster centers
            for (int i=0; i<branching; ++i) {
                memset(dcenters[i],0,sizeof(double)*veclen_);
                radiuses[i] = 0;
            }
            for (int i=0; i<indices_length; ++i) {
                ElementType* vec = dataset_[indices[i]];
                double* center = dcenters[belongs_to[i]];
                for (size_t k=0; k<veclen_; ++k) {
                    center[k] += vec[k];
                }
            }
            for (int i=0; i<branching; ++i) {
                int cnt = count[i];
                for (size_t k=0; k<veclen_; ++k) {
                    dcenters[i][k] /= cnt;
                }
            }

            // reassign points to clusters
            for (int i=0; i<indices_length; ++i) {
                DistanceType sq_dist = distance_(dataset_[indices[i]], dcenters[0], veclen_);
                int new_centroid = 0;
                for (int j=1; j<branching; ++j) {
                    DistanceType new_sq_dist = distance_(dataset_[indices[i]], dcenters[j], veclen_);
                    if (sq_dist>new_sq_dist) {
                        new_centroid = j;
                        sq_dist = new_sq_dist;
                    }
                }
                if (sq_dist>radiuses[new_centroid]) {
                    radiuses[new_centroid] = sq_dist;
                }
                if (new_centroid != belongs_to[i]) {
                    count[belongs_to[i]]--;
                    count[new_centroid]++;
                    belongs_to[i] = new_centroid;

                    converged = false;
                }
            }

            for (int i=0; i<branching; ++i) {
                // if one cluster converges to an empty cluster,
                // move an element into that cluster
                if (count[i]==0) {
                    int j = (i+1)%branching;
                    while (count[j]<=1) {
                        j = (j+1)%branching;
                    }

                    for (int k=0; k<indices_length; ++k) {
                        if (belongs_to[k]==j) {
                            // for cluster j, we move the furthest element from the center to the empty cluster i
                            if ( distance_(dataset_[indices[k]], dcenters[j], veclen_) == radiuses[j] ) {
                                belongs_to[k] = i;
                                count[j]--;
                                count[i]++;
                                break;
                            }
                        }
                    }
                    converged = false;
                }
            }

        }

        DistanceType** centers = new DistanceType*[branching];

        for (int i=0; i<branching; ++i) {
            centers[i] = new DistanceType[veclen_];
            memoryCounter_ += (int)(veclen_*sizeof(DistanceType));
            for (size_t k=0; k<veclen_; ++k) {
                centers[i][k] = (DistanceType)dcenters[i][k];
            }
        }


        // compute kmeans clustering for each of the resulting clusters
        node->childs = pool_.allocate<KMeansNodePtr>(branching);
        int start = 0;
        int end = start;
        for (int c=0; c<branching; ++c) {
            int s = count[c];

            DistanceType variance = 0;
            DistanceType mean_radius =0;
            for (int i=0; i<indices_length; ++i) {
                if (belongs_to[i]==c) {
                    DistanceType d = distance_(dataset_[indices[i]], ZeroIterator<ElementType>(), veclen_);
                    variance += d;
                    mean_radius += sqrt(d);
                    std::swap(indices[i],indices[end]);
                    std::swap(belongs_to[i],belongs_to[end]);
                    end++;
                }
            }
            variance /= s;
            mean_radius /= s;
            variance -= distance_(centers[c], ZeroIterator<ElementType>(), veclen_);

            node->childs[c] = pool_.allocate<KMeansNode>();
            std::memset(node->childs[c], 0, sizeof(KMeansNode));
            node->childs[c]->radius = radiuses[c];
            node->childs[c]->pivot = centers[c];
            node->childs[c]->variance = variance;
            node->childs[c]->mean_radius = mean_radius;
            computeClustering(node->childs[c],indices+start, end-start, branching, level+1);
            start=end;
        }

        delete[] dcenters.data;
        delete[] centers;
        delete[] count;
        delete[] belongs_to;
    }



    /**
     * Performs one descent in the hierarchical k-means tree. The branches not
     * visited are stored in a priority queue.
     *
     * Params:
     *      node = node to explore
     *      result = container for the k-nearest neighbors found
     *      vec = query points
     *      checks = how many points in the dataset have been checked so far
     *      maxChecks = maximum dataset points to checks
     */


    void findNN(KMeansNodePtr node, ResultSet<DistanceType>& result, const ElementType* vec, int& checks, int maxChecks,
                Heap<BranchSt>* heap)
    {
        // Ignore those clusters that are too far away
        {
            DistanceType bsq = distance_(vec, node->pivot, veclen_);
            DistanceType rsq = node->radius;
            DistanceType wsq = result.worstDist();

            DistanceType val = bsq-rsq-wsq;
            DistanceType val2 = val*val-4*rsq*wsq;

            //if (val>0) {
            if ((val>0)&&(val2>0)) {
                return;
            }
        }

        if (node->childs==NULL) {
            if (checks>=maxChecks) {
                if (result.full()) return;
            }
            checks += node->size;
            for (int i=0; i<node->size; ++i) {
                int index = node->indices[i];
                DistanceType dist;
                if(/*dataset_.rows < 1 && */NULL != descriptorDate_)
                {
                  dist = distance_(getDateSet(index)[0], vec, veclen_);
                }
                else
                {
                  dist = distance_(dataset_[index], vec, veclen_);
                }
                result.addPoint(dist, index);
            }
        }
        else {
            DistanceType* domain_distances = new DistanceType[branching_];
            int closest_center = exploreNodeBranches(node, vec, domain_distances, heap);
            delete[] domain_distances;
            findNN(node->childs[closest_center],result,vec, checks, maxChecks, heap);
        }
    }

    /**
     * Helper function that computes the nearest childs of a node to a given query point.
     * Params:
     *     node = the node
     *     q = the query point
     *     distances = array with the distances to each child node.
     * Returns:
     */
    int exploreNodeBranches(KMeansNodePtr node, const ElementType* q, DistanceType* domain_distances, Heap<BranchSt>* heap)
    {

        int best_index = 0;
        domain_distances[best_index] = distance_(q, node->childs[best_index]->pivot, veclen_);
        for (int i=1; i<branching_; ++i) {
            domain_distances[i] = distance_(q, node->childs[i]->pivot, veclen_);
            if (domain_distances[i]<domain_distances[best_index]) {
                best_index = i;
            }
        }

        //		float* best_center = node->childs[best_index]->pivot;
        for (int i=0; i<branching_; ++i) {
            if (i != best_index) {
                domain_distances[i] -= cb_index_*node->childs[i]->variance;

                //				float dist_to_border = getDistanceToBorder(node.childs[i].pivot,best_center,q);
                //				if (domain_distances[i]<dist_to_border) {
                //					domain_distances[i] = dist_to_border;
                //				}
                heap->insert(BranchSt(node->childs[i],domain_distances[i]));
            }
        }

        return best_index;
    }


    /**
     * Function the performs exact nearest neighbor search by traversing the entire tree.
     */
    void findExactNN(KMeansNodePtr node, ResultSet<DistanceType>& result, const ElementType* vec)
    {
        // Ignore those clusters that are too far away
        {
            DistanceType bsq = distance_(vec, node->pivot, veclen_);
            DistanceType rsq = node->radius;
            DistanceType wsq = result.worstDist();

            DistanceType val = bsq-rsq-wsq;
            DistanceType val2 = val*val-4*rsq*wsq;

            //                  if (val>0) {
            if ((val>0)&&(val2>0)) {
                return;
            }
        }


        if (node->childs==NULL) {
            for (int i=0; i<node->size; ++i) {
                int index = node->indices[i];
                DistanceType dist = distance_(dataset_[index], vec, veclen_);
                result.addPoint(dist, index);
            }
        }
        else {
            int* sort_indices = new int[branching_];

            getCenterOrdering(node, vec, sort_indices);

            for (int i=0; i<branching_; ++i) {
                findExactNN(node->childs[sort_indices[i]],result,vec);
            }

            delete[] sort_indices;
        }
    }


    /**
     * Helper function.
     *
     * I computes the order in which to traverse the child nodes of a particular node.
     */
    void getCenterOrdering(KMeansNodePtr node, const ElementType* q, int* sort_indices)
    {
        DistanceType* domain_distances = new DistanceType[branching_];
        for (int i=0; i<branching_; ++i) {
            DistanceType dist = distance_(q, node->childs[i]->pivot, veclen_);

            int j=0;
            while (domain_distances[j]<dist && j<i) j++;
            for (int k=i; k>j; --k) {
                domain_distances[k] = domain_distances[k-1];
                sort_indices[k] = sort_indices[k-1];
            }
            domain_distances[j] = dist;
            sort_indices[j] = i;
        }
        delete[] domain_distances;
    }

    /**
     * Method that computes the squared distance from the query point q
     * from inside region with center c to the border between this
     * region and the region with center p
     */
    DistanceType getDistanceToBorder(DistanceType* p, DistanceType* c, DistanceType* q)
    {
        DistanceType sum = 0;
        DistanceType sum2 = 0;

        for (int i=0; i<veclen_; ++i) {
            DistanceType t = c[i]-p[i];
            sum += t*(q[i]-(c[i]+p[i])/2);
            sum2 += t*t;
        }

        return sum*sum/sum2;
    }


    /**
     * Helper function the descends in the hierarchical k-means tree by spliting those clusters that minimize
     * the overall variance of the clustering.
     * Params:
     *     root = root node
     *     clusters = array with clusters centers (return value)
     *     varianceValue = variance of the clustering (return value)
     * Returns:
     */
    int getMinVarianceClusters(KMeansNodePtr root, KMeansNodePtr* clusters, int clusters_length, DistanceType& varianceValue)
    {
        int clusterCount = 1;
        clusters[0] = root;

        DistanceType meanVariance = root->variance*root->size;

        while (clusterCount<clusters_length) {
            DistanceType minVariance = (std::numeric_limits<DistanceType>::max)();
            int splitIndex = -1;

            for (int i=0; i<clusterCount; ++i) {
                if (clusters[i]->childs != NULL) {

                    DistanceType variance = meanVariance - clusters[i]->variance*clusters[i]->size;

                    for (int j=0; j<branching_; ++j) {
                        variance += clusters[i]->childs[j]->variance*clusters[i]->childs[j]->size;
                    }
                    if (variance<minVariance) {
                        minVariance = variance;
                        splitIndex = i;
                    }
                }
            }

            if (splitIndex==-1) break;
            if ( (branching_+clusterCount-1) > clusters_length) break;

            meanVariance = minVariance;

            // split node
            KMeansNodePtr toSplit = clusters[splitIndex];
            clusters[splitIndex] = toSplit->childs[0];
            for (int i=1; i<branching_; ++i) {
                clusters[clusterCount++] = toSplit->childs[i];
            }
        }

        varianceValue = meanVariance/root->size;
        return clusterCount;
    }

private:
    /** The branching factor used in the hierarchical k-means clustering */
    int branching_;

    /** Maximum number of iterations to use when performing k-means clustering */
    int iterations_;

    /** Algorithm for choosing the cluster centers */
    flann_centers_init_t centers_init_;

    /**
     * Cluster border index. This is used in the tree search phase when determining
     * the closest cluster to explore next. A zero value takes into account only
     * the cluster centres, a value greater then zero also take into account the size
     * of the cluster.
     */
    float cb_index_;

    /**
     * The dataset used by this index
     */
    const Matrix<ElementType> dataset_;

    /** Index parameters */
    IndexParams index_params_;

    /**
     * Number of features in the dataset.
     */
    size_t size_;

    /**
     * Length of each feature.
     */
    size_t veclen_;

    /**
     * The root node in the tree.
     */
    KMeansNodePtr root_;

    /**
     *  Array of indices to vectors in the dataset.
     */
    int* indices_;

    /**
     * The distance
     */
    Distance distance_;

    /**
     * Pooled memory allocator.
     */
    PooledAllocator pool_;

    /**
     * Memory occupied by the index.
     */
    int memoryCounter_;

    //sqlite
    void *descriptorDate_;
};

}




#endif