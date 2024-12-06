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
import org.openhab.binding.mqtt.generic.values.TextValue;

/**
 * Tests for {@link Tag}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class TagTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "tag/0AFFD2";

    @SuppressWarnings("null")
    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                    "topic": "0AFFD2/tag_scanned",
                    "value_template": "{{ value_json.PN532.UID }}"
                }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT Tag Scanner"));

        assertChannel(component, Tag.TAG_CHANNEL_ID, "0AFFD2/tag_scanned", "", "MQTT Tag Scanner", TextValue.class);

        publishMessage("0AFFD2/tag_scanned", """
                {
                  "Time": "2020-09-28T17:02:10",
                  "PN532": {
                    "UID": "E9F35959",
                    "DATA":"ILOVETASMOTA"
                  }
                }
                """);
        assertTriggered(component, Tag.TAG_CHANNEL_ID, "E9F35959");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
