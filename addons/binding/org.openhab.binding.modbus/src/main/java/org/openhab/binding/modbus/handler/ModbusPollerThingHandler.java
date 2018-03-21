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

import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusManager.PollTask;

/**
 * Interface for poller thing handlers
 *
 * @author Sami Salonen
 *
 */
public interface ModbusPollerThingHandler {

    /**
     * Return {@link PollTask} represented by this thing.
     *
     * Note that the poll task might be <code>null</code> in case initialization is not complete.
     * Also note that it is not guaranteed that the poll task is registered as regular poll with {@link ModbusManager}
     * (refresh=0 setting)
     *
     * @return
     */
    public PollTask getPollTask();

    /**
     * Get {@link ModbusManager} supplier
     *
     * @return
     */
    public Supplier<ModbusManager> getManagerRef();

    /**
     * Return the endpoint thing handler
     *
     * @return
     */
    public ModbusEndpointThingHandler getEndpointThingHandler();
}
