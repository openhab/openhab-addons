/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.handler;

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.Bridge;
import org.openhab.binding.modbus.internal.config.ModbusSerialConfiguration;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSerialSlaveEndpoint;

/**
 * Endpoint thing handler for serial slaves
 *
 * @author Sami Salonen - Initial contribution
 */
public class ModbusSerialThingHandler
        extends AbstractModbusEndpointThingHandler<ModbusSerialSlaveEndpoint, ModbusSerialConfiguration> {

    public ModbusSerialThingHandler(@NonNull Bridge bridge, @NonNull Supplier<ModbusManager> managerRef) {
        super(bridge, managerRef);
    }

    @Override
    protected void configure() {
        config = getConfigAs(ModbusSerialConfiguration.class);

        poolConfiguration = new EndpointPoolConfiguration();
        poolConfiguration.setConnectMaxTries(config.getConnectMaxTries());
        poolConfiguration.setConnectTimeoutMillis(config.getConnectTimeoutMillis());
        poolConfiguration.setInterTransactionDelayMillis(config.getTimeBetweenTransactionsMillis());

        // Never reconnect serial connections "automatically"
        poolConfiguration.setInterConnectDelayMillis(1000);
        poolConfiguration.setReconnectAfterMillis(-1);

        endpoint = new ModbusSerialSlaveEndpoint(config.getPort(), config.getBaud(), config.getFlowControlIn(),
                config.getFlowControlOut(), config.getDataBits(), config.getStopBits(), config.getParity(),
                config.getEncoding(), config.isEcho(), config.getReceiveTimeoutMillis());
    }

    @Override
    protected String formatConflictingParameterError(EndpointPoolConfiguration otherPoolConfig) {
        return String.format(
                "Endpoint '%s' has conflicting parameters: parameters of this thing (%s '%s') %s are different from some other things parameter: %s. Ensure that all endpoints pointing to serial port '%s' have same parameters.",
                endpoint, thing.getUID(), this.thing.getLabel(), this.poolConfiguration, otherPoolConfig,
                this.endpoint.getPortName());
    }

    @Override
    public int getSlaveId() {
        if (config == null) {
            throw new IllegalStateException("Poller not configured, but slave id is queried!");
        }
        return config.getId();
    }

}
