package io.slope.promise;

public interface PromiseCall<D> {

    /**
     * 当订阅的方法被调用的时候。调用这个方法触发异步事件。如果没有订阅者，就没有必要触发。
     */
    void call(final PromiseResult<D> promiser);
}
