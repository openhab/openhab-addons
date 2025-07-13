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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;

/**
 * Tests for {@link Lock}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings("null")
@NonNullByDefault
public class LockTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "lock/0x0000000000000000_lock_zigbee2mqtt";

    @Test
    public void test() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x0000000000000000"
                    ],
                    "manufacturer": "Locks inc",
                    "model": "Lock",
                    "name": "LockBlower",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "name": "lock",
                  "payload_unlock": "UNLOCK_",
                  "payload_lock": "LOCK_",
                  "state_unlocked": "UNLOCKED_",
                  "state_locked": "LOCKED_",
                  "state_topic": "zigbee2mqtt/lock/state",
                  "command_topic": "zigbee2mqtt/lock/set/state",
                  "optimistic": true
                }
                """);

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("lock"));

        assertChannel(component, Lock.LOCK_CHANNEL_ID, "zigbee2mqtt/lock/state", "zigbee2mqtt/lock/set/state", "Lock",
                OnOffValue.class);
        assertChannel(component, Lock.STATE_CHANNEL_ID, "zigbee2mqtt/lock/state", "zigbee2mqtt/lock/set/state", "State",
                TextValue.class);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/lock/state", "LOCKED_");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/lock/state", "UNLOCKED_");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/lock/state", "JAMMED");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/lock/state", "GARBAGE");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));

        component.getChannel(Lock.LOCK_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/lock/set/state", "UNLOCK_");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.LOCK_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/lock/set/state", "LOCK_");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("UNLOCK"));
        assertPublished("zigbee2mqtt/lock/set/state", "UNLOCK_", 2);
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("LOCK"));
        assertPublished("zigbee2mqtt/lock/set/state", "LOCK_", 2);
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);

        assertThrows(IllegalArgumentException.class,
                () -> component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("LOCK_")));
        assertThrows(IllegalArgumentException.class,
                () -> component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("OPEN_")));
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
    }

    @Test
    public void testNoStateTopicIsOptimistic() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x0000000000000000"
                    ],
                    "manufacturer": "Locks inc",
                    "model": "Lock",
                    "name": "LockBlower",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "name": "lock",
                  "command_topic": "zigbee2mqtt/lock/set/state"
                }
                """);
        // @formatter:on

        linkAllChannels(component);

        component.getChannel(Lock.LOCK_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/lock/set/state", "UNLOCK");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.LOCK_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/lock/set/state", "LOCK");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("UNLOCK"));
        assertPublished("zigbee2mqtt/lock/set/state", "UNLOCK", 2);
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("LOCK"));
        assertPublished("zigbee2mqtt/lock/set/state", "LOCK", 2);
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);

        assertThrows(IllegalArgumentException.class,
                () -> component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("OPEN")));
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
    }

    @Test
    public void testOpennable() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x0000000000000000"
                    ],
                    "manufacturer": "Locks inc",
                    "model": "Lock",
                    "name": "LockBlower",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "name": "lock",
                  "payload_open": "OPEN",
                  "state_topic": "zigbee2mqtt/lock/state",
                  "command_topic": "zigbee2mqtt/lock/set/state",
                  "optimistic": true
                }
                """);

        linkAllChannels(component);

        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("OPEN"));
        assertPublished("zigbee2mqtt/lock/set/state", "OPEN");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
    }

    @Test
    public void testNonOptimistic() throws InterruptedException {
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC), """
                {
                  "availability": [
                    {
                      "topic": "zigbee2mqtt/bridge/state"
                    }
                  ],
                  "device": {
                    "identifiers": [
                      "zigbee2mqtt_0x0000000000000000"
                    ],
                    "manufacturer": "Locks inc",
                    "model": "Lock",
                    "name": "LockBlower",
                    "sw_version": "Zigbee2MQTT 1.18.2"
                  },
                  "name": "lock",
                  "payload_open": "OPEN",
                  "state_topic": "zigbee2mqtt/lock/state",
                  "command_topic": "zigbee2mqtt/lock/set/state"
                }
                """);

        linkAllChannels(component);

        publishMessage("zigbee2mqtt/lock/state", "LOCKED");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/lock/state", "UNLOCKED");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("UNLOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/lock/state", "LOCKED");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("LOCKED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.ON);
        publishMessage("zigbee2mqtt/lock/state", "JAMMED");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        publishMessage("zigbee2mqtt/lock/state", "GARBAGE");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));

        component.getChannel(Lock.LOCK_CHANNEL_ID).getState().publishValue(OnOffType.OFF);
        assertPublished("zigbee2mqtt/lock/set/state", "UNLOCK");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.LOCK_CHANNEL_ID).getState().publishValue(OnOffType.ON);
        assertPublished("zigbee2mqtt/lock/set/state", "LOCK");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("UNLOCK"));
        assertPublished("zigbee2mqtt/lock/set/state", "UNLOCK", 2);
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("LOCK"));
        assertPublished("zigbee2mqtt/lock/set/state", "LOCK", 2);
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);

        component.getChannel(Lock.STATE_CHANNEL_ID).getState().publishValue(new StringType("OPEN"));
        assertPublished("zigbee2mqtt/lock/set/state", "OPEN");
        assertState(component, Lock.STATE_CHANNEL_ID, new StringType("JAMMED"));
        assertState(component, Lock.LOCK_CHANNEL_ID, OnOffType.OFF);
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
