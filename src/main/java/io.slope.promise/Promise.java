package io.slope.promise;

/**
 * io.ut.io.slope.promise 接口
 * <p>
 * created on 16-9-29
 */
public interface Promise<D> {
    State state();

    boolean isResolved();

    boolean isRejected();

    boolean isPending();

    Promise<D> then(DoneCallback<D> doneCallback);

    Promise<D> then(DoneCallback<D> doneCallback, FailCallback failCallback);

    <OUT> Promise<OUT> then(ChainDoneCallback<D,OUT> doneCallback);

    <OUT> Promise<OUT> then(ChainDoneCallback<D,OUT> doneCallback, FailCallback failCallback);

    enum State {
        PENDING, REJECTED, RESOLVED
    }
}
