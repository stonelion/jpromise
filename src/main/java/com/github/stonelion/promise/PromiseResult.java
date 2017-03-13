package com.github.stonelion.promise;

public interface PromiseResult<D> {

    void resolve(D resolve);

    void reject(Throwable reject);
}
