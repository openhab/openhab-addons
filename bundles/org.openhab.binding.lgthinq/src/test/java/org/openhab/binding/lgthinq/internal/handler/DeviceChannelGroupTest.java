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
package org.openhab.binding.lgthinq.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;

/**
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class DeviceChannelGroupTest {

    private static final ThingUID THING_UID = new ThingUID("lgthinq", "ac", "test");
    private static final String GROUP_ID = "dashboard";
    private DeviceChannelGroup group = new DeviceChannelGroup(THING_UID, GROUP_ID);

    @BeforeEach
    void setUp() {
        group = new DeviceChannelGroup(THING_UID, GROUP_ID);
    }

    @Test
    void uidReturnsExpectedChannelGroupUID() {
        ChannelGroupUID expected = new ChannelGroupUID(THING_UID, GROUP_ID);
        assertEquals(expected, group.uid());
    }

    @Test
    void channelReturnsExpectedChannelUID() {
        ChannelUID uid = group.channel("mode");
        assertEquals(new ChannelUID(new ChannelGroupUID(THING_UID, GROUP_ID), "mode"), uid);
    }

    @Test
    void channelReturnsSameInstanceOnSubsequentCalls() {
        ChannelUID first = group.channel("targetTemperature");
        ChannelUID second = group.channel("targetTemperature");
        assertSame(first, second);
    }

    @Test
    void channelDifferentIdsDontCollide() {
        ChannelUID a = group.channel("channelA");
        ChannelUID b = group.channel("channelB");
        assertNotEquals(a, b);
    }

    @Test
    void channelTwoGroupsWithSameIdProduceDifferentUIDs() {
        DeviceChannelGroup other = new DeviceChannelGroup(THING_UID, "extendedInfo");
        ChannelUID fromDashboard = group.channel("power");
        ChannelUID fromExtended = other.channel("power");
        assertNotEquals(fromDashboard, fromExtended);
    }

    @Test
    void uidContainsGroupId() {
        assertTrue(group.uid().getId().contains(GROUP_ID));
    }
}
