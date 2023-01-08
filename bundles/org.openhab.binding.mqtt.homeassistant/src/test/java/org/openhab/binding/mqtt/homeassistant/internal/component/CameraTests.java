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
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.ImageValue;
import org.openhab.core.library.types.RawType;

/**
 * Tests for {@link Camera}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class CameraTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "camera/0x0000000000000000_camera_zigbee2mqtt";

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
                        "    \"manufacturer\": \"Cameras inc\", " +
                        "    \"model\": \"Camera\", " +
                        "    \"name\": \"camera\", " +
                        "    \"sw_version\": \"Zigbee2MQTT 1.18.2\" " +
                        "  }, " +
                        "  \"name\": \"cam1\", " +
                        "  \"topic\": \"zigbee2mqtt/cam1/state\"" +
                        "}");
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("cam1"));

        assertChannel(component, Camera.CAMERA_CHANNEL_ID, "zigbee2mqtt/cam1/state", "", "cam1", ImageValue.class);

        var imageBytes = getResourceAsByteArray("component/image.png");
        publishMessage("zigbee2mqtt/cam1/state", imageBytes);
        assertState(component, Camera.CAMERA_CHANNEL_ID, new RawType(imageBytes, "image/png"));
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
