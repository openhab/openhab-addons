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
