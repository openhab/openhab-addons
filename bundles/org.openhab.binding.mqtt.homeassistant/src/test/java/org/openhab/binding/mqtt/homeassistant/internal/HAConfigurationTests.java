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
package org.openhab.binding.mqtt.homeassistant.internal;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.homeassistant.internal.BaseChannelConfiguration.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * @author Jochen Klein - Initial contribution
 */
public class HAConfigurationTests {

    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory())
            .create();

    private static String readTestJson(final String name) {
        StringBuilder result = new StringBuilder();

        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(HAConfigurationTests.class.getResourceAsStream(name), "UTF-8"))) {
            String line;

            while ((line = in.readLine()) != null) {
                result.append(line).append('\n');
            }
            return result.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void testAbbreviations() {
        String json = readTestJson("configA.json");

        BaseChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson);

        assertThat(config.name, is("A"));
        assertThat(config.icon, is("2"));
        assertThat(config.qos, is(1));
        assertThat(config.retain, is(true));
        assertThat(config.value_template, is("B"));
        assertThat(config.unique_id, is("C"));
        assertThat(config.availability_topic, is("D/E"));
        assertThat(config.payload_available, is("F"));
        assertThat(config.payload_not_available, is("G"));

        assertThat(config.device, is(notNullValue()));

        BaseChannelConfiguration.Device device = config.device;
        if (device != null) {
            assertThat(device.identifiers, contains("H"));
            assertThat(device.connections, is(notNullValue()));
            List<@NonNull Connection> connections = device.connections;
            if (connections != null) {
                assertThat(connections.get(0).type, is("I1"));
                assertThat(connections.get(0).identifier, is("I2"));
            }
            assertThat(device.name, is("J"));
            assertThat(device.model, is("K"));
            assertThat(device.sw_version, is("L"));
            assertThat(device.manufacturer, is("M"));
        }
    }

    @Test
    public void testTildeSubstritution() {
        String json = readTestJson("configB.json");

        ComponentSwitch.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentSwitch.ChannelConfiguration.class);

        assertThat(config.availability_topic, is("D/E"));
        assertThat(config.state_topic, is("O/D/"));
        assertThat(config.command_topic, is("P~Q"));
        assertThat(config.device, is(notNullValue()));

        BaseChannelConfiguration.Device device = config.device;
        if (device != null) {
            assertThat(device.identifiers, contains("H"));
        }
    }

    @Test
    public void testSampleFanConfig() {
        String json = readTestJson("configFan.json");

        ComponentFan.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentFan.ChannelConfiguration.class);
        assertThat(config.name, is("Bedroom Fan"));
    }

    @Test
    public void testDeviceListConfig() {
        String json = readTestJson("configDeviceList.json");

        ComponentFan.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentFan.ChannelConfiguration.class);
        assertThat(config.device, is(notNullValue()));

        BaseChannelConfiguration.Device device = config.device;
        if (device != null) {
            assertThat(device.identifiers, is(Arrays.asList("A", "B", "C")));
        }
    }

    @Test
    public void testDeviceSingleStringConfig() {
        String json = readTestJson("configDeviceSingleString.json");

        ComponentFan.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentFan.ChannelConfiguration.class);
        assertThat(config.device, is(notNullValue()));

        BaseChannelConfiguration.Device device = config.device;
        if (device != null) {
            assertThat(device.identifiers, is(Arrays.asList("A")));
        }
    }

    @Test
    public void testTS0601ClimateConfig() {
        String json = readTestJson("configTS0601ClimateThermostat.json");
        ComponentClimate.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentClimate.ChannelConfiguration.class);
        assertThat(config.device, is(notNullValue()));
        assertThat(config.device.identifiers, is(notNullValue()));
        assertThat(config.device.identifiers.get(0), is("zigbee2mqtt_0x847127fffe11dd6a"));
        assertThat(config.device.manufacturer, is("TuYa"));
        assertThat(config.device.model, is("Radiator valve with thermostat (TS0601_thermostat)"));
        assertThat(config.device.name, is("th1"));
        assertThat(config.device.sw_version, is("Zigbee2MQTT 1.18.2"));

        assertThat(config.action_template, is(
                "{% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}"));
        assertThat(config.action_topic, is("zigbee2mqtt/th1"));
        assertThat(config.away_mode_command_topic, is("zigbee2mqtt/th1/set/away_mode"));
        assertThat(config.away_mode_state_template, is("{{ value_json.away_mode }}"));
        assertThat(config.away_mode_state_topic, is("zigbee2mqtt/th1"));
        assertThat(config.current_temperature_template, is("{{ value_json.local_temperature }}"));
        assertThat(config.current_temperature_topic, is("zigbee2mqtt/th1"));
        assertThat(config.hold_command_topic, is("zigbee2mqtt/th1/set/preset"));
        assertThat(config.hold_modes, is(List.of("schedule", "manual", "boost", "complex", "comfort", "eco")));
        assertThat(config.hold_state_template, is("{{ value_json.preset }}"));
        assertThat(config.hold_state_topic, is("zigbee2mqtt/th1"));
        assertThat(config.json_attributes_topic, is("zigbee2mqtt/th1"));
        assertThat(config.max_temp, is(35f));
        assertThat(config.min_temp, is(5f));
        assertThat(config.mode_command_topic, is("zigbee2mqtt/th1/set/system_mode"));
        assertThat(config.mode_state_template, is("{{ value_json.system_mode }}"));
        assertThat(config.mode_state_topic, is("zigbee2mqtt/th1"));
        assertThat(config.modes, is(List.of("heat", "auto", "off")));
        assertThat(config.name, is("th1"));
        assertThat(config.temp_step, is(0.5f));
        assertThat(config.temperature_command_topic, is("zigbee2mqtt/th1/set/current_heating_setpoint"));
        assertThat(config.temperature_state_template, is("{{ value_json.current_heating_setpoint }}"));
        assertThat(config.temperature_state_topic, is("zigbee2mqtt/th1"));
        assertThat(config.temperature_unit, is("C"));
        assertThat(config.unique_id, is("0x847127fffe11dd6a_climate_zigbee2mqtt"));

        assertThat(config.initial, is(21));
        assertThat(config.send_if_off, is(true));
    }

    @Test
    public void testClimateConfig() {
        String json = readTestJson("configClimate.json");
        ComponentClimate.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentClimate.ChannelConfiguration.class);
        assertThat(config.action_template, is("a"));
        assertThat(config.action_topic, is("b"));
        assertThat(config.aux_command_topic, is("c"));
        assertThat(config.aux_state_template, is("d"));
        assertThat(config.aux_state_topic, is("e"));
        assertThat(config.away_mode_command_topic, is("f"));
        assertThat(config.away_mode_state_template, is("g"));
        assertThat(config.away_mode_state_topic, is("h"));
        assertThat(config.current_temperature_template, is("i"));
        assertThat(config.current_temperature_topic, is("j"));
        assertThat(config.fan_mode_command_template, is("k"));
        assertThat(config.fan_mode_command_topic, is("l"));
        assertThat(config.fan_mode_state_template, is("m"));
        assertThat(config.fan_mode_state_topic, is("n"));
        assertThat(config.fan_modes, is(List.of("p1", "p2")));
        assertThat(config.hold_command_template, is("q"));
        assertThat(config.hold_command_topic, is("r"));
        assertThat(config.hold_state_template, is("s"));
        assertThat(config.hold_state_topic, is("t"));
        assertThat(config.hold_modes, is(List.of("u1", "u2", "u3")));
        assertThat(config.json_attributes_template, is("v"));
        assertThat(config.json_attributes_topic, is("w"));
        assertThat(config.mode_command_template, is("x"));
        assertThat(config.mode_command_topic, is("y"));
        assertThat(config.mode_state_template, is("z"));
        assertThat(config.mode_state_topic, is("A"));
        assertThat(config.modes, is(List.of("B1", "B2")));
        assertThat(config.swing_command_template, is("C"));
        assertThat(config.swing_command_topic, is("D"));
        assertThat(config.swing_state_template, is("E"));
        assertThat(config.swing_state_topic, is("F"));
        assertThat(config.swing_modes, is(List.of("G1")));
        assertThat(config.temperature_command_template, is("H"));
        assertThat(config.temperature_command_topic, is("I"));
        assertThat(config.temperature_state_template, is("J"));
        assertThat(config.temperature_state_topic, is("K"));
        assertThat(config.temperature_high_command_template, is("L"));
        assertThat(config.temperature_high_command_topic, is("N"));
        assertThat(config.temperature_high_state_template, is("O"));
        assertThat(config.temperature_high_state_topic, is("P"));
        assertThat(config.temperature_low_command_template, is("Q"));
        assertThat(config.temperature_low_command_topic, is("R"));
        assertThat(config.temperature_low_state_template, is("S"));
        assertThat(config.temperature_low_state_topic, is("T"));
        assertThat(config.power_command_topic, is("U"));
        assertThat(config.initial, is(10));
        assertThat(config.max_temp, is(40f));
        assertThat(config.min_temp, is(0f));
        assertThat(config.temperature_unit, is("F"));
        assertThat(config.temp_step, is(1f));
        assertThat(config.precision, is(0.5f));
        assertThat(config.send_if_off, is(false));
    }
}
