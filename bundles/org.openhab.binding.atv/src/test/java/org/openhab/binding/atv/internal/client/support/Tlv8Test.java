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

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.exceptions.InvalidResponseError;

/**
 * Tests for {@link Tlv8}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class Tlv8Test {

    @Test
    public void roundTripsLongValue() {
        byte[] value = new byte[600];
        for (int i = 0; i < value.length; i++) {
            value[i] = (byte) i;
        }
        Map<Integer, byte[]> map = new LinkedHashMap<>();
        map.put(1, value);
        map.put(2, new byte[] { 9 });

        Map<Integer, byte[]> read = Tlv8.read(Tlv8.write(map));

        assertArrayEquals(value, read.get(1));
        assertArrayEquals(new byte[] { 9 }, read.get(2));
    }

    @Test
    public void rejectsTrailingTagWithoutLength() {
        assertThrows(InvalidResponseError.class, () -> Tlv8.read(new byte[] { 1, 1, 5, 2 }));
    }

    @Test
    public void rejectsTruncatedValue() {
        assertThrows(InvalidResponseError.class, () -> Tlv8.read(new byte[] { 1, 4, 5, 6 }));
    }
}
