/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.minecraft.internal.util;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * RX operator subscribing to observable with a delay after it has finished.
 *
 * @author Mattias Markehed
 */
public class RetryWithDelay implements Func1<Observable<? extends Throwable>, Observable<?>> {

    private final int maxRetries;
    private final long retryDelayMillis;
    private int retryCount;

    public RetryWithDelay(final long retryDelay, TimeUnit unit) {
        this(-1, TimeUnit.MILLISECONDS.convert(retryDelay, unit));
    }

    public RetryWithDelay(final int maxRetries, final long retryDelay, TimeUnit unit) {
        this(maxRetries, TimeUnit.MILLISECONDS.convert(retryDelay, unit));
    }

    private RetryWithDelay(final int maxRetries, final long retryDelayMillis) {
        this.maxRetries = maxRetries;
        this.retryDelayMillis = retryDelayMillis;
        this.retryCount = 0;
    }

    @Override
    public Observable<?> call(Observable<? extends Throwable> attempts) {
        return attempts.flatMap(new Func1<Throwable, Observable<?>>() {
            @Override
            public Observable<?> call(Throwable throwable) {
                if (maxRetries < 0 || ++retryCount < maxRetries) {
                    return Observable.timer(retryDelayMillis, TimeUnit.MILLISECONDS);
                }

                return Observable.error(throwable);
            }
        });
    }
}