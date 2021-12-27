package ai.aistem.xbot.framework.data.db.entities;

import android.os.Parcel;
import android.os.Parcelable;

import org.litepal.crud.LitePalSupport;

/**
 * @author: aistem
 * @created: 2019/3/28
 * @desc: ***
 */
public class AnimInfo extends LitePalSupport implements Parcelable {

    private int animId;

    private int startTime;

    private ReadAnimInfo readAnimInfo;

    public AnimInfo(){}

    protected AnimInfo(Parcel in) {
        animId = in.readInt();
        startTime = in.readInt();
    }

    public static final Creator<AnimInfo> CREATOR = new Creator<AnimInfo>() {
        @Override
        public AnimInfo createFromParcel(Parcel in) {
            return new AnimInfo(in);
        }

        @Override
        public AnimInfo[] newArray(int size) {
            return new AnimInfo[size];
        }
    };

    public ReadAnimInfo getReadAnimInfo() {
        return readAnimInfo;
    }

    public void setReadAnimInfo(ReadAnimInfo readAnimInfo) {
        this.readAnimInfo = readAnimInfo;
    }

    public int getAnimId() {
        return animId;
    }

    public void setAnimId(int animId) {
        this.animId = animId;
    }

    public int getStartTime() {
        return startTime;
    }

    public void setStartTime(int startTime) {
        this.startTime = startTime;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(animId);
        dest.writeInt(startTime);
    }
}
