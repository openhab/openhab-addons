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
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.StringType;

/**
 * Tests for {@link AlarmControlPanel}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class AlarmControlPanelTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "alarm_control_panel/0x0000000000000000_alarm_control_panel_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void testAlarmControlPanel() {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "code": "12345", \
                  "command_topic": "zigbee2mqtt/alarm/set/state", \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "BestAlarmEver", \
                    "model": "Heavy duty super duper alarm", \
                    "name": "Alarm", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "alarm", \
                  "payload_arm_away": "ARM_AWAY_", \
                  "payload_arm_home": "ARM_HOME_", \
                  "payload_arm_night": "ARM_NIGHT_", \
                  "payload_arm_custom_bypass": "ARM_CUSTOM_BYPASS_", \
                  "payload_disarm": "DISARM_", \
                  "state_topic": "zigbee2mqtt/alarm/state" \
                } \
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(4));
        assertThat(component.getName(), is("alarm"));

        assertChannel(component, AlarmControlPanel.STATE_CHANNEL_ID, "zigbee2mqtt/alarm/state", "", "alarm",
                TextValue.class);
        assertChannel(component, AlarmControlPanel.SWITCH_DISARM_CHANNEL_ID, "", "zigbee2mqtt/alarm/set/state", "alarm",
                TextValue.class);
        assertChannel(component, AlarmControlPanel.SWITCH_ARM_AWAY_CHANNEL_ID, "", "zigbee2mqtt/alarm/set/state",
                "alarm", TextValue.class);
        assertChannel(component, AlarmControlPanel.SWITCH_ARM_HOME_CHANNEL_ID, "", "zigbee2mqtt/alarm/set/state",
                "alarm", TextValue.class);

        publishMessage("zigbee2mqtt/alarm/state", "armed_home");
        assertState(component, AlarmControlPanel.STATE_CHANNEL_ID, new StringType("armed_home"));
        publishMessage("zigbee2mqtt/alarm/state", "armed_away");
        assertState(component, AlarmControlPanel.STATE_CHANNEL_ID, new StringType("armed_away"));

        component.getChannel(AlarmControlPanel.SWITCH_DISARM_CHANNEL_ID).getState()
                .publishValue(new StringType("DISARM_"));
        assertPublished("zigbee2mqtt/alarm/set/state", "DISARM_");
        component.getChannel(AlarmControlPanel.SWITCH_ARM_AWAY_CHANNEL_ID).getState()
                .publishValue(new StringType("ARM_AWAY_"));
        assertPublished("zigbee2mqtt/alarm/set/state", "ARM_AWAY_");
        component.getChannel(AlarmControlPanel.SWITCH_ARM_HOME_CHANNEL_ID).getState()
                .publishValue(new StringType("ARM_HOME_"));
        assertPublished("zigbee2mqtt/alarm/set/state", "ARM_HOME_");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
