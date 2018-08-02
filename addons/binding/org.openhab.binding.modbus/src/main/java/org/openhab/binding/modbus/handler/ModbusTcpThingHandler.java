/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.handler;

import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.modbus.internal.ModbusConfigurationException;
import org.openhab.binding.modbus.internal.config.ModbusTcpConfiguration;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;

/**
 * Endpoint thing handler for TCP slaves
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusTcpThingHandler
        extends AbstractModbusEndpointThingHandler<ModbusTCPSlaveEndpoint, ModbusTcpConfiguration> {

    public ModbusTcpThingHandler(Bridge bridge, Supplier<ModbusManager> managerRef) {
        super(bridge, managerRef);
    }

    @Override
    protected void configure() throws ModbusConfigurationException {
        ModbusTcpConfiguration config = getConfigAs(ModbusTcpConfiguration.class);

        String host = config.getHost();
        if (host == null) {
            throw new ModbusConfigurationException("host must be non-null!");
        }

        this.config = config;
        endpoint = new ModbusTCPSlaveEndpoint(host, config.getPort());

        EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
        this.poolConfiguration = poolConfiguration;
        poolConfiguration.setConnectMaxTries(config.getConnectMaxTries());
        poolConfiguration.setConnectTimeoutMillis(config.getConnectTimeoutMillis());
        poolConfiguration.setInterConnectDelayMillis(config.getTimeBetweenReconnectMillis());
        poolConfiguration.setInterTransactionDelayMillis(config.getTimeBetweenTransactionsMillis());
        poolConfiguration.setReconnectAfterMillis(config.getReconnectAfterMillis());
    }

    @Override
    protected String formatConflictingParameterError(@Nullable EndpointPoolConfiguration otherPoolConfig) {
        return String.format(
                "Endpoint '%s' has conflicting parameters: parameters of this thing (%s '%s') %s are different from some other things parameter: %s. Ensure that all endpoints pointing to tcp slave '%s:%s' have same parameters.",
                endpoint, thing.getUID(), this.thing.getLabel(), this.poolConfiguration, otherPoolConfig,
                Optional.ofNullable(this.endpoint).map(e -> e.getAddress()).orElse("<null>"),
                Optional.ofNullable(this.endpoint).map(e -> String.valueOf(e.getPort())).orElse("<null>"));
    }

    @Override
    public int getSlaveId() {
        if (config == null) {
            throw new IllegalStateException("Poller not configured, but slave id is queried!");
        }
        return config.getId();
    }

}
