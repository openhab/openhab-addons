/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.discovery.internal.ModbusEndpointDiscoveryService;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.internal.ModbusConfigurationException;
import org.openhab.binding.modbus.internal.config.ModbusTcpConfiguration;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.core.io.transport.modbus.endpoint.ModbusTCPSlaveEndpoint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * Endpoint thing handler for TCP slaves
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusTcpThingHandler
        extends AbstractModbusEndpointThingHandler<ModbusTCPSlaveEndpoint, ModbusTcpConfiguration> {

    public ModbusTcpThingHandler(Bridge bridge, ModbusManager manager) {
        super(bridge, manager);
    }

    @Override
    protected void configure() throws ModbusConfigurationException {
        ModbusTcpConfiguration config = getConfigAs(ModbusTcpConfiguration.class);

        String host = config.getHost();
        if (host == null) {
            throw new ModbusConfigurationException("host must be non-null!");
        }

        this.config = config;
        endpoint = new ModbusTCPSlaveEndpoint(host, config.getPort(), config.getRtuEncoded());

        EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
        this.poolConfiguration = poolConfiguration;
        poolConfiguration.setConnectMaxTries(config.getConnectMaxTries());
        poolConfiguration.setAfterConnectionDelayMillis(config.getAfterConnectionDelayMillis());
        poolConfiguration.setConnectTimeoutMillis(config.getConnectTimeoutMillis());
        poolConfiguration.setInterConnectDelayMillis(config.getTimeBetweenReconnectMillis());
        poolConfiguration.setInterTransactionDelayMillis(config.getTimeBetweenTransactionsMillis());
        poolConfiguration.setReconnectAfterMillis(config.getReconnectAfterMillis());
    }

    @SuppressWarnings("null") // since Optional.map is always called with NonNull argument
    @Override
    protected String formatConflictingParameterError() {
        return String.format(
                "Endpoint '%s' has conflicting parameters: parameters of this thing (%s '%s') are different from some other thing's parameter. Ensure that all endpoints pointing to tcp slave '%s:%s' have same parameters.",
                endpoint, thing.getUID(), this.thing.getLabel(),
                Optional.ofNullable(this.endpoint).map(e -> e.getAddress()).orElse("<null>"),
                Optional.ofNullable(this.endpoint).map(e -> String.valueOf(e.getPort())).orElse("<null>"));
    }

    @Override
    public int getSlaveId() throws EndpointNotInitializedException {
        ModbusTcpConfiguration localConfig = config;
        if (localConfig == null) {
            throw new EndpointNotInitializedException();
        }
        return localConfig.getId();
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    /**
     * Returns true if discovery is enabled
     */
    @Override
    public boolean isDiscoveryEnabled() {
        if (config != null) {
            return config.isDiscoveryEnabled();
        } else {
            return false;
        }
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ModbusEndpointDiscoveryService.class);
    }
}
