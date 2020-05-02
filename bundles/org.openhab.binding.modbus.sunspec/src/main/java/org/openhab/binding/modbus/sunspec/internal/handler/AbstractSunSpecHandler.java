/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sunspec.internal.handler;

import static org.openhab.binding.modbus.sunspec.internal.SunSpecConstants.*;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.QuantityType;
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
import org.openhab.binding.modbus.sunspec.internal.dto.ModelBlock;
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
 * Common things are handled here:
 *
 * - loads the configuration either from the configuration file or
 * from the properties that have been set by the auto discovery
 * - sets up a regular poller to the device
 * - handles incoming messages from the device:
 * - common properties are parsed and published
 * - other values are submitted to child implementations
 * - handles disposal of the device by removing any handlers
 * - implements some tool methods
 *
 * @author Nagy Attila Gabor - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractSunSpecHandler extends BaseThingHandler {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractSunSpecHandler.class);

    /**
     * Configuration instance
     */
    protected @Nullable SunSpecConfiguration config = null;

    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable PollTask pollTask = null;

    /**
     * This is the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusSlaveEndpoint endpoint = null;

    /**
     * This is the slave id, we store this once initialization is complete
     */
    private volatile int slaveId;

    /**
     * Reference to the modbus manager
     */
    protected final ModbusManager managerRef;

    /**
     * Instances of this handler should get a reference to the modbus manager
     *
     * @param thing the thing to handle
     * @param managerRef the modbus manager
     */
    public AbstractSunSpecHandler(Thing thing, ModbusManager managerRef) {
        super(thing);
        this.managerRef = managerRef;
    }

    /**
     * Handle incoming commands. This binding is read-only by default
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Currently we do not support any commands
    }

    /**
     * Initialization:
     * Load the config object of the block
     * Connect to the slave bridge
     * Start the periodic polling
     */
    @Override
    public void initialize() {
        config = getConfigAs(SunSpecConfiguration.class);
        logger.debug("Initializing thing with properties: {}", thing.getProperties());

        startUp();
    }

    /*
     * This method starts the operation of this handler
     * Load the config object of the block
     * Connect to the slave bridge
     * Start the periodic polling
     */
    private void startUp() {

        connectEndpoint();

        if (endpoint == null || config == null) {
            logger.debug("Invalid endpoint/config/manager ref for sunspec handler");
            return;
        }

        if (pollTask != null) {
            return;
        }

        // Try properties first
        @Nullable
        ModelBlock mainBlock = getAddressFromProperties();

        if (mainBlock == null) {
            mainBlock = getAddressFromConfig();
        }

        if (mainBlock != null) {
            publishUniqueAddress(mainBlock);
            updateStatus(ThingStatus.UNKNOWN);
            registerPollTask(mainBlock);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "SunSpec item should either have the address and length configuration set or should been created by auto discovery");
            return;
        }
    }

    /**
     * Load and parse configuration from the properties
     * These will be set by the auto discovery process
     */
    private @Nullable ModelBlock getAddressFromProperties() {
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
    private @Nullable ModelBlock getAddressFromConfig() {
        @Nullable
        SunSpecConfiguration myconfig = config;
        if (myconfig == null) {
            return null;
        }
        ModelBlock block = new ModelBlock();
        block.address = myconfig.address;
        block.length = myconfig.length;
        return block;
    }

    /**
     * Publish the unique address property if it has not been set before
     */
    private void publishUniqueAddress(ModelBlock block) {
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
    public void dispose() {
        tearDown();
    }

    /**
     * Unregister the poll task and release the endpoint reference
     */
    private void tearDown() {
        unregisterPollTask();
        unregisterEndpoint();
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
    private @Nullable ModbusEndpointThingHandler getEndpointThingHandler() {
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
            return null;
        }
    }

    /**
     * Get a reference to the modbus endpoint
     */
    private void connectEndpoint() {
        if (endpoint != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' is offline", label));
            logger.debug("No bridge handler available -- aborting init for {}", label);
            return;
        }

        try {
            slaveId = slaveEndpointThingHandler.getSlaveId();

            endpoint = slaveEndpointThingHandler.asSlaveEndpoint();
        } catch (EndpointNotInitializedException e) {
            // this will be handled below as endpoint remains null
        }

        if (endpoint == null) {
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' not completely initialized", label));
            logger.debug("Bridge not initialized fully (no endpoint) -- aborting init for {}", this);
            return;
        }
    }

    /**
     * Remove the endpoint if exists
     */
    private void unregisterEndpoint() {
        endpoint = null;
    }

    /**
     * Register poll task
     * This is where we set up our regular poller
     */
    private synchronized void registerPollTask(ModelBlock mainBlock) {
        if (pollTask != null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            throw new IllegalStateException("pollTask should be unregistered before registering a new one!");
        }
        @Nullable
        ModbusSlaveEndpoint myendpoint = endpoint;
        @Nullable
        SunSpecConfiguration myconfig = config;
        if (myconfig == null || myendpoint == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }

        logger.debug("Setting up regular polling");

        BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(getSlaveId(),
                ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS, mainBlock.address, mainBlock.length, myconfig.maxTries);

        pollTask = new BasicPollTaskImpl(myendpoint, request, new ModbusReadCallback() {

            @Override
            public void onRegisters(@Nullable ModbusReadRequestBlueprint request,
                    @Nullable ModbusRegisterArray registers) {
                if (registers == null) {
                    logger.debug("Received empty register array on poll");
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

        long refreshMillis = myconfig.getRefreshMillis();
        @Nullable
        PollTask task = pollTask;
        if (task != null) {
            managerRef.registerRegularPoll(task, refreshMillis, 1000);
        }
    }

    /**
     * This method should handle incoming poll data, and update the channels
     * with the values received
     */
    protected abstract void handlePolledData(ModbusRegisterArray registers);

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        logger.debug("Thing status changed to {}", this.getThing().getStatus().name());
        if (getThing().getStatus() == ThingStatus.ONLINE) {
            startUp();
        } else if (getThing().getStatus() == ThingStatus.OFFLINE) {
            tearDown();
        }
    }

    /**
     * Unregister poll task.
     *
     * No-op in case no poll task is registered, or if the initialization is incomplete.
     */
    private synchronized void unregisterPollTask() {
        @Nullable
        PollTask task = pollTask;
        if (task == null) {
            return;
        }
        logger.debug("Unregistering polling from ModbusManager");
        managerRef.unregisterRegularPoll(task);

        pollTask = null;
    }

    /**
     * Handle errors received during communication
     */
    protected void handleError(@Nullable Exception error) {
        // Ignore all incoming data and errors if configuration is not correct
        if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
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
     * Returns the channel UID for the specified group and channel id
     *
     * @param string the channel group
     * @param string the channel id in that group
     * @return the globally unique channel uid
     */
    ChannelUID channelUID(String group, String id) {
        return new ChannelUID(getThing().getUID(), group, id);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Optional<? extends Number> value, Optional<Short> scaleFactor, Unit<?> unit) {
        if (!value.isPresent() || !scaleFactor.isPresent()) {
            return UnDefType.UNDEF;
        }
        return getScaled(value.get().longValue(), scaleFactor.get(), unit);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Optional<? extends Number> value, Short scaleFactor, Unit<?> unit) {
        return getScaled(value, Optional.of(scaleFactor), unit);
    }

    /**
     * Returns value multiplied by the 10 on the power of scaleFactory
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor to use (may be negative)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Number value, Short scaleFactor, Unit<?> unit) {
        if (scaleFactor == 1) {
            return new QuantityType<>(value.longValue(), unit);
        }
        return new QuantityType<>(BigDecimal.valueOf(value.longValue(), scaleFactor * -1), unit);
    }
}
