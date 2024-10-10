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
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Sensor}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class SensorTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "sensor/0x0000000000000000_sensor_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability_topic": "zigbee2mqtt/bridge/state", \
                  "availability_template": "{{value_json.state}}", \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Sensors inc", \
                    "model": "Sensor", \
                    "name": "Sensor", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "sensor1", \
                  "expire_after": "1", \
                  "force_update": "true", \
                  "unit_of_measurement": "W", \
                  "state_topic": "zigbee2mqtt/sensor/state", \
                  "unique_id": "sn1" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("sensor1"));
        assertThat(component.getComponentId(), is("sn1"));

        assertChannel(component, Sensor.SENSOR_CHANNEL_ID, "zigbee2mqtt/sensor/state", "", "sensor1",
                NumberValue.class);

        publishMessage("zigbee2mqtt/bridge/state", "{ \"state\": \"online\" }");
        assertThat(haThing.getStatus(), is(ThingStatus.ONLINE));
        publishMessage("zigbee2mqtt/sensor/state", "10");
        assertState(component, Sensor.SENSOR_CHANNEL_ID, new QuantityType<>(10, Units.WATT));
        publishMessage("zigbee2mqtt/sensor/state", "20");
        assertState(component, Sensor.SENSOR_CHANNEL_ID, new QuantityType<>(20, Units.WATT));
        assertThat(component.getChannel(Sensor.SENSOR_CHANNEL_ID).getState().getCache().createStateDescription(true)
                .build().getPattern(), is("%.0f %unit%"));

        waitForAssert(() -> assertState(component, Sensor.SENSOR_CHANNEL_ID, UnDefType.UNDEF), 5000, 200);

        publishMessage("zigbee2mqtt/bridge/state", "{ \"state\": \"offline\" }");
        assertThat(haThing.getStatus(), is(ThingStatus.OFFLINE));
    }

    @Test
    public void testMeasurementStateClass() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Sensors inc", \
                    "model": "Sensor", \
                    "name": "Sensor", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "sensor1", \
                  "expire_after": "1", \
                  "force_update": "true", \
                  "state_class": "measurement", \
                  "state_topic": "zigbee2mqtt/sensor/state", \
                  "unique_id": "sn1" \
                }\
                """);
        // @formatter:on

        assertChannel(component, Sensor.SENSOR_CHANNEL_ID, "zigbee2mqtt/sensor/state", "", "sensor1",
                NumberValue.class);
    }

    @Test
    public void testNonNumericSensor() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Sensors inc", \
                    "model": "Sensor", \
                    "name": "Sensor", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "sensor1", \
                  "expire_after": "1", \
                  "force_update": "true", \
                  "state_topic": "zigbee2mqtt/sensor/state", \
                  "unique_id": "sn1" \
                }\
                """);
        // @formatter:on

        assertChannel(component, Sensor.SENSOR_CHANNEL_ID, "zigbee2mqtt/sensor/state", "", "sensor1", TextValue.class);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
