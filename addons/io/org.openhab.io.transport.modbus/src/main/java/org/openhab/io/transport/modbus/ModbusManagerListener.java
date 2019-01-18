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
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Interface for {@link ModbusManager} listeners
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public interface ModbusManagerListener {

    /**
     * Called on every call for {@link ModbusManager.setEndpointPoolConfiguration}
     *
     * @param endpoint value passed in call of <code>setEndpointPoolConfiguration</code>
     * @param configuration value passed in call of <code>setEndpointPoolConfiguration</code>
     */
    public void onEndpointPoolConfigurationSet(ModbusSlaveEndpoint endpoint,
            @Nullable EndpointPoolConfiguration configuration);

}
