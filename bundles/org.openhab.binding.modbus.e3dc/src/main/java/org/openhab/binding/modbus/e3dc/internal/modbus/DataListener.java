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
package org.openhab.binding.modbus.e3dc.internal.modbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.io.transport.modbus.AsyncModbusFailure;
import org.openhab.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;

/**
 * The {@link DataListener} Listener interface will be called after successful modbus poll
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public interface DataListener {
    /**
     * Informs all listeners that new data is arrived. Data needs to fetched by
     */
    public void handle(AsyncModbusReadResult result);

    /**
     * Informs all listeners that new data is arrived. Data needs to fetched by
     */
    public void handleError(AsyncModbusFailure<ModbusReadRequestBlueprint> result);
}
