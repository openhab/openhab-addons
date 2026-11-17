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
package org.openhab.binding.meteostick.internal.handler;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;

/**
 * Tests the {@link LineBuffer}.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
public class LineBufferTest {

    private static @Nullable String feed(LineBuffer buffer, String data) {
        String line = null;
        for (byte value : data.getBytes(StandardCharsets.US_ASCII)) {
            line = buffer.accept(value);
        }
        return line;
    }

    @Test
    public void testLineTerminatedByCarriageReturnIsReturned() {
        LineBuffer buffer = new LineBuffer();

        assertEquals("B 19.3 982.59", feed(buffer, "B 19.3 982.59\r"));
    }

    @Test
    public void testLineFeedIsIgnored() {
        LineBuffer buffer = new LineBuffer();

        assertEquals("B 19.3 982.59", feed(buffer, "B 19.3 982.59\n\r"));
    }

    @Test
    public void testIncompleteLineIsNotReturned() {
        LineBuffer buffer = new LineBuffer();

        assertNull(feed(buffer, "B 19.3"));
    }

    @Test
    public void testEmptyLineIsIgnored() {
        LineBuffer buffer = new LineBuffer();

        assertNull(feed(buffer, "\r"));
        assertEquals("B 19.3 982.59", feed(buffer, "B 19.3 982.59\r"));
    }

    @Test
    public void testConsecutiveLinesAreReturnedSeparately() {
        LineBuffer buffer = new LineBuffer();

        assertEquals("B 19.3 982.59", feed(buffer, "B 19.3 982.59\r"));
        assertEquals("W 1 0.44 241 -67", feed(buffer, "W 1 0.44 241 -67\r"));
    }

    @Test
    public void testLineWithMaximumLengthIsAccepted() {
        LineBuffer buffer = new LineBuffer();
        String data = "1".repeat(LineBuffer.MAX_LINE_LENGTH);

        assertEquals(data, feed(buffer, data + "\r"));
    }

    @Test
    public void testLineExceedingMaximumLengthIsDroppedAsWhole() {
        LineBuffer buffer = new LineBuffer();
        String data = "1".repeat(LineBuffer.MAX_LINE_LENGTH + 1);

        assertNull(feed(buffer, data + "\r"));
        assertTrue(buffer.consumeDroppedLine());
        assertFalse(buffer.consumeDroppedLine());
    }

    @Test
    public void testBufferRecoversAfterOverlongLine() {
        LineBuffer buffer = new LineBuffer();
        String overlong = "B " + "1".repeat(LineBuffer.MAX_LINE_LENGTH + 20);

        assertNull(feed(buffer, overlong + "\r"));
        assertTrue(buffer.consumeDroppedLine());
        assertEquals("B 19.3 982.59", feed(buffer, "B 19.3 982.59\r"));
    }
}
