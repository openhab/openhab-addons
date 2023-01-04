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
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;

/**
 * Tests for {@link Cover}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class CoverTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "cover/0x0000000000000000_cover_zigbee2mqtt";

    @SuppressWarnings("null")
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
                        "    \"manufacturer\": \"Covers inc\", " +
                        "    \"model\": \"cover v1\", " +
                        "    \"name\": \"Cover\", " +
                        "    \"sw_version\": \"Zigbee2MQTT 1.18.2\" " +
                        "  }, " +
                        "  \"name\": \"cover\", " +
                        "  \"payload_open\": \"OPEN_\", " +
                        "  \"payload_close\": \"CLOSE_\", " +
                        "  \"payload_stop\": \"STOP_\", " +
                        "  \"state_topic\": \"zigbee2mqtt/cover/state\", " +
                        "  \"command_topic\": \"zigbee2mqtt/cover/set/state\" " +
                        "}");
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("cover"));

        assertChannel(component, Cover.SWITCH_CHANNEL_ID, "zigbee2mqtt/cover/state", "zigbee2mqtt/cover/set/state",
                "cover", RollershutterValue.class);

        publishMessage("zigbee2mqtt/cover/state", "100");
        assertState(component, Cover.SWITCH_CHANNEL_ID, PercentType.HUNDRED);
        publishMessage("zigbee2mqtt/cover/state", "0");
        assertState(component, Cover.SWITCH_CHANNEL_ID, PercentType.ZERO);

        component.getChannel(Cover.SWITCH_CHANNEL_ID).getState().publishValue(PercentType.ZERO);
        assertPublished("zigbee2mqtt/cover/set/state", "OPEN_");
        component.getChannel(Cover.SWITCH_CHANNEL_ID).getState().publishValue(PercentType.HUNDRED);
        assertPublished("zigbee2mqtt/cover/set/state", "CLOSE_");
        component.getChannel(Cover.SWITCH_CHANNEL_ID).getState().publishValue(StopMoveType.STOP);
        assertPublished("zigbee2mqtt/cover/set/state", "STOP_");
        component.getChannel(Cover.SWITCH_CHANNEL_ID).getState().publishValue(PercentType.ZERO);
        assertPublished("zigbee2mqtt/cover/set/state", "OPEN_", 2);
        component.getChannel(Cover.SWITCH_CHANNEL_ID).getState().publishValue(StopMoveType.STOP);
        assertPublished("zigbee2mqtt/cover/set/state", "STOP_", 2);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
