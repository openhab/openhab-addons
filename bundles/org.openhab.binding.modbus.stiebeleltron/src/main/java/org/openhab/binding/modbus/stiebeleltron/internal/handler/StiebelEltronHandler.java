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
package org.openhab.binding.modbus.stiebeleltron.internal.handler;

import static org.openhab.binding.modbus.stiebeleltron.internal.StiebelEltronBindingConstants.*;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;

import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.stiebeleltron.internal.StiebelEltronConfiguration;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.EnergyBlockParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SystemInfromationBlockParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SystemParameterBlockParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SystemStateBlockParser;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.ModbusWriteRegisterRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.core.io.transport.modbus.PollTask;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OpenClosedType;
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
 * The {@link StiebelEltronHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus.
 *
 * @author Paul Frank - Initial contribution
 */
@NonNullByDefault
public class StiebelEltronHandler extends BaseThingHandler {

    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandler.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = StiebelEltronHandler.this.comms;
            if (mycomms != null) {
                mycomms.unregisterRegularPoll(task);
            }
            pollTask = null;
        }

        /**
         * Register poll task This is where we set up our regular poller
         */
        public synchronized void registerPollTask(int address, int length, ModbusReadFunctionCode readFunctionCode) {
            logger.debug("Setting up regular polling");

            ModbusCommunicationInterface mycomms = StiebelEltronHandler.this.comms;
            StiebelEltronConfiguration myconfig = StiebelEltronHandler.this.config;
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
            }, StiebelEltronHandler.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = StiebelEltronHandler.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandler.class);

    /**
     * Configuration instance
     */
    protected @Nullable StiebelEltronConfiguration config = null;
    /**
     * Parser used to convert incoming raw messages into system blocks
     */
    private final SystemInfromationBlockParser systemInformationBlockParser = new SystemInfromationBlockParser();
    /**
     * Parser used to convert incoming raw messages into system state blocks
     */
    private final SystemStateBlockParser systemstateBlockParser = new SystemStateBlockParser();
    /**
     * Parser used to convert incoming raw messages into system parameter blocks
     */
    private final SystemParameterBlockParser systemParameterBlockParser = new SystemParameterBlockParser();
    /**
     * Parser used to convert incoming raw messages into model blocks
     */
    private final EnergyBlockParser energyBlockParser = new EnergyBlockParser();
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller systemInformationPoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller energyPoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller systemStatePoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller systemParameterPoller = null;
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
    public StiebelEltronHandler(Thing thing) {
        super(thing);
    }

    /**
     * @param address address of the value to be written on the modbus
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        StiebelEltronConfiguration myconfig = StiebelEltronHandler.this.config;
        ModbusCommunicationInterface mycomms = StiebelEltronHandler.this.comms;

        if (myconfig == null || mycomms == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }
        // big endian byte ordering
        byte hi = (byte) (shortValue >> 8);
        byte lo = (byte) shortValue;
        ModbusRegisterArray data = new ModbusRegisterArray(hi, lo);

        ModbusWriteRegisterRequestBlueprint request = new ModbusWriteRegisterRequestBlueprint(slaveId, address, data,
                false, myconfig.getMaxTries());

        mycomms.submitOneTimeWrite(request, result -> {
            if (hasConfigurationError()) {
                return;
            }
            logger.debug("Successful write, matching request {}", request);
            StiebelEltronHandler.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            StiebelEltronHandler.this.handleWriteError(failure);
        });
    }

    /**
     * @param command get the value of this command.
     * @return short the value of the command multiplied by 10 (see datatype 2 in
     *         the stiebel eltron modbus documentation)
     */
    private short getScaledInt16Value(Command command) throws StiebelEltronException {
        if (command instanceof QuantityType quantityCommand) {
            QuantityType<?> c = quantityCommand.toUnit(CELSIUS);
            if (c != null) {
                return (short) (c.doubleValue() * 10);
            } else {
                throw new StiebelEltronException("Unsupported unit");
            }
        }
        if (command instanceof DecimalType c) {
            return (short) (c.doubleValue() * 10);
        }
        throw new StiebelEltronException("Unsupported command type");
    }

    /**
     * @param command get the value of this command.
     * @return short the value of the command as short
     */
    private short getInt16Value(Command command) throws StiebelEltronException {
        if (command instanceof DecimalType c) {
            return c.shortValue();
        }
        throw new StiebelEltronException("Unsupported command type");
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
                    case GROUP_SYSTEM_STATE:
                        poller = systemStatePoller;
                        break;
                    case GROUP_SYSTEM_PARAMETER:
                        poller = systemParameterPoller;
                        break;
                    case GROUP_SYSTEM_INFO:
                        poller = systemInformationPoller;
                        break;
                    case GROUP_ENERGY_INFO:
                        poller = energyPoller;
                        break;
                    default:
                        poller = null;
                        break;
                }
                if (poller != null) {
                    poller.poll();
                }
            }
        } else {
            try {
                if (GROUP_SYSTEM_PARAMETER.equals(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_OPERATION_MODE:
                            writeInt16(1500, getInt16Value(command));
                            break;
                        case CHANNEL_COMFORT_TEMPERATURE_HEATING:
                            writeInt16(1501, getScaledInt16Value(command));
                            break;
                        case CHANNEL_ECO_TEMPERATURE_HEATING:
                            writeInt16(1502, getScaledInt16Value(command));
                            break;
                        case CHANNEL_COMFORT_TEMPERATURE_WATER:
                            writeInt16(1509, getScaledInt16Value(command));
                            break;
                        case CHANNEL_ECO_TEMPERATURE_WATER:
                            writeInt16(1510, getScaledInt16Value(command));
                            break;
                    }
                }
            } catch (StiebelEltronException error) {
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
        config = getConfigAs(StiebelEltronConfiguration.class);
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
            logger.debug("Invalid comms/config/manager ref for stiebel eltron handler");
            return;
        }

        if (systemInformationPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemInformationData(registers);
                }
            };
            poller.registerPollTask(500, 36, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemInformationPoller = poller;
        }
        if (energyPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledEnergyData(registers);
                }
            };
            poller.registerPollTask(3500, 16, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            energyPoller = poller;
        }
        if (systemStatePoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemStateData(registers);
                }
            };
            poller.registerPollTask(2500, 2, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemStatePoller = poller;
        }
        if (systemParameterPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemParameterData(registers);
                }
            };
            poller.registerPollTask(1500, 11, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            systemParameterPoller = poller;
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
        AbstractBasePoller poller = systemInformationPoller;
        if (poller != null) {
            logger.debug("Unregistering systemInformationPoller from ModbusManager");
            poller.unregisterPollTask();

            systemInformationPoller = null;
        }

        poller = energyPoller;
        if (poller != null) {
            logger.debug("Unregistering energyPoller from ModbusManager");
            poller.unregisterPollTask();

            energyPoller = null;
        }

        poller = systemStatePoller;
        if (poller != null) {
            logger.debug("Unregistering systemStatePoller from ModbusManager");
            poller.unregisterPollTask();

            systemStatePoller = null;
        }

        poller = systemParameterPoller;
        if (poller != null) {
            logger.debug("Unregistering systemParameterPoller from ModbusManager");
            poller.unregisterPollTask();

            systemParameterPoller = null;
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

    /**
     * Returns value divided by the 10
     *
     * @param value the value to alter
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Number value, Unit<?> unit) {
        return QuantityType.valueOf(value.doubleValue() / 10, unit);
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
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    protected void handlePolledSystemInformationData(ModbusRegisterArray registers) {
        logger.trace("System Information block received, size: {}", registers.size());

        SystemInformationBlock block = systemInformationBlockParser.parse(registers);

        // System information group
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_TEMPERATURE), getScaled(block.temperatureFek, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureFekSetPoint, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_HUMIDITY), getScaled(block.humidityFek, PERCENT));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_DEWPOINT), getScaled(block.dewpointFek, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_OUTDOOR_TEMPERATURE),
                getScaled(block.temperatureOutdoor, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_HK1_TEMPERATURE), getScaled(block.temperatureHk1, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_HK1_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureHk1SetPoint, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_SUPPLY_TEMPERATURE),
                getScaled(block.temperatureSupply, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_RETURN_TEMPERATURE),
                getScaled(block.temperatureReturn, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_SOURCE_TEMPERATURE),
                getScaled(block.temperatureSource, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_WATER_TEMPERATURE),
                getScaled(block.temperatureWater, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_WATER_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureWaterSetPoint, CELSIUS));

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    protected void handlePolledEnergyData(ModbusRegisterArray registers) {
        logger.trace("Energy block received, size: {}", registers.size());

        EnergyBlock block = energyBlockParser.parse(registers);

        // Energy information group
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_PRODUCTION_HEAT_TODAY),
                new QuantityType<>(block.productionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_PRODUCTION_HEAT_TOTAL),
                getEnergyQuantity(block.productionHeatTotalHigh, block.productionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_PRODUCTION_WATER_TODAY),
                new QuantityType<>(block.productionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_PRODUCTION_WATER_TOTAL),
                getEnergyQuantity(block.productionWaterTotalHigh, block.productionWaterTotalLow));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_CONSUMPTION_HEAT_TODAY),
                new QuantityType<>(block.consumptionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_CONSUMPTION_HEAT_TOTAL),
                getEnergyQuantity(block.consumptionHeatTotalHigh, block.consumptionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_CONSUMPTION_WATER_TODAY),
                new QuantityType<>(block.consumptionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_INFO, CHANNEL_CONSUMPTION_WATER_TOTAL),
                getEnergyQuantity(block.consumptionWaterTotalHigh, block.consumptionWaterTotalLow));

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    protected void handlePolledSystemStateData(ModbusRegisterArray registers) {
        logger.trace("System state block received, size: {}", registers.size());

        SystemStateBlock block = systemstateBlockParser.parse(registers);
        boolean isHeating = (block.state & 16) != 0;
        updateState(channelUID(GROUP_SYSTEM_STATE, CHANNEL_IS_HEATING),
                isHeating ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE, CHANNEL_IS_HEATING_WATER),
                (block.state & 32) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE, CHANNEL_IS_COOLING),
                (block.state & 256) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE, CHANNEL_IS_SUMMER),
                (block.state & 128) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE, CHANNEL_IS_PUMPING),
                (block.state & 1) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    protected void handlePolledSystemParameterData(ModbusRegisterArray registers) {
        logger.trace("System state block received, size: {}", registers.size());

        SystemParameterBlock block = systemParameterBlockParser.parse(registers);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_OPERATION_MODE), new DecimalType(block.operationMode));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_COMFORT_TEMPERATURE_HEATING),
                getScaled(block.comfortTemperatureHeating, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_ECO_TEMPERATURE_HEATING),
                getScaled(block.ecoTemperatureHeating, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_COMFORT_TEMPERATURE_WATER),
                getScaled(block.comfortTemperatureWater, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_ECO_TEMPERATURE_WATER),
                getScaled(block.ecoTemperatureWater, CELSIUS));

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
