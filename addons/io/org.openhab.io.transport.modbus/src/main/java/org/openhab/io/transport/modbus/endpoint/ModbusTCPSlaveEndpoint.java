/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.endpoint;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Endpoint for TCP slaves
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusTCPSlaveEndpoint extends ModbusIPSlaveEndpoint {

    public ModbusTCPSlaveEndpoint(String address, int port) {
        super(address, port);
    }

    @Override
    public <R> R accept(ModbusSlaveEndpointVisitor<R> factory) {
        return factory.visit(this);
    }

}
