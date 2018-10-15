/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;

import net.wimpi.modbus.Modbus;

/**
 * Base interface for Modbus write requests
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusWriteRequestBlueprint extends ModbusRequestBlueprint {

    /**
     * Returns the protocol identifier of this
     * <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The identifier is a 2-byte (short) non negative
     * integer value valid in the range of 0-65535.
     * <p>
     *
     * @return the protocol identifier as <tt>int</tt>.
     */
    @Override
    public default int getProtocolID() {
        return Modbus.DEFAULT_PROTOCOL_ID;
    }

    /**
     * Returns the reference of the register/coil/discrete input to to start
     * writing with this request
     * <p>
     *
     * @return the reference of the register
     *         to start reading from as <tt>int</tt>.
     */
    public int getReference();

    /**
     * Returns the unit identifier of this
     * <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The identifier is a 1-byte non negative
     * integer value valid in the range of 0-255.
     * <p>
     *
     * @return the unit identifier as <tt>int</tt>.
     */
    @Override
    public int getUnitID();

    /**
     * Returns the function code of this
     * <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The function code is a 1-byte non negative
     * integer value valid in the range of 0-127.<br>
     * Function codes are ordered in conformance
     * classes their values are specified in
     * <tt>net.wimpi.modbus.Modbus</tt>.
     * <p>
     *
     * @return the function code as <tt>int</tt>.
     *
     * @see net.wimpi.modbus.Modbus
     */
    public ModbusWriteFunctionCode getFunctionCode();

    /**
     * Get maximum number of tries, in case errors occur. Should be at least 1.
     */
    @Override
    public int getMaxTries();

    /**
     * Accept visitor
     *
     * @param visitor
     */
    public void accept(ModbusWriteRequestBlueprintVisitor visitor);

}
