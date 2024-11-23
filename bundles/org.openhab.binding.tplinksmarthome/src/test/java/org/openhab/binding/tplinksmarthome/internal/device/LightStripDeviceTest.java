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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_BRIGHTNESS;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_COLOR;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_COLOR_TEMPERATURE;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_COLOR_TEMPERATURE_ABS;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_ENERGY_POWER;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_OTHER;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.CHANNEL_UID_SWITCH;
import static org.openhab.binding.tplinksmarthome.internal.TPLinkSmartHomeThingType.KL430;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * Test class for {@link BulbDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class LightStripDeviceTest extends DeviceTestBase<LightStripDevice> {

    private static final String DEVICE_OFF = "bulb_get_sysinfo_response_off";

    public LightStripDeviceTest() throws IOException {
        super(new LightStripDevice(KL430), "bulb_get_sysinfo_response_on");
    }

    @BeforeEach
    @Override
    public void setUp() throws IOException {
        super.setUp();
    }

    @Test
    public void testHandleCommandBrightness() throws IOException {
        assertInput("kl430_set_brightness");
        setSocketReturnAssert("kl430_set_brightness_response");
        assertTrue(device.handleCommand(CHANNEL_UID_BRIGHTNESS, new PercentType(73)),
                "Brightness channel should be handled");
    }

    @Test
    public void testHandleCommandBrightnessOnOff() throws IOException {
        assertInput("kl430_set_on");
        setSocketReturnAssert("kl430_set_brightness_response");
        assertTrue(device.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON),
                "Brightness channel with OnOff state should be handled");
    }

    @Test
    public void testHandleCommandColor() throws IOException {
        assertInput("kl430_set_color");
        setSocketReturnAssert("kl430_set_color_response");
        assertTrue(device.handleCommand(CHANNEL_UID_COLOR, new HSBType("115,75,73")),
                "Color channel should be handled");
    }

    public void testHandleCommandColorBrightness() throws IOException {
        assertInput("kl430_set_brightness");
        setSocketReturnAssert("kl430_set_brightness_response");
        assertTrue(device.handleCommand(CHANNEL_UID_COLOR, new PercentType(33)),
                "Color channel with Percentage state (=brightness) should be handled");
    }

    public void testHandleCommandColorOnOff() throws IOException {
        assertInput("bulb_transition_light_state_on");
        assertTrue(device.handleCommand(CHANNEL_UID_COLOR, OnOffType.ON),
                "Color channel with OnOff state should be handled");
    }

    @Test
    public void testHandleCommandColorTemperature() throws IOException {
        assertInput("kl430_set_colortemperature");
        setSocketReturnAssert("kl430_set_colortemperature_response");
        assertTrue(device.handleCommand(CHANNEL_UID_COLOR_TEMPERATURE, new PercentType(40)),
                "Color temperature channel should be handled");
    }

    @Test
    public void testHandleCommandColorTemperatureAbs() throws IOException {
        assertInput("kl430_set_colortemperature");
        setSocketReturnAssert("kl430_set_colortemperature_response");
        assertTrue(device.handleCommand(CHANNEL_UID_COLOR_TEMPERATURE_ABS, new DecimalType(5100)),
                "Color temperature channel should be handled");
    }

    @Test
    public void testHandleCommandColorTemperatureOnOff() throws IOException {
        assertInput("kl430_set_on");
        setSocketReturnAssert("kl430_set_colortemperature_response");
        assertTrue(device.handleCommand(CHANNEL_UID_COLOR_TEMPERATURE, OnOffType.ON),
                "Color temperature channel with OnOff state should be handled");
    }

    // ---- Update ----

    @Test
    public void testUpdateChannelBrightnessOn() {
        assertEquals(new PercentType(92), device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState),
                "Brightness should be on");
    }

    @Test
    public void testUpdateChannelBrightnessOff() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(DEVICE_OFF));
        assertEquals(PercentType.ZERO, device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState),
                "Brightness should be off");
    }

    @Test
    public void testUpdateChannelColorOn() {
        assertEquals(new HSBType("7,44,92"), device.updateChannel(CHANNEL_UID_COLOR, deviceState),
                "Color should be on");
    }

    @Test
    public void testUpdateChannelColorOff() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(DEVICE_OFF));
        assertEquals(new HSBType("7,44,0"), device.updateChannel(CHANNEL_UID_COLOR, deviceState),
                "Color should be off");
    }

    @Test
    public void testUpdateChannelSwitchOn() {
        assertSame(OnOffType.ON, device.updateChannel(CHANNEL_UID_SWITCH, deviceState), "Switch should be on");
    }

    @Test
    public void testUpdateChannelSwitchOff() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson(DEVICE_OFF));
        assertSame(OnOffType.OFF, device.updateChannel(CHANNEL_UID_SWITCH, deviceState), "Switch should be off");
    }

    @Test
    public void testUpdateChannelColorTemperature() throws IOException {
        assertInput("kl430_set_colortemperature");
        assertEquals(new PercentType(2), device.updateChannel(CHANNEL_UID_COLOR_TEMPERATURE, deviceState),
                "Color temperature should be set");
    }

    @Test
    public void testUpdateChannelColorTemperatureAbs() throws IOException {
        assertInput("kl430_set_colortemperature");
        assertEquals(new DecimalType(2630), device.updateChannel(CHANNEL_UID_COLOR_TEMPERATURE_ABS, deviceState),
                "Color temperature should be set");
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame(UnDefType.UNDEF, device.updateChannel(CHANNEL_UID_OTHER, deviceState),
                "Unknown channel should return UNDEF");
    }

    @Test
    public void testUpdateChannelPower() {
        assertEquals(new QuantityType<>(10.8, Units.WATT), device.updateChannel(CHANNEL_UID_ENERGY_POWER, deviceState),
                "Power values should be set");
    }
}
