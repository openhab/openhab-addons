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
package org.openhab.binding.philipsair.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIException;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDeviceDTO;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Tests that {@link PhilipsAirHandler} releases its connection when it is disposed.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class PhilipsAirHandlerLifecycleTest extends JavaTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_COAP, "test");
    private static final ChannelUID POWER_CHANNEL = new ChannelUID(THING_UID, CONTROLS, POWER);

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) PhilipsAirAPIConnection connection;
    private @Mock @NonNullByDefault({}) PhilipsAirAPIConnection secondConnection;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;
    private @Mock @NonNullByDefault({}) PhilipsAirStateDescriptionOptionProvider stateDescriptionProvider;

    private final CountDownLatch connectionCreated = new CountDownLatch(1);
    private final CountDownLatch releaseConnection = new CountDownLatch(1);
    private final CountDownLatch requestStarted = new CountDownLatch(1);
    private final CountDownLatch finishRequest = new CountDownLatch(1);
    private final AtomicInteger connectionsCreated = new AtomicInteger();
    private final Queue<String> latchTimeouts = new ConcurrentLinkedQueue<>();
    // returned instead of the connection by the creations after the first one
    private volatile @Nullable PhilipsAirAPIConnection replacementConnection;

    private @NonNullByDefault({}) PhilipsAirHandler handler;

    @BeforeEach
    public void setUp() {
        Configuration config = new Configuration();
        config.put(PhilipsAirConfiguration.CONFIG_HOST, "1.1.1.1");
        Thing thing = ThingBuilder.create(THING_TYPE_COAP, THING_UID).withConfiguration(config)
                .withChannel(ChannelBuilder.create(POWER_CHANNEL, "Switch").build()).build();
        handler = new PhilipsAirHandler(thing, httpClient, stateDescriptionProvider) {
            @Override
            PhilipsAirAPIConnection createConnection(PhilipsAirConfiguration config) {
                int created = connectionsCreated.incrementAndGet();
                connectionCreated.countDown();
                PhilipsAirAPIConnection replacement = replacementConnection;
                if (created > 1 && replacement != null) {
                    return replacement;
                }
                // a blocking connection attempt does not necessarily react to the interrupt of a cancelled job
                awaitUninterruptibly(releaseConnection);
                return connection;
            }
        };
        handler.setCallback(callback);
        // the thing is a CoAP thing, which is not polled right after the connection is created. Lenient, as the poll
        // that asks for it runs asynchronously and may be cancelled first.
        lenient().when(connection.isPushingStatus()).thenReturn(true);
    }

    /**
     * Makes the connection report the configuration it was created with, as it does once the device sent data.
     */
    private void stubConnectionConfig(PhilipsAirAPIConnection connection) {
        when(connection.getConfig()).thenReturn(new PhilipsAirConfiguration());
    }

    @AfterEach
    public void tearDown() {
        // released first, so that no thread of the handler keeps waiting, which is not recorded as a timeout
        releaseConnection.countDown();
        finishRequest.countDown();
        if (handler != null) {
            handler.dispose();
        }
        assertEquals(List.of(), List.copyOf(latchTimeouts), "a latch was not released by the test");
    }

    /**
     * Waits for the latch on a thread of the handler, where a failure would not fail the test. A timeout is recorded
     * and asserted by {@link #tearDown()}.
     */
    private void awaitUninterruptibly(CountDownLatch latch) {
        boolean interrupted = false;
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
        try {
            while (true) {
                try {
                    if (!latch.await(Math.max(0, deadline - System.nanoTime()), TimeUnit.NANOSECONDS)) {
                        latchTimeouts.add("timed out waiting for " + latch);
                    }
                    return;
                } catch (InterruptedException e) {
                    interrupted = true;
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private Map<String, String> initializeWithStoredProfile(String storedHost) {
        Configuration config = new Configuration();
        config.put(PhilipsAirConfiguration.CONFIG_HOST, "1.1.1.1");
        Thing thing = ThingBuilder.create(THING_TYPE_COAP, THING_UID).withConfiguration(config)
                .withProperties(Map.of(PROPERTY_DEVICE_PROFILE, "UNICORN", PROPERTY_DEVICE_PROFILE_HOST, storedHost))
                .build();
        PhilipsAirHandler storedHandler = new PhilipsAirHandler(thing, httpClient, stateDescriptionProvider) {
            @Override
            PhilipsAirAPIConnection createConnection(PhilipsAirConfiguration config) {
                return connection;
            }
        };
        storedHandler.setCallback(callback);
        try {
            storedHandler.initialize();
            return Map.copyOf(storedHandler.getThing().getProperties());
        } finally {
            storedHandler.dispose();
        }
    }

    @Test
    public void storedProfileIsKeptForTheSameHost() {
        assertEquals("UNICORN", initializeWithStoredProfile("1.1.1.1").get(PROPERTY_DEVICE_PROFILE));
    }

    @Test
    public void storedProfileIsDroppedWhenTheHostChanged() {
        assertNull(initializeWithStoredProfile("2.2.2.2").get(PROPERTY_DEVICE_PROFILE));
    }

    @Test
    public void disposeReleasesConnection() throws Exception {
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());
        handler.handleCommand(POWER_CHANNEL, OnOffType.ON);
        verify(connection, timeout(10000)).sendCommand(any(), any());

        handler.dispose();

        verify(connection).dispose();
        clearInvocations(connection);
        handler.handleCommand(POWER_CHANNEL, OnOffType.OFF);
        verify(connection, never()).sendCommand(any(), any());
    }

    @Test
    public void failedCommandKeepsLastState() throws Exception {
        when(callback.isChannelLinked(POWER_CHANNEL)).thenReturn(true);
        stubConnectionConfig(connection);
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(new Gson().fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class));
        when(connection.sendCommand(any(), any())).thenReturn(null);
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());
        // the device pushes its status, so the status is only requested by the refresh
        handler.handleCommand(POWER_CHANNEL, RefreshType.REFRESH);
        verify(callback, timeout(10000)).stateUpdated(POWER_CHANNEL, OnOffType.ON);

        clearInvocations(callback);
        handler.handleCommand(POWER_CHANNEL, OnOffType.OFF);

        verify(connection, timeout(10000)).sendCommand(any(), any());
        // the last known state is published again
        verify(callback, timeout(10000)).stateUpdated(POWER_CHANNEL, OnOffType.ON);
        verify(callback, never()).stateUpdated(POWER_CHANNEL, UnDefType.NULL);
        handler.dispose();
    }

    @Test
    public void pushedDataIsOnlyAcceptedFromActiveConnection() throws Exception {
        when(callback.isChannelLinked(POWER_CHANNEL)).thenReturn(true);
        stubConnectionConfig(connection);
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(new Gson().fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class));
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());

        handler.dataReceived(mock(PhilipsAirAPIConnection.class));
        verify(callback, never()).stateUpdated(any(), any());

        handler.dataReceived(connection);
        verify(callback, timeout(10000)).stateUpdated(POWER_CHANNEL, OnOffType.ON);

        handler.dispose();
        clearInvocations(callback);
        handler.dataReceived(connection);
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void cachedDeviceInfoIsOnlyDiscardedWhenTheHostChanges() throws Exception {
        Gson gson = new Gson();
        stubConnectionConfig(connection);
        when(connection.getAirPurifierDevice(any()))
                .thenReturn(gson.fromJson("{\"modelid\":\"AC2889/10\"}", PhilipsAirPurifierDeviceDTO.class));
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(gson.fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class));
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());
        handler.updateData(connection);
        verify(connection, times(1)).getAirPurifierDevice(any());
        handler.dispose();

        // initializing again with the same host keeps the info of the device
        handler.initialize();
        waitForAssert(() -> verify(connection, times(2)).ensureConnected());
        handler.updateData(connection);
        verify(connection, times(1)).getAirPurifierDevice(any());
        handler.dispose();

        handler.handleConfigurationUpdate(Map.of(PhilipsAirConfiguration.CONFIG_HOST, "2.2.2.2"));
        handler.initialize();
        waitForAssert(() -> verify(connection, times(3)).ensureConnected());
        handler.updateData(connection);
        verify(connection, times(2)).getAirPurifierDevice(any());
        handler.dispose();
    }

    @Test
    public void failedPollSetsOfflineAndRefreshRecovers() throws Exception {
        when(callback.isChannelLinked(POWER_CHANNEL)).thenReturn(true);
        stubConnectionConfig(connection);
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(new Gson().fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class));
        doThrow(new IllegalStateException("socket closed")).when(connection).ensureConnected();
        releaseConnection.countDown();

        handler.initialize();

        verify(callback, timeout(10000)).statusUpdated(any(Thing.class),
                eq(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "socket closed")));
        // the refresh job keeps running, the next update is not influenced by the failed one
        handler.handleCommand(POWER_CHANNEL, RefreshType.REFRESH);
        verify(callback, timeout(10000)).statusUpdated(any(Thing.class),
                eq(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null)));
        verify(callback).stateUpdated(POWER_CHANNEL, OnOffType.ON);
    }

    @Test
    public void failedPollWithoutMessageSetsOfflineWithTheDefaultDescription() {
        doThrow(new IllegalStateException()).when(connection).ensureConnected();
        releaseConnection.countDown();

        handler.initialize();

        verify(callback, timeout(10000)).statusUpdated(any(Thing.class), eq(new ThingStatusInfo(ThingStatus.OFFLINE,
                ThingStatusDetail.COMMUNICATION_ERROR, "@text/offline.communication-error.no-response")));
    }

    @Test
    public void noDataSetsOffline() {
        handler.updateData(connection);

        ArgumentCaptor<ThingStatusInfo> statusCaptor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback).statusUpdated(any(Thing.class), statusCaptor.capture());
        assertEquals(ThingStatus.OFFLINE, statusCaptor.getValue().getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, statusCaptor.getValue().getStatusDetail());
    }

    @Test
    public void noStatusIsPublishedAfterDispose() throws Exception {
        blockStatusRequest();
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());

        CompletableFuture<Void> refresh = CompletableFuture.runAsync(() -> handler.updateData(connection));
        assertTrue(requestStarted.await(10, TimeUnit.SECONDS));
        handler.dispose();
        clearInvocations(callback);
        finishRequest.countDown();

        refresh.get(10, TimeUnit.SECONDS);
        verify(callback, never()).statusUpdated(any(), any());
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void noStateOfOldConnectionIsPublishedAfterReinitialize() throws Exception {
        // only used if the state of the old connection is published, which is what the test must detect
        lenient().when(callback.isChannelLinked(POWER_CHANNEL)).thenReturn(true);
        blockStatusRequest();
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());

        CompletableFuture<Void> refresh = CompletableFuture.runAsync(() -> handler.updateData(connection));
        assertTrue(requestStarted.await(10, TimeUnit.SECONDS));
        handler.dispose();
        handler.initialize();
        verify(connection, timeout(10000).times(2)).ensureConnected();
        clearInvocations(callback);
        finishRequest.countDown();

        refresh.get(10, TimeUnit.SECONDS);
        verify(callback, never()).statusUpdated(any(), any());
        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    public void connectionCreatedBeforeReinitializeIsNotUsed() throws Exception {
        replacementConnection = secondConnection;
        lenient().when(secondConnection.isPushingStatus()).thenReturn(true);
        handler.initialize();
        assertTrue(connectionCreated.await(10, TimeUnit.SECONDS));

        handler.dispose();
        handler.initialize();
        // the new generation gets its own connection, while the first one is still being created
        waitForAssert(() -> verify(secondConnection).ensureConnected());
        releaseConnection.countDown();

        waitForAssert(() -> verify(connection).dispose());
        verify(connection, never()).ensureConnected();
        handler.handleCommand(POWER_CHANNEL, OnOffType.ON);
        verify(secondConnection, timeout(10000)).sendCommand(any(), any());
        verify(connection, never()).sendCommand(any(), any());
        verify(secondConnection, never()).dispose();
    }

    private void blockStatusRequest() throws PhilipsAirAPIException {
        when(connection.getAirPurifierStatus(any())).thenAnswer(invocation -> {
            requestStarted.countDown();
            awaitUninterruptibly(finishRequest);
            return new Gson().fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class);
        });
    }

    @Test
    public void connectionCreatedAfterDisposeIsReleased() throws Exception {
        handler.initialize();
        assertTrue(connectionCreated.await(10, TimeUnit.SECONDS));

        handler.dispose();
        releaseConnection.countDown();

        waitForAssert(() -> verify(connection).dispose());
        handler.handleCommand(POWER_CHANNEL, OnOffType.OFF);
        verify(connection, never()).sendCommand(any(), any());
    }
}
