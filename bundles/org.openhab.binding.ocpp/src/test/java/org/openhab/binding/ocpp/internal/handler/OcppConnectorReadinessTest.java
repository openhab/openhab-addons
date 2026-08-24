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
 * Tests that a connector holds a command sent before its charge point is ready, then sends it once
 * the charge point becomes ready, along with charge-limit conversion and coalescing behaviour.
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
        // Power-only charger: 16 A converts with the defaults (230 V, 1 phase) to 3680 W.
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
        // The vendor hardware-max key takes whole amperes, so 16.4 A rounds to 16 A on the wire and the
        // channel must report the rounded value.
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
        // A phase command with no limit yet sends nothing; the following limit send carries the phase.
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
        // Unset ⇒ numberPhases stays at the library schedule-period default of 3.
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

        verify(parent, never()).send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));

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
        ready.set(true);
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> first = new CompletableFuture<>();
        CompletableFuture<eu.chargetime.ocpp.model.Confirmation> second = new CompletableFuture<>();
        when(parent.send(any())).thenReturn(first, second);

        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(10, Units.AMPERE));
        handler.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(20, Units.AMPERE));

        first.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        second.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Rejected));

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

        // Out-of-order completion: the newer request is accepted before the older one.
        second.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        verify(callback).stateUpdated(org.mockito.ArgumentMatchers.eq(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT)),
                argThat(state -> state instanceof QuantityType<?> quantity && quantity.doubleValue() == 20.0));

        first.complete(new SetChargingProfileConfirmation(ChargingProfileStatus.Accepted));
        assertLimitNeverPublished(10.0);
    }

    @Test
    void aLimitCaughtMidCoalesceWindowIsReappliedAfterAnOfflineBlip() {
        OcppConnectorHandler coalescing = newConnector(Map.of("connectorId", 1, "profileMinIntervalMs", 100));

        ready.set(true);
        coalescing.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(10, Units.AMPERE));
        verify(parent, org.mockito.Mockito.timeout(1000).times(1))
                .send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));
        coalescing.handleCommand(new ChannelUID(CONN_UID, CHANNEL_CHARGE_LIMIT), new QuantityType<>(16, Units.AMPERE));

        coalescing
                .bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null));
        ready.set(false);
        verify(parent, org.mockito.Mockito.after(400).times(1))
                .send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));

        ready.set(true);
        coalescing.onChargePointReady();
        verify(parent, org.mockito.Mockito.timeout(1000).times(2))
                .send(argThat(OcppConnectorReadinessTest::isSetChargingProfile));
    }

    @Test
    void pausingSendsZeroAmpsAndUnpausingRestoresTheLimit() {
        // PAUSE keeps the transaction and sends a 0 A profile; the last limit is carried in stored
        // fields, not on the wire, and restored on unpause.
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
        ready.set(true);
        handler.onStatusNotification(
                new StatusNotificationRequest(1, ChargePointErrorCode.NoError, ChargePointStatus.Preparing));

        verify(parent, org.mockito.Mockito.after(300).never()).send(argThat(r -> r instanceof UnlockConnectorRequest));
    }

    @Test
    void meterValuesPollingCoalescesWhileAPreviousPollIsOutstanding() throws InterruptedException {
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
