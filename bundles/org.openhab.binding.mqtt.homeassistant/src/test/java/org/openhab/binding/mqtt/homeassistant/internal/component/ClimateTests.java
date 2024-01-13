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

/**
 * Tests for {@link Climate}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class ClimateTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "climate/0x847127fffe11dd6a_climate_zigbee2mqtt";

    @SuppressWarnings("null")
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

        publishMessage("zigbee2mqtt/th1", """
                {"running_state": "idle", "away_mode": "ON", \
                "local_temperature": "22.2", "preset": "schedule", "system_mode": "heat", \
                "current_heating_setpoint": "24"}\
                """);
        assertState(component, Climate.ACTION_CH_ID, new StringType("off"));
        assertState(component, Climate.AWAY_MODE_CH_ID, OnOffType.ON);
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, new QuantityType<>(22.2, SIUnits.CELSIUS));
        assertState(component, Climate.HOLD_CH_ID, new StringType("schedule"));
        assertState(component, Climate.MODE_CH_ID, new StringType("heat"));
        assertState(component, Climate.TEMPERATURE_CH_ID, new QuantityType<>(24, SIUnits.CELSIUS));

        component.getChannel(Climate.AWAY_MODE_CH_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/set/away_mode", "OFF");
        component.getChannel(Climate.HOLD_CH_ID).getState().publishValue(new StringType("eco"));
        assertPublished("zigbee2mqtt/th1/set/preset", "eco");
        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("auto"));
        assertPublished("zigbee2mqtt/th1/set/system_mode", "auto");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(25));
        assertPublished("zigbee2mqtt/th1/set/current_heating_setpoint", "25");
    }

    @SuppressWarnings("null")
    @Test
    public void testTS0601ClimateNotSendIfOff() {
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
                         "power_command_topic": "zigbee2mqtt/th1/power",\
                         "unique_id": "0x847127fffe11dd6a_climate_zigbee2mqtt", "send_if_off": "false"}\
                        """);

        assertThat(component.channels.size(), is(7));
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

        publishMessage("zigbee2mqtt/th1", """
                {"running_state": "idle", "away_mode": "ON", \
                "local_temperature": "22.2", "preset": "schedule", "system_mode": "heat", \
                "current_heating_setpoint": "24"}\
                """);
        assertState(component, Climate.ACTION_CH_ID, new StringType("off"));
        assertState(component, Climate.AWAY_MODE_CH_ID, OnOffType.ON);
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, new QuantityType<>(22.2, SIUnits.CELSIUS));
        assertState(component, Climate.HOLD_CH_ID, new StringType("schedule"));
        assertState(component, Climate.MODE_CH_ID, new StringType("heat"));
        assertState(component, Climate.TEMPERATURE_CH_ID, new QuantityType<>(24, SIUnits.CELSIUS));

        // Climate is in OFF state
        component.getChannel(Climate.AWAY_MODE_CH_ID).getState().publishValue(OnOffType.OFF);
        assertNotPublished("zigbee2mqtt/th1/set/away_mode", "OFF");
        component.getChannel(Climate.HOLD_CH_ID).getState().publishValue(new StringType("eco"));
        assertNotPublished("zigbee2mqtt/th1/set/preset", "eco");
        component.getChannel(Climate.MODE_CH_ID).getState().publishValue(new StringType("auto"));
        assertNotPublished("zigbee2mqtt/th1/set/system_mode", "auto");
        component.getChannel(Climate.TEMPERATURE_CH_ID).getState().publishValue(new DecimalType(25));
        assertNotPublished("zigbee2mqtt/th1/set/current_heating_setpoint", "25");
        component.getChannel(Climate.POWER_CH_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/th1/power", "ON");

        // Enabled
        publishMessage("zigbee2mqtt/th1", """
                {"running_state": "heat", "away_mode": "ON", \
                "local_temperature": "22.2", "preset": "schedule", "system_mode": "heat", \
                "current_heating_setpoint": "24"}\
                """);

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

    @SuppressWarnings("null")
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
                 ], "swing_command_template": "swing={{ value }}",\
                 "swing_command_topic": "zigbee2mqtt/th1/swing",\
                 "swing_state_template": "{{ value_json.swing }}",\
                 "swing_state_topic": "zigbee2mqtt/th1", "swing_modes": [ "G1",\
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
                 "temp_step": "1", "precision": "0.5", "send_if_off": "false" }\
                """);

        assertThat(component.channels.size(), is(12));
        assertThat(component.getName(), is("MQTT HVAC"));

        assertChannel(component, Climate.ACTION_CH_ID, "zigbee2mqtt/th1", "", "MQTT HVAC", TextValue.class);
        assertChannel(component, Climate.AUX_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/aux", "MQTT HVAC",
                OnOffValue.class);
        assertChannel(component, Climate.AWAY_MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/away_mode", "MQTT HVAC",
                OnOffValue.class);
        assertChannel(component, Climate.CURRENT_TEMPERATURE_CH_ID, "zigbee2mqtt/th1", "", "MQTT HVAC",
                NumberValue.class);
        assertChannel(component, Climate.FAN_MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/fan_mode", "MQTT HVAC",
                TextValue.class);
        assertChannel(component, Climate.HOLD_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/hold", "MQTT HVAC",
                TextValue.class);
        assertChannel(component, Climate.MODE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/mode", "MQTT HVAC",
                TextValue.class);
        assertChannel(component, Climate.SWING_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/swing", "MQTT HVAC",
                TextValue.class);
        assertChannel(component, Climate.TEMPERATURE_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/temperature",
                "MQTT HVAC", NumberValue.class);
        assertChannel(component, Climate.TEMPERATURE_HIGH_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/temperature_high",
                "MQTT HVAC", NumberValue.class);
        assertChannel(component, Climate.TEMPERATURE_LOW_CH_ID, "zigbee2mqtt/th1", "zigbee2mqtt/th1/temperature_low",
                "MQTT HVAC", NumberValue.class);
        assertChannel(component, Climate.POWER_CH_ID, "", "zigbee2mqtt/th1/power", "MQTT HVAC", OnOffValue.class);

        publishMessage("zigbee2mqtt/th1", """
                { "action": "fan",  "aux": "ON",  "away_mode": "OFF", \
                "current_temperature": "35.5",  "fan_mode": "p2",  "hold": "u2", \
                "mode": "B1",  "swing": "G1",  "temperature": "30", \
                "temperature_high": "37",  "temperature_low": "20" }\
                """);

        assertState(component, Climate.ACTION_CH_ID, new StringType("fan"));
        assertState(component, Climate.AUX_CH_ID, OnOffType.ON);
        assertState(component, Climate.AWAY_MODE_CH_ID, OnOffType.OFF);
        assertState(component, Climate.CURRENT_TEMPERATURE_CH_ID, new QuantityType<>(35.5, ImperialUnits.FAHRENHEIT));
        assertState(component, Climate.FAN_MODE_CH_ID, new StringType("p2"));
        assertState(component, Climate.HOLD_CH_ID, new StringType("u2"));
        assertState(component, Climate.MODE_CH_ID, new StringType("B1"));
        assertState(component, Climate.SWING_CH_ID, new StringType("G1"));
        assertState(component, Climate.TEMPERATURE_CH_ID, new QuantityType<>(30, ImperialUnits.FAHRENHEIT));
        assertState(component, Climate.TEMPERATURE_HIGH_CH_ID, new QuantityType<>(37, ImperialUnits.FAHRENHEIT));
        assertState(component, Climate.TEMPERATURE_LOW_CH_ID, new QuantityType<>(20, ImperialUnits.FAHRENHEIT));

        component.getChannel(Climate.AUX_CH_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/th1/aux", "OFF");
        component.getChannel(Climate.AWAY_MODE_CH_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/th1/away_mode", "ON");
        component.getChannel(Climate.FAN_MODE_CH_ID).getState().publishValue(new StringType("p1"));
        assertPublished("zigbee2mqtt/th1/fan_mode", "fan_mode=p1");
        component.getChannel(Climate.HOLD_CH_ID).getState().publishValue(new StringType("u3"));
        assertPublished("zigbee2mqtt/th1/hold", "hold=u3");
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

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
