/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.util.BitSet;
import java.util.stream.IntStream;

/**
 * Class that implements a collection for
 * bits
 *
 * @author Sami Salonen
 */
public class BitArrayImpl implements BitArray {

    private BitSet wrapped;
    private int length;

    public BitArrayImpl(int nbits) {
        this(new BitSet(nbits), nbits);
    }

    public BitArrayImpl(boolean... bits) {
        this(bitSetFromBooleans(bits), bits.length);
    }

    public BitArrayImpl(BitSet wrapped, int length) {
        this.wrapped = wrapped;
        this.length = length;
    }

    private static BitSet bitSetFromBooleans(boolean... bits) {
        BitSet bitSet = new BitSet(bits.length);
        for (int i = 0; i < bits.length; i++) {
            bitSet.set(i, bits[i]);
        }

        return bitSet;
    }

    @Override
    public boolean getBit(int index) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return this.wrapped.get(index);
    }

    public void setBit(int index, boolean value) {
        if (value) {
            this.wrapped.set(index);
        } else {
            this.wrapped.clear(index);
        }
    }

    @Override
    public int size() {
        return length;
    }

    private String zeroOneString() {
        final StringBuilder buffer = new StringBuilder(length);
        IntStream.range(0, length).mapToObj(i -> getBit(i) ? '1' : '0').forEach(buffer::append);
        return buffer.toString();
    }

    @Override
    public String toString() {
        return new StringBuilder("BitArrayImpl(wrapped=").append(zeroOneString()).toString();
    }

    @Override
    public boolean equals(Object obj) {
        return sizeAndValuesEquals(obj);
    }
}
