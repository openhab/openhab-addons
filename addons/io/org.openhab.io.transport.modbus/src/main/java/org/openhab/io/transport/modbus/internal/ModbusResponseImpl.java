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
