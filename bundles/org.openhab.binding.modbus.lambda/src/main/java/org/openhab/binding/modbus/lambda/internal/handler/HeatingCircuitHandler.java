/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.modbus.lambda.internal.handler;

import static org.openhab.binding.modbus.lambda.internal.LambdaBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.lambda.internal.HeatingCircuitConfiguration;
import org.openhab.binding.modbus.lambda.internal.dto.HeatingCircuitBlock;
import org.openhab.binding.modbus.lambda.internal.dto.HeatingCircuitReg50Block;
import org.openhab.binding.modbus.lambda.internal.parser.HeatingCircuitBlockParser;
import org.openhab.binding.modbus.lambda.internal.parser.HeatingCircuitReg50BlockParser;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.library.types.DecimalType;
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
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeatingCircuitHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
public class HeatingCircuitHandler extends BaseThingHandler {

    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(HeatingCircuitHandler.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = HeatingCircuitHandler.this.comms;
            if (mycomms != null) {
                mycomms.unregisterRegularPoll(task);
            }
            pollTask = null;
        }

        /**
         * Register poll task This is where we set up our regular poller
         */
        public synchronized void registerPollTask(int address, int length, ModbusReadFunctionCode readFunctionCode) {
            logger.debug("Setting up regular polling Address: {}", address);

            ModbusCommunicationInterface mycomms = HeatingCircuitHandler.this.comms;
            HeatingCircuitConfiguration myconfig = HeatingCircuitHandler.this.config;
            if (myconfig == null || mycomms == null) {
                throw new IllegalStateException("HeatingCircuit: registerPollTask called without proper configuration");
            }

            ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(), readFunctionCode, address,
                    length, myconfig.getMaxTries());

            long refreshMillis = myconfig.getRefreshMillis();

            pollTask = mycomms.registerRegularPoll(request, refreshMillis, 1000, result -> {
                result.getRegisters().ifPresent(this::handlePolledData);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }, HeatingCircuitHandler.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = HeatingCircuitHandler.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(HeatingCircuitHandler.class);

    /**
     * Configuration instance
     */
    protected @Nullable HeatingCircuitConfiguration config = null;

    /**
     * Parsers used to convert incoming raw messages into state blocks
     */

    private final HeatingCircuitBlockParser heatingcircuitBlockParser = new HeatingCircuitBlockParser();
    private final HeatingCircuitReg50BlockParser heatingcircuitReg50BlockParser = new HeatingCircuitReg50BlockParser();

    /**
     * These are the tasks used to poll the device
     */

    private volatile @Nullable AbstractBasePoller heatingcircuitPoller = null;
    private volatile @Nullable AbstractBasePoller heatingcircuitReg50Poller = null;
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
     */
    public HeatingCircuitHandler(Thing thing) {
        super(thing);
    }

    /**
     * @param address address of the value to be written on the modbus
     * 
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        HeatingCircuitConfiguration myconfig = HeatingCircuitHandler.this.config;
        ModbusCommunicationInterface mycomms = HeatingCircuitHandler.this.comms;

        if (myconfig == null || mycomms == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }
        // big endian byte ordering
        byte hi = (byte) (shortValue >> 8);
        byte lo = (byte) shortValue;
        ModbusRegisterArray data = new ModbusRegisterArray(hi, lo);

        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId, address, data,
                true, myconfig.getMaxTries());

        mycomms.submitOneTimeWrite(request, result -> {
            if (hasConfigurationError()) {
                return;
            }
            HeatingCircuitHandler.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            HeatingCircuitHandler.this.handleWriteError(failure);
        });
    }

    /**
     * @param command get the value of this command.
     * 
     * @return short the value of the command as short
     */
    private short getInt16Value(Command command) throws LambdaException {
        if (command instanceof QuantityType<?> quantityCommand) {
            QuantityType<?> c = quantityCommand.toUnit(WATT);
            if (c != null) {
                return c.shortValue();
            } else {
                throw new LambdaException("Unsupported unit");
            }
        }
        if (command instanceof DecimalType c) {
            return c.shortValue();
        }
        throw new LambdaException("Unsupported command type");
    }

    private short getScaledInt16Value(Command command) throws LambdaException {
        if (command instanceof QuantityType quantityCommand) {
            QuantityType<?> c = quantityCommand.toUnit(CELSIUS);
            if (c != null) {
                return (short) (c.doubleValue() * 10);
            } else {
                throw new LambdaException("Unsupported unit");
            }
        }
        if (command instanceof DecimalType c) {
            return (short) (c.doubleValue() * 10);
        }
        throw new LambdaException("Unsupported command type");
    }

    /**
     * Handle incoming commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (RefreshType.REFRESH == command) {
            String groupId = channelUID.getGroupId();
            if (groupId != null) {
                AbstractBasePoller poller;
                switch (groupId) {
                    case GROUP_HEATING_CIRCUIT:
                        poller = heatingcircuitPoller;
                        break;
                    case GROUP_HEATING_CIRCUIT_REG50:
                        poller = heatingcircuitReg50Poller;
                        break;
                    default:
                        poller = null;
                        break;
                }
                if (poller != null) {
                    logger.trace("HeatingCircuit: Polling initiated");
                    poller.poll();
                }
            }
        } else {
            try {
                if (GROUP_HEATING_CIRCUIT.equals(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_HEATING_CIRCUIT_ROOM_DEVICE_TEMPERATURE:
                            writeInt16(baseadress + 4, getScaledInt16Value(command));
                            break;
                        case CHANNEL_HEATING_CIRCUIT_SETPOINT_FLOW_LINE_TEMPERATURE:
                            writeInt16(baseadress + 5, getScaledInt16Value(command));
                            break;
                        case CHANNEL_HEATING_CIRCUIT_OPERATING_MODE:
                            writeInt16(baseadress + 6, getInt16Value(command));
                            break;

                    }
                }

                if (GROUP_HEATING_CIRCUIT_REG50.equals(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_HEATING_CIRCUIT_OFFSET_FLOW_LINE_TEMPERATURE:
                            writeInt16(reg50baseadress, getScaledInt16Value(command));
                            break;
                        case CHANNEL_HEATING_CIRCUIT_ROOM_HEATING_TEMPERATURE:
                            writeInt16(reg50baseadress + 1, getScaledInt16Value(command));
                            break;
                        case CHANNEL_HEATING_CIRCUIT_ROOM_COOLING_TEMPERATURE:
                            writeInt16(reg50baseadress + 2, getScaledInt16Value(command));
                            break;

                    }

                }

            } catch (LambdaException error) {
                if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
                    return;
                }
                String cls = error.getClass().getName();
                String msg = error.getMessage();

                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        String.format("Error with: %s: %s", cls, msg));
            }
        }
    }

    /**
     * Initialization: Load the config object of the block Connect to the slave
     * bridge Start the periodic polling
     */
    @Override
    public void initialize() {
        config = getConfigAs(HeatingCircuitConfiguration.class);

        logger.debug("Initializing thing with properties: {}", thing.getProperties());

        startUp();
    }

    /**
     * Adresses for the polling registers, used for reading and writing
     */
    private int baseadress;
    private int reg50baseadress;

    /**
     * This method starts the operation of this handler Connect to the slave bridge
     * Start the periodic polling1
     */
    private void startUp() {
        if (comms != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            return;
        }

        try {
            slaveId = slaveEndpointThingHandler.getSlaveId();

            comms = slaveEndpointThingHandler.getCommunicationInterface();
        } catch (EndpointNotInitializedException e) {
            logger.debug("HeatingCircuit: Error setting up SlaveId");

        }

        if (comms == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            logger.debug("CommunicationInterface of Heating Circuit is null, Thing & Bridge are offline");
            return;
        }

        if (config == null) {
            logger.debug("Invalid comms/config/manager ref for lambda heatingcircuit handler");
            return;
        }

        HeatingCircuitConfiguration myconfig = HeatingCircuitHandler.this.config;

        baseadress = 5000 + 100 * myconfig.getSubindex();
        reg50baseadress = baseadress + 50;

        if (heatingcircuitPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledHeatingData(registers);
                }
            };
            // neu: poller.registerPollTask(baseadress, 8, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            poller.registerPollTask(baseadress, 7, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            heatingcircuitPoller = poller;
        }
        if (heatingcircuitReg50Poller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledHeatingReg50Data(registers);
                }
            };

            poller.registerPollTask(reg50baseadress, 3, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            heatingcircuitReg50Poller = poller;
        }

        updateStatus(ThingStatus.UNKNOWN);
    }

    /**
     * Dispose the binding correctly
     */
    @Override
    public void dispose() {
        tearDown();
    }

    /**
     * Unregister the poll tasks and release the endpoint reference
     */
    private void tearDown() {
        AbstractBasePoller poller = heatingcircuitPoller;
        if (poller != null) {
            poller.unregisterPollTask();
            heatingcircuitPoller = null;
        }

        poller = heatingcircuitReg50Poller;
        if (poller != null) {
            poller.unregisterPollTask();
            heatingcircuitReg50Poller = null;
        }
        comms = null;
    }

    /**
     * Returns the current slave id from the bridge
     */
    public int getSlaveId() {
        return slaveId;
    }

    /**
     * Get the endpoint handler from the bridge this handler is connected to Checks
     * that we're connected to the right type of bridge
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

        if (handler instanceof ModbusEndpointThingHandler thingHandler) {
            return thingHandler;
        } else {
            throw new IllegalStateException("Unexpected bridge handler: " + handler.toString());
        }
    }

    protected State getScaled(Number value, Unit<?> unit, Double pow) {
        double factor = Math.pow(10, pow);
        return QuantityType.valueOf(value.doubleValue() * factor, unit);
    }

    protected State getUnscaled(Number value, Unit<?> unit) {
        return QuantityType.valueOf(value.doubleValue(), unit);
    }

    /**
     * Returns high value * 1000 + low value
     *
     * @param high the high value
     * 
     * @param low the low value
     * 
     * @return the scaled value as a DecimalType
     */
    protected State getEnergyQuantity(int high, int low) {
        double value = high * 1000 + low;
        return QuantityType.valueOf(value, KILOWATT_HOUR);
    }

    /**
     * These methods are called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */

    protected void handlePolledHeatingData(ModbusRegisterArray registers) {
        HeatingCircuitBlock block = heatingcircuitBlockParser.parse(registers);

        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_ERROR_NUMBER),
                new DecimalType(block.heatingcircuitErrorNumber));
        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_OPERATING_STATE),
                new DecimalType(block.heatingcircuitOperatingState));
        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_FLOW_LINE_TEMPERATURE),
                getScaled(block.heatingcircuitFlowLineTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_RETURN_LINE_TEMPERATURE),
                getScaled(block.heatingcircuitReturnLineTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_ROOM_DEVICE_TEMPERATURE),
                getScaled(block.heatingcircuitRoomDeviceTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_SETPOINT_FLOW_LINE_TEMPERATURE),
                getScaled(block.heatingcircuitSetpointFlowLineTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_OPERATING_MODE),
                new DecimalType(block.heatingcircuitOperatingMode));
        // neu: updateState(channelUID(GROUP_HEATING_CIRCUIT, CHANNEL_HEATING_CIRCUIT_TARGET_TEMPERATURE_FLOW_LINE),
        // neu: getScaled(block.heatingcircuitTargetTemperatureFlowLine, CELSIUS, -1.0));

        resetCommunicationError();
    }

    protected void handlePolledHeatingReg50Data(ModbusRegisterArray registers) {
        HeatingCircuitReg50Block block = heatingcircuitReg50BlockParser.parse(registers);

        // HeatingCircuit1Settting group

        updateState(channelUID(GROUP_HEATING_CIRCUIT_REG50, CHANNEL_HEATING_CIRCUIT_OFFSET_FLOW_LINE_TEMPERATURE),
                getScaled(block.heatingcircuitOffsetFlowLineTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_HEATING_CIRCUIT_REG50, CHANNEL_HEATING_CIRCUIT_ROOM_HEATING_TEMPERATURE),
                getScaled(block.heatingcircuitRoomHeatingTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_HEATING_CIRCUIT_REG50, CHANNEL_HEATING_CIRCUIT_ROOM_COOLING_TEMPERATURE),
                getScaled(block.heatingcircuitRoomCoolingTemperature, CELSIUS, -1.0));
        resetCommunicationError();
    }

    /**
     * @param bridgeStatusInfo
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        super.bridgeStatusChanged(bridgeStatusInfo);

        if (bridgeStatusInfo.getStatus() == ThingStatus.ONLINE) {
            startUp();
        } else if (bridgeStatusInfo.getStatus() == ThingStatus.OFFLINE) {
            tearDown();
        }
    }

    /**
     * Handle errors received during communication
     */
    protected void handleReadError(AsyncModbusFailure<ModbusReadRequestBlueprint> failure) {
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
     * Handle errors received during communication
     */
    protected void handleWriteError(AsyncModbusFailure<ModbusWriteRequestBlueprint> failure) {
        // Ignore all incoming data and errors if configuration is not correct
        if (hasConfigurationError() || getThing().getStatus() == ThingStatus.OFFLINE) {
            return;
        }
        String msg = failure.getCause().getMessage();
        String cls = failure.getCause().getClass().getName();
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                String.format("Error with write: %s: %s", cls, msg));
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
     * 
     * @param string the channel id in that group
     * 
     * @return the globally unique channel uid
     */
    ChannelUID channelUID(String group, String id) {
        return new ChannelUID(getThing().getUID(), group, id);
    }
}
