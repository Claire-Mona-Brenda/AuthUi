package com.konka.authui.presenter;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.support.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class BasePresenter<T> implements LifecycleObserver {
    private CompositeDisposable mDisposable = new CompositeDisposable();
    protected T mView;

    public BasePresenter(T view) {
        this.mView = view;
    }

    protected void addObserver(@NonNull Disposable disposable) {
        mDisposable.add(disposable);
    }

    protected void removeObserver(@NonNull Disposable disposable) {
        mDisposable.remove(disposable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void release() {
        mDisposable.clear();
        mView = null;
    }
}
