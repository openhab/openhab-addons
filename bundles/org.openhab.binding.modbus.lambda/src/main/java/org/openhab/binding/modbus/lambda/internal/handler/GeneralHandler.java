/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.lambda.internal.GeneralConfiguration;
import org.openhab.binding.modbus.lambda.internal.dto.AmbientBlock;
import org.openhab.binding.modbus.lambda.internal.dto.EManagerBlock;
import org.openhab.binding.modbus.lambda.internal.parser.AmbientBlockParser;
import org.openhab.binding.modbus.lambda.internal.parser.EManagerBlockParser;
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
 * The {@link GeneralHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus.
 *
 * @author Paul Frank - Initial contribution
 */
@NonNullByDefault
public class GeneralHandler extends BaseThingHandler {

    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(GeneralHandler.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = GeneralHandler.this.comms;
            if (mycomms != null) {
                mycomms.unregisterRegularPoll(task);
            }
            pollTask = null;
        }

        /**
         * Register poll task This is where we set up our regular poller
         */
        public synchronized void registerPollTask(int address, int length, ModbusReadFunctionCode readFunctionCode) {
            // logger.debug("Setting up regular polling Address: {}", address);

            ModbusCommunicationInterface mycomms = GeneralHandler.this.comms;
            GeneralConfiguration myconfig = GeneralHandler.this.config;
            if (myconfig == null || mycomms == null) {
                throw new IllegalStateException("registerPollTask called without proper configuration");
            }

            ModbusReadRequestBlueprint request = new ModbusReadRequestBlueprint(getSlaveId(), readFunctionCode, address,
                    length, myconfig.getMaxTries());

            long refreshMillis = myconfig.getRefreshMillis();

            pollTask = mycomms.registerRegularPoll(request, refreshMillis, 1000, result -> {
                result.getRegisters().ifPresent(this::handlePolledData);
                if (getThing().getStatus() != ThingStatus.ONLINE) {
                    updateStatus(ThingStatus.ONLINE);
                }
            }, GeneralHandler.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = GeneralHandler.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(GeneralHandler.class);

    /**
     * Configuration instance
     */
    protected @Nullable GeneralConfiguration config = null;
    /**
     * Parser used to convert incoming raw messages into system blocks
     * private final SystemInfromationBlockParser systemInformationBlockParser = new SystemInfromationBlockParser();
     */
    /**
     * Parsers used to convert incoming raw messages into state blocks
     */
    private final AmbientBlockParser ambientBlockParser = new AmbientBlockParser();
    private final EManagerBlockParser emanagerBlockParser = new EManagerBlockParser();

    /**
     * These are the tasks used to poll the device
     */
    private volatile @Nullable AbstractBasePoller ambientPoller = null;
    private volatile @Nullable AbstractBasePoller emanagerPoller = null;

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
    public GeneralHandler(Thing thing) {
        super(thing);
    }

    /**
     * @param address address of the value to be written on the modbus
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        // logger.trace("General: writeInt16: Es wird geschrieben, Adresse: {} Wert: {}", address, shortValue);
        GeneralConfiguration myconfig = GeneralHandler.this.config;
        ModbusCommunicationInterface mycomms = GeneralHandler.this.comms;

        if (myconfig == null || mycomms == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }
        // big endian byte ordering
        byte hi = (byte) (shortValue >> 8);
        byte lo = (byte) shortValue;
        ModbusRegisterArray data = new ModbusRegisterArray(hi, lo);

        // logger.trace("General: hi: {}, lo: {}", hi, lo);
        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId, address, data,
                true, myconfig.getMaxTries());

        mycomms.submitOneTimeWrite(request, result -> {
            if (hasConfigurationError()) {
                return;
            }
            // logger.trace("General: Successful write, matching request {}", request);
            GeneralHandler.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            GeneralHandler.this.handleWriteError(failure);
            // logger.trace("General: Unsuccessful write, matching request {}", request);
        });
    }

    /**
     * @param command get the value of this command.
     * @return short the value of the command as short
     */
    private short getInt16Value(Command command) throws LambdaException {
        if (command instanceof QuantityType quantityCommand) {
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
        // logger.trace("General: handleCommand, channelUID: {} command {} ", channelUID, command);
        if (RefreshType.REFRESH == command) {
            String groupId = channelUID.getGroupId();
            if (groupId != null) {
                AbstractBasePoller poller;
                switch (groupId) {
                    case GROUP_GENERAL_AMBIENT:
                        poller = ambientPoller;
                        break;
                    case GROUP_GENERAL_EMANAGER:
                        poller = emanagerPoller;
                        break;
                    default:
                        poller = null;
                        break;
                }
                if (poller != null) {
                    // logger.trace("General Es wird gepollt }");
                    poller.poll();
                }
            }
        } else {

            try {

                if (GROUP_GENERAL_AMBIENT.equals(channelUID.getGroupId())) {

                    // logger.trace(": im AMBIENT channelUID {} ", channelUID.getIdWithoutGroup());
                    switch (channelUID.getIdWithoutGroup()) {

                        case CHANNEL_ACTUAL_AMBIENT_TEMPERATURE:

                            // logger.trace("write Actual Ambient Temperature command: {}", command);
                            writeInt16(2, getInt16Value(command));
                            break;

                    }
                }

                // logger.trace("write EMANAGER");
                if (GROUP_GENERAL_EMANAGER.equals(channelUID.getGroupId())) {

                    // logger.trace("General: im EMANAGER channelUID {} ", channelUID.getIdWithoutGroup());
                    switch (channelUID.getIdWithoutGroup()) {

                        case CHANNEL_ACTUAL_POWER:

                            // logger.trace("336 command: {}", command);
                            writeInt16(102, getInt16Value(command));
                            break;

                    }
                }
            }

            catch (LambdaException error) {
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
        config = getConfigAs(GeneralConfiguration.class);

        logger.debug("Initializing thing with properties: {}", thing.getProperties());

        startUp();
    }

    /*
     * This method starts the operation of this handler Connect to the slave bridge
     * Start the periodic polling1
     */
    private void startUp() {
        if (comms != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is offline");
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
            return;
        }

        if (config == null) {
            logger.debug("Invalid comms/config/manager ref for lambda general handler");
            return;
        }

        if (ambientPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledAmbientData(registers);
                }
            };
            poller.registerPollTask(0, 5, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            ambientPoller = poller;
        }

        if (emanagerPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledEManagerData(registers);
                }
            };
            poller.registerPollTask(100, 5, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            emanagerPoller = poller;
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

        AbstractBasePoller poller = ambientPoller;
        if (poller != null) {
            poller.unregisterPollTask();
            ambientPoller = null;
        }

        poller = emanagerPoller;
        if (poller != null) {
            poller.unregisterPollTask();
            emanagerPoller = null;
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
        // logger.trace("General: value: {}", value.intValue());
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
     * @param low the low valze
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
    protected void handlePolledAmbientData(ModbusRegisterArray registers) {
        // logger.trace("Ambient block received, size: {}", registers.size());

        // Ambient group
        AmbientBlock block = ambientBlockParser.parse(registers);

        updateState(channelUID(GROUP_GENERAL_AMBIENT, CHANNEL_AMBIENT_ERROR_NUMBER),
                new DecimalType(block.ambientErrorNumber));
        updateState(channelUID(GROUP_GENERAL_AMBIENT, CHANNEL_AMBIENT_OPERATING_STATE),
                new DecimalType(block.ambientOperatingState));
        updateState(channelUID(GROUP_GENERAL_AMBIENT, CHANNEL_ACTUAL_AMBIENT_TEMPERATURE),
                getScaled(block.actualAmbientTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_GENERAL_AMBIENT, CHANNEL_AVERAGE_AMBIENT_TEMPERATURE),
                getScaled(block.averageAmbientTemperature, CELSIUS, -1.0));
        updateState(channelUID(GROUP_GENERAL_AMBIENT, CHANNEL_CALCULATED_AMBIENT_TEMPERATURE),
                getScaled(block.calculatedAmbientTemperature, CELSIUS, -1.0));
        resetCommunicationError();
    }

    protected void handlePolledEManagerData(ModbusRegisterArray registers) {
        // logger.trace("EManager block received, size: {}", registers.size());

        // EManager group
        EManagerBlock block = emanagerBlockParser.parse(registers);
        updateState(channelUID(GROUP_GENERAL_EMANAGER, CHANNEL_EMANAGER_ERROR_NUMBER),
                new DecimalType(block.emanagerErrorNumber));
        updateState(channelUID(GROUP_GENERAL_EMANAGER, CHANNEL_EMANAGER_OPERATING_STATE),
                new DecimalType(block.emanagerOperatingState));
        updateState(channelUID(GROUP_GENERAL_EMANAGER, CHANNEL_ACTUAL_POWER_CONSUMPTION),
                getUnscaled(block.actualPowerConsumption, WATT));
        updateState(channelUID(GROUP_GENERAL_EMANAGER, CHANNEL_ACTUAL_POWER), getUnscaled(block.actualPower, WATT));
        updateState(channelUID(GROUP_GENERAL_EMANAGER, CHANNEL_POWER_CONSUMPTION_SETPOINT),
                getUnscaled(block.powerConsumptionSetpoint, WATT));

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
     * @param string the channel id in that group
     * @return the globally unique channel uid
     */
    ChannelUID channelUID(String group, String id) {
        return new ChannelUID(getThing().getUID(), group, id);
    }
}
