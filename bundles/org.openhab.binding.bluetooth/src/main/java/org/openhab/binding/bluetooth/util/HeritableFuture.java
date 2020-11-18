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
package org.openhab.binding.bluetooth.util;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@code HeritableFuture} class extends {@link CompletableFuture} and adds the ability
 * to cancel upstream CompletableFuture tasks. Normally when a CompletableFuture
 * is cancelled only dependent futures cancel. This class will also cancel the parent
 * HeritableFuture instances as well. All of the {@code CompletionStage} methods will
 * return HeritableFuture children and thus by only maintaining a reference to the final future
 * in the task chain it would be possible to cancel the entire chain by calling {@code cancel}.
 * <p>
 * Due to child futures now having a link to their parent futures, it is no longer possible
 * for HeritableFuture to be garbage collected as upstream futures complete. It is highly
 * advisable to only use a HeritableFuture for defining finite (preferably small) task trees. Do not use
 * HeritableFuture in situations where you would endlessly append new tasks otherwise you will eventually
 * cause an OutOfMemoryException.
 *
 * @author Connor Petty - Initial contribution
 *
 */
// @NonNullByDefault - Can't be added
public class HeritableFuture<T> extends CompletableFuture<T> {

    protected final Object futureLock = new Object();
    protected @Nullable Future<?> parentFuture;

    public HeritableFuture() {
    }

    public HeritableFuture(Future<?> parent) {
        this.parentFuture = parent;
    }

    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new HeritableFuture<>(this);
    }

    protected void setParentFuture(Supplier<Future<?>> futureSupplier) {
        synchronized (futureLock) {
            var future = futureSupplier.get();
            if (future != this) {
                parentFuture = future;
            }
        }
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (completeExceptionally(new CancellationException())) {
            synchronized (futureLock) {
                var future = parentFuture;
                parentFuture = null;
                if (future != null) {
                    future.cancel(mayInterruptIfRunning);
                }
            }
            return true;
        }
        return isCancelled();
    }

    private <V, U> Function<V, ? extends CompletionStage<U>> wrapComposeFunction(
            Function<V, ? extends CompletionStage<U>> fn) {
        return fn.andThen(r -> {
            // the parent future has completed so we can safely assign a new future
            setParentFuture(r::toCompletableFuture);
            return r;
        });
    }

    @Override
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return super.thenCompose(wrapComposeFunction(fn));
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return super.thenComposeAsync(wrapComposeFunction(fn));
    }

    @Override
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
            Executor executor) {
        return super.thenComposeAsync(wrapComposeFunction(fn), executor);
    }
}