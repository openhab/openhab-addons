/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;

/**
 * Test class for {@link DimmerDevice}.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class DimmerDeviceTest extends DeviceTestBase<DimmerDevice> {

    private static final PercentType BRIGHTNESS_VALUE = new PercentType(50);

    public DimmerDeviceTest() throws IOException {
        super(new DimmerDevice(), "hs220_get_sysinfo_response_on");
    }

    @Test
    public void testHandleCommandBrightnessOnOff() throws IOException {
        assertInput("dimmer_set_switch_state_on");
        setSocketReturnAssert("dimmer_set_switch_state_on");
        assertTrue("Brightness channel as OnOffType type should be handled",
                device.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON));
    }

    @Test
    public void testHandleCommandBrightnessZero() throws IOException {
        assertInput("dimmer_set_switch_state_off");
        setSocketReturnAssert("dimmer_set_switch_state_response");
        assertTrue("Brightness channel with percentage 0 should be handled",
                device.handleCommand(CHANNEL_UID_BRIGHTNESS, PercentType.ZERO));
    }

    @Test
    public void testHandleCommandBrightness() throws IOException {
        assertInput("dimmer_set_brightness", "dimmer_set_switch_state_on");
        setSocketReturnAssert("dimmer_set_brightness_response", "dimmer_set_switch_state_on");
        assertTrue("Brightness channel should be handled",
                device.handleCommand(CHANNEL_UID_BRIGHTNESS, new PercentType(17)));
    }

    @Test
    public void testUpdateChannelSwitch() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson("hs220_get_sysinfo_response_off"));

        assertSame("Dimmer device should be off", OnOffType.OFF,
                ((PercentType) device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState)).as(OnOffType.class));
    }

    @Test
    public void testUpdateChannelBrightness() {
        assertEquals("Dimmer brightness should be set", BRIGHTNESS_VALUE,
                device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF,
                device.updateChannel(CHANNEL_UID_OTHER, deviceState));
    }
}
