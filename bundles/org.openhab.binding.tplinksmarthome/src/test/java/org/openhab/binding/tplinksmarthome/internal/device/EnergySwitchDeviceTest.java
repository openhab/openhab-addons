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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Test class for {@link EnergySwitchDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class EnergySwitchDeviceTest {

    private static final List<Object[]> TESTS = Arrays
            .asList(new Object[][] { { "plug_get_realtime_response", }, { "plug_get_realtime_response_v2", } });

    private final EnergySwitchDevice device = new EnergySwitchDevice();

    public static List<Object[]> data() {
        return TESTS;
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testUpdateChannelEnergyCurrent(String name) throws IOException {
        DeviceState deviceState = new DeviceState(ModelTestUtil.readJson(name));
        assertEquals(new QuantityType<>(1 + " A"), device.updateChannel(CHANNEL_UID_ENERGY_CURRENT, deviceState),
                "Energy current should have valid state value");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testUpdateChannelEnergyTotal(String name) throws IOException {
        DeviceState deviceState = new DeviceState(ModelTestUtil.readJson(name));
        assertEquals(new QuantityType<>(10 + " kWh"), device.updateChannel(CHANNEL_UID_ENERGY_TOTAL, deviceState),
                "Energy total should have valid state value");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testUpdateChannelEnergyVoltage(String name) throws IOException {
        DeviceState deviceState = new DeviceState(ModelTestUtil.readJson(name));
        State state = device.updateChannel(CHANNEL_UID_ENERGY_VOLTAGE, deviceState);
        assertEquals(230, ((QuantityType<?>) state).intValue(), "Energy voltage should have valid state value");
        assertEquals("230 V", state.format("%.0f %unit%"), "Channel patten to format voltage correctly");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testUpdateChanneEnergyPower(String name) throws IOException {
        DeviceState deviceState = new DeviceState(ModelTestUtil.readJson(name));
        assertEquals(new QuantityType<>(20 + " W"), device.updateChannel(CHANNEL_UID_ENERGY_POWER, deviceState),
                "Energy power should have valid state value");
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testUpdateChannelOther(String name) throws IOException {
        DeviceState deviceState = new DeviceState(ModelTestUtil.readJson(name));
        assertSame(UnDefType.UNDEF, device.updateChannel(CHANNEL_UID_OTHER, deviceState),
                "Unknown channel should return UNDEF");
    }
}
