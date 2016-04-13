package me.yugy.app.common.utils;

import android.support.annotation.Nullable;
import android.widget.HeaderViewListAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewUtils {

    @Nullable
    public static ListAdapter getInnerAdapter(ListView list) {
        ListAdapter adapter = list.getAdapter();
        if (adapter == null) return null;
        if (adapter instanceof HeaderViewListAdapter) {
            return ((HeaderViewListAdapter) adapter).getWrappedAdapter();
        }
        return adapter;
    }
}
