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
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.PercentageValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;

/**
 * Tests for {@link Valve}
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
public class ValveTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "valve/water_valve";

    @SuppressWarnings("null")
    @Test
    public void testSimple() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                    "name": "MQTT valve",
                    "command_topic": "home-assistant/valve/set",
                    "state_topic": "home-assistant/valve/state",
                    "availability": [{
                      "topic": "home-assistant/valve/availability"
                    }],
                    "qos": 0,
                    "reports_position": false,
                    "retain": true,
                    "payload_open": "OPEN",
                    "payload_close": "CLOSE",
                    "payload_stop": "STOP",
                    "state_open": "open",
                    "state_opening": "opening",
                    "state_closed": "closed",
                    "state_closing": "closing",
                    "payload_available": "online",
                    "payload_not_available": "offline",
                    "optimistic": false,
                    "value_template": "{{ value_json.x }}"
                  }
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("MQTT valve"));

        assertChannel(component, Valve.VALVE_CHANNEL_ID, "", "home-assistant/valve/set", "MQTT valve",
                OnOffValue.class);
        assertChannel(component, Valve.STATE_CHANNEL_ID, "", "", "State", TextValue.class);

        publishMessage("home-assistant/valve/state", "{\"x\": \"open\"}");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.ON);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("home-assistant/valve/state", "{\"x\": \"closed\"}");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.OFF);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("closed"));
        publishMessage("home-assistant/valve/state", "{\"x\": \"opening\"}");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.ON);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("opening"));

        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("home-assistant/valve/set", "OPEN");
        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("home-assistant/valve/set", "CLOSE");

        component.getChannel(Valve.STATE_CHANNEL_ID).getState().publishValue(new StringType("OPEN"));
        assertPublished("home-assistant/valve/set", "OPEN", 2);
        component.getChannel(Valve.STATE_CHANNEL_ID).getState().publishValue(new StringType("CLOSE"));
        assertPublished("home-assistant/valve/set", "CLOSE", 2);
        component.getChannel(Valve.STATE_CHANNEL_ID).getState().publishValue(new StringType("STOP"));
        assertPublished("home-assistant/valve/set", "STOP");
    }

    @SuppressWarnings("null")
    @Test
    public void testJsonWithSimple() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                    "name": "MQTT valve",
                    "command_topic": "home-assistant/valve/set",
                    "state_topic": "home-assistant/valve/state"
                  }
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("MQTT valve"));

        assertChannel(component, Valve.VALVE_CHANNEL_ID, "", "home-assistant/valve/set", "MQTT valve",
                OnOffValue.class);
        assertChannel(component, Valve.STATE_CHANNEL_ID, "", "", "State", TextValue.class);

        publishMessage("home-assistant/valve/state", "{\"state\": \"open\"}");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.ON);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("home-assistant/valve/state", "{\"state\": \"closed\"}");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.OFF);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("closed"));
        publishMessage("home-assistant/valve/state", "{\"state\": \"opening\"}");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.ON);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("opening"));
    }

    @SuppressWarnings("null")
    @Test
    public void testPositional() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                    "name": "MQTT valve",
                    "command_topic": "home-assistant/valve/set",
                    "state_topic": "home-assistant/valve/state",
                    "reports_position": true
                  }
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("MQTT valve"));

        assertChannel(component, Valve.VALVE_CHANNEL_ID, "", "home-assistant/valve/set", "MQTT valve",
                PercentageValue.class);
        assertChannel(component, Valve.STATE_CHANNEL_ID, "", "", "State", TextValue.class);

        publishMessage("home-assistant/valve/state", "open");
        assertState(component, Valve.VALVE_CHANNEL_ID, PercentType.HUNDRED);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("home-assistant/valve/state", "closed");
        assertState(component, Valve.VALVE_CHANNEL_ID, PercentType.ZERO);
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("closed"));
        publishMessage("home-assistant/valve/state", "50");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(50));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("home-assistant/valve/state", "opening");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(50));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("opening"));
        publishMessage("home-assistant/valve/state", "75");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(75));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("opening"));
        publishMessage("home-assistant/valve/state", "100");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(100));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("home-assistant/valve/state", "0");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(0));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("closed"));
        // check JSON messages
        publishMessage("home-assistant/valve/state", "{ \"position\": 50 }");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(50));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("home-assistant/valve/state", "{ \"position\": 20, \"state\": \"closing\" }");
        assertState(component, Valve.VALVE_CHANNEL_ID, new PercentType(20));
        assertState(component, Valve.STATE_CHANNEL_ID, new StringType("closing"));

        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("home-assistant/valve/set", "100");
        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(PercentType.HUNDRED);
        assertPublished("home-assistant/valve/set", "100", 2);
        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("home-assistant/valve/set", "0");
        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(PercentType.ZERO);
        assertPublished("home-assistant/valve/set", "0", 2);
        component.getChannel(Valve.VALVE_CHANNEL_ID).getState().publishValue(new PercentType(50));
        assertPublished("home-assistant/valve/set", "50");

        component.getChannel(Valve.STATE_CHANNEL_ID).getState().publishValue(new StringType("OPEN"));
        assertPublished("home-assistant/valve/set", "100", 3);
        component.getChannel(Valve.STATE_CHANNEL_ID).getState().publishValue(new StringType("CLOSE"));
        assertPublished("home-assistant/valve/set", "0", 3);
        component.getChannel(Valve.STATE_CHANNEL_ID).getState().publishValue(new StringType("STOP"));
        assertPublished("home-assistant/valve/set", "STOP");
    }

    @SuppressWarnings("null")
    @Test
    public void testNoState() {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                  {
                    "name": "MQTT valve",
                    "command_topic": "home-assistant/valve/set",
                    "state_topic": "home-assistant/valve/state",
                    "state_opening": null,
                    "state_closing": null
                  }
                """);

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("MQTT valve"));

        assertChannel(component, Valve.VALVE_CHANNEL_ID, "", "home-assistant/valve/set", "MQTT valve",
                OnOffValue.class);

        publishMessage("home-assistant/valve/state", "open");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.ON);
        publishMessage("home-assistant/valve/state", "closed");
        assertState(component, Valve.VALVE_CHANNEL_ID, OnOffType.OFF);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
