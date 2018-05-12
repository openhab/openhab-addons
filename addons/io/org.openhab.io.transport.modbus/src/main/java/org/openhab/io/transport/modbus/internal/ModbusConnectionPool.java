/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.io.transport.modbus.internal;

import org.apache.commons.pool2.KeyedPooledObjectFactory;
import org.apache.commons.pool2.impl.DefaultEvictionPolicy;
import org.apache.commons.pool2.impl.EvictionPolicy;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
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
public class ModbusConnectionPool extends GenericKeyedObjectPool<ModbusSlaveEndpoint, ModbusSlaveConnection> {

    public static class ModbusPoolConfig extends GenericKeyedObjectPoolConfig {

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
            this.evictionPolicy = evictionPolicy;
        }
    }

    private volatile EvictionPolicy<ModbusSlaveConnection> policy;

    public ModbusConnectionPool(KeyedPooledObjectFactory<ModbusSlaveEndpoint, ModbusSlaveConnection> factory) {
        super(factory, new ModbusPoolConfig());
    }

    @Override
    public void setConfig(GenericKeyedObjectPoolConfig conf) {
        if (!(conf instanceof ModbusPoolConfig)) {
            throw new IllegalArgumentException("Only ModbusPoolConfig accepted!");
        }
        setConfig((ModbusPoolConfig) conf);
    }

    /**
     * Similar to super class setConfig but with a workaround for POOL-338: Using explicit class for eviction policy
     */
    public void setConfig(ModbusPoolConfig conf) {
        // Same as GenericKeyedObjectPool.setConfig except for working around classpath issues
        // associated with evictionPolicyClassName. See https://issues.apache.org/jira/browse/POOL-338
        setLifo(conf.getLifo());
        setMaxIdlePerKey(conf.getMaxIdlePerKey());
        setMaxTotalPerKey(conf.getMaxTotalPerKey());
        setMaxTotal(conf.getMaxTotal());
        setMinIdlePerKey(conf.getMinIdlePerKey());
        setMaxWaitMillis(conf.getMaxWaitMillis());
        setBlockWhenExhausted(conf.getBlockWhenExhausted());
        setTestOnCreate(conf.getTestOnCreate());
        setTestOnBorrow(conf.getTestOnBorrow());
        setTestOnReturn(conf.getTestOnReturn());
        setTestWhileIdle(conf.getTestWhileIdle());
        setNumTestsPerEvictionRun(conf.getNumTestsPerEvictionRun());
        setMinEvictableIdleTimeMillis(conf.getMinEvictableIdleTimeMillis());
        setSoftMinEvictableIdleTimeMillis(conf.getSoftMinEvictableIdleTimeMillis());
        setTimeBetweenEvictionRunsMillis(conf.getTimeBetweenEvictionRunsMillis());
        // setEvictionPolicyClassName(conf.getEvictionPolicyClassName());
        policy = conf.getEvictionPolicy();
        if (policy == null) {
            throw new IllegalArgumentException("policy is null");
        }
        setEvictorShutdownTimeoutMillis(conf.getEvictorShutdownTimeoutMillis());
    }

    @Override
    protected EvictionPolicy<ModbusSlaveConnection> getEvictionPolicy() {
        return policy;
    }
}
