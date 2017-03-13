package com.github.stonelion.promise;

import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
public class PromiseImpl<D> implements Promise<D> {
    protected volatile State state = State.PENDING;

    protected volatile DoneCallback<D> doneCallback;

    protected volatile FailCallback failCallback;

    private final PromiseResult<D> resultCallback = new PromiseResult<D>() {
        @Override
        public void resolve(D resolve) {
            if (!isPending()) {
                throw new IllegalStateException(
                        "current state is not in pending, but is in " + state);
            }
            state = State.RESOLVED;

            doneCallback.onDone(resolve);
        }

        @Override
        public void reject(Throwable reject) {
            if (!isPending()) {
                throw new IllegalStateException(
                        "current state is not in pending, but is in " + state);
            }
            state = State.REJECTED;

            //default to print exception
            if (failCallback == null) {
                reject.printStackTrace();
            } else {
                failCallback.onFail(reject);
            }
        }
    };

    /**
     * 客户端执行的方法调用。
     */
    private PromiseCall<D> promiseCall;

    public PromiseImpl<D> setPromiseCall(PromiseCall<D> promiseCall) {
        this.promiseCall = promiseCall;
        return this;
    }

    @Override
    public State state() {
        return state;
    }

    @Override
    public boolean isResolved() {
        return state == State.RESOLVED;
    }

    @Override
    public boolean isRejected() {
        return state == State.REJECTED;
    }

    @Override
    public boolean isPending() {
        return state == State.PENDING;
    }

    @Override
    public <OUT> Promise<OUT> then(ChainDoneCallback<D, OUT> doneCallback) {
        return then(doneCallback, null);
    }

    @Override
    public <OUT> Promise<OUT> then(final ChainDoneCallback<D, OUT> chainDoneCallback,
            FailCallback failCallback) {
        Precondition.checkNotNull(chainDoneCallback);

        final PromiseImpl<OUT> resultPromise = new PromiseImpl<>();
        resultPromise.failCallback = failCallback;

        this.doneCallback = new DoneCallback<D>() {
            @Override
            public void onDone(D result) {
                PromiseCall<OUT> promiseCall = chainDoneCallback.onDone(result);

                Precondition.checkNotNull(promiseCall);

                promiseCall.call(resultPromise.getResultCallback());
            }
        };

        triggerCall();
        return resultPromise;
    }

    @Override
    public Promise<D> then(DoneCallback<D> doneCallback) {
        this.doneCallback = doneCallback;

        triggerCall();
        return this;
    }

    @Override
    public Promise<D> then(FailCallback failCallback) {
        this.failCallback = failCallback;

        triggerCall();
        return this;
    }

    public PromiseResult<D> getResultCallback() {
        return resultCallback;
    }

    protected void triggerCall() {
        if (promiseCall != null) {
            try {
                promiseCall.call(resultCallback);
            } catch (Throwable t) {
                state = State.REJECTED;
                if (failCallback != null) {
                    failCallback.onFail(t);
                } else {
                    throw t;
                }
            }
        }
    }

    @Override
    public Promise<D> then(DoneCallback<D> doneCallback, FailCallback failCallback) {
        this.failCallback = failCallback;
        this.doneCallback = doneCallback;

        triggerCall();
        return this;
    }
}
