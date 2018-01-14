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
import java.util.Arrays;
import java.util.List;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test class for {@link BulbDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@RunWith(value = Parameterized.class)
public class BulbDeviceTest extends DeviceTestBase {

    private final BulbDevice device = new BulbDevice(new ThingTypeUID(BINDING_ID, "lb130"), COLOR_TEMPERATURE_LB130_MIN,
            COLOR_TEMPERATURE_LB130_MAX);

    private static final List<Object[]> TESTS = Arrays
            .asList(new Object[][] { { "bulb_get_sysinfo_response" }, { "bulb_get_sysinfo_response_lb130" } });

    public BulbDeviceTest(String responseFilename) throws IOException {
        super(responseFilename);
    }

    @Override
    public void setUp() throws IOException {
        super.setUp();
        setSocketReturnAssert("bulb_transition_light_state_response");
    }

    @Parameters(name = "{0}")
    public static List<Object[]> data() {
        return TESTS;
    }

    @Test
    public void testHandleCommandBrightness() throws IOException {
        assertInput("bulb_transition_light_state_brightness");
        assertTrue("Brightness channel should be handled",
                device.handleCommand(CHANNEL_BRIGHTNESS, connection, new PercentType(33), configuration));
    }

    @Test
    public void testHandleCommandBrightnessOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Brightness channel with OnOff state should be handled",
                device.handleCommand(CHANNEL_BRIGHTNESS, connection, OnOffType.ON, configuration));
    }

    @Test
    public void testHandleCommandColor() throws IOException {
        assertInput("bulb_transition_light_state_color");
        assertTrue("Color channel should be handled",
                device.handleCommand(CHANNEL_COLOR, connection, new HSBType("55,44,33"), configuration));
    }

    public void testHandleCommandColorBrightness() throws IOException {
        assertInput("bulb_transition_light_state_brightness");
        assertTrue("Color channel with Percentage state (=brightness) should be handled",
                device.handleCommand(CHANNEL_COLOR, connection, new PercentType(33), configuration));
    }

    public void testHandleCommandColorOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Color channel with OnOff state should be handled",
                device.handleCommand(CHANNEL_COLOR, connection, OnOffType.ON, configuration));
    }

    @Test
    public void testHandleCommandColorTemperature() throws IOException {
        assertInput("bulb_transition_light_state_color_temp");
        assertTrue("Color temperature channel should be handled",
                device.handleCommand(CHANNEL_COLOR_TEMPERATURE, connection, new PercentType(40), configuration));
    }

    @Test
    public void testHandleCommandColorTemperatureOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Color temperature channel with OnOff state should be handled",
                device.handleCommand(CHANNEL_COLOR_TEMPERATURE, connection, OnOffType.ON, configuration));
    }

    @Test
    public void testHandleCommandSwitch() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Switch channel should be handled",
                device.handleCommand(CHANNEL_SWITCH, connection, OnOffType.ON, configuration));
    }

    @Test
    public void testUpdateChannelBrightness() {
        assertEquals("Switch should be on", new PercentType(92), device.updateChannel(CHANNEL_BRIGHTNESS, deviceState));
    }

    @Test
    public void testUpdateChannelColor() {
        assertEquals("Switch should be on", new HSBType("7,44,92"), device.updateChannel(CHANNEL_COLOR, deviceState));
    }

    @Test
    public void testUpdateChannelSwitch() {
        assertSame("Switch should be on", OnOffType.ON, device.updateChannel(CHANNEL_SWITCH, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF, device.updateChannel("OTHER", deviceState));
    }
}
