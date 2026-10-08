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
package org.openhab.binding.netatmo.internal.handler.capability;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.Invocation;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.netatmo.internal.api.ApiError;
import org.openhab.binding.netatmo.internal.api.NetatmoException;
import org.openhab.binding.netatmo.internal.api.dto.NAThing;
import org.openhab.binding.netatmo.internal.handler.CommonInterface;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Martin Littkovsky - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class RefreshAutoCapabilityTest {

    private static final ThingTypeUID WEATHER_STATION = new ThingTypeUID("netatmo", "weather-station");
    private static final ThingUID STATION = new ThingUID(WEATHER_STATION, "station");
    private static final Duration SLOT = Duration.ofSeconds(7);
    private static final NetatmoException BUSY = new NetatmoException(new ApiError(), 503, "27");
    private static final Instant ROUND_TEN_MINUTES = Instant.parse("2026-09-23T09:00:00Z");
    private static final Instant ROUND_FIVE_MINUTES = ROUND_TEN_MINUTES.plusSeconds(300);
    private static final Instant OUTSIDE_BUSY_WINDOW = ROUND_TEN_MINUTES.plusSeconds(100);

    private @Mock @NonNullByDefault({}) CommonInterface handler;
    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) NAThing station;
    private @NonNullByDefault({}) RefreshAutoCapability refresh;

    @BeforeEach
    void setUp() {
        when(thing.getUID()).thenReturn(STATION);
        when(thing.getThingTypeUID()).thenReturn(WEATHER_STATION);
        when(handler.getThing()).thenReturn(thing);
        refresh = new RefreshAutoCapability(handler);
    }

    @Test
    void retriesWithDoublingDelayAfterFailures() {
        refresh.fetchFailed(BUSY);
        assertEquals(Duration.ofSeconds(30), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));

        refresh.fetchFailed(BUSY);
        assertEquals(Duration.ofSeconds(60), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));

        refresh.fetchFailed(BUSY);
        assertEquals(Duration.ofMinutes(2), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));

        refresh.fetchFailed(BUSY);
        assertEquals(Duration.ofMinutes(2), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));
    }

    @Test
    void backoffStaysAtTwoMinutesAfterManyAttempts() {
        assertEquals(Duration.ofMinutes(2), RefreshAutoCapability.backoffDelay(40));
        assertEquals(Duration.ofMinutes(2), RefreshAutoCapability.backoffDelay(Integer.MAX_VALUE));
    }

    @Test
    void probesWithoutDataBackOffToTwoMinutes() {
        assertEquals(Duration.ofSeconds(30), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));
        assertEquals(Duration.ofSeconds(60), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));
        assertEquals(Duration.ofMinutes(2), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));
        assertEquals(Duration.ofMinutes(2), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));
    }

    @Test
    void newDataRestartsTheProbeSteps() {
        Instant lastSeen = OUTSIDE_BUSY_WINDOW.minusSeconds(300);
        when(station.getLastSeen()).thenReturn(ZonedDateTime.ofInstant(lastSeen, ZoneOffset.UTC));
        refresh.setInterval(Duration.ofMinutes(10));
        refresh.delayFrom(OUTSIDE_BUSY_WINDOW);
        refresh.delayFrom(OUTSIDE_BUSY_WINDOW);

        refresh.updateNAThing(station);
        assertEquals(Duration.ofSeconds(315), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));

        assertEquals(Duration.ofSeconds(30), refresh.delayFrom(OUTSIDE_BUSY_WINDOW.plusSeconds(400)));
    }

    @Test
    void disposeRestartsTheProbeSteps() {
        refresh.delayFrom(OUTSIDE_BUSY_WINDOW);
        refresh.delayFrom(OUTSIDE_BUSY_WINDOW);

        refresh.dispose();

        assertEquals(Duration.ofSeconds(30), refresh.delayFrom(OUTSIDE_BUSY_WINDOW));
    }

    @Test
    void failedPollInTheBusyWindowIsRetriedInTheSlotOfItsThing() {
        Duration untilWindowEnd = Duration.ofSeconds(55);
        refresh.fetchFailed(BUSY);

        assertEquals(untilWindowEnd.plus(RefreshAutoCapability.slotAfterBusyWindow(STATION)),
                refresh.delayFrom(ROUND_TEN_MINUTES.plusSeconds(5)));
    }

    @Test
    void pollInTheBusyWindowMovesToItsSlotBehindTheWindow() {
        Instant planned = ROUND_TEN_MINUTES.plusSeconds(600 * 3 + 12).plusMillis(500);
        Instant windowEnd = ROUND_TEN_MINUTES.plusSeconds(600 * 3 + 60);

        assertEquals(windowEnd, RefreshAutoCapability.outsideBusyWindow(planned, Duration.ZERO));
        assertEquals(windowEnd.plus(SLOT), RefreshAutoCapability.outsideBusyWindow(planned, SLOT));
    }

    @Test
    void slotBehindTheWindowIsFixedPerThingAndShorterThanTheSpread() {
        Duration slot = RefreshAutoCapability.slotAfterBusyWindow(STATION);

        assertEquals(slot, RefreshAutoCapability.slotAfterBusyWindow(new ThingUID(WEATHER_STATION, "station")));
        assertNotEquals(slot, RefreshAutoCapability.slotAfterBusyWindow(new ThingUID(WEATHER_STATION, "other")));
        assertFalse(slot.isNegative());
        assertTrue(slot.compareTo(Duration.ofSeconds(15)) < 0);
    }

    @Test
    void busyWindowAfterRoundFiveMinutesIsShorter() {
        Instant planned = ROUND_FIVE_MINUTES.plusSeconds(600 * 3 + 12).plusMillis(500);
        Instant atWindowEnd = ROUND_FIVE_MINUTES.plusSeconds(20);

        assertEquals(ROUND_FIVE_MINUTES.plusSeconds(600 * 3 + 20),
                RefreshAutoCapability.outsideBusyWindow(planned, Duration.ZERO));
        assertEquals(atWindowEnd, RefreshAutoCapability.outsideBusyWindow(atWindowEnd, SLOT));
    }

    @Test
    void pollOutsideTheBusyWindowStays() {
        Instant atWindowEnd = ROUND_TEN_MINUTES.plusSeconds(60);
        Instant beforeRoundFiveMinutes = ROUND_TEN_MINUTES.plusSeconds(299).plusMillis(999);
        Instant beforeRoundTenMinutes = ROUND_TEN_MINUTES.plusSeconds(599).plusMillis(999);

        assertEquals(atWindowEnd, RefreshAutoCapability.outsideBusyWindow(atWindowEnd, SLOT));
        assertEquals(beforeRoundFiveMinutes, RefreshAutoCapability.outsideBusyWindow(beforeRoundFiveMinutes, SLOT));
        assertEquals(beforeRoundTenMinutes, RefreshAutoCapability.outsideBusyWindow(beforeRoundTenMinutes, SLOT));
    }

    @Test
    void thingGoesOfflineOnlyAtTheFifthFailureInARow() {
        failPolls(4);
        verify(handler, never()).setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                BUSY.getMessage());

        failPolls(1);
        verify(handler, times(1)).setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                BUSY.getMessage());

        failPolls(1);
        verify(handler, times(1)).setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                BUSY.getMessage());
    }

    @Test
    void thingGoesOfflineAgainAfterARestartDuringAFailureStreak() {
        failPolls(5);
        refresh.dispose();
        failPolls(5);

        verify(handler, times(2)).setThingStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                BUSY.getMessage());
    }

    @Test
    void newDataEndsAFailureStreak() {
        refresh.fetchFailed(BUSY);

        refresh.updateNAThing(new NAThing());

        assertFalse(refresh.isRetrying());
    }

    @Test
    void retryingKeepsItsShortDelayWhileTheThingIsOffline() {
        when(thing.getStatus()).thenReturn(ThingStatus.OFFLINE);
        refresh.fetchFailed(BUSY);
        refresh.expireData();

        lastScheduledJob().run();

        assertTrue(lastScheduledDelay().compareTo(Duration.ofMinutes(2)) < 0);
    }

    @Test
    void failedRefreshDoesNotBlockTheNextExpiry() {
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        refresh.expireData();
        lastScheduledJob().run();

        refresh.expireData();

        assertEquals(RefreshCapability.ASAP, lastScheduledDelay());
    }

    @Test
    void expiryDuringARunningRefreshDoesNotStartAnotherOne() {
        when(thing.getStatus()).thenReturn(ThingStatus.ONLINE);
        doAnswer(invocation -> {
            refresh.expireData();
            return null;
        }).when(handler).proceedWithUpdate();
        refresh.expireData();

        lastScheduledJob().run();

        assertEquals(1,
                scheduleCalls().stream().filter(call -> RefreshCapability.ASAP.equals(call.getArguments()[1])).count());
    }

    @Test
    void refreshIsScheduledAgainAfterDisposeAndInitialize() {
        refresh.expireData();
        refresh.dispose();

        refresh.setInterval(Duration.ofMinutes(10));

        assertEquals(List.of(RefreshCapability.ASAP, RefreshCapability.ASAP),
                scheduleCalls().stream().map(call -> call.getArguments()[1]).toList());
    }

    private void failPolls(int count) {
        for (int poll = 0; poll < count; poll++) {
            refresh.fetchFailed(BUSY);
        }
    }

    private List<Invocation> scheduleCalls() {
        return mockingDetails(handler).getInvocations().stream()
                .filter(call -> "schedule".equals(call.getMethod().getName())).toList();
    }

    private Runnable lastScheduledJob() {
        return (Runnable) scheduleCalls().getLast().getArguments()[0];
    }

    private Duration lastScheduledDelay() {
        return (Duration) scheduleCalls().getLast().getArguments()[1];
    }
}
