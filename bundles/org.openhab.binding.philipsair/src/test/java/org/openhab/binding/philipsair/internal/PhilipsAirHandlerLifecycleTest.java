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
import static org.mockito.Mockito.*;
import static org.openhab.binding.philipsair.internal.PhilipsAirBindingConstants.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIConnection;
import org.openhab.binding.philipsair.internal.connection.PhilipsAirAPIException;
import org.openhab.binding.philipsair.internal.model.PhilipsAirPurifierDataDTO;
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
@MockitoSettings(strictness = Strictness.LENIENT)
public class PhilipsAirHandlerLifecycleTest extends JavaTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_COAP, "test");
    private static final ChannelUID POWER_CHANNEL = new ChannelUID(THING_UID, CONTROLS, POWER);

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) PhilipsAirAPIConnection connection;
    private @Mock @NonNullByDefault({}) HttpClient httpClient;

    private final CountDownLatch connectionCreated = new CountDownLatch(1);
    private final CountDownLatch releaseConnection = new CountDownLatch(1);

    private @NonNullByDefault({}) PhilipsAirHandler handler;

    @BeforeEach
    public void setUp() {
        Configuration config = new Configuration();
        config.put(PhilipsAirConfiguration.CONFIG_HOST, "1.1.1.1");
        Thing thing = ThingBuilder.create(THING_TYPE_COAP, THING_UID).withConfiguration(config)
                .withChannel(ChannelBuilder.create(POWER_CHANNEL, "Switch").build()).build();
        handler = new PhilipsAirHandler(thing, httpClient) {
            @Override
            PhilipsAirAPIConnection createConnection(PhilipsAirConfiguration config) {
                connectionCreated.countDown();
                try {
                    releaseConnection.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                return connection;
            }
        };
        handler.setCallback(callback);
        when(connection.getConfig()).thenReturn(new PhilipsAirConfiguration());
        // the thing is a CoAP thing, which is not polled right after the connection is created
        when(connection.isPushingStatus()).thenReturn(true);
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
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(new Gson().fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class));
        when(connection.sendCommand(any(), any())).thenReturn(null);
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> {
            handler.handleCommand(POWER_CHANNEL, RefreshType.REFRESH);
            verify(callback, atLeastOnce()).stateUpdated(POWER_CHANNEL, OnOffType.ON);
        });

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
        when(connection.getAirPurifierStatus(any()))
                .thenReturn(new Gson().fromJson("{\"pwr\":\"1\"}", PhilipsAirPurifierDataDTO.class));
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());

        handler.dataReceived(mock(PhilipsAirAPIConnection.class));
        verify(callback, never()).stateUpdated(any(), any());

        handler.dataReceived(connection);
        verify(callback).stateUpdated(POWER_CHANNEL, OnOffType.ON);

        handler.dispose();
        clearInvocations(callback);
        handler.dataReceived(connection);
        verify(callback, never()).stateUpdated(any(), any());
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
        CountDownLatch requestStarted = new CountDownLatch(1);
        CountDownLatch finishRequest = new CountDownLatch(1);
        when(connection.getAirPurifierStatus(any())).thenAnswer(invocation -> {
            requestStarted.countDown();
            finishRequest.await(10, TimeUnit.SECONDS);
            throw new PhilipsAirAPIException("interrupted");
        });
        releaseConnection.countDown();
        handler.initialize();
        waitForAssert(() -> verify(connection).ensureConnected());

        handler.handleCommand(POWER_CHANNEL, RefreshType.REFRESH);
        assertTrue(requestStarted.await(10, TimeUnit.SECONDS));
        handler.dispose();
        clearInvocations(callback);
        finishRequest.countDown();

        // the refresh completes asynchronously, without an observable event when nothing is published
        verify(callback, after(1000).never()).statusUpdated(any(), any());
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
