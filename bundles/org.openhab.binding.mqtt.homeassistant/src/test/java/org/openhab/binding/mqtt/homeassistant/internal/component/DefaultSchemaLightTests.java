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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentChannel;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;

/**
 * Tests for {@link Light} confirming to the default schema
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class DefaultSchemaLightTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "light/0x0000000000000000_light_zigbee2mqtt";

    @Test
    public void testRgb() throws InterruptedException {
        // @formatter:off
        var component = (Light) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
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
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state", \
                  "state_value_template": "{{ value_json.power }}", \
                  "payload_on": "ON_", \
                  "payload_off": "OFF_", \
                  "rgb_state_topic": "zigbee2mqtt/light/rgb", \
                  "rgb_command_topic": "zigbee2mqtt/light/set/rgb", \
                  "rgb_value_template": "{{ value_json.rgb }}", \
                  "brightness_state_topic": "zigbee2mqtt/light/brightness", \
                  "brightness_command_topic": "zigbee2mqtt/light/set/brightness", \
                  "brightness_value_template": "{{ value_json.br }}" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.COLOR_CHANNEL_ID, "", "dummy", "Color", ColorValue.class);

        @Nullable
        ComponentChannel onOffChannel = component.onOffChannel;
        assertThat(onOffChannel, is(notNullValue()));
        if (onOffChannel != null) {
            assertChannel(onOffChannel, "zigbee2mqtt/light/state", "zigbee2mqtt/light/set/state", "On/Off State",
                    OnOffValue.class);
        }
        @Nullable
        ComponentChannel brightnessChannel = component.brightnessChannel;
        assertThat(brightnessChannel, is(notNullValue()));
        if (brightnessChannel != null) {
            assertChannel(brightnessChannel, "zigbee2mqtt/light/brightness", "zigbee2mqtt/light/set/brightness",
                    "Brightness", PercentageValue.class);
        }

        publishMessage("zigbee2mqtt/light/state", "{\"power\": \"ON_\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.WHITE);
        publishMessage("zigbee2mqtt/light/rgb", "{\"rgb\": \"10,20,30\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.fromRGB(10, 20, 30));
        publishMessage("zigbee2mqtt/light/rgb", "{\"rgb\": \"255,255,255\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.WHITE);

        sendCommand(component, Light.COLOR_CHANNEL_ID, HSBType.BLUE);
        assertPublished("zigbee2mqtt/light/set/rgb", "0,0,255");

        // Brightness commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, new PercentType(50));
        assertPublished("zigbee2mqtt/light/set/brightness", "128");

        // OnOff commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "OFF_");
    }

    @Test
    public void testRgbWithoutBrightness() throws InterruptedException {
        // @formatter:off
        var component = (Light) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "name": "light", \
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state", \
                  "state_value_template": "{{ value_json.power }}", \
                  "payload_on": "ON_", \
                  "payload_off": "OFF_", \
                  "rgb_state_topic": "zigbee2mqtt/light/rgb", \
                  "rgb_command_topic": "zigbee2mqtt/light/set/rgb", \
                  "rgb_value_template": "{{ value_json.rgb }}"\
                }\
                """);
        // @formatter:on

        publishMessage("zigbee2mqtt/light/rgb", "{\"rgb\": \"255,255,255\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.WHITE);

        // Brightness commands should route to the correct topic, converted to RGB
        sendCommand(component, Light.COLOR_CHANNEL_ID, new PercentType(50));
        assertPublished("zigbee2mqtt/light/set/rgb", "128,128,128");

        // OnOff commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "OFF_");
    }

    @Test
    public void testHsb() throws InterruptedException {
        // @formatter:off
        var component = (Light) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "name": "light", \
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state", \
                  "state_value_template": "{{ value_json.power }}", \
                  "payload_on": "ON_", \
                  "payload_off": "OFF_", \
                  "hs_state_topic": "zigbee2mqtt/light/hs", \
                  "hs_command_topic": "zigbee2mqtt/light/set/hs", \
                  "hs_value_template": "{{ value_json.hs }}", \
                  "brightness_state_topic": "zigbee2mqtt/light/brightness", \
                  "brightness_command_topic": "zigbee2mqtt/light/set/brightness", \
                  "brightness_value_template": "{{ value_json.br }}" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.COLOR_CHANNEL_ID, "", "dummy", "Color", ColorValue.class);

        @Nullable
        ComponentChannel onOffChannel = component.onOffChannel;
        assertThat(onOffChannel, is(notNullValue()));
        if (onOffChannel != null) {
            assertChannel(onOffChannel, "zigbee2mqtt/light/state", "zigbee2mqtt/light/set/state", "On/Off State",
                    OnOffValue.class);
        }
        @Nullable
        ComponentChannel brightnessChannel = component.brightnessChannel;
        assertThat(brightnessChannel, is(notNullValue()));
        if (brightnessChannel != null) {
            assertChannel(brightnessChannel, "zigbee2mqtt/light/brightness", "zigbee2mqtt/light/set/brightness",
                    "Brightness", PercentageValue.class);
        }

        publishMessage("zigbee2mqtt/light/hs", "{\"hs\": \"180,50\"}");
        publishMessage("zigbee2mqtt/light/brightness", "{\"br\": \"128\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, new HSBType(new DecimalType(180), new PercentType(50),
                new PercentType(new BigDecimal(128 * 100).divide(new BigDecimal(255), MathContext.DECIMAL128))));

        sendCommand(component, Light.COLOR_CHANNEL_ID, HSBType.BLUE);
        assertPublished("zigbee2mqtt/light/set/brightness", "255");
        assertPublished("zigbee2mqtt/light/set/hs", "240,100");

        // Brightness commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, new PercentType(50));
        assertPublished("zigbee2mqtt/light/set/brightness", "128");

        // OnOff commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "OFF_");
    }

    @Test
    public void testBrightnessAndOnOff() throws InterruptedException {
        // @formatter:off
        var component = (Light) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "name": "light", \
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state", \
                  "state_value_template": "{{ value_json.power }}", \
                  "brightness_state_topic": "zigbee2mqtt/light/brightness", \
                  "brightness_command_topic": "zigbee2mqtt/light/set/brightness", \
                  "payload_on": "ON_", \
                  "payload_off": "OFF_" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.BRIGHTNESS_CHANNEL_ID, "zigbee2mqtt/light/brightness",
                "zigbee2mqtt/light/set/brightness", "Brightness", PercentageValue.class);
        @Nullable
        ComponentChannel onOffChannel = component.onOffChannel;
        assertThat(onOffChannel, is(notNullValue()));
        if (onOffChannel != null) {
            assertChannel(onOffChannel, "zigbee2mqtt/light/state", "zigbee2mqtt/light/set/state", "On/Off State",
                    OnOffValue.class);
        }

        publishMessage("zigbee2mqtt/light/brightness", "128");
        assertState(component, Light.BRIGHTNESS_CHANNEL_ID,
                new PercentType(new BigDecimal(128 * 100).divide(new BigDecimal(255), MathContext.DECIMAL128)));
        publishMessage("zigbee2mqtt/light/brightness", "64");
        assertState(component, Light.BRIGHTNESS_CHANNEL_ID,
                new PercentType(new BigDecimal(64 * 100).divide(new BigDecimal(255), MathContext.DECIMAL128)));

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "OFF_");

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, OnOffType.ON);
        assertPublished("zigbee2mqtt/light/set/state", "ON_");
    }

    @Test
    public void testOnOffOnly() throws InterruptedException {
        // @formatter:off
        var component = (Light) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "name": "light", \
                  "state_topic": "zigbee2mqtt/light/state", \
                  "command_topic": "zigbee2mqtt/light/set/state", \
                  "state_value_template": "{{ value_json.power }}", \
                  "payload_on": "ON_", \
                  "payload_off": "OFF_" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.ON_OFF_CHANNEL_ID, "zigbee2mqtt/light/state", "zigbee2mqtt/light/set/state",
                "On/Off State", OnOffValue.class);
        assertThat(component.brightnessChannel, is(nullValue()));

        publishMessage("zigbee2mqtt/light/state", "{\"power\": \"ON_\"}");
        assertState(component, Light.ON_OFF_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/light/state", "{\"power\": \"OFF_\"}");
        assertState(component, Light.ON_OFF_CHANNEL_ID, OnOffType.OFF);

        sendCommand(component, Light.ON_OFF_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "OFF_");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
