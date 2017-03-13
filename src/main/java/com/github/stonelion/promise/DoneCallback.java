package com.github.stonelion.promise;


public interface DoneCallback<D> {
    void onDone(final D result);
}
