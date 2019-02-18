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

/**
 * Interface for write callbacks
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusWriteCallback extends ModbusCallback {

    /**
     * Callback handler method for cases when an error occurred with write
     *
     * Note that only one of the two is called: onError, onResponse
     *
     * @request ModbusWriteRequestBlueprint representing the request
     * @param Exception representing the issue with the request. Instance of
     *            {@link ModbusUnexpectedTransactionIdException} or {@link ModbusTransportException}.
     */
    void onError(ModbusWriteRequestBlueprint request, Exception error);

    /**
     * Callback handler method for successful writes
     *
     * Note that only one of the two is called: onError, onResponse
     *
     * @param request ModbusWriteRequestBlueprint representing the request
     * @param response response matching the write request
     */
    void onWriteResponse(ModbusWriteRequestBlueprint request, ModbusResponse response);

}
