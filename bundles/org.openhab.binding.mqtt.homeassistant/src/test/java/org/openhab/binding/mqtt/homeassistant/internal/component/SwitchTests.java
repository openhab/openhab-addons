/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
 * Tests for {@link Switch}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings("ConstantConditions")
@NonNullByDefault
public class SwitchTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt";

    @Test
    public void testSwitchWithStateAndCommand() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                "" + "{\n" + "  \"availability\": [\n" + "    {\n" + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n"
                        + "    }\n" + "  ],\n" + "  \"command_topic\": \"zigbee2mqtt/th1/set/auto_lock\",\n"
                        + "  \"device\": {\n" + "    \"identifiers\": [\n"
                        + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                        + "    \"manufacturer\": \"TuYa\",\n"
                        + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                        + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                        + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"name\": \"th1 auto lock\",\n"
                        + "  \"payload_off\": \"MANUAL\",\n" + "  \"payload_on\": \"AUTO\",\n"
                        + "  \"state_off\": \"MANUAL\",\n" + "  \"state_on\": \"AUTO\",\n"
                        + "  \"state_topic\": \"zigbee2mqtt/th1\",\n"
                        + "  \"unique_id\": \"0x847127fffe11dd6a_auto_lock_zigbee2mqtt\",\n"
                        + "  \"value_template\": \"{{ value_json.auto_lock }}\"\n" + "}");

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/auto_lock", "state",
                OnOffValue.class);

        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"MANUAL\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"AUTO\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.ON);

        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "MANUAL");
        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "AUTO");
    }

    @Test
    public void testSwitchWithState() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                "" + "{\n" + "  \"availability\": [\n" + "    {\n" + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n"
                        + "    }\n" + "  ],\n" + "  \"device\": {\n" + "    \"identifiers\": [\n"
                        + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                        + "    \"manufacturer\": \"TuYa\",\n"
                        + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                        + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                        + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"name\": \"th1 auto lock\",\n"
                        + "  \"state_off\": \"MANUAL\",\n" + "  \"state_on\": \"AUTO\",\n"
                        + "  \"state_topic\": \"zigbee2mqtt/th1\",\n"
                        + "  \"unique_id\": \"0x847127fffe11dd6a_auto_lock_zigbee2mqtt\",\n"
                        + "  \"value_template\": \"{{ value_json.auto_lock }}\"\n" + "}");

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "zigbee2mqtt/th1", "", "state", OnOffValue.class);

        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"MANUAL\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/th1", "{\"auto_lock\": \"AUTO\"}");
        assertState(component, Switch.SWITCH_CHANNEL_ID, OnOffType.ON);
    }

    @Test
    public void testSwitchWithCommand() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                "" + "{\n" + "  \"availability\": [\n" + "    {\n" + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n"
                        + "    }\n" + "  ],\n" + "  \"command_topic\": \"zigbee2mqtt/th1/set/auto_lock\",\n"
                        + "  \"device\": {\n" + "    \"identifiers\": [\n"
                        + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                        + "    \"manufacturer\": \"TuYa\",\n"
                        + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                        + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                        + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"name\": \"th1 auto lock\",\n"
                        + "  \"payload_off\": \"MANUAL\",\n" + "  \"payload_on\": \"AUTO\",\n"
                        + "  \"unique_id\": \"0x847127fffe11dd6a_auto_lock_zigbee2mqtt\",\n"
                        + "  \"value_template\": \"{{ value_json.auto_lock }}\"\n" + "}");

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("th1 auto lock"));

        assertChannel(component, Switch.SWITCH_CHANNEL_ID, "", "zigbee2mqtt/th1/set/auto_lock", "state",
                OnOffValue.class);

        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "MANUAL");
        component.getChannel(Switch.SWITCH_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/th1/set/auto_lock", "AUTO");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
