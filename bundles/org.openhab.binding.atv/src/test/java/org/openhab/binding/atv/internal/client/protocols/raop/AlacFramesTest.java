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
package org.openhab.binding.atv.internal.client.protocols.raop;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link AlacFrames}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class AlacFramesTest {

    @Test
    void samplesFollowTheHeaderShiftedByOneBitAndEndWithTheEndTag() {
        byte[] frame = AlacFrames.wrap(new byte[] { (byte) 0x80, 0x01 });

        // Header: channel pair, then the "not compressed" flag as the last header bit
        // Sample 0x8001 starts at bit 23, the end tag 111 at bit 39
        assertArrayEquals(new byte[] { 0x20, 0x00, 0x03, 0x00, 0x03, (byte) 0xC0 }, frame);
    }

    @Test
    void fullPacketGrowsByFourBytes() {
        assertEquals(1412, AlacFrames.wrap(new byte[352 * 4]).length);
    }
}
