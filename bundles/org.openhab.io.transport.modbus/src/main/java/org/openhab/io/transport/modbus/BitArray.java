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
package org.openhab.io.transport.modbus;

import java.util.BitSet;
import java.util.Iterator;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Class that implements a collection for
 * bits
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class BitArray implements Iterable<Boolean> {

    private final BitSet wrapped;
    private final int length;

    public BitArray(int nbits) {
        this(new BitSet(nbits), nbits);
    }

    public BitArray(boolean... bits) {
        this(bitSetFromBooleans(bits), bits.length);
    }

    public BitArray(BitSet wrapped, int length) {
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

    private boolean sizeAndValuesEquals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BitArray)) {
            return false;
        }
        BitArray other = (BitArray) obj;
        if (this.size() != other.size()) {
            return false;
        }
        for (int i = 0; i < this.size(); i++) {
            if (this.getBit(i) != other.getBit(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the state of the bit at the given index
     *
     * Index 0 matches LSB (rightmost) bit
     * <p>
     *
     * @param index the index of the bit to be returned.
     * @return true if the bit at the specified index is set,
     *         false otherwise.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
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

    /**
     * Get number of bits stored in this instance
     *
     * @return
     */
    public int size() {
        return length;
    }

    @Override
    public String toString() {
        return new StringBuilder("BitArray(bits=").append(length == 0 ? "<empty>" : toBinaryString()).append(")")
                .toString();
    }

    @Override
    public Iterator<Boolean> iterator() {
        return IntStream.range(0, size()).mapToObj(i -> getBit(i)).iterator();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return sizeAndValuesEquals(obj);
    }

    /**
     * Get data as binary string
     *
     * For example, 0010
     *
     * @return string representing the data
     */
    public String toBinaryString() {
        final StringBuilder buffer = new StringBuilder(size());
        IntStream.range(0, size()).mapToObj(i -> getBit(i) ? '1' : '0').forEach(buffer::append);
        return buffer.toString();
    }
}
