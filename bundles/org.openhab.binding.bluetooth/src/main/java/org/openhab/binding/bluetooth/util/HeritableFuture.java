/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
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
 * cause an OutOfMemoryError.
 *
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
public class HeritableFuture<T> extends CompletableFuture<T> {

    protected final Object futureLock = new Object();
    protected @Nullable Future<?> parentFuture;

    public HeritableFuture() {
    }

    public HeritableFuture(Future<?> parent) {
        this.parentFuture = parent;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @implNote
     *           This implementation returns a new HeritableFuture instance that uses
     *           the current instance as a parent. Cancellation of the child will result in
     *           cancellation of the parent.
     */
    @Override
    public <U> CompletableFuture<U> newIncompleteFuture() {
        return new HeritableFuture<>(this);
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    protected void setParentFuture(Supplier<@Nullable Future<?>> futureSupplier) {
        synchronized (futureLock) {
            var future = futureSupplier.get();
            if (future != this) {
                if (isCancelled() && future != null) {
                    future.cancel(true);
                } else {
                    parentFuture = future;
                }
            }
        }
    }

    /**
     *
     * {@inheritDoc}
     *
     * @implNote
     *           This implementation cancels this future first, then cancels the parent future.
     */
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

    /**
     *
     * {@inheritDoc}
     *
     * @implNote
     *           This implementation will treat the future returned by the function as a parent future.
     */
    @Override
    @NonNullByDefault({}) // the generics here don't play well with the null checker
    public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
        return new ComposeFunctionWrapper<>(fn, false, null).returnedFuture;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @implNote
     *           This implementation will treat the future returned by the function as a parent future.
     */
    @Override
    @NonNullByDefault({}) // the generics here don't play well with the null checker
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn) {
        return new ComposeFunctionWrapper<>(fn, true, null).returnedFuture;
    }

    /**
     *
     * {@inheritDoc}
     *
     * @implNote
     *           This implementation will treat the future returned by the function as a parent future.
     */
    @Override
    @NonNullByDefault({}) // the generics here don't play well with the null checker
    public <U> CompletableFuture<U> thenComposeAsync(Function<? super T, ? extends CompletionStage<U>> fn,
            Executor executor) {
        return new ComposeFunctionWrapper<>(fn, true, executor).returnedFuture;
    }

    /**
     * This class is responsible for wrapping the supplied compose function.
     * The instant the function returns the next CompletionStage, the parentFuture of the downstream HeritableFuture
     * will be reassigned to the completion stage. This way cancellations of
     * downstream futures will be able to reach the future returned by the supplied function.
     *
     * Most of the complexity going on in this class is due to the fact that the apply function might be
     * called while calling `super.thenCompose`. This would happen if the current future is already complete
     * since the next stage would be started immediately either on the current thread or asynchronously.
     *
     * @param <U> the type to be returned by the composed future
     */
    private class ComposeFunctionWrapper<U> implements Function<T, CompletionStage<U>> {

        private final Object fieldsLock = new Object();
        private final Function<? super T, ? extends CompletionStage<U>> fn;
        private @Nullable HeritableFuture<U> composedFuture;
        private @Nullable CompletionStage<U> innerStage;
        // The final composed future to be used by users of this wrapper class
        final HeritableFuture<U> returnedFuture;

        public ComposeFunctionWrapper(Function<? super T, ? extends CompletionStage<U>> fn, boolean async,
                @Nullable Executor executor) {
            this.fn = fn;

            var f = (HeritableFuture<U>) thenCompose(async, executor);
            synchronized (fieldsLock) {
                this.composedFuture = f;
                var stage = innerStage;
                if (stage != null) {
                    // getting here means that the `apply` function was run before `composedFuture` was initialized.
                    f.setParentFuture(stage::toCompletableFuture);
                }
            }
            this.returnedFuture = f;
        }

        private CompletableFuture<U> thenCompose(boolean async, @Nullable Executor executor) {
            if (!async) {
                return HeritableFuture.super.thenCompose(this);
            }
            if (executor == null) {
                return HeritableFuture.super.thenComposeAsync(this);
            }
            return HeritableFuture.super.thenComposeAsync(this, executor);
        }

        @Override
        public CompletionStage<U> apply(T t) {
            CompletionStage<U> stage = fn.apply(t);
            synchronized (fieldsLock) {
                var f = composedFuture;
                if (f == null) {
                    // We got here before the wrapper finished initializing, so that
                    // means that the enclosing future was already complete at the time `super.thenCompose` was called.
                    // In which case the best we can do is save this stage so that the constructor can finish the job.
                    innerStage = stage;
                } else {
                    f.setParentFuture(stage::toCompletableFuture);
                }
            }
            return stage;
        }
    }
}
