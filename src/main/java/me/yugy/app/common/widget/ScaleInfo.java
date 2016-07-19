package me.yugy.app.common.widget;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.View;

public class ScaleInfo implements Parcelable {
    public int x;
    public int y;
    public int width;
    public int height;

    @Nullable
    public static ScaleInfo from(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ScaleInfo scaleInfo = new ScaleInfo();
        scaleInfo.x = location[0];
        scaleInfo.y = location[1];
        scaleInfo.width = view.getWidth();
        scaleInfo.height = view.getHeight();
        if (scaleInfo.width == 0 || scaleInfo.height == 0) {
            return null;
        } else {
            return scaleInfo;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.x);
        dest.writeInt(this.y);
        dest.writeInt(this.width);
        dest.writeInt(this.height);
    }

    public ScaleInfo() {
    }

    protected ScaleInfo(Parcel in) {
        this.x = in.readInt();
        this.y = in.readInt();
        this.width = in.readInt();
        this.height = in.readInt();
    }

    public static final Parcelable.Creator<ScaleInfo> CREATOR = new Parcelable.Creator<ScaleInfo>() {
        @Override
        public ScaleInfo createFromParcel(Parcel source) {
            return new ScaleInfo(source);
        }

        @Override
        public ScaleInfo[] newArray(int size) {
            return new ScaleInfo[size];
        }
    };
}
