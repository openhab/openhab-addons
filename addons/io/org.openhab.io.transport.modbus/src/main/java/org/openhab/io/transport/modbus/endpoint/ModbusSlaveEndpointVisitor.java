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
import org.eclipse.jdt.annotation.Nullable;

/**
 * Visitor for ModbusSlaveEndpoint
 *
 * @param <R> return type from visit
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusSlaveEndpointVisitor<R> {

    @Nullable
    R visit(ModbusTCPSlaveEndpoint endpoint);

    @Nullable
    R visit(ModbusSerialSlaveEndpoint endpoint);

    @Nullable
    R visit(ModbusUDPSlaveEndpoint endpoint);
}
