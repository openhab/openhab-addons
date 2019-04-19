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
package org.openhab.binding.modbus.internal.handler;

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
