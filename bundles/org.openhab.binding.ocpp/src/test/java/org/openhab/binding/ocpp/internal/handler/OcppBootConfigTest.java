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
package org.openhab.binding.ocpp.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.ocpp.internal.config.OcppServerConfiguration;
import org.openhab.binding.ocpp.internal.transport.OcppTransport;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.ChangeConfigurationRequest;
import eu.chargetime.ocpp.model.core.ConfigurationStatus;
import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.GetConfigurationRequest;

/**
 * Tests the configuration a charge point receives after it boots and the outbound-request
 * serialization around it.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class OcppBootConfigTest {

    private static final ThingUID SERVER_UID = new ThingUID(THING_TYPE_SERVER, "server");
    private static final ThingUID CP_UID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");

    private @NonNullByDefault({}) OcppChargePointHandler handler;
    private @NonNullByDefault({}) OcppTransport transport;
    private @NonNullByDefault({}) OcppServerConfiguration serverConfig;
    private final List<ChangeConfigurationRequest> sent = new ArrayList<>();

    @BeforeEach
    void setUp() {
        serverConfig = new OcppServerConfiguration();
        serverConfig.meterValuesData = "";
        serverConfig.disableRemoteTxAuthorization = true;

        transport = mock(OcppTransport.class);
        acceptEverything();

        OcppServerBridgeHandler serverHandler = mock(OcppServerBridgeHandler.class);
        when(serverHandler.getServerConfig()).thenReturn(serverConfig);
        when(serverHandler.getTransport()).thenReturn(transport);

        Bridge serverThing = mock(Bridge.class);
        when(serverThing.getHandler()).thenReturn(serverHandler);

        Bridge cpThing = mock(Bridge.class);
        when(cpThing.getUID()).thenReturn(CP_UID);
        when(cpThing.getBridgeUID()).thenReturn(SERVER_UID);
        when(cpThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(cpThing.getConfiguration())
                .thenReturn(new Configuration(Map.of("chargePointId", "charger", "configSettleSeconds", 0)));

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(SERVER_UID)).thenReturn(serverThing);

        handler = new OcppChargePointHandler(cpThing);
        handler.setCallback(callback);
        handler.initialize();
        handler.onConnected(UUID.randomUUID());
    }

    private void acceptEverything() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            record(invocation.getArgument(1));
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        });
    }

    private void record(Request request) {
        if (request instanceof ChangeConfigurationRequest change) {
            synchronized (sent) {
                sent.add(change);
            }
        }
    }

    private List<String> sentValuesFor(String key) {
        synchronized (sent) {
            return sent.stream().filter(r -> key.equals(r.getKey())).map(ChangeConfigurationRequest::getValue).toList();
        }
    }

    private void awaitReady() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (!handler.isReady() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(handler.isReady(), "charge point should have become ready");
    }

    @Test
    void outboundRequestsAreSerializedToOneCallInFlight() throws InterruptedException {
        // OCPP-J keeps one CALL outstanding: the second send waits until the first settles.
        handler.onHeartbeat();
        awaitReady();

        ChangeConfigurationRequest first = new ChangeConfigurationRequest("First", "1");
        ChangeConfigurationRequest second = new ChangeConfigurationRequest("Second", "2");
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstResult = new CompletableFuture<>();
        when(transport.send(any(), eq(first))).thenReturn(firstResult);

        handler.send(first);
        handler.send(second);

        verify(transport, timeout(1000)).send(any(), eq(first));
        verify(transport, org.mockito.Mockito.after(500).never()).send(any(), eq(second));

        firstResult.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        verify(transport, timeout(1000)).send(any(), eq(second));
    }

    @Test
    void theStatusProbePathIsSerializedToo() {
        // sendNow bypasses the readiness gate but must still queue behind an in-flight CALL.
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstProbe = new CompletableFuture<>();
        when(transport.send(any(), any())).thenReturn(firstProbe);

        handler.sendNow(new eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest(
                eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType.StatusNotification));
        handler.sendNow(new eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest(
                eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType.StatusNotification));

        verify(transport, timeout(1000).times(1)).send(any(), any());
        verify(transport, org.mockito.Mockito.after(400).times(1)).send(any(), any());
        firstProbe.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        verify(transport, timeout(1000).times(2)).send(any(), any());
    }

    @Test
    void aReconnectDoesNotWedgeTheDispatcherBehindAnInFlightRequestFromTheOldSession() throws InterruptedException {
        // The embedded library never completes the old promise when its session closes, so the
        // dispatcher must abandon the in-flight request on the session change and keep draining.
        handler.onHeartbeat();
        awaitReady();

        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstNeverAnswers = new CompletableFuture<>();
        ChangeConfigurationRequest first = new ChangeConfigurationRequest("First", "1");
        when(transport.send(any(), eq(first))).thenReturn(firstNeverAnswers);
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstResult = handler.send(first)
                .toCompletableFuture();
        verify(transport, timeout(1000)).send(any(), eq(first));

        handler.onConnected(UUID.randomUUID());
        assertTrue(firstResult.isCompletedExceptionally(),
                "the in-flight request must be abandoned on the session change");

        handler.onHeartbeat();
        awaitReady();
        ChangeConfigurationRequest second = new ChangeConfigurationRequest("Second", "2");
        handler.send(second);
        verify(transport, timeout(1000)).send(any(), eq(second));

        assertFalse(firstNeverAnswers.isDone());
    }

    @Test
    void aLateCompletionOfAnAbandonedRequestDoesNotDisturbTheNewSessionChain() throws InterruptedException {
        // The request-timeout reaper can complete an abandoned request's future late, on a scheduler
        // thread; a drain-chain epoch keeps that late completion from forking a second CALL.
        handler.onHeartbeat();
        awaitReady();

        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> f1 = new CompletableFuture<>();
        ChangeConfigurationRequest r1 = new ChangeConfigurationRequest("R1", "1");
        when(transport.send(any(), eq(r1))).thenReturn(f1);
        handler.send(r1);
        verify(transport, timeout(1000)).send(any(), eq(r1));

        handler.onConnected(UUID.randomUUID());
        handler.onHeartbeat();
        awaitReady();

        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> f2 = new CompletableFuture<>();
        ChangeConfigurationRequest r2 = new ChangeConfigurationRequest("R2", "2");
        when(transport.send(any(), eq(r2))).thenReturn(f2);
        handler.send(r2);
        verify(transport, timeout(1000)).send(any(), eq(r2));

        f1.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        ChangeConfigurationRequest r3 = new ChangeConfigurationRequest("R3", "3");
        handler.send(r3);
        verify(transport, org.mockito.Mockito.after(500).never()).send(any(), eq(r3));

        f2.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        verify(transport, timeout(1000)).send(any(), eq(r3));
    }

    @Test
    void configurationIsSentWhenAChargerBoots() {
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(2000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
        assertEquals(List.of("false"), sentValuesFor("AuthorizeRemoteTxRequests"));
    }

    @Test
    void anAcceptedConfigurationIsNotSentAgainOnTheNextBoot() {
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(2000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(1000).times(1)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void aRejectedConfigurationIsRetriedOnTheNextBoot() {
        // A Rejected ChangeConfiguration completes normally but has not applied, so the burst must not
        // latch on it.
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            record(invocation.getArgument(1));
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Rejected));
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(2000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(2000).times(2)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void aChargerThatNeverAnswersIsNotRetriedForever() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            record(invocation.getArgument(1));
            return CompletableFuture.failedFuture(new IllegalStateException("no answer"));
        });

        for (int boot = 0; boot < 6; boot++) {
            handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        }

        verify(transport, timeout(3000).atLeast(1)).send(any(), any());
        synchronized (sent) {
            assertTrue(sent.size() <= 3, "attempts must be capped, but saw " + sent.size());
        }
    }

    @Test
    void aSettleDelayedBootConfigDoesNotRunAgainstAReplacementSession() {
        // A bare WebSocket reconnect (session B) sends no fresh BootNotification; A's delayed burst
        // must key off the session captured when scheduled, not the current one.
        OcppServerBridgeHandler serverHandler = mock(OcppServerBridgeHandler.class);
        when(serverHandler.getServerConfig()).thenReturn(serverConfig);
        when(serverHandler.getTransport()).thenReturn(transport);
        Bridge serverThing = mock(Bridge.class);
        when(serverThing.getHandler()).thenReturn(serverHandler);
        Bridge cpThing = mock(Bridge.class);
        when(cpThing.getUID()).thenReturn(CP_UID);
        when(cpThing.getBridgeUID()).thenReturn(SERVER_UID);
        when(cpThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(cpThing.getConfiguration())
                .thenReturn(new Configuration(Map.of("chargePointId", "charger", "configSettleSeconds", 1)));
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(SERVER_UID)).thenReturn(serverThing);
        OcppChargePointHandler delayed = new OcppChargePointHandler(cpThing);
        delayed.setCallback(callback);
        delayed.initialize();
        delayed.onConnected(UUID.randomUUID());

        delayed.onBootNotification(new BootNotificationRequest("vendor", "model"));
        delayed.onConnected(UUID.randomUUID());

        verify(transport, org.mockito.Mockito.after(2000).never()).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void anAbandonedBootSequenceDoesNotContinueOnAReplacementSession() {
        serverConfig.extraConfig = List.of("VendorKey=42");
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstStep = new CompletableFuture<>();
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request request = invocation.getArgument(1);
            record(request);
            if (request instanceof GetConfigurationRequest) {
                return CompletableFuture.completedFuture(new GetConfigurationConfirmation());
            }
            return firstStep;
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        handler.onConnected(UUID.randomUUID());
        handler.onHeartbeat();

        firstStep.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        verify(transport, org.mockito.Mockito.after(1500).never()).send(any(),
                eq(new ChangeConfigurationRequest("VendorKey", "42")));

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(), eq(new ChangeConfigurationRequest("VendorKey", "42")));
    }

    @Test
    void aRequestQueuedOnOneSessionIsNotSentOnItsSuccessor() {
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> queued = handler
                .send(new ChangeConfigurationRequest("Key", "1")).toCompletableFuture();
        assertFalse(queued.isDone());

        handler.onConnected(UUID.randomUUID());
        assertTrue(queued.isCompletedExceptionally(), "a request queued on a superseded session must fail");

        handler.onHeartbeat();
        verify(transport, org.mockito.Mockito.after(1500).never()).send(any(),
                eq(new ChangeConfigurationRequest("Key", "1")));
    }

    @Test
    void aChangedConfigurationIsSentAgainOnTheNextBoot() {
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, org.mockito.Mockito.after(1500).times(1)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        // The applied latch is keyed on the effective settings, so a changed value resends the burst.
        serverConfig.extraConfig = List.of("VendorKey=42");
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(), eq(new ChangeConfigurationRequest("VendorKey", "42")));
        verify(transport, timeout(3000).times(2)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void nothingIsSentWhileTheBootNotificationIsBeingHandled() {
        // The library sends the BootNotification confirmation only after the event handler returns, so
        // anything sent from inside the handler would reach the charger before its boot answer.
        OcppConnectorHandler connector = mock(OcppConnectorHandler.class);
        handler.registerConnector(1, connector);

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, org.mockito.Mockito.never()).send(any(), any());
        verify(connector, org.mockito.Mockito.never()).onChargePointReady();

        verify(transport, timeout(3000).atLeastOnce()).send(any(), any());
        verify(connector, timeout(3000)).onChargePointReady();
    }

    @Test
    void aListReducedForSampledDataDoesNotNarrowAlignedData() {
        // Sampled and aligned data may support different measurand sets, so a rejection negotiating one
        // key must not shrink the other's starting list.
        serverConfig.meterValuesData = "Energy.Active.Import.Register,Power.Active.Import,Temperature";
        serverConfig.disableRemoteTxAuthorization = false;
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request request = invocation.getArgument(1);
            record(request);
            boolean rejected = request instanceof ChangeConfigurationRequest change
                    && "MeterValuesSampledData".equals(change.getKey()) && change.getValue() != null
                    && change.getValue().contains("Temperature");
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(
                    rejected ? ConfigurationStatus.Rejected : ConfigurationStatus.Accepted));
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(3000).atLeastOnce()).send(any(),
                argThat(r -> r instanceof ChangeConfigurationRequest c && "MeterValuesAlignedData".equals(c.getKey())));
        List<String> aligned = sentValuesFor("MeterValuesAlignedData");
        assertEquals("Energy.Active.Import.Register,Power.Active.Import,Temperature", aligned.get(0),
                "the aligned negotiation must start from the full configured list");
    }

    @Test
    void aRejectedMeasurandIsDroppedUntilTheChargerAcceptsTheList() {
        serverConfig.meterValuesData = "Energy.Active.Import.Register,Power.Active.Import,Temperature";
        serverConfig.disableRemoteTxAuthorization = false;
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request request = invocation.getArgument(1);
            record(request);
            boolean rejected = request instanceof ChangeConfigurationRequest change && change.getValue() != null
                    && change.getValue().contains("Temperature");
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(
                    rejected ? ConfigurationStatus.Rejected : ConfigurationStatus.Accepted));
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, timeout(3000).atLeast(2)).send(any(), any());
        List<String> tried = sentValuesFor("MeterValuesSampledData");
        assertEquals("Energy.Active.Import.Register,Power.Active.Import,Temperature", tried.get(0));
        assertEquals("Energy.Active.Import.Register,Power.Active.Import", tried.get(tried.size() - 1),
                "the rejected measurand should have been dropped");
    }

    @Test
    void aDefaultBootWithNoStepsHoldsTheStatusRefreshUntilReadiness() {
        serverConfig.disableRemoteTxAuthorization = false;
        serverConfig.meterValuesData = "";
        realConnector(1);

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, org.mockito.Mockito.after(600).never()).send(any(), any());

        handler.onHeartbeat();
        verify(transport, timeout(2000)).send(any(), argThat(OcppBootConfigTest::isStatusTrigger));
    }

    @Test
    void anUndiscoveredConnectorIsTriggeredToAppearAfterBoot() {
        serverConfig.disableRemoteTxAuthorization = false;
        serverConfig.meterValuesData = "";
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request req = invocation.getArgument(1);
            if (req instanceof GetConfigurationRequest) {
                eu.chargetime.ocpp.model.core.KeyValueType count = new eu.chargetime.ocpp.model.core.KeyValueType(
                        "NumberOfConnectors", Boolean.TRUE);
                count.setValue("1");
                GetConfigurationConfirmation conf = new GetConfigurationConfirmation();
                conf.setConfigurationKey(new eu.chargetime.ocpp.model.core.KeyValueType[] { count });
                return CompletableFuture.completedFuture(conf);
            }
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        handler.onHeartbeat();

        verify(transport, timeout(2000)).send(any(), argThat(r -> isStatusTrigger(r) && Integer.valueOf(1)
                .equals(((eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest) r).getConnectorId())));
    }

    @Test
    void learningACardAddsItToTheLocalAuthList() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request req = invocation.getArgument(1);
            if (req instanceof GetConfigurationRequest) {
                eu.chargetime.ocpp.model.core.KeyValueType profiles = new eu.chargetime.ocpp.model.core.KeyValueType(
                        "SupportedFeatureProfiles", Boolean.TRUE);
                profiles.setValue("Core,LocalAuthListManagement,RemoteTrigger");
                GetConfigurationConfirmation conf = new GetConfigurationConfirmation();
                conf.setConfigurationKey(new eu.chargetime.ocpp.model.core.KeyValueType[] { profiles });
                return CompletableFuture.completedFuture(conf);
            }
            if (req instanceof eu.chargetime.ocpp.model.localauthlist.GetLocalListVersionRequest) {
                return CompletableFuture
                        .completedFuture(new eu.chargetime.ocpp.model.localauthlist.GetLocalListVersionConfirmation(0));
            }
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        });

        handler.handleCommand(new org.openhab.core.thing.ChannelUID(CP_UID, "learn-card"),
                org.openhab.core.library.types.OnOffType.ON);
        handler.onAuthorized("RFID-LEARN");
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        handler.onHeartbeat();

        verify(transport, timeout(2000)).send(any(),
                argThat(r -> r instanceof eu.chargetime.ocpp.model.localauthlist.SendLocalListRequest s
                        && s.getUpdateType() == eu.chargetime.ocpp.model.localauthlist.UpdateType.Full
                        && s.getLocalAuthorizationList() != null && s.getLocalAuthorizationList().length == 1
                        && "RFID-LEARN".equals(s.getLocalAuthorizationList()[0].getIdTag())));
    }

    @Test
    void localAuthListIsNotSentWhenTheChargerLacksTheProfile() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request req = invocation.getArgument(1);
            if (req instanceof GetConfigurationRequest) {
                eu.chargetime.ocpp.model.core.KeyValueType profiles = new eu.chargetime.ocpp.model.core.KeyValueType(
                        "SupportedFeatureProfiles", Boolean.TRUE);
                profiles.setValue("Core,RemoteTrigger");
                GetConfigurationConfirmation conf = new GetConfigurationConfirmation();
                conf.setConfigurationKey(new eu.chargetime.ocpp.model.core.KeyValueType[] { profiles });
                return CompletableFuture.completedFuture(conf);
            }
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        });

        handler.handleCommand(new org.openhab.core.thing.ChannelUID(CP_UID, "local-auth-list"),
                new org.openhab.core.library.types.StringType("RFID-A"));
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        handler.onHeartbeat();

        verify(transport, org.mockito.Mockito.after(600).never()).send(any(),
                argThat(r -> r instanceof eu.chargetime.ocpp.model.localauthlist.SendLocalListRequest));
    }

    @Test
    void theLocalAuthListChannelDrivesTheListSentOnBoot() {
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request req = invocation.getArgument(1);
            if (req instanceof GetConfigurationRequest) {
                eu.chargetime.ocpp.model.core.KeyValueType profiles = new eu.chargetime.ocpp.model.core.KeyValueType(
                        "SupportedFeatureProfiles", Boolean.TRUE);
                profiles.setValue("Core,LocalAuthListManagement");
                GetConfigurationConfirmation conf = new GetConfigurationConfirmation();
                conf.setConfigurationKey(new eu.chargetime.ocpp.model.core.KeyValueType[] { profiles });
                return CompletableFuture.completedFuture(conf);
            }
            if (req instanceof eu.chargetime.ocpp.model.localauthlist.GetLocalListVersionRequest) {
                return CompletableFuture
                        .completedFuture(new eu.chargetime.ocpp.model.localauthlist.GetLocalListVersionConfirmation(0));
            }
            return CompletableFuture.completedFuture(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        });

        handler.handleCommand(new org.openhab.core.thing.ChannelUID(CP_UID, "local-auth-list"),
                new org.openhab.core.library.types.StringType("RFID-A, RFID-B"));
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        handler.onHeartbeat();

        verify(transport, timeout(2000)).send(any(),
                argThat(r -> r instanceof eu.chargetime.ocpp.model.localauthlist.SendLocalListRequest s
                        && s.getLocalAuthorizationList() != null && s.getLocalAuthorizationList().length == 2));
    }

    private static boolean isStatusTrigger(Request request) {
        return request instanceof eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest trigger && trigger
                .getRequestedMessage() == eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType.StatusNotification;
    }

    private OcppConnectorHandler realConnector(int connectorId) {
        Bridge cpBridge = mock(Bridge.class);
        when(cpBridge.getHandler()).thenReturn(handler);
        Thing connThing = mock(Thing.class);
        when(connThing.getUID()).thenReturn(new ThingUID(THING_TYPE_CONNECTOR, "server", "charger", "c" + connectorId));
        when(connThing.getThingTypeUID()).thenReturn(THING_TYPE_CONNECTOR);
        when(connThing.getBridgeUID()).thenReturn(CP_UID);
        when(connThing.getConfiguration()).thenReturn(new Configuration(Map.of("connectorId", connectorId)));
        when(connThing.getChannels()).thenReturn(List.of());
        when(connThing.getProperties()).thenReturn(Map.of());
        ThingHandlerCallback connCallback = mock(ThingHandlerCallback.class);
        when(connCallback.getBridge(CP_UID)).thenReturn(cpBridge);
        OcppConnectorHandler connector = new OcppConnectorHandler(connThing);
        connector.setCallback(connCallback);
        connector.initialize();
        return connector;
    }
}
