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
import org.openhab.binding.modbus.internal.config.ModbusSerialConfiguration;
import org.openhab.core.io.transport.modbus.ModbusManager;
import org.openhab.core.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.core.io.transport.modbus.endpoint.ModbusSerialSlaveEndpoint;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerService;

/**
 * Endpoint thing handler for serial slaves
 *
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusSerialThingHandler
        extends AbstractModbusEndpointThingHandler<ModbusSerialSlaveEndpoint, ModbusSerialConfiguration> {

    public ModbusSerialThingHandler(Bridge bridge, ModbusManager manager) {
        super(bridge, manager);
    }

    @Override
    protected void configure() throws ModbusConfigurationException {
        ModbusSerialConfiguration config = getConfigAs(ModbusSerialConfiguration.class);
        String port = config.getPort();
        int baud = config.getBaud();
        String flowControlIn = config.getFlowControlIn();
        String flowControlOut = config.getFlowControlOut();
        String stopBits = config.getStopBits();
        String parity = config.getParity();
        String encoding = config.getEncoding();
        if (port == null || flowControlIn == null || flowControlOut == null || stopBits == null || parity == null
                || encoding == null) {
            throw new ModbusConfigurationException(
                    "port, baud, flowControlIn, flowControlOut, stopBits, parity, encoding all must be non-null!");
        }

        this.config = config;

        EndpointPoolConfiguration poolConfiguration = new EndpointPoolConfiguration();
        this.poolConfiguration = poolConfiguration;
        poolConfiguration.setConnectMaxTries(config.getConnectMaxTries());
        poolConfiguration.setAfterConnectionDelayMillis(config.getAfterConnectionDelayMillis());
        poolConfiguration.setConnectTimeoutMillis(config.getConnectTimeoutMillis());
        poolConfiguration.setInterTransactionDelayMillis(config.getTimeBetweenTransactionsMillis());

        // Never reconnect serial connections "automatically"
        poolConfiguration.setInterConnectDelayMillis(1000);
        poolConfiguration.setReconnectAfterMillis(-1);

        endpoint = new ModbusSerialSlaveEndpoint(port, baud, flowControlIn, flowControlOut, config.getDataBits(),
                stopBits, parity, encoding, config.isEcho(), config.getReceiveTimeoutMillis());
    }

    /**
     * Return true if auto discovery is enabled in the config
     */
    @Override
    public boolean isDiscoveryEnabled() {
        if (config != null) {
            return config.isDiscoveryEnabled();
        } else {
            return false;
        }
    }

    @SuppressWarnings("null") // Since endpoint in Optional.map cannot be null
    @Override
    protected String formatConflictingParameterError() {
        return String.format(
                "Endpoint '%s' has conflicting parameters: parameters of this thing (%s '%s') are different from some other thing's parameter. Ensure that all endpoints pointing to serial port '%s' have same parameters.",
                endpoint, thing.getUID(), this.thing.getLabel(),
                Optional.ofNullable(this.endpoint).map(e -> e.getPortName()).orElse("<null>"));
    }

    @Override
    public int getSlaveId() throws EndpointNotInitializedException {
        ModbusSerialConfiguration config = this.config;
        if (config == null) {
            throw new EndpointNotInitializedException();
        }
        return config.getId();
    }

    @Override
    public ThingUID getUID() {
        return getThing().getUID();
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(ModbusEndpointDiscoveryService.class);
    }
}
