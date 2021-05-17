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

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * Tests for {@link Climate}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings("ConstantConditions")
public class ClimateTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "climate/0x847127fffe11dd6a_climate_zigbee2mqtt";

    @Test
    public void testTS0601Climate() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), "{\n"
                + " \"action_template\": \"{% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}\",\n"
                + " \"action_topic\": \"zigbee2mqtt/th1\", \"availability\": [ {\n"
                + " \"topic\": \"zigbee2mqtt/bridge/state\" } ],\n"
                + " \"away_mode_command_topic\": \"zigbee2mqtt/th1/set/away_mode\",\n"
                + " \"away_mode_state_template\": \"{{ value_json.away_mode }}\",\n"
                + " \"away_mode_state_topic\": \"zigbee2mqtt/th1\",\n"
                + " \"current_temperature_template\": \"{{ value_json.local_temperature }}\",\n"
                + " \"current_temperature_topic\": \"zigbee2mqtt/th1\", \"device\": {\n"
                + " \"identifiers\": [ \"zigbee2mqtt_0x847127fffe11dd6a\" ],\n" + " \"manufacturer\": \"TuYa\",\n"
                + " \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                + " \"name\": \"th1\", \"sw_version\": \"Zigbee2MQTT 1.18.2\" },\n"
                + " \"hold_command_topic\": \"zigbee2mqtt/th1/set/preset\", \"hold_modes\": [\n"
                + " \"schedule\", \"manual\", \"boost\", \"complex\",\n" + " \"comfort\", \"eco\" ],\n"
                + " \"hold_state_template\": \"{{ value_json.preset }}\",\n"
                + " \"hold_state_topic\": \"zigbee2mqtt/th1\",\n"
                + " \"json_attributes_topic\": \"zigbee2mqtt/th1\", \"max_temp\": \"35\",\n"
                + " \"min_temp\": \"5\", \"mode_command_topic\": \"zigbee2mqtt/th1/set/system_mode\",\n"
                + " \"mode_state_template\": \"{{ value_json.system_mode }}\",\n"
                + " \"mode_state_topic\": \"zigbee2mqtt/th1\", \"modes\": [ \"heat\",\n"
                + " \"auto\", \"off\" ], \"name\": \"th1\", \"temp_step\": 0.5,\n"
                + " \"temperature_command_topic\": \"zigbee2mqtt/th1/set/current_heating_setpoint\",\n"
                + " \"temperature_state_template\": \"{{ value_json.current_heating_setpoint }}\",\n"
                + " \"temperature_state_topic\": \"zigbee2mqtt/th1\", \"temperature_unit\": \"C\",\n"
                + " \"unique_id\": \"0x847127fffe11dd6a_climate_zigbee2mqtt\"}");

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

        publishMessage("zigbee2mqtt/th1",
                "{\"running_state\": \"idle\", \"away_mode\": \"ON\", "
                        + "\"local_temperature\": \"22.2\", \"preset\": \"schedule\", \"system_mode\": \"heat\", "
                        + "\"current_heating_setpoint\": \"24\"}");
        assertState(component, Climate.ACTION_CH_ID, new StringType("off"));
        assertState(component, Climate.AWAY_MODE_CH_ID, OnOffType.ON);
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, new DecimalType(22.2));
        assertState(component, Climate.HOLD_CH_ID, new StringType("schedule"));
        assertState(component, Climate.MODE_CH_ID, new StringType("heat"));
        assertState(component, Climate.TEMPERATURE_CH_ID, new DecimalType(24));

        component.getChannel(Climate.AWAY_MODE_CH_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/away_mode", "OFF");
        component.getChannel(Climate.HOLD_CH_ID).getState().publishValue(new StringType("eco"));
        assertPublished("zigbee2mqtt/th1/set/preset", "eco");
        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("auto"));
        assertPublished("zigbee2mqtt/th1/set/system_mode", "auto");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(25));
        assertPublished("zigbee2mqtt/th1/set/current_heating_setpoint", "25");
    }

    @Test
    public void testTS0601ClimateNotSendIfOff() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), "{\n"
                + " \"action_template\": \"{% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}\",\n"
                + " \"action_topic\": \"zigbee2mqtt/th1\", \"availability\": [ {\n"
                + " \"topic\": \"zigbee2mqtt/bridge/state\" } ],\n"
                + " \"away_mode_command_topic\": \"zigbee2mqtt/th1/set/away_mode\",\n"
                + " \"away_mode_state_template\": \"{{ value_json.away_mode }}\",\n"
                + " \"away_mode_state_topic\": \"zigbee2mqtt/th1\",\n"
                + " \"current_temperature_template\": \"{{ value_json.local_temperature }}\",\n"
                + " \"current_temperature_topic\": \"zigbee2mqtt/th1\", \"device\": {\n"
                + " \"identifiers\": [ \"zigbee2mqtt_0x847127fffe11dd6a\" ],\n" + " \"manufacturer\": \"TuYa\",\n"
                + " \"model\": \"Radiator valve with thermostat (TS0601_thermostat)\",\n"
                + " \"name\": \"th1\", \"sw_version\": \"Zigbee2MQTT 1.18.2\" },\n"
                + " \"hold_command_topic\": \"zigbee2mqtt/th1/set/preset\", \"hold_modes\": [\n"
                + " \"schedule\", \"manual\", \"boost\", \"complex\",\n" + " \"comfort\", \"eco\" ],\n"
                + " \"hold_state_template\": \"{{ value_json.preset }}\",\n"
                + " \"hold_state_topic\": \"zigbee2mqtt/th1\",\n"
                + " \"json_attributes_topic\": \"zigbee2mqtt/th1\", \"max_temp\": \"35\",\n"
                + " \"min_temp\": \"5\", \"mode_command_topic\": \"zigbee2mqtt/th1/set/system_mode\",\n"
                + " \"mode_state_template\": \"{{ value_json.system_mode }}\",\n"
                + " \"mode_state_topic\": \"zigbee2mqtt/th1\", \"modes\": [ \"heat\",\n"
                + " \"auto\", \"off\" ], \"name\": \"th1\", \"temp_step\": 0.5,\n"
                + " \"temperature_command_topic\": \"zigbee2mqtt/th1/set/current_heating_setpoint\",\n"
                + " \"temperature_state_template\": \"{{ value_json.current_heating_setpoint }}\",\n"
                + " \"temperature_state_topic\": \"zigbee2mqtt/th1\", \"temperature_unit\": \"C\",\n"
                + " \"unique_id\": \"0x847127fffe11dd6a_climate_zigbee2mqtt\", \"send_if_off\": \"false\"}");

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

        publishMessage("zigbee2mqtt/th1",
                "{\"running_state\": \"idle\", \"away_mode\": \"ON\", "
                        + "\"local_temperature\": \"22.2\", \"preset\": \"schedule\", \"system_mode\": \"heat\", "
                        + "\"current_heating_setpoint\": \"24\"}");
        assertState(component, Climate.ACTION_CH_ID, new StringType("off"));
        assertState(component, Climate.AWAY_MODE_CH_ID, OnOffType.ON);
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, new DecimalType(22.2));
        assertState(component, Climate.HOLD_CH_ID, new StringType("schedule"));
        assertState(component, Climate.MODE_CH_ID, new StringType("heat"));
        assertState(component, Climate.TEMPERATURE_CH_ID, new DecimalType(24));

        // Climate is in OFF state
        component.getChannel(Climate.AWAY_MODE_CH_ID).getState().publishValue(OnOffType.OFF);
        assertNotPublished("zigbee2mqtt/th1/set/away_mode", "OFF");
        component.getChannel(Climate.HOLD_CH_ID).getState().publishValue(new StringType("eco"));
        assertNotPublished("zigbee2mqtt/th1/set/preset", "eco");
        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("auto"));
        assertNotPublished("zigbee2mqtt/th1/set/system_mode", "auto");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(25));
        assertNotPublished("zigbee2mqtt/th1/set/current_heating_setpoint", "25");

        // Enabled
        publishMessage("zigbee2mqtt/th1",
                "{\"running_state\": \"heat\", \"away_mode\": \"ON\", "
                        + "\"local_temperature\": \"22.2\", \"preset\": \"schedule\", \"system_mode\": \"heat\", "
                        + "\"current_heating_setpoint\": \"24\"}");

        // Climate is in ON state
        component.getChannel(Climate.AWAY_MODE_CH_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/away_mode", "OFF");
        component.getChannel(Climate.HOLD_CH_ID).getState().publishValue(new StringType("eco"));
        assertPublished("zigbee2mqtt/th1/set/preset", "eco");
        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("auto"));
        assertPublished("zigbee2mqtt/th1/set/system_mode", "auto");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(25));
        assertPublished("zigbee2mqtt/th1/set/current_heating_setpoint", "25");
    }

    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
