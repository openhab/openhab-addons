/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.services.batterylevel;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Fault;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Faults;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;

/**
 * Unit tests for {@link BatteryLevel}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class BatteryLevelTest {

    @Test
    void testGet() {
        assertSame(BatteryLevel.OK, BatteryLevel.get("OK"));
        assertSame(BatteryLevel.OK, BatteryLevel.get("ok"));
        assertSame(BatteryLevel.LOW_BATTERY, BatteryLevel.get("LOW_BATTERY"));
        assertSame(BatteryLevel.LOW_BATTERY, BatteryLevel.get("low_battery"));
        assertSame(BatteryLevel.CRITICAL_LOW, BatteryLevel.get("CRITICAL_LOW"));
        assertSame(BatteryLevel.CRITICAL_LOW, BatteryLevel.get("critical_low"));
        assertSame(BatteryLevel.CRITICALLY_LOW_BATTERY, BatteryLevel.get("CRITICALLY_LOW_BATTERY"));
        assertSame(BatteryLevel.CRITICALLY_LOW_BATTERY, BatteryLevel.get("critically_low_battery"));
        assertSame(BatteryLevel.NOT_AVAILABLE, BatteryLevel.get("NOT_AVAILABLE"));
        assertSame(BatteryLevel.NOT_AVAILABLE, BatteryLevel.get("not_available"));
        assertNull(BatteryLevel.get("foo"));
    }

    @Test
    void testFromDeviceServiceData() {
        DeviceServiceData deviceServiceData = new DeviceServiceData();
        assertSame(BatteryLevel.OK, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        Faults faults = new Faults();
        deviceServiceData.faults = faults;
        assertSame(BatteryLevel.OK, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        ArrayList<Fault> entries = new ArrayList<>();
        faults.entries = entries;
        assertSame(BatteryLevel.OK, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        Fault fault = new Fault();
        entries.add(fault);
        assertSame(BatteryLevel.OK, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        fault.category = "WARNING";
        fault.type = "LOW_BATTERY";
        assertSame(BatteryLevel.LOW_BATTERY, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        fault.type = "CRITICAL_LOW";
        assertSame(BatteryLevel.CRITICAL_LOW, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        fault.type = "CRITICALLY_LOW_BATTERY";
        assertSame(BatteryLevel.CRITICALLY_LOW_BATTERY, BatteryLevel.fromDeviceServiceData(deviceServiceData));

        fault.type = "FOO";
        assertSame(BatteryLevel.OK, BatteryLevel.fromDeviceServiceData(deviceServiceData));
    }

    @Test
    void testToState() {
        assertEquals(new DecimalType(100), BatteryLevel.OK.toState());
        assertEquals(new DecimalType(10), BatteryLevel.LOW_BATTERY.toState());
        assertEquals(new DecimalType(1), BatteryLevel.CRITICAL_LOW.toState());
        assertEquals(new DecimalType(1), BatteryLevel.CRITICALLY_LOW_BATTERY.toState());
        assertEquals(UnDefType.UNDEF, BatteryLevel.NOT_AVAILABLE.toState());
    }

    @Test
    void testToLowBatteryState() {
        assertEquals(OnOffType.OFF, BatteryLevel.OK.toLowBatteryState());
        assertEquals(OnOffType.ON, BatteryLevel.LOW_BATTERY.toLowBatteryState());
        assertEquals(OnOffType.ON, BatteryLevel.CRITICAL_LOW.toLowBatteryState());
        assertEquals(OnOffType.ON, BatteryLevel.CRITICALLY_LOW_BATTERY.toLowBatteryState());
        assertEquals(OnOffType.OFF, BatteryLevel.NOT_AVAILABLE.toLowBatteryState());
    }
}
