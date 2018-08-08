/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import java.util.Iterator;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Interface for immutable sequence of Modbus registers
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusRegisterArray extends Iterable<ModbusRegister> {
    /**
     * Return register at the given index
     *
     * Index 0 matches first register (lowest register index).
     * <p>
     *
     * @param index the index of the register to be returned.
     * @throws IndexOutOfBoundsException if the index is out of bounds.
     */
    ModbusRegister getRegister(int index);

    /**
     * Get number of registers stored in this instance
     *
     * @return
     */
    int size();

    /**
     * Iterator over all the registers
     */
    @Override
    default Iterator<ModbusRegister> iterator() {
        return IntStream.range(0, size()).mapToObj(i -> getRegister(i)).iterator();
    }

    /**
     * Get register data as a hex string
     *
     * For example, 04 45 00 00
     *
     * @return string representing the bytes of the register array
     */
    default String toHexString() {
        if (size() == 0) {
            return "";
        }
        // Initialize capacity to (n*2 + n-1), two chars per byte + spaces in between
        StringBuffer buffer = new StringBuffer(size() * 2 + (size() - 1));
        return appendHexString(buffer).toString();
    }

    /**
     * Appends the register data as hex string to the given StringBuffer
     *
     */
    default StringBuffer appendHexString(StringBuffer buffer) {
        IntStream.range(0, size()).forEachOrdered(index -> {
            getRegister(index).appendHexString(buffer);
            if (index < size() - 1) {
                buffer.append(' ');
            }
        });
        return buffer;
    }

}
