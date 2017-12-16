/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusManagerListener;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.slf4j.LoggerFactory;

/**
 * Base class for Modbus Slave endpoint thing handlers
 *
 * @author Sami Salonen
 *
 * @param <E> endpoint class
 * @param <C> config class
 */
public abstract class AbstractModbusEndpointThingHandler<E extends ModbusSlaveEndpoint, C> extends BaseBridgeHandler
        implements ModbusManagerListener, ModbusEndpointThingHandler {

    protected volatile C config;
    protected volatile E endpoint;

    protected @NonNull Supplier<ModbusManager> managerRef;
    protected volatile EndpointPoolConfiguration poolConfiguration;

    @SuppressWarnings("null")
    public AbstractModbusEndpointThingHandler(@NonNull Bridge bridge, @NonNull Supplier<ModbusManager> managerRef) {
        super(bridge);
        this.managerRef = managerRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    /**
     * Must be overriden by subclasses to initialize config, endpoint, and poolConfiguration
     */
    protected abstract void configure();

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("null")
    @Override
    public void initialize() {
        synchronized (this) {
            LoggerFactory.getLogger(this.getClass()).trace("Initializing {} from status {}", this.getThing().getUID(),
                    this.getThing().getStatus());
            try {
                configure();
                managerRef.get().addListener(this);
                managerRef.get().setEndpointPoolConfiguration(endpoint, poolConfiguration);
                updateStatus(ThingStatus.ONLINE);
            } catch (Throwable e) {
                LoggerFactory.getLogger(this.getClass()).error("Exception during initialization", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                        "Exception during initialization: %s (%s)", e.getMessage(), e.getClass().getSimpleName()));
            } finally {
                LoggerFactory.getLogger(this.getClass()).trace("initialize() of thing {} '{}' finished", thing.getUID(),
                        thing.getLabel());
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        managerRef.get().removeListener(this);
        updateStatus(ThingStatus.OFFLINE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ModbusSlaveEndpoint asSlaveEndpoint() {
        return endpoint;
    }

    @Override
    public abstract int getSlaveId();

    /**
     * Format error message in case some other endpoint has been configured with different
     * {@link EndpointPoolConfiguration}
     *
     * @param otherPoolConfig
     * @return
     */
    protected abstract String formatConflictingParameterError(EndpointPoolConfiguration otherPoolConfig);

    /**
     * {@inheritDoc}
     */
    @Override
    public Supplier<ModbusManager> getManagerRef() {
        return managerRef;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onEndpointPoolConfigurationSet(ModbusSlaveEndpoint otherEndpoint,
            EndpointPoolConfiguration otherPoolConfiguration) {
        synchronized (this) {
            if (endpoint == null) {
                return;
            }
            if (this.poolConfiguration != null && otherEndpoint.equals(this.endpoint)
                    && !this.poolConfiguration.equals(otherPoolConfiguration)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        formatConflictingParameterError(otherPoolConfiguration));
            }
        }
    }

}