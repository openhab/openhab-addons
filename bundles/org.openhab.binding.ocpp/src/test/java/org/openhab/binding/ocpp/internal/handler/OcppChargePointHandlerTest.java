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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.core.BootNotificationRequest;
import eu.chargetime.ocpp.model.core.ChargePointErrorCode;
import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.MeterValuesRequest;
import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

/**
 * Tests how {@link OcppChargePointHandler} routes a charger's messages to its connectors, and how it
 * tracks transactions.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class OcppChargePointHandlerTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");

    private @NonNullByDefault({}) OcppChargePointHandler handler;
    private @NonNullByDefault({}) OcppConnectorHandler connector1;
    private @NonNullByDefault({}) OcppConnectorHandler connector2;

    @BeforeEach
    void setUp() {
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(THING_UID);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        handler = new OcppChargePointHandler(bridge);
        handler.setCallback(mock(ThingHandlerCallback.class));

        connector1 = mock(OcppConnectorHandler.class);
        connector2 = mock(OcppConnectorHandler.class);
        handler.registerConnector(1, connector1);
        handler.registerConnector(2, connector2);
    }

    private static StartTransactionRequest start(int connectorId) {
        return new StartTransactionRequest(connectorId, "tag", 0, ZonedDateTime.now());
    }

    private static StopTransactionRequest stop(int transactionId) {
        return new StopTransactionRequest(0, ZonedDateTime.now(), transactionId);
    }

    @Test
    void messagesReachTheConnectorTheyNameAndNoOther() {
        handler.onStatusNotification(
                new StatusNotificationRequest(2, ChargePointErrorCode.NoError, ChargePointStatus.Charging));

        verify(connector2).onStatusNotification(any());
        verify(connector1, never()).onStatusNotification(any());
    }

    @Test
    void chargePointWideMessagesAreNotRoutedToAConnector() {
        // Connector 0 addresses the charge point itself, so there is no connector to hand it to.
        handler.onStatusNotification(
                new StatusNotificationRequest(0, ChargePointErrorCode.NoError, ChargePointStatus.Available));
        handler.onMeterValues(new MeterValuesRequest(0));

        verify(connector1, never()).onStatusNotification(any());
        verify(connector1, never()).onMeterValues(any());
        verify(connector2, never()).onStatusNotification(any());
    }

    @Test
    void aStoppedTransactionIsReportedToTheConnectorThatOwnsIt() {
        handler.onStartTransaction(start(1), 100);
        handler.onStartTransaction(start(2), 200);

        handler.onStopTransaction(stop(200));

        verify(connector2).onTransactionStopped(any());
        verify(connector1, never()).onTransactionStopped(any());
    }

    @Test
    void aTransactionThatWasNeverStoppedIsDiscardedWhenTheNextOneStarts() {
        // A connector runs one transaction at a time, so a new start must discard a prior one whose StopTransaction was
        // lost (charger dropped mid-session) rather than leak it.
        handler.onStartTransaction(start(1), 100);
        handler.onStartTransaction(start(1), 101);

        handler.onStopTransaction(stop(100));

        verify(connector1, never()).onTransactionStopped(any());

        handler.onStopTransaction(stop(101));
        verify(connector1).onTransactionStopped(any());
    }

    @Test
    void anUnknownTransactionIdIsIgnored() {
        handler.onStopTransaction(stop(999));

        verify(connector1, never()).onTransactionStopped(any());
        verify(connector2, never()).onTransactionStopped(any());
    }

    @Test
    void aTransactionOnAConnectorThatHasNoThingIsIgnored() {
        handler.onStartTransaction(start(7), 300);

        verify(connector1, never()).onTransactionStarted(any(), anyInt());
        verify(connector2, never()).onTransactionStarted(any(), anyInt());
    }

    private void awaitReady() throws InterruptedException {
        long deadline = System.currentTimeMillis() + 3000;
        while (!handler.isReady() && System.currentTimeMillis() < deadline) {
            Thread.sleep(20);
        }
        assertTrue(handler.isReady(), "charge point should have become ready");
    }

    @Test
    void aChargePointIsNotReadyUntilItHasBooted() throws InterruptedException {
        handler.onConnected(UUID.randomUUID());
        assertFalse(handler.isReady(), "a just-connected charger has not booted yet");

        handler.onBootNotification(new BootNotificationRequest("vendor", "model"));
        // Readiness must NOT flip inside the boot handler; it flips asynchronously once the confirmation is sent.
        assertFalse(handler.isReady(), "not ready while the boot notification is still being handled");
        awaitReady();
    }

    @Test
    void becomingReadyReleasesConnectorsThatDeferredASend() {
        handler.onConnected(UUID.randomUUID());
        // A heartbeat also proves the charger booted (e.g. socket reopened without a fresh BootNotification); the
        // release runs off the library thread, hence the timeout.
        handler.onHeartbeat();

        verify(connector1, org.mockito.Mockito.timeout(2000)).onChargePointReady();
        verify(connector2, org.mockito.Mockito.timeout(2000)).onChargePointReady();
    }

    @Test
    void aDisconnectMakesItNotReadyAgain() throws InterruptedException {
        UUID session = UUID.randomUUID();
        handler.onConnected(session);
        handler.onHeartbeat();
        awaitReady();

        handler.onDisconnected(session);
        assertFalse(handler.isReady(), "a disconnected charger is not ready");
    }
}
