/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.provider;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.util.ShellyUtils.mkChannelId;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Tests {@link ShellyChannelDefinitions#createSensorChannels} for the Shelly Presence Gen4: the zone readings only
 * appear once a zone matches the configured main zone, so the channel set must not depend on the first status poll.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelDefinitionsPresenceTest {

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    private static Thing mockThing() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID("shelly", "shellypluspresence", "test"));
        return thing;
    }

    private static Map<String, Channel> createChannels(ShellyStatusSensor sdata) {
        return ShellyChannelDefinitions.createSensorChannels(mockThing(),
                new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSPRESENCE), sdata);
    }

    @Test
    void presenceChannelsAreCreatedEvenWhenNoZoneReportedYet() {
        Map<String, Channel> created = createChannels(new ShellyStatusSensor());

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PRESENCE)));
        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_OBJECT_COUNT)));
        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_CONTROL, CHANNEL_CTRL_SENSOR_ENABLE)));
    }

    @Test
    void presenceChannelsAreCreatedWhenTheZoneIsAlreadyReported() {
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        sdata.presence = true;
        sdata.objectCount = 2;
        sdata.sensorEnable = true;

        Map<String, Channel> created = createChannels(sdata);

        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PRESENCE)));
        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_OBJECT_COUNT)));
        assertTrue(created.containsKey(mkChannelId(CHANNEL_GROUP_CONTROL, CHANNEL_CTRL_SENSOR_ENABLE)));
    }

    @Test
    void presenceChannelsAreNotCreatedForOtherSensors() {
        Map<String, Channel> created = ShellyChannelDefinitions.createSensorChannels(mockThing(),
                new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSSMOKE), new ShellyStatusSensor());

        assertFalse(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PRESENCE)));
        assertFalse(created.containsKey(mkChannelId(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_OBJECT_COUNT)));
        assertFalse(created.containsKey(mkChannelId(CHANNEL_GROUP_CONTROL, CHANNEL_CTRL_SENSOR_ENABLE)));
    }
}
