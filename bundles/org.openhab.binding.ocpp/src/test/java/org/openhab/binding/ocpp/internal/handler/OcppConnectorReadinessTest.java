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
import org.openhab.binding.ocpp.internal.transport.ChargerCapabilities;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;

import eu.chargetime.ocpp.model.Request;
import eu.chargetime.ocpp.model.core.ChargePointErrorCode;
import eu.chargetime.ocpp.model.core.ChargePointStatus;
import eu.chargetime.ocpp.model.core.ChargingRateUnitType;
import eu.chargetime.ocpp.model.core.ChargingSchedule;
import eu.chargetime.ocpp.model.core.GetConfigurationConfirmation;
import eu.chargetime.ocpp.model.core.KeyValueType;
import eu.chargetime.ocpp.model.core.StatusNotificationRequest;
import eu.chargetime.ocpp.model.core.UnlockConnectorRequest;
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
        when(parent.getCapabilities()).thenReturn(ChargerCapabilities.unknown());
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

    private static ChargerCapabilities capsWithoutSmartCharging() {
        KeyValueType profiles = new KeyValueType("SupportedFeatureProfiles", Boolean.TRUE);
        profiles.setValue("Core,FirmwareManagement,RemoteTrigger");
        GetConfigurationConfirmation confirmation = new GetConfigurationConfirmation();
        confirmation.setConfigurationKey(new KeyValueType[] { profiles });
        return ChargerCapabilities.from(confirmation);
    }

    @Test
    void aChargerWithoutSmartChargingIsNeverSentAProfile() {
        // The charger reported SupportedFeatureProfiles without SmartCharging, so a SetChargingProfile
        // is refused and can even stop its charge — neither a charge limit nor a pause may go out.
        ready.set(true);
        when(parent.getCapabilities()).thenReturn(capsWithoutSmartCharging());

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_PAUSE), OnOffType.ON);

        verify(parent, never()).send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));
    }

    private static ChargerCapabilities capsWithRateUnit(String allowedUnits) {
        KeyValueType profiles = new KeyValueType("SupportedFeatureProfiles", Boolean.TRUE);
        profiles.setValue("Core,SmartCharging");
        KeyValueType unit = new KeyValueType("ChargingScheduleAllowedChargingRateUnit", Boolean.TRUE);
        unit.setValue(allowedUnits);
        GetConfigurationConfirmation confirmation = new GetConfigurationConfirmation();
        confirmation.setConfigurationKey(new KeyValueType[] { profiles, unit });
        return ChargerCapabilities.from(confirmation);
    }

    private List<Request> captureAcceptedSends() {
        List<Request> sent = new java.util.ArrayList<>();
        when(parent.send(any())).thenAnswer(inv -> {
            sent.add(inv.getArgument(0));
            return CompletableFuture
                    .completedFuture(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        });
        return sent;
    }

    private static ChargingSchedule firstSchedule(List<Request> sent) {
        SetChargingProfileRequest profile = (SetChargingProfileRequest) sent.stream()
                .filter(OcppConnectorReadinessTest::isSetChargingProfile).findFirst().orElseThrow();
        return profile.getCsChargingProfiles().getChargingSchedule();
    }

    @Test
    void aPowerOnlyChargerGetsTheAmpsLimitConvertedToWatts() {
        // The charger only accepts a power limit, so 16 A is converted with the defaults (230 V, 1
        // phase): 16 * 230 = 3680 W, sent in the Power unit.
        ready.set(true);
        when(parent.getCapabilities()).thenReturn(capsWithRateUnit("Power"));
        List<Request> sent = captureAcceptedSends();

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        ChargingSchedule schedule = firstSchedule(sent);
        org.junit.jupiter.api.Assertions.assertEquals(ChargingRateUnitType.W, schedule.getChargingRateUnit());
        org.junit.jupiter.api.Assertions.assertEquals(3680.0,
                schedule.getChargingSchedulePeriod()[0].getLimit().doubleValue());
    }

    @Test
    void aWattsPowerLimitIsSentDirectlyWhenTheChargerAcceptsPower() {
        // An explicit watts limit wins over the amps channel and is sent as-is, no conversion.
        ready.set(true);
        when(parent.getCapabilities()).thenReturn(capsWithRateUnit("Current,Power"));
        List<Request> sent = captureAcceptedSends();

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_POWER_LIMIT), new QuantityType<>(3000, Units.WATT));

        ChargingSchedule schedule = firstSchedule(sent);
        org.junit.jupiter.api.Assertions.assertEquals(ChargingRateUnitType.W, schedule.getChargingRateUnit());
        org.junit.jupiter.api.Assertions.assertEquals(3000.0,
                schedule.getChargingSchedulePeriod()[0].getLimit().doubleValue());
    }

    @Test
    void aCurrentChargerStillGetsAmpsUnconverted() {
        // The charger accepts a current limit, so the amps go out as amps — unchanged from before.
        ready.set(true);
        when(parent.getCapabilities()).thenReturn(capsWithRateUnit("Current"));
        List<Request> sent = captureAcceptedSends();

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        ChargingSchedule schedule = firstSchedule(sent);
        org.junit.jupiter.api.Assertions.assertEquals(ChargingRateUnitType.A, schedule.getChargingRateUnit());
        org.junit.jupiter.api.Assertions.assertEquals(16.0,
                schedule.getChargingSchedulePeriod()[0].getLimit().doubleValue());
    }

    @Test
    void hardwareMaxCurrentPublishesTheWholeAmpereActuallySent() {
        // The vendor hardware-max key takes whole amperes, so a fractional command is rounded on the
        // wire; the channel must then report that rounded value, not the fractional request — 16.4 A
        // applies 16 A, and reporting 16.4 would misstate what the charger is enforcing.
        ready.set(true);
        List<Request> sent = new java.util.ArrayList<>();
        when(parent.send(argThat(r -> r instanceof eu.chargetime.ocpp.model.core.ChangeConfigurationRequest)))
                .thenAnswer(inv -> {
                    sent.add(inv.getArgument(0));
                    return CompletableFuture
                            .completedFuture(new eu.chargetime.ocpp.model.core.ChangeConfigurationConfirmation(
                                    eu.chargetime.ocpp.model.core.ConfigurationStatus.Accepted));
                });
        OcppConnectorHandler connector = newConnector(Map.of("connectorId", 1, "hardwareMaxCurrentKey", "MaxCurrent"));

        connector.handleCommand(new ChannelUID(CONN_UID, CHANNEL_HARDWARE_MAX_CURRENT),
                new QuantityType<>(16.4, Units.AMPERE));

        org.junit.jupiter.api.Assertions.assertEquals("16",
                ((eu.chargetime.ocpp.model.core.ChangeConfigurationRequest) sent.get(0)).getValue(),
                "the whole-ampere value must go on the wire");
        verify(callback).stateUpdated(
                org.mockito.ArgumentMatchers.eq(new ChannelUID(CONN_UID, CHANNEL_HARDWARE_MAX_CURRENT)),
                org.mockito.ArgumentMatchers.eq(new QuantityType<>(16, Units.AMPERE)));
    }

    @Test
    void aRequestedPhaseCountIsPutOnTheProfile() {
        // number-phases 1 requests single-phase charging; it appears on the schedule period sent with
        // the limit (the phase command alone, with no limit yet, clears — the limit send carries it).
        ready.set(true);
        when(parent.getCapabilities()).thenReturn(capsWithRateUnit("Current"));
        List<Request> sent = captureAcceptedSends();

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_NUMBER_PHASES), new DecimalType(1));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        org.junit.jupiter.api.Assertions.assertEquals(Integer.valueOf(1),
                firstSchedule(sent).getChargingSchedulePeriod()[0].getNumberPhases());
    }

    @Test
    void withNoPhaseCountTheProfileKeepsTheDefault() {
        // Unset ⇒ the binding does not touch numberPhases, so it stays at the library/OCPP default of 3
        // (three-phase) that the schedule-period constructor sets — unchanged from before this channel.
        ready.set(true);
        when(parent.getCapabilities()).thenReturn(capsWithRateUnit("Current"));
        List<Request> sent = captureAcceptedSends();

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        org.junit.jupiter.api.Assertions.assertEquals(Integer.valueOf(3),
                firstSchedule(sent).getChargingSchedulePeriod()[0].getNumberPhases());
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
    void anAcceptedOlderRequestPublishesWhenTheNewerOneIsRejected() {
        // The charger accepts the 10 A request and rejects the newer 20 A one: it is then genuinely
        // running 10 A, and the channels must say so — publishing the mutable fields would have
        // reported 20 A, and publishing nothing would hide the applied state.
        ready.set(true);
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> first = new CompletableFuture<>();
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> second = new CompletableFuture<>();
        when(parent.send(any())).thenReturn(first, second);

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(10, Units.AMPERE));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(20, Units.AMPERE));

        first.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        second.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Rejected));

        // The accepted request's own 10 A is published; the rejected 20 A never is.
        verify(callback).stateUpdated(org.mockito.ArgumentMatchers.eq(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT)),
                argThat(state -> state instanceof QuantityType<?> quantity && quantity.doubleValue() == 10.0));
        assertLimitNeverPublished(20.0);
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

    @Test
    void aLimitCaughtMidCoalesceWindowIsReappliedAfterAnOfflineBlip() {
        // With coalescing enabled, a limit can be sitting in a scheduled flush when the charge point
        // drops offline. Cancelling that flush must not silently lose the setpoint — the latest value
        // is re-applied when the charge point comes back, rather than dropped until the next command.
        OcppConnectorHandler coalescing = newConnector(Map.of("connectorId", 1, "profileMinIntervalMs", 100));

        ready.set(true);
        // The first command sends immediately (nothing to coalesce against yet) and opens the interval.
        coalescing.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(10, Units.AMPERE));
        verify(parent, org.mockito.Mockito.timeout(1000).times(1))
                .send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));
        // A second command within the interval only schedules a flush — nothing on the wire yet.
        coalescing.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        // The charge point drops before the flush fires: the scheduled 16 A flush is cancelled and
        // must not sneak out afterwards — still exactly one send through the whole offline window.
        coalescing
                .bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null));
        ready.set(false);
        verify(parent, org.mockito.Mockito.after(400).times(1))
                .send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));

        // Back ready: the 16 A the cancelled flush would have carried is re-applied, not lost.
        ready.set(true);
        coalescing.onChargePointReady();
        verify(parent, org.mockito.Mockito.timeout(1000).times(2))
                .send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));
    }

    @Test
    void pausingSendsZeroAmpsAndUnpausingRestoresTheLimit() {
        // PAUSE ON sends a 0 A charging profile (limit 0, transaction kept); PAUSE OFF restores the
        // last commanded limit. The stored fields, not the wire, carry the setpoint across the pause.
        ready.set(true);
        List<Request> sent = new java.util.ArrayList<>();
        when(parent.send(any())).thenAnswer(inv -> {
            sent.add(inv.getArgument(0));
            return CompletableFuture
                    .completedFuture(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        });

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_PAUSE), OnOffType.ON);
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_PAUSE), OnOffType.OFF);

        List<Double> limits = sent.stream().filter(OcppConnectorReadinessTest::isSetChargingProfile)
                .map(r -> ((SetChargingProfileRequest) r).getCsChargingProfiles().getChargingSchedule()
                        .getChargingSchedulePeriod()[0].getLimit().doubleValue())
                .toList();
        org.junit.jupiter.api.Assertions.assertEquals(List.of(16.0, 0.0, 16.0), limits,
                "pause should send 0 A and unpause should restore 16 A");
    }

    @Test
    void aTransientStateDoesNotAutoUnlockWhenStuckRecoveryIsOff() {
        // stuckStateRecovery defaults to false: a connector sitting in a transient OCPP state
        // (Preparing/Finishing) must never be auto-unlocked — that physical action is opt-in per
        // charger, not triggered by elapsed time in an otherwise normal state.
        ready.set(true);
        handler.onStatusNotification(
                new StatusNotificationRequest(1, ChargePointErrorCode.NoError, ChargePointStatus.Preparing));

        verify(parent, org.mockito.Mockito.after(300).never()).send(argThat(r -> r instanceof UnlockConnectorRequest));
    }

    @Test
    void meterValuesPollingCoalescesWhileAPreviousPollIsOutstanding() throws InterruptedException {
        // 1-second polling against a charger that keeps its socket open but stops answering: while a
        // poll is still outstanding, subsequent ticks must be skipped so the queue cannot grow an
        // unbounded backlog behind the request timeout.
        ready.set(true);
        java.util.concurrent.atomic.AtomicInteger polls = new java.util.concurrent.atomic.AtomicInteger();
        when(parent.send(argThat(OcppConnectorReadinessTest::isMeterValuesTrigger))).thenAnswer(inv -> {
            polls.incrementAndGet();
            return new CompletableFuture<>(); // never completes — the charger is not answering
        });
        OcppConnectorHandler polling = newConnector(Map.of("connectorId", 1, "refreshInterval", 1));

        Thread.sleep(3200); // three 1-second poll ticks elapse

        try {
            org.junit.jupiter.api.Assertions.assertEquals(1, polls.get(),
                    "only the first poll should go out while it is still outstanding");
        } finally {
            polling.dispose();
        }
    }

    private static boolean isMeterValuesTrigger(Request request) {
        return request instanceof eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequest trigger && trigger
                .getRequestedMessage() == eu.chargetime.ocpp.model.remotetrigger.TriggerMessageRequestType.MeterValues;
    }

    private OcppConnectorHandler newConnector(Map<String, Object> config) {
        Thing connThing = mock(Thing.class);
        when(connThing.getUID()).thenReturn(CONN_UID);
        when(connThing.getThingTypeUID()).thenReturn(THING_TYPE_CONNECTOR);
        when(connThing.getBridgeUID()).thenReturn(CP_UID);
        when(connThing.getConfiguration()).thenReturn(new Configuration(config));
        when(connThing.getChannels()).thenReturn(List.of());
        when(connThing.getProperties()).thenReturn(Map.of());
        OcppConnectorHandler connector = new OcppConnectorHandler(connThing);
        connector.setCallback(callback);
        connector.initialize();
        return connector;
    }
}
