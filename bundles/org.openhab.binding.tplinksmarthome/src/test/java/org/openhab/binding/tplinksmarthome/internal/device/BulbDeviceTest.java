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
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeBindingConstants.*;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.LB130;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link BulbDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class BulbDeviceTest extends DeviceTestBase<BulbDevice> {

    private static final String DEVICE_OFF = "bulb_get_sysinfo_response_off";

    public BulbDeviceTest() throws IOException {
        super(new BulbDevice(LB130.thingTypeUID(), COLOR_TEMPERATURE_2_MIN, COLOR_TEMPERATURE_2_MAX),
                "bulb_get_sysinfo_response_on");
    }

    @Override
    public void setUp() throws IOException {
        super.setUp();
        setSocketReturnAssert("bulb_transition_light_state_response");
    }

    @Test
    public void testHandleCommandBrightness() throws IOException {
        assertInput("bulb_transition_light_state_brightness");
        assertTrue("Brightness channel should be handled",
                device.handleCommand(CHANNEL_UID_BRIGHTNESS, new PercentType(33)));
    }

    @Test
    public void testHandleCommandBrightnessOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Brightness channel with OnOff state should be handled",
                device.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON));
    }

    @Test
    public void testHandleCommandColor() throws IOException {
        assertInput("bulb_transition_light_state_color");
        assertTrue("Color channel should be handled", device.handleCommand(CHANNEL_UID_COLOR, new HSBType("55,44,33")));
    }

    public void testHandleCommandColorBrightness() throws IOException {
        assertInput("bulb_transition_light_state_brightness");
        assertTrue("Color channel with Percentage state (=brightness) should be handled",
                device.handleCommand(CHANNEL_UID_COLOR, new PercentType(33)));
    }

    public void testHandleCommandColorOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Color channel with OnOff state should be handled",
                device.handleCommand(CHANNEL_UID_COLOR, OnOffType.ON));
    }

    @Test
    public void testHandleCommandColorTemperature() throws IOException {
        assertInput("bulb_transition_light_state_color_temp");
        assertTrue("Color temperature channel should be handled",
                device.handleCommand(CHANNEL_UID_COLOR_TEMPERATURE, new PercentType(40)));
    }

    @Test
    public void testHandleCommandColorTemperatureAbs() throws IOException {
        assertInput("bulb_transition_light_state_color_temp");
        assertTrue("Color temperature channel should be handled",
                device.handleCommand(CHANNEL_UID_COLOR_TEMPERATURE_ABS, new DecimalType(5100)));
    }

    @Test
    public void testHandleCommandColorTemperatureOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Color temperature channel with OnOff state should be handled",
                device.handleCommand(CHANNEL_UID_COLOR_TEMPERATURE, OnOffType.ON));
    }

    @Test
    public void testHandleCommandSwitch() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue("Switch channel should be handled", device.handleCommand(CHANNEL_UID_SWITCH, OnOffType.ON));
    }

    @Test
    public void testUpdateChannelBrightnessOn() {
        assertEquals("Brightness should be on", new PercentType(92),
                device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState));
    }

    @Test
    public void testUpdateChannelBrightnessOff() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(DEVICE_OFF));
        assertEquals("Brightness should be off", PercentType.ZERO,
                device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState));
    }

    @Test
    public void testUpdateChannelColorOn() {
        assertEquals("Color should be on", new HSBType("7,44,92"),
                device.updateChannel(CHANNEL_UID_COLOR, deviceState));
    }

    @Test
    public void testUpdateChannelColorOff() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(DEVICE_OFF));
        assertEquals("Color should be off", new HSBType("7,44,0"),
                device.updateChannel(CHANNEL_UID_COLOR, deviceState));
    }

    @Test
    public void testUpdateChannelSwitchOn() {
        assertSame("Switch should be on", OnOffType.ON, device.updateChannel(CHANNEL_UID_SWITCH, deviceState));
    }

    @Test
    public void testUpdateChannelSwitchOff() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(DEVICE_OFF));
        assertSame("Switch should be off", OnOffType.OFF, device.updateChannel(CHANNEL_UID_SWITCH, deviceState));
    }

    @Test
    public void testUpdateChannelColorTemperature() {
        assertEquals("Color temperature should be set", new PercentType(2),
                device.updateChannel(CHANNEL_UID_COLOR_TEMPERATURE, deviceState));
    }

    @Test
    public void testUpdateChannelColorTemperatureAbs() {
        assertEquals("Color temperature should be set", new DecimalType(2630),
                device.updateChannel(CHANNEL_UID_COLOR_TEMPERATURE_ABS, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF,
                device.updateChannel(CHANNEL_UID_OTHER, deviceState));
    }

    @Test
    public void testUpdateChannelPower() {
        assertEquals("Power values should be set", new DecimalType(10.8),
                device.updateChannel(CHANNEL_UID_ENERGY_POWER, deviceState));
    }

}
