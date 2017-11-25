/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

/**
 * Interface for 16 bit Modbus registers.
 *
 * @author Sami Salonen
 */
public interface ModbusRegister {

    /**
     * Get raw data represented by this register. Since register is 16 bits, array of length 2 will be returned.
     *
     * @return byte array of length 2, high byte first.
     */
    public byte[] getBytes();

    /**
     * Returns the value of this register as integer representing 16 bit data parsed as signed integer.
     *
     * @return the register content as unsigned integer
     */
    public int getValue();

    /**
     * Returns the value of this register as integer representing 16 bit data parsed as unsigned integer.
     *
     * @return the register content as unsigned integer
     */
    public int toUnsignedShort();
}
