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
