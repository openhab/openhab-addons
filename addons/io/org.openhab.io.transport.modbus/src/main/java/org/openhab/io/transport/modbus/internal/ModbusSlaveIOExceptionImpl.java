/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.ModbusSlaveIOException;

import net.wimpi.modbus.ModbusIOException;

/**
 * Exception for all IO errors
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusSlaveIOExceptionImpl extends ModbusSlaveIOException {

    private static final long serialVersionUID = -8910463902857643468L;
    private Exception error;

    public ModbusSlaveIOExceptionImpl(ModbusIOException e) {
        this.error = e;
    }

    public ModbusSlaveIOExceptionImpl(IOException e) {
        this.error = e;
    }

    @Override
    public String getMessage() {
        return String.format("Modbus IO Error with cause=%s, EOF=%s, message='%s', cause2=%s",
                error.getClass().getSimpleName(),
                error instanceof ModbusIOException ? ((ModbusIOException) error).isEOF() : "?", error.getMessage(),
                error.getCause());
    }

    @Override
    public String toString() {
        return String.format("ModbusSlaveIOException(cause=%s, EOF=%s, message='%s', cause2=%s)",
                error.getClass().getSimpleName(),
                error instanceof ModbusIOException ? ((ModbusIOException) error).isEOF() : "?", error.getMessage(),
                error.getCause());
    }
}
