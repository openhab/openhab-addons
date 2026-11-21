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
package org.openhab.binding.atv.internal.client.scan;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Tests for {@link Dns#parseDomainName(ByteBuffer)}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class DnsTest {

    @Test
    public void followsBackwardPointer() {
        // "foo" at offset 0, then "bar" + pointer to offset 0
        byte[] data = { 3, 'f', 'o', 'o', 0, 3, 'b', 'a', 'r', (byte) 0xC0, 0x00 };
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(5);

        assertEquals("bar.foo", Dns.parseDomainName(buffer));
        assertEquals(data.length, buffer.position());
    }

    @Test
    @Timeout(5)
    public void rejectsSelfReferencingPointer() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { (byte) 0xC0, 0x00 });

        assertThrows(IllegalArgumentException.class, () -> Dns.parseDomainName(buffer));
    }

    @Test
    @Timeout(5)
    public void rejectsPointerLoopThroughLabels() {
        // "a" at 0, "b" at 2, then a pointer back to "b": b -> b -> b ...
        byte[] data = { 1, 'a', 1, 'b', (byte) 0xC0, 0x02 };
        ByteBuffer buffer = ByteBuffer.wrap(data);
        buffer.position(2);

        assertThrows(IllegalArgumentException.class, () -> Dns.parseDomainName(buffer));
    }

    @Test
    public void rejectsOverlongName() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] label = "x".repeat(63).getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < 5; i++) {
            out.write(label.length);
            out.writeBytes(label);
        }
        out.write(0);

        assertThrows(IllegalArgumentException.class, () -> Dns.parseDomainName(ByteBuffer.wrap(out.toByteArray())));
    }
}
