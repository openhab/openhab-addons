/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus;

import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Exception for connection issues
 *
 * @author Sami Salonen
 *
 */
@SuppressWarnings("serial")
public class ModbusConnectionException extends ModbusTransportException {

    private ModbusSlaveEndpoint endpoint;

    /**
     *
     * @param endpoint endpoint associated with this exception
     */
    public ModbusConnectionException(ModbusSlaveEndpoint endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Get endpoint associated with this connection error
     *
     * @return endpoint with the error
     */
    public ModbusSlaveEndpoint getEndpoint() {
        return endpoint;
    }

    @Override
    public String getMessage() {
        return String.format("Error connecting to endpoint %s", endpoint);
    }

    @Override
    public String toString() {
        return String.format("ModbusConnectionException(Error connecting to endpoint=%s)", endpoint);
    }
}
