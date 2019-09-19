/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;

import net.wimpi.modbus.Modbus;

/**
 * Base interface for Modbus requests
 *
 * @see ModbusReadRequestBlueprint
 * @see ModbusWriteRequestBlueprint
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusRequestBlueprint {

    /**
     * Returns the protocol identifier of this
     * <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The identifier is a 2-byte (short) non negative
     * integer value valid in the range of 0-65535.
     * <p>
     *
     * @return the protocol identifier as <tt>int</tt>.
     */
    public default int getProtocolID() {
        return Modbus.DEFAULT_PROTOCOL_ID;
    }

    /**
     * Returns the unit identifier of this
     * <tt>ModbusMessage</tt> as <tt>int</tt>.<br>
     * The identifier is a 1-byte non negative
     * integer value valid in the range of 0-255.
     * <p>
     *
     * @return the unit identifier as <tt>int</tt>.
     */
    public int getUnitID();

    /**
     * Maximum number of tries to execute the request, when request fails
     *
     * For example, number 1 means on try only with no re-tries.
     *
     * @return number of maximum tries
     */
    public int getMaxTries();

}
