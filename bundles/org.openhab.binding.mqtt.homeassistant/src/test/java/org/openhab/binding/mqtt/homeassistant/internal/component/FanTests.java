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
package org.openhab.binding.mqtt.homeassistant.internal.component;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests for {@link Fan}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class FanTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "fan/0x0000000000000000_fan_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Fans inc", \
                    "model": "Fan", \
                    "name": "FanBlower", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "fan", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "state_topic": "zigbee2mqtt/fan/state", \
                  "command_topic": "zigbee2mqtt/fan/set/state" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("fan"));

        assertChannel(component, Fan.SWITCH_CHANNEL_ID, "zigbee2mqtt/fan/state", "zigbee2mqtt/fan/set/state", "fan",
                OnOffValue.class);

        publishMessage("zigbee2mqtt/fan/state", "ON_");
        assertState(component, Fan.SWITCH_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/fan/state", "ON_");
        assertState(component, Fan.SWITCH_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/fan/state", "OFF_");
        assertState(component, Fan.SWITCH_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/fan/state", "ON_");
        assertState(component, Fan.SWITCH_CHANNEL_ID, OnOffType.ON);

        component.getChannel(Fan.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/fan/set/state", "OFF_");
        component.getChannel(Fan.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/fan/set/state", "ON_");
    }

    @SuppressWarnings("null")
    @Test
    public void testCommandTemplate() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                        {
                            "availability": [
                            {
                                "topic": "zigbee2mqtt/bridge/state"
                            }
                            ],
                            "device": {
                            "identifiers": [
                                "zigbee2mqtt_0x0000000000000000"
                            ],
                            "manufacturer": "Fans inc",
                            "model": "Fan",
                            "name": "FanBlower",
                            "sw_version": "Zigbee2MQTT 1.18.2"
                            },
                            "name": "fan",
                            "payload_off": "OFF_",
                            "payload_on": "ON_",
                            "state_topic": "zigbee2mqtt/fan/state",
                            "command_topic": "zigbee2mqtt/fan/set/state",
                            "command_template": "set to {{ value }}"
                        }
                """);

        assertThat(component.channels.size(), is(1));

        component.getChannel(Fan.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/fan/set/state", "set to OFF_");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
