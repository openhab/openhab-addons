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
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link Humidifier}
 *
 * @author Cody Cutrer - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class HumidifierTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "humidifier/bedroom_humidifier";

    @Test
    public void test() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "platform": "humidifier",
                  "name": "Bedroom humidifier",
                  "device_class": "humidifier",
                  "state_topic": "bedroom_humidifier/on/state",
                  "action_topic": "bedroom_humidifier/action",
                  "command_topic": "bedroom_humidifier/on/set",
                  "current_humidity_topic": "bedroom_humidifier/humidity/current",
                  "target_humidity_command_topic": "bedroom_humidifier/humidity/set",
                  "target_humidity_state_topic": "bedroom_humidifier/humidity/state",
                  "mode_state_topic": "bedroom_humidifier/mode/state",
                  "mode_command_topic": "bedroom_humidifier/preset/preset_mode",
                  "modes": [
                    "normal",
                    "eco",
                    "away",
                    "boost",
                    "comfort",
                    "home",
                    "sleep",
                    "auto",
                    "baby"],
                  "qos": 0,
                  "payload_on": "true",
                  "payload_off": "false",
                  "min_humidity": 30,
                  "max_humidity": 80
                }
                """);

        assertThat(component.channels.size(), is(6));
        assertThat(component.getName(), is("Bedroom humidifier"));

        assertChannel(component, Humidifier.STATE_CHANNEL_ID, "bedroom_humidifier/on/state",
                "bedroom_humidifier/on/set", "State", OnOffValue.class);
        assertChannel(component, Humidifier.ACTION_CHANNEL_ID, "bedroom_humidifier/action", "", "Action",
                TextValue.class);
        assertChannel(component, Humidifier.MODE_CHANNEL_ID, "bedroom_humidifier/mode/state",
                "bedroom_humidifier/preset/preset_mode", "Mode", TextValue.class);
        assertChannel(component, Humidifier.DEVICE_CLASS_CHANNEL_ID, "", "", "Device Class", TextValue.class);
        assertChannel(component, Humidifier.CURRENT_HUMIDITY_CHANNEL_ID, "bedroom_humidifier/humidity/current", "",
                "Current Humidity", NumberValue.class);
        assertChannel(component, Humidifier.TARGET_HUMIDITY_CHANNEL_ID, "bedroom_humidifier/humidity/state",
                "bedroom_humidifier/humidity/set", "Target Humidity", NumberValue.class);

        linkAllChannels(component);

        publishMessage("bedroom_humidifier/on/state", "true");
        assertState(component, Humidifier.STATE_CHANNEL_ID, OnOffType.ON);
        publishMessage("bedroom_humidifier/on/state", "false");
        assertState(component, Humidifier.STATE_CHANNEL_ID, OnOffType.OFF);

        publishMessage("bedroom_humidifier/action", "off");
        assertState(component, Humidifier.ACTION_CHANNEL_ID, new StringType("off"));
        publishMessage("bedroom_humidifier/action", "idle");
        assertState(component, Humidifier.ACTION_CHANNEL_ID, new StringType("idle"));
        publishMessage("bedroom_humidifier/action", "invalid");
        assertState(component, Humidifier.ACTION_CHANNEL_ID, new StringType("idle"));

        publishMessage("bedroom_humidifier/mode/state", "eco");
        assertState(component, Humidifier.MODE_CHANNEL_ID, new StringType("eco"));
        publishMessage("bedroom_humidifier/mode/state", "invalid");
        assertState(component, Humidifier.MODE_CHANNEL_ID, new StringType("eco"));
        publishMessage("bedroom_humidifier/mode/state", "None");
        assertState(component, Humidifier.MODE_CHANNEL_ID, UnDefType.NULL);

        publishMessage("bedroom_humidifier/humidity/current", "35");
        assertState(component, Humidifier.CURRENT_HUMIDITY_CHANNEL_ID, QuantityType.valueOf(35, Units.PERCENT));
        publishMessage("bedroom_humidifier/humidity/state", "40");
        assertState(component, Humidifier.TARGET_HUMIDITY_CHANNEL_ID, QuantityType.valueOf(40, Units.PERCENT));

        component.getChannel(Humidifier.STATE_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("bedroom_humidifier/on/set", "false");
        component.getChannel(Humidifier.STATE_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("bedroom_humidifier/on/set", "true");

        component.getChannel(Humidifier.MODE_CHANNEL_ID).getState().publishValue(new StringType("eco"));
        assertPublished("bedroom_humidifier/preset/preset_mode", "eco");

        component.getChannel(Humidifier.TARGET_HUMIDITY_CHANNEL_ID).getState().publishValue(new DecimalType(45));
        assertPublished("bedroom_humidifier/humidity/set", "45");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
