package com.septrivium.augeo.rxhelper;

import android.util.Log;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * Created by harold on 7/24/2015.
 */
public class RetryWithDelay
        implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int _maxRetries;
    private int _retryCount;

    /**
     *
     * @param maxRetries
     */
    public RetryWithDelay(final int maxRetries) {
        _maxRetries = maxRetries;
        _retryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts.flatMap(new Func1<Throwable, Observable<?>>() {
            @Override
            public Observable<?> call(Throwable throwable) {
                if (++_retryCount < _maxRetries) {
                    // When this Observable calls onNext, the original
                    // Observable will be retried (i.e. re-subscribed).
                    Log.d("RX_RETRY", "Retry in : " + (_retryCount * _retryCount) + " Seconds");

                    return Observable.timer(_retryCount * _retryCount ,
                            TimeUnit.SECONDS);
                }

                // Max retries hit. Just pass the error along.
                return Observable.error(throwable);
            }
        });
    }
}