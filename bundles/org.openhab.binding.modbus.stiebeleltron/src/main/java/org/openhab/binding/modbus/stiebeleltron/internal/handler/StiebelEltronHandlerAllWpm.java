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
import org.openhab.binding.modbus.stiebeleltron.internal.StiebelEltronHpV2Configuration;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm.EnergyRuntimeFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm.EnergyRuntimeHpFeature;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.EnergyRuntimeControlAllWpm.EnergyRuntimeHpFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementControl;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementControl.SgReadyEnMgmtFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementSettingsBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementSystemInformationBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm.SysInfoFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm.SysInfoHpFeature;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemInformationControlAllWpm.SysInfoHpFeaturelKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemParameterControlAllWpm.SysParamFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateBlockAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateControlAllWpm;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SystemStateControlAllWpm.SystemStateFeatureKeys;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.EnergyRuntimeBlockAllWpmParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SgReadyEnergyManagementSettingsBlockParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SgReadyEnergyManagementSystemInformationBlockParser;
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
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.wimpi.modbus.Modbus;

/**
 * The {@link Modbus.StiebelEltronHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus.
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Extended thing handler for a WPM compatible heat pump
 */
@NonNullByDefault
public class StiebelEltronHandlerAllWpm extends BaseThingHandler {
    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandlerAllWpm.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = StiebelEltronHandlerAllWpm.this.comms;
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

            ModbusCommunicationInterface mycomms = StiebelEltronHandlerAllWpm.this.comms;
            StiebelEltronHpV2Configuration myconfig = StiebelEltronHandlerAllWpm.this.config;
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
            }, StiebelEltronHandlerAllWpm.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = StiebelEltronHandlerAllWpm.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandlerAllWpm.class);

    /**
     * Configuration instance
     */
    protected @Nullable StiebelEltronHpV2Configuration config = null;
    /**
     * Parser used to convert incoming raw messages into system blocks
     */
    private final SystemInformationBlockAllWpmParser systemInformationBlockParser = new SystemInformationBlockAllWpmParser();
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
    private final EnergyRuntimeBlockAllWpmParser energyRuntimeBlockParser = new EnergyRuntimeBlockAllWpmParser();
    /**
     * Parser used to convert incoming raw messages into SG Ready Energy Management Settings blocks
     */
    private final SgReadyEnergyManagementSettingsBlockParser sgReadyEnergyManagementSettingsBlockParser = new SgReadyEnergyManagementSettingsBlockParser();
    /**
     * Parser used to convert incoming raw messages into SG Ready Energy Management Settings blocks
     */
    private final SgReadyEnergyManagementSystemInformationBlockParser sgReadyEnergyManagementSystemInformationBlockParser = new SgReadyEnergyManagementSystemInformationBlockParser();
    /**
     * This is the task used to poll the device for system information
     */
    private volatile @Nullable AbstractBasePoller systemInformationPoller = null;
    /**
     * This is the control object for the system information block
     */
    private volatile @Nullable SystemInformationControlAllWpm systemInformationControl = null;
    /**
     * This is the task used to poll the device for system parameter
     */
    private volatile @Nullable AbstractBasePoller systemParameterPoller = null;
    /**
     * This is the control object for the system parameter block
     */
    private volatile @Nullable SystemParameterControlAllWpm systemParameterControl = null;
    /**
     * This is the task used to poll the device for system state
     */
    private volatile @Nullable AbstractBasePoller systemStatePoller = null;
    /**
     * This is the control object for the system state block
     */
    private volatile @Nullable SystemStateControlAllWpm systemStateControl = null;
    /**
     * This is the task used to poll the device for energy & runtime
     */
    private volatile @Nullable AbstractBasePoller energyRuntimePoller = null;
    /**
     * This is the control object for the energy & runtime block
     */
    private volatile @Nullable EnergyRuntimeControlAllWpm energyRuntimeControl = null;
    /**
     * This is the task used to poll the device for SG Ready Energy Management System Information
     */
    private volatile @Nullable AbstractBasePoller sgReadyEnergyManagementSystemInformationPoller = null;
    /**
     * This is the task used to poll the device for SG Ready Energy Management Settings
     */
    private volatile @Nullable AbstractBasePoller sgReadyEnergyManagementSettingsPoller = null;
    /**
     * This is the control object for the SG Ready Energy Management blocks
     */
    private volatile @Nullable SgReadyEnergyManagementControl sgReadyEnMgmtControl = null;
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
     * @param modbusManager the modbus manager
     */
    public StiebelEltronHandlerAllWpm(Thing thing) {
        super(thing);
    }

    /**
     * @param address address of the value to be written on the modbus
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        StiebelEltronHpV2Configuration myconfig = StiebelEltronHandlerAllWpm.this.config;
        ModbusCommunicationInterface mycomms = StiebelEltronHandlerAllWpm.this.comms;

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
            StiebelEltronHandlerAllWpm.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            StiebelEltronHandlerAllWpm.this.handleWriteError(failure);
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
                    case GROUP_SYSTEM_INFORMATION_ALLWPM:
                        poller = systemInformationPoller;
                        break;
                    case GROUP_SYSTEM_PARAMETER_ALLWPM:
                        poller = systemParameterPoller;
                        break;
                    case GROUP_SYSTEM_STATE_ALLWPM:
                        poller = systemStatePoller;
                        break;
                    case GROUP_ENERGY_RUNTIME_INFO_ALLWPM:
                        poller = energyRuntimePoller;
                        break;
                    case GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS:
                        poller = sgReadyEnergyManagementSettingsPoller;
                        break;
                    case GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION:
                        poller = sgReadyEnergyManagementSystemInformationPoller;
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
                if (GROUP_SYSTEM_PARAMETER_ALLWPM.equals(channelUID.getGroupId())) {
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
                } else if (GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS.equals(channelUID.getGroupId())) {
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_SG_READY_ON_OFF_SWITCH:
                            writeInt16(4000, getInt16Value(command));
                            break;
                    }
                    switch (channelUID.getIdWithoutGroup()) {
                        case CHANNEL_SG_READY_INPUT_LINES:
                            int selectedOpState = getInt16Value(command);
                            // only OS1 to OS4 can be used for setting
                            if (selectedOpState < 1) {
                                logger.debug("Unsupported value {} used for setting SG Ready Input Lines",
                                        selectedOpState);
                                break;
                            }
                            short input1 = (short) ((selectedOpState < 3) ? 0 : 1);
                            short input2 = (short) (((selectedOpState == 2) || (selectedOpState == 3)) ? 0 : 1);
                            logger.trace("SG Ready Input Lines will be set like: input1:{} input2:{}", input1, input2);
                            writeInt16(4001, input1);
                            writeInt16(4002, input2);
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
        config = getConfigAs(StiebelEltronHpV2Configuration.class);
        logger.debug("Initializing thing with properties: {}", thing.getProperties());

        startUp();
    }

    /*
     * This method starts the operation of this handler Connect to the slave bridge
     * Start the periodic polling1
     */
    @SuppressWarnings("null")
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
            systemInformationControl = new SystemInformationControlAllWpm(config.getNrOfHps());
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemInformationData(registers);
                }
            };
            int nrOfRegistersToRead = 41 + config.getNrOfHps() * 7;
            poller.registerPollTask(500, nrOfRegistersToRead, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemInformationPoller = poller;
        }
        if (systemParameterPoller == null) {
            systemParameterControl = new SystemParameterControlAllWpm();
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
            systemStateControl = new SystemStateControlAllWpm();
            logger.trace("systemStateControl start Values \n: {}", systemStateControl);
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledSystemStateData(registers);
                }
            };
            int stateBlockLength = config.getStateBlockLength();
            poller.registerPollTask(2500, stateBlockLength, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            systemStatePoller = poller;
        }
        if (energyRuntimePoller == null) {
            energyRuntimeControl = new EnergyRuntimeControlAllWpm(config.getNrOfHps());
            logger.trace("energyRuntimeControl start Values \n: {}", energyRuntimeControl);
            AbstractBasePoller poller = new AbstractBasePoller() {
                @Override
                protected void handlePolledData(ModbusRegisterArray registers) {
                    handlePolledEnergyRuntimeData(registers);
                }
            };
            int nrOfRegistersToRead = 22 + config.getNrOfHps() * 26;
            poller.registerPollTask(3500, nrOfRegistersToRead, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
            energyRuntimePoller = poller;
        }
        if (config.getPollSgReadyFlag()) {
            sgReadyEnMgmtControl = new SgReadyEnergyManagementControl();
            if (sgReadyEnergyManagementSettingsPoller == null) {
                AbstractBasePoller poller = new AbstractBasePoller() {
                    @Override
                    protected void handlePolledData(ModbusRegisterArray registers) {
                        handlePolledSgReadyEnergyManagementSettingsData(registers);
                    }
                };
                poller.registerPollTask(4000, 3, ModbusReadFunctionCode.READ_MULTIPLE_REGISTERS);
                sgReadyEnergyManagementSettingsPoller = poller;
            }
            if (sgReadyEnergyManagementSystemInformationPoller == null) {
                AbstractBasePoller poller = new AbstractBasePoller() {
                    @Override
                    protected void handlePolledData(ModbusRegisterArray registers) {
                        handlePolledSgReadyEnergyManagementSystemInformationData(registers);
                    }
                };
                poller.registerPollTask(5000, 2, ModbusReadFunctionCode.READ_INPUT_REGISTERS);
                sgReadyEnergyManagementSystemInformationPoller = poller;
            }
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

        poller = sgReadyEnergyManagementSettingsPoller;
        if (poller != null) {
            logger.debug("Unregistering sgReadyEnergyManagementSettingsPoller from ModbusManager");
            poller.unregisterPollTask();

            sgReadyEnergyManagementSettingsPoller = null;
        }

        poller = sgReadyEnergyManagementSystemInformationPoller;
        if (poller != null) {
            logger.debug("Unregistering sgReadyEnergyManagementSystemInformationPoller from ModbusManager");
            poller.unregisterPollTask();

            sgReadyEnergyManagementSystemInformationPoller = null;
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
    @SuppressWarnings("null")
    protected void handlePolledSystemInformationData(ModbusRegisterArray registers) {
        logger.trace("System Information block received, size: {}", registers.size());

        SystemInformationBlockAllWpm block = systemInformationBlockParser.parse(registers, systemInformationControl,
                config.getNrOfHps());

        logger.trace("\n {} ", block);
        logger.trace("\n {} ", systemInformationControl);

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.FE7)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FE7_TEMPERATURE),
                    getScaled(block.temperatureFe7, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FE7_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureFe7SetPoint, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.FE7)) {
            logger.trace("FE7 not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FE7_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FE7_TEMPERATURE_SETPOINT), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.FE7, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.FEK)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_TEMPERATURE),
                    getScaled(block.temperatureFek, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureFekSetPoint, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_HUMIDITY),
                    getScaled(block.humidityFek, 10, PERCENT));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_DEWPOINT),
                    getScaled(block.dewpointFek, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.FEK)) {
            logger.trace("FEK not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_TEMPERATURE_SETPOINT), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_HUMIDITY), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FEK_DEWPOINT), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.FEK, true);
        }

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_OUTDOOR_TEMPERATURE),
                getScaled(block.temperatureOutdoor, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HC1_TEMPERATURE),
                getScaled(block.temperatureHc1, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HC1_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureHc1SetPoint, 10, CELSIUS));

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.HC2)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HC2_TEMPERATURE),
                    getScaled(block.temperatureHc2, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HC2_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureHc2SetPoint, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.TEMP_FLOW)) {
            logger.trace("HC2 not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HC2_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HC2_TEMPERATURE_SETPOINT), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.HC2, true);
        }

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HP_FLOW_TEMPERATURE),
                getScaled(block.temperatureFlowHp, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_NHZ_FLOW_TEMPERATURE),
                getScaled(block.temperatureFlowNhz, 10, CELSIUS));

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.TEMP_FLOW)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FLOW_TEMPERATURE),
                    getScaled(block.temperatureFlow, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.FE7)) {
            logger.trace("HP Flow temperature not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FLOW_TEMPERATURE), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.TEMP_FLOW, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.TEMP_RETURN)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_RETURN_TEMPERATURE),
                    getScaled(block.temperatureReturn, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.TEMP_RETURN)) {

            logger.trace("HP Return temperature not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_RETURN_TEMPERATURE), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.TEMP_RETURN, true);
        }

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FIXED_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureFixedSetPoint, 10, CELSIUS));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_BUFFER_TEMPERATURE),
                getScaled(block.temperatureBuffer, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_BUFFER_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureBufferSetPoint, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HEATING_PRESSURE),
                getScaled(block.pressureHeating, 100, BAR));

        // Comment about FlowRate scale
        // It looks like there's a typo in the documentation; data type of flow rate is 2, but the value is about factor
        // 10 to high compared to value reported in ISG web interface; therefore using factor 100 (data type 7)
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FLOW_RATE),
                getScaled(block.flowRate, 100, LITRE_PER_MINUTE));

        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HOTWATER_TEMPERATURE),
                getScaled(block.temperatureWater, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HOTWATER_TEMPERATURE_SETPOINT),
                getScaled(block.temperatureWaterSetPoint, 10, CELSIUS));

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.FAN_COOLING)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FAN_COOLING_TEMPERATURE),
                    getScaled(block.temperatureFanCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FAN_COOLING_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureFanCoolingSetPoint, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.FAN_COOLING)) {
            logger.trace("Fan cooling not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FAN_COOLING_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_FAN_COOLING_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.FAN_COOLING, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.AREA_COOLING)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_AREA_COOLING_TEMPERATURE),
                    getScaled(block.temperatureAreaCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_AREA_COOLING_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureAreaCoolingSetPoint, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.AREA_COOLING)) {
            logger.trace("Area cooling not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_AREA_COOLING_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_AREA_COOLING_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.AREA_COOLING, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.SOLAR_THERMAL)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOLAR_THERMAL_COLLECTOR_TEMPERATURE),
                    getScaled(block.temperatureCollectorSolar, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOLAR_THERMAL_CYLINDER_TEMPERATURE),
                    getScaled(block.temperatureCylinderSolar, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOLAR_THERMAL_RUNTIME),
                    new QuantityType<>(block.runtimeSolar, HOUR));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.SOLAR_THERMAL)) {
            logger.trace("Solar thermal not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOLAR_THERMAL_COLLECTOR_TEMPERATURE),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOLAR_THERMAL_CYLINDER_TEMPERATURE),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOLAR_THERMAL_RUNTIME), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.SOLAR_THERMAL, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.EXTERNAL_HEATING)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE),
                    getScaled(block.temperatureExtHeatSource, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE_SETPOINT),
                    getScaled(block.temperatureExtHeatSourceSetPoint, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_EXT_HEAT_SOURCE_RUNTIME),
                    new QuantityType<>(block.runtimeExtHeatSource, HOUR));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.EXTERNAL_HEATING)) {
            logger.trace("External heating not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_EXT_HEAT_SOURCE_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_EXT_HEAT_SOURCE_RUNTIME), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.EXTERNAL_HEATING, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.LOWER_LIMITS)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_LOWER_APPLICATION_LIMIT_HEATING),
                    getScaled(block.lowerHeatingLimit, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_LOWER_APPLICATION_LIMIT_HOTWATER),
                    getScaled(block.lowerWaterLimit, 10, CELSIUS));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.LOWER_LIMITS)) {
            logger.trace("Lower limits not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_LOWER_APPLICATION_LIMIT_HEATING),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_LOWER_APPLICATION_LIMIT_HOTWATER),
                    UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.LOWER_LIMITS, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.SOURCE_VALUES)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOURCE_TEMPERATURE),
                    getScaled(block.temperatureSource, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_MIN_SOURCE_TEMPERATURE),
                    getScaled(block.temperatureSourceMin, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOURCE_PRESSURE),
                    getScaled(block.pressureSource, 100, BAR));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.SOURCE_VALUES)) {
            logger.trace("Source values not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOURCE_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_MIN_SOURCE_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_SOURCE_PRESSURE), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.SOURCE_VALUES, true);
        }

        if (systemInformationControl.featureAvailable(SysInfoFeatureKeys.HOTGAS)) {
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HOTGAS_TEMPERATURE),
                    getScaled(block.temperatureHotgas, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HIGH_PRESSURE),
                    getScaled(block.pressureHigh, 100, BAR));
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_LOW_PRESSURE),
                    getScaled(block.pressureLow, 100, BAR));
        } else if (!systemInformationControl.featureReported(SysInfoFeatureKeys.HOTGAS)) {
            logger.trace("Hotgas values not available");
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HOTGAS_TEMPERATURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_HIGH_PRESSURE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM, CHANNEL_LOW_PRESSURE), UnDefType.UNDEF);
            systemInformationControl.setFeatureReported(SysInfoFeatureKeys.HOTGAS, true);
        }

        for (int idx = 0; idx < config.getNrOfHps(); idx++) {
            SysInfoHpFeature hpFeaturesObj = systemInformationControl.hpSysInfoList[idx];
            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_TEMPERATURE_RETURN)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_RETURN_TEMPERATURE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].temperatureReturn, 10, CELSIUS));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_TEMPERATURE_RETURN)) {
                logger.trace("HP{} Return temperature not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_RETURN_TEMPERATURE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_TEMPERATURE_RETURN, true);
            }

            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_TEMPERATURE_FLOW)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_FLOW_TEMPERATURE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].temperatureFlow, 10, CELSIUS));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_TEMPERATURE_FLOW)) {
                logger.trace("HP{} Flow temperature not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_FLOW_TEMPERATURE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_TEMPERATURE_FLOW, true);
            }

            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_TEMPERATURE_HOTGAS)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_HOTGAS_TEMPERATURE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].temperatureFlow, 10, CELSIUS));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_TEMPERATURE_HOTGAS)) {
                logger.trace("HP{} Hotgas temperature not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_HOTGAS_TEMPERATURE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_TEMPERATURE_HOTGAS, true);
            }

            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_PRESSURE_LOW)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_LOW_PRESSURE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].pressureLow, 100, BAR));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_PRESSURE_LOW)) {
                logger.trace("HP{} Low pressure not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_LOW_PRESSURE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_PRESSURE_LOW, true);
            }
            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_PRESSURE_MEAN)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_MEAN_PRESSURE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].pressureMean, 100, BAR));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_PRESSURE_MEAN)) {
                logger.trace("HP{} Mean pressure not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_MEAN_PRESSURE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_PRESSURE_MEAN, true);
            }
            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_PRESSURE_HIGH)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_HIGH_PRESSURE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].pressureHigh, 100, BAR));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_PRESSURE_HIGH)) {
                logger.trace("HP{} High pressure not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_HIGH_PRESSURE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_PRESSURE_HIGH, true);
            }
            if (hpFeaturesObj.available(SysInfoHpFeaturelKeys.HP_FLOW_RATE)) {
                updateState(
                        channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                                String.format(CHANNEL_HP_FLOW_RATE_FORMAT, idx + 1)),
                        getScaled(block.heatPumps[idx].flowRate, 100, LITRE_PER_MINUTE));
            } else if (!hpFeaturesObj.reported(SysInfoHpFeaturelKeys.HP_FLOW_RATE)) {
                logger.trace("HP{} Flow rate not available", idx + 1);
                updateState(channelUID(GROUP_SYSTEM_INFORMATION_ALLWPM,
                        String.format(CHANNEL_HP_FLOW_RATE_FORMAT, idx + 1)), UnDefType.UNDEF);
                hpFeaturesObj.setReported(SysInfoHpFeaturelKeys.HP_FLOW_RATE, true);
            }

        }

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @SuppressWarnings("null")
    protected void handlePolledSystemParameterData(ModbusRegisterArray registers) {
        logger.trace("System parameter block received, size: {}", registers.size());

        SystemParameterBlockAllWpm block = systemParameterBlockParser.parse(registers, systemParameterControl);
        logger.trace("\n {}", block);
        logger.trace("\n {}", systemParameterControl);

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_OPERATING_MODE),
                new DecimalType(block.operationMode));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HC1_COMFORT_TEMPERATURE),
                getScaled(block.comfortTemperatureHeating, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HC1_ECO_TEMPERATURE),
                getScaled(block.ecoTemperatureHeating, 10, CELSIUS));
        double heatingCurveRiseHeatingHk1 = block.heatingCurveRiseHc1 / 100.0;
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HC1_HEATING_CURVE_RISE),
                new DecimalType(heatingCurveRiseHeatingHk1));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HC2_COMFORT_TEMPERATURE),
                getScaled(block.comfortTemperatureHeatingHc2, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HC2_ECO_TEMPERATURE),
                getScaled(block.ecoTemperatureHeatingHc2, 10, CELSIUS));
        double heatingCurveRiseHeatingHk2 = block.heatingCurveRiseHc2 / 100.0;
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HC2_HEATING_CURVE_RISE),
                new DecimalType(heatingCurveRiseHeatingHk2));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FIXED_VALUE_OPERATION),
                getScaled(block.fixedValueOperation, 10, CELSIUS));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HEATING_DUAL_MODE_TEMPERATURE),
                getScaled(block.dualModeTemperatureHeating, 10, CELSIUS));

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HOTWATER_COMFORT_TEMPERATURE),
                getScaled(block.comfortTemperatureWater, 10, CELSIUS));
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HOTWATER_ECO_TEMPERATURE),
                getScaled(block.ecoTemperatureWater, 10, CELSIUS));

        if (systemParameterControl.featureAvailable(SysParamFeatureKeys.WATER_STAGES)) {
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HOTWATER_STAGES),
                    new DecimalType(block.hotwaterStages));
        } else if (!systemParameterControl.featureReported(SysParamFeatureKeys.WATER_STAGES)) {
            logger.info("water stages not available");
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HOTWATER_STAGES), UnDefType.UNDEF);
            systemParameterControl.setFeatureReported(SysParamFeatureKeys.WATER_STAGES, true);
        }

        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_HOTWATER_DUAL_MODE_TEMPERATURE),
                getScaled(block.hotwaterDualModeTemperature, 10, CELSIUS));

        if (systemParameterControl.featureAvailable(SysParamFeatureKeys.AREA_COOLING)) {
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_SETPOINT),
                    getScaled(block.flowTemperatureAreaCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_HYSTERESIS),
                    getScaled(block.roomTemperatureAreaCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_AREA_COOLING_ROOM_TEMPERATURE_SETPOINT),
                    getScaled(block.roomTemperatureAreaCooling, 10, CELSIUS));
        } else if (!systemParameterControl.featureReported(SysParamFeatureKeys.AREA_COOLING)) {
            logger.info("areaCooling values not available");
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_AREA_COOLING_FLOW_TEMPERATURE_HYSTERESIS),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_AREA_COOLING_ROOM_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            systemParameterControl.setFeatureReported(SysParamFeatureKeys.AREA_COOLING, true);
        }

        if (systemParameterControl.featureAvailable(SysParamFeatureKeys.FAN_COOLING)) {
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_SETPOINT),
                    getScaled(block.flowTemperatureFanCooling, 10, CELSIUS));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_HYSTERESIS),
                    getScaled(block.flowTemperatureHysteresisFanCooling, 10, KELVIN));
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FAN_COOLING_ROOM_TEMPERATURE_SETPOINT),
                    getScaled(block.roomTemperatureFanCooling, 10, CELSIUS));
        } else if (!systemParameterControl.featureReported(SysParamFeatureKeys.FAN_COOLING)) {
            logger.info("fanCooling values not available");
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FAN_COOLING_FLOW_TEMPERATURE_HYSTERESIS),
                    UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_FAN_COOLING_ROOM_TEMPERATURE_SETPOINT),
                    UnDefType.UNDEF);
            systemParameterControl.setFeatureReported(SysParamFeatureKeys.FAN_COOLING, true);
        }

        logger.trace("reset = {}", block.reset);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_RESET), new DecimalType(block.reset));
        logger.trace("restartISG = {}", block.restartIsg);
        updateState(channelUID(GROUP_SYSTEM_PARAMETER_ALLWPM, CHANNEL_RESTART_ISG), new DecimalType(block.restartIsg));

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @SuppressWarnings("null")
    protected void handlePolledSystemStateData(ModbusRegisterArray registers) {
        logger.trace("System state block received, size: {}", registers.size());

        SystemStateBlockAllWpm block = systemStateBlockParser.parse(registers, systemStateControl);
        logger.trace("\n {}", block);
        logger.trace("\n {}", systemStateControl);

        // operating status bit-coded
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_HC1_PUMP_ACTIVE),
                (block.state & 1) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_HC2_PUMP_ACTIVE),
                (block.state & 2) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_HEAT_UP_PROGRAM_ACTIVE),
                (block.state & 4) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_NHZ_RUNNING),
                (block.state & 8) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_HP_IN_HEATING_MODE),
                (block.state & 16) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_HP_IN_HOTWATER_MODE),
                (block.state & 32) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR_RUNNING),
                (block.state & 64) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_SUMMER_MODE_ACTIVE),
                (block.state & 128) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COOLING_MODE_ACTIVE),
                (block.state & 256) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_MIN_ONE_IWS_IN_DEFROSTING_MODE),
                (block.state & 512) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);

        if (config.getWpmControllerId() == StiebelEltronHpV2Configuration.WPM3
                || config.getWpmControllerId() == StiebelEltronHpV2Configuration.WPMSYSTEM) {
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_SILENT_MODE1_ACTIVE),
                    (block.operatingStatus & 1024) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_SILENT_MODE2_ACTIVE),
                    (block.operatingStatus & 2048) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        } else if (!systemStateControl.featureReported(SystemStateFeatureKeys.SILENT_MODES)) {
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_SILENT_MODE1_ACTIVE), UnDefType.NULL);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_SILENT_MODE2_ACTIVE), UnDefType.NULL);
            systemStateControl.setFeatureReported(SystemStateFeatureKeys.SILENT_MODES, true);
        }

        // power-off bit-coded
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_POWER_OFF),
                (block.powerOff == 1) ? OpenClosedType.OPEN : OpenClosedType.CLOSED);

        if (systemStateControl.featureAvailable(SystemStateFeatureKeys.OPERATING_STATUS)) {
            logger.trace("operatingStatus = {}", block.operatingStatus);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR1_ACTIVE),
                    (block.operatingStatus & 1) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR2_ACTIVE),
                    (block.operatingStatus & 2) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR3_ACTIVE),
                    (block.operatingStatus & 4) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR4_ACTIVE),
                    (block.operatingStatus & 8) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR5_ACTIVE),
                    (block.operatingStatus & 16) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR6_ACTIVE),
                    (block.operatingStatus & 32) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP1_ACTIVE),
                    (block.operatingStatus & 64) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP2_ACTIVE),
                    (block.operatingStatus & 128) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP3_ACTIVE),
                    (block.operatingStatus & 256) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP4_ACTIVE),
                    (block.operatingStatus & 512) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP5_ACTIVE),
                    (block.operatingStatus & 1024) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP6_ACTIVE),
                    (block.operatingStatus & 2048) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_NHZ1_ACTIVE),
                    (block.operatingStatus & 4096) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_NHZ2_ACTIVE),
                    (block.operatingStatus & 8192) != 0 ? OpenClosedType.OPEN : OpenClosedType.CLOSED);
        } else if (!systemStateControl.featureReported(SystemStateFeatureKeys.OPERATING_STATUS)) {
            logger.info("Operating status not available");
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR1_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR2_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR3_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR4_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR5_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_COMPRESSOR6_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP1_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP2_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP3_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP4_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP5_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUFFER_CHARGING_PUMP6_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_NHZ1_ACTIVE), UnDefType.UNDEF);
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_NHZ2_ACTIVE), UnDefType.UNDEF);
            systemStateControl.setFeatureReported(SystemStateFeatureKeys.OPERATING_STATUS, true);
        }

        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_FAULT_STATUS), new DecimalType(block.faultStatus));
        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_BUS_STATUS), new DecimalType(block.busStatus));

        if (systemStateControl.featureAvailable(SystemStateFeatureKeys.DEFROST_INITIATED)) {
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_DEFROST_INITIATED),
                    new DecimalType(block.defrostInitiated));
        } else if (!systemStateControl.featureReported(SystemStateFeatureKeys.DEFROST_INITIATED)) {
            logger.info("DefrostInitiated status not available");
            updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_DEFROST_INITIATED), UnDefType.UNDEF);
            systemStateControl.setFeatureReported(SystemStateFeatureKeys.DEFROST_INITIATED, true);
        }

        updateState(channelUID(GROUP_SYSTEM_STATE_ALLWPM, CHANNEL_ACTIVE_ERROR), new DecimalType(block.activeError));

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @SuppressWarnings("null")
    protected void handlePolledEnergyRuntimeData(ModbusRegisterArray registers) {
        logger.trace("Energy block received, size: {}", registers.size());

        EnergyRuntimeBlockAllWpm energyRuntimeBlock = energyRuntimeBlockParser.parse(registers, energyRuntimeControl,
                config.getNrOfHps());

        logger.trace("\n {}", energyRuntimeBlock);
        logger.trace("\n {}", energyRuntimeControl);

        // Energy and runtime information group
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_PRODUCTION_HEAT_TODAY),
                new QuantityType<>(energyRuntimeBlock.productionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_PRODUCTION_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionHeatTotalHigh, energyRuntimeBlock.productionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_PRODUCTION_WATER_TODAY),
                new QuantityType<>(energyRuntimeBlock.productionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_PRODUCTION_WATER_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionWaterTotalHigh, energyRuntimeBlock.productionWaterTotalLow));

        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_PRODUCTION_NHZ_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionNhzHeatingTotalHigh, energyRuntimeBlock.productionNhzHeatingTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_PRODUCTION_NHZ_WATER_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.productionNhzHotwaterTotalHigh, energyRuntimeBlock.productionNhzHotwaterTotalLow));

        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_CONSUMPTION_HEAT_TODAY),
                new QuantityType<>(energyRuntimeBlock.consumptionHeatToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_CONSUMPTION_HEAT_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.consumptionHeatTotalHigh, energyRuntimeBlock.consumptionHeatTotalLow));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_CONSUMPTION_WATER_TODAY),
                new QuantityType<>(energyRuntimeBlock.consumptionWaterToday, KILOWATT_HOUR));
        updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_CONSUMPTION_WATER_TOTAL), getEnergyQuantity(
                energyRuntimeBlock.consumptionWaterTotalHigh, energyRuntimeBlock.consumptionWaterTotalLow));

        if (energyRuntimeControl.featureAvailable(EnergyRuntimeFeatureKeys.COMMON_RUNTIMES)) {
            logger.trace("runtimeCompressorHeating = {}", energyRuntimeBlock.runtimeCompressorHeating);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_HEATING_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.runtimeCompressorHeating, HOUR));
            logger.trace("runtimeCompressorHotwater = {}", energyRuntimeBlock.runtimeCompressorHotwater);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_HOTWATER_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.runtimeCompressorHotwater, HOUR));

            logger.trace("runtimeNhz1 = {}", energyRuntimeBlock.runtimeNhz1);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_NHZ1_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.runtimeNhz1, HOUR));
            logger.trace("runtimeNhz2 = {}", energyRuntimeBlock.runtimeNhz2);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_NHZ2_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.runtimeNhz2, HOUR));
            logger.trace("runtimeNhz12 = {}", energyRuntimeBlock.runtimeNhz12);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_NHZ12_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.runtimeNhz12, HOUR));
        } else if (!energyRuntimeControl.featureReported(EnergyRuntimeFeatureKeys.COMMON_RUNTIMES)) {
            logger.trace("Common compressor runtimes not available");
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_HEATING_RUNTIME), UnDefType.NULL);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_HOTWATER_RUNTIME), UnDefType.NULL);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_NHZ1_RUNTIME), UnDefType.NULL);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_NHZ2_RUNTIME), UnDefType.NULL);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_NHZ12_RUNTIME), UnDefType.NULL);
            energyRuntimeControl.setFeatureReported(EnergyRuntimeFeatureKeys.COMMON_RUNTIMES, true);
        }

        if (energyRuntimeControl.featureAvailable(EnergyRuntimeFeatureKeys.COMMON_COOLING_RUNTIME)) {
            logger.trace("runtimeCooling = {}", energyRuntimeBlock.runtimeCompressorCooling);
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_COOLING_RUNTIME),
                    new QuantityType<>(energyRuntimeBlock.runtimeCompressorCooling, HOUR));
        } else if (!energyRuntimeControl.featureReported(EnergyRuntimeFeatureKeys.COMMON_COOLING_RUNTIME)) {
            logger.trace("Common cooling runtime not available");
            updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM, CHANNEL_COOLING_RUNTIME), UnDefType.NULL);
            energyRuntimeControl.setFeatureReported(EnergyRuntimeFeatureKeys.COMMON_COOLING_RUNTIME, true);
        }

        for (int idx = 0; idx < config.getNrOfHps(); idx++) {
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_PRODUCTION_HEAT_TODAY_FORMAT, idx + 1)),
                    new QuantityType<>(energyRuntimeBlock.heatPumps[0].productionHeatToday, KILOWATT_HOUR));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_PRODUCTION_HEAT_TOTAL_FORMAT, idx + 1)),
                    getEnergyQuantity(energyRuntimeBlock.heatPumps[0].productionHeatTotalHigh,
                            energyRuntimeBlock.heatPumps[0].productionHeatTotalLow));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_PRODUCTION_WATER_TODAY_FORMAT, idx + 1)),
                    new QuantityType<>(energyRuntimeBlock.heatPumps[0].productionWaterToday, KILOWATT_HOUR));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_PRODUCTION_WATER_TOTAL_FORMAT, idx + 1)),
                    getEnergyQuantity(energyRuntimeBlock.heatPumps[0].productionWaterTotalHigh,
                            energyRuntimeBlock.heatPumps[0].productionWaterTotalLow));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_PRODUCTION_NHZ_HEAT_TOTAL_FORMAT, idx + 1)),
                    getEnergyQuantity(energyRuntimeBlock.heatPumps[0].productionNhzHeatingTotalHigh,
                            energyRuntimeBlock.heatPumps[0].productionNhzHeatingTotalLow));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_PRODUCTION_NHZ_WATER_TOTAL_FORMAT, idx + 1)),
                    getEnergyQuantity(energyRuntimeBlock.heatPumps[0].productionNhzHotwaterTotalHigh,
                            energyRuntimeBlock.heatPumps[0].productionNhzHotwaterTotalLow));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_CONSUMPTION_HEAT_TODAY_FORMAT, idx + 1)),
                    new QuantityType<>(energyRuntimeBlock.heatPumps[0].consumptionHeatToday, KILOWATT_HOUR));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_CONSUMPTION_HEAT_TOTAL_FORMAT, idx + 1)),
                    getEnergyQuantity(energyRuntimeBlock.heatPumps[0].consumptionHeatTotalHigh,
                            energyRuntimeBlock.heatPumps[0].consumptionHeatTotalLow));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_CONSUMPTION_WATER_TODAY_FORMAT, idx + 1)),
                    new QuantityType<>(energyRuntimeBlock.heatPumps[0].consumptionWaterToday, KILOWATT_HOUR));
            updateState(
                    channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                            String.format(CHANNEL_HP_CONSUMPTION_WATER_TOTAL_FORMAT, idx + 1)),
                    getEnergyQuantity(energyRuntimeBlock.heatPumps[0].consumptionWaterTotalHigh,
                            energyRuntimeBlock.heatPumps[0].consumptionWaterTotalLow));

            EnergyRuntimeHpFeature hpFeaturesObj = energyRuntimeControl.hpEgRtList[idx];
            if (hpFeaturesObj.featureAvailable(EnergyRuntimeHpFeatureKeys.RUNTIMES)) {
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_CP1_HEATING_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressor1Heating, HOUR));
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_CP2_HEATING_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressor2Heating, HOUR));
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_CP12_HEATING_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressor12Heating, HOUR));
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_CP1_HOTWATER_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressor1Hotwater, HOUR));
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_CP2_HOTWATER_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressor2Hotwater, HOUR));
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_CP12_HOTWATER_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressor12Hotwater, HOUR));
            } else if (!hpFeaturesObj.featureReported(EnergyRuntimeHpFeatureKeys.RUNTIMES)) {
                logger.trace("HP{} compressor runtimes not available", idx + 1);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_CP1_HEATING_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_CP2_HEATING_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_CP12_HEATING_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_CP1_HOTWATER_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_CP2_HOTWATER_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_CP12_HOTWATER_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                hpFeaturesObj.setFeatureReported(EnergyRuntimeHpFeatureKeys.RUNTIMES, true);
            }

            if (hpFeaturesObj.featureAvailable(EnergyRuntimeHpFeatureKeys.COOLING_RUNTIME)) {
                updateState(
                        channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                                String.format(CHANNEL_HP_COOLING_RUNTIME_FORMAT, idx + 1)),
                        new QuantityType<>(energyRuntimeBlock.heatPumps[0].runtimeCompressorCooling, HOUR));
            } else if (!hpFeaturesObj.featureReported(EnergyRuntimeHpFeatureKeys.COOLING_RUNTIME)) {
                logger.trace("HP{} cooling runtime not available", idx + 1);
                updateState(channelUID(GROUP_ENERGY_RUNTIME_INFO_ALLWPM,
                        String.format(CHANNEL_HP_COOLING_RUNTIME_FORMAT, idx + 1)), UnDefType.NULL);
                hpFeaturesObj.setFeatureReported(EnergyRuntimeHpFeatureKeys.COOLING_RUNTIME, true);
            }
        }

        resetCommunicationError();
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @SuppressWarnings("null")
    protected void handlePolledSgReadyEnergyManagementSettingsData(ModbusRegisterArray registers) {
        logger.trace("SG Ready Energy Management Settings block received, size: {}", registers.size());

        SgReadyEnergyManagementSettingsBlock block = sgReadyEnergyManagementSettingsBlockParser.parse(registers,
                sgReadyEnMgmtControl);

        logger.trace("\n {}", block);
        logger.trace("\n {}", sgReadyEnMgmtControl);

        if (sgReadyEnMgmtControl.featureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS)) {
            updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS, CHANNEL_SG_READY_ON_OFF_SWITCH),
                    new DecimalType(block.sgReadyOnOffSwitch));
            int sgReadyInputLines = -1;
            if (!sgReadyEnMgmtControl.featureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS_INPUT1)
                    && !sgReadyEnMgmtControl.featureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS_INPUT2)) {
                sgReadyInputLines = -1;
            } else if (block.sgReadyOnOffSwitch == 0) {
                sgReadyInputLines = 0;
            } else {
                if ((block.sgReadyInput1 == 0) && (block.sgReadyInput2 == 1)) {
                    sgReadyInputLines = 1;
                } else if ((block.sgReadyInput1 == 0) && (block.sgReadyInput2 == 0)) {
                    sgReadyInputLines = 2;
                } else if ((block.sgReadyInput1 == 1) && (block.sgReadyInput2 == 0)) {
                    sgReadyInputLines = 3;
                } else if ((block.sgReadyInput1 == 1) && (block.sgReadyInput2 == 1)) {
                    sgReadyInputLines = 4;
                }
            }
            updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS, CHANNEL_SG_READY_INPUT_LINES),
                    new DecimalType(sgReadyInputLines));
        } else if (!sgReadyEnMgmtControl.featureReported(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS)) {
            logger.info("SG Ready Energy Management Settings not available");
            updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS, CHANNEL_SG_READY_ON_OFF_SWITCH),
                    UnDefType.NULL);
            updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS, CHANNEL_SG_READY_INPUT_LINES),
                    UnDefType.NULL);
            sgReadyEnMgmtControl.setFeatureReported(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS, true);
            sgReadyEnMgmtControl.setFeatureReported(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS_INPUT1, true);
            sgReadyEnMgmtControl.setFeatureReported(SgReadyEnMgmtFeatureKeys.EN_MGMT_SETTINGS_INPUT2, true);
        }
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    @SuppressWarnings("null")
    protected void handlePolledSgReadyEnergyManagementSystemInformationData(ModbusRegisterArray registers) {
        logger.trace("SG Ready Energy Management Settings block received, size: {}", registers.size());

        SgReadyEnergyManagementSystemInformationBlock block = sgReadyEnergyManagementSystemInformationBlockParser
                .parse(registers, sgReadyEnMgmtControl);

        logger.trace("\n {}", block);
        logger.trace("\n {}", sgReadyEnMgmtControl);

        if (sgReadyEnMgmtControl.featureAvailable(SgReadyEnMgmtFeatureKeys.EN_MGMT_SYS_INFO)) {
            updateState(
                    channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION, CHANNEL_SG_READY_OPERATING_STATE),
                    new DecimalType(block.sgReadyOperatingState));
            updateState(
                    channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION, CHANNEL_SG_READY_CONTROLLER_IDENT),
                    new DecimalType(block.sgReadyControllerIdentification));
            // If the SgReady System Information Registers are available and the configured
            // WPM ControllerID doesn't match, the one from the WPM is applied
            if (config.getWpmControllerId() != block.sgReadyControllerIdentification) {
                int newWpmCtrlId = block.sgReadyControllerIdentification;
                logger.info("Updating configured WPM Controller ID from '{}' to '{}' (read from SG Ready register)!",
                        config.getWpmControllerId(), newWpmCtrlId);
                config.setWpmControllerId(block.sgReadyControllerIdentification);
            }
        } else if (!sgReadyEnMgmtControl.featureReported(SgReadyEnMgmtFeatureKeys.EN_MGMT_SYS_INFO)) {
            logger.info("SG Ready Energy Management system information not available");
            updateState(
                    channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION, CHANNEL_SG_READY_OPERATING_STATE),
                    UnDefType.NULL);
            updateState(
                    channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION, CHANNEL_SG_READY_CONTROLLER_IDENT),
                    UnDefType.NULL);
            sgReadyEnMgmtControl.setFeatureReported(SgReadyEnMgmtFeatureKeys.EN_MGMT_SYS_INFO, true);
        }
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
