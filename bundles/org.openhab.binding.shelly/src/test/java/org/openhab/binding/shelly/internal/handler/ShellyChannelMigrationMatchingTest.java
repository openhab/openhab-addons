/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Tests for {@link ShellyChannelMigration#findChannels(List, String)} — the exact and wildcard
 * matching used by the channel migration rules.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyChannelMigrationMatchingTest {

    private static final ThingUID THING_UID = new ThingUID("shelly", "shellyplus1pm", "test");

    @Test
    void exactRuleMatchesOnlyThatGroup() {
        List<Channel> channels = List.of(channel("device#totalKWH"), channel("meter#totalKWH"));

        List<Channel> result = ShellyChannelMigration.findChannels(channels, "device#totalKWH");

        assertEquals(1, result.size());
        assertEquals("device#totalKWH", result.get(0).getUID().getId());
    }

    @Test
    void exactRuleDoesNotMatchDifferentGroup() {
        List<Channel> channels = List.of(channel("meter#totalKWH"));

        List<Channel> result = ShellyChannelMigration.findChannels(channels, "device#totalKWH");

        assertTrue(result.isEmpty());
    }

    @Test
    void wildcardRuleMatchesAllIndexedMeterGroups() {
        List<Channel> channels = List.of(channel("meter#totalKWH"), channel("meter1#totalKWH"),
                channel("meter2#totalKWH"), channel("meter3#totalKWH"), channel("meter4#totalKWH"));

        List<Channel> result = ShellyChannelMigration.findChannels(channels, "meter*#totalKWH");

        assertEquals(5, result.size());
    }

    @Test
    void wildcardRuleDoesNotMatchDeviceOrNmeterGroup() {
        List<Channel> channels = List.of(channel("device#totalKWH"), channel("nmeter#totalKWH"),
                channel("meter1#totalKWH"));

        List<Channel> result = ShellyChannelMigration.findChannels(channels, "meter*#totalKWH");

        assertEquals(1, result.size());
        assertEquals("meter1#totalKWH", result.get(0).getUID().getId());
    }

    @Test
    void wildcardRuleDoesNotMatchDifferentChannelName() {
        List<Channel> channels = List.of(channel("meter1#currentWatts"));

        List<Channel> result = ShellyChannelMigration.findChannels(channels, "meter*#totalKWH");

        assertTrue(result.isEmpty());
    }

    @Test
    void bareNameWithoutGroupMatchesNothing() {
        List<Channel> channels = List.of(channel("meter#totalKWH"), channel("device#totalKWH"));

        assertTrue(ShellyChannelMigration.findChannels(channels, "totalKWH").isEmpty());
    }

    @Test
    void noChannelsMatchReturnsEmptyList() {
        List<Channel> channels = List.of(channel("meter#currentPower"));

        assertTrue(ShellyChannelMigration.findChannels(channels, "device#totalKWH").isEmpty());
        assertTrue(ShellyChannelMigration.findChannels(channels, "meter*#totalKWH").isEmpty());
    }

    private static Channel channel(String channelId) {
        return ChannelBuilder.create(new ChannelUID(THING_UID, channelId)).build();
    }
}
