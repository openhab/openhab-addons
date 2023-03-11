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

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

    private static class MyKNXChannelType extends KNXChannelType {
        public MyKNXChannelType(String channelTypeID) {
            super(channelTypeID);
        }

        @Override
        protected Set<String> getAllGAKeys() {
            return Collections.emptySet();
        }

        @Override
        protected String getDefaultDPT(String gaConfigKey) {
            return "";
        }
    }
}
