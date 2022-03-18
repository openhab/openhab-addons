/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.binding.mqtt.generic.values.ColorValue;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;

/**
 * Tests for {@link Light}
 * The current {@link Light} is non-compliant with the Specification and must be rewritten from scratch.
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class LightTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "light/0x0000000000000000_light_zigbee2mqtt";

    @Test
    public void test() throws InterruptedException {
        // @formatter:off
        var component = (Light) discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
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
                        "    \"manufacturer\": \"Lights inc\", " +
                        "    \"model\": \"light v1\", " +
                        "    \"name\": \"Light\", " +
                        "    \"sw_version\": \"Zigbee2MQTT 1.18.2\" " +
                        "  }, " +
                        "  \"name\": \"light\", " +
                        "  \"state_topic\": \"zigbee2mqtt/light/state\", " +
                        "  \"command_topic\": \"zigbee2mqtt/light/set/state\", " +
                        "  \"state_value_template\": \"{{ value_json.power }}\", " +
                        "  \"payload_on\": \"ON_\", " +
                        "  \"payload_off\": \"OFF_\", " +
                        "  \"rgb_state_topic\": \"zigbee2mqtt/light/rgb\", " +
                        "  \"rgb_command_topic\": \"zigbee2mqtt/light/set/rgb\", " +
                        "  \"rgb_value_template\": \"{{ value_json.rgb }}\", " +
                        "  \"brightness_state_topic\": \"zigbee2mqtt/light/brightness\", " +
                        "  \"brightness_command_topic\": \"zigbee2mqtt/light/set/brightness\", " +
                        "  \"brightness_value_template\": \"{{ value_json.br }}\" " +
                        "}");
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("light"));

        assertChannel(component, Light.COLOR_CHANNEL_ID, "zigbee2mqtt/light/rgb", "zigbee2mqtt/light/set/rgb", "light",
                ColorValue.class);

        assertChannel(component.switchChannel, "zigbee2mqtt/light/state", "zigbee2mqtt/light/set/state", "light",
                ColorValue.class);
        assertChannel(component.brightnessChannel, "zigbee2mqtt/light/brightness", "zigbee2mqtt/light/set/brightness",
                "light", ColorValue.class);

        publishMessage("zigbee2mqtt/light/rgb", "{\"rgb\": \"255,255,255\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.fromRGB(255, 255, 255));
        publishMessage("zigbee2mqtt/light/rgb", "{\"rgb\": \"10,20,30\"}");
        assertState(component, Light.COLOR_CHANNEL_ID, HSBType.fromRGB(10, 20, 30));

        component.switchChannel.getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/light/set/state", "0,0,0");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
