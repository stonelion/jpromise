package io.slope.promise;


public interface DoneCallback<D> {
    void onDone(final D result);
}
