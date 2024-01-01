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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.type.AutoUpdatePolicy;

/**
 * Tests for {@link Button}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class ButtonTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "button/0x847127fffe11dd6a_auto_lock_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void testButton() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                    "dev_cla":"restart",
                    "name":"Restart",
                    "entity_category":"config",
                    "cmd_t":"esphome/single-car-gdo/button/restart/command",
                    "avty_t":"esphome/single-car-gdo/status",
                    "uniq_id":"78e36d645710-button-ba0e8e32",
                    "dev":{
                      "ids":"78e36d645710",
                      "name":"Single Car Garage Door Opener",
                      "sw":"esphome v2023.10.4 Nov  1 2023, 09:27:02",
                      "mdl":"esp32dev",
                      "mf":"espressif"}
                    }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("Restart"));

        assertChannel(component, Button.BUTTON_CHANNEL_ID, "", "esphome/single-car-gdo/button/restart/command",
                "Restart", TextValue.class);
        assertThat(Objects.requireNonNull(component.getChannel(Button.BUTTON_CHANNEL_ID)).getChannel()
                .getAutoUpdatePolicy(), is(AutoUpdatePolicy.VETO));

        assertThrows(IllegalArgumentException.class,
                () -> component.getChannel(Button.BUTTON_CHANNEL_ID).getState().publishValue(new StringType("ON")));
        assertNothingPublished("esphome/single-car-gdo/button/restart/command");
        component.getChannel(Button.BUTTON_CHANNEL_ID).getState().publishValue(new StringType("PRESS"));
        assertPublished("esphome/single-car-gdo/button/restart/command", "PRESS");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
