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
package org.openhab.io.transport.modbus.internal.pooling;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.EvictionConfig;
import org.apache.commons.pool2.impl.EvictionPolicy;
import org.openhab.io.transport.modbus.internal.pooling.ModbusSlaveConnectionFactoryImpl.PooledConnection;

import net.wimpi.modbus.net.ModbusSlaveConnection;

/**
 * Eviction policy, i.e. policy for deciding when to close idle, unused connections.
 *
 * Connections are evicted according to {@link PooledConnection} maybeResetConnection method.
 *
 * @author Sami Salonen - Initial contribution
 */
public class ModbusSlaveConnectionEvictionPolicy implements EvictionPolicy<ModbusSlaveConnection> {

    @Override
    public boolean evict(EvictionConfig config, PooledObject<ModbusSlaveConnection> underTest, int idleCount) {
        return ((PooledConnection) underTest).maybeResetConnection("evict");
    }
}
