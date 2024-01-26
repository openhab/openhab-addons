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
package org.openhab.binding.boschshc.internal.devices.universalswitch;

import static org.mockito.Mockito.verify;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.boschshc.internal.devices.AbstractBatteryPoweredDeviceHandlerTest;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Unit tests for {@link UniversalSwitchHandler}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class UniversalSwitchHandlerTest extends AbstractBatteryPoweredDeviceHandlerTest<UniversalSwitchHandler> {

    @Override
    protected UniversalSwitchHandler createFixture() {
        return new UniversalSwitchHandler(getThing(), () -> ZoneId.systemDefault());
    }

    @Override
    protected String getDeviceID() {
        return "hdm:ZigBee:001e43d085b91a96";
    }

    @Override
    protected ThingTypeUID getThingTypeUID() {
        return BoschSHCBindingConstants.THING_TYPE_UNIVERSAL_SWITCH;
    }

    @Test
    void testUpdateChannelsKeypadService() {
        JsonElement jsonObject = JsonParser.parseString("""
                {
                  "@type":"keypadState",
                  "keyCode":1,
                  "keyName":"UPPER_LEFT_BUTTON",
                  "eventType":"PRESS_SHORT",
                  "eventTimestamp":1705130891435
                }
                """);

        getFixture().processUpdate("Keypad", jsonObject);

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_KEY_CODE), new DecimalType(1));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_KEY_NAME),
                new StringType("UPPER_LEFT_BUTTON"));

        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_KEY_EVENT_TYPE),
                new StringType("PRESS_SHORT"));

        ZonedDateTime expectedTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1705130891435l),
                ZoneId.systemDefault());
        verify(getCallback()).stateUpdated(
                new ChannelUID(getThing().getUID(), BoschSHCBindingConstants.CHANNEL_KEY_EVENT_TIMESTAMP),
                new DateTimeType(expectedTime));
    }
}
