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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.smartcharging.ChargingProfileStatus;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileConfirmation;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;

/**
 * Tests that a connector holds a command sent before its charge point is ready and sends it once the
 * charge point becomes ready, rather than transmitting before the charger can accept it or dropping
 * it. This is the caller-side of the outbound gate — the recovery probe stays ungated elsewhere.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class OcppConnectorReadinessTest {

    private static final ThingUID CP_UID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");
    private static final ThingUID CONN_UID = new ThingUID(THING_TYPE_CONNECTOR, "server", "charger", "c1");

    private final AtomicBoolean ready = new AtomicBoolean(false);
    private @NonNullByDefault({}) OcppChargePointHandler parent;
    private @NonNullByDefault({}) OcppConnectorHandler handler;
    private @NonNullByDefault({}) ThingHandlerCallback callback;

    @BeforeEach
    void setUp() {
        parent = mock(OcppChargePointHandler.class);
        when(parent.isReady()).thenAnswer(invocation -> ready.get());
        when(parent.getChargePointId()).thenReturn("charger");
        when(parent.send(any())).thenReturn(
                CompletableFuture.completedFuture(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted)));

        Bridge parentThing = mock(Bridge.class);
        when(parentThing.getHandler()).thenReturn(parent);

        Thing connThing = mock(Thing.class);
        when(connThing.getUID()).thenReturn(CONN_UID);
        when(connThing.getThingTypeUID()).thenReturn(THING_TYPE_CONNECTOR);
        when(connThing.getBridgeUID()).thenReturn(CP_UID);
        when(connThing.getConfiguration()).thenReturn(new Configuration(Map.of("connectorId", 1)));
        when(connThing.getChannels()).thenReturn(List.of());
        when(connThing.getProperties()).thenReturn(Map.of());

        callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(CP_UID)).thenReturn(parentThing);

        handler = new OcppConnectorHandler(connThing);
        handler.setCallback(callback);
        handler.initialize();
    }

    private static boolean isSetChargingProfile(Request request) {
        return request instanceof SetChargingProfileRequest;
    }

    @Test
    void aLimitSetBeforeTheChargePointIsReadyIsHeldThenSentWhenItBecomesReady() {
        ready.set(false);
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        // Not ready: nothing goes out yet.
        verify(parent, never()).send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));

        // The charger boots and proves it is ready; the held limit is now sent.
        ready.set(true);
        handler.onChargePointReady();

        verify(parent).send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));
    }

    private void assertLimitNeverPublished(double amps) {
        verify(callback, never()).stateUpdated(
                org.mockito.ArgumentMatchers.eq(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT)),
                argThat(state -> state instanceof QuantityType<?> quantity && quantity.doubleValue() == amps));
    }

    @Test
    void aConfirmationPublishesTheValuesOfItsOwnRequestNotTheCurrentFields() {
        // The charger answers the FIRST request while a newer one is already in flight. Publishing
        // the mutable fields here would report the newer 20 A as accepted even though only 10 A was —
        // and if the newer request is then rejected, the channels would be lying for good.
        ready.set(true);
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> first = new CompletableFuture<>();
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> second = new CompletableFuture<>();
        when(parent.send(any())).thenReturn(first, second);

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(10, Units.AMPERE));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(20, Units.AMPERE));

        first.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        second.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Rejected));

        // The stale first confirmation must not publish anything (neither its own 10 A — it is no
        // longer the newest request — nor the 20 A the fields held), and the rejected second must not
        // publish either.
        assertLimitNeverPublished(20.0);
        assertLimitNeverPublished(10.0);
    }

    @Test
    void aLateStaleConfirmationCannotOverwriteANewerResult() {
        ready.set(true);
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> first = new CompletableFuture<>();
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> second = new CompletableFuture<>();
        when(parent.send(any())).thenReturn(first, second);

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(10, Units.AMPERE));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(20, Units.AMPERE));

        // Out-of-order completion: the newer request is accepted first...
        second.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        verify(callback).stateUpdated(org.mockito.ArgumentMatchers.eq(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT)),
                argThat(state -> state instanceof QuantityType<?> quantity && quantity.doubleValue() == 20.0));

        // ...and the older confirmation arriving afterwards must not roll the channel back to 10 A.
        first.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        assertLimitNeverPublished(10.0);
    }
}
