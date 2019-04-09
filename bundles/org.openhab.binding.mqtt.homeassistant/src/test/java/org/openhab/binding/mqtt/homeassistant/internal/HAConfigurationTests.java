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

import org.junit.Test;
import org.openhab.binding.mqtt.homeassistant.internal.BaseChannelConfiguration;
import org.openhab.binding.mqtt.homeassistant.internal.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentFan;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentSwitch;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class HAConfigurationTests {

    private Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory())
            .create();

    @Test
    public void testAbbreviations() {
        String json = "{\n"//
                + "    \"name\":\"A\",\n" //
                + "    \"icon\":\"2\",\n" //
                + "    \"qos\":1,\n" //
                + "    \"retain\":true,\n" //
                + "    \"val_tpl\":\"B\",\n" //
                + "    \"uniq_id\":\"C\",\n" //
                + "    \"avty_t\":\"~E\",\n" //
                + "    \"pl_avail\":\"F\",\n" //
                + "    \"pl_not_avail\":\"G\",\n" //
                + "    \"device\":{\n" //
                + "        \"ids\":[\"H\"],\n" //
                + "        \"cns\":[{\n" //
                + "           \"type\": \"I1\",\n" //
                + "           \"identifier\": \"I2\"\n" //
                + "        }],\n" //
                + "        \"name\":\"J\",\n" //
                + "        \"mdl\":\"K\",\n" //
                + "        \"sw\":\"L\",\n" //
                + "        \"mf\":\"M\"\n" //
                + "    },\n" //
                + "    \"~\":\"D/\"\n" //
                + "}";

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
        assertThat(config.device.identifiers, contains("H"));
        assertThat(config.device.connections, is(notNullValue()));
        assertThat(config.device.connections.get(0).type, is("I1"));
        assertThat(config.device.connections.get(0).identifier, is("I2"));
        assertThat(config.device.name, is("J"));
        assertThat(config.device.model, is("K"));
        assertThat(config.device.sw_version, is("L"));
        assertThat(config.device.manufacturer, is("M"));
    }

    @Test
    public void testTildeSubstritution() {
        String json = "{\n"//
                + "    \"name\":\"A\",\n" //
                + "    \"icon\":\"2\",\n" //
                + "    \"qos\":1,\n" //
                + "    \"retain\":true,\n" //
                + "    \"val_tpl\":\"B\",\n" //
                + "    \"uniq_id\":\"C\",\n" //
                + "    \"avty_t\":\"~E\",\n" //
                + "    \"pl_avail\":\"F\",\n" //
                + "    \"pl_not_avail\":\"G\",\n" //
                + "    \"optimistic\":true,\n" //
                + "    \"state_topic\":\"O/~\",\n" //
                + "    \"command_topic\":\"P~Q\",\n" //
                + "    \"device\":{\n" //
                + "        \"ids\":[\"H\"],\n" //
                + "        \"cns\":[{\n" //
                + "           \"type\": \"I1\",\n" //
                + "           \"identifier\": \"I2\"\n" //
                + "        }],\n" //
                + "        \"name\":\"J\",\n" //
                + "        \"mdl\":\"K\",\n" //
                + "        \"sw\":\"L\",\n" //
                + "        \"mf\":\"M\"\n" //
                + "    },\n" //
                + "    \"~\":\"D/\"\n" //
                + "}";

        ComponentSwitch.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentSwitch.ChannelConfiguration.class);

        assertThat(config.availability_topic, is("D/E"));
        assertThat(config.state_topic, is("O/D/"));
        assertThat(config.command_topic, is("P~Q"));

    }

    @Test
    public void testSampleFanConfig() {
        String json = "{\n" //
                + "    \"name\": \"Bedroom Fan\",\n" //
                + "    \"state_topic\": \"bedroom_fan/on/state\",\n" //
                + "    \"command_topic\": \"bedroom_fan/on/set\",\n" //
                + "    \"oscillation_state_topic\": \"bedroom_fan/oscillation/state\",\n" //
                + "    \"oscillation_command_topic\": \"bedroom_fan/oscillation/set\",\n" //
                + "    \"speed_state_topic\": \"bedroom_fan/speed/state\",\n" //
                + "    \"speed_command_topic\": \"bedroom_fan/speed/set\",\n" //
                + "    \"qos\": 0,\n" //
                + "    \"payload_on\": \"true\",\n" //
                + "    \"payload_off\": \"false\",\n" //
                + "    \"payload_oscillation_on\": \"true\",\n" //
                + "    \"payload_oscillation_off\": \"false\",\n"//
                + "    \"payload_low_speed\": \"low\",\n" //
                + "    \"payload_medium_speed\": \"medium\",\n" //
                + "    \"payload_high_speed\": \"high\",\n" //
                + "    \"speeds\": [\n" //
                + "        \"low\", \"medium\", \"high\"\n" //
                + "    ]\n" //
                + "}";

        ComponentFan.ChannelConfiguration config = BaseChannelConfiguration.fromString(json, gson,
                ComponentFan.ChannelConfiguration.class);
        assertThat(config.name, is("Bedroom Fan"));

    }
}
