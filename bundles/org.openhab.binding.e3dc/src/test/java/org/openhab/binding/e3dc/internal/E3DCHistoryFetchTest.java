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
package org.openhab.binding.e3dc.internal;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
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
    void theFetchTriesTwoYearsInARowBeforeItStops() {
        assertEquals(2, E3DCPowerPlantHandler.MAX_EMPTY_YEARS);
        assertEquals(2000, E3DCPowerPlantHandler.FIRST_HISTORY_YEAR);
    }
}
