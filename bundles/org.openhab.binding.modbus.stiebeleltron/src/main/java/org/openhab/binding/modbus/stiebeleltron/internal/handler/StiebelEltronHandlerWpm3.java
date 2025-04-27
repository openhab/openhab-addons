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
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.EnergyRuntimeBlockAllWpmParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SystemInformationBlockAllWpmParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SystemParameterBlockAllWpmParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SystemStateBlockParserAllWpm;
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

import net.wimpi.modbus.Modbus;

/**
 * The {@link Modbus.StiebelEltronHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus.
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Thing handler for a WPM3i compatible heat pump
 */
@NonNullByDefault
public class StiebelEltronHandlerWpm3 extends BaseThingHandler {
    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandlerWpm3.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = StiebelEltronHandlerWpm3.this.comms;
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

            ModbusCommunicationInterface mycomms = StiebelEltronHandlerWpm3.this.comms;
            StiebelEltronConfiguration myconfig = StiebelEltronHandlerWpm3.this.config;
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
            }, StiebelEltronHandlerWpm3.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = StiebelEltronHandlerWpm3.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandlerWpm3.class);

    /**
     * Configuration instance
     */
    protected @Nullable StiebelEltronConfiguration config = null;
    /**
     * Parser used to convert incoming raw messages into system blocks
     */
    private final SystemInformationBlockAllWpmParser systemInformationBlockParser = new SystemInformationBlockAllWpmParser(
            true);
    /**
     * Parser used to convert incoming raw messages into system parameter blocks
     */
    private final SystemParameterBlockAllWpmParser systemParameterBlockParser = new SystemParameterBlockAllWpmParser();
    /**
     * Parser used to convert incoming raw messages into system state blocks
     */
    private final SystemStateBlockParserAllWpm systemStateBlockParser = new SystemStateBlockParserAllWpm();
    /**
     * Parser used to convert incoming raw messages into energy and runtime blocks
     */
    private final EnergyRuntimeBlockAllWpmParser energyRuntimeBlockParser = new EnergyRuntimeBlockAllWpmParser(true);
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller systemInformationPoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller systemParameterPoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller systemStatePoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller energyRuntimePoller = null;
    /**
     * Communication interface to the slave endpoint we're connecting to
     */
    protected volatile @Nullable ModbusCommunicationInterface comms = null;

    /**
     * This is the slave id, we store this once initialization is complete
     */
    private volatile int slaveId;

    /**
     * Variables for reporting only once that value is not available and setting it.
     */
    private boolean runtimeCompressorCoolingReported = false;
    private boolean waterStagesReported = false;

    /**
     * Instances of this handler should get a reference to the modbus manager
     *
     * @param thing the thing to handle
     * @param modbusManager the modbus manager
     */
    public StiebelEltronHandlerWpm3(Thing thing) {
        super(thing);
    }

    /**
     * @param address address of the value to be written on the modbus
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        StiebelEltronConfiguration myconfig = StiebelEltronHandlerWpm3.this.config;
        ModbusCommunicationInterface mycomms = StiebelEltronHandlerWpm3.this.comms;

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
            StiebelEltronHandlerWpm3.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            StiebelEltronHandlerWpm3.this.handleWriteError(failure);
        });
    }

    /**
     * @param command get the value of this command.
     * @param scaleFactor the scale factor (multiplication)
     * @return short the value of the command multiplied by scaleFactor (see datatype 2 (10)
     *         and datatype 7 (100) in the stiebel eltron modbus documentation)
     */
    private short getScaledInt16Value(Command command, Integer scaleFactor) throws StiebelEltronException {
        if (command instanceof QuantityType) {
            QuantityType<?> c = ((QuantityType<?>) command).toUnit(CELSIUS);
            if (c != null) {
                return (short) (c.doubleValue() * scaleFactor);
            } else {
                throw new StiebelEltronException("Unsupported unit");
            }
        }
        if (command instanceof DecimalType) {
            DecimalType c = (DecimalType) command;
            return (short) (c.doubleValue() * scaleFactor);
        }
        throw new StiebelEltronException("Unsupported command type");
    }

    /**
     * @param command get the value of this command.
     * @return short the value of the command as short
     */
    private short getInt16Value(Command command) throws StiebelEltronException {
        if (command instanceof DecimalType) {
            DecimalType c = (DecimalType) command;
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
                    case GROUP_SYSTEM_INFORMATION_WPM3:
                        poller = systemInformationPoller;
                        break;
                    case GROUP_SYSTEM_PARAMETER_WPM3WPM3I:
                        poller = systemParameterPoller;
                        break;
                    case GROUP_SYSTEM_STATE_WPM3:
                        poller = systemStatePoller;
                        break;
                    case GROUP_ENERGY_RUNTIME_INFO_WPMWPM3:
                        poller = energyRuntimePoller;
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
                if (GROUP_SYSTEM_PARAMETER_WPM3WPM3I.equals(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_OPERATION_MODE:
                            writeInt16(1500, getInt16Value(command));
                            break;
                        case CHANNEL_HC1_COMFORT_TEMPERATURE:
                            writeInt16(1501, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_HC1_ECO_TEMPERATURE:
                            writeInt16(1502, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_HC1_HEATING_CURVE_RISE:
                            writeInt16(1503, getScaledInt16Value(command, 100));
                            break;
                        case CHANNEL_HC2_COMFORT_TEMPERATURE:
                            writeInt16(1504, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_HC2_ECO_TEMPERATURE:
                            writeInt16(1505, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_HC2_HEATING_CURVE_RISE:
                            writeInt16(1506, getScaledInt16Value(command, 100));
                            break;
                        case CHANNEL_FIXED_TEMPERATURE_SETPOINT:
                            writeInt16(1507, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_HEATING_DUAL_MODE_TEMPERATURE:
                            writeInt16(1508, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_COMFORT_TEMPERATURE_WATER:
                            writeInt16(1509, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_ECO_TEMPERATURE_WATER:
                            writeInt16(1510, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_HOTWATER_STAGES:
                            writeInt16(1511, getInt16Value(command));
                            break;
                        case CHANNEL_HOTWATER_DUAL_MODE_TEMPERATURE:
                            writeInt16(1512, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_SETPOINT:
                            writeInt16(1513, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_HYSTERESIS:
                            writeInt16(1514, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_AREA_COOLING_ROOM_TEMPERATURE_SETPOINT:
                            writeInt16(1515, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_SETPOINT:
                            writeInt16(1516, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_HYSTERESIS:
                            writeInt16(1517, getScaledInt16Value(command, 10));
                            break;
                        case CHANNEL_FAN_COOLING_ROOM_TEMPERATURE_SETPOINT:
                            writeInt16(1518, getScaledInt16Value(command, 10));
                            break;
                        /* take care can reset heat pump settings */
                        /* disabled per default */
                        case CHANNEL_RESET:
                            short resetCmd = getInt16Value(command);
                            if (resetCmd != CHANNEL_RESET_CMD_SYSTEM_RESET) {
                                logger.info("RESET command received with value {} - but ignored right now", resetCmd);
                                // writeInt16(1519, resetCmd);
                            } else {
                                logger.warn("Command 'Reset System' ignored!");
                            }
                            break;

                        case CHANNEL_RESTART_ISG:
                            short restartIsgCmd = getInt16Value(command);
                            if (restartIsgCmd != CHANNEL_RESTART_ISG_CMD_SERVICE_KEY) {
                                logger.info("RESTART-ISG command received with value {} - but ignored right now",
                                        restartIsgCmd);
                                // writeInt16(1520, restartIsgCmd);
                            } else {
                                logger.warn("Command 'Service Key' ignored!");
                            }
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
            poller.registerPollTask(500, 48, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemInformationPoller = poller;
        }
        if (systemParameterPoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemParameterData(registers);
                }
            };
            poller.registerPollTask(1500, 21, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
            systemParameterPoller = poller;
        }
        if (systemStatePoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemStateData(registers);
                }
            };
            poller.registerPollTask(2500, 7, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemStatePoller = poller;
        }
        if (energyRuntimePoller == null) {
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledEnergyRuntimeData(registers);
                }
            };
            poller.registerPollTask(3500, 48, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            energyRuntimePoller = poller;
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

        poller = systemParameterPoller;
        if (poller != null) {
            logger.debug("Unregistering systemParameterPoller from ModbusManager");
            poller.unregisterPollTask();

            systemParameterPoller = null;
        }

        poller = systemStatePoller;
        if (poller != null) {
            logger.debug("Unregistering systemStatePoller from ModbusManager");
            poller.unregisterPollTask();

            systemStatePoller = null;
        }

        poller = energyRuntimePoller;
        if (poller != null) {
            logger.debug("Unregistering energyRuntimePoller from ModbusManager");
            poller.unregisterPollTask();

            energyRuntimePoller = null;
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

        if (handler instanceof ModbusEndpointThingHandler) {
            ModbusEndpointThingHandler slaveEndpoint = (ModbusEndpointThingHandler) handler;
            return slaveEndpoint;
        } else {
            throw new IllegalStateException("Unexpected bridge handler: " + handler.toString());
        }
    }

    /**
     * Returns value divided by the scaleFactor (type 2: 10, type 7: 100)
     *
     * @param value the value to alter
     * @param scaleFactor the scale factor (divison)
     * @return the scaled value as a DecimalType
     */
    protected State getScaled(Number value, Integer scaleFactor, Unit<?> unit) {
        return QuantityType.valueOf(value.doubleValue() / scaleFactor, unit);
    }

    /**
     * Returns high value * 1000 + low value
     *
     * @param high the high value
     * @param low the low value
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

        SystemInformationBlockAllWpm block = systemInformationBlockParser.parse(registers);

        if (block.temperatureFe7 != -32768) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FE7_TEMPERATURE),
                    getScaled(block.temperatureFe7, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FE7_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureFe7SetPoint, 10, CELSIUS));
        } else {
            logger.trace("FE7 not available");
        }

        if (block.temperatureFek != -32768) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FEK_TEMPERATURE),
                    getScaled(block.temperatureFek, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FEK_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureFekSetPoint, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FEK_HUMIDITY),
                    getScaled(block.humidityFek, 10, PERCENT));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FEK_DEWPOINT),
                    getScaled(block.dewpointFek, 10, CELSIUS));
        } else {
            logger.trace("FEK not available");
        }

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_OUTDOOR_TEMPERATURE),
                getScaled(block.temperatureOutdoor, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HC1_TEMPERATURE),
                getScaled(block.temperatureHc1, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HC1_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureHc1SetPoint, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HC2_TEMPERATURE),
                getScaled(block.temperatureHc2, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HC2_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureHc2SetPoint, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP_FLOW_TEMPERATURE),
                getScaled(block.temperatureFlowHp, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_NHZ_FLOW_TEMPERATURE),
                getScaled(block.temperatureFlowNhz, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FLOW_TEMPERATURE),
                getScaled(block.temperatureFlow, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_RETURN_TEMPERATURE),
                getScaled(block.temperatureReturn, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FIXED_TEMPERATURE_SETPOINT), getScaled(
                (block.temperatureFixedSetPoint == -28672) ? 0 : block.temperatureFixedSetPoint, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_BUFFER_TEMPERATURE),
                getScaled(block.temperatureBuffer, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_BUFFER_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureBufferSetPoint, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HEATING_PRESSURE),
                getScaled(block.pressureHeating, 100, BAR));

        // Comment about FlowRate scale
        // It looks like there's a typo in the documentation; data type of flow rate is 2, but the value is about factor
        // 10 to high compared to value reported in ISG web interface; therefore using factor 100 (data type 7)
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FLOW_RATE),
                getScaled(block.flowRate, 100, LITRE_PER_MINUTE));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HOTWATER_TEMPERATURE),
                getScaled(block.temperatureWater, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HOTWATER_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureWaterSetPoint, 10, CELSIUS));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FAN_COOLING_TEMPERATURE),
                getScaled(block.temperatureFanCooling, 10, KELVIN));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_FAN_COOLING_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureFanCoolingSetPoint, 10, KELVIN));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_AREA_COOLING_TEMPERATURE),
                getScaled(block.temperatureAreaCooling, 10, KELVIN));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_AREA_COOLING_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureAreaCoolingSetPoint, 10, KELVIN));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_SOLAR_THERMAL_COLLECTOR_TEMPERATURE),
                getScaled(block.temperatureCollectorSolar, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_SOLAR_THERMAL_CYLINDER_TEMPERATURE),
                getScaled(block.temperatureCylinderSolar, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_SOLAR_THERMAL_RUNTIME),
                new QuantityType<>(block.runtimeSolar, HOUR));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE),
                getScaled(block.temperatureExtHeatSource, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureExtHeatSourceSetPoint, 10, KELVIN));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_EXT_HEAT_SOURCE_RUNTIME),
                new QuantityType<>(block.runtimeExtHeatSource, HOUR));

        // Check for OFF value (0x9000)-> use -410, which returns -41; valid range is -40..40
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_LOWER_APPLICATION_LIMIT_HEATING),
                getScaled((block.lowerHeatingLimit == -28672) ? -410 : block.lowerHeatingLimit, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_LOWER_APPLICATION_LIMIT_HOTWATER),
                getScaled((block.lowerWaterLimit == -28672) ? -410 : block.lowerWaterLimit, 10, CELSIUS));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_SOURCE_TEMPERATURE),
                getScaled(block.temperatureSource, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_MIN_SOURCE_TEMPERATURE),
                getScaled(block.temperatureSourceMin, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_SOURCE_PRESSURE),
                getScaled(block.pressureSource, 100, BAR));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_RETRURN_TEMPERATURE),
                getScaled(block.hp1TemperatureReturn, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_FLOW_TEMPERATURE),
                getScaled(block.hp1TemperatureFlow, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_HOTGAS_TEMPERATURE),
                getScaled(block.hp1TemperatureHotgas, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_LOW_PRESSURE),
                getScaled(block.hp1PressureLow, 100, BAR));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_MEAN_PRESSURE),
                getScaled(block.hp1PressureMean, 100, BAR));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_HGH_PRESSURE),
                getScaled(block.hp1PressureHigh, 100, BAR));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_WPM3, CHANNEL_HP1_FLOW_RATE),
                getScaled(block.hp1FlowRate, 10, LITRE_PER_MINUTE));

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

        SystemParameterBlockAllWpm block = systemParameterBlockParser.parse(registers);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_OPERATING_MODE),
                new DecimalType(block.operationMode));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HC1_COMFORT_TEMPERATURE),
                getScaled(block.comfortTemperatureHeating, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HC1_ECO_TEMPERATURE),
                getScaled(block.ecoTemperatureHeating, 10, CELSIUS));

        double heatingCurveRiseHeatingHk1 = block.heatingCurveRiseHc1 / 100.0;
        logger.trace("curve rise 1 = {}", heatingCurveRiseHeatingHk1);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HC1_HEATING_CURVE_RISE),
                new DecimalType(heatingCurveRiseHeatingHk1));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HC2_COMFORT_TEMPERATURE),
                getScaled(block.comfortTemperatureHeatingHc2, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HC2_ECO_TEMPERATURE),
                getScaled(block.ecoTemperatureHeatingHc2, 10, CELSIUS));
        double heatingCurveRiseHeatingHk2 = block.heatingCurveRiseHc2 / 100.0;
        logger.trace("curve rise 2 = {}", heatingCurveRiseHeatingHk2);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HC2_HEATING_CURVE_RISE),
                new DecimalType(heatingCurveRiseHeatingHk2));

        // if 0x9000 => means OFF, so set it to 0 to indicate off
        logger.trace("fixedValueOperation = {}", block.fixedValueOperation);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_FIXED_VALUE_OPERATION),
                getScaled((block.fixedValueOperation == -28672) ? 0 : block.fixedValueOperation, 10, CELSIUS));

        logger.trace("dualModeTemperatureHeating = {}", block.dualModeTemperatureHeating);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HEATING_DUAL_MODE_TEMPERATURE),
                getScaled(block.dualModeTemperatureHeating, 10, CELSIUS));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HOTWATER_COMFORT_TEMPERATURE),
                getScaled(block.comfortTemperatureWater, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HOTWATER_ECO_TEMPERATURE),
                getScaled(block.ecoTemperatureWater, 10, CELSIUS));

        logger.trace("waterStages = {}", block.hotwaterStages);
        if (block.hotwaterStages != 32768) {
            logger.trace("waterStages available");
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HOTWATER_STAGES),
                    new DecimalType(block.hotwaterStages));
        } else {
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HOTWATER_STAGES), new DecimalType(0));
            if (!waterStagesReported) {
                logger.trace("waterStages not available - setting fix to 0!");
                waterStagesReported = true;
            }
        }

        logger.trace("dualModeTemperatureWater = {}", block.hotwaterDualModeTemperature);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_HOTWATER_DUAL_MODE_TEMPERATURE),
                getScaled(block.hotwaterDualModeTemperature, 10, CELSIUS));

        logger.trace("areaCoolingFlowTemperatureSetPoint = {}", block.flowTemperatureAreaCooling);
        if (block.flowTemperatureAreaCooling != -32768) {
            logger.trace("areaCoolingFlowTemperatureSetPoint available");
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_SETPOINT),
                    getScaled(block.flowTemperatureAreaCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_AREA_COOLING_ROOM_TEMPERATURE_SETPOINT),
                    getScaled(block.roomTemperatureAreaCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_HYSTERESIS),
                    getScaled(block.flowTemperatureHysteresisAreaCooling, 10, KELVIN));
        }

        logger.trace("fanCoolingFlowTemperatureSetPoint = {}", block.flowTemperatureFanCooling);
        if (block.flowTemperatureFanCooling != -32768) {
            logger.trace("fanCoolingFlowTemperatureSetPoint available");
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_SETPOINT),
                    getScaled(block.flowTemperatureFanCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_FAN_COOLING_ROOM_TEMPERATURE_SETPOINT),
                    getScaled(block.roomTemperatureFanCooling, 10, CELSIUS));
            if (block.flowTemperatureHysteresisFanCooling != -32768) {
                logger.trace("fanCoolingFlowTemperatureHysteres available");
                updateState(
                        channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_HYSTERESIS),
                        getScaled(block.flowTemperatureHysteresisFanCooling, 10, KELVIN));
            }
        }

        logger.trace("reset = {}", block.reset);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_RESET), new DecimalType(block.reset));
        logger.trace("restartISG = {}", block.restartIsg);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_WPM3WPM3I, CHANNEL_RESTART_ISG),
                new DecimalType(block.restartIsg));

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

        SystemStateBlockAllWpm block = systemStateBlockParser.parse(registers);
        // operating status bit-coded
        logger.trace("operatingState = {}", block.state);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_HC1_PUMP_ACTIVE),
                (block.state & 1) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_HC2_PUMP_ACTIVE),
                (block.state & 2) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_HEAT_UP_PROGRAM_ACTIVE),
                (block.state & 4) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_NHZ_RUNNING),
                (block.state & 8) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_HP_IN_HEATING_MODE),
                (block.state & 16) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_HP_IN_HOTWATER_MODE),
                (block.state & 32) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR_RUNNING),
                (block.state & 64) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_SUMMER_MODE_ACTIVE),
                (block.state & 128) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COOLING_MODE_ACTIVE),
                (block.state & 256) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_MIN_ONE_IWS_IN_DEFROSTING_MODE),
                (block.state & 512) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);

        if (block.operatingStatus != 32768) {
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_SILENT_MODE1_ACTIVE),
                    (block.operatingStatus & 1024) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_SILENT_MODE2_ACTIVE),
                    (block.operatingStatus & 2048) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }

        // power-off bit-coded
        logger.trace("powerOff = {}", block.powerOff);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_POWER_OFF),
                (block.powerOff == 1) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);

        // operating status WPM3 only bit-coded
        logger.trace("operatingStatus = {}", block.operatingStatus);
        if (block.operatingStatus != 32768) {
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR1_ACTIVE),
                    (block.operatingStatus & 1) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR2_ACTIVE),
                    (block.operatingStatus & 2) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR3_ACTIVE),
                    (block.operatingStatus & 4) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR4_ACTIVE),
                    (block.operatingStatus & 8) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR5_ACTIVE),
                    (block.operatingStatus & 16) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_COMPRESSOR6_ACTIVE),
                    (block.operatingStatus & 32) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUFFER_CHARGING_PUMP1_ACTIVE),
                    (block.operatingStatus & 64) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUFFER_CHARGING_PUMP2_ACTIVE),
                    (block.operatingStatus & 128) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUFFER_CHARGING_PUMP3_ACTIVE),
                    (block.operatingStatus & 256) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUFFER_CHARGING_PUMP4_ACTIVE),
                    (block.operatingStatus & 512) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUFFER_CHARGING_PUMP5_ACTIVE),
                    (block.operatingStatus & 1024) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUFFER_CHARGING_PUMP6_ACTIVE),
                    (block.operatingStatus & 2048) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_NHZ1_ACTIVE),
                    (block.operatingStatus & 4096) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_NHZ2_ACTIVE),
                    (block.operatingStatus & 8192) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        }

        logger.trace("faultStatus = {}", block.faultStatus);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_FAULT_STATUS), new DecimalType(block.faultStatus));

        logger.trace("busStatus = {}", block.busStatus);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_BUS_STATUS), new DecimalType(block.busStatus));

        logger.trace("defrostInitiated = {}", block.defrostInitiated);
        if (block.defrostInitiated != 32768) {
            updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_DEFROST_INITIATED),
                    new DecimalType(block.defrostInitiated));
        }

        DecimalType actErr = new DecimalType(block.activeError);
        logger.trace("activeError = {}, actErr = {}", block.activeError, actErr);
        updateState(channelUID(GROUP_SYSTEM_STATE_WPM3, CHANNEL_ACTIVE_ERROR), new DecimalType(block.activeError));

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    protected void handlePolledEnergyRuntimeData(ModbusRegisterArray registers) {
        logger.trace("Energy block received, size: {}", registers.size());

        EnergyRuntimeBlockAllWpm energyRuntimeBlock = energyRuntimeBlockParser.parse(registers);

        // Energy information group
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_PRODUCTION_HEAT_TODAY),
                new QuantityType<>(energyRuntimeBlock.productionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_PRODUCTION_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionHeatTotalHigh, energyRuntimeBlock.productionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_PRODUCTION_WATER_TODAY),
                new QuantityType<>(energyRuntimeBlock.productionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_PRODUCTION_WATER_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionWaterTotalHigh, energyRuntimeBlock.productionWaterTotalLow));

        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_PRODUCTION_NHZ_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionNhzHeatingTotalHigh, energyRuntimeBlock.productionNhzHeatingTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_PRODUCTION_NHZ_WATER_TOTAL),
                getEnergyQuantity(energyRuntimeBlock.productionNhzHotwaterTotalHigh,
                        energyRuntimeBlock.productionNhzHotwaterTotalLow));

        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_CONSUMPTION_HEAT_TODAY),
                new QuantityType<>(energyRuntimeBlock.consumptionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_CONSUMPTION_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.consumptionHeatTotalHigh, energyRuntimeBlock.consumptionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_CONSUMPTION_WATER_TODAY),
                new QuantityType<>(energyRuntimeBlock.consumptionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_CONSUMPTION_WATER_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.consumptionWaterTotalHigh, energyRuntimeBlock.consumptionWaterTotalLow));

        logger.trace("runtimeNhz1 = {}", energyRuntimeBlock.runtimeNhz1);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_NHZ1_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.runtimeNhz1, HOUR));
        logger.trace("runtimeNhz2 = {}", energyRuntimeBlock.runtimeNhz2);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_NHZ2_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.runtimeNhz2, HOUR));
        logger.trace("runtimeNhz12 = {}", energyRuntimeBlock.runtimeNhz12);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_NHZ12_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.runtimeNhz12, HOUR));

        // Heat Pump 1
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_PRODUCTION_HEAT_TODAY),
                new QuantityType<>(energyRuntimeBlock.hp1ProductionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_PRODUCTION_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.hp1ProductionHeatTotalHigh, energyRuntimeBlock.hp1ProductionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_PRODUCTION_WATER_TODAY),
                new QuantityType<>(energyRuntimeBlock.hp1ProductionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_PRODUCTION_WATER_TOTAL),
                getEnergyQuantity(energyRuntimeBlock.hp1ProductionWaterTotalHigh,
                        energyRuntimeBlock.hp1ProductionWaterTotalLow));

        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_PRODUCTION_NHZ_HEAT_TOTAL),
                getEnergyQuantity(energyRuntimeBlock.hp1ProductionNhzHeatingTotalHigh,
                        energyRuntimeBlock.hp1ProductionNhzHeatingTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_PRODUCTION_NHZ_WATER_TOTAL),
                getEnergyQuantity(energyRuntimeBlock.hp1ProductionNhzHotwaterTotalHigh,
                        energyRuntimeBlock.hp1ProductionNhzHotwaterTotalLow));

        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CONSUMPTION_HEAT_TODAY),
                new QuantityType<>(energyRuntimeBlock.hp1ConsumptionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CONSUMPTION_HEAT_TOTAL),
                getEnergyQuantity(energyRuntimeBlock.hp1ConsumptionHeatTotalHigh,
                        energyRuntimeBlock.hp1ConsumptionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CONSUMPTION_WATER_TODAY),
                new QuantityType<>(energyRuntimeBlock.hp1ConsumptionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CONSUMPTION_WATER_TOTAL),
                getEnergyQuantity(energyRuntimeBlock.hp1ConsumptionWaterTotalHigh,
                        energyRuntimeBlock.hp1ConsumptionWaterTotalLow));

        logger.trace("hp1RuntimeCompressor1Heating = {}", energyRuntimeBlock.hp1RuntimeCompressor1Heating);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CP1_HEATING_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressor1Heating, HOUR));
        logger.trace("hp1RuntimeCompressor2Heating = {}", energyRuntimeBlock.hp1RuntimeCompressor2Heating);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CP2_HEATING_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressor2Heating, HOUR));
        logger.trace("hp1RuntimeCompressor12Heating = {}", energyRuntimeBlock.hp1RuntimeCompressor12Heating);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CP12_HEATING_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressor12Heating, HOUR));

        logger.trace("hp1RuntimeCompressor1Hotwater = {}", energyRuntimeBlock.hp1RuntimeCompressor1Hotwater);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CP1_HOTWATER_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressor1Hotwater, HOUR));
        logger.trace("hp1RuntimeCompressor2Hotwater = {}", energyRuntimeBlock.hp1RuntimeCompressor2Hotwater);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CP2_HOTWATER_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressor2Hotwater, HOUR));
        logger.trace("hp1RuntimeCompressor12Hotwater = {}", energyRuntimeBlock.hp1RuntimeCompressor12Hotwater);
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_CP12_HOTWATER_RUNTIME),
                new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressor12Hotwater, HOUR));

        if (energyRuntimeBlock.hp1RuntimeCompressorCooling != 32768) {
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_COOLING_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.hp1RuntimeCompressorCooling, HOUR));
        } else {
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_WPMWPM3, CHANNEL_HP1_COOLING_RUNTIME),
                    new QuantityType<>(0, HOUR));
            if (!runtimeCompressorCoolingReported) {
                logger.trace("hp1RuntimeCompressorCooling not available - setting to 0!");
                runtimeCompressorCoolingReported = true;
            }
        }

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
