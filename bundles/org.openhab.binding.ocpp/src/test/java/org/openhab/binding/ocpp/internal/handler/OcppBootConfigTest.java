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
 * Tests the configuration a charger receives after it boots.
 *
 * <p>
 * This is deliberately conservative: a charger that is busy — flushing an offline message queue, for
 * instance — may leave a request unanswered. Such a request fails after the configured timeout (the
 * session itself stays up), so re-sending the configuration on every reconnect is pointless traffic
 * against a struggling charger, which is why it is sent until accepted once for the effective
 * settings and then left alone.
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
        serverConfig.disableRemoteTxAuthorization = true; // exactly one step unless stated otherwise

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
        // A heartbeat makes the charge point ready (nothing queued). Two sends then follow: only the
        // first may reach the transport until it has settled — OCPP-J keeps one CALL outstanding.
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

        // Only once the first CALL settles is the second one transmitted.
        firstResult.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        verify(transport, timeout(1000)).send(any(), eq(second));
    }

    @Test
    void theStatusProbePathIsSerializedToo() {
        // The status-recovery probe (sendNow) bypasses the readiness gate but must still queue behind
        // any in-flight CALL — two probes, as requestConnectorStatuses issues for a two-connector
        // charger, do not go out together.
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
        // Regression: a request still in flight when the charger reconnects must not stall every
        // request to the NEW session until the old one times out. The embedded library never completes
        // the old promise when its session closes, so the dispatcher itself has to abandon the
        // in-flight request on the session change and keep draining — rather than sit with a single
        // CALL slot latched for the whole request timeout.
        handler.onHeartbeat(); // ready on session A (from setUp)
        awaitReady();

        // The first request goes in flight and the transport never answers it — the charger vanished
        // mid-request, the same fault that drops the socket.
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstNeverAnswers = new CompletableFuture<>();
        ChangeConfigurationRequest first = new ChangeConfigurationRequest("First", "1");
        when(transport.send(any(), eq(first))).thenReturn(firstNeverAnswers);
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstResult = handler.send(first)
                .toCompletableFuture();
        verify(transport, timeout(1000)).send(any(), eq(first)); // it is in flight

        // The charger reconnects under a fresh session. The in-flight request must fail at once, not
        // hang until its timeout.
        handler.onConnected(UUID.randomUUID());
        assertTrue(firstResult.isCompletedExceptionally(),
                "the in-flight request must be abandoned on the session change");

        // The new session boots; a request on it must go out immediately, not wait behind the old
        // (never-arriving) answer.
        handler.onHeartbeat();
        awaitReady();
        ChangeConfigurationRequest second = new ChangeConfigurationRequest("Second", "2");
        handler.send(second);
        verify(transport, timeout(1000)).send(any(), eq(second));

        // The old request's transport future was never completed — proof the successor's drain never
        // depended on it.
        assertFalse(firstNeverAnswers.isDone());
    }

    @Test
    void aLateCompletionOfAnAbandonedRequestDoesNotDisturbTheNewSessionChain() throws InterruptedException {
        // After a reconnect abandons an in-flight request, that request's transport future can still
        // complete later — the request-timeout reaper fires on a scheduler thread regardless of the
        // socket. That late completion must be inert: it must neither complete anything on, nor start a
        // second drain against, the NEW session's chain (which by then has its own request in flight).
        // The drain-chain epoch is what makes it a no-op.
        handler.onHeartbeat(); // ready on session A
        awaitReady();

        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> f1 = new CompletableFuture<>();
        ChangeConfigurationRequest r1 = new ChangeConfigurationRequest("R1", "1");
        when(transport.send(any(), eq(r1))).thenReturn(f1);
        handler.send(r1);
        verify(transport, timeout(1000)).send(any(), eq(r1)); // R1 in flight on A

        // Reconnect: R1 is abandoned; the new session boots and becomes ready.
        handler.onConnected(UUID.randomUUID());
        handler.onHeartbeat();
        awaitReady();

        // A request goes in flight on the NEW session.
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> f2 = new CompletableFuture<>();
        ChangeConfigurationRequest r2 = new ChangeConfigurationRequest("R2", "2");
        when(transport.send(any(), eq(r2))).thenReturn(f2);
        handler.send(r2);
        verify(transport, timeout(1000)).send(any(), eq(r2)); // R2 in flight on the new session

        // The abandoned R1's transport future completes late (the reaper). It must be a no-op: it must
        // not release R3 (that would be a second, concurrent CALL alongside R2).
        f1.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        ChangeConfigurationRequest r3 = new ChangeConfigurationRequest("R3", "3");
        handler.send(r3);
        verify(transport, org.mockito.Mockito.after(500).never()).send(any(), eq(r3)); // still behind R2

        // Only when R2 settles does R3 go — the one-CALL-at-a-time chain was never forked.
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

        // Still exactly one send: repeating it is what turns a reconnect into a connect/configure/
        // drop loop on a charger that cannot answer promptly.
        verify(transport, timeout(1000).times(1)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void aRejectedConfigurationIsRetriedOnTheNextBoot() {
        // A ChangeConfiguration answered Rejected completes normally but has not applied, so the
        // burst must not latch on it — the key is attempted again when the charger next boots.
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
        // configSettleSeconds > 0: session A boots and schedules its burst for later. Before the
        // delay expires the charger reconnects as session B (a bare WebSocket reconnect that sends no
        // fresh BootNotification). A's delayed task must recognise its originating session is gone —
        // captured when scheduled, not read when it runs — and not configure B.
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
        delayed.onConnected(UUID.randomUUID()); // reconnect as a new session during the settle delay

        // The 1s-delayed burst must never fire against the replacement session.
        verify(transport, org.mockito.Mockito.after(2000).never()).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void anAbandonedBootSequenceDoesNotContinueOnAReplacementSession() {
        // A step of session A's boot-configuration sequence is answered only after the charger has
        // reconnected as session B (a timeout resolves this way too). The old sequence must stop:
        // advancing it would transmit its remaining stale steps through B, interleaved with the
        // sequence B's own boot runs, and could latch its fingerprint as applied.
        serverConfig.vendorConfig = List.of("VendorKey=42"); // steps: AuthorizeRemoteTxRequests, VendorKey
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> firstStep = new CompletableFuture<>();
        when(transport.send(any(), any())).thenAnswer(invocation -> {
            Request request = invocation.getArgument(1);
            record(request);
            // Let the capability read complete so the burst starts; hang its first ChangeConfiguration
            // step — the sequence element this test abandons on the reconnect.
            if (request instanceof GetConfigurationRequest) {
                return CompletableFuture.completedFuture(new GetConfigurationConfirmation());
            }
            return firstStep;
        });

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        // The charger reconnects and proves itself on a fresh session.
        handler.onConnected(UUID.randomUUID());
        handler.onHeartbeat();

        // A's outstanding step now completes; the abandoned sequence must not send its next step.
        firstStep.complete(new ChangeConfigurationConfirmation(ConfigurationStatus.Accepted));
        verify(transport, org.mockito.Mockito.after(1500).never()).send(any(),
                eq(new ChangeConfigurationRequest("VendorKey", "42")));

        // And it must not have latched its fingerprint: B's own boot sends the configuration fresh.
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(), eq(new ChangeConfigurationRequest("VendorKey", "42")));
    }

    @Test
    void aRequestQueuedOnOneSessionIsNotSentOnItsSuccessor() {
        // The charger is connected (session A, from setUp) but not booted, so this request queues.
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> queued = handler
                .send(new ChangeConfigurationRequest("Key", "1")).toCompletableFuture();
        assertFalse(queued.isDone());

        // The charger reconnects under a fresh session B: A's queued request belongs to A's context
        // and must fail rather than carry over.
        handler.onConnected(UUID.randomUUID());
        assertTrue(queued.isCompletedExceptionally(), "a request queued on a superseded session must fail");

        // And when B becomes ready, the old request must not be transmitted on it.
        handler.onHeartbeat();
        verify(transport, org.mockito.Mockito.after(1500).never()).send(any(),
                eq(new ChangeConfigurationRequest("Key", "1")));
    }

    @Test
    void aChangedConfigurationIsSentAgainOnTheNextBoot() {
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        // Unchanged configuration: the next boot sends nothing again.
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, org.mockito.Mockito.after(1500).times(1)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));

        // Changed configuration: the applied latch is keyed on the effective settings, so the next
        // boot must send the new value — and the rest of the burst with it.
        serverConfig.vendorConfig = List.of("VendorKey=42");
        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        verify(transport, timeout(3000)).send(any(), eq(new ChangeConfigurationRequest("VendorKey", "42")));
        verify(transport, timeout(3000).times(2)).send(any(),
                eq(new ChangeConfigurationRequest("AuthorizeRemoteTxRequests", "false")));
    }

    @Test
    void nothingIsSentWhileTheBootNotificationIsBeingHandled() {
        // The library sends the BootNotification confirmation only after the event handler returns,
        // so any outbound request transmitted from inside the handler would reach the charger before
        // its boot answer. The boot configuration (settle 0 here) and any deferred connector traffic
        // must therefore be held: nothing on the wire when the handler returns, everything shortly
        // after.
        OcppConnectorHandler connector = mock(OcppConnectorHandler.class);
        handler.registerConnector(1, connector);

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        verify(transport, org.mockito.Mockito.never()).send(any(), any());
        verify(connector, org.mockito.Mockito.never()).onChargePointReady();

        verify(transport, timeout(3000)).send(any(), any());
        verify(connector, timeout(3000)).onChargePointReady();
    }

    @Test
    void aListReducedForSampledDataDoesNotNarrowAlignedData() {
        // Sampled and aligned data may support different measurand sets, so a rejection while
        // negotiating one key must not shrink the starting list of the other.
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

        verify(transport, timeout(3000).atLeast(3)).send(any(), any());
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
        // The default server configuration produces no boot-configuration steps, so runBootConfig
        // reaches its completion branch at once and asks for connector statuses. That refresh must go
        // through the readiness gate (send), not the ungated fallback (sendNow): nothing may reach the
        // wire until the charger is ready after its BootNotification is answered.
        serverConfig.disableRemoteTxAuthorization = false; // no steps at all now
        serverConfig.meterValuesData = "";
        realConnector(1); // real connector, so requestStatus actually gates through the charge point

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));

        // While the boot is being handled and before readiness: nothing on the wire.
        verify(transport, org.mockito.Mockito.after(600).never()).send(any(), any());

        // The charger's first post-boot message flips readiness; only now does the gated refresh go out.
        handler.onHeartbeat();
        verify(transport, timeout(2000)).send(any(), argThat(OcppBootConfigTest::isStatusTrigger));
    }

    private static boolean isStatusTrigger(Request request) {
        return request instanceof eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest trigger && trigger
                .getRequestedMessage() == eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType.StatusNotification;
    }

    /** A real connector handler bridged to this test's real charge point handler. */
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
