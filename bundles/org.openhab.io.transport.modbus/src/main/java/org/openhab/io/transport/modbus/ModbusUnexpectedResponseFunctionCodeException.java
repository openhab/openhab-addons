/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

/**
 * Exception representing situation where function code of the response does not match request
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusUnexpectedResponseFunctionCodeException extends ModbusTransportException {

    private static final long serialVersionUID = 1109165449703638949L;
    private int requestFunctionCode;
    private int responseFunctionCode;

    public ModbusUnexpectedResponseFunctionCodeException(int requestFunctionCode, int responseFunctionCode) {
        this.requestFunctionCode = requestFunctionCode;
        this.responseFunctionCode = responseFunctionCode;
    }

    @Override
    public String getMessage() {
        return String.format("Function code of request (%d) does not equal response (%d)", requestFunctionCode,
                responseFunctionCode);
    }

    @Override
    public String toString() {
        return String.format(
                "ModbusUnexpectedResponseFunctionCodeException(requestFunctionCode=%d, responseFunctionCode=%d)",
                requestFunctionCode, responseFunctionCode);
    }
}
