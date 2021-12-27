package ai.aistem.xbot.framework.internal.mqtt;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

public class Connections {

    /**
     * Singleton instance of <code>Connections</code>
     **/
    private static Connections instance = null;

    /**
     * List of {@link Connection} object
     **/
    private HashMap<String, Connection> connections = null;

    /**
     * Create a Connections object
     *
     * @param context Applications context
     */
    private Connections(Context context) {
        connections = new HashMap<String, Connection>();
    }

    /**
     * Returns an already initialised instance of <code>Connections</code>, if Connections has yet to be created, it will
     * create and return that instance.
     *
     * @param context The applications context used to create the <code>Connections</code> object if it is not already initialised
     * @return <code>Connections</code> instance
     */
    public synchronized static Connections getInstance(Context context) {
        if (instance == null) {
            instance = new Connections(context);
        }
        return instance;
    }

    /**
     * Finds and returns a {@link Connection} object that the given client handle points to
     *
     * @param handle The handle to the {@link Connection} to return
     * @return a connection associated with the client handle, <code>null</code> if one is not found
     */
    public Connection getConnection(String handle) {
        return connections.get(handle);
    }

    /**
     * Adds a {@link Connection} object to the collection of connections associated with this object
     *
     * @param connection {@link Connection} to add
     */
    public void addConnection(Connection connection) {
        connections.clear();
        connections.put(connection.handle(), connection);
    }

    /**
     * Get all the connections associated with this <code>Connections</code> object.
     *
     * @return <code>Map</code> of connections
     */
    public Map<String, Connection> getConnections() {
        return connections;
    }

    /**
     * Removes a connection from the map of connections
     *
     * @param connection connection to be removed
     */
    public void removeConnection(Connection connection) {
        connections.remove(connection.handle());
    }


    /**
     * Updates an existing connection within the map of
     * connections as well as in the persisted model
     *
     * @param connection connection to be updated.
     */
    public void updateConnection(Connection connection) {
        connections.put(connection.handle(), connection);
    }


}
