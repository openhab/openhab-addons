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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.StringType;

/**
 * Tests for {@link Select}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class SelectTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "select/0x54ef44100064b266";

    @SuppressWarnings("null")
    @Test
    public void testSelectWithStateAndCommand() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                    {
                        "availability": [
                            {"topic": "zigbee2mqtt/bridge/state"},
                            {"topic": "zigbee2mqtt/gbos/availability"}
                        ],
                        "availability_mode": "all",
                        "command_topic": "zigbee2mqtt/gbos/set/approach_distance",
                        "device": {
                            "configuration_url": "#/device/0x54ef44100064b266/info",
                            "identifiers": [
                                "zigbee2mqtt_0x54ef44100064b266"
                            ],
                            "manufacturer": "Xiaomi",
                            "model": "Aqara presence detector FP1 (RTCZCGQ11LM)",
                            "name": "Guest Bathroom Occupancy Sensor",
                            "sw_version": ""
                        },
                        "name": "Guest Bathroom Occupancy Sensor approach distance",
                        "options": [
                            "far",
                            "medium",
                            "near"
                        ],
                        "state_topic": "zigbee2mqtt/gbos",
                        "unique_id": "0x54ef44100064b266_approach_distance_zigbee2mqtt",
                        "value_template":"{{ value_json.approach_distance }}"
                    }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("Guest Bathroom Occupancy Sensor approach distance"));

        assertChannel(component, Select.SELECT_CHANNEL_ID, "zigbee2mqtt/gbos", "zigbee2mqtt/gbos/set/approach_distance",
                "Guest Bathroom Occupancy Sensor approach distance", TextValue.class);

        publishMessage("zigbee2mqtt/gbos", "{\"approach_distance\": \"far\"}");
        assertState(component, Select.SELECT_CHANNEL_ID, new StringType("far"));
        publishMessage("zigbee2mqtt/gbos", "{\"approach_distance\": \"medium\"}");
        assertState(component, Select.SELECT_CHANNEL_ID, new StringType("medium"));

        component.getChannel(Select.SELECT_CHANNEL_ID).getState().publishValue(new StringType("near"));
        assertPublished("zigbee2mqtt/gbos/set/approach_distance", "near");
        component.getChannel(Select.SELECT_CHANNEL_ID).getState().publishValue(new StringType("medium"));
        assertPublished("zigbee2mqtt/gbos/set/approach_distance", "medium");
        assertThrows(IllegalArgumentException.class,
                () -> component.getChannel(Select.SELECT_CHANNEL_ID).getState().publishValue(new StringType("bogus")));
        assertNotPublished("zigbee2mqtt/gbos/set/approach_distance", "bogus");
    }

    @SuppressWarnings("null")
    @Test
    public void testSelectWithCommandTemplate() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                    {
                        "availability": [
                            {"topic": "zigbee2mqtt/bridge/state"},
                            {"topic": "zigbee2mqtt/gbos/availability"}
                        ],
                        "availability_mode": "all",
                        "command_topic": "zigbee2mqtt/gbos/set/approach_distance",
                        "command_template": "set to {{ value }}",
                        "device": {
                            "configuration_url": "#/device/0x54ef44100064b266/info",
                            "identifiers": [
                                "zigbee2mqtt_0x54ef44100064b266"
                            ],
                            "manufacturer": "Xiaomi",
                            "model": "Aqara presence detector FP1 (RTCZCGQ11LM)",
                            "name": "Guest Bathroom Occupancy Sensor",
                            "sw_version": ""
                        },
                        "name": "Guest Bathroom Occupancy Sensor approach distance",
                        "options": [
                            "far",
                            "medium",
                            "near"
                        ],
                        "state_topic": "zigbee2mqtt/gbos",
                        "unique_id": "0x54ef44100064b266_approach_distance_zigbee2mqtt",
                        "value_template":"{{ value_json.approach_distance }}"
                    }
                """);

        component.getChannel(Select.SELECT_CHANNEL_ID).getState().publishValue(new StringType("near"));
        assertPublished("zigbee2mqtt/gbos/set/approach_distance", "set to near");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
