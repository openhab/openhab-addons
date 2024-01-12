/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.dali.internal.protocol;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dali.internal.handler.DaliException;

/**
 * The {@link DaliFrame} represents a message on the DALI bus.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliFrame {
    int bits;
    int data;

    public DaliFrame(int bits, byte[] data) throws DaliException {
        if (bits < 1) {
            throw new DaliException("Frames must contain at least 1 data bit");
        }

        this.bits = bits;

        int d = 0;
        for (byte b : data) {
            d = (d << 8) | Byte.toUnsignedInt(b);
        }

        this.data = d;

        if (this.data < 0) {
            throw new DaliException("Initial data must not be negative");
        }

        if (Math.abs(this.data) >= (1 << this.bits)) {
            throw new DaliException("Initial data will not fit in the specified number of bits");
        }
    }

    public int length() {
        return this.bits;
    }

    public byte[] pack() {
        int remaining = length();
        List<Byte> bytesList = new ArrayList<>();
        int tmp = this.data;
        while (remaining > 0) {
            bytesList.add((byte) (tmp & 0xff));
            tmp = tmp >> 8;
            remaining = remaining - 8;
        }
        byte[] result = new byte[bytesList.size()];
        int i = 0;
        for (byte b : bytesList) {
            result[bytesList.size() - ++i] = b;
        }
        return result;
    }
}
