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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * Tests for {@link Light} conforming to the JSON schema
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class JSONSchemaLightTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "light/0x0000000000000000_light_zigbee2mqtt";

    @Test
    public void testRgbNewStyle() throws InterruptedException {
        // @formatter:off
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
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
                    "manufacturer": "Lights inc", \
                    "model": "light v1", \
                    "name": "Light", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "light", \
                  "schema": "json", \
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state", \
                  "brightness": true, \
                  "supported_color_modes": ["rgb"]\
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.COLOR_CHANNEL_ID, "", "dummy", "light", ColorValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/light/state", "{ \"state\": \"ON\" }");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.WHITE);
        publishMessage("zigbee2mqtt/light/state", "{ \"color\": {\"r\": 10, \"g\": 20, \"b\": 30 } }");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.fromRGB(10, 20, 30));
        publishMessage("zigbee2mqtt/light/state", "{ \"brightness\": 255 }");
        assertState(component, Light.COLOR_CHANNEL_ID, new HSBType("210,67,100"));

        sendCommand(component, Light.COLOR_CHANNEL_ID, HSBType.BLUE);
        assertPublished("zigbee2mqtt/light/set/state",
                "{\"state\":\"ON\",\"brightness\":255,\"color\":{\"r\":0,\"g\":0,\"b\":255}}");

        // OnOff commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"OFF\"}");

        sendCommand(component, Light.COLOR_CHANNEL_ID, OnOffType.ON);
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"ON\"}");

        sendCommand(component, Light.COLOR_CHANNEL_ID, new PercentType(50));
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"ON\",\"brightness\":127}");
    }

    @Test
    public void testBrightnessAndOnOff() throws InterruptedException {
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "name": "light",
                  "schema": "json",
                  "state_topic": "zigbee2mqtt/light/state",
                  "command_topic": "zigbee2mqtt/light/set/state",
                  "brightness": true
                }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.BRIGHTNESS_CHANNEL_ID, "", "dummy", "light", PercentageValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/light/state", "{ \"state\": \"ON\", \"brightness\": 128 }");
        assertState(component, Light.BRIGHTNESS_CHANNEL_ID,
                new PercentType(new BigDecimal(128 * 100).divide(new BigDecimal(255), MathContext.DECIMAL128)));

        publishMessage("zigbee2mqtt/light/state", "{ \"state\": \"OFF\", \"brightness\": 128 }");
        assertState(component, Light.BRIGHTNESS_CHANNEL_ID, PercentType.ZERO);

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, PercentType.HUNDRED);
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"ON\",\"brightness\":255}");

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"OFF\"}");
    }

    @Test
    public void testOnOffOnly() throws InterruptedException {
        // @formatter:off
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "name": "light", \
                  "schema": "json", \
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state"\
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.SWITCH_CHANNEL_ID, "", "dummy", "light", OnOffValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/light/state", "{ \"state\": \"ON\" }");
        assertState(component, Light.SWITCH_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/light/state", "{ \"state\": \"OFF\" }");
        assertState(component, Light.SWITCH_CHANNEL_ID, OnOffType.OFF);

        sendCommand(component, Light.SWITCH_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"OFF\"}");
        sendCommand(component, Light.SWITCH_CHANNEL_ID, OnOffType.ON);
        assertPublished("zigbee2mqtt/light/set/state", "{\"state\":\"ON\"}");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
