/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.modbus.sunspec.internal.handler;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecBindingConstants.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.sunspec.internal.SunSpecConfiguration;
import org.openhab.binding.modbus.sunspec.internal.detector.ModelBlock;
import org.openhab.io.transport.modbus.BasicModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractSunSpecHandler} is the base class for any sunspec handlers
 * Common things like setting up the connection, parsing the configuration etc
 * are handler here
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
public abstract class AbstractSunSpecHandler extends BaseThingHandler {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractSunSpecHandler.class);

    /**
     * Configuration instance
     */
    @Nullable
    protected SunSpecConfiguration config;

    /**
     * This is the task used to poll the device
     */
    private volatile PollTask pollTask;

    /**
     * This is the slave endpoint we're connecting to
     */
    protected volatile ModbusSlaveEndpoint endpoint;

    /**
     * This is the slave id, we store this once initialization is complete
     */
    private volatile int slaveId;

    /**
     * Set to true after we're disposed
     */
    private volatile boolean disposed = false;

    /**
     * Reference to the modbus manager
     */
    protected Supplier<ModbusManager> managerRef;

    /**
     * Descriptor of the main block
     */
    protected ModelBlock mainBlock;

    /**
     * Instances of this handler should get a reference to the modbus managet
     *
     * @param thing the thing to handle
     * @param managerRef the modbus manager
     */
    public AbstractSunSpecHandler(Thing thing, Supplier<ModbusManager> managerRef) {
        super(thing);
        this.managerRef = managerRef;
    }

    /**
     * Handle incoming commands. This binding is read-only be default
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Currently we do not support any commands
    }

    /**
     * Initialization:
     * Load the config object
     * Connect to the slave bridge
     * Start the periodic polling
     */
    @Override
    public void initialize() {
        config = getConfigAs(SunSpecConfiguration.class);

        disposed = false;

        connectEndpoint();
        logger.debug("Initializing thing with properties: {}", thing.getProperties());

        if (endpoint == null || config == null || managerRef == null) {
            logger.debug("Invalid enpoint/config/manager ref for sunspec handler");
            return;
        }

        // Try properties first
        mainBlock = getAddressFromProperties();

        // Try the configuration second
        if (mainBlock == null) {
            mainBlock = getAddressFromConfig();
            if (mainBlock != null) {
                publishUniqueAddress(mainBlock);
            }
        }

        if (mainBlock == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "SunSpec item should either have the address and length configuration set or should been created by auto discovery");
            return;
        }

        registerPollTask();
    }

    /**
     * Load and parse configuration from the properties
     * These will be set by the auto discovery process
     */
    private ModelBlock getAddressFromProperties() {
        Map<String, String> properties = thing.getProperties();
        if (!properties.containsKey(PROPERTY_BLOCK_ADDRESS) || !properties.containsKey(PROPERTY_BLOCK_LENGTH)) {
            return null;
        }
        try {
            ModelBlock block = new ModelBlock();
            block.address = (int) Double.parseDouble(thing.getProperties().get(PROPERTY_BLOCK_ADDRESS));
            block.length = (int) Double.parseDouble(thing.getProperties().get(PROPERTY_BLOCK_LENGTH));
            return block;
        } catch (NumberFormatException ex) {
            logger.debug("Could not parse address and length properties, error: {}", ex.getMessage());
            return null;
        }
    }

    /**
     * Load configuration from main configuration
     */
    @SuppressWarnings("null")
    private ModelBlock getAddressFromConfig() {
        if (config == null) {
            return null;
        }
        if (config.getAddress() == null || config.getLength() == null) {
            return null;
        }
        ModelBlock block = new ModelBlock();
        block.address = config.getAddress();
        block.length = config.getLength();
        return block;
    }

    /**
     * Publish the unique address property if it has not been set before
     */
    private void publishUniqueAddress(@NonNull ModelBlock block) {
        Map<String, String> properties = getThing().getProperties();
        if (properties.containsKey(PROPERTY_UNIQUE_ADDRESS) && !properties.get(PROPERTY_UNIQUE_ADDRESS).isEmpty()) {
            logger.debug("Current unique address is: {}", properties.get(PROPERTY_UNIQUE_ADDRESS));
            return;
        }

        ModbusEndpointThingHandler handler = getEndpointThingHandler();
        if (handler == null) {
            return;
        }
        getThing().setProperty(PROPERTY_UNIQUE_ADDRESS, handler.getUID().getAsString() + ":" + block.address);
    }

    /**
     * Dispose the binding correctly
     */
    @Override
    public synchronized void dispose() {
        dispose(null);
    }

    /**
     * Dispose the binding correctly
     *
     * @param reason status detail to be set
     */
    private synchronized void dispose(ThingStatusDetail reason) {
        logger.debug("dispose()");
        // Mark handler as disposed as soon as possible to halt processing of callbacks
        disposed = true;
        unregisterPollTask();
        unregisterEndpoint();
        if (reason != null) {
            updateStatus(ThingStatus.OFFLINE, reason);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    /**
     * Returns the current slave id from the bridge
     */
    public int getSlaveId() {
        return slaveId;
    }

    /**
     * Get the endpoint handler from the bridge this handler is connected to
     * Checks that we're connected to the right type of bridge
     *
     * @return the endpoint handler or null if the bridge does not exist
     */
    private ModbusEndpointThingHandler getEndpointThingHandler() {
        Bridge bridge = getBridge();
        if (bridge == null) {
            logger.debug("Bridge is null");
            return null;
        }
        if (bridge.getStatus() != ThingStatus.ONLINE) {
            logger.debug("Bridge is not online");
            return null;
        }

        ThingHandler handler = bridge.getHandler();
        if (handler == null) {
            logger.debug("Bridge handler is null");
            return null;
        }

        if (handler instanceof ModbusEndpointThingHandler) {
            ModbusEndpointThingHandler slaveEndpoint = (ModbusEndpointThingHandler) handler;
            return slaveEndpoint;
        } else {
            logger.debug("Unexpected bridge handler: {}", handler);
            throw new IllegalStateException();
        }
    }

    /**
     * Get a reference to the modbus endpoint
     */
    @SuppressWarnings("null")
    private synchronized void connectEndpoint() {
        if (endpoint != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            throw new IllegalStateException("endpoint should be unregistered before registering a new one!");
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, String.format("Bridge '%s' is offline",
                    Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>")));
            logger.debug("No bridge handler available -- aborting init for {}", this);
            return;
        }

        try {
            slaveId = slaveEndpointThingHandler.getSlaveId();

            endpoint = slaveEndpointThingHandler.asSlaveEndpoint();
        } catch (EndpointNotInitializedException e) {
            // this will be handled below as endpoint remains null
        }

        if (endpoint == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, String.format(
                    "Bridge '%s' not completely initialized", Optional.ofNullable(getBridge()).map(b -> b.getLabel())));
            logger.debug("Bridge not initialized fully (no endpoint) -- aborting init for {}", this);
            return;
        }

    }

    /**
     * Remove the endpoint if exists
     */
    private synchronized void unregisterEndpoint() {
        if (endpoint == null) {
            return;
        }
        endpoint = null;
    }

    /**
     * Register poll task
     * This is where we set up our regular poller
     */
    @SuppressWarnings("null")
    private synchronized void registerPollTask() {
        if (pollTask != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            throw new IllegalStateException("pollTask should be unregistered before registering a new one!");
        }
        if (config == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }

        logger.debug("Setting up regular polling");

        BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(getSlaveId(),
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, mainBlock.address, mainBlock.length,
                config.getMaxTries());

        pollTask = new BasicPollTaskImpl(endpoint, request, new ModbusReadCallback() {

            @Override
            public void onRegisters(@Nullable ModbusReadRequestBlueprint request,
                    @Nullable ModbusRegisterArray registers) {

                if (registers == null) {
                    logger.info("Received empty register array on poll");
                    return;
                }

                handlePolledData(registers);

                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }

            }

            @Override
            public void onError(@Nullable ModbusReadRequestBlueprint request, @Nullable Exception error) {
                handleError(error);
            }

            @Override
            public void onBits(@Nullable ModbusReadRequestBlueprint request, @Nullable BitArray bits) {
                // don't care, we don't expect this result
            }
        });

        managerRef.get().registerRegularPoll(pollTask, config.getRefreshMillis(), 1000);
    }

    /**
     * This method should handle incoming poll data, and update the channels
     * with the values received
     */
    abstract protected void handlePolledData(@NonNull ModbusRegisterArray registers);

    @Override
    public synchronized void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        logger.debug("bridgeStatusChanged for {}. Reseting handler", this.getThing().getUID());
        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE && getThing().getStatus() == ThingStatus.OFFLINE) {
            initialize();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            dispose(ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }

    /**
     * Unregister poll task.
     *
     * No-op in case no poll task is registered, or if the initialization is incomplete.
     */
    @SuppressWarnings("null")
    private synchronized void unregisterPollTask() {
        logger.trace("unregisterPollTask()");
        if (pollTask == null) {
            return;
        }

        logger.debug("Unregistering polling from ModbusManager");
        managerRef.get().unregisterRegularPoll(pollTask);

        pollTask = null;

    }

    /**
     * Handle errors received during communication
     */
    protected void handleError(@Nullable Exception error) {
        // Ignore all incoming data and errors if configuration is not correct
        if (hasConfigurationError() || disposed) {
            return;
        }
        String msg = "";
        String cls = "";
        if (error != null) {
            cls = error.getClass().getName();
            msg = error.getMessage();
        }
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                String.format("Error with read: %s: %s", cls, msg));
    }

    /**
     * Returns true, if we're in a CONFIGURATION_ERROR state
     *
     * @return
     */
    protected boolean hasConfigurationError() {
        ThingStatusInfo statusInfo = getThing().getStatusInfo();
        return statusInfo.getStatus() == ThingStatus.OFFLINE
                && statusInfo.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR;
    }

    /**
     * Reset communication status to ONLINE if we're in an OFFLINE state
     */
    protected void resetCommunicationError() {
        ThingStatusInfo statusInfo = thing.getStatusInfo();
        if (ThingStatus.OFFLINE.equals(statusInfo.getStatus())
                && ThingStatusDetail.COMMUNICATION_ERROR.equals(statusInfo.getStatusDetail())) {
            updateStatus(ThingStatus.ONLINE);
        }
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Short value, Short scaleFactor) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return getScaled(Long.valueOf(value), scaleFactor);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Integer value, Short scaleFactor) {
        if (value == null) {
            return UnDefType.UNDEF;
        }
        return getScaled(Long.valueOf(value), scaleFactor);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Long value, Short scaleFactor) {
        if (value == null) {
            return UnDefType.UNDEF;
        }

        if (scaleFactor == 1 || scaleFactor == null) {
            return new DecimalType(value);
        }
        return new DecimalType(BigDecimal.valueOf(value, scaleFactor * -1));
    }
}
