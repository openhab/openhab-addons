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
package org.openhab.binding.shelly.internal.api2;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

@NonNullByDefault
@SuppressWarnings("null")
public class Shelly2ByMinuteConversionTest {

    @Test
    void byMinuteMilliwattHoursConvertedToWattHours() {
        // Community-measured Plus Plug S values: 428.5 W load → by_minute[0] ≈ 7148.092 mWh = 7.148092 Wh
        Double @Nullable [] wh = Shelly2ApiClient.byMinuteToWh(new Double[] { 7148.092, 7160.587, 6429.836 });

        assertNotNull(wh);
        assertEquals(3, wh.length);
        assertEquals(7.148092, wh[0], 0.000001);
        assertEquals(7.160587, wh[1], 0.000001);
        assertEquals(6.429836, wh[2], 0.000001);
    }

    @Test
    void byMinuteNullInputReturnsNull() {
        assertNull(Shelly2ApiClient.byMinuteToWh(null));
    }

    @Test
    void byMinuteEmptyArrayReturnsNull() {
        assertNull(Shelly2ApiClient.byMinuteToWh(new Double[0]));
    }

    @Test
    void byMinuteNullSlot0ReturnsNull() {
        // Device clock not synced: firmware omits usable minute data
        assertNull(Shelly2ApiClient.byMinuteToWh(new Double[] { null, 100.0, 200.0 }));
    }

    @Test
    void byMinuteNullTrailingSlotPreserved() {
        Double @Nullable [] wh = Shelly2ApiClient.byMinuteToWh(new Double[] { 1000.0, null, 3000.0 });

        assertNotNull(wh);
        assertEquals(1.0, wh[0], 0.000001);
        assertNull(wh[1]);
        assertEquals(3.0, wh[2], 0.000001);
    }

    @Test
    void byMinuteSingleSlotConverted() {
        Double @Nullable [] wh = Shelly2ApiClient.byMinuteToWh(new Double[] { 500.0 });

        assertNotNull(wh);
        assertEquals(1, wh.length);
        assertEquals(0.5, wh[0], 0.000001);
    }

    @Test
    void byMinuteZeroLoadStaysZero() {
        Double @Nullable [] wh = Shelly2ApiClient.byMinuteToWh(new Double[] { 0.0, 0.0, 0.0 });

        assertNotNull(wh);
        assertEquals(0.0, wh[0], 0.000001);
        assertEquals(0.0, wh[1], 0.000001);
        assertEquals(0.0, wh[2], 0.000001);
    }
}
