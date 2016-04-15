package me.yugy.app.common.core;

import android.view.View;

import butterknife.ButterKnife;

@SuppressWarnings("unused")
public abstract class BaseHolder<T> {

    public View rootView;

    public BaseHolder(View view) {
        rootView = view;
        ButterKnife.bind(this, view);
        view.setTag(this);
    }

    public abstract void parse(T data);
}
