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
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Vacuum}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class VacuumTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "vacuum/rockrobo_vacuum";

    @SuppressWarnings("null")
    @Test
    public void testRoborockValetudo() {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {\
                "name":"Rockrobo",\
                "unique_id":"rockrobo_vacuum",\
                "schema":"state",\
                "device":{\
                   "manufacturer":"Roborock",\
                   "model":"v1",\
                   "name":"rockrobo",\
                   "identifiers":["rockrobo"],\
                   "sw_version":"0.9.9"\
                },\
                "supported_features":["start","pause","stop","return_home","battery","status",\
                   "locate","clean_spot","fan_speed","send_command"],\
                "command_topic":"valetudo/rockrobo/command",\
                "state_topic":"valetudo/rockrobo/state",\
                "set_fan_speed_topic":"valetudo/rockrobo/set_fan_speed",\
                "fan_speed_list":["min","medium","high","max","mop"],\
                "send_command_topic":"valetudo/rockrobo/custom_command",\
                "json_attributes_topic":"valetudo/rockrobo/attributes"\
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(6)); // command, state, fan speed, send command, battery, json attrs
        assertThat(component.getName(), is("Rockrobo"));
        assertChannel(component, Vacuum.COMMAND_CH_ID, "", "valetudo/rockrobo/command", "Rockrobo", TextValue.class);
        assertChannel(component, Vacuum.STATE_CH_ID, "valetudo/rockrobo/state", "", "Rockrobo", TextValue.class);
        assertChannel(component, Vacuum.FAN_SPEED_CH_ID, "valetudo/rockrobo/state", "valetudo/rockrobo/set_fan_speed",
                "Rockrobo", TextValue.class);
        assertChannel(component, Vacuum.CUSTOM_COMMAND_CH_ID, "", "valetudo/rockrobo/custom_command", "Rockrobo",
                TextValue.class);
        assertChannel(component, Vacuum.BATTERY_LEVEL_CH_ID, "valetudo/rockrobo/state", "", "Rockrobo",
                PercentageValue.class);
        assertChannel(component, Vacuum.JSON_ATTRIBUTES_CH_ID, "valetudo/rockrobo/attributes", "", "Rockrobo",
                TextValue.class);

        assertState(component, Vacuum.STATE_CH_ID, UnDefType.UNDEF);
        assertState(component, Vacuum.FAN_SPEED_CH_ID, UnDefType.UNDEF);
        assertState(component, Vacuum.BATTERY_LEVEL_CH_ID, UnDefType.UNDEF);
        assertState(component, Vacuum.JSON_ATTRIBUTES_CH_ID, UnDefType.UNDEF);

        // @formatter:off
        String jsonValue;
        publishMessage("valetudo/rockrobo/attributes", jsonValue = """
                {\
                "mainBrush":"245.1",\
                "sideBrush":"145.1",\
                "filter":"95.1",\
                "sensor":"0.0",\
                "currentCleanTime":"52.0",\
                "currentCleanArea":"46.7",\
                "cleanTime":"54.9",\
                "cleanArea":"3280.9",\
                "cleanCount":84,\
                "last_run_stats":{\
                   "startTime":1633257319000,\
                   "endTime":1633260439000,\
                   "duration":3120,\
                   "area":"46.7",\
                   "errorCode":0,\
                   "errorDescription":"No error",\
                   "finishedFlag":true\
                },\
                "last_bin_out":2147483647000,\
                "state":"docked",\
                "valetudo_state":{\
                   "id":8,\
                   "name":"Charging"\
                },\
                "last_bin_full":0\
                }\
                """);
        // @formatter:on

        // @formatter:off
        publishMessage("valetudo/rockrobo/state", """
                {\
                "state":"docked",\
                "battery_level":100,\
                "fan_speed":"max"\
                }\
                """);
        // @formatter:on

        assertState(component, Vacuum.STATE_CH_ID, new StringType(Vacuum.STATE_DOCKED));
        assertState(component, Vacuum.FAN_SPEED_CH_ID, new StringType("max"));
        assertState(component, Vacuum.BATTERY_LEVEL_CH_ID, new PercentType(100));
        assertState(component, Vacuum.JSON_ATTRIBUTES_CH_ID, new StringType(jsonValue));

        component.getChannel(Vacuum.COMMAND_CH_ID).getState().publishValue(new StringType("start"));
        assertPublished("valetudo/rockrobo/command", "start");

        // @formatter:off
        publishMessage("valetudo/rockrobo/state", """
                {\
                "state":"cleaning",\
                "battery_level":99,\
                "fan_speed":"max"\
                }\
                """);
        // @formatter:on

        assertState(component, Vacuum.STATE_CH_ID, new StringType(Vacuum.STATE_CLEANING));
        assertState(component, Vacuum.FAN_SPEED_CH_ID, new StringType("max"));
        assertState(component, Vacuum.BATTERY_LEVEL_CH_ID, new PercentType(99));
        assertState(component, Vacuum.JSON_ATTRIBUTES_CH_ID, new StringType(jsonValue));

        component.getChannel(Vacuum.FAN_SPEED_CH_ID).getState().publishValue(new StringType("medium"));
        assertPublished("valetudo/rockrobo/set_fan_speed", "medium");

        // @formatter:off
        publishMessage("valetudo/rockrobo/state", """
                {\
                "state":"returning",\
                "battery_level":80,\
                "fan_speed":"medium"\
                }\
                """);
        // @formatter:on

        assertState(component, Vacuum.STATE_CH_ID, new StringType(Vacuum.STATE_RETURNING));
        assertState(component, Vacuum.FAN_SPEED_CH_ID, new StringType("medium"));
        assertState(component, Vacuum.BATTERY_LEVEL_CH_ID, new PercentType(80));
        assertState(component, Vacuum.JSON_ATTRIBUTES_CH_ID, new StringType(jsonValue));
    }

    @SuppressWarnings("null")
    @Test
    public void testLegacySchema() {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {\
                "name":"Rockrobo",\
                "unique_id":"rockrobo_vacuum",\
                "device":{\
                   "manufacturer":"Roborock",\
                   "model":"v1",\
                   "name":"rockrobo",\
                   "identifiers":["rockrobo"],\
                   "sw_version":"0.9.9"\
                },\
                "supported_features":["turn_on", "turn_off","pause","stop","return_home","battery","status",\
                   "locate","clean_spot","fan_speed","send_command"],\
                "command_topic":"vacuum/command",\
                "battery_level_topic":"vacuum/state",\
                "battery_level_template":"{{ value_json.battery_level }}",\
                "charging_topic":"vacuum/state",\
                "charging_template":"{{ value_json.charging }}",\
                "cleaning_topic":"vacuum/state",\
                "cleaning_template":"{{ value_json.cleaning }}",\
                "docked_topic":"vacuum/state",\
                "docked_template":"{{ value_json.docked }}",\
                "error_topic":"vacuum/state",\
                "error_template":"{{ value_json.error }}",\
                "fan_speed_topic":"vacuum/state",\
                "set_fan_speed_topic":"vacuum/set_fan_speed",\
                "fan_speed_template":"{{ value_json.fan_speed }}",\
                "fan_speed_list":["min","medium","high","max"],\
                "send_command_topic":"vacuum/send_command"\
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(8)); // command, battery, charging, cleaning, docked, error,
        // fan speed, send command
        assertThat(component.getName(), is("Rockrobo"));
        assertChannel(component, Vacuum.COMMAND_CH_ID, "", "vacuum/command", "Rockrobo", TextValue.class);
        assertChannel(component, Vacuum.BATTERY_LEVEL_CH_ID, "vacuum/state", "", "Rockrobo", PercentageValue.class);
        assertChannel(component, Vacuum.CHARGING_CH_ID, "vacuum/state", "", "Rockrobo", OnOffValue.class);
        assertChannel(component, Vacuum.CLEANING_CH_ID, "vacuum/state", "", "Rockrobo", OnOffValue.class);
        assertChannel(component, Vacuum.DOCKED_CH_ID, "vacuum/state", "", "Rockrobo", OnOffValue.class);
        assertChannel(component, Vacuum.ERROR_CH_ID, "vacuum/state", "", "Rockrobo", TextValue.class);
        assertChannel(component, Vacuum.FAN_SPEED_CH_ID, "vacuum/state", "vacuum/set_fan_speed", "Rockrobo",
                TextValue.class);
        assertChannel(component, Vacuum.CUSTOM_COMMAND_CH_ID, "", "vacuum/send_command", "Rockrobo", TextValue.class);

        // @formatter:off
        publishMessage("vacuum/state", """
                {\
                "battery_level": 61,\
                "docked": true,\
                "cleaning": false,\
                "charging": true,\
                "fan_speed": "off",\
                "error": "Error message"\
                }\
                """);
        // @formatter:on

        assertState(component, Vacuum.BATTERY_LEVEL_CH_ID, new PercentType(61));
        assertState(component, Vacuum.DOCKED_CH_ID, OnOffType.ON);
        assertState(component, Vacuum.CLEANING_CH_ID, OnOffType.OFF);
        assertState(component, Vacuum.CHARGING_CH_ID, OnOffType.ON);
        assertState(component, Vacuum.FAN_SPEED_CH_ID, new StringType("off"));
        assertState(component, Vacuum.ERROR_CH_ID, new StringType("Error message"));

        component.getChannel(Vacuum.COMMAND_CH_ID).getState().publishValue(new StringType("turn_on"));
        assertPublished("vacuum/command", "turn_on");

        // @formatter:off
        publishMessage("vacuum/state", """
                {\
                "battery_level": 55,\
                "docked": false,\
                "cleaning": true,\
                "charging": false,\
                "fan_speed": "medium",\
                "error": ""\
                }\
                """);
        // @formatter:on

        assertState(component, Vacuum.BATTERY_LEVEL_CH_ID, new PercentType(55));
        assertState(component, Vacuum.DOCKED_CH_ID, OnOffType.OFF);
        assertState(component, Vacuum.CLEANING_CH_ID, OnOffType.ON);
        assertState(component, Vacuum.CHARGING_CH_ID, OnOffType.OFF);
        assertState(component, Vacuum.FAN_SPEED_CH_ID, new StringType("medium"));
        assertState(component, Vacuum.ERROR_CH_ID, new StringType(""));

        component.getChannel(Vacuum.FAN_SPEED_CH_ID).getState().publishValue(new StringType("high"));
        assertPublished("vacuum/set_fan_speed", "high");

        component.getChannel(Vacuum.CUSTOM_COMMAND_CH_ID).getState().publishValue(new StringType("custom_command"));
        assertPublished("vacuum/send_command", "custom_command");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
