/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.handler;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Base interface for thing handlers of endpoint things
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusEndpointThingHandler {

    /**
     * Gets the {@link ModbusSlaveEndpoint} represented by the thing
     *
     * Note that the endpoint can be <code>null</code> in case of incomplete initialization
     *
     * @return endpoint represented by this thing handler
     */
    public @Nullable ModbusSlaveEndpoint asSlaveEndpoint();

    /**
     * Get Slave ID, also called as unit id, represented by the thing
     *
     * @return slave id represented by this thing handler
     * @throws EndpointNotInitializedException in case the initialization is not complete
     */
    public int getSlaveId() throws EndpointNotInitializedException;

    /**
     * Get {@link ModbusManager} supplier
     *
     * @return reference to ModbusManager
     */
    public Supplier<ModbusManager> getManagerRef();
}
