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
import org.openhab.binding.mqtt.generic.values.TextValue;

/**
 * Tests for {@link Event}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class EventTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "event/doorbell/action";

    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                      "event_types": [
                        "press",
                        "release"
                      ],
                      "state_topic": "zigbee2mqtt/doorbell/action"
                  }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT Event"));

        assertChannel(component, "event-type", "zigbee2mqtt/doorbell/action", "", "MQTT Event", TextValue.class);

        publishMessage("zigbee2mqtt/doorbell/action", "{ \"event_type\": \"press\" }");
        assertTriggered(component, "event-type", "press");

        publishMessage("zigbee2mqtt/doorbell/action", "{ \"event_type\": \"release\" }");
        assertTriggered(component, "event-type", "release");

        publishMessage("zigbee2mqtt/doorbell/action", "{ \"event_type\": \"else\" }");
        assertNotTriggered(component, "event-type", "else");
    }

    @Test
    public void testJsonAttributes() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                      "event_types": [
                        "press",
                        "release"
                      ],
                      "state_topic": "zigbee2mqtt/doorbell/action",
                      "json_attributes_topic": "zigbee2mqtt/doorbell/action"
                  }
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("MQTT Event"));

        assertChannel(component, "event-type", "zigbee2mqtt/doorbell/action", "", "Event", TextValue.class);
        assertChannel(component, "json-attributes", "zigbee2mqtt/doorbell/action", "", "JSON Attributes",
                TextValue.class);

        publishMessage("zigbee2mqtt/doorbell/action", "{ \"event_type\": \"press\" }");
        assertTriggered(component, "json-attributes", "{ \"event_type\": \"press\" }");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
