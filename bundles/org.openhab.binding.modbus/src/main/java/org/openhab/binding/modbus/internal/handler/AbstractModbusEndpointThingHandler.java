/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.internal.ModbusConfigurationException;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusManagerListener;
import org.openhab.io.transport.modbus.endpoint.EndpointPoolConfiguration;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class for Modbus Slave endpoint thing handlers
 *
 * @author Sami Salonen - Initial contribution
 *
 * @param <E> endpoint class
 * @param <C> config class
 */
@NonNullByDefault
public abstract class AbstractModbusEndpointThingHandler<E extends ModbusSlaveEndpoint, C> extends BaseBridgeHandler
        implements ModbusManagerListener, ModbusEndpointThingHandler {

    @Nullable
    protected volatile C config;
    @Nullable
    protected volatile E endpoint;
    protected Supplier<ModbusManager> managerRef;
    @Nullable
    protected volatile EndpointPoolConfiguration poolConfiguration;
    private final Logger logger = LoggerFactory.getLogger(AbstractModbusEndpointThingHandler.class);

    public AbstractModbusEndpointThingHandler(Bridge bridge, Supplier<ModbusManager> managerRef) {
        super(bridge);
        this.managerRef = managerRef;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        synchronized (this) {
            logger.trace("Initializing {} from status {}", this.getThing().getUID(), this.getThing().getStatus());
            if (this.getThing().getStatus().equals(ThingStatus.ONLINE)) {
                // If the bridge was online then first change it to offline.
                // this ensures that children will be notified about the change
                updateStatus(ThingStatus.OFFLINE);
            }
            try {
                configure();
                @Nullable
                E endpoint = this.endpoint;
                if (endpoint == null) {
                    throw new IllegalArgumentException("endpoint null after configuration!");
                }
                managerRef.get().addListener(this);
                managerRef.get().setEndpointPoolConfiguration(endpoint, poolConfiguration);
                updateStatus(ThingStatus.ONLINE);
            } catch (ModbusConfigurationException e) {
                logger.debug("Exception during initialization", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, String.format(
                        "Exception during initialization: %s (%s)", e.getMessage(), e.getClass().getSimpleName()));
            } finally {
                logger.trace("initialize() of thing {} '{}' finished", thing.getUID(), thing.getLabel());
            }
        }
    }

    @Override
    public void dispose() {
        managerRef.get().removeListener(this);
    }

    @Override
    public @Nullable ModbusSlaveEndpoint asSlaveEndpoint() {
        return endpoint;
    }

    @Override
    public Supplier<ModbusManager> getManagerRef() {
        return managerRef;
    }

    @Override
    public void onEndpointPoolConfigurationSet(ModbusSlaveEndpoint otherEndpoint,
            @Nullable EndpointPoolConfiguration otherPoolConfiguration) {
        synchronized (this) {
            if (endpoint == null) {
                return;
            }
            EndpointPoolConfiguration poolConfiguration = this.poolConfiguration;
            if (poolConfiguration != null && otherEndpoint.equals(this.endpoint)
                    && !poolConfiguration.equals(otherPoolConfiguration)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        formatConflictingParameterError(otherPoolConfiguration));
            }
        }
    }

    @Override
    public abstract int getSlaveId();

    /**
     * Must be overriden by subclasses to initialize config, endpoint, and poolConfiguration
     */
    protected abstract void configure() throws ModbusConfigurationException;

    /**
     * Format error message in case some other endpoint has been configured with different
     * {@link EndpointPoolConfiguration}
     *
     * @param otherPoolConfig
     * @return
     */
    protected abstract String formatConflictingParameterError(@Nullable EndpointPoolConfiguration otherPoolConfig);
}
