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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bydbatterybox.internal.dto.BatteryType;
import org.openhab.binding.bydbatterybox.internal.dto.SystemInfo;

/**
 * Tests {@link ChannelLayout}.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
class ChannelLayoutTest {

    private static SystemInfo info(BatteryType type, int towers, int modules) {
        return new SystemInfo("", "", "", 'A', "", 'A', 0, towers, modules, type, 0, 0, 0, null);
    }

    @Test
    void hvsSingleTowerThreeModules() {
        SystemInfo hvs = info(BatteryType.HVS, 1, 3);

        assertEquals(List.of("tower-1", "tower-1-module-1", "tower-1-module-2", "tower-1-module-3"),
                ChannelLayout.groups(hvs));
        assertTrue(ChannelLayout.exists("tower-1", "cell-voltage-spread", hvs));
        assertTrue(ChannelLayout.exists("tower-1-module-3", "cell-voltage-32", hvs));
        assertTrue(ChannelLayout.exists("tower-1-module-3", "cell-temperature-12", hvs));
        assertTrue(ChannelLayout.exists("tower-1-module-3", "voltage", hvs));
        assertFalse(ChannelLayout.exists("tower-1-module-4", "cell-voltage-01", hvs));
        assertFalse(ChannelLayout.exists("tower-2", "voltage", hvs));
        assertFalse(ChannelLayout.exists("tower-2-module-1", "voltage", hvs));
    }

    @Test
    void hvmModulesHaveSixteenCellsAndEightSensors() {
        SystemInfo hvm = info(BatteryType.HVM, 2, 8);

        assertEquals(18, ChannelLayout.groups(hvm).size());
        assertTrue(ChannelLayout.exists("tower-2-module-8", "cell-voltage-16", hvm));
        assertFalse(ChannelLayout.exists("tower-2-module-8", "cell-voltage-17", hvm));
        assertTrue(ChannelLayout.exists("tower-2-module-8", "cell-temperature-08", hvm));
        assertFalse(ChannelLayout.exists("tower-2-module-8", "cell-temperature-09", hvm));
        assertTrue(ChannelLayout.exists("tower-2-module-8", "average-cell-voltage", hvm));
        assertFalse(ChannelLayout.exists("tower-3", "voltage", hvm));
    }

    @Test
    void unsupportedTypeHasNoTowerChannels() {
        SystemInfo unsupported = info(BatteryType.UNSUPPORTED, 1, 4);

        assertTrue(ChannelLayout.groups(unsupported).isEmpty());
        assertFalse(ChannelLayout.exists("tower-1", "voltage", unsupported));
        assertFalse(ChannelLayout.exists("tower-1-module-1", "cell-voltage-01", unsupported));
    }

    @Test
    void otherGroupsAlwaysExist() {
        SystemInfo unsupported = info(BatteryType.UNSUPPORTED, 1, 0);

        assertTrue(ChannelLayout.exists("status", "voltage", unsupported));
        assertTrue(ChannelLayout.exists("events", "event", unsupported));
        assertTrue(ChannelLayout.exists("diagnostics", "bmu-clock-offset", unsupported));
    }
}
