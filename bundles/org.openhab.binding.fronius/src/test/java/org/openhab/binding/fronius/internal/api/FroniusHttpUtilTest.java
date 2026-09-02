/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.fronius.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link FroniusHttpUtil}.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
class FroniusHttpUtilTest {

    @Test
    void pollingRequestsAreSkippedWhenBridgeIsBusy() throws Exception {
        FroniusHttpUtil httpUtil = new FroniusHttpUtil();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);
        AtomicReference<Throwable> backgroundFailure = new AtomicReference<>();
        AtomicInteger skippedExecutorCalls = new AtomicInteger();

        Thread firstRequest = new Thread(() -> {
            try {
                httpUtil.executeUrl(HttpMethod.GET, "http://fronius-a", null, null, null, 5000,
                        (httpMethod, url, httpHeaders, content, contentType, timeout) -> {
                            firstStarted.countDown();
                            try {
                                assertTrue(releaseFirstRequest.await(5, TimeUnit.SECONDS));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException(e);
                            }
                            return "ok";
                        });
            } catch (Throwable t) {
                backgroundFailure.set(t);
            }
        });

        firstRequest.start();
        try {
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

            assertThrows(FroniusPollingSkipException.class,
                    () -> httpUtil.executePollingUrl(HttpMethod.GET, "http://fronius-b", null, null, null, 5000,
                            (httpMethod, url, httpHeaders, content, contentType, timeout) -> {
                                skippedExecutorCalls.incrementAndGet();
                                return "unexpected";
                            }));
            assertEquals(0, skippedExecutorCalls.get());
        } finally {
            releaseFirstRequest.countDown();
            firstRequest.join(5000);
        }

        assertFalse(firstRequest.isAlive());
        assertNull(backgroundFailure.get());
    }

    @Test
    void controlRequestsWaitForBusyBridge() throws Exception {
        FroniusHttpUtil httpUtil = new FroniusHttpUtil();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);
        CountDownLatch secondExecuted = new CountDownLatch(1);
        AtomicReference<Throwable> backgroundFailure = new AtomicReference<>();

        Thread firstRequest = new Thread(() -> {
            try {
                httpUtil.executeUrl(HttpMethod.GET, "http://fronius-b", null, null, null, 5000,
                        (httpMethod, url, httpHeaders, content, contentType, timeout) -> {
                            firstStarted.countDown();
                            try {
                                assertTrue(releaseFirstRequest.await(5, TimeUnit.SECONDS));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException(e);
                            }
                            return "ok";
                        });
            } catch (Throwable t) {
                backgroundFailure.set(t);
            }
        });

        Thread secondRequest = new Thread(() -> {
            try {
                httpUtil.executeUrl(HttpMethod.GET, "http://fronius-c", null, null, null, 5000,
                        (httpMethod, url, httpHeaders, content, contentType, timeout) -> {
                            secondExecuted.countDown();
                            return "ok";
                        });
            } catch (Throwable t) {
                backgroundFailure.set(t);
            }
        });

        firstRequest.start();
        try {
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

            secondRequest.start();
            assertFalse(secondExecuted.await(200, TimeUnit.MILLISECONDS));

            releaseFirstRequest.countDown();
            assertTrue(secondExecuted.await(5, TimeUnit.SECONDS));
        } finally {
            releaseFirstRequest.countDown();
            firstRequest.join(5000);
            secondRequest.join(5000);
        }

        assertFalse(firstRequest.isAlive());
        assertFalse(secondRequest.isAlive());
        assertNull(backgroundFailure.get());
    }

    @Test
    void requestsOnDifferentInstancesDoNotShareLock() throws Exception {
        FroniusHttpUtil firstBridgeHttpUtil = new FroniusHttpUtil();
        FroniusHttpUtil secondBridgeHttpUtil = new FroniusHttpUtil();
        CountDownLatch firstStarted = new CountDownLatch(1);
        CountDownLatch releaseFirstRequest = new CountDownLatch(1);
        AtomicReference<Throwable> backgroundFailure = new AtomicReference<>();

        Thread firstRequest = new Thread(() -> {
            try {
                firstBridgeHttpUtil.executeUrl(HttpMethod.GET, "http://fronius-d", null, null, null, 5000,
                        (httpMethod, url, httpHeaders, content, contentType, timeout) -> {
                            firstStarted.countDown();
                            try {
                                assertTrue(releaseFirstRequest.await(5, TimeUnit.SECONDS));
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new IOException(e);
                            }
                            return "ok";
                        });
            } catch (Throwable t) {
                backgroundFailure.set(t);
            }
        });

        firstRequest.start();
        try {
            assertTrue(firstStarted.await(5, TimeUnit.SECONDS));

            String response = secondBridgeHttpUtil.executePollingUrl(HttpMethod.GET, "http://fronius-e", null, null,
                    null, 5000, (httpMethod, url, httpHeaders, content, contentType, timeout) -> "other-bridge-ok");
            assertEquals("other-bridge-ok", response);
        } finally {
            releaseFirstRequest.countDown();
            firstRequest.join(5000);
        }

        assertFalse(firstRequest.isAlive());
        assertNull(backgroundFailure.get());
    }
}
