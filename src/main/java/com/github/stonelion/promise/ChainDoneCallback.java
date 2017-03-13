package com.github.stonelion.promise;

public interface ChainDoneCallback<D, OUT> {
    PromiseCall<OUT> onDone(final D result);
}
