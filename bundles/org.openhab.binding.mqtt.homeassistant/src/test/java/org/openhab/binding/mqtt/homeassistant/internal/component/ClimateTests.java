/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;

/**
 * Tests for {@link Climate}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
public class ClimateTests extends AbstractComponentTests {

    @Test
    public void testTS0601Climate() {
        var component = discoverComponent("homeassistant/climate/0x847127fffe11dd6a_climate_zigbee2mqtt/config", "{\n"
                + "  \"action_template\": \"{% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}\",\n"
                + "  \"action_topic\": \"zigbee2mqtt/th1\",\n" + "  \"availability\": [\n" + "    {\n"
                + "      \"topic\": \"zigbee2mqtt/bridge/state\"\n" + "    }\n" + "  ],\n"
                + "  \"away_mode_command_topic\": \"zigbee2mqtt/th1/set/away_mode\",\n"
                + "  \"away_mode_state_template\": \"{{ value_json.away_mode }}\",\n"
                + "  \"away_mode_state_topic\": \"zigbee2mqtt/th1\",\n"
                + "  \"current_temperature_template\": \"{{ value_json.local_temperature }}\",\n"
                + "  \"current_temperature_topic\": \"zigbee2mqtt/th1\",\n" + "  \"device\": {\n"
                + "    \"identifiers\": [\n" + "      \"zigbee2mqtt_0x847127fffe11dd6a\"\n" + "    ],\n"
                + "    \"manufacturer\": \"TuYa\",\n"
                + "    \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                + "    \"name\": \"th1\",\n" + "    \"sw_version\": \"Zigbee2MQTT 1.18.2\"\n" + "  },\n"
                + "  \"hold_command_topic\": \"zigbee2mqtt/th1/set/preset\",\n" + "  \"hold_modes\": [\n"
                + "    \"schedule\",\n" + "    \"manual\",\n" + "    \"boost\",\n" + "    \"complex\",\n"
                + "    \"comfort\",\n" + "    \"eco\"\n" + "  ],\n"
                + "  \"hold_state_template\": \"{{ value_json.preset }}\",\n"
                + "  \"hold_state_topic\": \"zigbee2mqtt/th1\",\n"
                + "  \"json_attributes_topic\": \"zigbee2mqtt/th1\",\n" + "  \"max_temp\": \"35\",\n"
                + "  \"min_temp\": \"5\",\n" + "  \"mode_command_topic\": \"zigbee2mqtt/th1/set/system_mode\",\n"
                + "  \"mode_state_template\": \"{{ value_json.system_mode }}\",\n"
                + "  \"mode_state_topic\": \"zigbee2mqtt/th1\",\n" + "  \"modes\": [\n" + "    \"heat\",\n"
                + "    \"auto\",\n" + "    \"off\"\n" + "  ],\n" + "  \"name\": \"th1\",\n" + "  \"temp_step\": 0.5,\n"
                + "  \"temperature_command_topic\": \"zigbee2mqtt/th1/set/current_heating_setpoint\",\n"
                + "  \"temperature_state_template\": \"{{ value_json.current_heating_setpoint }}\",\n"
                + "  \"temperature_state_topic\": \"zigbee2mqtt/th1\",\n" + "  \"temperature_unit\": \"C\",\n"
                + "  \"unique_id\": \"0x847127fffe11dd6a_climate_zigbee2mqtt\"\n" + "}");

        assertThat(component.channels.size(), is(6));
        assertThat(component.getName(), is("th1"));

        assertChannel(component, Climate.ACTION_CH_ID, "zigbee2mqtt/th1", "", "th1", TextValue.class);
        assertChannel(component, Climate.AWAY_MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/away_mode", "th1",
                OnOffValue.class);
        assertChannel(component, Climate.CURRENT_TEMPERATURE_CH_ID, "zigbee2mqtt/th1", "", "th1", NumberValue.class);
        assertChannel(component, Climate.HOLD_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/preset", "th1",
                TextValue.class);
        assertChannel(component, Climate.MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/system_mode", "th1",
                TextValue.class);
        assertChannel(component, Climate.TEMPERATURE_CH_ID, "zigbee2mqtt/th1",
                "zigbee2mqtt/th1/set/current_heating_setpoint", "th1", NumberValue.class);
    }
}
