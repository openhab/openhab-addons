/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.minecraft.internal.util;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.functions.Func1;

/**
 * RX operator subscribing to observable with a delay after it has finished.
 *
 * @author Mattias Markehed - Initial contribution
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
