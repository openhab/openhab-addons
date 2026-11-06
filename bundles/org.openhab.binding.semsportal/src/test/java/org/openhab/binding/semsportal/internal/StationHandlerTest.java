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
package org.openhab.binding.semsportal.internal;

import static org.mockito.Mockito.*;
import static org.openhab.binding.semsportal.internal.SEMSPortalBindingConstants.*;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.semsportal.internal.dto.StationStatus;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests how the status of the power station reaches the Thing and its channels, and the lifecycle of the task that
 * refreshes it. The portal is replaced by a mock; the handler runs its tasks on the framework's scheduler, so the
 * results are awaited with a bounded timeout.
 *
 * @author Julio Vilmar Gesser - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StationHandlerTest {
    private static final String STATION = "00000000-0000-0000-0000-0000000000a1";
    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_PORTAL, "test");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_STATION, BRIDGE_UID, STATION);
    private static final long TIMEOUT_MS = 10_000;

    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) PortalHandler portal;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;

    private @NonNullByDefault({}) Thing thing;
    private @NonNullByDefault({}) StationHandler handler;

    @BeforeEach
    public void setUp() throws Exception {
        List<Channel> channels = ALL_CHANNELS.stream()
                .map(id -> ChannelBuilder.create(new ChannelUID(THING_UID, id), "Number").build())
                .collect(Collectors.toList());
        thing = ThingBuilder.create(THING_TYPE_STATION, THING_UID).withBridge(BRIDGE_UID)
                .withProperties(Map.of(STATION_UUID, STATION)).withChannels(channels).build();
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(bridge.getHandler()).thenReturn(portal);
        when(portal.getUpdateInterval()).thenReturn(5L);
        when(portal.getStationStatus(STATION)).thenReturn(status(true));

        handler = new StationHandler(thing);
        handler.setCallback(callback);
    }

    @AfterEach
    public void tearDown() {
        // cancels the task that refreshes the status
        handler.dispose();
    }

    @Test
    public void testOnlineWithChannels() {
        handler.initialize();

        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        verifyState(CHANNEL_CURRENT_OUTPUT, new QuantityType<>(2044.0, Units.WATT));
        verifyState(CHANNEL_TODAY_TOTAL, new QuantityType<>(9.1, Units.KILOWATT_HOUR));
        verifyState(CHANNEL_MONTH_TOTAL, new QuantityType<>(789.6, Units.KILOWATT_HOUR));
        verifyState(CHANNEL_OVERALL_TOTAL, new QuantityType<>(49119.3, Units.KILOWATT_HOUR));
        verifyState(CHANNEL_TODAY_INCOME, new DecimalType(10.2));
        verifyState(CHANNEL_TOTAL_INCOME, new DecimalType(31965.0));
        verifyState(CHANNEL_LASTUPDATE, new DateTimeType(lastUpdate()));
    }

    @Test
    public void testOfflineWhenStationIsNotOperational() throws Exception {
        when(portal.getStationStatus(STATION)).thenReturn(status(false));

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Station not operational");
        // the counters are known while the station is not producing
        verifyState(CHANNEL_OVERALL_TOTAL, new QuantityType<>(49119.3, Units.KILOWATT_HOUR));
    }

    @Test
    public void testOfflineWithoutStatus() throws Exception {
        // the portal gives no status when it refuses the credentials
        when(portal.getStationStatus(STATION)).thenReturn(null);

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, "Station not operational");
        verifyState(CHANNEL_CURRENT_OUTPUT, UnDefType.UNDEF);
    }

    @Test
    public void testCommunicationError() throws Exception {
        when(portal.getStationStatus(STATION)).thenThrow(new CommunicationException("No response received"));

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No response received");
    }

    @Test
    public void testConfigurationError() throws Exception {
        when(portal.getStationStatus(STATION)).thenThrow(new ConfigurationException("Check your station ID"));

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Check your station ID");
    }

    @Test
    public void testBridgeWithoutPortal() {
        when(bridge.getHandler()).thenReturn(null);

        handler.initialize();

        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                "Unable to update station info. Check Bridge status for details.");
    }

    @Test
    public void testRefreshCommandUpdatesTheChannel() {
        handler.handleCommand(new ChannelUID(THING_UID, CHANNEL_CURRENT_OUTPUT), RefreshType.REFRESH);

        verifyState(CHANNEL_CURRENT_OUTPUT, new QuantityType<>(2044.0, Units.WATT));
    }

    @Test
    public void testRefreshOfAllChannelsFetchesTheStatusOnce() throws Exception {
        // the framework sends a REFRESH for each linked channel when it starts
        ALL_CHANNELS.forEach(id -> handler.handleCommand(new ChannelUID(THING_UID, id), RefreshType.REFRESH));

        verifyState(CHANNEL_CURRENT_OUTPUT, new QuantityType<>(2044.0, Units.WATT));
        verifyState(CHANNEL_LASTUPDATE, new DateTimeType(lastUpdate()));
        verify(portal, times(1)).getStationStatus(STATION);
    }

    @Test
    public void testRefreshUsesTheRecentStatus() throws Exception {
        handler.initialize();
        verifyState(CHANNEL_CURRENT_OUTPUT, new QuantityType<>(2044.0, Units.WATT));

        handler.handleCommand(new ChannelUID(THING_UID, CHANNEL_CURRENT_OUTPUT), RefreshType.REFRESH);

        // once by the fetch and once by the command
        verify(callback, timeout(TIMEOUT_MS).times(2)).stateUpdated(new ChannelUID(THING_UID, CHANNEL_CURRENT_OUTPUT),
                new QuantityType<>(2044.0, Units.WATT));
        verify(portal, times(1)).getStationStatus(STATION);
    }

    @Test
    public void testRefreshFetchesAgainAfterAFailure() throws Exception {
        when(portal.getStationStatus(STATION)).thenThrow(new CommunicationException("No response received"))
                .thenReturn(status(true));
        ChannelUID channel = new ChannelUID(THING_UID, CHANNEL_CURRENT_OUTPUT);

        handler.handleCommand(channel, RefreshType.REFRESH);
        verifyStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "No response received");
        handler.handleCommand(channel, RefreshType.REFRESH);

        verifyStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        verify(portal, times(2)).getStationStatus(STATION);
    }

    @Test
    public void testTasksRestartWhenTheBridgeIsBackAfterAConfigurationError() throws Exception {
        handler.initialize();
        verify(portal, timeout(TIMEOUT_MS).times(1)).getStationStatus(STATION);

        // a rejected login stops the updates, until the bridge is online again
        handler.bridgeStatusChanged(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Check username"));
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        verify(portal, timeout(TIMEOUT_MS).times(2)).getStationStatus(STATION);
    }

    @Test
    public void testTasksOnlyRestartByInitializeAfterDispose() throws Exception {
        handler.initialize();
        verify(portal, timeout(TIMEOUT_MS).times(1)).getStationStatus(STATION);

        handler.dispose();
        // a late notification of the bridge does not bring the updates back
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verify(portal, after(500).times(1)).getStationStatus(STATION);

        handler.initialize();
        verify(portal, timeout(TIMEOUT_MS).times(2)).getStationStatus(STATION);
    }

    @Test
    public void testStartupFetchesTheStatusOnce() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        slowPortal(release);

        handler.initialize();
        verify(portal, timeout(TIMEOUT_MS).times(1)).getStationStatus(STATION);
        // the framework sends the REFRESH commands while the periodic task is still fetching
        ALL_CHANNELS.forEach(id -> handler.handleCommand(new ChannelUID(THING_UID, id), RefreshType.REFRESH));
        verify(portal, after(500).times(1)).getStationStatus(STATION);

        release.countDown();
        verifyState(CHANNEL_CURRENT_OUTPUT, new QuantityType<>(2044.0, Units.WATT));
        verify(portal, times(1)).getStationStatus(STATION);
    }

    @Test
    public void testStatusFetchedWhenTheBridgeStopsTheUpdatesIsNotPublished() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        slowPortal(release);
        handler.initialize();
        verify(portal, timeout(TIMEOUT_MS).times(1)).getStationStatus(STATION);

        // the Bridge reports that the login is refused while the status is being fetched
        handler.bridgeStatusChanged(
                new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Check username"));
        release.countDown();

        verify(callback, after(500).never()).stateUpdated(any(), any());
        verify(callback, never()).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    public void testStatusFetchedBeforeDisposeIsNotPublished() throws Exception {
        CountDownLatch release = new CountDownLatch(1);
        slowPortal(release);
        handler.initialize();
        verify(portal, timeout(TIMEOUT_MS).times(1)).getStationStatus(STATION);

        handler.dispose();
        release.countDown();

        verify(callback, after(500).never()).stateUpdated(any(), any());
        verify(callback, never()).statusUpdated(eq(thing), argThat(info -> info.getStatus() == ThingStatus.ONLINE));
    }

    /**
     * The portal answers when the latch is released, also when the task that waits for it is cancelled.
     */
    private void slowPortal(CountDownLatch release) throws Exception {
        when(portal.getStationStatus(STATION)).thenAnswer(invocation -> {
            try {
                release.await(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return status(true);
        });
    }

    private ZonedDateTime lastUpdate() {
        return ZonedDateTime.of(2026, 11, 1, 10, 44, 0, 0, ZoneId.systemDefault());
    }

    private StationStatus status(boolean operational) {
        return new StationStatus(2044.0, 9.1, 789.6, 49119.3, 10.2, 31965.0, operational, lastUpdate());
    }

    private void verifyStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
        verify(callback, timeout(TIMEOUT_MS).atLeastOnce()).statusUpdated(eq(thing),
                argThat(info -> info.getStatus() == status && info.getStatusDetail() == detail
                        && (description == null || description.equals(info.getDescription()))));
    }

    private void verifyState(String channel, State state) {
        verify(callback, timeout(TIMEOUT_MS).atLeastOnce()).stateUpdated(new ChannelUID(THING_UID, channel), state);
    }
}
