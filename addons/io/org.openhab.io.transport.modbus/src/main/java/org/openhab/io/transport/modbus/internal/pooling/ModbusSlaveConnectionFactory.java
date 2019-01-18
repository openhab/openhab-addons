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
package org.openhab.io.transport.modbus.internal.pooling;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpointVisitor;

import net.wimpi.modbus.net.ModbusSlaveConnection;

/**
 * Factory for ModbusSlaveConnection objects using endpoint definition.
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusSlaveConnectionFactory extends ModbusSlaveEndpointVisitor<ModbusSlaveConnection> {

}
