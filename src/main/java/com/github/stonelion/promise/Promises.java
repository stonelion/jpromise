package com.github.stonelion.promise;

import javax.annotation.Nonnull;

public class Promises {

    @Nonnull
    public static <D> Promise<D> newPromise(PromiseCall<D> promiseCall) {
        if (promiseCall == null) {
            throw new NullPointerException();
        }

        return new PromiseImpl<D>().setPromiseCall(promiseCall);
    }
}
