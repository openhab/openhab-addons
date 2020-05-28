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
package org.openhab.binding.comfoair.internal;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.unit.SIUnits;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.comfoair.internal.datatypes.ComfoAirDataType;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeBoolean;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeNumber;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeRPM;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeTemperature;
import org.openhab.binding.comfoair.internal.datatypes.DataTypeVolt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ComfoAirHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Hans Böhm - Initial contribution
 */
@NonNullByDefault
public class ComfoAirHandler extends BaseThingHandler {
    private static final int DEFAULT_REFRESH_INTERVAL = 60;

    private final Logger logger = LoggerFactory.getLogger(ComfoAirHandler.class);
    private final SerialPortManager serialPortManager;
    private @Nullable ScheduledFuture<?> poller;
    private @Nullable ScheduledFuture<?> affectedItemsPoller;
    private @Nullable ComfoAirSerialConnector comfoAirConnector;

    public static final int BAUDRATE = 9600;

    public ComfoAirHandler(Thing thing, final SerialPortManager serialPortManager) {
        super(thing);
        this.serialPortManager = serialPortManager;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channelId = channelUID.getId();

        if (command instanceof RefreshType) {
            Channel channel = this.thing.getChannel(channelUID);
            if (channel != null) {
                updateChannelState(channel);
            }
        } else {
            State state = commandToState(command, channelId);

            if (state instanceof UnDefType) {
                logger.warn("Unhandled command type: {}", command.toString());
            } else {
                ComfoAirCommand changeCommand = ComfoAirCommandType.getChangeCommand(channelId, state);

                if (changeCommand != null) {
                    Set<String> keysToUpdate = getThing().getChannels().stream().map(Channel::getUID)
                            .filter(this::isLinked).map(ChannelUID::getId).collect(Collectors.toSet());
                    sendCommand(changeCommand, channelId);

                    Collection<ComfoAirCommand> affectedReadCommands = ComfoAirCommandType
                            .getAffectedReadCommands(channelId, keysToUpdate);

                    if (affectedReadCommands.size() > 0) {
                        Runnable updateThread = new AffectedItemsUpdateThread(affectedReadCommands);
                        affectedItemsPoller = scheduler.schedule(updateThread, 3, TimeUnit.SECONDS);
                    }
                }
            }
        }
    }

    @Override
    public void initialize() {
        ComfoAirConfiguration config = getConfigAs(ComfoAirConfiguration.class);
        String serialPort = config.serialPort;

        if (StringUtils.isEmpty(serialPort)) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        } else {
            ComfoAirSerialConnector comfoAirConnector = new ComfoAirSerialConnector(serialPortManager, serialPort,
                    BAUDRATE);
            this.comfoAirConnector = comfoAirConnector;
        }
        if (comfoAirConnector != null) {
            comfoAirConnector.open();
            if (comfoAirConnector != null && comfoAirConnector.isConnected()) {
                updateStatus(ThingStatus.ONLINE);
                pullDeviceProperties();

                List<Channel> channels = this.thing.getChannels();

                poller = scheduler.scheduleWithFixedDelay(() -> {
                    for (Channel channel : channels) {
                        updateChannelState(channel);
                    }
                }, 0, (config.refreshInterval > 0) ? config.refreshInterval : DEFAULT_REFRESH_INTERVAL,
                        TimeUnit.SECONDS);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
        }
    }

    @Override
    public void dispose() {
        if (comfoAirConnector != null) {
            comfoAirConnector.close();
        }

        final ScheduledFuture<?> localPoller = poller;

        if (localPoller != null && !localPoller.isCancelled()) {
            localPoller.cancel(true);
            poller = null;
        }

        final ScheduledFuture<?> localAffectedItemsPoller = affectedItemsPoller;

        if (localAffectedItemsPoller != null && !localAffectedItemsPoller.isCancelled()) {
            localAffectedItemsPoller.cancel(true);
            affectedItemsPoller = null;
        }
    }

    private void updateChannelState(Channel channel) {
        try {
            if (!isLinked(channel.getUID())) {
                return;
            }
            String commandKey = channel.getUID().getId();

            ComfoAirCommand readCommand = ComfoAirCommandType.getReadCommand(commandKey);
            if (readCommand != null) {
                scheduler.submit(() -> {
                    State state = sendCommand(readCommand, commandKey);
                    updateState(channel.getUID(), state);
                });
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown channel {}", channel.getUID().getId());
        }
    }

    private State commandToState(Command command, String channelId) {
        ComfoAirCommandType comfoAirCommandType = ComfoAirCommandType.getCommandTypeByKey(channelId);
        if (comfoAirCommandType != null) {
            ComfoAirDataType dataType = comfoAirCommandType.getDataType();

            if (dataType instanceof DataTypeBoolean) {
                return (OnOffType) command;
            } else if (dataType instanceof DataTypeNumber || dataType instanceof DataTypeRPM) {
                return (DecimalType) command;
            } else if (dataType instanceof DataTypeTemperature) {
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> celsius = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);
                    if (celsius != null) {
                        return new DecimalType(celsius.doubleValue());
                    }
                } else {
                    return (DecimalType) command;
                }
            } else if (dataType instanceof DataTypeVolt) {
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> volts = ((QuantityType<?>) command).toUnit(SmartHomeUnits.VOLT);
                    if (volts != null) {
                        return new DecimalType(volts.doubleValue());
                    }
                } else {
                    return (DecimalType) command;
                }
            }
        }
        return UnDefType.UNDEF;
    }

    private State sendCommand(ComfoAirCommand command, String commandKey) {
        ComfoAirSerialConnector comfoAirConnector = this.comfoAirConnector;

        if (comfoAirConnector != null) {
            Integer requestCmd = command.getRequestCmd();
            Integer replyCmd = command.getReplyCmd();
            int[] requestData = command.getRequestData();

            Integer preRequestCmd;
            Integer preReplyCmd;
            int[] preResponse = ComfoAirCommandType.Constants.EMPTY_INT_ARRAY;

            if (requestCmd != null) {
                switch (requestCmd) {
                    case ComfoAirCommandType.Constants.REQUEST_SET_ANALOGS:
                        preRequestCmd = ComfoAirCommandType.Constants.REQUEST_GET_ANALOGS;
                        preReplyCmd = ComfoAirCommandType.Constants.REPLY_GET_ANALOGS;
                        break;
                    case ComfoAirCommandType.Constants.REQUEST_SET_DELAYS:
                        preRequestCmd = ComfoAirCommandType.Constants.REQUEST_GET_DELAYS;
                        preReplyCmd = ComfoAirCommandType.Constants.REPLY_GET_DELAYS;
                        break;
                    case ComfoAirCommandType.Constants.REQUEST_SET_FAN_LEVEL:
                        preRequestCmd = ComfoAirCommandType.Constants.REQUEST_GET_FAN_LEVEL;
                        preReplyCmd = ComfoAirCommandType.Constants.REPLY_GET_FAN_LEVEL;
                        break;
                    case ComfoAirCommandType.Constants.REQUEST_SET_STATES:
                        preRequestCmd = ComfoAirCommandType.Constants.REQUEST_SET_STATES;
                        preReplyCmd = ComfoAirCommandType.Constants.REPLY_GET_STATES;
                        break;
                    case ComfoAirCommandType.Constants.REQUEST_SET_EWT:
                        preRequestCmd = ComfoAirCommandType.Constants.REQUEST_GET_EWT;
                        preReplyCmd = ComfoAirCommandType.Constants.REPLY_GET_EWT;
                        break;
                    default:
                        preRequestCmd = requestCmd;
                        preReplyCmd = replyCmd;
                }

                if (!preRequestCmd.equals(requestCmd)) {
                    command.setRequestCmd(preRequestCmd);
                    command.setReplyCmd(preReplyCmd);
                    command.setRequestData(ComfoAirCommandType.Constants.EMPTY_INT_ARRAY);

                    preResponse = comfoAirConnector.sendCommand(command, ComfoAirCommandType.Constants.EMPTY_INT_ARRAY);

                    if (preResponse.length <= 0) {
                        return UnDefType.NULL;
                    } else {
                        command.setRequestCmd(requestCmd);
                        command.setReplyCmd(replyCmd);
                        command.setRequestData(requestData);
                    }
                }

                int[] response = comfoAirConnector.sendCommand(command, preResponse);

                if (response.length > 0) {
                    ComfoAirCommandType comfoAirCommandType = ComfoAirCommandType.getCommandTypeByKey(commandKey);
                    State value = UnDefType.UNDEF;

                    if (comfoAirCommandType != null) {
                        ComfoAirDataType dataType = comfoAirCommandType.getDataType();
                        value = dataType.convertToState(response, comfoAirCommandType);
                    }
                    if (value instanceof UnDefType) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("unexpected value for DATA: {}", ComfoAirSerialConnector.dumpData(response));
                        }
                    }
                    return value;
                }
            }
        }
        return UnDefType.UNDEF;
    }

    public void pullDeviceProperties() {
        Map<String, String> properties = editProperties();
        ComfoAirSerialConnector comfoAirConnector = this.comfoAirConnector;

        if (comfoAirConnector != null) {
            String[] namedProperties = new String[] { ComfoAirBindingConstants.PROPERTY_SOFTWARE_MAIN_VERSION,
                    ComfoAirBindingConstants.PROPERTY_SOFTWARE_MINOR_VERSION,
                    ComfoAirBindingConstants.PROPERTY_DEVICE_NAME };

            for (String prop : namedProperties) {
                ComfoAirCommand readCommand = ComfoAirCommandType.getReadCommand(prop);
                if (readCommand != null) {
                    int[] response = comfoAirConnector.sendCommand(readCommand,
                            ComfoAirCommandType.Constants.EMPTY_INT_ARRAY);
                    if (response.length > 0) {
                        ComfoAirCommandType comfoAirCommandType = ComfoAirCommandType.getCommandTypeByKey(prop);
                        String value = "";

                        if (comfoAirCommandType != null) {
                            ComfoAirDataType dataType = comfoAirCommandType.getDataType();
                            if (prop.equals(ComfoAirBindingConstants.PROPERTY_DEVICE_NAME)) {
                                value = dataType.calculateStringValue(response, comfoAirCommandType);
                            } else {
                                value = String.valueOf(dataType.calculateNumberValue(response, comfoAirCommandType));
                            }
                        }
                        properties.put(prop, value);
                    }
                }
            }
            thing.setProperties(properties);
        }
    }

    private class AffectedItemsUpdateThread implements Runnable {

        private Collection<ComfoAirCommand> affectedReadCommands;

        public AffectedItemsUpdateThread(Collection<ComfoAirCommand> affectedReadCommands) {
            this.affectedReadCommands = affectedReadCommands;
        }

        @Override
        public void run() {
            for (ComfoAirCommand readCommand : this.affectedReadCommands) {
                Integer replyCmd = readCommand.getReplyCmd();
                if (replyCmd != null) {
                    List<ComfoAirCommandType> commandTypes = ComfoAirCommandType.getCommandTypesByReplyCmd(replyCmd);

                    for (ComfoAirCommandType commandType : commandTypes) {
                        String commandKey = commandType.getKey();
                        sendCommand(readCommand, commandKey);
                    }
                }
            }
        }
    }
}
