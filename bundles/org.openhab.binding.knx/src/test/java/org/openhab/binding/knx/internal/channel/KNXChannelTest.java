/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.UnDefType;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

/**
 *
 * @author Simon Kaufmann - Initial contribution
 *
 */
@NonNullByDefault
class KNXChannelTest {

    @Test
    public void invalidFails() {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("5.001:<1/3/22+0/3/22+<0/8/15");
        assertNull(res);
    }

    @Test
    void testParseWithDptMultipleWithRead() throws KNXFormatException {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("5.001:<1/3/22+0/3/22+<0/7/15");

        if (res == null) {
            fail();
            return;
        }

        assertEquals("5.001", res.getDPT());
        assertEquals(new GroupAddress("1/3/22"), res.getMainGA());
        assertTrue(res.getReadGAs().contains(res.getMainGA()));
        assertEquals(3, res.getListenGAs().size());
        assertEquals(2, res.getReadGAs().size());
    }

    @Test
    void testParseWithDptMultipleWithoutRead() throws KNXFormatException {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("5.001:1/3/22+0/3/22+0/7/15");

        if (res == null) {
            fail();
            return;
        }

        assertEquals("5.001", res.getDPT());
        assertEquals(new GroupAddress("1/3/22"), res.getMainGA());
        assertFalse(res.getReadGAs().contains(res.getMainGA()));
        assertEquals(3, res.getListenGAs().size());
        assertEquals(0, res.getReadGAs().size());
    }

    @Test
    void testParseWithoutDptSingleWithoutRead() throws KNXFormatException {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("1/3/22");

        if (res == null) {
            fail();
            return;
        }

        assertNull(res.getDPT());
        assertEquals(new GroupAddress("1/3/22"), res.getMainGA());
        assertFalse(res.getReadGAs().contains(res.getMainGA()));
        assertEquals(1, res.getListenGAs().size());
        assertEquals(0, res.getReadGAs().size());
    }

    @Test
    void testParseWithoutDptSingleWithRead() throws KNXFormatException {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("<1/3/22");

        if (res == null) {
            fail();
            return;
        }

        assertNull(res.getDPT());
        assertEquals(new GroupAddress("1/3/22"), res.getMainGA());
        assertTrue(res.getReadGAs().contains(res.getMainGA()));
        assertEquals(1, res.getListenGAs().size());
        assertEquals(1, res.getReadGAs().size());
    }

    @Test
    void testParseTwoLevel() throws KNXFormatException {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("5.001:<3/1024+<4/1025");

        if (res == null) {
            fail();
            return;
        }

        assertEquals(new GroupAddress("3/1024"), res.getMainGA());
        assertTrue(res.getReadGAs().contains(res.getMainGA()));
        assertEquals(2, res.getListenGAs().size());
        assertEquals(2, res.getReadGAs().size());
    }

    @Test
    void testParseFreeLevel() throws KNXFormatException {
        GroupAddressConfiguration res = GroupAddressConfiguration.parse("5.001:<4610+<4611");

        if (res == null) {
            fail();
            return;
        }

        assertEquals(new GroupAddress("4610"), res.getMainGA());
        assertEquals(2, res.getListenGAs().size());
        assertEquals(2, res.getReadGAs().size());
    }

    @Test
    public void testChannelGaParsing() throws KNXFormatException {
        Channel channel = mock(Channel.class);
        Configuration configuration = new Configuration(
                Map.of("key1", "5.001:<1/2/3+4/5/6+1/5/6", "key2", "1.001:7/1/9+1/1/2"));
        when(channel.getChannelTypeUID()).thenReturn(new ChannelTypeUID("a:b:c"));
        when(channel.getConfiguration()).thenReturn(configuration);
        when(channel.getAcceptedItemType()).thenReturn("none");

        MyKNXChannel knxChannel = new MyKNXChannel(channel);

        Set<GroupAddress> listenAddresses = knxChannel.getAllGroupAddresses();
        assertEquals(5, listenAddresses.size());
        // we don't check the content since parsing has been checked before and the quantity is correct
        Set<GroupAddress> writeAddresses = knxChannel.getWriteAddresses();
        assertEquals(2, writeAddresses.size());
        assertTrue(writeAddresses.contains(new GroupAddress("1/2/3")));
        assertTrue(writeAddresses.contains(new GroupAddress("7/1/9")));
    }

    private static class MyKNXChannel extends KNXChannel {
        public MyKNXChannel(Channel channel) {
            super(Set.of("key1", "key2"), List.of(UnDefType.class), channel);
        }

        @Override
        protected String getDefaultDPT(String gaConfigKey) {
            return "";
        }
    }
}
