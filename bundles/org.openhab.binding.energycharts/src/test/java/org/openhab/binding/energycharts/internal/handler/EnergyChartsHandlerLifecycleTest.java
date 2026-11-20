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
package org.openhab.binding.energycharts.internal.handler;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.energycharts.internal.EnergyChartsBindingConstants;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.TimeSeries;

/**
 * Tests that a request still in flight cannot publish after the {@link EnergyChartsHandler} was disposed or
 * initialized again.
 *
 * @author Thomas Leber - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
@Timeout(30)
class EnergyChartsHandlerLifecycleTest {
    private static final long START = 1718400000L;
    private static final long WAIT_MILLIS = 5000;
    private static final long QUIET_MILLIS = 500;
    private static final ThingUID THING_UID = new ThingUID("energycharts", "prices", "home");
    private static final ChannelUID CHANNEL = new ChannelUID(THING_UID,
            EnergyChartsBindingConstants.CHANNEL_SPOT_PRICE);

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) TimeZoneProvider timeZoneProvider;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private final CountDownLatch requestStarted = new CountDownLatch(1);
    private final CountDownLatch releaseRequest = new CountDownLatch(1);

    private @NonNullByDefault({}) EnergyChartsHandler handler;

    @BeforeEach
    void setUp() {
        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getStatus()).thenReturn(ThingStatus.UNKNOWN);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.of("UTC"));
        useZone("AT");

        Clock clock = Clock.fixed(Instant.ofEpochSecond(START + 60), ZoneOffset.UTC);
        handler = new EnergyChartsHandler(thing, httpClient, timeZoneProvider, clock);
        handler.setCallback(callback);
    }

    @AfterEach
    void tearDown() {
        releaseRequest.countDown();
        handler.dispose();
    }

    private void useZone(String zone) {
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("biddingZone", zone)));
    }

    private ContentResponse pricesResponse() {
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(HttpStatus.OK_200);
        when(response.getContentAsString())
                .thenReturn("{\"unix_seconds\":[" + START + "," + (START + 900) + "],\"price\":[100.0,200.0]}");
        return response;
    }

    private Request request() {
        Request request = mock(Request.class);
        when(request.method(HttpMethod.GET)).thenReturn(request);
        when(request.timeout(10, TimeUnit.SECONDS)).thenReturn(request);
        return request;
    }

    /** Waits for the release, also when {@code dispose()} interrupts the refresh job. */
    private void pauseRequest() {
        requestStarted.countDown();
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(WAIT_MILLIS);
        while (true) {
            try {
                releaseRequest.await(deadline - System.nanoTime(), TimeUnit.NANOSECONDS);
                return;
            } catch (InterruptedException e) {
                // the request has to outlive the cancelled job
            }
        }
    }

    private void pausedResponseFor(String zone) throws Exception {
        Request request = request();
        when(request.send()).thenAnswer(invocation -> {
            pauseRequest();
            return pricesResponse();
        });
        when(httpClient.newRequest(contains("bzn=" + zone))).thenReturn(request);
    }

    private void pausedFailureFor(String zone) throws Exception {
        Request request = request();
        when(request.send()).thenAnswer(invocation -> {
            pauseRequest();
            throw new TimeoutException();
        });
        when(httpClient.newRequest(contains("bzn=" + zone))).thenReturn(request);
    }

    private static ThingStatusInfo anyStatusBut(ThingStatus status) {
        return argThat(info -> info.getStatus() != status);
    }

    @Test
    void responsePublishesWhileInitialized() throws Exception {
        pausedResponseFor("AT");

        handler.initialize();
        assertTrue(requestStarted.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
        releaseRequest.countDown();

        verify(callback, timeout(WAIT_MILLIS)).sendTimeSeries(eq(CHANNEL), any(TimeSeries.class));
        verify(callback, timeout(WAIT_MILLIS)).stateUpdated(eq(CHANNEL), any());
        verify(callback, timeout(WAIT_MILLIS)).statusUpdated(eq(thing), anyStatusBut(ThingStatus.UNKNOWN));
    }

    @Test
    void responseAfterDisposeIsDropped() throws Exception {
        pausedResponseFor("AT");

        handler.initialize();
        assertTrue(requestStarted.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
        handler.dispose();
        releaseRequest.countDown();

        verify(callback, after(QUIET_MILLIS).never()).sendTimeSeries(any(), any());
        verify(callback, after(QUIET_MILLIS).never()).stateUpdated(any(), any());
        verify(callback, after(QUIET_MILLIS).never()).statusUpdated(eq(thing), anyStatusBut(ThingStatus.UNKNOWN));
    }

    @Test
    void failureAfterDisposeIsDropped() throws Exception {
        pausedFailureFor("AT");

        handler.initialize();
        assertTrue(requestStarted.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
        handler.dispose();
        releaseRequest.countDown();

        verify(callback, after(QUIET_MILLIS).never()).statusUpdated(eq(thing), anyStatusBut(ThingStatus.UNKNOWN));
    }

    @Test
    void responseOfPreviousZoneIsDropped() throws Exception {
        pausedResponseFor("AT");
        Request newZone = request();
        ContentResponse newZoneResponse = pricesResponse();
        when(newZone.send()).thenReturn(newZoneResponse);
        when(httpClient.newRequest(contains("bzn=DE-LU"))).thenReturn(newZone);

        handler.initialize();
        assertTrue(requestStarted.await(WAIT_MILLIS, TimeUnit.MILLISECONDS));
        useZone("DE-LU");
        handler.thingUpdated(thing);
        verify(httpClient, timeout(WAIT_MILLIS)).newRequest(contains("bzn=DE-LU"));
        verify(callback, timeout(WAIT_MILLIS)).sendTimeSeries(eq(CHANNEL), any(TimeSeries.class));

        releaseRequest.countDown();

        verify(callback, after(QUIET_MILLIS).times(1)).sendTimeSeries(eq(CHANNEL), any(TimeSeries.class));
        verify(httpClient).newRequest(contains("bzn=AT"));
        verify(httpClient).newRequest(contains("bzn=DE-LU"));
        verify(httpClient, after(0).times(2)).newRequest(anyString());
    }
}
