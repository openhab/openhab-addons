/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.fmiweather.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.fmiweather.internal.client.Data;

/**
 * Base class for response parsing tests
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault

public class AbstractWeatherHandlerTest {

    private static BigDecimal bd(Number x) {
        return new BigDecimal(x.doubleValue());
    }

    protected static long floorToEvenMinutes(long epochSeconds, int roundMinutes) {
        Object res = AbstractWeatherHandler.floorToEvenMinutes(epochSeconds, roundMinutes);
        assertNotNull(res);
        return (long) res;
    }

    protected static long ceilToEvenMinutes(long epochSeconds, int roundMinutes) {
        Object res = AbstractWeatherHandler.ceilToEvenMinutes(epochSeconds, roundMinutes);
        assertNotNull(res);
        return (long) res;
    }

    public static List<Object[]> parametersForFloorToEvenMinutes() {
        return Arrays.asList(new Object[][] { //
                { 1626605128L /* 2021-07-18 10:45:28 */, 1, 1626605100 /* 10:45 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 5, 1626605100 /* 10:45 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 10, 1626604800 /* 10:40 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 30, 1626604200 /* 10:30 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 60, 1626602400 /* 10:00 */ }, //
        });
    }

    protected static int lastValidIndex(Data data) {
        Object res = AbstractWeatherHandler.lastValidIndex(data);
        assertNotNull(res);
        return (int) res;
    }

    @ParameterizedTest
    @MethodSource("parametersForFloorToEvenMinutes")
    public void testFloorToEvenMinutes(long epochSeconds, int roundMinutes, long expected) {
        assertEquals(expected, floorToEvenMinutes(epochSeconds, roundMinutes));
    }

    public static List<Object[]> parametersForCeilToEvenMinutes() {
        return Arrays.asList(new Object[][] { //
                { 1626605128L /* 2021-07-18 10:45:28 */, 1, 1626605160 /* 10:46 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 5, 1626605400 /* 10:50 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 10, 1626605400 /* 10:50 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 30, 1626606000 /* 11:00 */ }, //
                { 1626605128L /* 2021-07-18 10:45:28 */, 60, 1626606000 /* 11:00 */ }, //
        });
    }

    @ParameterizedTest
    @MethodSource("parametersForCeilToEvenMinutes")
    public void testCeilToEvenMinutes(long epochSeconds, int roundMinutes, long expected) {
        assertEquals(expected, ceilToEvenMinutes(epochSeconds, roundMinutes));
    }

    public static List<Object[]> parametersForLastValidIndex() {
        return Arrays.asList(new Object[][] { //
                { "no nulls", 1, new BigDecimal[] { bd(1), bd(2) } }, //
                { "one null in beginning", 1, new BigDecimal[] { null, bd(2) } }, //
                { "two nulls", 2, new BigDecimal[] { null, null, bd(2) } }, //
                { "three nulls", 3, new BigDecimal[] { null, null, null, bd(2) } }, //
                { "null at end", 1, new BigDecimal[] { bd(1), bd(2), null } }, //
                { "null at end #2", 2, new BigDecimal[] { bd(1), bd(2), bd(2), null } }, //
                { "null in middle", 3, new BigDecimal[] { bd(1), bd(2), null, bd(3) } }, //
                { "null in beginning and middle", 3, new BigDecimal[] { null, bd(2), null, bd(3) } }, //
                { "all null #1", -1, new BigDecimal[] { null, null, } }, //
                { "all null #2", -1, new BigDecimal[] { null, null, null, } }, //
                { "all null #3", -1, new BigDecimal[] { null, null, null, null } }, //
        });
    }

    @ParameterizedTest
    @MethodSource("parametersForLastValidIndex")
    public void testLastValidIndex(String msg, int expected, @Nullable BigDecimal... values) {
        long[] dummyTimestamps = new long[values.length];
        assertEquals(expected, lastValidIndex(new Data(dummyTimestamps, values)), msg);
    }
}
