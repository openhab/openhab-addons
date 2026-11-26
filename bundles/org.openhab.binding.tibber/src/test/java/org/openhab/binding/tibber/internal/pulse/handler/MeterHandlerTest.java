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
package org.openhab.binding.tibber.internal.pulse.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;
import static org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.Recorder;
import org.openhab.binding.tibber.internal.pulse.sml.SmlReading;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * Tests the scenarios of the {@code thing-lifecycle} spec (meter identification, status model, stale data, connection
 * loss, reconfiguration, disposal, properties) and of the {@code channels} spec (missing values, per-channel
 * staleness, last update, updates on every reading, refresh, meter identifier) at the level of the meter handler.
 * The stale timer is driven by hand through a monotonic clock that the test moves.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class MeterHandlerTest {

    private static final long SECOND = TimeUnit.SECONDS.toNanos(1);

    private final Bridge bridge = bridge("b1", "bridge.example", PASSWORD);
    private final AtomicLong nanos = new AtomicLong(1_000 * SECOND);
    private @Nullable MeterHandler handler;
    private @Nullable Recorder recorder;

    @AfterEach
    void tearDown() {
        MeterHandler h = handler;
        if (h != null) {
            h.dispose();
        }
    }

    private MeterHandler start(String deviceId, @Nullable Integer staleTimeout) {
        return start(deviceId, staleTimeout, ThingStatus.ONLINE);
    }

    private MeterHandler start(String deviceId, @Nullable Integer staleTimeout, ThingStatus bridgeStatus) {
        Thing thing = meter(bridge, "m1", deviceId, staleTimeout);
        setStatus(bridge, bridgeStatus);
        Recorder rec = new Recorder(thing, bridge);
        MeterHandler h = new MeterHandler(thing, nanos::get);
        h.setCallback(rec.callback);
        thing.setHandler(h);
        recorder = rec;
        handler = h;
        h.initialize();
        return h;
    }

    private Recorder rec() {
        Recorder r = recorder;
        assertNotNull(r);
        return r;
    }

    private static SmlReading reading(Object... pairs) {
        return new SmlReading(values(pairs), null);
    }

    /** What the framework does: the bridge has a new status, then it notifies the child. */
    private void bridgeChanged(MeterHandler h, ThingStatusInfo info) {
        bridge.setStatusInfo(info);
        h.bridgeStatusChanged(info);
    }

    private void advance(long seconds) {
        nanos.addAndGet(seconds * SECOND);
    }

    private void refresh(MeterHandler h, String channel) {
        h.handleCommand(new ChannelUID(h.getThing().getUID(), channel), RefreshType.REFRESH);
    }

    private void assertStatus(ThingStatus status, ThingStatusDetail detail) {
        MeterHandler h = handler;
        assertNotNull(h);
        assertEquals(status, h.getThing().getStatus());
        assertEquals(detail, h.getThing().getStatusInfo().getStatusDetail());
    }

    // thing-lifecycle: Meter identification, Configuration parameters, Status model

    @Test
    void whenTheBridgeIsOnlineAndNoReadingArrivedThenTheMeterIsUnknownAndAllChannelsAreUndefined() {
        start(DEVICE_A, null);

        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_FREQUENCY));
    }

    @Test
    void whenTheMeterIsInitializedForTheFirstTimeThenCountersAreNotWritten() {
        // A state that persistence restored on startup must survive the initialization.
        start(DEVICE_A, null);

        assertEquals(0, rec().count(CHANNEL_ENERGY_IMPORT));
        assertEquals(0, rec().count(CHANNEL_ENERGY_EXPORT));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenTheFirstInitializationFindsTheBridgeOfflineThenCountersAreNotWritten() {
        start(DEVICE_A, null, ThingStatus.OFFLINE);

        assertEquals(0, rec().count(CHANNEL_ENERGY_IMPORT));
    }

    @Test
    void whenTheDeviceIdIsUpperCaseThenTheMeterMatchesTheLowerCaseIdAndShowsItAsProperty() {
        MeterHandler h = start("AAAAAAAAAAAAAAAA", null);

        assertTrue(h.matches(DEVICE_A));
        assertFalse(h.matches(DEVICE_B));
        assertEquals(DEVICE_A, h.getThing().getProperties().get(PROPERTY_DEVICE_ID));
    }

    @Test
    void whenTheDeviceIdIsInvalidThenTheMeterIsOfflineWithConfigurationErrorNamingTheDeviceId() {
        MeterHandler h = start("not-a-device-id", null);

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals(TEXT_INVALID_DEVICE_ID, rec().lastStatus().getDescription());
        assertTrue(String.valueOf(rec().lastStatus().getDescription()).contains("device-id"));
        assertFalse(h.matches("not-a-device-id"));
    }

    @Test
    void whenTheStaleTimeoutIsBelowTheMinimumThenTheMeterIsOfflineWithConfigurationError() {
        start(DEVICE_A, 5);

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals(TEXT_INVALID_STALE_TIMEOUT, rec().lastStatus().getDescription());
    }

    @Test
    void whenTheMeterHasNoBridgeThenItIsOfflineWithConfigurationError() {
        Thing thing = meter(bridge, "m1", DEVICE_A, null);
        Recorder rec = new Recorder(thing, null);
        MeterHandler h = new MeterHandler(thing, nanos::get);
        h.setCallback(rec.callback);
        handler = h;
        recorder = rec;

        h.initialize();

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals(TEXT_NO_BRIDGE, rec.lastStatus().getDescription());
    }

    @Test
    void whenTheBridgeIsOfflineAtStartThenTheMeterIsOfflineWithBridgeOffline() {
        start(DEVICE_A, null, ThingStatus.OFFLINE);

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Test
    void whenTheBridgeIsNotInitializedAtStartThenTheMeterIsOfflineWithBridgeOfflineNeverUninitialized() {
        start(DEVICE_A, null, ThingStatus.UNINITIALIZED);

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
    }

    @Test
    void whenTheBridgeTurnsOnlineThenTheMeterDoesNotGoOnlineBeforeItsFirstValidReading() {
        MeterHandler h = start(DEVICE_A, null, ThingStatus.OFFLINE);

        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertTrue(rec().statuses.stream().noneMatch(s -> s.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void whenTheFirstValidReadingArrivesThenTheMeterGoesOnlineWithTheDecodedValues() {
        MeterHandler h = start(DEVICE_A, null);

        h.onReading(DEVICE_A, new SmlReading(values(CHANNEL_POWER, watts("1523"), CHANNEL_ENERGY_IMPORT,
                kilowattHours("1234.5678"), CHANNEL_VOLTAGE_L1, volts("225.5")), SYNTHETIC_METER_ID));

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertEquals(watts("1523"), rec().lastState(CHANNEL_POWER));
        assertEquals(kilowattHours("1234.5678"), rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertEquals(volts("225.5"), rec().lastState(CHANNEL_VOLTAGE_L1));
        assertEquals(SYNTHETIC_METER_ID, h.getThing().getProperties().get(PROPERTY_METER_ID));
    }

    @Test
    void whenTheBridgeStatusRepeatsOnlineAfterAReadingThenTheMeterStaysOnline() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1")));
        rec().clear();

        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        assertTrue(rec().statuses.isEmpty());
    }

    @Test
    void whenNotificationsArriveOutOfOrderThenTheMeterFollowsTheCurrentBridgeStatus() {
        MeterHandler h = start(DEVICE_A, null);
        bridge.setStatusInfo(info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
        bridge.setStatusInfo(info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        // The OFFLINE notification was queued first but is delivered after the ONLINE one.
        h.bridgeStatusChanged(info(ThingStatus.ONLINE, ThingStatusDetail.NONE));
        h.bridgeStatusChanged(info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));

        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertTrue(h.isStaleTimerArmed());
        assertTrue(rec().statuses.stream().noneMatch(s -> s.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void whenTheBridgeWentOfflineButAnOutdatedOnlineNotificationArrivesThenTheMeterStaysBridgeOffline() {
        MeterHandler h = start(DEVICE_A, null);
        bridge.setStatusInfo(info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));

        h.bridgeStatusChanged(info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        assertFalse(h.isStaleTimerArmed());
    }

    @Test
    void whenAReadingArrivesBeforeTheBridgeStatusChangeIsDeliveredThenTheMeterIsOnlineAndStaysSo() {
        MeterHandler h = start(DEVICE_A, null, ThingStatus.UNKNOWN);

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1")));
        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    void whenTheMeterIsMisconfiguredThenBridgeStatusChangesAndReadingsAreIgnored() {
        MeterHandler h = start("not-a-device-id", null);
        rec().clear();

        bridgeChanged(h, info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1")));

        assertTrue(rec().statuses.isEmpty());
        assertTrue(rec().updates.isEmpty());
    }

    // thing-lifecycle: Connection loss clears live values, Bridge offline

    @Test
    void whenTheBridgeGoesOfflineThenLiveValuesAreUndefinedAndCountersKeepTheirValues() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10"),
                CHANNEL_ENERGY_EXPORT, kilowattHours("2"), CHANNEL_FREQUENCY, hertz("49.9")));
        rec().clear();

        bridgeChanged(h, info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_FREQUENCY));
        assertNull(rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertNull(rec().lastState(CHANNEL_ENERGY_EXPORT));
        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_ENERGY_IMPORT), RefreshType.REFRESH);
        assertEquals(kilowattHours("10"), rec().lastState(CHANNEL_ENERGY_IMPORT));
    }

    @Test
    void whenTheBridgeReturnsAndReadingsResumeThenTheMeterIsOnlineAgainWithoutTheEarlierError() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        bridgeChanged(h, info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));

        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));
        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("300")));

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertNull(rec().lastStatus().getDescription());
        assertEquals(watts("300"), rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenTheBridgeIsUnknownThenTheMeterIsUnknownAndLiveValuesAreCleared() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));

        bridgeChanged(h, info(ThingStatus.UNKNOWN, ThingStatusDetail.NONE));

        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
    }

    // thing-lifecycle: Stale data; channels: Per-channel staleness

    @Test
    void whenReadingsStopThenTheMeterGoesOfflineWithCommunicationErrorAndOnlyLiveValuesAreUndefined() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10"),
                CHANNEL_ENERGY_EXPORT, kilowattHours("2")));
        rec().clear();

        advance(59);
        h.checkStale();
        assertTrue(rec().statuses.isEmpty(), "not stale yet");
        advance(2);
        h.checkStale();

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        assertEquals("@text/offline.comm-error.no-reading [\"60\"]", rec().lastStatus().getDescription());
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertNull(rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertNull(rec().lastState(CHANNEL_ENERGY_EXPORT));
    }

    @Test
    void whenReadingsResumeAfterTheMeterWentStaleThenItIsOnlineAndPowerShowsTheNewValue() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        advance(61);
        h.checkStale();

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("300")));

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertEquals(watts("300"), rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenNoReadingEverArrivesThenTheStaleTimeLimitCountsFromTheOpeningOfTheStream() {
        MeterHandler h = start(DEVICE_A, 30, ThingStatus.OFFLINE);
        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));

        advance(29);
        h.checkStale();
        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        advance(2);
        h.checkStale();

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        assertEquals("@text/offline.comm-error.no-reading [\"30\"]", rec().lastStatus().getDescription());
    }

    @Test
    void whenOnlyOtherTrafficArrivesThenTheMeterGoesStaleAfterTheStaleTimeout() {
        // Messages of other meters and invalid messages never reach the meter handler (the bridge handler drops or
        // routes them), so a meter that only waits for its own readings must run out of time.
        MeterHandler h = start(DEVICE_A, 30);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));

        for (int i = 0; i < 15; i++) {
            advance(2);
        }
        advance(1);
        h.checkStale();

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    void whenOneQuantityDisappearsThenOnlyItsChannelBecomesUndefinedAndTheMeterStaysOnline() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("100"), CHANNEL_FREQUENCY, hertz("49.9")));
        for (int i = 0; i < 20; i++) {
            advance(3);
            h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("100")));
        }
        rec().clear();

        advance(1);
        h.checkStale();

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_FREQUENCY));
        assertNull(rec().lastState(CHANNEL_POWER));
        assertTrue(rec().statuses.isEmpty());
    }

    @Test
    void whenTheStaleTimeoutIsChangedThenTheNewLimitAppliesAfterReinitialization() {
        MeterHandler h = start(DEVICE_A, 60);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("100")));
        h.handleConfigurationUpdate(Map.of("staleTimeout", 20));
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("100")));

        advance(21);
        h.checkStale();

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
    }

    @Test
    void whenTheStreamIsOpenThenOneStaleTimerRunsAndItEndsWithTheStreamAndTheThing() {
        MeterHandler h = start(DEVICE_A, null);
        assertTrue(h.isStaleTimerArmed(), "armed when the stream opened, even before the first reading");

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1")));
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("2")));
        assertTrue(h.isStaleTimerArmed());

        bridgeChanged(h, info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
        assertFalse(h.isStaleTimerArmed(), "no timer while the stream is down");

        bridgeChanged(h, info(ThingStatus.ONLINE, ThingStatusDetail.NONE));
        assertTrue(h.isStaleTimerArmed());
        h.dispose();
        assertFalse(h.isStaleTimerArmed());
    }

    @Test
    void whenTheMeterWentStaleThenNoTimerKeepsRunning() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1")));
        advance(61);

        h.checkStale();

        assertFalse(h.isStaleTimerArmed());
    }

    // thing-lifecycle: Runtime reconfiguration (device id)

    @Test
    void whenTheDeviceIdIsChangedThenTheOldValuesAreUndefinedAndTheMeterWaitsForTheNewDevice() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10")));
        rec().clear();

        h.handleConfigurationUpdate(Map.of("deviceId", DEVICE_B));

        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_EXPORT));
        assertFalse(h.matches(DEVICE_A));
        assertTrue(h.matches(DEVICE_B));
        assertEquals(DEVICE_B, h.getThing().getProperties().get(PROPERTY_DEVICE_ID));
    }

    @Test
    void whenTheDeviceIdIsChangedThenTheCachedCountersOfTheOldMeterAreGone() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A,
                reading(CHANNEL_ENERGY_IMPORT, kilowattHours("10"), CHANNEL_ENERGY_EXPORT, kilowattHours("2")));

        h.handleConfigurationUpdate(Map.of("deviceId", DEVICE_B));
        rec().clear();
        refresh(h, CHANNEL_ENERGY_IMPORT);
        refresh(h, CHANNEL_ENERGY_EXPORT);

        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_EXPORT));
    }

    @Test
    void whenOnlyTheStaleTimeoutIsChangedThenCountersKeepTheirValuesAndLiveValuesAreUndefined() {
        MeterHandler h = start(DEVICE_A, 60);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10"),
                CHANNEL_ENERGY_EXPORT, kilowattHours("2")));
        rec().clear();

        h.handleConfigurationUpdate(Map.of("staleTimeout", 20));

        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertEquals(0, rec().count(CHANNEL_ENERGY_IMPORT));
        assertEquals(0, rec().count(CHANNEL_ENERGY_EXPORT));
    }

    @Test
    void whenTheHandlerIsRestartedWithTheSameDeviceIdThenCountersAreNotWritten() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10")));
        rec().clear();

        h.dispose();
        h.initialize();

        assertEquals(0, rec().count(CHANNEL_ENERGY_IMPORT));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenTheMeterIsReinitializedWithTheSameDeviceIdThenRefreshReturnsTheKeptCounterValues() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10"),
                CHANNEL_ENERGY_EXPORT, kilowattHours("2")));
        h.handleConfigurationUpdate(Map.of("staleTimeout", 20));
        rec().clear();

        refresh(h, CHANNEL_ENERGY_IMPORT);
        refresh(h, CHANNEL_ENERGY_EXPORT);
        refresh(h, CHANNEL_POWER);

        assertEquals(kilowattHours("10"), rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertEquals(kilowattHours("2"), rec().lastState(CHANNEL_ENERGY_EXPORT));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenTheDeviceIdIsInvalidAndThenCorrectedToTheSameIdThenTheCountersAreClearedBecauseNoMeterWasIdentified() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_ENERGY_IMPORT, kilowattHours("10")));

        h.handleConfigurationUpdate(Map.of("deviceId", "not-a-device-id"));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_IMPORT));
        h.handleConfigurationUpdate(Map.of("deviceId", DEVICE_A));
        rec().clear();
        refresh(h, CHANNEL_ENERGY_IMPORT);

        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_IMPORT));
    }

    @Test
    void whenTheStaleTimeoutBecomesInvalidThenLiveValuesAreClearedAndCountersStay() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("10")));
        rec().clear();

        h.handleConfigurationUpdate(Map.of("staleTimeout", 5));

        assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertEquals(0, rec().count(CHANNEL_ENERGY_IMPORT));
        h.handleConfigurationUpdate(Map.of("staleTimeout", 30));
        rec().clear();
        refresh(h, CHANNEL_ENERGY_IMPORT);
        assertEquals(kilowattHours("10"), rec().lastState(CHANNEL_ENERGY_IMPORT));
    }

    // thing-lifecycle: Disposal

    @Test
    void whenTheMeterIsDisposedThenNothingIsPublishedAnymore() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        h.dispose();
        rec().clear();

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("5")));
        bridgeChanged(h, info(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR));
        advance(1_000);
        h.checkStale();
        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_POWER), RefreshType.REFRESH);

        assertTrue(rec().statuses.isEmpty());
        assertTrue(rec().updates.isEmpty());
        assertFalse(h.matches(DEVICE_A));
    }

    // channels: Missing values are never zero, Updates on every reading, Last update

    @Test
    void whenAMeterReportsNoPowerThenThePowerChannelsAreNeverPublishedAsZero() {
        MeterHandler h = start(DEVICE_A, null);

        h.onReading(DEVICE_A, reading(CHANNEL_ENERGY_IMPORT, kilowattHours("10"), CHANNEL_ENERGY_EXPORT,
                kilowattHours("2"), CHANNEL_VOLTAGE_L1, volts("225.5")));

        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER_L1));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_FREQUENCY));
        assertEquals(volts("225.5"), rec().lastState(CHANNEL_VOLTAGE_L1));
    }

    @Test
    void whenAnExplicitZeroIsReportedThenItIsPublishedAsZero() {
        MeterHandler h = start(DEVICE_A, null);

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("0"), CHANNEL_ENERGY_EXPORT, kilowattHours("0")));

        assertEquals(watts("0"), rec().lastState(CHANNEL_POWER));
        assertEquals(kilowattHours("0"), rec().lastState(CHANNEL_ENERGY_EXPORT));
    }

    @Test
    void whenTheSameValueArrivesAgainThenAnUpdateIsPublishedAgain() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_VOLTAGE_L1, volts("225.5")));
        long before = rec().count(CHANNEL_VOLTAGE_L1);

        h.onReading(DEVICE_A, reading(CHANNEL_VOLTAGE_L1, volts("225.5")));

        assertEquals(before + 1, rec().count(CHANNEL_VOLTAGE_L1));
    }

    @Test
    void whenAReadingLacksAQuantityThenNoUpdateIsPublishedForIt() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1"), CHANNEL_FREQUENCY, hertz("50")));
        long before = rec().count(CHANNEL_FREQUENCY);

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("2")));

        assertEquals(before, rec().count(CHANNEL_FREQUENCY));
    }

    @Test
    void whenTheMeterIdIsUnchangedThenThePropertyIsNotWrittenAgain() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, new SmlReading(values(CHANNEL_POWER, watts("1")), SYNTHETIC_METER_ID));
        org.mockito.Mockito.clearInvocations(rec().callback);

        h.onReading(DEVICE_A, new SmlReading(values(CHANNEL_POWER, watts("2")), SYNTHETIC_METER_ID));

        org.mockito.Mockito.verify(rec().callback, org.mockito.Mockito.never())
                .thingUpdated(org.mockito.ArgumentMatchers.any());
    }

    // channels: Refresh handling, Command to a read-only channel

    @Test
    void whenRefreshIsSentWithAKnownValueThenTheValueIsPublishedAgain() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        rec().clear();

        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_POWER), RefreshType.REFRESH);

        assertEquals(watts("1200"), rec().lastState(CHANNEL_POWER));
        assertTrue(rec().statuses.isEmpty());
    }

    @Test
    void whenRefreshIsSentBeforeTheFirstReadingThenTheStateIsUndefined() {
        MeterHandler h = start(DEVICE_A, null);
        rec().clear();

        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_POWER), RefreshType.REFRESH);

        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenRefreshIsSentForAStaleValueThenTheStateIsUndefined() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        advance(61);
        rec().clear();

        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_POWER), RefreshType.REFRESH);

        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_POWER));
    }

    @Test
    void whenACommandIsSentToAReadOnlyChannelThenNothingChanges() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        rec().clear();

        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_POWER), OnOffType.ON);
        h.handleCommand(new ChannelUID(h.getThing().getUID(), CHANNEL_POWER), new DecimalType(5));

        assertTrue(rec().updates.isEmpty());
        assertTrue(rec().statuses.isEmpty());
    }

    // thing-lifecycle: Thing properties without secrets

    @Test
    void whenTheMeterRunsThenNoPropertyContainsTheBridgePassword() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, new SmlReading(values(CHANNEL_POWER, watts("1")), SYNTHETIC_METER_ID));

        h.getThing().getProperties().values().forEach(v -> assertFalse(v.contains(PASSWORD)));
        assertEquals(2, h.getThing().getProperties().size());
        assertTrue(rec().statuses.stream().noneMatch(s -> {
            String description = s.getDescription();
            return description != null && (description.contains(PASSWORD) || description.contains(DEVICE_A)
                    || description.contains(SYNTHETIC_METER_ID));
        }));
    }

    @Test
    void whenValuesAreUnitQuantitiesThenTheyAreUsedAsPublishedStates() {
        MeterHandler h = start(DEVICE_A, null);

        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("-2300")));

        assertTrue(rec().lastState(CHANNEL_POWER) instanceof QuantityType<?>);
        assertEquals(watts("-2300"), rec().lastState(CHANNEL_POWER));
    }

    // thing-lifecycle: Meter identification, the moment of the comparison

    private static final List<String> VALUE_CHANNELS = List.of(CHANNEL_POWER, CHANNEL_ENERGY_IMPORT,
            CHANNEL_ENERGY_EXPORT, CHANNEL_POWER_L1, CHANNEL_POWER_L2, CHANNEL_POWER_L3, CHANNEL_VOLTAGE_L1,
            CHANNEL_VOLTAGE_L2, CHANNEL_VOLTAGE_L3, CHANNEL_CURRENT_L1, CHANNEL_CURRENT_L2, CHANNEL_CURRENT_L3,
            CHANNEL_FREQUENCY);

    /** An ONLINE meter for device A with a value on the live channels and the counters. */
    private MeterHandler startOnlineMeterWithValues() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("1000")));
        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertEquals(kilowattHours("1000"), rec().lastState(CHANNEL_ENERGY_IMPORT));
        return h;
    }

    private void assertEveryValueChannelUndefined() {
        for (String channel : VALUE_CHANNELS) {
            assertEquals(UnDefType.UNDEF, rec().lastState(channel), channel);
        }
    }

    /** What a refresh publishes is what the meter holds: nothing of a dropped reading may be cached. */
    private void assertEveryCachedValueUndefined(MeterHandler h) {
        rec().clear();
        for (String channel : VALUE_CHANNELS) {
            refresh(h, channel);
            assertEquals(UnDefType.UNDEF, rec().lastState(channel), "cached value of " + channel);
        }
    }

    @Test
    void readingOfThePreviousDeviceAfterADeviceIdChangeIsDroppedWithoutAnyEffect() {
        MeterHandler h = startOnlineMeterWithValues();
        SmlReading assignedBeforeTheChange = new SmlReading(
                values(CHANNEL_POWER, watts("900"), CHANNEL_ENERGY_IMPORT, kilowattHours("1001")), SYNTHETIC_METER_ID);
        h.handleConfigurationUpdate(Map.of("deviceId", DEVICE_B));
        for (String channel : VALUE_CHANNELS) {
            assertEquals(UnDefType.UNDEF, rec().lastState(channel), channel);
        }
        rec().clear();

        h.onReading(DEVICE_A, assignedBeforeTheChange);

        assertTrue(rec().updates.isEmpty(), () -> "channel updates: " + rec().updates);
        assertTrue(rec().statuses.isEmpty(), () -> "status updates: " + rec().statuses);
        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);
        assertNull(h.getThing().getProperties().get(PROPERTY_METER_ID));
        assertEveryCachedValueUndefined(h);
    }

    @Test
    void readingOfTheNewDeviceAfterADeviceIdChangeIsApplied() {
        MeterHandler h = startOnlineMeterWithValues();
        h.handleConfigurationUpdate(Map.of("deviceId", DEVICE_B));
        h.onReading(DEVICE_A, new SmlReading(
                values(CHANNEL_POWER, watts("900"), CHANNEL_ENERGY_IMPORT, kilowattHours("1001")), SYNTHETIC_METER_ID));

        h.onReading(DEVICE_B, reading(CHANNEL_POWER, watts("300")));

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertEquals(watts("300"), rec().lastState(CHANNEL_POWER));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_IMPORT));
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_EXPORT));
        assertNull(h.getThing().getProperties().get(PROPERTY_METER_ID));
        rec().clear();
        refresh(h, CHANNEL_ENERGY_IMPORT);
        assertEquals(UnDefType.UNDEF, rec().lastState(CHANNEL_ENERGY_IMPORT));
    }

    @Test
    void readingAppliedBeforeTheDeviceIdChangeLeavesEveryValueChannelUndefinedAfterTheChange() {
        MeterHandler h = start(DEVICE_A, null);
        h.onReading(DEVICE_A,
                new SmlReading(values(CHANNEL_POWER, watts("1200"), CHANNEL_ENERGY_IMPORT, kilowattHours("1000"),
                        CHANNEL_ENERGY_EXPORT, kilowattHours("2"), CHANNEL_POWER_L1, watts("400"), CHANNEL_POWER_L2,
                        watts("400"), CHANNEL_POWER_L3, watts("400"), CHANNEL_VOLTAGE_L1, volts("230"),
                        CHANNEL_VOLTAGE_L2, volts("230"), CHANNEL_VOLTAGE_L3, volts("230"), CHANNEL_CURRENT_L1,
                        amperes("1.7"), CHANNEL_CURRENT_L2, amperes("1.7"), CHANNEL_CURRENT_L3, amperes("1.7"),
                        CHANNEL_FREQUENCY, hertz("50")), null));
        assertEquals(watts("400"), rec().lastState(CHANNEL_POWER_L3));

        h.handleConfigurationUpdate(Map.of("deviceId", DEVICE_B));

        assertEveryValueChannelUndefined();
        assertEveryCachedValueUndefined(h);
    }

    @Test
    void readingInFlightDeviceIdUnchangedIsApplied() {
        MeterHandler h = start(DEVICE_A, 60);
        h.onReading(DEVICE_A, reading(CHANNEL_POWER, watts("1200")));
        SmlReading assignedBeforeTheChange = reading(CHANNEL_POWER, watts("900"));
        // The thing is initialized again with the same device id.
        h.handleConfigurationUpdate(Map.of("staleTimeout", 20));
        assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE);

        h.onReading(DEVICE_A, assignedBeforeTheChange);

        assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE);
        assertEquals(watts("900"), rec().lastState(CHANNEL_POWER));
    }

    @Test
    void twinOfAReconfiguredMeterDropsTheReadingWhileTheOtherMeterAppliesIt() {
        MeterHandler first = startOnlineMeterWithValues();
        Recorder firstRecorder = rec();
        Thing twinThing = meter(bridge, "m2", DEVICE_A, null);
        Recorder twinRecorder = new Recorder(twinThing, bridge);
        MeterHandler twin = new MeterHandler(twinThing, nanos::get);
        twin.setCallback(twinRecorder.callback);
        twinThing.setHandler(twin);
        twin.initialize();
        try {
            SmlReading assignedToBoth = new SmlReading(values(CHANNEL_POWER, watts("900")), SYNTHETIC_METER_ID);
            first.handleConfigurationUpdate(Map.of("deviceId", DEVICE_B));
            firstRecorder.clear();

            first.onReading(DEVICE_A, assignedToBoth);
            twin.onReading(DEVICE_A, assignedToBoth);

            assertTrue(firstRecorder.updates.isEmpty());
            assertTrue(firstRecorder.statuses.isEmpty());
            assertNull(first.getThing().getProperties().get(PROPERTY_METER_ID));
            assertEquals(ThingStatus.UNKNOWN, first.getThing().getStatus());
            assertEquals(ThingStatus.ONLINE, twin.getThing().getStatus());
            assertEquals(watts("900"), twinRecorder.lastState(CHANNEL_POWER));
            assertEquals(SYNTHETIC_METER_ID, twin.getThing().getProperties().get(PROPERTY_METER_ID));
        } finally {
            twin.dispose();
        }
    }
}
