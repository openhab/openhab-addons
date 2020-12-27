/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link DaliFrame} represents a message on the DALI bus.
 *
 * @author Robert Schmid - Initial contribution
 */
@NonNullByDefault
public class DaliFrame {
    int bits;
    int data;

    public DaliFrame(int bits, byte[] data) {
        if (bits < 1) {
            throw new RuntimeException("Frames must contain at least 1 data bit");
        }

        this.bits = bits;

        int d = 0;
        for (byte b : data) {
            d = (d << 8) | Byte.toUnsignedInt(b);
        }

        this.data = d;

        if (this.data < 0) {
            throw new RuntimeException("Initial data must not be negative");
        }

        if (Math.abs(this.data) >= (1 << this.bits)) {
            throw new RuntimeException("Initial data will not fit in the specified number of bits");
        }
    }

    public int length() {
        return this.bits;
    }

    public byte[] pack() {
        int remaining = length();
        List<Byte> l = new ArrayList<Byte>();
        int d = this.data;
        while (remaining > 0) {
            l.add((byte) (d & 0xff));
            d = d >> 8;
            remaining = remaining - 8;
        }
        Collections.reverse(l);
        Byte[] bytes = l.toArray(new Byte[l.size()]);
        return ArrayUtils.toPrimitive(bytes);
    }
}
