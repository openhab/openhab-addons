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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * ModbusManager is the main interface for interacting with Modbus slaves
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusManager {

    /**
     * Open communication interface to endpoint
     *
     * @param endpoint endpoint pointing to modbus slave
     * @param configuration configuration for the endpoint
     * @return Communication interface for interacting with the slave
     * @throws IllegalArgumentException if there is already open communication interface with same endpoint but
     *             differing configuration
     */
    public ModbusCommunicationInterface newModbusCommunicationInterface(ModbusSlaveEndpoint endpoint,
            @Nullable EndpointPoolConfiguration configuration) throws IllegalArgumentException;

    /**
     * Get general configuration settings applied to a given endpoint
     *
     * Note that default configuration settings are returned in case the endpoint has not been configured.
     *
     * @param endpoint endpoint to query
     * @return general connection settings of the given endpoint
     */
    public @Nullable EndpointPoolConfiguration getEndpointPoolConfiguration(ModbusSlaveEndpoint endpoint);
}
