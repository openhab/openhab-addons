/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import java.util.Objects;

import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.EvictionPolicy;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.eclipse.jdt.annotation.NonNullByDefault;

import net.wimpi.modbus.net.ModbusSlaveConnection;

/**
 * Configuration for Modbus connection pool
 *
 * Default is that
 * - there is only one connection per endpoint
 * - clients are served "fairly" (first-come-first-serve)
 *
 * @author Sami Salonen - Initial contribution
 *
 */
@NonNullByDefault
public class ModbusPoolConfig extends GenericKeyedObjectPoolConfig {

    private EvictionPolicy<ModbusSlaveConnection> evictionPolicy = new DefaultEvictionPolicy<ModbusSlaveConnection>();

    public ModbusPoolConfig() {
        // When the pool is exhausted, multiple calling threads may be simultaneously blocked waiting for instances
        // to
        // become available. As of pool 1.5, a "fairness" algorithm has been implemented to ensure that threads
        // receive
        // available instances in request arrival order.
        setFairness(true);

        // Limit one connection per endpoint (i.e. same ip:port pair or same serial device).
        // If there are multiple read/write requests to process at the same time, block until previous one finishes
        setBlockWhenExhausted(true);
        setMaxTotalPerKey(1);

        // block infinitely when exhausted
        setMaxWaitMillis(-1);

        // Connections are "tested" on return. Effectively, disconnected connections are destroyed when returning on
        // pool
        // Note that we do not test on borrow -- that would mean blocking situation when connection cannot be
        // established.
        // Instead, borrowing connection from pool can return unconnected connection.
        setTestOnReturn(true);

        // disable JMX
        setJmxEnabled(false);
    }

    public EvictionPolicy<ModbusSlaveConnection> getEvictionPolicy() {
        return evictionPolicy;
    }

    public void setEvictionPolicy(EvictionPolicy<ModbusSlaveConnection> evictionPolicy) {
        this.evictionPolicy = Objects.requireNonNull(evictionPolicy, "evictionPolicy should not be null!");
    }
}
