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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.time.ZonedDateTime;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.core.StartTransactionRequest;
import eu.chargetime.ocpp.model.core.StopTransactionRequest;

/**
 * Tests that transaction state survives an openHAB restart. Because the id and the
 * transaction-to-connector mapping are persisted through the server bridge, a StopTransaction that
 * arrives after a restart — when the in-memory map is empty — still reaches its connector, and a
 * connector can recover the id it needs for a RemoteStop or a TxProfile.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class OcppTransactionRecoveryTest {

    private static final ThingUID SERVER_UID = new ThingUID(THING_TYPE_SERVER, "server");
    private static final ThingUID CP_UID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");

    private @NonNullByDefault({}) OcppServerBridgeHandler server;
    private @NonNullByDefault({}) OcppChargePointHandler handler;

    @BeforeEach
    void setUp() {
        server = mock(OcppServerBridgeHandler.class);
        when(server.getServerConfig())
                .thenReturn(new org.openhab.binding.ocpp.internal.config.OcppServerConfiguration());

        Bridge serverThing = mock(Bridge.class);
        when(serverThing.getHandler()).thenReturn(server);

        Bridge cpThing = mock(Bridge.class);
        when(cpThing.getUID()).thenReturn(CP_UID);
        when(cpThing.getBridgeUID()).thenReturn(SERVER_UID);
        when(cpThing.getStatus()).thenReturn(ThingStatus.ONLINE);
        when(cpThing.getConfiguration()).thenReturn(new Configuration(Map.of("chargePointId", "charger")));

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(SERVER_UID)).thenReturn(serverThing);

        handler = new OcppChargePointHandler(cpThing);
        handler.setCallback(callback);
        handler.initialize();
    }

    private static StartTransactionRequest start(int connectorId) {
        return new StartTransactionRequest(connectorId, "tag", 0, ZonedDateTime.now());
    }

    private static StopTransactionRequest stop(int transactionId) {
        return new StopTransactionRequest(0, ZonedDateTime.now(), transactionId);
    }

    @Test
    void aStartedTransactionIsPersisted() {
        OcppConnectorHandler connector = mock(OcppConnectorHandler.class);
        handler.registerConnector(1, connector);

        handler.onStartTransaction(start(1), 100);

        verify(server).rememberTransaction(100, "charger", 1);
    }

    @Test
    void aStopAfterARestartRecoversTheConnectorFromThePersistedMapping() {
        OcppConnectorHandler connector = mock(OcppConnectorHandler.class);
        handler.registerConnector(1, connector);
        // No onStartTransaction in this process — the in-memory map is empty, as it is after a restart.
        when(server.transactionConnector(100, "charger")).thenReturn(1);

        handler.onStopTransaction(stop(100));

        verify(connector).onTransactionStopped(any());
        verify(server).forgetTransaction(100);
    }

    @Test
    void aConnectorRecoversItsOpenTransactionIdFromTheServer() {
        when(server.openTransactionFor("charger", 1)).thenReturn(55);

        assertEquals(Integer.valueOf(55), handler.recoverTransactionId(1));
    }
}
