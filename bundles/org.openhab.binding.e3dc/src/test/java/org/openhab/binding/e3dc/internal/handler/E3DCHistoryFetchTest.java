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
package org.openhab.binding.e3dc.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.e3dc.internal.E3DCBindingConstants;
import org.openhab.binding.e3dc.internal.E3DCCommandOptionsProvider;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.binding.e3dc.internal.rscp.RscpValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;
import org.openhab.core.types.TimeSeries;

/**
 * Tests the history fetch: the rule that decides whether the device holds history for a period, the encoding of
 * the period requests and the states the fetched values are published as.
 *
 * <p>
 * The fetch goes back year by year and stops at the periods before the installation, so a period the device
 * answers with zeros must not count as a period with data.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class E3DCHistoryFetchTest {

    @Test
    void aZeroDecimalIsNoData() {
        assertFalse(E3DCPowerPlantHandler.isNonZero(DecimalType.ZERO));
    }

    @Test
    void aZeroQuantityIsNoData() {
        assertFalse(E3DCPowerPlantHandler.isNonZero(new QuantityType<>(0, Units.KILOWATT_HOUR)));
    }

    @Test
    void aNonZeroNumberIsData() {
        assertTrue(E3DCPowerPlantHandler.isNonZero(new DecimalType(1)));
        assertTrue(E3DCPowerPlantHandler.isNonZero(new QuantityType<>(12.5, Units.KILOWATT_HOUR)));
    }

    @Test
    void aStateThatIsNoNumberCountsAsData() {
        assertTrue(E3DCPowerPlantHandler.isNonZero(new StringType("x")));
    }

    @Test
    void twoEmptyPeriodsInARowStopTheFetch() {
        E3DCPowerPlantHandler.EmptyPeriods periods = new E3DCPowerPlantHandler.EmptyPeriods(2);
        assertFalse(periods.exhausted(), "the fetch starts with an unused run");
        periods.record(false);
        assertFalse(periods.exhausted(), "a single period without data is tolerated");
        periods.record(false);
        assertTrue(periods.exhausted(), "two periods in a row without data end the fetch");
    }

    @Test
    void dataEndsTheRunOfEmptyPeriods() {
        E3DCPowerPlantHandler.EmptyPeriods periods = new E3DCPowerPlantHandler.EmptyPeriods(2);
        periods.record(false);
        periods.record(true);
        periods.record(false);
        assertFalse(periods.exhausted(), "data in between starts a new run of empty periods");
        periods.record(false);
        assertTrue(periods.exhausted());
    }

    @Test
    void thePeriodLengthFollowsTheContainer() {
        assertEquals(1, E3DCPowerPlantHandler.daysOf(RscpTag.DB_HISTORY_DATA_DAY, LocalDate.of(2026, 2, 3)));
        assertEquals(7, E3DCPowerPlantHandler.daysOf(RscpTag.DB_HISTORY_DATA_WEEK, LocalDate.of(2026, 2, 2)));
        assertEquals(28, E3DCPowerPlantHandler.daysOf(RscpTag.DB_HISTORY_DATA_MONTH, LocalDate.of(2026, 2, 1)));
        assertEquals(366, E3DCPowerPlantHandler.daysOf(RscpTag.DB_HISTORY_DATA_YEAR, LocalDate.of(2024, 1, 1)));
    }

    @Test
    void thePeriodRequestCarriesStartIntervalAndSpan() {
        var request = E3DCBridgeHandler.periodHistoryRequest(RscpTag.DB_HISTORY_DATA_WEEK, LocalDate.of(2026, 2, 2), 7,
                ZoneId.of("UTC"));
        assertEquals(RscpTag.DB_REQ_HISTORY_DATA_WEEK, request.getTag());
        assertEquals(3, request.getChildren().size());
        // the interface declares these parameters as timestamps, which are moments of twelve bytes
        assertEquals(RscpType.TIMESTAMP, request.getChildren().get(0).getType());
        assertEquals(RscpType.TIMESTAMP, request.getChildren().get(1).getType());
        assertEquals(RscpType.TIMESTAMP, request.getChildren().get(2).getType());
    }

    @Test
    void thePeriodStartsAtLocalMidnightOfTheGivenZone() {
        // in winter Berlin is one hour ahead of UTC, so its midnight is 23:00 of the day before in UTC
        var request = E3DCBridgeHandler.periodHistoryRequest(RscpTag.DB_HISTORY_DATA_DAY, LocalDate.of(2026, 2, 2), 1,
                ZoneId.of("Europe/Berlin"));

        assertEquals(Instant.parse("2026-02-01T23:00:00Z"), request.getChildren().get(0).getValue());
    }

    @Test
    void thePeriodStartDependsOnTheZoneNotOnTheSystemDefault() {
        var utc = E3DCBridgeHandler.periodHistoryRequest(RscpTag.DB_HISTORY_DATA_DAY, LocalDate.of(2026, 7, 1), 1,
                ZoneId.of("UTC"));
        var sydney = E3DCBridgeHandler.periodHistoryRequest(RscpTag.DB_HISTORY_DATA_DAY, LocalDate.of(2026, 7, 1), 1,
                ZoneId.of("Australia/Sydney"));

        assertEquals(Instant.parse("2026-07-01T00:00:00Z"), utc.getChildren().get(0).getValue());
        assertEquals(Instant.parse("2026-06-30T14:00:00Z"), sydney.getChildren().get(0).getValue());
    }

    /**
     * The fetched values are published as a time series, which persistence stores as they are, so a value has to
     * carry the unit of its channel instead of a plain number.
     */
    @Test
    void aFetchedValueCarriesTheUnitOfItsChannel() throws Exception {
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        E3DCBridgeHandler bridgeHandler = mock(E3DCBridgeHandler.class);
        when(bridgeHandler.isConnected()).thenReturn(true);
        when(bridgeHandler.getZone()).thenReturn(ZoneId.of("UTC"));
        // the day container holds the sum container, which carries the energy of the day in Wh
        when(bridgeHandler.sendRequest(any())).thenReturn(
                RscpValue.container(RscpTag.DB_HISTORY_DATA_DAY, List.of(RscpValue.container(RscpTag.DB_SUM_CONTAINER,
                        List.of(RscpValue.of(RscpTag.DB_DC_POWER, RscpType.DOUBLE64, 5000.0))))));

        ThingUID bridgeUID = new ThingUID(E3DCBindingConstants.THING_TYPE_BRIDGE, "device");
        Bridge bridge = BridgeBuilder.create(E3DCBindingConstants.THING_TYPE_BRIDGE, bridgeUID).build();
        bridge.setHandler(bridgeHandler);
        when(callback.getBridge(bridgeUID)).thenReturn(bridge);

        ThingUID thingUID = new ThingUID(E3DCBindingConstants.THING_TYPE_POWER_PLANT, bridgeUID, "plant");
        ChannelUID daySolar = new ChannelUID(thingUID, E3DCBindingConstants.GROUP_HISTORY + "#day-solar");
        Thing thing = ThingBuilder.create(E3DCBindingConstants.THING_TYPE_POWER_PLANT, thingUID).withBridge(bridgeUID)
                .withChannel(ChannelBuilder.create(daySolar, "Number:Energy").build()).build();
        E3DCPowerPlantHandler handler = new E3DCPowerPlantHandler(thing, mock(E3DCCommandOptionsProvider.class));
        handler.setCallback(callback);

        assertEquals(1, handler.fetchPeriodHistory("day", 1));

        ArgumentCaptor<TimeSeries> series = ArgumentCaptor.forClass(TimeSeries.class);
        verify(callback).sendTimeSeries(eq(daySolar), series.capture());
        TimeSeries.Entry entry = series.getValue().getStates().findFirst().orElseThrow();
        State state = entry.state();
        assertTrue(state instanceof QuantityType<?>,
                "a fetched value must carry the unit of its channel, but was " + state);
        QuantityType<?> energy = (QuantityType<?>) state;
        assertEquals(Units.KILOWATT_HOUR, energy.getUnit());
        assertEquals(5.0, energy.doubleValue(), 0.001);
    }
}
