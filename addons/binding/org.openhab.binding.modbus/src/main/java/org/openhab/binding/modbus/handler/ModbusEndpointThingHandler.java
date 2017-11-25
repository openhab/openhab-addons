/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.handler;

import java.util.function.Supplier;

import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Base interface for thing handlers of endpoint things
 *
 * @author Sami Salonen
 *
 */
public interface ModbusEndpointThingHandler {

    /**
     * Gets the {@link ModbusSlaveEndpoint} represented by the thing
     *
     * Note that teh endpoint can be <code>null</code> in case of incomplete initialization
     *
     * @return
     */
    public ModbusSlaveEndpoint asSlaveEndpoint();

    /**
     * Get Slave ID, also called as unit id, represented by the thing
     *
     * @return
     * @throws IllegalStateException in case the initialization is not complete
     */
    public int getSlaveId() throws IllegalStateException;

    /**
     * Get {@link ModbusManager} supplier
     *
     * @return
     */
    public Supplier<ModbusManager> getManagerRef();
}
