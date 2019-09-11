/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.junit.Test;
import org.openhab.binding.mqtt.homeassistant.internal.BaseChannelConfiguration.Connection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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
}
