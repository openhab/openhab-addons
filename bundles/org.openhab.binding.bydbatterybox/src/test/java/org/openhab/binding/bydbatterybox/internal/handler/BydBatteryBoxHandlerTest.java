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
package org.openhab.binding.bydbatterybox.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.bydbatterybox.internal.BydBatteryBoxBindingConstants.*;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.bydbatterybox.internal.FakeBattery;
import org.openhab.binding.bydbatterybox.internal.Fixtures;
import org.openhab.binding.bydbatterybox.internal.dto.EventRecord;
import org.openhab.binding.bydbatterybox.internal.protocol.BydClient;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.osgi.framework.Bundle;

/**
 * Tests {@link BydBatteryBoxHandler} against a {@link FakeBattery}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Timeout(value = 30, unit = TimeUnit.SECONDS)
class BydBatteryBoxHandlerTest {

    private static final long FIRST_CYCLE_TIMEOUT_MILLIS = 15_000;

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) TranslationProvider i18nProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;
    private @Mock @NonNullByDefault({}) Bundle bundle;

    private @Nullable BydBatteryBoxHandler handler;
    private @Nullable FakeBattery battery;

    @AfterEach
    void tearDown() throws Exception {
        BydBatteryBoxHandler localHandler = handler;
        if (localHandler != null) {
            localHandler.dispose();
        }
        FakeBattery localBattery = battery;
        if (localBattery != null) {
            localBattery.close();
        }
    }

    private BydBatteryBoxHandler createHandler(int port) {
        return createHandler(port, List.of());
    }

    private BydBatteryBoxHandler createHandler(int port, List<Channel> channels) {
        // only the status cycle: cell details and event logs are covered by the protocol tests
        return createHandler(port, channels, Map.of("detailInterval", BigDecimal.ZERO));
    }

    private BydBatteryBoxHandler createHandler(int port, List<Channel> channels, Map<String, Object> parameters) {
        // the handler waits for real only for its first cycle, not between requests or before a retry
        return createHandler(port, channels, parameters, millis -> {
        });
    }

    private BydBatteryBoxHandler createHandler(int port, List<Channel> channels, Map<String, Object> parameters,
            BydClient.Sleeper sleeper) {
        Map<String, Object> properties = new HashMap<>(Map.of("hostname", "127.0.0.1", "port", BigDecimal.valueOf(port),
                "refreshInterval", BigDecimal.valueOf(30), "eventLogInterval", BigDecimal.ZERO, "connectionTimeout",
                BigDecimal.valueOf(2)));
        properties.putAll(parameters);
        Thing thing = ThingBuilder.create(THING_TYPE_HV_PREMIUM, "test")
                .withConfiguration(new Configuration(properties)).withChannels(channels).build();
        BydBatteryBoxHandler localHandler = new BydBatteryBoxHandler(thing, i18nProvider, localeProvider, bundle,
                sleeper);
        localHandler.setCallback(callback);
        handler = localHandler;
        return localHandler;
    }

    /**
     * Identity and status, then the cell details of both towers of the HVS with 2 towers of 5 modules each. The status
     * reply is the one of the HVS 7.7, because it was not captured on the 2-tower system.
     */
    private static byte[][] twoTowerDetailReplies(boolean secondPass) {
        List<byte[]> replies = new ArrayList<>(List.of(Fixtures.frame("hvs2x5-identity"), Fixtures.frame("status")));
        for (int tower = 1; tower <= 2; tower++) {
            String prefix = "hvs2x5-t" + tower;
            replies.addAll(List.of(Fixtures.frame("measurement-start-echo"), Fixtures.frame("measurement-state-ready"),
                    Fixtures.frame(prefix + "-measurement-1"), Fixtures.frame(prefix + "-measurement-2"),
                    Fixtures.frame(prefix + "-measurement-3"), Fixtures.frame(prefix + "-measurement-4")));
            if (secondPass) {
                replies.addAll(List.of(Fixtures.frame("second-pass-echo"), Fixtures.frame("measurement-start-echo"),
                        Fixtures.frame("measurement-state-ready"), Fixtures.frame(prefix + "-second-pass-1"),
                        Fixtures.frame(prefix + "-second-pass-2")));
            }
        }
        return replies.toArray(byte[][]::new);
    }

    private void verifyState(ChannelUID channel, String expected) {
        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).stateUpdated(eq(channel),
                argThat(state -> expected.equals(state.toString())));
    }

    @Test
    void detailCycleOfTwoTowersWithExtendedCells() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, twoTowerDetailReplies(true));
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port(), List.of(),
                Map.of("detailInterval", BigDecimal.valueOf(600), "readExtendedCells", true));
        ThingUID thingUID = localHandler.getThing().getUID();

        localHandler.initialize();

        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS))
                .stateUpdated(eq(new ChannelUID(thingUID, GROUP_DIAGNOSTICS, CHANNEL_LAST_DETAIL_UPDATE)), any());
        verifyState(new ChannelUID(thingUID, "tower-1-module-1", "cell-voltage-01"), "3207 mV");
        verifyState(new ChannelUID(thingUID, "tower-1-module-5", "cell-voltage-01"), "3203 mV");
        verifyState(new ChannelUID(thingUID, "tower-2-module-5", "cell-voltage-32"), "3205 mV");
        verifyState(new ChannelUID(thingUID, "tower-2-module-5", "cell-temperature-12"), "14 °C");
        verifyState(new ChannelUID(thingUID, "tower-2", "state-of-charge"), "15.8 %");
        assertEquals(2 + 2 * 11, localBattery.requests().size());
    }

    @Test
    void detailCycleOfTwoTowersWithoutExtendedCells() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, twoTowerDetailReplies(false));
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port(), List.of(),
                Map.of("detailInterval", BigDecimal.valueOf(600)));
        ThingUID thingUID = localHandler.getThing().getUID();

        localHandler.initialize();

        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS))
                .stateUpdated(eq(new ChannelUID(thingUID, GROUP_DIAGNOSTICS, CHANNEL_LAST_DETAIL_UPDATE)), any());
        verifyState(new ChannelUID(thingUID, "tower-2-module-4", "cell-voltage-32"), "3205 mV");
        verifyState(new ChannelUID(thingUID, "tower-2-module-5", "cell-voltage-01"), "UNDEF");
        verifyState(new ChannelUID(thingUID, "tower-2-module-5", "voltage"), "UNDEF");
        // the temperatures of module 5 are part of the first pass
        verifyState(new ChannelUID(thingUID, "tower-2-module-5", "cell-temperature-12"), "14 °C");
        assertEquals(2 + 2 * 6, localBattery.requests().size());
    }

    @Test
    void eventLogRequestsAreAtLeastTheMinimumSpacingApart() {
        Instant now = Instant.parse("2026-11-22T12:00:00Z");
        Instant minSpacingAgo = now.minus(BydBatteryBoxHandler.MIN_EVENT_LOG_SPACING);

        assertTrue(BydBatteryBoxHandler.isEventLogDue(null, null, 600, 60, now));
        // a configured interval shorter than the minimum spacing is stretched to it
        assertFalse(BydBatteryBoxHandler.isEventLogDue(now.minusSeconds(60), now.minusSeconds(60), 30, 30, now));
        assertTrue(BydBatteryBoxHandler.isEventLogDue(minSpacingAgo, minSpacingAgo, 30, 30, now));
        // after a failed read, the next attempt waits for the minimum spacing as well
        assertFalse(BydBatteryBoxHandler.isEventLogDue(now.minusSeconds(700), now.minusSeconds(20), 600, 60, now));
        assertTrue(BydBatteryBoxHandler.isEventLogDue(now.minusSeconds(700), minSpacingAgo, 600, 60, now));
        assertFalse(BydBatteryBoxHandler.isEventLogDue(null, null, 0, 60, now));
    }

    @Test
    void capacityIsCalculatedFromTopologyAndHealth() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, twoTowerDetailReplies(false));
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port(), List.of(),
                Map.of("detailInterval", BigDecimal.valueOf(600)));
        ThingUID thingUID = localHandler.getThing().getUID();

        localHandler.initialize();

        // 2 towers with 5 HVS modules of 2.56 kWh; system SoH 94 %, SoC 6 %; tower SoH 95 % and 98 %
        verifyState(new ChannelUID(thingUID, GROUP_STATUS, CHANNEL_USABLE_CAPACITY), "24.06 kWh");
        verifyState(new ChannelUID(thingUID, GROUP_STATUS, CHANNEL_STORED_ENERGY), "1.44 kWh");
        verifyState(new ChannelUID(thingUID, "tower-1", CHANNEL_USABLE_CAPACITY), "12.16 kWh");
        verifyState(new ChannelUID(thingUID, "tower-2", CHANNEL_USABLE_CAPACITY), "12.54 kWh");
        assertEquals("25.6 kWh", localHandler.getThing().getProperties().get(PROPERTY_NOMINAL_CAPACITY));
        assertEquals("1", localHandler.getThing().getProperties().get(PROPERTY_PHASE));
    }

    @Test
    void capacityIsUndefinedForAnUnknownBatteryType() throws Exception {
        // identity byte 37 is the module type; 0 is HVL, whose module capacity is not known
        FakeBattery localBattery = FakeBattery.start(64, Fixtures.frameWithByte("identity", 37, 0),
                Fixtures.frame("status"));
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port());
        ThingUID thingUID = localHandler.getThing().getUID();

        localHandler.initialize();

        verifyState(new ChannelUID(thingUID, GROUP_STATUS, CHANNEL_USABLE_CAPACITY), "UNDEF");
        verifyState(new ChannelUID(thingUID, GROUP_STATUS, CHANNEL_STORED_ENERGY), "UNDEF");
        assertEquals(null, localHandler.getThing().getProperties().get(PROPERTY_NOMINAL_CAPACITY));
    }

    @Test
    void refreshRepublishesTheLastState() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, Fixtures.frame("identity"), Fixtures.frame("status"));
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port());
        ChannelUID stateOfCharge = new ChannelUID(localHandler.getThing().getUID(), GROUP_STATUS,
                CHANNEL_STATE_OF_CHARGE);
        QuantityType<?> expected = new QuantityType<>(6, Units.PERCENT);

        localHandler.initialize();
        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        verify(callback, times(1)).stateUpdated(stateOfCharge, expected);

        localHandler.handleCommand(stateOfCharge, RefreshType.REFRESH);
        verify(callback, times(2)).stateUpdated(stateOfCharge, expected);
    }

    @Test
    void channelsAreReducedToTheReportedTopology() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, Fixtures.frame("identity"), Fixtures.frame("status"));
        battery = localBattery;
        ThingUID thingUID = new ThingUID(THING_TYPE_HV_PREMIUM, "test");
        List<Channel> channels = Stream
                .of("status#voltage", "tower-1#voltage", "tower-2#voltage", "tower-1-module-3#cell-voltage-32",
                        "tower-1-module-4#cell-voltage-01")
                .map(id -> ChannelBuilder.create(new ChannelUID(thingUID, id)).build()).toList();
        // the framework would create all channels of a group; one channel per group is enough to show what is
        // added: a missing channel of module 1 and a missing status channel (e.g. lost by an earlier version)
        when(callback.createChannelBuilders(any(), any())).thenAnswer(invocation -> {
            ChannelGroupUID group = invocation.getArgument(0);
            return switch (group.getId()) {
                case "tower-1-module-1" -> List.of(ChannelBuilder.create(new ChannelUID(group, "cell-voltage-01")));
                case "status" -> List.of(ChannelBuilder.create(new ChannelUID(group, "state-of-charge")),
                        ChannelBuilder.create(new ChannelUID(group, "voltage")));
                default -> List.of();
            };
        });
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port(), channels);

        localHandler.initialize();

        Set<String> expected = Set.of("status#voltage", "status#state-of-charge", "tower-1#voltage",
                "tower-1-module-3#cell-voltage-32", "tower-1-module-1#cell-voltage-01");
        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS).atLeastOnce()).thingUpdated(argThat(thing -> expected
                .equals(thing.getChannels().stream().map(c -> c.getUID().getId()).collect(Collectors.toSet()))));
    }

    @Test
    void refreshBeforeTheFirstReadingPublishesNothing() {
        BydBatteryBoxHandler localHandler = createHandler(1);
        ChannelUID stateOfCharge = new ChannelUID(localHandler.getThing().getUID(), GROUP_STATUS,
                CHANNEL_STATE_OF_CHARGE);

        localHandler.handleCommand(stateOfCharge, RefreshType.REFRESH);

        verify(callback, never()).stateUpdated(any(), any());
    }

    @Test
    void lastEventIsPublishedBeforeTheEventTriggers() {
        BydBatteryBoxHandler localHandler = createHandler(1);
        ThingUID thingUID = localHandler.getThing().getUID();
        EventRecord older = new EventRecord(EventRecord.UNIT_BMU, 32, LocalDateTime.of(2026, 11, 16, 7, 42, 2),
                new byte[23]);
        EventRecord newer = new EventRecord(1, 38, LocalDateTime.of(2026, 11, 16, 7, 42, 4), new byte[23]);
        ChannelUID lastEvent = new ChannelUID(thingUID, GROUP_EVENTS, CHANNEL_LAST_EVENT);
        ChannelUID lastEventTime = new ChannelUID(thingUID, GROUP_EVENTS, CHANNEL_LAST_EVENT_TIME);
        ChannelUID event = new ChannelUID(thingUID, GROUP_EVENTS, CHANNEL_EVENT);

        localHandler.publishEvents(List.of(older, newer), newer, Duration.ZERO);

        InOrder inOrder = inOrder(callback);
        inOrder.verify(callback).stateUpdated(eq(lastEvent), argThat(state -> state.toString().startsWith("BMS 1: ")));
        inOrder.verify(callback).stateUpdated(eq(lastEventTime), any());
        inOrder.verify(callback).channelTriggered(any(), eq(event), eq("BMU:32"));
        inOrder.verify(callback).channelTriggered(any(), eq(event), eq("BMS1:38"));
    }

    @Test
    void busyBatteryGoesOfflineWithCommunicationError() throws Exception {
        FakeBattery localBattery = FakeBattery.start(64, FakeBattery.CLOSE);
        battery = localBattery;
        BydBatteryBoxHandler localHandler = createHandler(localBattery.port());

        localHandler.initialize();

        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).statusUpdated(any(), argThat(
                info -> info.getStatus() == ThingStatus.OFFLINE && "@text/offline.busy".equals(info.getDescription())));
    }

    @Test
    void shortOutageIsBridgedByOneRetry() throws Exception {
        int port = freePort();
        List<Long> sleeps = new CopyOnWriteArrayList<>();
        // the battery is unreachable at first and answers again when the handler retries
        BydBatteryBoxHandler localHandler = createHandler(port, List.of(), Map.of("detailInterval", BigDecimal.ZERO),
                millis -> {
                    // the client also waits between requests
                    if (millis != BydBatteryBoxHandler.RETRY_DELAY_MILLIS) {
                        return;
                    }
                    sleeps.add(millis);
                    try {
                        battery = FakeBattery.startOnPort(port, 64, Fixtures.frame("identity"),
                                Fixtures.frame("status"));
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });

        localHandler.initialize();

        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.ONLINE));
        verify(callback, never()).statusUpdated(any(), argThat(info -> info.getStatus() == ThingStatus.OFFLINE));
        assertEquals(List.of(BydBatteryBoxHandler.RETRY_DELAY_MILLIS), sleeps);
    }

    @Test
    void unreachableBatteryGoesOfflineAfterOneRetry() throws Exception {
        int port = freePort();
        List<Long> sleeps = new CopyOnWriteArrayList<>();
        BydBatteryBoxHandler localHandler = createHandler(port, List.of(), Map.of("detailInterval", BigDecimal.ZERO),
                sleeps::add);

        localHandler.initialize();

        verify(callback, timeout(FIRST_CYCLE_TIMEOUT_MILLIS)).statusUpdated(any(),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && "@text/offline.unreachable".equals(info.getDescription())));
        assertEquals(List.of(BydBatteryBoxHandler.RETRY_DELAY_MILLIS), sleeps);
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getLoopbackAddress())) {
            return socket.getLocalPort();
        }
    }
}
