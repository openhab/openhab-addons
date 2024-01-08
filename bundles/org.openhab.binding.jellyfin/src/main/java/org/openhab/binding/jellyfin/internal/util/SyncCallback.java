/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.jellyfin.internal.util;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.jellyfin.sdk.compatibility.JavaContinuation;

/**
 * The {@link SyncCallback} util to consume kotlin suspend functions.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public abstract class SyncCallback<T> extends JavaContinuation<@Nullable T> {
    private final CountDownLatch latch;
    @Nullable
    private T result;
    @Nullable
    private Throwable error;

    protected SyncCallback() {
        latch = new CountDownLatch(1);
    }

    @Override
    public void onSuccess(@Nullable T result) {
        this.result = result;
        latch.countDown();
    }

    @Override
    public void onError(@Nullable Throwable error) {
        this.error = error;
        latch.countDown();
    }

    public T awaitResult() throws SyncCallbackError {
        return awaitResult(10);
    }

    public T awaitResult(int timeoutSecs) throws SyncCallbackError {
        try {
            if (!latch.await(timeoutSecs, TimeUnit.SECONDS)) {
                throw new SyncCallbackError("Execution timeout");
            }
        } catch (InterruptedException e) {
            throw new SyncCallbackError(e);
        }
        var error = this.error;
        if (error != null) {
            throw new SyncCallbackError(error);
        }
        var result = this.result;
        if (result == null) {
            throw new SyncCallbackError("Missing result");
        }
        return result;
    }

    public static class SyncCallbackError extends Exception {
        private static final long serialVersionUID = 2157912759968949551L;

        protected SyncCallbackError(String message) {
            super(message);
        }

        protected SyncCallbackError(Throwable original) {
            super(original);
        }
    }
}
