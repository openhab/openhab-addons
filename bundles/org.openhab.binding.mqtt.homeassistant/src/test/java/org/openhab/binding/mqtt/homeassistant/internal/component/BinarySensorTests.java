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
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link BinarySensor}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class BinarySensorTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "binary_sensor/0x0000000000000000_binary_sensor_zigbee2mqtt";

    @Test
    public void test() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Sensors inc", \
                    "model": "On Off Sensor", \
                    "name": "OnOffSensor", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "onoffsensor", \
                  "force_update": "true", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "state_topic": "zigbee2mqtt/sensor/state", \
                  "unique_id": "sn1", \
                  "value_template": "{{ value_json.state }}" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("onoffsensor"));
        assertThat(component.getComponentId(), is("0x0000000000000000_binary_sensor_zigbee2mqtt"));

        assertChannel(component, BinarySensor.SENSOR_CHANNEL_ID, "zigbee2mqtt/sensor/state", "", "onoffsensor",
                OnOffValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/sensor/state", "{ \"state\": \"ON_\" }");
        assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/sensor/state", "{ \"state\": \"ON_\" }");
        assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/sensor/state", "{ \"state\": \"OFF_\" }");
        assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/sensor/state", "{ \"state\": \"ON_\" }");
        assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.ON);
    }

    @Test
    public void offDelayTest() {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Sensors inc", \
                    "model": "On Off Sensor", \
                    "name": "OnOffSensor", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "onoffsensor", \
                  "force_update": "true", \
                  "off_delay": "1", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "state_topic": "zigbee2mqtt/sensor/state", \
                  "unique_id": "sn1", \
                  "value_template": "{{ value_json.state }}" \
                }\
                """);
        // @formatter:on

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/sensor/state", "{ \"state\": \"ON_\" }");
        assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.ON);

        waitForAssert(() -> assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.OFF), 10000, 200);
    }

    @Test
    public void expireAfterTest() {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Sensors inc", \
                    "model": "On Off Sensor", \
                    "name": "OnOffSensor", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "onoffsensor", \
                  "expire_after": "1", \
                  "force_update": "true", \
                  "payload_off": "OFF_", \
                  "payload_on": "ON_", \
                  "state_topic": "zigbee2mqtt/sensor/state", \
                  "unique_id": "sn1", \
                  "value_template": "{{ value_json.state }}" \
                }\
                """);
        // @formatter:on

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/sensor/state", "{ \"state\": \"OFF_\" }");
        assertState(component, BinarySensor.SENSOR_CHANNEL_ID, OnOffType.OFF);

        waitForAssert(() -> assertState(component, BinarySensor.SENSOR_CHANNEL_ID, UnDefType.UNDEF), 10000, 200);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
