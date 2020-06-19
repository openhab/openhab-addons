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
package org.openhab.binding.modbus.stiebeleltron.internal.handler;

import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.KILOWATT_HOUR;
import static org.eclipse.smarthome.core.library.unit.SmartHomeUnits.PERCENT;
import static org.openhab.binding.modbus.stiebeleltron.internal.StiebelEltronBindingConstants.*;

import java.math.BigDecimal;
import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
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
import org.openhab.io.transport.modbus.BasicModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.BasicModbusRegister;
import org.openhab.io.transport.modbus.BasicModbusRegisterArray;
import org.openhab.io.transport.modbus.BasicModbusWriteRegisterRequestBlueprint;
import org.openhab.io.transport.modbus.BasicPollTaskImpl;
import org.openhab.io.transport.modbus.BasicWriteTask;
import org.openhab.io.transport.modbus.BitArray;
import org.openhab.io.transport.modbus.ModbusManager;
import org.openhab.io.transport.modbus.ModbusReadCallback;
import org.openhab.io.transport.modbus.ModbusReadFunctionCode;
import org.openhab.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.io.transport.modbus.ModbusRegister;
import org.openhab.io.transport.modbus.ModbusRegisterArray;
import org.openhab.io.transport.modbus.ModbusResponse;
import org.openhab.io.transport.modbus.ModbusWriteCallback;
import org.openhab.io.transport.modbus.ModbusWriteRequestBlueprint;
import org.openhab.io.transport.modbus.PollTask;
import org.openhab.io.transport.modbus.endpoint.ModbusSlaveEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The {@link Modbus.StiebelEltronHandler} is responsible for handling commands,
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
            logger.debug("Unregistering polling from ModbusManager");
            StiebelEltronHandler.this.modbusManager.unregisterRegularPoll(task);

            pollTask = null;
        }

        /**
         * Register poll task This is where we set up our regular poller
         */
        public synchronized void registerPollTask(int address, int length, ModbusReadFunctionCode readFunctionCode) {

            logger.debug("Setting up regular polling");

            ModbusSlaveEndpoint myendpoint = StiebelEltronHandler.this.endpoint;
            StiebelEltronConfiguration myconfig = StiebelEltronHandler.this.config;
            if (myconfig == null || myendpoint == null) {
                throw new IllegalStateException("registerPollTask called without proper configuration");
            }

            BasicModbusReadRequestBlueprint request = new BasicModbusReadRequestBlueprint(getSlaveId(),
                    readFunctionCode, address, length, myconfig.getMaxTries());

            pollTask = new BasicPollTaskImpl(myendpoint, request, new ModbusReadCallback() {

                @Override
                public void onRegisters(@Nullable ModbusReadRequestBlueprint request,
                        @Nullable ModbusRegisterArray registers) {
                    if (registers == null) {
                        logger.warn("Received empty register array on poll");
                        return;
                    }
                    handlePolledData(registers);

                    if (StiebelEltronHandler.this.getThing().getStatus() != ThingStatus.ONLINE) {
                        updateStatus(ThingStatus.ONLINE);
                    }
                }

                @Override
                public void onError(@Nullable ModbusReadRequestBlueprint request, @Nullable Exception error) {
                    StiebelEltronHandler.this.handleError(error);
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
                StiebelEltronHandler.this.modbusManager.registerRegularPoll(task, refreshMillis, 1000);
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
    protected ModbusManager modbusManager;

    /**
     * Instances of this handler should get a reference to the modbus manager
     *
     * @param thing the thing to handle
     * @param modbusManager the modbus manager
     */
    public StiebelEltronHandler(Thing thing, ModbusManager modbusManager) {
        super(thing);
        this.modbusManager = modbusManager;
    }

    /**
     * @param address address of the value to be written on the modbus
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        @Nullable
        StiebelEltronConfiguration myconfig = StiebelEltronHandler.this.config;
        if (myconfig == null) {
            throw new IllegalStateException("registerPollTask called without proper configuration");
        }        
        // big endian byte ordering
        byte b1 = (byte) (shortValue >> 8);
        byte b2 = (byte) shortValue;

        ModbusRegister register = new BasicModbusRegister(b1, b2);
        ModbusRegisterArray data = new BasicModbusRegisterArray(new ModbusRegister[] { register });

        BasicModbusWriteRegisterRequestBlueprint request = new BasicModbusWriteRegisterRequestBlueprint(slaveId,
                address, data, false, myconfig.getMaxTries());

        ModbusSlaveEndpoint slaveEndpoint = this.endpoint;
        if (slaveEndpoint == null) {
            return;
        }

        BasicWriteTask writeTask = new BasicWriteTask(slaveEndpoint, request, new ModbusWriteCallback() {
            @Override
            public void onWriteResponse(ModbusWriteRequestBlueprint request, ModbusResponse response) {
                if (hasConfigurationError()) {
                    return;
                }
                logger.debug("Successful write, matching request {}", request);
                StiebelEltronHandler.this.updateStatus(ThingStatus.ONLINE);
            }

            @Override
            public void onError(ModbusWriteRequestBlueprint request, Exception error) {
                StiebelEltronHandler.this.handleError(error);
            }
        });
        logger.trace("Submitting write task: {}", writeTask);
        modbusManager.submitOneTimeWrite(writeTask);
    }

    /**
     * @param command get the value of this command.
     * @return short the value of the command multiplied by 10 (see datatype 2 in the stiebel eltron modbus
     *         documentation)
     */
    private short getScaledInt16Value(Command command) throws IllegalArgumentException {
        if (command instanceof QuantityType) {
            QuantityType<?> c = ((QuantityType<?>) command).toUnit(CELSIUS);
            return (short) (c.doubleValue() * 10);
        }
        if (command instanceof DecimalType) {
            DecimalType c = (DecimalType) command;
            return (short) (c.doubleValue() * 10);
        }
        throw new IllegalArgumentException();
    }

    /**
     * @param command get the value of this command.
     * @return short the value of the command as short
     */
    private short getInt16Value(Command command) throws IllegalArgumentException {
        if (command instanceof DecimalType) {
            DecimalType c = (DecimalType) command;
            return c.shortValue();
        }
        throw new IllegalArgumentException();
    }

    /**
     * Handle incoming commands.
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
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
        }
        catch(IllegalArgumentException e){
            handleError(e);
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
     * This method starts the operation of this handler
     * Connect to the slave bridge Start the periodic polling1
     */
    private void startUp() {

        if (endpoint != null) {
            return;
        }

        ModbusEndpointThingHandler slaveEndpointThingHandler = getEndpointThingHandler();
        if (slaveEndpointThingHandler == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Bridge is offline");
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
            @SuppressWarnings("null")
            String label = Optional.ofNullable(getBridge()).map(b -> b.getLabel()).orElse("<null>");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                    String.format("Bridge '%s' not completely initialized", label));
            logger.debug("Bridge not initialized fully (no endpoint) -- aborting init for {}", this);
            return;
        }

        if (config == null) {
            logger.debug("Invalid endpoint/config/manager ref for stiebel eltron handler");
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
            AbstractBasePoller poller  = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledEnergyData(registers);
                }

            };
            poller.registerPollTask(3500, 16, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            energyPoller = poller;
        }
        if (systemStatePoller == null) {
            AbstractBasePoller poller  = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemStateData(registers);
                }

            };
            poller.registerPollTask(2500, 2, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemStatePoller = poller;
        }
        if (systemParameterPoller == null) {
            AbstractBasePoller poller  = new AbstractBasePoller() {
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

        endpoint = null;
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

        if (handler instanceof ModbusEndpointThingHandler) {
            ModbusEndpointThingHandler slaveEndpoint = (ModbusEndpointThingHandler) handler;
            return slaveEndpoint;
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
        return new QuantityType<>(BigDecimal.valueOf(value.longValue(), 1), unit);
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
        return new QuantityType<>(value, KILOWATT_HOUR);
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
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_TEMPERATURE), getScaled(block.temperature_fek, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_TEMPERATURE_SETPOINT),
                getScaled(block.temperature_fek_setpoint, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_HUMIDITY), getScaled(block.humidity_ffk, PERCENT));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_FEK_DEWPOINT), getScaled(block.dewpoint_ffk, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_OUTDOOR_TEMPERATURE),
                getScaled(block.temperature_outdoor, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_HK1_TEMPERATURE), getScaled(block.temperature_hk1, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_HK1_TEMPERATURE_SETPOINT),
                getScaled(block.temperature_hk1_setpoint, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_SUPPLY_TEMPERATURE),
                getScaled(block.temperature_supply, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_RETURN_TEMPERATURE),
                getScaled(block.temperature_return, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_SOURCE_TEMPERATURE),
                getScaled(block.temperature_source, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_WATER_TEMPERATURE),
                getScaled(block.temperature_water, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFO, CHANNEL_WATER_TEMPERATURE_SETPOINT),
                getScaled(block.temperature_water_setpoint, CELSIUS));
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
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_OPERATION_MODE), new DecimalType(block.operation_mode));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_COMFORT_TEMPERATURE_HEATING),
                getScaled(block.comfort_temperature_heating, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_ECO_TEMPERATURE_HEATING),
                getScaled(block.eco_temperature_heating, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_COMFORT_TEMPERATURE_WATER),
                getScaled(block.comfort_temperature_water, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER, CHANNEL_ECO_TEMPERATURE_WATER),
                getScaled(block.eco_temperature_water, CELSIUS));
    }

    /**
     * @param bridgeStatusInfo
     */
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

}
