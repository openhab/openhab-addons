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
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

/**
 * Common base interface for read and write tasks.
 *
 * @author Sami Salonen - Initial contribution
 *
 * @param <R> request type
 * @param <C> callback type
 */
@NonNullByDefault
public interface TaskWithEndpoint<R, C extends ModbusCallback> {
    /**
     * Gets endpoint associated with this task
     *
     * @return
     */
    ModbusSlaveEndpoint getEndpoint();

    /**
     * Gets request associated with this task
     *
     * @return
     */
    R getRequest();

    /**
     * Gets callback associated with this task, will be called with response
     *
     * @return
     */
    @Nullable
    C getCallback();

    int getMaxTries();
}
