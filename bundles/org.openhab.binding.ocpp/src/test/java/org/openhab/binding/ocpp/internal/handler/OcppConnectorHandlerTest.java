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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.core.ChargePointErrorCode;
import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;

/**
 * Tests how {@link OcppConnectorHandler} turns a charger's reported status into channel state.
 *
 * <p>
 * The charging channel is deliberately driven by the reported status rather than by transaction
 * bookkeeping: a StopTransaction that never arrives would otherwise leave a connector showing a
 * charging session forever, which in turn misleads anything acting on that state.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class OcppConnectorHandlerTest {

    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_CONNECTOR, "server", "charger", "c1");

    private @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) OcppConnectorHandler handler;

    private @NonNullByDefault({}) Thing thing;

    @BeforeEach
    void setUp() {
        thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_CONNECTOR);
        when(thing.getChannels()).thenReturn(java.util.List.of());
        when(thing.getConfiguration()).thenReturn(new org.openhab.core.config.core.Configuration());
        when(thing.getProperties()).thenReturn(java.util.Map.of());
        callback = mock(ThingHandlerCallback.class);
        handler = new OcppConnectorHandler(thing);
        handler.setCallback(callback);
    }

    private static StatusNotificationRequest status(ChargePointStatus status) {
        return new StatusNotificationRequest(1, ChargePointErrorCode.NoError, status);
    }

    private void assertChannel(String channelId, org.openhab.core.types.State expected) {
        verify(callback).stateUpdated(eq(new ChannelUID(THING_UID, channelId)), eq(expected));
    }

    @Test
    void chargingReportsAnActiveSessionWithACablePresent() {
        handler.onStatusNotification(status(ChargePointStatus.Charging));

        assertChannel(CHANNEL_STATUS, new StringType("Charging"));
        assertChannel(CHANNEL_CHARGING, OnOffType.ON);
        assertChannel(CHANNEL_CABLE_CONNECTED, OnOffType.ON);
    }

    @Test
    void aSuspendedSessionIsStillAnActiveSession() {
        // The car has paused, not unplugged — treating this as "not charging" would end the session
        // in anything watching the channel.
        handler.onStatusNotification(status(ChargePointStatus.SuspendedEV));

        assertChannel(CHANNEL_CHARGING, OnOffType.ON);
        assertChannel(CHANNEL_CABLE_CONNECTED, OnOffType.ON);
    }

    @Test
    void availableClearsChargingEvenIfATransactionWasNeverStopped() {
        handler.onTransactionStarted(
                new eu.chargetime.ocpp.model.core.StartTransactionRequest(1, "tag", 0, java.time.ZonedDateTime.now()),
                7);

        handler.onStatusNotification(status(ChargePointStatus.Available));

        assertChannel(CHANNEL_CHARGING, OnOffType.OFF);
        assertChannel(CHANNEL_CABLE_CONNECTED, OnOffType.OFF);
    }

    @Test
    void preparingMeansACableIsPluggedInButNotYetCharging() {
        handler.onStatusNotification(status(ChargePointStatus.Preparing));

        assertChannel(CHANNEL_CABLE_CONNECTED, OnOffType.ON);
        assertChannel(CHANNEL_CHARGING, OnOffType.OFF);
    }

    @Test
    void unavailableIsReportedAsNotAvailable() {
        handler.onStatusNotification(status(ChargePointStatus.Unavailable));

        assertChannel(CHANNEL_AVAILABILITY, OnOffType.OFF);
    }

    @Test
    void anOperationalStatusIsReportedAsAvailable() {
        handler.onStatusNotification(status(ChargePointStatus.Available));

        assertChannel(CHANNEL_AVAILABILITY, OnOffType.ON);
    }

    private static eu.chargetime.ocpp.model.core.MeterValuesRequest meterValues(String measurand,
            @org.eclipse.jdt.annotation.Nullable String phase, String unit, String value) {
        eu.chargetime.ocpp.model.core.SampledValue sample = new eu.chargetime.ocpp.model.core.SampledValue(value);
        sample.setMeasurand(measurand);
        if (phase != null) {
            sample.setPhase(phase);
        }
        sample.setUnit(unit);
        eu.chargetime.ocpp.model.core.MeterValuesRequest request = new eu.chargetime.ocpp.model.core.MeterValuesRequest(
                1);
        request.setMeterValue(
                new eu.chargetime.ocpp.model.core.MeterValue[] { new eu.chargetime.ocpp.model.core.MeterValue(
                        java.time.ZonedDateTime.now(), new eu.chargetime.ocpp.model.core.SampledValue[] { sample }) });
        return request;
    }

    @Test
    void aMeasurandTheChargerReportsGetsAChannelEvenIfItIsNotDeclared() {
        // Optional telemetry is not declared on every connector: a charger that reports it gets the
        // channel, one that never does stays free of channels that would only ever be empty.
        handler.onMeterValues(meterValues("SoC", null, "Percent", "62"));

        org.mockito.ArgumentCaptor<Thing> updated = org.mockito.ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(updated.capture());
        org.junit.jupiter.api.Assertions.assertNotNull(
                updated.getValue().getChannel(new ChannelUID(THING_UID, CHANNEL_SOC)),
                "reporting SoC should have added the soc channel");
    }

    @Test
    void aDeclaredMeasurandDoesNotTriggerAThingUpdate() {
        // Phased current maps to a statically declared channel, so nothing about the thing changes.
        handler.onMeterValues(meterValues("Current.Import", "L1", "A", "14.2"));

        verify(callback, org.mockito.Mockito.never()).thingUpdated(org.mockito.ArgumentMatchers.any());
        assertChannel(CHANNEL_CURRENT_L1, new org.openhab.core.library.types.QuantityType<>("14.2 A"));
    }

    @Test
    void aFaultLeavesAvailabilityAlone() {
        // Faulted describes a fault, not whether the operator has taken the connector out of service,
        // so it must not overwrite the availability the operator set.
        handler.onStatusNotification(status(ChargePointStatus.Faulted));

        verify(callback, org.mockito.Mockito.never()).stateUpdated(eq(new ChannelUID(THING_UID, CHANNEL_AVAILABILITY)),
                org.mockito.ArgumentMatchers.any());
    }
}
