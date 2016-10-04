package io.slope.promise;


import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

public class PromiseTest {

    @Test
    public void isPending() throws Exception {
        Promise<String> p = Promises.newPromise(new PromiseCall<String>() {
            @Override
            public void call(final PromiseResult<String> promiseResult) {
            }
        });

        assertTrue(p.isPending());
    }

    @Test
    public void onDone() throws InterruptedException {
        final AtomicBoolean successful = new AtomicBoolean();

        Promise<String> p = Promises.newPromise(new PromiseCall<String>() {
            @Override
            public void call(final PromiseResult<String> promiseResult) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        promiseResult.resolve("123");
                    }
                }.start();

            }
        });

        p.then(new DoneCallback<String>() {
            @Override
            public void onDone(String result) {
                assertEquals(result, "123");
                successful.set(true);
            }
        });

        Thread.sleep(150);
        assertTrue("async call back is not done", successful.get());
        assertTrue(p.isResolved());
    }

    @Test
    public void onChainDone() throws InterruptedException {
        final AtomicBoolean successful = new AtomicBoolean();

        Promise<Integer> p = Promises.newPromise(new PromiseCall<String>() {
            @Override
            public void call(final PromiseResult<String> promiseResult) {
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        promiseResult.resolve("223");
                    }
                }.start();
            }
        }).then(new ChainDoneCallback<String, Integer>() {
            @Override
            public PromiseCall<Integer> onDone(final String result) {
                System.out.println(result);

                return new PromiseCall<Integer>() {
                    @Override
                    public void call(final PromiseResult<Integer> promiser) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                promiser.resolve(Integer.valueOf(result) + 1000);
                            }
                        }.start();
                    }
                };
            }
        }).then(new ChainDoneCallback<Integer, Integer>() {
            @Override
            public PromiseCall<Integer> onDone(final Integer result) {
                System.out.println(result);


                return new PromiseCall<Integer>() {
                    @Override
                    public void call(final PromiseResult<Integer> promiser) {
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(100);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                promiser.resolve(Integer.valueOf(result) + 2000);
                            }
                        }.start();
                    }
                };
            }
        }).then(new DoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                successful.set(true);
                System.out.println(result + 100);
            }
        });

        Thread.sleep(500);
        assertTrue("async call back is not done", successful.get());
    }

    @Test
    public void onFail() throws InterruptedException {
        final AtomicBoolean successful = new AtomicBoolean();

        Promise<Integer> p = Promises.newPromise(new PromiseCall<Integer>() {
            @Override
            public void call(PromiseResult<Integer> promiser) {
                throw new RuntimeException("abc");
            }
        }).then(new DoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                successful.set(false);
                System.out.println(result);
            }
        }, new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                successful.set(true);
                System.out.println(result.toString());
            }
        });

        Thread.sleep(100);
        assertTrue("async call back is not done", successful.get());
        assertTrue(p.isRejected());
    }

    @Test(expected = RuntimeException.class)
    public void noFailHandle() throws InterruptedException {
        Promise<Integer> p = Promises.newPromise(new PromiseCall<Integer>() {
            @Override
            public void call(PromiseResult<Integer> promiser) {
                throw new RuntimeException("abc");
            }
        }).then(new DoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                System.out.println(result);
            }
        });
    }

    @Test
    public void onRejectWithDefaultHandle() {
        Promise<Integer> p = Promises.newPromise(new PromiseCall<Integer>() {
            @Override
            public void call(PromiseResult<Integer> promiser) {
                promiser.reject(new RuntimeException("reject"));
            }
        }).then(new DoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                System.out.println(result);
            }
        });
    }

    @Test
    public void onRejectWithHandle() {
        final AtomicBoolean successful = new AtomicBoolean();
        final RuntimeException reject = new RuntimeException("reject");

        Promise<Integer> p = Promises.newPromise(new PromiseCall<Integer>() {
            @Override
            public void call(PromiseResult<Integer> promiser) {
                promiser.reject(reject);
            }
        }).then(new DoneCallback<Integer>() {
            @Override
            public void onDone(Integer result) {
                System.out.println(result);
            }
        }, new FailCallback() {
            @Override
            public void onFail(Throwable result) {
                assertEquals(reject, result);
                successful.set(true);
            }
        });

        assertTrue(successful.get());
    }


}