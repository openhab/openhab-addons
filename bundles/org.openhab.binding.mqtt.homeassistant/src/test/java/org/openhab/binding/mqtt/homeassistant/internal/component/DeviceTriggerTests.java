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
import org.openhab.binding.mqtt.generic.values.TextValue;

/**
 * Tests for {@link DeviceTrigger}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class DeviceTriggerTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "device_automation/0x8cf681fffe2fd2a6";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                    "automation_type": "trigger",
                    "device": {
                        "configuration_url": "#/device/0x8cf681fffe2fd2a6/info",
                        "identifiers": [
                            "zigbee2mqtt_0x8cf681fffe2fd2a6"
                        ],
                        "manufacturer": "IKEA",
                        "model": "TRADFRI shortcut button (E1812)",
                        "name": "Charge Now Button",
                        "sw_version": "2.3.015"
                    },
                    "payload": "on",
                    "subtype": "on",
                    "topic": "zigbee2mqtt/Charge Now Button/action",
                    "type": "action"
                }
                    """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT Device Trigger"));

        assertChannel(component, "action", "zigbee2mqtt/Charge Now Button/action", "", "MQTT Device Trigger",
                TextValue.class);

        spyOnChannelUpdates(component, "action");
        publishMessage("zigbee2mqtt/Charge Now Button/action", "on");
        assertTriggered(component, "action", "on");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
