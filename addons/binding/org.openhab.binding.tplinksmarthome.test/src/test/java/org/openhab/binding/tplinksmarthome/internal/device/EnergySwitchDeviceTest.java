/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.Assert.*;
import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Before;
import org.junit.Test;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link EnergySwitchDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class EnergySwitchDeviceTest {

    private EnergySwitchDevice device = new EnergySwitchDevice();
    private DeviceState deviceState;

    @Before
    public void setUp() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson("plug_get_realtime_response"));
    }

    @Test
    public void testUpdateChannelEnergyCurrent() {
        assertEquals("Energy current should have valid state value", 1,
                ((DecimalType) device.updateChannel(CHANNEL_ENERGY_CURRENT, deviceState)).intValue());
    }

    @Test
    public void testUpdateChannelEnergyTotal() {
        assertEquals("Energy total should have valid state value", 10,
                ((DecimalType) device.updateChannel(CHANNEL_ENERGY_TOTAL, deviceState)).intValue());
    }

    @Test
    public void testUpdateChannelEnergyVoltage() {
        State state = device.updateChannel(CHANNEL_ENERGY_VOLTAGE, deviceState);
        assertEquals("Energy voltage should have valid state value", 230, ((DecimalType) state).intValue());
        assertEquals("Channel patten to display as int", "230 V", state.format("%.0f V"));
    }

    @Test
    public void testUpdateChanneEnergyPowerl() {
        assertEquals("Energy power should have valid state value", 20,
                ((DecimalType) device.updateChannel(CHANNEL_ENERGY_POWER, deviceState)).intValue());
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF, device.updateChannel("OTHER", deviceState));
    }

}
