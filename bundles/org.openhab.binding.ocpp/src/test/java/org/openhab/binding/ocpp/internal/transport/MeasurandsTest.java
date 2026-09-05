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
package org.openhab.binding.ocpp.internal.transport;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests the measurand drop-last elimination used when negotiating MeterValues with a charger.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class MeasurandsTest {

    @Test
    void dropsTheLastEntry() {
        assertEquals("Energy.Active.Import.Register,Power.Active.Import,Current.Import",
                Measurands.dropLast("Energy.Active.Import.Register,Power.Active.Import,Current.Import,Voltage"));
    }

    @Test
    void trimsWhitespaceAroundEntries() {
        assertEquals("Voltage,Temperature", Measurands.dropLast("Voltage, Temperature, Current.Import"));
    }

    @Test
    void emptyOnceASingleEntryRemains() {
        assertEquals("", Measurands.dropLast("Voltage"));
    }

    @Test
    void emptyForNullOrBlank() {
        assertEquals("", Measurands.dropLast(null));
        assertEquals("", Measurands.dropLast(""));
        assertEquals("", Measurands.dropLast("   "));
    }
}
