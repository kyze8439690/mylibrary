package me.yugy.app.common.view;

import android.view.View;

import butterknife.ButterKnife;

@SuppressWarnings("unused")
public abstract class BaseHolder<T> {

    View rootView;

    public BaseHolder(View view) {
        rootView = view;
        ButterKnife.inject(this, view);
        view.setTag(this);
    }

    public abstract void parse(T data);
}
