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
import org.openhab.io.transport.modbus.PollTask;

/**
 * Interface for poller thing handlers
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public interface ModbusPollerThingHandler {

    /**
     * Return {@link PollTask} represented by this thing.
     *
     * Note that the poll task might be <code>null</code> in case initialization is not complete.
     *
     * @return poll task represented by this poller
     */
    public @Nullable PollTask getPollTask();

    /**
     * Get {@link ModbusManager} supplier
     *
     * @return supplier of ModbusManger
     */
    public Supplier<ModbusManager> getManagerRef();

    /**
     * Refresh data
     */
    public void refresh();

}
