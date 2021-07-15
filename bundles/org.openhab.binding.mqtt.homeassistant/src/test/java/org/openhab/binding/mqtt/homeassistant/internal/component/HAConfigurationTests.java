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
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.AbstractChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.Connection;
import org.openhab.binding.mqtt.homeassistant.internal.config.dto.Device;

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

        AbstractChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson);

        assertThat(config.getName(), is("A"));
        assertThat(config.getIcon(), is("2"));
        assertThat(config.getQos(), is(1));
        assertThat(config.isRetain(), is(true));
        assertThat(config.getValueTemplate(), is("B"));
        assertThat(config.getUniqueId(), is("C"));
        assertThat(config.getAvailabilityTopic(), is("D/E"));
        assertThat(config.getPayloadAvailable(), is("F"));
        assertThat(config.getPayloadNotAvailable(), is("G"));

        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), contains("H"));
            assertThat(device.getConnections(), is(notNullValue()));
            List<@NonNull Connection> connections = device.getConnections();
            if (connections != null) {
                assertThat(connections.get(0).getType(), is("I1"));
                assertThat(connections.get(0).getIdentifier(), is("I2"));
            }
            assertThat(device.getName(), is("J"));
            assertThat(device.getModel(), is("K"));
            assertThat(device.getSwVersion(), is("L"));
            assertThat(device.getManufacturer(), is("M"));
        }
    }

    @Test
    public void testTildeSubstritution() {
        String json = readTestJson("configB.json");

        Switch.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Switch.ChannelConfiguration.class);

        assertThat(config.getAvailabilityTopic(), is("D/E"));
        assertThat(config.state_topic, is("O/D/"));
        assertThat(config.command_topic, is("P~Q"));
        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), contains("H"));
        }
    }

    @Test
    public void testSampleFanConfig() {
        String json = readTestJson("configFan.json");

        Fan.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Fan.ChannelConfiguration.class);
        assertThat(config.getName(), is("Bedroom Fan"));
    }

    @Test
    public void testDeviceListConfig() {
        String json = readTestJson("configDeviceList.json");

        Fan.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Fan.ChannelConfiguration.class);
        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), is(Arrays.asList("A", "B", "C")));
        }
    }

    @Test
    public void testDeviceSingleStringConfig() {
        String json = readTestJson("configDeviceSingleString.json");

        Fan.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Fan.ChannelConfiguration.class);
        assertThat(config.getDevice(), is(notNullValue()));

        Device device = config.getDevice();
        if (device != null) {
            assertThat(device.getIdentifiers(), is(Arrays.asList("A")));
        }
    }

    @Test
    public void testTS0601ClimateConfig() {
        String json = readTestJson("configTS0601ClimateThermostat.json");
        Climate.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Climate.ChannelConfiguration.class);
        assertThat(config.getDevice(), is(notNullValue()));
        assertThat(config.getDevice().getIdentifiers(), is(notNullValue()));
        assertThat(config.getDevice().getIdentifiers().get(0), is("zigbee2mqtt_0x847127fffe11dd6a"));
        assertThat(config.getDevice().getManufacturer(), is("TuYa"));
        assertThat(config.getDevice().getModel(), is("Radiator valve with thermostat (TS0601_thermostat)"));
        assertThat(config.getDevice().getName(), is("th1"));
        assertThat(config.getDevice().getSwVersion(), is("Zigbee2MQTT 1.18.2"));

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
        assertThat(config.getName(), is("th1"));
        assertThat(config.temp_step, is(0.5f));
        assertThat(config.temperature_command_topic, is("zigbee2mqtt/th1/set/current_heating_setpoint"));
        assertThat(config.temperature_state_template, is("{{ value_json.current_heating_setpoint }}"));
        assertThat(config.temperature_state_topic, is("zigbee2mqtt/th1"));
        assertThat(config.temperature_unit, is("C"));
        assertThat(config.getUniqueId(), is("0x847127fffe11dd6a_climate_zigbee2mqtt"));

        assertThat(config.initial, is(21));
        assertThat(config.send_if_off, is(true));
    }

    @Test
    public void testClimateConfig() {
        String json = readTestJson("configClimate.json");
        Climate.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Climate.ChannelConfiguration.class);
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
