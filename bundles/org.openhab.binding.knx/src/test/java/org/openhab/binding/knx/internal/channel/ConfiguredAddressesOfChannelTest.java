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
package org.openhab.binding.knx.internal.channel;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.knx.internal.project.KnxConfiguredAddress;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

import io.calimero.GroupAddress;

/**
 * Checks the group addresses a channel lists for a comparison with the ETS project.
 *
 * @author Stamate Viorel - Initial contribution
 */
@NonNullByDefault
class ConfiguredAddressesOfChannelTest {
    private static final ThingUID THING = new ThingUID("knx", "device", "made-up");

    private static KNXChannel channel(String id, String type, Map<String, Object> configuration) {
        return KNXChannelFactory.createKnxChannel(
                ChannelBuilder.create(new ChannelUID(THING, id)).withType(new ChannelTypeUID(BINDING_ID, type))
                        .withConfiguration(new Configuration(configuration)).build());
    }

    private static int raw(int main, int middle, int sub) {
        return new GroupAddress(main, middle, sub).getRawAddress();
    }

    @Test
    void mainThenListeningAddressesWithTypeReadMainAndTheFirstWrittenAsStatus() {
        KNXChannel channel = channel("temperature", CHANNEL_NUMBER, Map.of("ga", "9.001:31/7/2+31/7/9+<31/7/3"));

        List<KnxConfiguredAddress> addresses = channel.configuredAddresses();

        String label = "knx:device:made-up:temperature ga";
        assertEquals(List.of(new KnxConfiguredAddress(raw(31, 7, 2), "9.001", false, true, label),
                new KnxConfiguredAddress(raw(31, 7, 3), "9.001", true, false, label),
                new KnxConfiguredAddress(raw(31, 7, 9), "9.001", false, false, true, label)), addresses);
    }

    @Test
    void aReadMainAddressIsReadAndMain() {
        KNXChannel channel = channel("light", CHANNEL_SWITCH, Map.of("ga", "1.001:<31/7/1"));

        assertEquals(
                List.of(new KnxConfiguredAddress(raw(31, 7, 1), "1.001", true, true, "knx:device:made-up:light ga")),
                channel.configuredAddresses());
    }

    @Test
    void aWriteOnlyMainAddressIsListedOnce() {
        KNXChannel channel = channel("light", CHANNEL_SWITCH, Map.of("ga", "1.001:>31/7/1"));

        assertEquals(
                List.of(new KnxConfiguredAddress(raw(31, 7, 1), "1.001", false, true, "knx:device:made-up:light ga")),
                channel.configuredAddresses());
    }

    @Test
    void withoutATypeTheKeysDefaultTypeIsTaken() {
        KNXChannel channel = channel("dimmer", CHANNEL_DIMMER,
                Map.of("switch", "31/7/1", "position", "5.001:31/7/5+31/7/6"));

        List<KnxConfiguredAddress> addresses = channel.configuredAddresses();

        assertEquals(3, addresses.size());
        KnxConfiguredAddress sw = addresses.stream().filter(a -> a.rawAddress() == raw(31, 7, 1)).findFirst()
                .orElseThrow();
        assertEquals("1.001", sw.datapointType());
        assertEquals("knx:device:made-up:dimmer switch", sw.label());
        assertTrue(addresses.stream().filter(a -> a.label().endsWith(" position"))
                .allMatch(a -> "5.001".equals(a.datapointType())));
    }
}
