package me.yugy.app.common.core;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public abstract class BaseAdapter<T, E extends BaseHolder<T>> extends android.widget.BaseAdapter {

    @NonNull private ArrayList<T> mData = new ArrayList<>();

    public BaseAdapter(@Nullable List<T> data) {
        setData(data);
    }

    public void setData(@Nullable List<T> data) {
        if (data == null) {
            data = new ArrayList<>();
        }
        mData.clear();
        mData.addAll(data);
        notifyDataSetChanged();
    }

    @NonNull
    public ArrayList<T> getData() {
        return mData;
    }

    public void prepend(T item) {
        mData.add(0, item);
        notifyDataSetChanged();
    }

    public void prepend(List<T> items) {
        mData.addAll(0, items);
        notifyDataSetChanged();
    }

    public void append(T item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void append(List<T> items) {
        mData.addAll(items);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        E holder;
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            holder = generateHolder(inflater, parent, getItemViewType(position));
        } else {
            //noinspection unchecked
            holder = (E) convertView.getTag();
        }
        holder.parse(getItem(position));
        return holder.rootView;
    }

    public abstract E generateHolder(LayoutInflater inflater, ViewGroup parent, int itemType);
}
