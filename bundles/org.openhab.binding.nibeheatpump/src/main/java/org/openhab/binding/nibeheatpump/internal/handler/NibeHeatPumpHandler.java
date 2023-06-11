/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.nibeheatpump.internal.handler;

import static org.openhab.binding.nibeheatpump.internal.NibeHeatPumpBindingConstants.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpCommandResult;
import org.openhab.binding.nibeheatpump.internal.NibeHeatPumpException;
import org.openhab.binding.nibeheatpump.internal.config.NibeHeatPumpConfiguration;
import org.openhab.binding.nibeheatpump.internal.connection.NibeHeatPumpConnector;
import org.openhab.binding.nibeheatpump.internal.connection.NibeHeatPumpEventListener;
import org.openhab.binding.nibeheatpump.internal.connection.SerialConnector;
import org.openhab.binding.nibeheatpump.internal.connection.SimulatorConnector;
import org.openhab.binding.nibeheatpump.internal.connection.UDPConnector;
import org.openhab.binding.nibeheatpump.internal.message.ModbusDataReadOutMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusReadResponseMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusValue;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteRequestMessage;
import org.openhab.binding.nibeheatpump.internal.message.ModbusWriteResponseMessage;
import org.openhab.binding.nibeheatpump.internal.message.NibeHeatPumpMessage;
import org.openhab.binding.nibeheatpump.internal.models.PumpModel;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation.NibeDataType;
import org.openhab.binding.nibeheatpump.internal.models.VariableInformation.Type;
import org.openhab.core.io.transport.serial.SerialPortManager;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NibeHeatPumpHandler} is responsible for handling commands, which
 * are sent to one of the channels.
 *
 * @author Pauli Anttila - Initial contribution
 */
public class NibeHeatPumpHandler extends BaseThingHandler implements NibeHeatPumpEventListener {

    private static final int TIMEOUT = 4500;
    private final Logger logger = LoggerFactory.getLogger(NibeHeatPumpHandler.class);
    private final PumpModel pumpModel;
    private final SerialPortManager serialPortManager;
    private final List<Integer> itemsToPoll = Collections.synchronizedList(new ArrayList<>());
    private final List<Integer> itemsToEnableWrite = new ArrayList<>();
    private final Map<Integer, CacheObject> stateMap = Collections.synchronizedMap(new HashMap<>());
    private NibeHeatPumpConfiguration configuration;
    private NibeHeatPumpConnector connector;
    private boolean reconnectionRequest;
    private NibeHeatPumpCommandResult writeResult;
    private NibeHeatPumpCommandResult readResult;
    private final Runnable pollingRunnable = new Runnable() {
        @Override
        public void run() {
            if (!configuration.enableReadCommands) {
                logger.trace("All read commands denied, skip polling!");
                return;
            }

            List<Integer> items;
            synchronized (itemsToPoll) {
                items = new ArrayList<>(itemsToPoll);
            }

            for (int item : items) {
                if (connector != null && connector.isConnected()
                        && getThing().getStatusInfo().getStatus() == ThingStatus.ONLINE) {
                    CacheObject oldValue = stateMap.get(item);
                    if (oldValue == null
                            || (oldValue.lastUpdateTime + refreshIntervalMillis()) < System.currentTimeMillis()) {
                        // it's time to refresh data
                        logger.debug("Time to refresh variable '{}' data", item);

                        ModbusReadRequestMessage request = new ModbusReadRequestMessage.MessageBuilder()
                                .coilAddress(item).build();

                        try {
                            readResult = sendMessageToNibe(request);
                            ModbusReadResponseMessage result = (ModbusReadResponseMessage) readResult.get(TIMEOUT,
                                    TimeUnit.MILLISECONDS);
                            if (result != null) {
                                if (request.getCoilAddress() != result.getCoilAddress()) {
                                    logger.debug("Data from wrong register '{}' received, expected '{}'",
                                            result.getCoilAddress(), request.getCoilAddress());
                                }
                                // update variable anyway
                                handleVariableUpdate(pumpModel, result.getValueAsModbusValue());
                            }
                        } catch (TimeoutException e) {
                            logger.debug("Message sending to heat pump failed, no response");
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        } catch (InterruptedException e) {
                            logger.debug("Message sending to heat pump failed, sending interrupted");
                        } catch (NibeHeatPumpException e) {
                            logger.debug("Message sending to heat pump failed, exception {}", e.getMessage());
                            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                        } finally {
                            readResult = null;
                        }
                    }
                }
            }
        }
    };
    private ScheduledFuture<?> connectorTask;
    private ScheduledFuture<?> pollingJob;
    private long lastUpdateTime = 0;

    public NibeHeatPumpHandler(Thing thing, PumpModel pumpModel, SerialPortManager serialPortManager) {
        super(thing);
        this.pumpModel = pumpModel;
        this.serialPortManager = serialPortManager;
    }

    private NibeHeatPumpConnector getConnector() throws NibeHeatPumpException {
        ThingTypeUID type = thing.getThingTypeUID();

        if (THING_TYPE_F1X45_UDP.equals(type) || THING_TYPE_F1X55_UDP.equals(type) || THING_TYPE_SMO40_UDP.equals(type)
                || THING_TYPE_F750_UDP.equals(type) || THING_TYPE_F470_UDP.equals(type)) {
            return new UDPConnector();
        } else if (THING_TYPE_F1X45_SERIAL.equals(type) || THING_TYPE_F1X55_SERIAL.equals(type)
                || THING_TYPE_SMO40_SERIAL.equals(type) || THING_TYPE_F750_SERIAL.equals(type)
                || THING_TYPE_F470_SERIAL.equals(type)) {
            return new SerialConnector(serialPortManager);
        } else if (THING_TYPE_F1X45_SIMULATOR.equals(type) || THING_TYPE_F1X55_SIMULATOR.equals(type)
                || THING_TYPE_SMO40_SIMULATOR.equals(type) || THING_TYPE_F750_SIMULATOR.equals(type)
                || THING_TYPE_F470_SIMULATOR.equals(type)) {
            return new SimulatorConnector();
        }

        String description = String.format("Unknown connector type %s", type);
        throw new NibeHeatPumpException(description);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received channel: {}, command: {}", channelUID, command);

        int coilAddress = parseCoilAddressFromChannelUID(channelUID);

        if (command.equals(RefreshType.REFRESH)) {
            logger.debug("Clearing cache value for channel '{}' to refresh channel data", channelUID);
            clearCache(coilAddress);
            return;
        }

        if (!configuration.enableWriteCommands) {
            logger.info(
                    "All write commands denied, ignoring command! Change Nibe heat pump binding configuration if you want to enable write commands.");
            return;
        }

        if (!itemsToEnableWrite.contains(coilAddress)) {
            logger.info(
                    "Write commands to register '{}' not allowed, ignoring command! Add this register to Nibe heat pump binding configuration if you want to enable write commands.",
                    coilAddress);
            return;
        }

        if (connector != null) {
            VariableInformation variableInfo = VariableInformation.getVariableInfo(pumpModel, coilAddress);
            logger.debug("Using variable information for register {}: {}", coilAddress, variableInfo);

            if (variableInfo != null && variableInfo.type == VariableInformation.Type.SETTING) {
                try {
                    int value = convertCommandToNibeValue(variableInfo, command);

                    ModbusWriteRequestMessage msg = new ModbusWriteRequestMessage.MessageBuilder()
                            .coilAddress(coilAddress).value(value).build();

                    writeResult = sendMessageToNibe(msg);
                    ModbusWriteResponseMessage result = (ModbusWriteResponseMessage) writeResult.get(TIMEOUT,
                            TimeUnit.MILLISECONDS);
                    if (result != null) {
                        if (result.isSuccessfull()) {
                            logger.debug("Write message sending to heat pump succeeded");
                        } else {
                            logger.error("Message sending to heat pump failed, value not accepted by the heat pump");
                        }
                    } else {
                        logger.debug("Something weird happen, result for write command is null");
                    }
                } catch (TimeoutException e) {
                    logger.warn("Message sending to heat pump failed, no response");
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "No response received from the heat pump");
                } catch (InterruptedException e) {
                    logger.debug("Message sending to heat pump failed, sending interrupted");
                } catch (NibeHeatPumpException e) {
                    logger.debug("Message sending to heat pump failed, exception {}", e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                } catch (CommandTypeNotSupportedException e) {
                    logger.warn("Unsupported command type {} received for channel {}, coil address {}.",
                            command.getClass().getName(), channelUID.getId(), coilAddress);
                } finally {
                    writeResult = null;
                }

                // Clear cache value to refresh coil data from the pump.
                // We might not know if write message have succeed or not, so let's always refresh it.
                logger.debug("Clearing cache value for channel '{}' to refresh channel data", channelUID);
                clearCache(coilAddress);
            } else {
                logger.debug("Command to channel '{}' rejected, because item is read only parameter", channelUID);
            }
        } else {
            logger.debug("No connection to heat pump");
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.HANDLER_MISSING_ERROR);
        }
    }

    @Override
    public void channelLinked(ChannelUID channelUID) {
        logger.debug("channelLinked: {}", channelUID);

        // Add channel to polling loop
        int coilAddress = parseCoilAddressFromChannelUID(channelUID);
        synchronized (itemsToPoll) {
            if (!itemsToPoll.contains(coilAddress)) {
                logger.debug("New channel '{}' found, register '{}'", channelUID.getAsString(), coilAddress);
                itemsToPoll.add(coilAddress);
            }
        }
        clearCache(coilAddress);
    }

    @Override
    public void channelUnlinked(ChannelUID channelUID) {
        logger.debug("channelUnlinked: {}", channelUID);

        // remove channel from polling loop
        int coilAddress = parseCoilAddressFromChannelUID(channelUID);
        synchronized (itemsToPoll) {
            itemsToPoll.removeIf(c -> c.equals(coilAddress));
        }
    }

    private int parseCoilAddressFromChannelUID(ChannelUID channelUID) {
        if (channelUID.getId().contains("#")) {
            String[] parts = channelUID.getId().split("#");
            return Integer.parseInt(parts[parts.length - 1]);
        } else {
            return Integer.parseInt(channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        logger.debug("Initialized Nibe Heat Pump device handler for {}", getThing().getUID());
        configuration = getConfigAs(NibeHeatPumpConfiguration.class);
        logger.debug("Using configuration: {}", configuration.toString());

        try {
            parseWriteEnabledItems();
            connector = getConnector();
        } catch (IllegalArgumentException | NibeHeatPumpException e) {
            String description = String.format("Illegal configuration, %s", e.getMessage());
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, description);
            return;
        }

        itemsToPoll.clear();
        itemsToPoll.addAll(this.getThing().getChannels().stream().filter(c -> isLinked(c.getUID())).map(c -> {
            int coilAddress = parseCoilAddressFromChannelUID(c.getUID());
            logger.debug("Linked channel '{}' found, register '{}'", c.getUID().getAsString(), coilAddress);
            return coilAddress;
        }).filter(c -> c != 0).collect(Collectors.toSet()));

        logger.debug("Linked registers {}: {}", itemsToPoll.size(), itemsToPoll);

        clearCache();

        if (connectorTask == null || connectorTask.isCancelled()) {
            connectorTask = scheduler.scheduleWithFixedDelay(() -> {
                if (reconnectionRequest) {
                    logger.debug("Restarting requested, restarting...");
                    reconnectionRequest = false;
                    closeConnection();
                }

                logger.debug("Checking Nibe Heat pump connection, thing status = {}", thing.getStatus());
                connect();
            }, 0, 10, TimeUnit.SECONDS);
        }
    }

    private void connect() {
        if (!connector.isConnected()) {
            logger.debug("Connecting to heat pump");
            try {
                connector.addEventListener(this);
                connector.connect(configuration);
                updateStatus(ThingStatus.ONLINE);

                if (pollingJob == null || pollingJob.isCancelled()) {
                    logger.debug("Start refresh task, interval={}sec", 1);
                    pollingJob = scheduler.scheduleWithFixedDelay(pollingRunnable, 0, 1, TimeUnit.SECONDS);
                }
            } catch (NibeHeatPumpException e) {
                logger.debug("Error occurred when connecting to heat pump, exception {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        } else {
            logger.debug("Connection to heat pump already open");
        }
    }

    @Override
    public void dispose() {
        logger.debug("Thing {} disposed.", getThing().getUID());

        if (connectorTask != null && !connectorTask.isCancelled()) {
            connectorTask.cancel(true);
            connectorTask = null;
        }

        closeConnection();
    }

    private void closeConnection() {
        logger.debug("Closing connection to the heat pump");

        if (pollingJob != null && !pollingJob.isCancelled()) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        if (connector != null) {
            connector.removeEventListener(this);
            connector.disconnect();
        }
    }

    private long refreshIntervalMillis() {
        return configuration.refreshInterval * 1000;
    }

    private int convertCommandToNibeValue(VariableInformation variableInfo, Command command)
            throws CommandTypeNotSupportedException {
        int value;

        if (command instanceof DecimalType || command instanceof QuantityType || command instanceof StringType) {
            BigDecimal v;
            if (command instanceof DecimalType) {
                v = ((DecimalType) command).toBigDecimal();
            } else if (command instanceof QuantityType) {
                v = ((QuantityType) command).toBigDecimal();
            } else {
                v = new BigDecimal(command.toString());
            }
            int decimals = (int) Math.log10(variableInfo.factor);
            value = v.movePointRight(decimals).intValue();
        } else if ((command instanceof OnOffType || command instanceof OpenClosedType || command instanceof UpDownType)
                && variableInfo.factor == 1) {
            value = (command.equals(OnOffType.ON) || command.equals(UpDownType.UP)
                    || command.equals(OpenClosedType.OPEN)) ? 1 : 0;
        } else {
            throw new CommandTypeNotSupportedException();
        }

        return value;
    }

    private void parseWriteEnabledItems() throws IllegalArgumentException {
        itemsToEnableWrite.clear();
        if (configuration.enableWriteCommands && configuration.enableWriteCommandsToRegisters != null
                && configuration.enableWriteCommandsToRegisters.length() > 0) {
            String[] items = configuration.enableWriteCommandsToRegisters.replace(" ", "").split(",");
            for (String item : items) {
                try {
                    int coilAddress = Integer.parseInt(item);
                    VariableInformation variableInformation = VariableInformation.getVariableInfo(pumpModel,
                            coilAddress);
                    if (variableInformation == null) {
                        String description = String.format("Unknown register %s", coilAddress);
                        throw new IllegalArgumentException(description);
                    }
                    itemsToEnableWrite.add(coilAddress);
                } catch (NumberFormatException e) {
                    String description = String.format("Illegal register %s", item);
                    throw new IllegalArgumentException(description);
                }
            }
        }
        logger.debug("Enabled registers for write commands: {}", itemsToEnableWrite);
    }

    private State convertNibeValueToState(VariableInformation variableInfo, int value, String acceptedItemType) {
        State state = UnDefType.UNDEF;
        long x;

        NibeDataType dataType = variableInfo.dataType;
        int decimals = (int) Math.log10(variableInfo.factor);
        switch (dataType) {
            case U8:
                x = Byte.toUnsignedLong((byte) (value & 0xFF));
                break;
            case U16:
                x = Short.toUnsignedLong((short) (value & 0xFFFF));
                break;
            case U32:
                x = Integer.toUnsignedLong(value);
                break;
            case S8:
                x = (byte) (value & 0xFF);
                break;
            case S16:
                x = (short) (value & 0xFFFF);
                break;
            case S32:
                x = value;
                break;
            default:
                return state;
        }
        BigDecimal converted = new BigDecimal(x).movePointLeft(decimals).setScale(decimals, RoundingMode.HALF_EVEN);

        if ("String".equalsIgnoreCase(acceptedItemType)) {
            state = new StringType(converted.toString());

        } else if ("Switch".equalsIgnoreCase(acceptedItemType)) {
            state = converted.intValue() == 0 ? OnOffType.OFF : OnOffType.ON;

        } else if ("Number".equalsIgnoreCase(acceptedItemType)) {
            state = new DecimalType(converted);
        }

        return state;
    }

    private void clearCache() {
        stateMap.clear();
        lastUpdateTime = 0;
    }

    private void clearCache(int coilAddress) {
        stateMap.put(coilAddress, null);
    }

    private synchronized NibeHeatPumpCommandResult sendMessageToNibe(NibeHeatPumpMessage msg)
            throws NibeHeatPumpException {
        logger.debug("Sending message: {}", msg);
        connector.sendDatagram(msg);
        return new NibeHeatPumpCommandResult();
    }

    @Override
    public void msgReceived(NibeHeatPumpMessage msg) {
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("Received raw data: {}", msg.toHexString());
            }

            logger.debug("Received message: {}", msg);

            updateStatus(ThingStatus.ONLINE);

            if (msg instanceof ModbusReadResponseMessage) {
                handleReadResponseMessage((ModbusReadResponseMessage) msg);
            } else if (msg instanceof ModbusWriteResponseMessage) {
                handleWriteResponseMessage((ModbusWriteResponseMessage) msg);
            } else if (msg instanceof ModbusDataReadOutMessage) {
                handleDataReadOutMessage((ModbusDataReadOutMessage) msg);
            } else {
                logger.debug("Received unknown message: {}", msg.toString());
            }
        } catch (Exception e) {
            logger.debug("Error occurred when parsing received message, reason: {}", e.getMessage());
        }
    }

    @Override
    public void errorOccurred(String error) {
        logger.debug("Error '{}' occurred, re-establish the connection", error);
        reconnectionRequest = true;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
    }

    private void handleReadResponseMessage(ModbusReadResponseMessage msg) {
        if (readResult != null) {
            readResult.set(msg);
        }
    }

    private void handleWriteResponseMessage(ModbusWriteResponseMessage msg) {
        if (writeResult != null) {
            writeResult.set(msg);
        }
    }

    private void handleDataReadOutMessage(ModbusDataReadOutMessage msg) {
        boolean parse = true;

        logger.debug("Received data read out message");
        if (configuration.throttleTime > 0) {
            if ((lastUpdateTime + configuration.throttleTime) > System.currentTimeMillis()) {
                logger.debug("Skipping data read out message parsing");
                parse = false;
            }
        }

        if (parse) {
            logger.debug("Parsing data read out message");
            lastUpdateTime = System.currentTimeMillis();
            List<ModbusValue> regValues = msg.getValues();

            if (regValues != null) {
                for (ModbusValue val : regValues) {
                    handleVariableUpdate(pumpModel, val);
                }
            }
        }
    }

    private void handleVariableUpdate(PumpModel pumpModel, ModbusValue value) {
        logger.debug("Received variable update: {}", value);
        int coilAddress = value.getCoilAddress();

        VariableInformation variableInfo = VariableInformation.getVariableInfo(pumpModel, coilAddress);

        if (variableInfo != null) {
            logger.trace("Using variable information to register {}: {}", coilAddress, variableInfo);

            int val = value.getValue();
            logger.debug("{} = {}", coilAddress + ":" + variableInfo.variable + "/" + variableInfo.factor, val);

            CacheObject oldValue = stateMap.get(coilAddress);

            if (oldValue != null && val == oldValue.value
                    && (oldValue.lastUpdateTime + refreshIntervalMillis() / 2) >= System.currentTimeMillis()) {
                logger.trace("Value did not change, ignoring update");
            } else {
                final String channelPrefix = (variableInfo.type == Type.SETTING ? "setting#" : "sensor#");
                final String channelId = channelPrefix + String.valueOf(coilAddress);
                final String acceptedItemType = thing.getChannel(channelId).getAcceptedItemType();

                logger.trace("AcceptedItemType for channel {} = {}", channelId, acceptedItemType);
                State state = convertNibeValueToState(variableInfo, val, acceptedItemType);
                logger.debug("Setting state {} = {}", coilAddress + ":" + variableInfo.variable, state);
                stateMap.put(coilAddress, new CacheObject(System.currentTimeMillis(), val));
                updateState(new ChannelUID(getThing().getUID(), channelId), state);
            }
        } else {
            logger.debug("Unknown register {}", coilAddress);
        }
    }

    protected class CacheObject {

        /** Time when cache object updated in milliseconds */
        final long lastUpdateTime;

        /** Cache value */
        final int value;

        /**
         * Initialize cache object.
         *
         * @param lastUpdateTime Time in milliseconds.
         * @param value Cache value.
         */
        CacheObject(long lastUpdateTime, int value) {
            this.lastUpdateTime = lastUpdateTime;
            this.value = value;
        }
    }
}
