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

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.common.NamedThreadFactory;

/**
 * @author Connor Petty - Initial contribution
 *
 */
@NonNullByDefault
class RetryFutureTest {

    private static final int TIMEOUT_MS = 1000;
    private @NonNullByDefault({}) ScheduledExecutorService scheduler;

    @BeforeEach
    public void init() {
        ScheduledThreadPoolExecutor scheduler = new ScheduledThreadPoolExecutor(1,
                new NamedThreadFactory("RetryFutureTest", true));
        scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        scheduler.setRemoveOnCancelPolicy(true);
        this.scheduler = scheduler;
    }

    @AfterEach
    public void cleanup() {
        scheduler.shutdownNow();
    }

    @Test
    void callWithRetryNormal() {
        Future<String> retryFuture = RetryFuture.callWithRetry(() -> "test", scheduler);
        try {
            assertEquals("test", retryFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
    }

    @Test
    void callWithRetry1() {
        AtomicInteger visitCount = new AtomicInteger();
        Future<String> retryFuture = RetryFuture.callWithRetry(() -> {
            if (visitCount.getAndIncrement() == 0) {
                throw new RetryException(0, TimeUnit.SECONDS);
            }
            return "test";
        }, scheduler);
        try {
            assertEquals("test", retryFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
    }

    @Test
    void composeWithRetryNormal() {
        CompletableFuture<?> composedFuture = new CompletableFuture<>();

        Future<?> retryFuture = RetryFuture.composeWithRetry(() -> {
            composedFuture.complete(null);
            return composedFuture;
        }, scheduler);

        try {
            retryFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
        assertTrue(composedFuture.isDone());
    }

    @Test
    void composeWithRetryThrow() {
        CompletableFuture<?> composedFuture = new CompletableFuture<>();

        Future<?> retryFuture = RetryFuture.composeWithRetry(() -> {
            composedFuture.completeExceptionally(new DummyException());
            return composedFuture;
        }, scheduler);

        try {
            retryFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException | TimeoutException e) {
            fail(e);
        } catch (ExecutionException ex) {
            assertTrue(ex.getCause() instanceof DummyException);
        }
        assertTrue(composedFuture.isDone());
    }

    @Test
    void composeWithRetry1() {
        AtomicInteger visitCount = new AtomicInteger();
        CompletableFuture<String> composedFuture = new CompletableFuture<>();
        Future<String> retryFuture = RetryFuture.composeWithRetry(() -> {
            if (visitCount.getAndIncrement() == 0) {
                return CompletableFuture.failedFuture(new RetryException(0, TimeUnit.SECONDS));
            }
            composedFuture.complete("test");
            return composedFuture;
        }, scheduler);

        try {
            assertEquals("test", retryFuture.get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
        assertEquals(2, visitCount.get());
        assertTrue(composedFuture.isDone());
    }

    @Test
    void composeWithRetry1Cancel() {
        CountDownLatch latch = new CountDownLatch(1);
        AtomicInteger visitCount = new AtomicInteger();
        CompletableFuture<String> composedFuture = new CompletableFuture<>();
        Future<String> retryFuture = RetryFuture.composeWithRetry(() -> {
            if (visitCount.getAndIncrement() == 0) {
                return CompletableFuture.failedFuture(new RetryException(0, TimeUnit.SECONDS));
            }
            latch.countDown();
            return composedFuture;
        }, scheduler);

        try {
            if (!latch.await(TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                fail("Timeout while waiting for latch");
            }
            Future<Boolean> future = scheduler.submit(() -> {
                retryFuture.cancel(false);
                return composedFuture.isCancelled();
            });
            assertTrue(future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            fail(e);
        }
        assertEquals(2, visitCount.get());
        assertTrue(composedFuture.isDone());
    }

    private static class DummyException extends Exception {
        private static final long serialVersionUID = 1L;
    }
}
