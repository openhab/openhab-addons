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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
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
@NonNullByDefault
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
            throw new UncheckedIOException(e);
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
            List<Connection> connections = device.getConnections();
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
        assertThat(config.stateTopic, is("O/D/"));
        assertThat(config.commandTopic, is("P~Q"));
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

    @SuppressWarnings("null")
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

        assertThat(config.actionTemplate, is(
                "{% set values = {'idle':'off','heat':'heating','cool':'cooling','fan only':'fan'} %}{{ values[value_json.running_state] }}"));
        assertThat(config.actionTopic, is("zigbee2mqtt/th1"));
        assertThat(config.awayModeCommandTopic, is("zigbee2mqtt/th1/set/away_mode"));
        assertThat(config.awayModeStateTemplate, is("{{ value_json.away_mode }}"));
        assertThat(config.awayModeStateTopic, is("zigbee2mqtt/th1"));
        assertThat(config.currentTemperatureTemplate, is("{{ value_json.local_temperature }}"));
        assertThat(config.currentTemperatureTopic, is("zigbee2mqtt/th1"));
        assertThat(config.holdCommandTopic, is("zigbee2mqtt/th1/set/preset"));
        assertThat(config.holdModes, is(List.of("schedule", "manual", "boost", "complex", "comfort", "eco")));
        assertThat(config.holdStateTemplate, is("{{ value_json.preset }}"));
        assertThat(config.holdStateTopic, is("zigbee2mqtt/th1"));
        assertThat(config.jsonAttributesTopic, is("zigbee2mqtt/th1"));
        assertThat(config.maxTemp, is(new BigDecimal(35)));
        assertThat(config.minTemp, is(new BigDecimal(5)));
        assertThat(config.modeCommandTopic, is("zigbee2mqtt/th1/set/system_mode"));
        assertThat(config.modeStateTemplate, is("{{ value_json.system_mode }}"));
        assertThat(config.modeStateTopic, is("zigbee2mqtt/th1"));
        assertThat(config.modes, is(List.of("heat", "auto", "off")));
        assertThat(config.getName(), is("th1"));
        assertThat(config.tempStep, is(new BigDecimal("0.5")));
        assertThat(config.temperatureCommandTopic, is("zigbee2mqtt/th1/set/current_heating_setpoint"));
        assertThat(config.temperatureStateTemplate, is("{{ value_json.current_heating_setpoint }}"));
        assertThat(config.temperatureStateTopic, is("zigbee2mqtt/th1"));
        assertThat(config.temperatureUnit, is(Climate.TemperatureUnit.CELSIUS));
        assertThat(config.getUniqueId(), is("0x847127fffe11dd6a_climate_zigbee2mqtt"));

        assertThat(config.initial, is(21));
        assertThat(config.sendIfOff, is(true));
    }

    @Test
    public void testClimateConfig() {
        String json = readTestJson("configClimate.json");
        Climate.ChannelConfiguration config = AbstractChannelConfiguration.fromString(json, gson,
                Climate.ChannelConfiguration.class);
        assertThat(config.actionTemplate, is("a"));
        assertThat(config.actionTopic, is("b"));
        assertThat(config.auxCommandTopic, is("c"));
        assertThat(config.auxStateTemplate, is("d"));
        assertThat(config.auxStateTopic, is("e"));
        assertThat(config.awayModeCommandTopic, is("f"));
        assertThat(config.awayModeStateTemplate, is("g"));
        assertThat(config.awayModeStateTopic, is("h"));
        assertThat(config.currentTemperatureTemplate, is("i"));
        assertThat(config.currentTemperatureTopic, is("j"));
        assertThat(config.fanModeCommandTemplate, is("k"));
        assertThat(config.fanModeCommandTopic, is("l"));
        assertThat(config.fanModeStateTemplate, is("m"));
        assertThat(config.fanModeStateTopic, is("n"));
        assertThat(config.fanModes, is(List.of("p1", "p2")));
        assertThat(config.holdCommandTemplate, is("q"));
        assertThat(config.holdCommandTopic, is("r"));
        assertThat(config.holdStateTemplate, is("s"));
        assertThat(config.holdStateTopic, is("t"));
        assertThat(config.holdModes, is(List.of("u1", "u2", "u3")));
        assertThat(config.jsonAttributesTemplate, is("v"));
        assertThat(config.jsonAttributesTopic, is("w"));
        assertThat(config.modeCommandTemplate, is("x"));
        assertThat(config.modeCommandTopic, is("y"));
        assertThat(config.modeStateTemplate, is("z"));
        assertThat(config.modeStateTopic, is("A"));
        assertThat(config.modes, is(List.of("B1", "B2")));
        assertThat(config.swingCommandTemplate, is("C"));
        assertThat(config.swingCommandTopic, is("D"));
        assertThat(config.swingStateTemplate, is("E"));
        assertThat(config.swingStateTopic, is("F"));
        assertThat(config.swingModes, is(List.of("G1")));
        assertThat(config.temperatureCommandTemplate, is("H"));
        assertThat(config.temperatureCommandTopic, is("I"));
        assertThat(config.temperatureStateTemplate, is("J"));
        assertThat(config.temperatureStateTopic, is("K"));
        assertThat(config.temperatureHighCommandTemplate, is("L"));
        assertThat(config.temperatureHighCommandTopic, is("N"));
        assertThat(config.temperatureHighStateTemplate, is("O"));
        assertThat(config.temperatureHighStateTopic, is("P"));
        assertThat(config.temperatureLowCommandTemplate, is("Q"));
        assertThat(config.temperatureLowCommandTopic, is("R"));
        assertThat(config.temperatureLowStateTemplate, is("S"));
        assertThat(config.temperatureLowStateTopic, is("T"));
        assertThat(config.powerCommandTopic, is("U"));
        assertThat(config.initial, is(10));
        assertThat(config.maxTemp, is(new BigDecimal(40)));
        assertThat(config.minTemp, is(BigDecimal.ZERO));
        assertThat(config.temperatureUnit, is(Climate.TemperatureUnit.FAHRENHEIT));
        assertThat(config.tempStep, is(BigDecimal.ONE));
        assertThat(config.precision, is(new BigDecimal("0.5")));
        assertThat(config.sendIfOff, is(false));
    }
}
