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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.ocpp.internal.OcppBindingConstants.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.ocpp.internal.transport.ChargerCapabilities;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.ChargePointErrorCode;
import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.smartcharging.ChargingProfileStatus;
import eu.chargetime.ocpp.model.smartcharging.ClearChargingProfileConfirmation;
import eu.chargetime.ocpp.model.smartcharging.ClearChargingProfileRequest;
import eu.chargetime.ocpp.model.smartcharging.ClearChargingProfileStatus;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileConfirmation;
import eu.chargetime.ocpp.model.smartcharging.SetChargingProfileRequest;

/**
 * Tests how {@link OcppConnectorHandler} turns a charger's reported status into channel state.
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
    void sessionEnergyIsPublishedAtStopAsMeterStopMinusMeterStart() {
        handler.onTransactionStarted(
                new eu.chargetime.ocpp.model.core.StartTransactionRequest(1, "tag", 100, java.time.ZonedDateTime.now()),
                7);
        handler.onTransactionStopped(
                new eu.chargetime.ocpp.model.core.StopTransactionRequest(1600, java.time.ZonedDateTime.now(), 7));

        assertChannel(CHANNEL_SESSION_ENERGY,
                new org.openhab.core.library.types.QuantityType<>(1500, org.openhab.core.library.unit.Units.WATT_HOUR));
    }

    @Test
    void aRemoteStartTheChargerDoesNotAnswerIsRetried() {
        ThingUID chargePointUID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");
        when(thing.getBridgeUID()).thenReturn(chargePointUID);
        when(thing.getConfiguration()).thenReturn(new org.openhab.core.config.core.Configuration(
                java.util.Map.of("remoteStartRetries", new java.math.BigDecimal(1))));
        OcppChargePointHandler chargePoint = mock(OcppChargePointHandler.class);
        when(chargePoint.isReady()).thenReturn(true);
        when(chargePoint.getChargePointId()).thenReturn("charger");
        when(chargePoint.recoverTransactionId(org.mockito.ArgumentMatchers.anyInt())).thenReturn(null);
        when(chargePoint.send(any()))
                .thenReturn(CompletableFuture.completedFuture(mock(eu.chargetime.ocpp.model.Confirmation.class)));
        when(chargePoint.send(argThat(r -> r instanceof eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest)))
                .thenReturn(CompletableFuture.failedFuture(new java.util.concurrent.TimeoutException("no answer")));
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(chargePoint);
        when(callback.getBridge(chargePointUID)).thenReturn(bridge);

        handler.initialize();
        command(CHANNEL_CHARGING, OnOffType.ON);

        // The initial attempt plus one retry after the delay, since nothing started in between.
        verify(chargePoint, timeout(10000).times(2))
                .send(argThat(r -> r instanceof eu.chargetime.ocpp.model.core.RemoteStartTransactionRequest));
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
        handler.onMeterValues(meterValues("SoC", null, "Percent", "62"));

        org.mockito.ArgumentCaptor<Thing> updated = org.mockito.ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(updated.capture());
        org.junit.jupiter.api.Assertions.assertNotNull(
                updated.getValue().getChannel(new ChannelUID(THING_UID, CHANNEL_SOC)),
                "reporting SoC should have added the soc channel");
    }

    @Test
    void aDeclaredMeasurandDoesNotTriggerAThingUpdate() {
        handler.onMeterValues(meterValues("Current.Import", "L1", "A", "14.2"));

        verify(callback, org.mockito.Mockito.never()).thingUpdated(org.mockito.ArgumentMatchers.any());
        assertChannel(CHANNEL_CURRENT_L1, new org.openhab.core.library.types.QuantityType<>("14.2 A"));
    }

    @Test
    void aMultiBlockMeterValuesPublishesTheLastBlocksTimestamp() {
        java.time.ZonedDateTime older = java.time.ZonedDateTime.parse("2026-01-01T10:00:00Z");
        java.time.ZonedDateTime newer = java.time.ZonedDateTime.parse("2026-01-01T10:05:00Z");
        eu.chargetime.ocpp.model.core.SampledValue first = new eu.chargetime.ocpp.model.core.SampledValue("10");
        first.setMeasurand("Energy.Active.Import.Register");
        first.setUnit("Wh");
        eu.chargetime.ocpp.model.core.SampledValue second = new eu.chargetime.ocpp.model.core.SampledValue("20");
        second.setMeasurand("Energy.Active.Import.Register");
        second.setUnit("Wh");
        eu.chargetime.ocpp.model.core.MeterValuesRequest request = new eu.chargetime.ocpp.model.core.MeterValuesRequest(
                1);
        request.setMeterValue(new eu.chargetime.ocpp.model.core.MeterValue[] {
                new eu.chargetime.ocpp.model.core.MeterValue(older,
                        new eu.chargetime.ocpp.model.core.SampledValue[] { first }),
                new eu.chargetime.ocpp.model.core.MeterValue(newer,
                        new eu.chargetime.ocpp.model.core.SampledValue[] { second }) });

        handler.onMeterValues(request);

        assertChannel(CHANNEL_TIMESTAMP, new org.openhab.core.library.types.DateTimeType(newer));
    }

    @Test
    void aFaultLeavesAvailabilityAlone() {
        // Faulted reports a fault, not the operator taking the connector out of service, so availability must stand.
        handler.onStatusNotification(status(ChargePointStatus.Faulted));

        verify(callback, org.mockito.Mockito.never()).stateUpdated(eq(new ChannelUID(THING_UID, CHANNEL_AVAILABILITY)),
                org.mockito.ArgumentMatchers.any());
    }

    /**
     * Uses the ONLINE bridge-status path rather than initialize(), so set-up transmits nothing and every
     * captured request comes from the command under test.
     */
    private OcppChargePointHandler attachReadyChargePoint() {
        return attachReadyChargePoint(ClearChargingProfileStatus.Accepted);
    }

    private OcppChargePointHandler attachReadyChargePoint(ClearChargingProfileStatus clearStatus) {
        ThingUID chargePointUID = new ThingUID(THING_TYPE_CHARGEPOINT, "server", "charger");
        when(thing.getBridgeUID()).thenReturn(chargePointUID);
        OcppChargePointHandler chargePoint = mock(OcppChargePointHandler.class);
        when(chargePoint.isReady()).thenReturn(true);
        when(chargePoint.getCapabilities()).thenReturn(ChargerCapabilities.unknown());
        when(chargePoint.send(any())).thenAnswer(invocation -> {
            Request request = invocation.getArgument(0);
            if (request instanceof ClearChargingProfileRequest) {
                return CompletableFuture.completedFuture(new ClearChargingProfileConfirmation(clearStatus));
            }
            return CompletableFuture
                    .completedFuture(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        });
        Bridge chargePointBridge = mock(Bridge.class);
        when(chargePointBridge.getHandler()).thenReturn(chargePoint);
        when(callback.getBridge(chargePointUID)).thenReturn(chargePointBridge);
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        return chargePoint;
    }

    private void command(String channelId, Command value) {
        handler.handleCommand(new ChannelUID(THING_UID, channelId), value);
    }

    private static double sentLimit(Request request) {
        return ((SetChargingProfileRequest) request).getCsChargingProfiles().getChargingSchedule()
                .getChargingSchedulePeriod()[0].getLimit();
    }

    @Test
    void unpausingWithoutALimitClearsTheProfileInsteadOfSuspending() {
        // Un-pause with no stored limit must clear the cap, not re-send 0 A (0 A reads as suspend).
        OcppChargePointHandler chargePoint = attachReadyChargePoint();

        command(CHANNEL_PAUSE, OnOffType.ON);
        command(CHANNEL_PAUSE, OnOffType.OFF);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(chargePoint, times(2)).send(captor.capture());
        List<Request> requests = captor.getAllValues();

        assertTrue(requests.get(0) instanceof SetChargingProfileRequest, "pause must send a SetChargingProfile");
        assertEquals(0.0, sentLimit(requests.get(0)), "pause must cap the connector at 0 A");

        Request unpause = requests.get(1);
        assertTrue(unpause instanceof ClearChargingProfileRequest,
                "un-pausing without a limit must clear the profile, not re-send 0 A");
        ClearChargingProfileRequest clear = (ClearChargingProfileRequest) unpause;
        assertEquals(Integer.valueOf(1), clear.getConnectorId(), "clear must target this connector");
        assertEquals(Integer.valueOf(0), clear.getStackLevel(), "clear must target our stack level");
        verify(callback).stateUpdated(eq(new ChannelUID(THING_UID, CHANNEL_CHARGE_LIMIT)), eq(UnDefType.UNDEF));
        verify(callback).stateUpdated(eq(new ChannelUID(THING_UID, CHANNEL_PAUSE)), eq(OnOffType.OFF));
    }

    @Test
    void aZeroChargeLimitClearsTheCapRatherThanSuspending() {
        OcppChargePointHandler chargePoint = attachReadyChargePoint();

        command(CHANNEL_CHARGE_LIMIT, new DecimalType(0));

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(chargePoint, times(1)).send(captor.capture());
        assertTrue(captor.getValue() instanceof ClearChargingProfileRequest,
                "a 0 A charge limit must clear the cap, not suspend the connector");
    }

    @Test
    void aResumeIsPublishedEvenWhenTheChargerReportsNoProfileToClear() {
        // A charger with no matching profile answers ClearChargingProfile Unknown (not Accepted); still uncapped.
        OcppChargePointHandler chargePoint = attachReadyChargePoint(ClearChargingProfileStatus.Unknown);

        command(CHANNEL_CHARGE_LIMIT, new DecimalType(0));

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(chargePoint, times(1)).send(captor.capture());
        assertTrue(captor.getValue() instanceof ClearChargingProfileRequest, "a 0 A limit must clear the cap");
        verify(callback).stateUpdated(eq(new ChannelUID(THING_UID, CHANNEL_CHARGE_LIMIT)), eq(UnDefType.UNDEF));
        verify(callback).stateUpdated(eq(new ChannelUID(THING_UID, CHANNEL_PAUSE)), eq(OnOffType.OFF));
    }

    @Test
    void unpausingRestoresAPreviouslySetLimit() {
        OcppChargePointHandler chargePoint = attachReadyChargePoint();

        command(CHANNEL_CHARGE_LIMIT, new DecimalType(16));
        command(CHANNEL_PAUSE, OnOffType.ON);
        command(CHANNEL_PAUSE, OnOffType.OFF);

        ArgumentCaptor<Request> captor = ArgumentCaptor.forClass(Request.class);
        verify(chargePoint, times(3)).send(captor.capture());
        List<Request> requests = captor.getAllValues();

        assertEquals(16.0, sentLimit(requests.get(0)), "the explicit limit must be sent");
        assertEquals(0.0, sentLimit(requests.get(1)), "pause must cap at 0 A");
        Request unpause = requests.get(2);
        assertTrue(unpause instanceof SetChargingProfileRequest,
                "un-pausing with a set limit must restore it, not clear the cap");
        assertEquals(16.0, sentLimit(unpause), "un-pausing must restore the previous 16 A limit");
    }
}
