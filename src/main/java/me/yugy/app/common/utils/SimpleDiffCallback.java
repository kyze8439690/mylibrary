package me.yugy.app.common.utils;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Remember to implement {@link Object#equals(Object)} method in item.
 */
public class SimpleDiffCallback extends DiffUtil.Callback {

    private List mOldList = new ArrayList();
    private List mNewList = new ArrayList();

    public SimpleDiffCallback(@Nullable List oldList, @Nullable List newList) {
        if (oldList == null) {
            mOldList = new ArrayList();
        } else {
            mOldList = oldList;
        }

        if (newList == null) {
            mNewList = new ArrayList();
        } else {
            mNewList = newList;
        }
    }

    @Override
    public int getOldListSize() {
        return mOldList.size();
    }

    @Override
    public int getNewListSize() {
        return mNewList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return mOldList.get(oldItemPosition).equals(mNewList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return areItemsTheSame(oldItemPosition, newItemPosition);
    }
}
