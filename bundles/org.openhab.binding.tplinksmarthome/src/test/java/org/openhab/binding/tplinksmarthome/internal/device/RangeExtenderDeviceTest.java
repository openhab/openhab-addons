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
import org.eclipse.smarthome.core.types.UnDefType;
import org.junit.Test;

/**
 * Test class for {@link RangeExtenderDevice} class.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public class RangeExtenderDeviceTest extends DeviceTestBase<RangeExtenderDevice> {

    public RangeExtenderDeviceTest() throws IOException {
        super(new RangeExtenderDevice(), "rangeextender_get_sysinfo_response");
    }

    @Test
    public void testHandleCommandSwitch() throws IOException {
        assertFalse("Switch channel not yet supported so should not be handled",
                device.handleCommand(CHANNEL_UID_SWITCH, OnOffType.ON));
    }

    @Test
    public void testUpdateChannelSwitch() {
        assertSame("Switch should be on", OnOffType.ON, device.updateChannel(CHANNEL_UID_SWITCH, deviceState));
    }

    @Test
    public void testUpdateChannelLed() {
        assertSame("Led should be on", OnOffType.ON, device.updateChannel(CHANNEL_UID_LED, deviceState));
    }

    @Test
    public void testUpdateChannelOther() {
        assertSame("Unknown channel should return UNDEF", UnDefType.UNDEF,
                device.updateChannel(CHANNEL_UID_OTHER, deviceState));
    }
}
