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
package org.openhab.binding.boschshc.internal.devices.windowcontact;

import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit Tests for {@link WindowContactHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class WindowContactHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<WindowContactHandler> {

    @Override
    protected WindowContactHandler createFixture() {
        return new WindowContactHandler(getThing());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:HomeMaticIP:3014D711A000009D545DEB39D";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_WINDOW_CONTACT;
    }

    @Test
    public void testUpdateChannelsShutterContactService() {
        JsonElement jsonObject = JsonParser
                .parseString("{\n" + "   \"@type\": \"shutterContactState\",\n" + "   \"value\": \"OPEN\"\n" + " }");
        getFixture().processUpdate("ShutterContact", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CONTACT), OpenClosedType.OPEN);

        jsonObject = JsonParser
                .parseString("{\n" + "   \"@type\": \"shutterContactState\",\n" + "   \"value\": \"CLOSED\"\n" + " }");
        getFixture().processUpdate("ShutterContact", jsonObject);
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_CONTACT), OpenClosedType.CLOSED);
    }
}
