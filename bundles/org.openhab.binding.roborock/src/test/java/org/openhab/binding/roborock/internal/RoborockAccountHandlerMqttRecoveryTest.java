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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.net.SocketException;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests MQTT connection failure, recovery, and cancellation without network access.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class RoborockAccountHandlerMqttRecoveryTest {
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
    private final Bridge bridge = mock(Bridge.class);
    private final Storage<String> storage = mock();
    private final Queue<Runnable> submitted = new ArrayDeque<>();
    private final ScheduledFuture<?> retryFuture = mock(ScheduledFuture.class);
    private final MockedStatic<ThreadPoolManager> threadPools;
    private final MockedConstruction<RoborockWebTargets> webTargets;
    private @Nullable MockedConstruction<MqttClient> clients;
    private final MqttInboundLivenessWatchdog watchdog = mock(MqttInboundLivenessWatchdog.class);
    private final RoborockAccountHandler handler;

    RoborockAccountHandlerMqttRecoveryTest() {
        threadPools = mockStatic(ThreadPoolManager.class);
        threadPools.when(() -> ThreadPoolManager.getScheduledPool("thingHandler")).thenReturn(scheduler);
        webTargets = mockConstruction(RoborockWebTargets.class);
        when(scheduler.submit(any(Runnable.class))).thenAnswer(invocation -> {
            submitted.add(invocation.getArgument(0));
            return mock(Future.class);
        });
        doReturn(retryFuture).when(scheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
        when(bridge.getUID()).thenReturn(new ThingUID("roborock:account:test"));
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of("email", "test@example.invalid")));
        when(storage.get("token")).thenReturn("test-token");
        when(storage.get("rriot")).thenReturn("""
                {"u":"test-user","s":"test-secret","k":"test-key",
                 "r":{"a":"https://api.example.invalid","m":"ssl://mqtt.example.invalid:8883"}}
                """);
        handler = new RoborockAccountHandler(bridge, storage, mock(HttpClient.class), watchdog);
        handler.setCallback(callback);
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
        var clients = this.clients;
        if (clients != null) {
            clients.close();
        }
        webTargets.close();
        threadPools.close();
    }

    @Test
    void failedInitialConnectionIsClosedAndRetriedUntilRecovery() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> {
            if (context.getCount() <= 2) {
                doThrow(new MqttException(new SocketException("Network is unreachable"))).when(client)
                        .connect(any(MqttConnectOptions.class));
            }
        });
        this.clients = clients;
        initializeAndConnect();
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        verify(clients.constructed().getFirst(), atLeastOnce()).close();

        var retry = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(retry.capture(), eq(60L), eq(TimeUnit.SECONDS));
        retry.getValue().run();
        verify(clients.constructed().get(1)).close();
        verify(scheduler, times(2)).schedule(retry.capture(), eq(60L), eq(TimeUnit.SECONDS));
        clearInvocations(callback);
        retry.getValue().run();

        assertEquals(3, clients.constructed().size());
        verify(clients.constructed().get(2)).connect(any(MqttConnectOptions.class));
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        verify(scheduler, times(2)).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void disposeCancelsPendingRetryAndPreventsAnotherConnection() throws Exception {
        var clients = mockConstruction(MqttClient.class,
                (client, context) -> doThrow(new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR))
                        .when(client).connect(any(MqttConnectOptions.class)));
        this.clients = clients;
        initializeAndConnect();
        var retry = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(retry.capture(), eq(60L), eq(TimeUnit.SECONDS));

        handler.dispose();
        verify(retryFuture).cancel(true);
        retry.getValue().run();
        assertEquals(1, clients.constructed().size());
    }

    @Test
    void failureAfterDisposalDoesNotScheduleRetry() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> doAnswer(invocation -> {
            handler.dispose();
            throw new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR);
        }).when(client).connect(any(MqttConnectOptions.class)));
        this.clients = clients;
        initializeAndConnect();

        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void authenticationFailureDoesNotScheduleRetry() throws Exception {
        var clients = mockConstruction(MqttClient.class,
                (client, context) -> doThrow(new MqttSecurityException(MqttException.REASON_CODE_FAILED_AUTHENTICATION))
                        .when(client).connect(any(MqttConnectOptions.class)));
        this.clients = clients;
        initializeAndConnect();

        verify(clients.constructed().getFirst(), atLeastOnce()).close();
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void connectionLossMarksBridgeOfflineAndReconnectResubscribes() throws Exception {
        var clients = mockConstruction(MqttClient.class);
        this.clients = clients;
        initializeAndConnect();
        clearInvocations(callback);

        handler.connectionLost(new MqttException(new SocketException("Connection reset")));
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
        clearInvocations(callback);
        handler.connectComplete(true, "ssl://mqtt.example.invalid:8883");
        verify(clients.constructed().getFirst()).subscribe(anyString(), eq(0));
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void expiredSessionDuringConnectionFailureDoesNotScheduleRetry() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> doAnswer(invocation -> {
            handler.handleSessionExpired();
            throw new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR);
        }).when(client).connect(any(MqttConnectOptions.class)));
        this.clients = clients;
        initializeAndConnect();

        verify(clients.constructed().getFirst(), atLeastOnce()).close();
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void invalidMqttUriDoesNotScheduleRetry() {
        when(storage.get("rriot")).thenReturn("""
                {"u":"test-user","s":"test-secret","k":"test-key",
                 "r":{"a":"https://api.example.invalid","m":"invalid uri"}}
                """);
        initializeAndConnect();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void obsoleteFailureDoesNotCloseReplacementOrOverwriteItsStatus(boolean authenticationFailure) throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> {
            if (context.getCount() == 1) {
                doAnswer(invocation -> {
                    handler.dispose();
                    initializeAndConnect();
                    if (authenticationFailure) {
                        throw new MqttSecurityException(MqttException.REASON_CODE_FAILED_AUTHENTICATION);
                    }
                    throw new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR);
                }).when(client).connect(any(MqttConnectOptions.class));
            }
        });
        this.clients = clients;
        initializeAndConnect();

        assertEquals(2, clients.constructed().size());
        verify(clients.constructed().get(1)).connect(any(MqttConnectOptions.class));
        verify(clients.constructed().get(1), never()).close();
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        handler.connectComplete(true, "ssl://mqtt.example.invalid:8883");
        verify(clients.constructed().get(1)).subscribe(anyString(), eq(0));
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void obsoleteSuccessDoesNotOverwriteReplacementFailure() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> {
            if (context.getCount() == 1) {
                doAnswer(invocation -> {
                    handler.dispose();
                    initializeAndConnect();
                    return null;
                }).when(client).connect(any(MqttConnectOptions.class));
            } else {
                doThrow(new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR)).when(client)
                        .connect(any(MqttConnectOptions.class));
            }
        });
        this.clients = clients;
        initializeAndConnect();

        assertEquals(2, clients.constructed().size());
        verify(clients.constructed().getFirst(), atLeastOnce()).close();
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        verify(scheduler).schedule(any(Runnable.class), eq(60L), eq(TimeUnit.SECONDS));
    }

    @Test
    void reinitializationBeforeCredentialsReloadDoesNotReviveOldAttempt() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> doAnswer(invocation -> {
            handler.dispose();
            handler.initialize();
            throw new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR);
        }).when(client).connect(any(MqttConnectOptions.class)));
        this.clients = clients;
        initializeAndConnect();

        verifyStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void replacementInstalledDuringCleanupRemainsUsable() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> {
            if (context.getCount() == 1) {
                doThrow(new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR)).when(client)
                        .connect(any(MqttConnectOptions.class));
                doAnswer(invocation -> {
                    handler.dispose();
                    initializeAndConnect();
                    return null;
                }).when(client).close();
            }
        });
        this.clients = clients;
        initializeAndConnect();

        assertEquals(2, clients.constructed().size());
        verify(clients.constructed().get(1), never()).close();
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        handler.connectComplete(true, "ssl://mqtt.example.invalid:8883");
        verify(clients.constructed().get(1)).subscribe(anyString(), eq(0));
        verify(scheduler, never()).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));
    }

    @Test
    void watchdogFromPreviousSessionDoesNotDisconnectReplacement() throws Exception {
        var clients = mockConstruction(MqttClient.class);
        this.clients = clients;
        initializeAndConnect();
        when(watchdog.evaluate(any(), anyBoolean())).thenAnswer(invocation -> {
            handler.dispose();
            initializeAndConnect();
            return MqttInboundLivenessWatchdog.Decision.STALE;
        });

        handler.checkMqttInboundLiveness();

        assertEquals(2, clients.constructed().size());
        verify(clients.constructed().get(1), never()).close();
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void failedWatchdogReplacementIsRetried() throws Exception {
        var clients = mockConstruction(MqttClient.class, (client, context) -> {
            if (context.getCount() == 2) {
                doThrow(new MqttException(MqttException.REASON_CODE_SERVER_CONNECT_ERROR)).when(client)
                        .connect(any(MqttConnectOptions.class));
            }
        });
        this.clients = clients;
        initializeAndConnect();
        when(watchdog.evaluate(any(), anyBoolean())).thenReturn(MqttInboundLivenessWatchdog.Decision.STALE);

        handler.checkMqttInboundLiveness();

        verify(clients.constructed().get(1)).close();
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        var retry = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(retry.capture(), eq(60L), eq(TimeUnit.SECONDS));
        retry.getValue().run();
        assertEquals(3, clients.constructed().size());
        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    private void initializeAndConnect() {
        handler.initialize();
        submitted.remove().run();
        submitted.remove().run();
    }

    private void verifyStatus(ThingStatus status, ThingStatusDetail detail) {
        var updates = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeastOnce()).statusUpdated(eq(bridge), updates.capture());
        var last = updates.getAllValues().getLast();
        assertEquals(status, last.getStatus());
        assertEquals(detail, last.getStatusDetail());
    }
}
