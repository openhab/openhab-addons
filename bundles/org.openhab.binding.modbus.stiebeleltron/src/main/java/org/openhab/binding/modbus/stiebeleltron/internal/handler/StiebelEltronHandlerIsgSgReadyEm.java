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
import static org.openhab.core.library.unit.Units.KILOWATT_HOUR;

import java.util.Optional;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.modbus.handler.EndpointNotInitializedException;
import org.openhab.binding.modbus.handler.ModbusEndpointThingHandler;
import org.openhab.binding.modbus.stiebeleltron.internal.StiebelEltronConfiguration;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementSettingsBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.dto.SgReadyEnergyManagementSystemInformationBlock;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SgReadyEnergyManagementSettingsBlockParser;
import org.openhab.binding.modbus.stiebeleltron.internal.parser.SgReadyEnergyManagementSystemInformationBlockParser;
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

import net.wimpi.modbus.Modbus;

/**
 * The {@link Modbus.StiebelEltronHandler} is responsible for handling commands,
 * which are sent to one of the channels and for polling the modbus.
 *
 * @author Paul Frank - Initial contribution
 * @author Thomas Burri - Thing handler for the ISG SG Ready Energy Management
 */
@NonNullByDefault
public class StiebelEltronHandlerIsgSgReadyEm extends BaseThingHandler {
    public abstract class AbstractBasePoller {

        private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandlerIsgSgReadyEm.class);

        private volatile @Nullable PollTask pollTask;

        public synchronized void unregisterPollTask() {
            PollTask task = pollTask;
            if (task == null) {
                return;
            }

            ModbusCommunicationInterface mycomms = StiebelEltronHandlerIsgSgReadyEm.this.comms;
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

            ModbusCommunicationInterface mycomms = StiebelEltronHandlerIsgSgReadyEm.this.comms;
            StiebelEltronConfiguration myconfig = StiebelEltronHandlerIsgSgReadyEm.this.config;
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
            }, StiebelEltronHandlerIsgSgReadyEm.this::handleReadError);
        }

        public synchronized void poll() {
            PollTask task = pollTask;
            ModbusCommunicationInterface mycomms = StiebelEltronHandlerIsgSgReadyEm.this.comms;
            if (task != null && mycomms != null) {
                mycomms.submitOneTimePoll(task.getRequest(), task.getResultCallback(), task.getFailureCallback());
            }
        }

        protected abstract void handlePolledData(ModbusRegisterArray registers);
    }

    /**
     * Logger instance
     */
    private final Logger logger = LoggerFactory.getLogger(StiebelEltronHandlerIsgSgReadyEm.class);

    /**
     * Configuration instance
     */
    protected @Nullable StiebelEltronConfiguration config = null;
    /**
     * Parser used to convert incoming raw messages into SG Ready Energy Management Settings blocks
     */
    private final SgReadyEnergyManagementSettingsBlockParser sgReadyEnergyManagementSettingsBlockParser = new SgReadyEnergyManagementSettingsBlockParser();
    /**
     * Parser used to convert incoming raw messages into SG Ready Energy Management Settings blocks
     */
    private final SgReadyEnergyManagementSystemInformationBlockParser sgReadyEnergyManagementSystemInformationBlockParser = new SgReadyEnergyManagementSystemInformationBlockParser();
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller sgReadyEnergyManagementSettingsPoller = null;
    /**
     * This is the task used to poll the device
     */
    private volatile @Nullable AbstractBasePoller sgReadyEnergyManagementSystemInformationPoller = null;
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
    public StiebelEltronHandlerIsgSgReadyEm(Thing thing) {
        super(thing);
    }

    /**
     * @param address address of the value to be written on the modbus
     * @param shortValue value to be written on the modbus
     */
    protected void writeInt16(int address, short shortValue) {
        StiebelEltronConfiguration myconfig = StiebelEltronHandlerIsgSgReadyEm.this.config;
        ModbusCommunicationInterface mycomms = StiebelEltronHandlerIsgSgReadyEm.this.comms;

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
            StiebelEltronHandlerIsgSgReadyEm.this.updateStatus(ThingStatus.ONLINE);
        }, failure -> {
            StiebelEltronHandlerIsgSgReadyEm.this.handleWriteError(failure);
        });
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
                if (GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS.equals(channelUID.getGroupId())) {
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
            sgReadyEnergyManagementSettingsPoller = poller;
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
        AbstractBasePoller poller = sgReadyEnergyManagementSettingsPoller;
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
     * @param scaleFactor the scale factor (division)
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
    protected void handlePolledSgReadyEnergyManagementSettingsData(ModbusRegisterArray registers) {
        logger.trace("SG Ready Energy Management Settings block received, size: {}", registers.size());

        SgReadyEnergyManagementSettingsBlock block = sgReadyEnergyManagementSettingsBlockParser.parse(registers);

        logger.trace("SG Ready Switch  = {}", block.sgReadyOnOffSwitch);
        logger.trace("SG Ready Input 1 = {}", block.sgReadyInput1);
        logger.trace("SG Ready Input 2 = {}", block.sgReadyInput2);

        updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SETTINGS, CHANNEL_SG_READY_ON_OFF_SWITCH),
                new DecimalType(block.sgReadyOnOffSwitch));

        int sgReadyInputLines = -1;
        if ((block.sgReadyInput1 == 32768) || (block.sgReadyInput2 == 32768)) {
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
    }

    /**
     * This method is called each time new data has been polled from the modbus
     * slave The register array is first parsed, then each of the channels are
     * updated to the new values
     *
     * @param registers byte array read from the modbus slave
     */
    protected void handlePolledSgReadyEnergyManagementSystemInformationData(ModbusRegisterArray registers) {
        logger.trace("SG Ready Energy Management Settings block received, size: {}", registers.size());

        SgReadyEnergyManagementSystemInformationBlock block = sgReadyEnergyManagementSystemInformationBlockParser
                .parse(registers);

        logger.trace("SG Ready Operating State           = {}", block.sgReadyOperatingState);
        logger.trace("SG Ready Controller Identification = {}", block.sgReadyControllerIdentification);

        updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION, CHANNEL_SG_READY_OPERATING_STATE),
                new DecimalType(block.sgReadyOperatingState));
        updateState(channelUID(GROUP_SG_READY_ENERGY_MANAGEMENT_SYSTEM_INFORMATION, CHANNEL_SG_READY_CONTROLLER_IDENT),
                new DecimalType(block.sgReadyControllerIdentification));
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
