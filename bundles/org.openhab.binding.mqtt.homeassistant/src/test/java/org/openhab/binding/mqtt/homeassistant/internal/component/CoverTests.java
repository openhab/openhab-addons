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
import org.openhab.binding.mqtt.generic.values.RollershutterValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;

/**
 * Tests for {@link Cover}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@NonNullByDefault
public class CoverTests extends AbstractComponentTests {
    public static final String CONFIG_TOPIC = "cover/0x0000000000000000_cover_zigbee2mqtt";

    @SuppressWarnings("null")
    @Test
    public void testStateOnly() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                { \
                  "availability": [ \
                    { \
                      "topic": "zigbee2mqtt/bridge/state" \
                    } \
                  ], \
                  "device": { \
                    "identifiers": [ \
                      "zigbee2mqtt_0x0000000000000000" \
                    ], \
                    "manufacturer": "Covers inc", \
                    "model": "cover v1", \
                    "name": "Cover", \
                    "sw_version": "Zigbee2MQTT 1.18.2" \
                  }, \
                  "name": "cover", \
                  "payload_open": "OPEN_", \
                  "payload_close": "CLOSE_", \
                  "payload_stop": "STOP_", \
                  "state_topic": "zigbee2mqtt/cover/state", \
                  "command_topic": "zigbee2mqtt/cover/set/state" \
                }\
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("cover"));

        assertChannel(component, Cover.STATE_CHANNEL_ID, "zigbee2mqtt/cover/state", "", "State", TextValue.class);
        assertChannel(component, Cover.COVER_CHANNEL_ID, "zigbee2mqtt/cover/state", "zigbee2mqtt/cover/set/state",
                "Cover", RollershutterValue.class);

        publishMessage("zigbee2mqtt/cover/state", "closed");
        assertState(component, Cover.COVER_CHANNEL_ID, UpDownType.DOWN);
        assertState(component, Cover.STATE_CHANNEL_ID, new StringType("closed"));
        publishMessage("zigbee2mqtt/cover/state", "open");
        assertState(component, Cover.STATE_CHANNEL_ID, new StringType("open"));
        assertState(component, Cover.COVER_CHANNEL_ID, UpDownType.UP);

        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(UpDownType.UP);
        assertPublished("zigbee2mqtt/cover/set/state", "OPEN_");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(UpDownType.DOWN);
        assertPublished("zigbee2mqtt/cover/set/state", "CLOSE_");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(StopMoveType.STOP);
        assertPublished("zigbee2mqtt/cover/set/state", "STOP_");
    }

    @SuppressWarnings("null")
    @Test
    public void testPositionAndState() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                {
                  "dev_cla":"garage",
                  "pos_t":"esphome/single-car-gdo/cover/door/position/state",
                  "set_pos_t":"esphome/single-car-gdo/cover/door/position/command",
                  "name":"Door",
                  "stat_t":"esphome/single-car-gdo/cover/door/state",
                  "cmd_t":"esphome/single-car-gdo/cover/door/command",
                  "avty_t":"esphome/single-car-gdo/status",
                  "uniq_id":"78e36d645710-cover-d27845ad",
                  "dev":{
                    "ids":"78e36d645710",
                    "name":"Single Car Garage Door Opener",
                    "sw":"esphome v2023.10.4 Nov  7 2023, 16:19:39",
                    "mdl":"esp32dev",
                    "mf":"espressif"}
                }
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(2));
        assertThat(component.getName(), is("Door"));

        assertChannel(component, Cover.STATE_CHANNEL_ID, "esphome/single-car-gdo/cover/door/state", "", "State",
                TextValue.class);
        assertChannel(component, Cover.COVER_CHANNEL_ID, "esphome/single-car-gdo/cover/door/position/state",
                "esphome/single-car-gdo/cover/door/position/command", "Cover", RollershutterValue.class);

        publishMessage("esphome/single-car-gdo/cover/door/state", "closed");
        assertState(component, Cover.STATE_CHANNEL_ID, new StringType("closed"));
        publishMessage("esphome/single-car-gdo/cover/door/state", "open");
        assertState(component, Cover.STATE_CHANNEL_ID, new StringType("open"));
        publishMessage("esphome/single-car-gdo/cover/door/state", "opening");
        assertState(component, Cover.STATE_CHANNEL_ID, new StringType("opening"));

        publishMessage("esphome/single-car-gdo/cover/door/position/state", "100");
        assertState(component, Cover.COVER_CHANNEL_ID, PercentType.ZERO);
        publishMessage("esphome/single-car-gdo/cover/door/position/state", "40");
        assertState(component, Cover.COVER_CHANNEL_ID, new PercentType(60));
        publishMessage("esphome/single-car-gdo/cover/door/position/state", "0");
        assertState(component, Cover.COVER_CHANNEL_ID, PercentType.HUNDRED);

        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(PercentType.ZERO);
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "100");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(PercentType.HUNDRED);
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "0");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(StopMoveType.STOP);
        assertPublished("esphome/single-car-gdo/cover/door/command", "STOP");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(UpDownType.UP);
        assertPublished("esphome/single-car-gdo/cover/door/command", "OPEN");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(UpDownType.DOWN);
        assertPublished("esphome/single-car-gdo/cover/door/command", "CLOSE");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(new PercentType(40));
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "60");
    }

    @SuppressWarnings("null")
    @Test
    public void testPositionOnly() throws InterruptedException {
        // @formatter:off
        var component = discoverComponent(configTopicToMqtt(CONFIG_TOPIC),
                """
                {
                  "dev_cla":"garage",
                  "pos_t":"esphome/single-car-gdo/cover/door/position/state",
                  "set_pos_t":"esphome/single-car-gdo/cover/door/position/command",
                  "name":"Door",
                  "avty_t":"esphome/single-car-gdo/status",
                  "uniq_id":"78e36d645710-cover-d27845ad",
                  "dev":{
                    "ids":"78e36d645710",
                    "name":"Single Car Garage Door Opener",
                    "sw":"esphome v2023.10.4 Nov  7 2023, 16:19:39",
                    "mdl":"esp32dev",
                    "mf":"espressif"}
                }
                """);
        // @formatter:on

        assertThat(component.channels.size(), is(1));
        assertThat(component.getName(), is("Door"));

        assertChannel(component, Cover.COVER_CHANNEL_ID, "esphome/single-car-gdo/cover/door/position/state",
                "esphome/single-car-gdo/cover/door/position/command", "Cover", RollershutterValue.class);

        publishMessage("esphome/single-car-gdo/cover/door/position/state", "100");
        assertState(component, Cover.COVER_CHANNEL_ID, PercentType.ZERO);
        publishMessage("esphome/single-car-gdo/cover/door/position/state", "40");
        assertState(component, Cover.COVER_CHANNEL_ID, new PercentType(60));
        publishMessage("esphome/single-car-gdo/cover/door/position/state", "0");
        assertState(component, Cover.COVER_CHANNEL_ID, PercentType.HUNDRED);

        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(PercentType.ZERO);
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "100");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(PercentType.HUNDRED);
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "0");
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(UpDownType.UP);
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "100", 2);
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(UpDownType.DOWN);
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "0", 2);
        component.getChannel(Cover.COVER_CHANNEL_ID).getState().publishValue(new PercentType(40));
        assertPublished("esphome/single-car-gdo/cover/door/position/command", "60");
    }

    @Override
    protected Set<String> getConfigTopics() {
        return Set.of(CONFIG_TOPIC);
    }
}
