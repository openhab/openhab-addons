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

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;

import tuwien.auto.calimero.GroupAddress;
import tuwien.auto.calimero.KNXFormatException;

/**
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
class KNXChannelTypeTest {

    private KNXChannelType ct = new MyKNXChannelType("");

    @BeforeEach
    void setup() {
        ct = new MyKNXChannelType("");
    }

    @Test
    void testParseWithDptMultipleWithRead() {
        ChannelConfiguration res = ct.parse("5.001:<1/3/22+0/3/22+<0/8/15");

        if (res == null) {
            fail();
            return;
        }

        assertEquals("5.001", res.getDPT());
        assertEquals("1/3/22", res.getMainGA().getGA());
        assertTrue(res.getMainGA().isRead());
        assertEquals(3, res.getListenGAs().size());
        assertEquals(2, res.getReadGAs().size());
    }

    @Test
    void testParseWithDptMultipleWithoutRead() {
        ChannelConfiguration res = ct.parse("5.001:1/3/22+0/3/22+0/8/15");

        if (res == null) {
            fail();
            return;
        }

        assertEquals("5.001", res.getDPT());
        assertEquals("1/3/22", res.getMainGA().getGA());
        assertFalse(res.getMainGA().isRead());
        assertEquals(3, res.getListenGAs().size());
        assertEquals(0, res.getReadGAs().size());
    }

    @Test
    void testParseWithoutDptSingleWithoutRead() {
        ChannelConfiguration res = ct.parse("1/3/22");

        if (res == null) {
            fail();
            return;
        }

        assertNull(res.getDPT());
        assertEquals("1/3/22", res.getMainGA().getGA());
        assertFalse(res.getMainGA().isRead());
        assertEquals(1, res.getListenGAs().size());
        assertEquals(0, res.getReadGAs().size());
    }

    @Test
    void testParseWithoutDptSingleWitRead() {
        ChannelConfiguration res = ct.parse("<1/3/22");

        if (res == null) {
            fail();
            return;
        }

        assertNull(res.getDPT());
        assertEquals("1/3/22", res.getMainGA().getGA());
        assertTrue(res.getMainGA().isRead());
        assertEquals(1, res.getListenGAs().size());
        assertEquals(1, res.getReadGAs().size());
    }

    @Test
    void testParseTwoLevel() {
        ChannelConfiguration res = ct.parse("5.001:<3/1024+<4/1025");

        if (res == null) {
            fail();
            return;
        }

        assertEquals("3/1024", res.getMainGA().getGA());
        assertEquals(2, res.getListenGAs().size());
        assertEquals(2, res.getReadGAs().size());
    }

    @Test
    void testParseFreeLevel() {
        ChannelConfiguration res = ct.parse("5.001:<4610+<4611");

        if (res == null) {
            fail();
            return;
        }

        assertEquals("4610", res.getMainGA().getGA());
        assertEquals(2, res.getListenGAs().size());
        assertEquals(2, res.getReadGAs().size());
    }

    @Test
    public void testChannelGaParsing() throws KNXFormatException {
        Configuration configuration = new Configuration(
                Map.of("key1", "5.001:<1/2/3+4/5/6+1/5/6", "key2", "1.001:7/1/9+1/1/2"));

        Set<GroupAddress> listenAddresses = ct.getAllGroupAddresses(configuration);
        assertEquals(5, listenAddresses.size());
        // we don't check the content since parsing has been checked before and the quantity is correct
        Set<GroupAddress> writeAddresses = ct.getWriteAddresses(configuration);
        assertEquals(2, writeAddresses.size());
        assertTrue(writeAddresses.contains(new GroupAddress("1/2/3")));
        assertTrue(writeAddresses.contains(new GroupAddress("7/1/9")));
    }

    private static class MyKNXChannelType extends KNXChannelType {
        public MyKNXChannelType(String channelTypeID) {
            super(Set.of("key1", "key2"), channelTypeID);
        }

        @Override
        protected String getDefaultDPT(String gaConfigKey) {
            return "";
        }
    }
}
