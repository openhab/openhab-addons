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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Sensor}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings("ConstantConditions")
public class SensorTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "sensor/0x0000000000000000_sensor_zigbee2mqtt";

    @Test
    public void test() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                "{ " +
                        "  \"availability\": [ " +
                        "    { " +
                        "      \"topic\": \"zigbee2mqtt/bridge/state\" " +
                        "    } " +
                        "  ], " +
                        "  \"device\": { " +
                        "    \"identifiers\": [ " +
                        "      \"zigbee2mqtt_0x0000000000000000\" " +
                        "    ], " +
                        "    \"manufacturer\": \"Sensors inc\", " +
                        "    \"model\": \"Sensor\", " +
                        "    \"name\": \"Sensor\", " +
                        "    \"sw_version\": \"Zigbee2MQTT 1.18.2\" " +
                        "  }, " +
                        "  \"name\": \"sensor1\", " +
                        "  \"expire_after\": \"1\", " +
                        "  \"force_update\": \"true\", " +
                        "  \"unit_of_measurement\": \"W\", " +
                        "  \"state_topic\": \"zigbee2mqtt/sensor/state\", " +
                        "  \"unique_id\": \"sn1\" " +
                        "}");
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("sensor1"));
        assertThat(component.getGroupUID().getId(), is("sn1"));

        assertChannel(component, Sensor.sensorChannelID, "zigbee2mqtt/sensor/state", "", "sensor1", NumberValue.class);

        publishMessage("zigbee2mqtt/sensor/state", "10");
        assertState(component, Sensor.sensorChannelID, DecimalType.valueOf("10"));
        publishMessage("zigbee2mqtt/sensor/state", "20");
        assertState(component, Sensor.sensorChannelID, DecimalType.valueOf("20"));
        assertThat(component.getChannel(Sensor.sensorChannelID).getState().getCache().createStateDescription(true)
                .build().getPattern(), is("%s W"));

        waitForAssert(() -> assertState(component, Sensor.sensorChannelID, UnDefType.UNDEF), 10000, 200);
    }

    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
