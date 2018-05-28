/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.Assert.*;
import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.CHANNEL_BRIGHTNESS;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link DimmerDevice}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class DimmerDeviceTest extends DeviceTestBase {

    private static final DecimalType BRIGHTNESS_VALUE = new DecimalType(50);

    private final DimmerDevice device = new DimmerDevice();

    public DimmerDeviceTest() throws IOException {
        super("hs220_get_sysinfo_response_on");
    }

    @Test
    public void testHandleCommandSwitch() throws IOException {
        assertInput("dimmer_set_switch_state_on");
        setSocketReturnAssert("dimmer_set_switch_state_response");
        assertTrue("Switch channel should be handled",
                device.handleCommand(CHANNEL_BRIGHTNESS, connection, OnOffType.ON, configuration));
    }

    @Test
    public void testHandleCommandBrightness() throws IOException {
        assertInput("dimmer_set_brightness", "dimmer_set_switch_state_on");
        setSocketReturnAssert("dimmer_set_brightness_response", "dimmer_set_switch_state_response");
        assertTrue("Brightness channel should be handled",
                device.handleCommand(CHANNEL_BRIGHTNESS, connection, new DecimalType(17), configuration));
    }

    @Test
    public void testUpdateChannelSwitch() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson("hs220_get_sysinfo_response_off"));

        assertSame("Dimmer device should be off", OnOffType.OFF,
                ((DecimalType) device.updateChannel(CHANNEL_BRIGHTNESS, deviceState)).as(OnOffType.class));
    }

    @Test
    public void testUpdateChannelBrightness() {
        assertEquals("Dimmer brightness should be set", BRIGHTNESS_VALUE,
                device.updateChannel(CHANNEL_BRIGHTNESS, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF, device.updateChannel("OTHER", deviceState));
    }
}
