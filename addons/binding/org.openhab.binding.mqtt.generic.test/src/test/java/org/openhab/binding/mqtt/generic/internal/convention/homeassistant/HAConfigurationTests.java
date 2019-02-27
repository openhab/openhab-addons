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
package org.openhab.binding.mqtt.generic.internal.convention.homeassistant;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.collection.IsIterableContainingInOrder.contains;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.google.gson.Gson;

public class HAConfigurationTests {

    @Test
    public void testTasmotaSwitch() {
        String json = "{\n"//
                + "    \"name\":\"Licht Dachterasse\",\n" //
                + "    \"cmd_t\":\"~cmnd/POWER\",\n" //
                + "    \"stat_t\":\"~tele/STATE\",\n" //
                + "    \"val_tpl\":\"{{value_json.POWER}}\",\n" //
                + "    \"pl_off\":\"OFF\",\n" //
                + "    \"pl_on\":\"ON\",\n" //
                + "    \"avty_t\":\"~tele/LWT\",\n" //
                + "    \"pl_avail\":\"Online\",\n" //
                + "    \"pl_not_avail\":\"Offline\",\n" //
                + "    \"uniq_id\":\"86C9AC_RL_1\",\n" //
                + "    \"device\":{\n" //
                + "        \"identifiers\":[\"86C9AC\"],\n" //
                + "        \"name\":\"Licht Dachterasse\",\n" //
                + "        \"model\":\"Sonoff TH\",\n" //
                + "        \"sw_version\":\"6.4.1(release-sensors)\",\n" //
                + "        \"manufacturer\":\"Tasmota\"\n" //
                + "    },\n" //
                + "    \"~\":\"sonoff-2476/\"\n" //
                + "}";

        HAConfiguration config = HAConfiguration.fromString(json, new Gson());

        assertThat(config.name, is("Licht Dachterasse"));
        assertThat(config.device, is(notNullValue()));
        assertThat(config.device.identifiers, contains("86C9AC"));
        assertThat(config.device.name, is("Licht Dachterasse"));
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

        HAConfiguration config = HAConfiguration.fromString(json, new Gson(), ComponentFan.Config.class);
        assertThat(config.name, is("Bedroom Fan"));

    }
}
