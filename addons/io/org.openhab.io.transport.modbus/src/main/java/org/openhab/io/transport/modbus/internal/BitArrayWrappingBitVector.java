/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.BitArray;

import net.wimpi.modbus.util.BitVector;

/**
 * BitArray implementation which wraps {@link BitVector}
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class BitArrayWrappingBitVector implements BitArray {

    private BitVector wrapped;
    private int safeSize;

    /**
     * Construct instance using BitVector data
     *
     * Depending how the wrapped data is constructed, its size might not be correct (bug of jamod library, as of
     * 2017-10). Due to this reason, <code>safeSize</code> is provided to check out-of-bounds situations
     *
     * @param wrapped wrapped data
     * @param safeSize size of wrapped data
     */
    public BitArrayWrappingBitVector(BitVector wrapped, int safeSize) {
        this.wrapped = wrapped;
        this.safeSize = safeSize;
    }

    @Override
    public boolean getBit(int index) {
        if (index >= size()) {
            throw new IndexOutOfBoundsException();
        }
        return this.wrapped.getBit(index);
    }

    @Override
    public int size() {
        return safeSize;
    }

    @Override
    public String toString() {
        return new StringBuilder("BitArrayWrappingBitVector(bits=").append(safeSize == 0 ? "<empty>" : toBinaryString())
                .append(")").toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return sizeAndValuesEquals(obj);
    }

}
