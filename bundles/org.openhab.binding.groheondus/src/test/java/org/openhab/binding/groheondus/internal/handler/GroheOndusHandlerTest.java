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
package org.openhab.binding.groheondus.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.groheondus.internal.GroheOndusBindingConstants.*;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.groheondus.internal.api.GroheOndusApi;
import org.openhab.binding.groheondus.internal.api.GroheOndusApiException;
import org.openhab.binding.groheondus.internal.api.SenseSample;
import org.openhab.binding.groheondus.internal.api.WaterWithdrawal;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.TimeSeries;

/**
 * Tests for the data processing in the appliance handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
class GroheOndusHandlerTest {
    private static final Instant T0 = Instant.parse("2026-11-17T09:00:00Z");

    private static final List<SenseSample> SAMPLES = List.of(new SenseSample(T0, 20.5f, 40f),
            new SenseSample(T0.plusSeconds(3600), null, 41f), new SenseSample(T0.plusSeconds(7200), 21.5f, 42f));

    @Test
    void allSamplesWithValueAreAddedWithOriginalTimestamps() {
        TimeSeries timeSeries = GroheOndusBaseHandler.buildTimeSeries(SAMPLES, SenseSample::timestamp,
                SenseSample::temperature, SIUnits.CELSIUS, null);

        assertEquals(TimeSeries.Policy.ADD, timeSeries.getPolicy());
        List<TimeSeries.Entry> entries = timeSeries.getStates().toList();
        assertEquals(2, entries.size());
        assertEquals(T0, entries.get(0).timestamp());
        assertEquals(new QuantityType<>(20.5f, SIUnits.CELSIUS), entries.get(0).state());
        assertEquals(T0.plusSeconds(7200), entries.get(1).timestamp());
    }

    @Test
    void alreadyPublishedSamplesAreSkipped() {
        TimeSeries timeSeries = GroheOndusBaseHandler.buildTimeSeries(SAMPLES, SenseSample::timestamp,
                SenseSample::humidity, Units.PERCENT, T0.plusSeconds(3600));

        List<TimeSeries.Entry> entries = timeSeries.getStates().toList();
        assertEquals(1, entries.size());
        assertEquals(T0.plusSeconds(7200), entries.get(0).timestamp());
    }

    @Test
    void waterConsumptionIncludesPeriodStartingAtCutoff() {
        Instant midnight = Instant.parse("2026-11-17T23:00:00Z");
        List<WaterWithdrawal> withdrawals = List.of(new WaterWithdrawal(midnight.minusSeconds(3600), 10.1),
                new WaterWithdrawal(midnight, 2.5), new WaterWithdrawal(midnight.plusSeconds(7 * 3600), 1.4));

        assertEquals(new QuantityType<>("3.9 l"),
                GroheOndusSenseGuardHandler.sumWaterConsumption(withdrawals, midnight));
        assertEquals(new QuantityType<>("14.0 l"),
                GroheOndusSenseGuardHandler.sumWaterConsumption(withdrawals, Instant.MIN));
    }

    @Test
    void deviceIsSilentWhenLastReportIsTooOld() {
        Duration maxAge = Duration.ofHours(48);
        Instant now = Instant.parse("2026-11-18T20:00:00Z");

        assertFalse(GroheOndusBaseHandler.isSilent(now.minus(maxAge), now, maxAge));
        assertTrue(GroheOndusBaseHandler.isSilent(now.minus(maxAge).minusSeconds(1), now, maxAge));
        assertTrue(GroheOndusBaseHandler.isSilent(Instant.parse("2026-01-18T18:25:46Z"), now, maxAge));
        assertTrue(GroheOndusBaseHandler.isSilent(null, now, maxAge));
    }

    private static final ThingUID BRIDGE_UID = new ThingUID(THING_TYPE_BRIDGE_ACCOUNT, "account");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_SENSEGUARD, BRIDGE_UID, "guard");
    private static final ChannelUID VALVE_CHANNEL = new ChannelUID(THING_UID, CHANNEL_VALVE_OPEN);
    private static final ChannelUID PAUSE_CHANNEL = new ChannelUID(THING_UID, CHANNEL_PAUSE);

    private final GroheOndusApi api = mock(GroheOndusApi.class);
    private final List<Runnable> queuedCommands = new ArrayList<>();
    private final GroheOndusSenseGuardHandler handler = createHandler();

    private GroheOndusSenseGuardHandler createHandler() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(THING_UID);
        when(thing.getBridgeUID()).thenReturn(BRIDGE_UID);
        when(thing.getConfiguration()).thenReturn(new Configuration(Map.of("applianceId", "guard-1")));
        when(thing.getChannels()).thenReturn(List.of());

        GroheOndusAccountHandler accountHandler = mock(GroheOndusAccountHandler.class);
        when(accountHandler.getApi()).thenReturn(api);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(bridge.getStatus()).thenReturn(ThingStatus.ONLINE);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);

        // Large thing counter so that the first scheduled poll does not run during the test
        GroheOndusSenseGuardHandler handler = new GroheOndusSenseGuardHandler(thing, 3600,
                mock(TimeZoneProvider.class)) {
            @Override
            void submitCommand(Runnable task) {
                queuedCommands.add(task);
            }
        };
        handler.setCallback(callback);
        return handler;
    }

    @BeforeEach
    void initializeHandler() {
        handler.initialize();
    }

    @AfterEach
    void disposeHandler() {
        handler.dispose();
    }

    private void runQueuedCommands() {
        queuedCommands.forEach(Runnable::run);
        queuedCommands.clear();
    }

    @Test
    void valveCommandRunsWhileHandlerIsActive() throws GroheOndusApiException {
        handler.handleCommand(VALVE_CHANNEL, OnOffType.ON);
        runQueuedCommands();

        verify(api).setValveOpen(any(), eq(true));
    }

    @Test
    void queuedValveCommandIsDiscardedAfterDispose() throws GroheOndusApiException {
        handler.handleCommand(VALVE_CHANNEL, OnOffType.ON);
        handler.dispose();
        runQueuedCommands();

        verify(api, never()).setValveOpen(any(), anyBoolean());
    }

    @Test
    void queuedCommandsAreDiscardedAfterReinitialize() throws GroheOndusApiException {
        handler.handleCommand(VALVE_CHANNEL, OnOffType.ON);
        handler.handleCommand(PAUSE_CHANNEL, new QuantityType<>(30, Units.MINUTE));
        handler.dispose();
        handler.initialize();
        runQueuedCommands();

        verify(api, never()).setValveOpen(any(), anyBoolean());
        verify(api, never()).setPause(any(), anyInt());
    }
}
