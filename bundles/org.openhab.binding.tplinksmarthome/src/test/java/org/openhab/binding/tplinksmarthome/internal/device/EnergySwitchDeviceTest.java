/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.Assert.*;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link EnergySwitchDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
@NonNullByDefault
public class EnergySwitchDeviceTest {

    private static final List<Object[]> TESTS = Arrays
            .asList(new Object[][] { { "plug_get_realtime_response", }, { "plug_get_realtime_response_v2", } });

    private final EnergySwitchDevice device = new EnergySwitchDevice();
    private final DeviceState deviceState;

    public EnergySwitchDeviceTest(String name) throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(name));
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        return TESTS;
    }

    @Test
    public void testUpdateChannelEnergyCurrent() {
        assertEquals("Energy current should have valid state value", new QuantityType<>(1 + " A"),
                device.updateChannel(CHANNEL_UID_ENERGY_CURRENT, deviceState));
    }

    @Test
    public void testUpdateChannelEnergyTotal() {
        assertEquals("Energy total should have valid state value", new QuantityType<>(10 + " kWh"),
                device.updateChannel(CHANNEL_UID_ENERGY_TOTAL, deviceState));
    }

    @Test
    public void testUpdateChannelEnergyVoltage() {
        State state = device.updateChannel(CHANNEL_UID_ENERGY_VOLTAGE, deviceState);
        assertEquals("Energy voltage should have valid state value", 230, ((QuantityType<?>) state).intValue());
        assertEquals("Channel patten to format voltage correctly", "230 V", state.format("%.0f %unit%"));
    }

    @Test
    public void testUpdateChanneEnergyPower() {
        assertEquals("Energy power should have valid state value", new QuantityType<>(20 + " W"),
                device.updateChannel(CHANNEL_UID_ENERGY_POWER, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF,
                device.updateChannel(CHANNEL_UID_OTHER, deviceState));
    }

}
