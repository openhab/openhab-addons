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
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Light} conforming to the template schema
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class TemplateSchemaLightTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "light/0x0000000000000000_light_zigbee2mqtt";

    @Test
    public void testRgb() throws InterruptedException {
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                    "availability": [
                    {
                        "topic": "zigbee2mqtt/bridge/state"
                    }
                    ],
                    "device": {
                    "identifiers": [
                        "zigbee2mqtt_0x0000000000000000"
                    ],
                    "manufacturer": "Lights inc",
                    "model": "light v1",
                    "name": "Light",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                    },
                    "name": "light",
                    "schema": "template",
                    "state_topic": "zigbee2mqtt/light/state",
                    "command_topic": "zigbee2mqtt/light/set/state",
                    "command_on_template": "{{state}},{{red}},{{green}},{{blue}}",
                    "command_off_template": "off",
                    "state_template": "{{value_json.state}}",
                    "red_template": "{{value_json.r}}",
                    "green_template": "{{value_json.g}}",
                    "blue_template": "{{value_json.b}}",
                    "brightness_template": "{{value_json.brightness}}"
                }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.COLOR_CHANNEL_ID, "", "dummy", "light", ColorValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/light/state", """
                { "state": "on", "r": 255, "g": 255, "b": 255, "brightness": 255 }
                """);
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.WHITE);

        sendCommand(component, Light.COLOR_CHANNEL_ID, HSBType.BLUE);
        assertPublished("zigbee2mqtt/light/set/state", "on,0,0,255");

        // OnOff commands should route to the correct topic
        sendCommand(component, Light.COLOR_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "off");
    }

    @Test
    public void testBrightnessAndOnOff() throws InterruptedException {
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                    "name": "light",
                    "schema": "template",
                    "state_topic": "zigbee2mqtt/light/state",
                    "command_topic": "zigbee2mqtt/light/set/state",
                    "command_on_template": "{{state}},{{brightness}}",
                    "command_off_template": "off",
                    "state_template": "{{value_json.state}}",
                    "brightness_template": "{{value_json.brightness}}"
                }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.BRIGHTNESS_CHANNEL_ID, "", "dummy", "light", PercentageValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/light/state", "{ \"state\": \"on\", \"brightness\": 128 }");
        assertState(component, Light.BRIGHTNESS_CHANNEL_ID,
                new PercentType(new BigDecimal(128 * 100).divide(new BigDecimal(255), MathContext.DECIMAL128)));

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, PercentType.HUNDRED);
        assertPublished("zigbee2mqtt/light/set/state", "on,255");

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "off");
    }

    @Test
    public void testBrightnessAndCCT() throws InterruptedException {
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                        {
                            "schema": "template",
                            "name": "Bulb-white",
                            "command_topic": "shellies/bulb/color/0/set",
                            "state_topic": "shellies/bulb/color/0/status",
                            "availability_topic": "shellies/bulb/online",
                            "command_on_template": "{\\"turn\\": \\"on\\", \\"mode\\": \\"white\\"{%- if brightness is defined -%}, \\"brightness\\": {{brightness | float | multiply(0.39215686) | round(0)}}{%- endif -%}{%- if color_temp is defined -%}, \\"temp\\": {{ (1000000 / color_temp | float) | round(0) }}{%- endif -%}}",
                            "command_off_template": "{\\"turn\\":\\"off\\", \\"mode\\": \\"white\\"}",
                            "state_template": "{% if value_json.ison and value_json.mode == 'white' %}on{% else %}off{% endif %}",
                            "brightness_template": "{{ value_json.brightness | float | multiply(2.55) | round(0) }}",
                            "color_temp_template": "{{ (1000000 / value_json.temp | float) | round(0) }}",
                            "payload_available": "true",
                            "payload_not_available": "false",
                            "max_mireds": 334,
                            "min_mireds": 153,
                            "qos": 1,
                            "retain": false,
                            "optimistic": false
                        }
                        """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("Bulb-white"));

        assertChannel(component, Light.BRIGHTNESS_CHANNEL_ID, "", "dummy", "Brightness", PercentageValue.class);
        assertChannel(component, Light.COLOR_TEMP_CHANNEL_ID, "", "dummy", "Color Temperature", NumberValue.class);

        linkAllChannels(component);

        publishMessage("shellies/bulb/color/0/status", "{ \"state\": \"on\", \"brightness\": 100 }");
        assertState(component, Light.BRIGHTNESS_CHANNEL_ID, PercentType.HUNDRED);
        assertState(component, Light.COLOR_TEMP_CHANNEL_ID, UnDefType.NULL);

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, PercentType.HUNDRED);
        assertPublished("shellies/bulb/color/0/set", "{\"turn\": \"on\", \"mode\": \"white\", \"brightness\": 100}");

        sendCommand(component, Light.BRIGHTNESS_CHANNEL_ID, OnOffType.OFF);
        assertPublished("shellies/bulb/color/0/set", "{\"turn\":\"off\", \"mode\": \"white\"}");

        sendCommand(component, Light.COLOR_TEMP_CHANNEL_ID, QuantityType.valueOf(200, Units.MIRED));
        assertPublished("shellies/bulb/color/0/set", "{\"turn\": \"on\", \"mode\": \"white\", \"temp\": 5000}");
    }

    @Test
    public void testOnOffOnly() throws InterruptedException {
        var component = (Light<?>) discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                    "name": "light",
                    "schema": "template",
                    "state_topic": "zigbee2mqtt/light/state",
                    "command_topic": "zigbee2mqtt/light/set/state",
                    "state_template": "{{ value_json.power }}",
                    "command_on_template": "{{state}}",
                    "command_off_template": "off"
                }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.SWITCH_CHANNEL_ID, "", "dummy", "light", OnOffValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/light/state", "{\"power\": \"on\"}");
        assertState(component, Light.SWITCH_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/light/state", "{\"power\": \"off\"}");
        assertState(component, Light.SWITCH_CHANNEL_ID, OnOffType.OFF);

        sendCommand(component, Light.SWITCH_CHANNEL_ID, OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "off");
        sendCommand(component, Light.SWITCH_CHANNEL_ID, OnOffType.ON);
        assertPublished("zigbee2mqtt/light/set/state", "on");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
