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
import org.apache.commons.pool2.impl.EvictionPolicy;
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

    // policy is set in super constructor via setConfig
    @NonNullByDefault({})
    private volatile EvictionPolicy<ModbusSlaveConnection> policy;

    public ModbusConnectionPool(KeyedPooledObjectFactory<ModbusSlaveEndpoint, ModbusSlaveConnection> factory) {
        super(factory, new ModbusPoolConfig());
    }

    @Override
    public void setConfig(@Nullable GenericKeyedObjectPoolConfig conf) {
        if (conf == null) {
            return;
        } else if (!(conf instanceof ModbusPoolConfig)) {
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
        setEvictorShutdownTimeoutMillis(conf.getEvictorShutdownTimeoutMillis());
    }

    @Override
    protected EvictionPolicy<ModbusSlaveConnection> getEvictionPolicy() {
        return policy;
    }
}
