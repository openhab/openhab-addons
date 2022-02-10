/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal.handler;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.sungrow.internal.SungrowConfiguration;
import org.openhab.binding.modbus.sungrow.internal.dto.ModelBlock;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AbstractSungrowHandler} is the base class for any sungrow handlers
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
 * @author Ferdinand Schwenk - reused for sungrow bundle
 */
@NonNullByDefault
public abstract class AbstractSungrowHandler extends BaseThingHandler {

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(AbstractSungrowHandler.class);

    /**
     * Configuration instance
     */
    protected @Nullable SungrowConfiguration config = null;

    /**
     * Storage for the tasks used to poll the device
     */
    private Map<ModelBlock, PollTask> pollTasks = new HashMap<>();

    /**
     * Communication interface to the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusCommunicationInterface comms = null;

    /**
     * This is the slave id, we store this once initialization is complete
     */
    private volatile int slaveId;

    /**
     * Instances of this handler should get a reference to the modbus manager
     *
     * @param thing the thing to handle
     * @param managerRef the modbus manager
     */
    public AbstractSungrowHandler(Thing thing) {
        super(thing);
    }

    /**
     * Handle incoming commands. This binding is read-only by default
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Currently we do not support any commands
    }

    /**
     * This methodes should handle incoming poll data, and update the channels
     * with the values received
     */
    protected abstract void handlePolled5kData(ModbusRegisterArray registers);

    protected abstract void handlePolled13kData(ModbusRegisterArray registers);

    /**
     * Initialization:
     * Load the config object of the block
     * Connect to the slave bridge
     * Start the periodic polling
     */
    @Override
    public void initialize() {
        config = getConfigAs(SungrowConfiguration.class);
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

        if (comms == null || config == null) {
            logger.debug("Invalid endpoint/config/manager ref for sungrow handler");
            return;
        }

        ModelBlock block5k = new ModelBlock();
        block5k.address = 5000 - 1;
        block5k.length = 5036 - 5000 + 1;
        ModbusReadFunctionCode block5kReadFunction = ModbusReadFunctionCode.READ_INPUT_REGISTERS;

        if (!pollTasks.containsKey(block5k)) {
            updateStatus(ThingStatus.UNKNOWN);
            registerPollTask(block5k, block5kReadFunction, this::handlePolled5kData);
        }

        // @Nullable
        ModelBlock block13k = new ModelBlock();
        block13k.address = 13000 - 1;
        block13k.length = 13047 - 13000 + 1;
        ModbusReadFunctionCode block13kReadFunction = ModbusReadFunctionCode.READ_INPUT_REGISTERS;

        if (!pollTasks.containsKey(block13k)) {
            updateStatus(ThingStatus.UNKNOWN);
            registerPollTask(block13k, block13kReadFunction, this::handlePolled13kData);
        }

        if (pollTasks.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Sungrow item should either have the address and length configuration set or should been created by auto discovery");
            return;
        }
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
        if (comms != null) {
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
            comms = slaveEndpointThingHandler.getCommunicationInterface();
        } catch (EndpointNotInitializedException e) {
            // this will be handled below as endpoint remains null
        }

        if (comms == null) {
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
        // Comms will be close()'d by endpoint thing handler
        comms = null;
    }

    /**
     * Register poll task
     * This is where we set up our regular poller
     */
    private synchronized void registerPollTask(ModelBlock block, ModbusReadFunctionCode mbReadFunction,
            Consumer<ModbusRegisterArray> action) {
        if (pollTasks.containsKey(block)) {
            logger.warn("PollTask for block {} should be unregistered before registering a new one!", block.toString());
        }
        @Nullable
        ModbusCommunicationInterface mycomms = comms;
        @Nullable
        SungrowConfiguration myconfig = config;
        if (myconfig == null || mycomms == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }

        logger.debug("Setting up regular polling {} regs from address {}", block.length, block.address);

        ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(), mbReadFunction, block.address,
                block.length, myconfig.maxTries);

        long refreshMillis = myconfig.getRefreshMillis();
        PollTask task = mycomms.registerRegularPoll(request, refreshMillis, 1000, result -> {
            logger.trace("New ReadResult: {}", result.toString());
            result.getRegisters().ifPresent(action);
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            }
        }, this::handleError);
        pollTasks.put(block, task);
    }

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
        ModbusCommunicationInterface mycomms = comms;
        if (mycomms == null) {
            return;
        }
        @Nullable
        PollTask task = null;
        while (!pollTasks.isEmpty()) {
            ModelBlock block = pollTasks.keySet().iterator().next();
            task = pollTasks.get(block);
            logger.debug("Unregistering polling for block {} from ModbusManager", block.toString());
            if (task != null) {
                mycomms.unregisterRegularPoll(task);
            }
            pollTasks.remove(block);
            task = null;
        }
    }

    /**
     * Handle errors received during communication
     */
    protected void handleError(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
        // Ignore all incoming data and errors if configuration is not correct
        if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        String msg = failure.getCause().getMessage();
        String cls = failure.getCause().getClass().getName();
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
        if (scaleFactor == 0) {
            return new QuantityType<>(value.longValue(), unit);
        }
        return new QuantityType<>(BigDecimal.valueOf(value.longValue(), scaleFactor * -1), unit);
    }
}
