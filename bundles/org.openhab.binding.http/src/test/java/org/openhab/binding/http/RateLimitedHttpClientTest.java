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
package org.openhab.binding.http;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.http.internal.http.RateLimitedHttpClient;

/**
 * Tests request scheduling and priority handling in {@link RateLimitedHttpClient}.
 *
 * @author Jan N. Klug - Initial contribution
 * @author Leo Siepel - Deterministic rate limiting tests
 */
@NonNullByDefault
public class RateLimitedHttpClientTest {
    private static final URI TEST_URL = URI.create("http://localhost/testlocation");

    private final HttpClient httpClient = Objects.requireNonNull(mock(HttpClient.class));
    private final ScheduledExecutorService scheduler = Objects.requireNonNull(mock(ScheduledExecutorService.class));
    private final Request request = Objects.requireNonNull(mock(Request.class));

    @BeforeEach
    public void setUp() {
        when(httpClient.newRequest(TEST_URL)).thenReturn(request);
        when(request.method(HttpMethod.GET)).thenReturn(request);
    }

    @Test
    public void testWithoutLimit() {
        RateLimitedHttpClient client = new RateLimitedHttpClient(httpClient, scheduler);
        client.setDelay(0);

        CompletableFuture<Request> first = client.newRequest(TEST_URL, HttpMethod.GET, "", null);
        CompletableFuture<Request> second = client.newRequest(TEST_URL, HttpMethod.GET, "", null);

        assertTrue(first.isDone());
        assertTrue(second.isDone());
        assertSame(request, first.join());
        assertSame(request, second.join());
        Objects.requireNonNull(verify(httpClient, times(2))).newRequest(TEST_URL);
        verifyNoInteractions(scheduler);
    }

    @Test
    public void testWithLimit() {
        RateLimitedHttpClient client = new RateLimitedHttpClient(httpClient, scheduler);
        client.setDelay(500);
        Runnable processQueue = scheduledTask();

        CompletableFuture<Request> first = client.newRequest(TEST_URL, HttpMethod.GET, "", null);
        CompletableFuture<Request> second = client.newRequest(TEST_URL, HttpMethod.GET, "", null);

        assertFalse(first.isDone());
        assertFalse(second.isDone());

        processQueue.run();
        assertTrue(first.isDone());
        assertSame(request, first.join());
        assertFalse(second.isDone());

        processQueue.run();
        assertTrue(second.isDone());
        assertSame(request, second.join());
        Objects.requireNonNull(verify(httpClient, times(2))).newRequest(TEST_URL);
    }

    @Test
    public void testWithLimitAndPriority() {
        RateLimitedHttpClient client = new RateLimitedHttpClient(httpClient, scheduler);
        client.setDelay(500);
        Runnable processQueue = scheduledTask();

        CompletableFuture<Request> first = client.newRequest(TEST_URL, HttpMethod.GET, "", null);
        CompletableFuture<Request> second = client.newRequest(TEST_URL, HttpMethod.GET, "", null);
        CompletableFuture<Request> priority = client.newPriorityRequest(TEST_URL, HttpMethod.GET, "", null);

        assertFalse(first.isDone());
        assertFalse(second.isDone());
        assertFalse(priority.isDone());

        processQueue.run();
        assertTrue(priority.isDone());
        assertSame(request, priority.join());
        assertFalse(first.isDone());
        assertFalse(second.isDone());

        processQueue.run();
        assertTrue(first.isDone());
        assertSame(request, first.join());
        assertFalse(second.isDone());

        processQueue.run();
        assertTrue(second.isDone());
        assertSame(request, second.join());
        Objects.requireNonNull(verify(httpClient, times(3))).newRequest(TEST_URL);
    }

    private Runnable scheduledTask() {
        ArgumentCaptor<Runnable> task = ArgumentCaptor.forClass(Runnable.class);
        Objects.requireNonNull(verify(scheduler)).scheduleWithFixedDelay(task.capture(), eq(0L), eq(500L),
                eq(TimeUnit.MILLISECONDS));
        return Objects.requireNonNull(task.getValue());
    }
}
