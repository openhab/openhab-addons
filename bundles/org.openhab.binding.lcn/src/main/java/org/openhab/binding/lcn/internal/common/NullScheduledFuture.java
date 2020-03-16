/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.lcn.internal.common;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Empty ScheduledFuture, used for initialization.
 *
 * @author Fabian Wolter - Initial Contribution
 */
@NonNullByDefault
public class NullScheduledFuture implements ScheduledFuture<Object> {
    @NonNullByDefault({})
    private static class LazyHolder {
        static final NullScheduledFuture INSTANCE = new NullScheduledFuture();
    }

    private NullScheduledFuture() {
        // nothing
    }

    /** Gets the instance of this singleton. */
    public static NullScheduledFuture getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public long getDelay(@Nullable TimeUnit unit) {
        return 0;
    }

    @Override
    public int compareTo(@Nullable Delayed o) {
        return 0;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return true;
    }

    @Override
    public boolean isCancelled() {
        return true;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        return new Object();
    }

    @Override
    public Object get(long timeout, @Nullable TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException {
        return new Object();
    }
}
