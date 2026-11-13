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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.e3dc.internal.rscp.RscpTag;
import org.openhab.binding.e3dc.internal.rscp.RscpType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;

/**
 * Tests the rule that decides whether the device holds history for a year.
 *
 * <p>
 * The history fetch goes back year by year and stops at the years before the installation, so a year the device
 * answers with zeros must not count as a year with data.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
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
}
