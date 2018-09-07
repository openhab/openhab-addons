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
import static org.openhab.binding.tplinksmarthome.TPLinkSmartHomeBindingConstants.*;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Test class for {@link RangeExtenderDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
public class RangeExtenderDeviceTest extends DeviceTestBase {
    private final RangeExtenderDevice device = new RangeExtenderDevice();

    public RangeExtenderDeviceTest() throws IOException {
        super("rangeextender_get_sysinfo_response");
    }

    @Test
    public void testHandleCommandSwitch() throws IOException {
        assertFalse("Switch channel not yet supported so should not be handled",
                device.handleCommand(CHANNEL_SWITCH, connection, OnOffType.ON, configuration));
    }

    @Ignore("Ignore handle led command because led support in binding not (yet) implemented.")
    @Test
    public void testHandleCommandLed() throws IOException {
        // assertInput("rangeextender_set_led_on");
        // setSocketReturnAssert("rangeextender_set_led_on");
        assertFalse("Led channel should be handled",
                device.handleCommand(CHANNEL_LED, connection, OnOffType.ON, configuration));
    }

    @Test
    public void testUpdateChannelSwitch() {
        assertSame("Switch should be on", OnOffType.ON, device.updateChannel(CHANNEL_SWITCH, deviceState));
    }

    @Test
    public void testUpdateChannelLed() {
        assertSame("Led should be on", OnOffType.ON, device.updateChannel(CHANNEL_LED, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF, device.updateChannel("OTHER", deviceState));
    }

}
