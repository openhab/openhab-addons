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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

import eu.chargetime.ocpp.NotConnectedException;
import eu.chargetime.ocpp.model.Confirmation;
import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest;
import eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType;

/**
 * Proves the embedded ChargeTime OCA-OCPP library is reachable and functional from inside the
 * bundle: the transport composes the library server (which pulls in Java-WebSocket and gson off the
 * bundle class path), verifies its startup with real connections, and surfaces a failed bind.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ChargeTimeTransportTest {

    private OcppServerListener noopListener() {
        return new OcppServerListener() {
            @Override
            public void onSessionOpened(UUID session, @Nullable String chargePointId,
                    @Nullable InetSocketAddress remote) {
            }

            @Override
            public void onSessionClosed(UUID session) {
            }

            @Override
            public void onBootNotification(UUID session, BootNotificationRequest request) {
            }

            @Override
            public void onStatusNotification(UUID session, StatusNotificationRequest request) {
            }

            @Override
            public void onMeterValues(UUID session, MeterValuesRequest request) {
            }

            @Override
            public void onHeartbeat(UUID session) {
            }

            @Override
            public void onStartTransaction(UUID session, StartTransactionRequest request, int transactionId) {
            }

            @Override
            public void onStopTransaction(UUID session, StopTransactionRequest request) {
            }

            @Override
            public boolean isTagAuthorized(@Nullable String idTag) {
                return true;
            }

            @Override
            public int heartbeatFor(UUID session) {
                return 300;
            }

            @Override
            public int nextTransactionId() {
                return 1;
            }
        };
    }

    private ChargeTimeTransport newTransport() {
        return new ChargeTimeTransport(noopListener(), 0, 30, "");
    }

    /** A port that was free a moment ago — the standard local-test approximation. */
    private static int findFreePort() throws java.io.IOException {
        try (java.net.ServerSocket socket = new java.net.ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    @Test
    void constructsTheEmbeddedJsonServer() {
        ChargeTimeTransport transport = newTransport();
        assertNotNull(transport);
        assertFalse(transport.isRunning());
    }

    @Test
    void normalizesTheChargePointIdentifierByStrippingTheLeadingSlash() {
        // The library reports the WebSocket path (e.g. "/charx"); the charge point id is "charx".
        assertEquals("charx", ChargeTimeTransport.normalizeIdentifier("/charx"));
        assertEquals("car3", ChargeTimeTransport.normalizeIdentifier("car3"));
        assertNull(ChargeTimeTransport.normalizeIdentifier(null));
    }

    /**
     * The library refuses to send a request whose feature profile isn't registered on the server
     * ({@code UnsupportedFeatureException}) — which silently breaks charge-limit control and status
     * refresh. Sending to an unknown session must therefore fail as "not connected", never as
     * "unsupported feature".
     */
    @Test
    void nonCoreFeatureProfilesAreRegisteredSoTheirRequestsCanBeSent() throws java.io.IOException {
        ChargeTimeTransport transport = newTransport();
        transport.start("127.0.0.1", findFreePort());
        try {
            assertFailsAsNotConnected(transport, ChargingProfileBuilder.currentLimit(1, 16.0, true, null));
            assertFailsAsNotConnected(transport,
                    new TriggerMessageRequest(TriggerMessageRequestType.StatusNotification));
        } finally {
            transport.stop();
        }
    }

    private void assertFailsAsNotConnected(ChargeTimeTransport transport, Request request) {
        CompletionStage<Confirmation> result = transport.send(UUID.randomUUID(), request);
        CompletionException thrown = assertThrows(CompletionException.class, () -> result.toCompletableFuture().join());
        assertInstanceOf(NotConnectedException.class, thrown.getCause(),
                "expected NotConnectedException — an UnsupportedFeatureException means the feature " + "profile for "
                        + request.getClass().getSimpleName() + " is not registered");
    }

    @Test
    void startVerifiesTheServerAcceptsARealConnection() throws java.io.IOException {
        // start() returning must mean a listening socket, not just a started thread: the embedded
        // server binds asynchronously and start() probes it with a real TCP connection. The test
        // repeats that proof with its own connection.
        int port = findFreePort();
        ChargeTimeTransport transport = newTransport();
        transport.start("127.0.0.1", port);
        try {
            assertTrue(transport.isRunning());
            try (java.net.Socket connection = new java.net.Socket()) {
                connection.connect(new InetSocketAddress("127.0.0.1", port), 1000);
                assertTrue(connection.isConnected(), "the started server must accept a TCP connection");
            }
        } finally {
            transport.stop();
        }
        assertFalse(transport.isRunning());
    }

    @Test
    void startFailsWhenThePortIsAlreadyOccupied() throws java.io.IOException {
        // The embedded server reports a failed bind only to an internal callback and would
        // otherwise appear to have started; the transport must surface it as a startup failure so
        // the bridge cannot go ONLINE with no socket listening.
        try (java.net.ServerSocket occupier = new java.net.ServerSocket(0)) {
            ChargeTimeTransport transport = newTransport();
            assertThrows(IllegalStateException.class, () -> transport.start("127.0.0.1", occupier.getLocalPort()));
        }
    }
}
