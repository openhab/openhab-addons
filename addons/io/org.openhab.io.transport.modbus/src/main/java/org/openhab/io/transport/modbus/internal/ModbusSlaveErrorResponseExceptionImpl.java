/**
 * Copyright (c) 2010-2019 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.ModbusSlaveErrorResponseException;

import net.wimpi.modbus.ModbusSlaveException;

/**
 * Exception for explicit exception responses from Modbus slave
 *
 * @author Sami Salonen - Initial contribution
 * @author Nagy Attila Gabor - added getter for error type
 *
 */
@NonNullByDefault
public class ModbusSlaveErrorResponseExceptionImpl extends ModbusSlaveErrorResponseException {

    private static final long serialVersionUID = 6334580162425192133L;
    private int type;

    public ModbusSlaveErrorResponseExceptionImpl(ModbusSlaveException e) {
        type = e.getType();
    }

    /**
     * @return the Modbus exception code that happened
     */
    @Override
    public int getExceptionCode() {
        return type;
    }

    @Override
    public String getMessage() {
        return String.format("Slave responsed with error=%d", type);
    }

    @Override
    public String toString() {
        return String.format("ModbusSlaveErrorResponseException(error=%d)", type);
    }
}
