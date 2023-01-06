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
package org.openhab.binding.tplinksmarthome.internal.device;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tplinksmarthome.internal.ChannelUIDConstants.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.tplinksmarthome.internal.model.ModelTestUtil;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.types.UnDefType;

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
        assertTrue(device.handleCommand(CHANNEL_UID_BRIGHTNESS, OnOffType.ON),
                "Brightness channel as OnOffType type should be handled");
    }

    @Test
    public void testHandleCommandBrightnessZero() throws IOException {
        assertInput("dimmer_set_switch_state_off");
        setSocketReturnAssert("dimmer_set_switch_state_response");
        assertTrue(device.handleCommand(CHANNEL_UID_BRIGHTNESS, PercentType.ZERO),
                "Brightness channel with percentage 0 should be handled");
    }

    @Test
    public void testHandleCommandBrightness() throws IOException {
        assertInput("dimmer_set_brightness", "dimmer_set_switch_state_on");
        setSocketReturnAssert("dimmer_set_brightness_response", "dimmer_set_switch_state_on");
        assertTrue(device.handleCommand(CHANNEL_UID_BRIGHTNESS, new PercentType(17)),
                "Brightness channel should be handled");
    }

    @Test
    public void testUpdateChannelSwitch() throws IOException {
        deviceState = new DeviceState(ModelTestUtil.readJson("hs220_get_sysinfo_response_off"));

        assertSame(OnOffType.OFF,
                ((PercentType) device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState)).as(OnOffType.class),
                "Dimmer device should be off");
    }

    @Test
    public void testUpdateChannelBrightness() {
        assertEquals(BRIGHTNESS_VALUE, device.updateChannel(CHANNEL_UID_BRIGHTNESS, deviceState),
                "Dimmer brightness should be set");
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame(UnDefType.UNDEF, device.updateChannel(CHANNEL_UID_OTHER, deviceState),
                "Unknown channel should return UNDEF");
    }
}
