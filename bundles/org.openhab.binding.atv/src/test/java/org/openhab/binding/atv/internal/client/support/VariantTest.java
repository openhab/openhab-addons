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

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link Variant}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class VariantTest {

    @Test
    public void roundTripsMaxValue() {
        byte[] encoded = Variant.writeVariant(-1L);

        assertEquals(10, encoded.length);
        Optional<Variant.Result> result = Variant.tryReadVariant(ByteBuffer.wrap(encoded));
        assertTrue(result.isPresent());
        assertEquals(-1L, result.get().value());
        assertEquals(10, result.get().consumed());
    }

    @Test
    public void waitsForMoreDataOnPartialVariant() {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[] { (byte) 0x80, (byte) 0x80 });

        assertTrue(Variant.tryReadVariant(buffer).isEmpty());
        assertEquals(0, buffer.position());
    }

    @Test
    public void rejectsOverlongVariant() {
        byte[] continuation = new byte[11];
        Arrays.fill(continuation, (byte) 0x80);

        assertThrows(IllegalArgumentException.class, () -> Variant.tryReadVariant(ByteBuffer.wrap(continuation)));
        assertThrows(IllegalArgumentException.class, () -> Variant.readVariant(continuation));
    }
}
