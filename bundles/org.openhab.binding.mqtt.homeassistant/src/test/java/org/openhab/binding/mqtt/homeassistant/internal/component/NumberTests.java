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
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.core.library.types.DecimalType;

/**
 * Tests for {@link Number}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class NumberTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "number/0x0000000000000000_number_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                    {
                        "name": "BWA Link Hot Tub Pump 1",
                        "availability_topic": "homie/bwa/$state",
                        "payload_available": "ready",
                        "payload_not_available": "lost",
                        "qos": 1,
                        "icon": "mdi:chart-bubble",
                        "device": {
                            "manufacturer": "Balboa Water Group",
                            "sw_version": "2.1.3",
                            "model": "BFBP20",
                            "name": "BWA Link",
                            "identifiers": "bwa"
                        },
                        "state_topic": "homie/bwa/spa/pump1",
                        "command_topic": "homie/bwa/spa/pump1/set",
                        "command_template": "{{ value | round(0) }}",
                        "min": 0,
                        "max": 2,
                        "unique_id": "bwa_spa_pump1"
                    }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("BWA Link Hot Tub Pump 1"));

        assertChannel(component, Number.NUMBER_CHANNEL_ID, "homie/bwa/spa/pump1", "homie/bwa/spa/pump1/set",
                "BWA Link Hot Tub Pump 1", NumberValue.class);

        publishMessage("homie/bwa/spa/pump1", "1");
        assertState(component, Number.NUMBER_CHANNEL_ID, new DecimalType(1));
        publishMessage("homie/bwa/spa/pump1", "2");
        assertState(component, Number.NUMBER_CHANNEL_ID, new DecimalType(2));

        component.getChannel(Number.NUMBER_CHANNEL_ID).getState().publishValue(new DecimalType(1.1));
        assertPublished("homie/bwa/spa/pump1/set", "1");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
