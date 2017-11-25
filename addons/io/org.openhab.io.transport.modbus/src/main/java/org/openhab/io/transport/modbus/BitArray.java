/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.util.Iterator;
import java.util.stream.IntStream;

/**
 * Class that implements a collection for
 * bits
 *
 * @author Sami Salonen
 */
public interface BitArray extends Iterable<Boolean> {
    /**
     * Returns the state of the bit at the given index
     *
     * Index 0 matches LSB (rightmost) bit
     * <p>
     *
     * @param index the index of the bit to be returned.
     *
     * @return true if the bit at the specified index is set,
     *         false otherwise.
     *
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    public boolean getBit(int index);

    /**
     * Get number of bits stored in this instance
     *
     * @return
     */
    public int size();

    @Override
    public default Iterator<Boolean> iterator() {
        return IntStream.range(0, size()).mapToObj(i -> getBit(i)).iterator();
    }

    public default boolean sizeAndValuesEquals(Object obj) {
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
}
