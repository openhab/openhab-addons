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
package org.openhab.binding.sbus.internal.handler;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.sbus.discovery.internal.ModbusEndpointDiscoveryService;
import org.openhab.binding.sbus.handler.EndpointNotInitializedException;
import org.openhab.binding.sbus.internal.ModbusConfigurationException;
import org.openhab.binding.sbus.internal.config.ModbusUdpConfiguration;
import org.openhab.core.io.transport.sbus.ModbusManager;
import org.openhab.core.io.transport.sbus.endpoint.EndpointPoolConfiguration;
import org.openhab.core.io.transport.sbus.endpoint.ModbusUDPSlaveEndpoint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * Endpoint thing handler for UDP slaves
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class ModbusUdpThingHandler
        extends AbstractModbusEndpointThingHandler<ModbusUDPSlaveEndpoint, ModbusUdpConfiguration> {

    public ModbusUdpThingHandler(Bridge bridge, ModbusManager manager) {
        super(bridge, manager);
    }

    @Override
    protected void configure() throws ModbusConfigurationException {
        ModbusUdpConfiguration config = getConfigAs(ModbusUdpConfiguration.class);

        String host = config.getHost();
        if (host == null) {
            throw new ModbusConfigurationException("host must be non-null!");
        }

        this.config = config;
        endpoint = new ModbusUDPSlaveEndpoint(host, config.getPort());

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
                "Endpoint '%s' has conflicting parameters: parameters of this thing (%s '%s') are different from some other thing's parameter. Ensure that all endpoints pointing to udp slave '%s:%s' have same parameters.",
                endpoint, thing.getUID(), this.thing.getLabel(),
                Optional.ofNullable(this.endpoint).map(e -> e.getAddress()).orElse("<null>"),
                Optional.ofNullable(this.endpoint).map(e -> String.valueOf(e.getPort())).orElse("<null>"));
    }

    @Override
    public int getSlaveId() throws EndpointNotInitializedException {
        ModbusUdpConfiguration localConfig = config;
        if (localConfig == null) {
            throw new EndpointNotInitializedException();
        }
        return localConfig.getId();
    }

    @Override
    public int getSubnetId() throws EndpointNotInitializedException {
        ModbusUdpConfiguration localConfig = config;
        if (localConfig == null) {
            throw new EndpointNotInitializedException();
        }
        return localConfig.getSubnetId();
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
