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

/**
 * Interface for read callbacks
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusReadCallback extends ModbusCallback {

    /**
     * Callback for "input register" and "holding register" data in the case of no errors
     *
     * @param ModbusReadRequestBlueprint representing the request
     * @param registers data received from slave device in the last pollInterval
     */
    void onRegisters(ModbusReadRequestBlueprint request, ModbusRegisterArray registers);

    /**
     * Callback for "coil" and "discrete input" bit data in the case of no errors
     *
     * @param ModbusReadRequestBlueprint representing the request
     * @param bits data received from slave device
     */
    void onBits(ModbusReadRequestBlueprint request, BitArray bits);

    /**
     * Callback for errors with read
     *
     * @request ModbusRequestBlueprint representing the request
     * @param Exception representing the issue with the request. Instance of
     *            {@link ModbusUnexpectedTransactionIdException} or {@link ModbusTransportException}.
     */
    void onError(ModbusReadRequestBlueprint request, Exception error);

}
