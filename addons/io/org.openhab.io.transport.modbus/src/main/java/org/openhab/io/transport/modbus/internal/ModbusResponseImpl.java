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
import org.openhab.io.transport.modbus.ModbusResponse;

import net.wimpi.modbus.msg.ModbusMessage;

/**
 * Basic implementation of {@link ModbusResponse}
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusResponseImpl implements ModbusResponse {

    private int responseFunctionCode;

    public ModbusResponseImpl(ModbusMessage response) {
        super();
        this.responseFunctionCode = response.getFunctionCode();
    }

    @Override
    public int getFunctionCode() {
        return responseFunctionCode;
    }

    @Override
    public String toString() {
        return String.format("ModbusResponseImpl(responseFC=%d)", responseFunctionCode);
    }

}
