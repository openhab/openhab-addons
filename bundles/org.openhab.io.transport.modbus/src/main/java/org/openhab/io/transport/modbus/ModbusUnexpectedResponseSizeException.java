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
 * Exception representing situation where data length of the response does not match request
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusUnexpectedResponseSizeException extends ModbusTransportException {

    private static final long serialVersionUID = 2460907938819984483L;
    private int requestSize;
    private int responseSize;

    public ModbusUnexpectedResponseSizeException(int requestSize, int responseSize) {
        this.requestSize = requestSize;
        this.responseSize = responseSize;
    }

    @Override
    public String getMessage() {
        return String.format("Data length of the request (%d) does not equal response (%d). Slave response is invalid.",
                requestSize, responseSize);
    }

    @Override
    public String toString() {
        return String.format("ModbusUnexpectedResponseSizeException(requestFunctionCode=%d, responseFunctionCode=%d)",
                requestSize, responseSize);
    }
}
