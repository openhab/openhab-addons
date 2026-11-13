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
package org.openhab.binding.atv.internal.client.support;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link ReadBuffer}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class ReadBufferTest {

    @Test
    public void keepsOrderAcrossGrowthAndCompaction() {
        ReadBuffer buffer = new ReadBuffer();
        byte[] chunk = new byte[5000];
        int next = 0;
        int expected = 0;
        for (int round = 0; round < 20; round++) {
            for (int i = 0; i < chunk.length; i++) {
                chunk[i] = (byte) next++;
            }
            buffer.append(chunk, 0, chunk.length);
            byte[] taken = buffer.take(3000 + round);
            for (byte b : taken) {
                assertEquals((byte) expected++, b);
            }
        }
        assertEquals(next - expected, buffer.size());
        assertEquals(expected & 0xFF, buffer.peek(0));
    }

    @Test
    public void viewShowsUnreadBytes() {
        ReadBuffer buffer = new ReadBuffer();
        buffer.append(new byte[] { 1, 2, 3, 4 }, 0, 4);
        buffer.skip(1);

        var view = buffer.view();
        assertEquals(3, view.remaining());
        assertEquals(2, view.get(0));
    }
}
