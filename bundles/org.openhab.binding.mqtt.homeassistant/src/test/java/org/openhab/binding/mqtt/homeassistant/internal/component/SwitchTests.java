/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Switch}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class SwitchTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt";

    @Test
    public void testSwitchWithStateAndCommand() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "command_topic": "zigbee2mqtt/th1/set/auto_lock",
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x847127fffe11dd6a"
                    ],
                    "manufacturer": "TuYa",
                    "model": "Radiator valve with thermostat (TS0601_thermostat)",
                    "name": "th1",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "json_attributes_topic": "zigbee2mqtt/th1",
                  "name": "th1 auto lock",
                  "payload_off": "MANUAL",
                  "payload_on": "AUTO",
                  "state_off": "MANUAL",
                  "state_on": "AUTO",
                  "state_topic": "zigbee2mqtt/th1",
                  "unique_id": "0x847127fffe11dd6a_auto_lock_zigbee2mqtt",
                  "value_template": "{{ value_json.auto_lock }}"
                }\
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/auto_lock", "Switch",
                OnOffValue.class);
        assertChannel(component, Switch.JSON_ATTRIBUTES_CHANNEL_ID, "zigbee2mqtt/th1", "", "JSON Attributes",
                TextValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"MANUAL\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.OFF);
        assertState(component, Switch.JSON_ATTRIBUTES_CHANNEL_ID, new StringType("{\"auto_lock\": \"MANUAL\"}"));
        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"AUTO\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.ON);

        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "MANUAL");
        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "AUTO");
    }

    @Test
    public void testSwitchWithCommand() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "command_topic": "zigbee2mqtt/th1/set/auto_lock",
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x847127fffe11dd6a"
                    ],
                    "manufacturer": "TuYa",
                    "model": "Radiator valve with thermostat (TS0601_thermostat)",
                    "name": "th1",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "json_attributes_topic": "zigbee2mqtt/th1",
                  "name": "th1 auto lock",
                  "payload_off": "MANUAL",
                  "payload_on": "AUTO",
                  "unique_id": "0x847127fffe11dd6a_auto_lock_zigbee2mqtt",
                  "value_template": "{{ value_json.auto_lock }}"
                }\
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("th1 auto lock"));
        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "", "zigbee2mqtt/th1/set/auto_lock", "Switch",
                OnOffValue.class);
        assertChannel(component, Switch.JSON_ATTRIBUTES_CHANNEL_ID, "zigbee2mqtt/th1", "", "JSON Attributes",
                TextValue.class);

        linkAllChannels(component);

        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "MANUAL");
        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "AUTO");
    }

    @Test
    public void testSwitchNoName() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state",
                      "value_template": "{{ value_json.state }}"
                    },
                    {
                      "topic": "zigbee2mqtt/Master Bedroom Subwoofer/availability",
                      "value_template": "{{ value_json.state }}"
                    }
                  ],
                  "availability_mode": "all",
                  "command_topic": "zigbee2mqtt/Master Bedroom Subwoofer/set",
                  "device": {
                    "configuration_url": "http://z2m:8084/#/device/0x00124b0029e7388c/info",
                    "identifiers": [
                      "zigbee2mqtt_0x00124b0029e7388c"
                    ],
                    "manufacturer": "SONOFF",
                    "model": "15A Zigbee smart plug (S40ZBTPB)",
                    "name": "Master Bedroom Subwoofer",
                    "sw_version": "1.1.0",
                    "via_device": "zigbee2mqtt_bridge_0xe0798dfffe882ce4"
                  },
                  "name": null,
                  "object_id": "master_bedroom_subwoofer",
                  "origin": {
                    "name": "Zigbee2MQTT",
                    "sw": "1.42.0-dev",
                    "url": "https://www.zigbee2mqtt.io"
                  },
                  "payload_off": "OFF",
                  "payload_on": "ON",
                  "state_topic": "zigbee2mqtt/Master Bedroom Subwoofer",
                  "unique_id": "0x00124b0029e7388c_switch_zigbee2mqtt",
                  "value_template": "{{ value_json.state }}"
                }
                          """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT Switch"));
        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "zigbee2mqtt/Master Bedroom Subwoofer",
                "zigbee2mqtt/Master Bedroom Subwoofer/set", "MQTT Switch", OnOffValue.class);
    }

    @Test
    public void testUnlinkedChannelsDontSubscribe() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "command_topic": "zigbee2mqtt/th1/set/auto_lock",
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x847127fffe11dd6a"
                    ],
                    "manufacturer": "TuYa",
                    "model": "Radiator valve with thermostat (TS0601_thermostat)",
                    "name": "th1",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "json_attributes_topic": "zigbee2mqtt/th1",
                  "name": "th1 auto lock",
                  "state_off": "MANUAL",
                  "state_on": "AUTO",
                  "state_topic": "zigbee2mqtt/th1",
                  "unique_id": "0x847127fffe11dd6a_auto_lock_zigbee2mqtt",
                  "value_template": "{{ value_json.auto_lock }}"
                }\
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/auto_lock", "Switch",
                OnOffValue.class);
        assertChannel(component, Switch.JSON_ATTRIBUTES_CHANNEL_ID, "zigbee2mqtt/th1", "", "JSON Attributes",
                TextValue.class);

        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"MANUAL\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, UnDefType.UNDEF);
        assertState(component, Switch.JSON_ATTRIBUTES_CHANNEL_ID, UnDefType.UNDEF);

        linkChannel(component, Switch.SWITCH_CHANNEL_ID);

        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"MANUAL\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.OFF);
        assertState(component, Switch.JSON_ATTRIBUTES_CHANNEL_ID, UnDefType.UNDEF);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
