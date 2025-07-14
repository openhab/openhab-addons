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
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;

/**
 * Tests for {@link Climate}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class ClimateTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "climate/0x847127fffe11dd6a_climate_zigbee2mqtt";
    public static final String TION_CONFIG_TOPIC = "climate/living-room-tion-3s/living-room-tion-3s";

    @Test
    public void testTS0601Climate() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                        {\
                         "action_template": "{% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}",\
                         "action_topic": "zigbee2mqtt/th1", "availability": [ {\
                         "topic": "zigbee2mqtt/bridge/state" } ],\
                         "away_mode_command_topic": "zigbee2mqtt/th1/set/away_mode",\
                         "away_mode_state_template": "{{ value_json.away_mode }}",\
                         "away_mode_state_topic": "zigbee2mqtt/th1",\
                         "current_temperature_template": "{{ value_json.local_temperature }}",\
                         "current_temperature_topic": "zigbee2mqtt/th1", "device": {\
                         "identifiers": [ "zigbee2mqtt_0x847127fffe11dd6a" ],  "manufacturer": "TuYa",\
                         "model": "Radiator valve with thermostat (TS0601_thermostat)",\
                         "name": "th1", "sw_version": "Zigbee2MQTT 1.18.2" },\
                         "hold_command_topic": "zigbee2mqtt/th1/set/preset", "hold_modes": [\
                         "schedule", "manual", "boost", "complex",  "comfort", "eco" ],\
                         "hold_state_template": "{{ value_json.preset }}",\
                         "hold_state_topic": "zigbee2mqtt/th1",\
                         "json_attributes_topic": "zigbee2mqtt/th1", "max_temp": "35",\
                         "min_temp": "5", "mode_command_topic": "zigbee2mqtt/th1/set/system_mode",\
                         "mode_state_template": "{{ value_json.system_mode }}",\
                         "mode_state_topic": "zigbee2mqtt/th1", "modes": [ "heat",\
                         "auto", "off" ], "name": "th1", "temp_step": 0.5,\
                         "temperature_command_topic": "zigbee2mqtt/th1/set/current_heating_setpoint",\
                         "temperature_state_template": "{{ value_json.current_heating_setpoint }}",\
                         "temperature_state_topic": "zigbee2mqtt/th1", "temperature_unit": "C",\
                         "unique_id": "0x847127fffe11dd6a_climate_zigbee2mqtt"}\
                        """);

        assertThat(component.channels.size(), is(5));
        assertThat(component.getName(), is("th1"));

        assertChannel(component, Climate.ACTION_CH_ID, "zigbee2mqtt/th1", "", "Action", TextValue.class);
        assertChannel(component, Climate.CURRENT_TEMPERATURE_CH_ID, "zigbee2mqtt/th1", "", "Current Temperature",
                NumberValue.class);
        assertChannel(component, Climate.MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/set/system_mode", "Mode",
                TextValue.class);
        assertChannel(component, Climate.TEMPERATURE_CH_ID, "zigbee2mqtt/th1",
                "zigbee2mqtt/th1/set/current_heating_setpoint", "Target Temperature", NumberValue.class);
        assertChannel(component, Climate.JSON_ATTRIBUTES_CHANNEL_ID, "zigbee2mqtt/th1", "", "JSON Attributes",
                TextValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/th1", """
                {"running_state": "idle", "away_mode": "ON", \
                "local_temperature": "22.2", "preset": "schedule", "system_mode": "heat", \
                "current_heating_setpoint": "24"}\
                """);
        assertState(component, Climate.ACTION_CH_ID, new StringType("off"));
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, QuantityType.valueOf(22.2, SIUnits.CELSIUS));
        assertState(component, Climate.MODE_CH_ID, new StringType("heat"));
        assertState(component, Climate.TEMPERATURE_CH_ID, QuantityType.valueOf(24, SIUnits.CELSIUS));

        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("auto"));
        assertPublished("zigbee2mqtt/th1/set/system_mode", "auto");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(25));
        assertPublished("zigbee2mqtt/th1/set/current_heating_setpoint", "25");
    }

    @Test
    public void testClimate() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {"action_template": "{{ value_json.action }}", "action_topic": "zigbee2mqtt/th1",\
                 "aux_command_topic": "zigbee2mqtt/th1/aux",\
                 "aux_state_template": "{{ value_json.aux }}", "aux_state_topic": "zigbee2mqtt/th1",\
                 "away_mode_command_topic": "zigbee2mqtt/th1/away_mode",\
                 "away_mode_state_template": "{{ value_json.away_mode }}",\
                 "away_mode_state_topic": "zigbee2mqtt/th1",\
                 "current_temperature_template": "{{ value_json.current_temperature }}",\
                 "current_temperature_topic": "zigbee2mqtt/th1",\
                 "fan_mode_command_template": "fan_mode={{ value }}",\
                 "fan_mode_command_topic": "zigbee2mqtt/th1/fan_mode",\
                 "fan_mode_state_template": "{{ value_json.fan_mode }}",\
                 "fan_mode_state_topic": "zigbee2mqtt/th1", "fan_modes": [ "p1",\
                 "p2" ], "hold_command_template": "hold={{ value }}",\
                 "hold_command_topic": "zigbee2mqtt/th1/hold",\
                 "hold_state_template": "{{ value_json.hold }}",\
                 "hold_state_topic": "zigbee2mqtt/th1", "hold_modes": [ "u1", "u2",\
                 "u3" ], "json_attributes_template": "{{ value_json.attrs }}",\
                 "json_attributes_topic": "zigbee2mqtt/th1",\
                 "mode_command_template": "mode={{ value }}",\
                 "mode_command_topic": "zigbee2mqtt/th1/mode",\
                 "mode_state_template": "{{ value_json.mode }}",\
                 "mode_state_topic": "zigbee2mqtt/th1", "modes": [ "B1", "B2"\
                 ], "swing_mode_command_template": "swing={{ value }}",\
                 "swing_mode_command_topic": "zigbee2mqtt/th1/swing",\
                 "swing_mode_state_template": "{{ value_json.swing }}",\
                 "swing_mode_state_topic": "zigbee2mqtt/th1", "swing_modes": [ "G1",\
                 "G2" ], "temperature_command_template": "temperature={{ value }}",\
                 "temperature_command_topic": "zigbee2mqtt/th1/temperature",\
                 "temperature_state_template": "{{ value_json.temperature }}",\
                 "temperature_state_topic": "zigbee2mqtt/th1",\
                 "temperature_high_command_template": "temperature_high={{ value }}",\
                 "temperature_high_command_topic": "zigbee2mqtt/th1/temperature_high",\
                 "temperature_high_state_template": "{{ value_json.temperature_high }}",\
                 "temperature_high_state_topic": "zigbee2mqtt/th1",\
                 "temperature_low_command_template": "temperature_low={{ value }}",\
                 "temperature_low_command_topic": "zigbee2mqtt/th1/temperature_low",\
                 "temperature_low_state_template": "{{ value_json.temperature_low }}",\
                 "temperature_low_state_topic": "zigbee2mqtt/th1",\
                 "power_command_topic": "zigbee2mqtt/th1/power", "initial": "10",\
                 "max_temp": "40", "min_temp": "0", "temperature_unit": "F",\
                 "temp_step": "1", "precision": 0.5, "send_if_off": "false" }\
                """);

        assertThat(component.channels.size(), is(10));
        assertThat(component.getName(), is("MQTT HVAC"));

        assertChannel(component, Climate.ACTION_CH_ID, "zigbee2mqtt/th1", "", "Action", TextValue.class);
        assertChannel(component, Climate.CURRENT_TEMPERATURE_CH_ID, "zigbee2mqtt/th1", "", "Current Temperature",
                NumberValue.class);
        assertChannel(component, Climate.FAN_MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/fan_mode", "Fan Mode",
                TextValue.class);
        assertChannel(component, Climate.MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/mode", "Mode",
                TextValue.class);
        assertChannel(component, Climate.SWING_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/swing", "Swing Mode",
                TextValue.class);
        assertChannel(component, Climate.TEMPERATURE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/temperature",
                "Target Temperature", NumberValue.class);
        assertChannel(component, Climate.TEMPERATURE_HIGH_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/temperature_high",
                "Highest Allowed Temperature", NumberValue.class);
        assertChannel(component, Climate.TEMPERATURE_LOW_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/temperature_low",
                "Lowest Allowed Temperature", NumberValue.class);
        assertChannel(component, Climate.POWER_CH_ID, "", "zigbee2mqtt/th1/power", "Power", OnOffValue.class);
        assertChannel(component, Climate.JSON_ATTRIBUTES_CHANNEL_ID, "zigbee2mqtt/th1", "", "JSON Attributes",
                TextValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/th1", """
                { "action": "fan",  "aux": "ON",  "away_mode": "OFF", \
                "current_temperature": "35.5",  "fan_mode": "p2",  "hold": "u2", \
                "mode": "B1",  "swing": "G1",  "temperature": "30", \
                "temperature_high": "37",  "temperature_low": "20" }\
                """);

        assertState(component, Climate.ACTION_CH_ID, new StringType("fan"));
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, QuantityType.valueOf(35.5, ImperialUnits.FAHRENHEIT));
        assertState(component, Climate.FAN_MODE_CH_ID, new StringType("p2"));
        assertState(component, Climate.MODE_CH_ID, new StringType("B1"));
        assertState(component, Climate.SWING_CH_ID, new StringType("G1"));
        assertState(component, Climate.TEMPERATURE_CH_ID, QuantityType.valueOf(30, ImperialUnits.FAHRENHEIT));
        assertState(component, Climate.TEMPERATURE_HIGH_CH_ID, QuantityType.valueOf(37, ImperialUnits.FAHRENHEIT));
        assertState(component, Climate.TEMPERATURE_LOW_CH_ID, QuantityType.valueOf(20, ImperialUnits.FAHRENHEIT));

        component.getChannel(Climate.FAN_MODE_CH_ID).getState().publishValue(new StringType("p1"));
        assertPublished("zigbee2mqtt/th1/fan_mode", "fan_mode=p1");
        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("B2"));
        assertPublished("zigbee2mqtt/th1/mode", "mode=B2");
        component.getChannel(Climate.SWING_CH_ID).getState().publishValue(new StringType("G2"));
        assertPublished("zigbee2mqtt/th1/swing", "swing=G2");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(30.5));
        assertPublished("zigbee2mqtt/th1/temperature", "temperature=30.5");
        component.getChannel(Climate.TEMPERATURE_HIGH_CH_ID).getState().publishValue(new DecimalType(39.5));
        assertPublished("zigbee2mqtt/th1/temperature_high", "temperature_high=39.5");
        component.getChannel(Climate.TEMPERATURE_LOW_CH_ID).getState().publishValue(new DecimalType(19.5));
        assertPublished("zigbee2mqtt/th1/temperature_low", "temperature_low=19.5");
        component.getChannel(Climate.POWER_CH_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/power", "OFF");
    }

    @Test
    public void testClimateWithPresetMode() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                        {
                        "action_template": "{% set values = {None:None,'idle':'idle','heat':'heating','cool':'cooling','fan_only':'fan'} %}{{ values[value_json.running_state] }}",
                        "action_topic": "zigbee2mqtt/th2",
                        "current_temperature_template": "{{ value_json.local_temperature }}",
                        "current_temperature_topic": "zigbee2mqtt/th2",
                        "json_attributes_topic": "zigbee2mqtt/th2",
                        "max_temp": "35",
                        "min_temp": "5",
                        "mode_command_topic": "zigbee2mqtt/th2/set/system_mode",
                        "mode_state_template": "{{ value_json.system_mode }}",
                        "mode_state_topic": "zigbee2mqtt/th2",
                        "modes": ["auto","heat","off"],
                        "name": "th2",
                        "preset_mode_command_topic": "zigbee2mqtt/th2/set/preset",
                        "preset_mode_state_topic": "zigbee2mqtt/th2",
                        "preset_mode_value_template": "{{ value_json.preset }}",
                        "preset_modes": ["auto","manual","off","on"],
                        "temp_step": 0.5,
                        "temperature_command_topic": "zigbee2mqtt/th2/set/current_heating_setpoint",
                        "temperature_state_template": "{{ value_json.current_heating_setpoint }}",
                        "temperature_state_topic": "zigbee2mqtt/th2",
                        "temperature_unit": "C"
                        }
                        """);

        assertThat(component.channels.size(), is(6));

        assertChannel(component, Climate.PRESET_MODE_CH_ID, "zigbee2mqtt/th2", "zigbee2mqtt/th2/set/preset", "Preset",
                TextValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/th2", """
                {"running_state": "heat",
                "local_temperature": "22.2", "preset": "manual", "system_mode": "heat",
                "current_heating_setpoint": "24"}
                """);
        assertState(component, Climate.MODE_CH_ID, new StringType("heat"));
        assertState(component, Climate.TEMPERATURE_CH_ID, QuantityType.valueOf(24, SIUnits.CELSIUS));
        assertState(component, Climate.PRESET_MODE_CH_ID, new StringType("manual"));
        TextValue presetModes = (TextValue) component.getChannel(Climate.PRESET_MODE_CH_ID).getState().getCache();
        Set<String> presets = presetModes.getStates().keySet();
        assertThat(presets.size(), is(5));
        assertThat(presets.contains("none"), is(true));
        assertThat(presets.contains("auto"), is(true));
        assertThat(presets.contains("manual"), is(true));
        assertThat(presets.contains("off"), is(true));
        assertThat(presets.contains("on"), is(true));
        component.getChannel(Climate.PRESET_MODE_CH_ID).getState().publishValue(new StringType("on"));
        assertPublished("zigbee2mqtt/th2/set/preset", "on");
    }

    @Test
    public void testClimateHumidity() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                "current_humidity_template": "{{ value_json.humidity }}",
                "current_humidity_topic": "zigbee2mqtt/th2",
                "max_humidity": "70",
                "min_humidity": "30",
                "target_humidity_command_topic": "zigbee2mqtt/th2/set/humidity_setpoint",
                "target_humidity_state_template": "{{ value_json.humidity_setpoint }}",
                "target_humidity_state_topic": "zigbee2mqtt/th2",
                "name": "th2"
                }
                """);

        assertThat(component.channels.size(), is(2));

        assertChannel(component, Climate.CURRENT_HUMIDITY_CH_ID, "zigbee2mqtt/th2", "", "Current Humidity",
                NumberValue.class);
        assertChannel(component, Climate.TARGET_HUMIDITY_CH_ID, "zigbee2mqtt/th2",
                "zigbee2mqtt/th2/set/humidity_setpoint", "Target Humidity", NumberValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/th2", """
                {"humidity": "55", "humidity_setpoint": "50"}\
                """);
        assertState(component, Climate.CURRENT_HUMIDITY_CH_ID, QuantityType.valueOf(55, Units.PERCENT));
        assertState(component, Climate.TARGET_HUMIDITY_CH_ID, QuantityType.valueOf(50, Units.PERCENT));
    }

    @Test
    public void testClimateWithEmptyName() {
        var component = discoverComponent(configTopicToMqtt(TION_CONFIG_TOPIC), """
                {
                  "curr_temp_t": "living-room-tion-3s/climate/living-room-tion-3s/current_temperature/state",
                  "mode_cmd_t": "living-room-tion-3s/climate/living-room-tion-3s/mode/command",
                  "mode_stat_t": "living-room-tion-3s/climate/living-room-tion-3s/mode/state",
                  "modes": [
                    "off",
                    "heat",
                    "fan_only",
                    "heat_cool"
                  ],
                  "temp_cmd_t": "living-room-tion-3s/climate/living-room-tion-3s/target_temperature/command",
                  "temp_stat_t": "living-room-tion-3s/climate/living-room-tion-3s/target_temperature/state",
                  "min_temp": 1,
                  "max_temp": 25,
                  "temp_step": 1,
                  "precision": 1,
                  "temp_unit": "C",
                  "min_hum": 30,
                  "max_hum": 99,
                  "act_t": "living-room-tion-3s/climate/living-room-tion-3s/action/state",
                  "fan_mode_cmd_t": "living-room-tion-3s/climate/living-room-tion-3s/fan_mode/command",
                  "fan_mode_stat_t": "living-room-tion-3s/climate/living-room-tion-3s/fan_mode/state",
                  "fan_modes": [
                    "1",
                    "2",
                    "3",
                    "4",
                    "5",
                    "6"
                  ],
                  "name": "",
                  "ic": "mdi:air-filter",
                  "avty_t": "living-room-tion-3s/status",
                  "uniq_id": "ESPclimateliving-room-tion-3s",
                  "dev": {
                    "ids": "f09e9e213ab0",
                    "name": "living-room-tion-3s",
                    "sw": "2024.8.0 (ESPHome 2024.11.3)",
                    "mdl": "tion",
                    "mf": "dentra",
                    "cns": [
                      [
                        "mac",
                        "f09e9e213ab0"
                      ]
                    ]
                  }
                }
                """);

        assertThat(component.channels.size(), is(5));
        assertThat(component.getName(), is("MQTT HVAC"));

        assertChannel(component, Climate.ACTION_CH_ID, "living-room-tion-3s/climate/living-room-tion-3s/action/state",
                "", "Action", TextValue.class);
        assertChannel(component, Climate.CURRENT_TEMPERATURE_CH_ID,
                "living-room-tion-3s/climate/living-room-tion-3s/current_temperature/state", "", "Current Temperature",
                NumberValue.class);
        assertChannel(component, Climate.FAN_MODE_CH_ID,
                "living-room-tion-3s/climate/living-room-tion-3s/fan_mode/state",
                "living-room-tion-3s/climate/living-room-tion-3s/fan_mode/command", "Fan Mode", TextValue.class);
        assertChannel(component, Climate.MODE_CH_ID, "living-room-tion-3s/climate/living-room-tion-3s/mode/state",
                "living-room-tion-3s/climate/living-room-tion-3s/mode/command", "Mode", TextValue.class);
        assertChannel(component, Climate.TEMPERATURE_CH_ID,
                "living-room-tion-3s/climate/living-room-tion-3s/target_temperature/state",
                "living-room-tion-3s/climate/living-room-tion-3s/target_temperature/command", "Target Temperature",
                NumberValue.class);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC, TION_CONFIG_TOPIC);
    }
}
