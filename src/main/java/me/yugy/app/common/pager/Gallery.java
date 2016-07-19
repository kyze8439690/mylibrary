package me.yugy.app.common.pager;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class Gallery<T extends Photo> implements Parcelable {

    public int initPosition;
    public List<T> images;

    public boolean isValid() {
        return !(initPosition < 0 || images == null || images.size() == 0);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.initPosition);
        if (images == null) {
            dest.writeInt(-1);
            return;
        }
        dest.writeInt(images.size());
        if (images.isEmpty()) {
            return;
        }
        dest.writeSerializable(images.get(0).getClass());
        dest.writeList(images);
    }

    public Gallery() {
    }

    protected Gallery(Parcel in) {
        this.initPosition = in.readInt();
        int size = in.readInt();
        if (size == -1) {
            images = null;
            return;
        }
        images = new ArrayList<>();
        Class<?> type = (Class<?>) in.readSerializable();
        in.readList(images, type.getClassLoader());
    }

    public static final Creator<Gallery> CREATOR = new Creator<Gallery>() {
        @Override
        public Gallery createFromParcel(Parcel source) {
            return new Gallery(source);
        }

        @Override
        public Gallery[] newArray(int size) {
            return new Gallery[size];
        }
    };
}
