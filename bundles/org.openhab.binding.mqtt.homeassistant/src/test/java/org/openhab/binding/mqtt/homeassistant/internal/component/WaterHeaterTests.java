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
import static org.mockito.Mockito.*;

import java.util.Set;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.NumberValue;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.ImperialUnits;

/**
 * Tests for {@link WaterHeater}
 *
 * @author Cody Cutrer - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class WaterHeaterTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "water_heater/boiler";

    @Test
    public void test() {
        when(unitProvider.getUnit(Temperature.class)).thenReturn(ImperialUnits.FAHRENHEIT);

        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "platform": "water_heater",
                  "name": "Boiler",
                  "modes": [
                    "off",
                    "eco",
                    "performance"
                  ],
                  "mode_state_topic": "basement/boiler/mode",
                  "mode_command_topic": "basement/boiler/mode/set",
                  "mode_command_template": "{{ value if value==\\"off\\" else \\"on\\" }}",
                  "temperature_state_topic": "basement/boiler/temperature",
                  "temperature_command_topic": "basement/boiler/temperature/set",
                  "current_temperature_topic": "basement/boiler/current_temperature",
                  "precision": 1.0
                }
                """);

        assertThat(component.channels.size(), is(3));
        assertThat(component.getName(), is("Boiler"));

        assertChannel(component, WaterHeater.MODE_CHANNEL_ID, "basement/boiler/mode", "basement/boiler/mode/set",
                "Mode", TextValue.class);
        assertChannel(component, WaterHeater.CURRENT_TEMPERATURE_CHANNEL_ID, "basement/boiler/current_temperature", "",
                "Current Temperature", NumberValue.class);
        assertChannel(component, WaterHeater.TARGET_TEMPERATURE_CHANNEL_ID, "basement/boiler/temperature",
                "basement/boiler/temperature/set", "Target Temperature", NumberValue.class);

        linkAllChannels(component);

        publishMessage("basement/boiler/mode", "eco");
        assertState(component, WaterHeater.MODE_CHANNEL_ID, new StringType("eco"));
        publishMessage("basement/boiler/mode", "invalid");
        assertState(component, WaterHeater.MODE_CHANNEL_ID, new StringType("eco"));

        publishMessage("basement/boiler/current_temperature", "120");
        assertState(component, WaterHeater.CURRENT_TEMPERATURE_CHANNEL_ID,
                QuantityType.valueOf(120, ImperialUnits.FAHRENHEIT));
        publishMessage("basement/boiler/temperature", "125");
        assertState(component, WaterHeater.TARGET_TEMPERATURE_CHANNEL_ID,
                QuantityType.valueOf(125, ImperialUnits.FAHRENHEIT));

        component.getChannel(WaterHeater.MODE_CHANNEL_ID).getState().publishValue(new StringType("eco"));
        assertPublished("basement/boiler/mode/set", "on");
        component.getChannel(WaterHeater.MODE_CHANNEL_ID).getState().publishValue(new StringType("off"));
        assertPublished("basement/boiler/mode/set", "off");

        component.getChannel(WaterHeater.TARGET_TEMPERATURE_CHANNEL_ID).getState().publishValue(new DecimalType(130));
        assertPublished("basement/boiler/temperature/set", "130");
    }

    @Test
    public void testSynthesizedPowerState() {
        when(unitProvider.getUnit(Temperature.class)).thenReturn(ImperialUnits.FAHRENHEIT);

        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "platform": "water_heater",
                  "name": "Boiler",
                  "modes": [
                    "off",
                    "eco",
                    "performance"
                  ],
                  "mode_state_topic": "basement/boiler/mode",
                  "mode_command_topic": "basement/boiler/mode/set",
                  "temperature_state_topic": "basement/boiler/temperature",
                  "temperature_command_topic": "basement/boiler/temperature/set",
                  "current_temperature_topic": "basement/boiler/current_temperature",
                  "precision": 1.0,
                  "power_command_topic": "basement/boiler/power/set"
                }
                """);

        assertThat(component.channels.size(), is(4));
        assertThat(component.getName(), is("Boiler"));

        assertChannel(component, WaterHeater.STATE_CHANNEL_ID, "basement/boiler/mode", "basement/boiler/power/set",
                "State", OnOffValue.class);
        assertChannel(component, WaterHeater.MODE_CHANNEL_ID, "basement/boiler/mode", "basement/boiler/mode/set",
                "Mode", TextValue.class);
        assertChannel(component, WaterHeater.CURRENT_TEMPERATURE_CHANNEL_ID, "basement/boiler/current_temperature", "",
                "Current Temperature", NumberValue.class);
        assertChannel(component, WaterHeater.TARGET_TEMPERATURE_CHANNEL_ID, "basement/boiler/temperature",
                "basement/boiler/temperature/set", "Target Temperature", NumberValue.class);

        linkAllChannels(component);

        publishMessage("basement/boiler/mode", "eco");
        assertState(component, WaterHeater.MODE_CHANNEL_ID, new StringType("eco"));
        assertState(component, WaterHeater.STATE_CHANNEL_ID, OnOffType.ON);
        publishMessage("basement/boiler/mode", "invalid");
        assertState(component, WaterHeater.MODE_CHANNEL_ID, new StringType("eco"));
        assertState(component, WaterHeater.STATE_CHANNEL_ID, OnOffType.ON);
        publishMessage("basement/boiler/mode", "off");
        assertState(component, WaterHeater.MODE_CHANNEL_ID, new StringType("off"));
        assertState(component, WaterHeater.STATE_CHANNEL_ID, OnOffType.OFF);

        component.getChannel(WaterHeater.MODE_CHANNEL_ID).getState().publishValue(new StringType("eco"));
        assertPublished("basement/boiler/mode/set", "eco");
        component.getChannel(WaterHeater.MODE_CHANNEL_ID).getState().publishValue(new StringType("off"));
        assertPublished("basement/boiler/mode/set", "off");

        component.getChannel(WaterHeater.STATE_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("basement/boiler/power/set", "ON");
        component.getChannel(WaterHeater.STATE_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("basement/boiler/power/set", "OFF");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
