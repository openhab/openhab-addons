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
package org.openhab.io.transport.modbus.internal;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;

import net.wimpi.modbus.net.ModbusSlaveConnection;

/**
 * Pool for modbus connections.
 *
 * Only one connection is allowed to be active at a time.
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusConnectionPool extends GenericKeyedObjectPool<ModbusSlaveEndpoint, ModbusSlaveConnection> {

    public ModbusConnectionPool(KeyedPooledObjectFactory<ModbusSlaveEndpoint, ModbusSlaveConnection> factory) {
        super(factory, new ModbusPoolConfig());
    }

    @Override
    public void setConfig(@Nullable GenericKeyedObjectPoolConfig<ModbusSlaveConnection> conf) {
        if (conf == null) {
            return;
        } else if (!(conf instanceof ModbusPoolConfig)) {
            throw new IllegalArgumentException("Only ModbusPoolConfig accepted!");
        }
        super.setConfig(conf);
    }
}
