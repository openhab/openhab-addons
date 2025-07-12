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
import org.openhab.core.library.types.StringType;

/**
 * Tests for {@link TExt}
 *
 * @author Cody Cutrer - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class TextTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "text/0x54ef44100064b266";

    @Test
    public void test() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                    {
                        "command_topic": "txt/cmd",
                        "state_topic": "txt/state",
                        "min": 2,
                        "max": 20
                    }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT Text"));

        assertChannel(component, Text.TEXT_CHANNEL_ID, "txt/state", "txt/cmd", "MQTT Text", TextValue.class);

        linkAllChannels(component);

        publishMessage("txt/state", "stuff");
        assertState(component, Text.TEXT_CHANNEL_ID, new StringType("stuff"));

        component.getChannel(Text.TEXT_CHANNEL_ID).getState().publishValue(new StringType("near"));
        assertPublished("txt/cmd", "near");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
