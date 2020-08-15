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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.comfoair.internal.datatypes.ComfoAirDataType;
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
    private static final int DEFAULT_REFRESH_INTERVAL_SEC = 60;

    private final Logger logger = LoggerFactory.getLogger(ComfoAirHandler.class);
    private final ComfoAirConfiguration config = getConfigAs(ComfoAirConfiguration.class);
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
            ComfoAirCommand changeCommand = ComfoAirCommandType.getChangeCommand(channelId, command);

            if (changeCommand != null) {
                Set<String> keysToUpdate = getThing().getChannels().stream().map(Channel::getUID).filter(this::isLinked)
                        .map(ChannelUID::getId).collect(Collectors.toSet());
                sendCommand(changeCommand, channelId);

                Collection<ComfoAirCommand> affectedReadCommands = ComfoAirCommandType
                        .getAffectedReadCommands(channelId, keysToUpdate);

                if (affectedReadCommands.size() > 0) {
                    Runnable updateThread = new AffectedItemsUpdateThread(affectedReadCommands);
                    affectedItemsPoller = scheduler.schedule(updateThread, 3, TimeUnit.SECONDS);
                }
            } else {
                logger.warn("Unhandled command type: {}, channelId: {}", command.toString(), channelId);
            }
        }
    }

    @Override
    public void initialize() {
        String serialPort = this.config.serialPort;

        if (serialPort.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Serial port is not configured.");
            return;
        } else {
            ComfoAirSerialConnector comfoAirConnector = new ComfoAirSerialConnector(serialPortManager, serialPort,
                    BAUDRATE);
            this.comfoAirConnector = comfoAirConnector;
        }
        updateStatus(ThingStatus.UNKNOWN);
        scheduler.submit(this::connect);
    }

    private void connect() {
        if (comfoAirConnector != null) {
            try {
                comfoAirConnector.open();
                if (comfoAirConnector != null) {
                    updateStatus(ThingStatus.ONLINE);
                    pullDeviceProperties();
                    Map<String, String> properties = thing.getProperties();

                    List<Channel> toBeRemovedChannels = new ArrayList<>();
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_PREHEATER)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        toBeRemovedChannels.addAll(getThing()
                                .getChannelsOfGroup(ComfoAirBindingConstants.CG_PREHEATER_PREFIX.replaceAll("#$", "")));
                        Channel stateChannel = getThing().getChannel(ComfoAirBindingConstants.CG_MENUP9_PREFIX
                                + ComfoAirBindingConstants.CHANNEL_FROST_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_BYPASS)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        toBeRemovedChannels.addAll(getThing()
                                .getChannelsOfGroup(ComfoAirBindingConstants.CG_BYPASS_PREFIX.replaceAll("#$", "")));
                        Channel stateChannel = getThing().getChannel(ComfoAirBindingConstants.CG_MENUP9_PREFIX
                                + ComfoAirBindingConstants.CHANNEL_BYPASS_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_CHIMNEY)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        Channel stateChannel = getThing().getChannel(ComfoAirBindingConstants.CG_MENUP9_PREFIX
                                + ComfoAirBindingConstants.CHANNEL_CHIMNEY_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_COOKERHOOD)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        toBeRemovedChannels.addAll(getThing().getChannelsOfGroup(
                                ComfoAirBindingConstants.CG_COOKERHOOD_PREFIX.replaceAll("#$", "")));
                        Channel stateChannel = getThing().getChannel(ComfoAirBindingConstants.CG_MENUP9_PREFIX
                                + ComfoAirBindingConstants.CHANNEL_COOKERHOOD_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_HEATER)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        toBeRemovedChannels.addAll(getThing()
                                .getChannelsOfGroup(ComfoAirBindingConstants.CG_HEATER_PREFIX.replaceAll("#$", "")));
                        Channel stateChannel = getThing().getChannel(ComfoAirBindingConstants.CG_MENUP9_PREFIX
                                + ComfoAirBindingConstants.CHANNEL_HEATER_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_ENTHALPY)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        toBeRemovedChannels.addAll(getThing()
                                .getChannelsOfGroup(ComfoAirBindingConstants.CG_ENTHALPY_PREFIX.replaceAll("#$", "")));
                        Channel stateChannel = getThing().getChannel(ComfoAirBindingConstants.CG_MENUP9_PREFIX
                                + ComfoAirBindingConstants.CHANNEL_ENTHALPY_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    if (properties.get(ComfoAirBindingConstants.PROPERTY_OPTION_EWT)
                            .equals(ComfoAirBindingConstants.COMMON_OPTION_STATES[0])) {
                        toBeRemovedChannels.addAll(getThing()
                                .getChannelsOfGroup(ComfoAirBindingConstants.CG_EWT_PREFIX.replaceAll("#$", "")));
                        Channel stateChannel = getThing().getChannel(
                                ComfoAirBindingConstants.CG_MENUP9_PREFIX + ComfoAirBindingConstants.CHANNEL_EWT_STATE);
                        if (stateChannel != null) {
                            toBeRemovedChannels.add(stateChannel);
                        }
                    }
                    ThingBuilder builder = editThing().withoutChannels(toBeRemovedChannels);
                    updateThing(builder.build());

                    List<Channel> channels = this.thing.getChannels();

                    poller = scheduler.scheduleWithFixedDelay(() -> {
                        for (Channel channel : channels) {
                            updateChannelState(channel);
                        }
                    }, 0, (this.config.refreshInterval > 0) ? this.config.refreshInterval
                            : DEFAULT_REFRESH_INTERVAL_SEC, TimeUnit.SECONDS);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            } catch (ComfoAirSerialException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
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

        if (localPoller != null) {
            localPoller.cancel(true);
            poller = null;
        }

        final ScheduledFuture<?> localAffectedItemsPoller = affectedItemsPoller;

        if (localAffectedItemsPoller != null) {
            localAffectedItemsPoller.cancel(true);
            affectedItemsPoller = null;
        }
    }

    private void updateChannelState(Channel channel) {
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
                        preRequestCmd = ComfoAirCommandType.Constants.REQUEST_GET_STATES;
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
            String[] optionProperties = new String[] { ComfoAirBindingConstants.PROPERTY_OPTION_PREHEATER,
                    ComfoAirBindingConstants.PROPERTY_OPTION_BYPASS, ComfoAirBindingConstants.PROPERTY_OPTION_RECU_TYPE,
                    ComfoAirBindingConstants.PROPERTY_OPTION_RECU_SIZE,
                    ComfoAirBindingConstants.PROPERTY_OPTION_CHIMNEY,
                    ComfoAirBindingConstants.PROPERTY_OPTION_COOKERHOOD,
                    ComfoAirBindingConstants.PROPERTY_OPTION_HEATER, ComfoAirBindingConstants.PROPERTY_OPTION_ENTHALPY,
                    ComfoAirBindingConstants.PROPERTY_OPTION_EWT };

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

            ComfoAirCommand optionsReadCommand = new ComfoAirCommand(ComfoAirBindingConstants.PROPERTY_OPTION_PREHEATER,
                    ComfoAirCommandType.Constants.REQUEST_GET_STATES, ComfoAirCommandType.Constants.REPLY_GET_STATES,
                    ComfoAirCommandType.Constants.EMPTY_INT_ARRAY, null, null);
            int[] response = comfoAirConnector.sendCommand(optionsReadCommand,
                    ComfoAirCommandType.Constants.EMPTY_INT_ARRAY);
            if (response.length > 0) {
                for (String prop : optionProperties) {
                    ComfoAirCommandType comfoAirCommandType = ComfoAirCommandType.getCommandTypeByKey(prop);
                    String value = "";

                    if (comfoAirCommandType != null) {
                        ComfoAirDataType dataType = comfoAirCommandType.getDataType();
                        int intValue = dataType.calculateNumberValue(response, comfoAirCommandType);

                        switch (prop) {
                            case ComfoAirBindingConstants.PROPERTY_OPTION_RECU_TYPE:
                                value = intValue == 1 ? "LEFT" : "RIGHT";
                                break;
                            case ComfoAirBindingConstants.PROPERTY_OPTION_RECU_SIZE:
                                value = intValue == 1 ? "BIG" : "SMALL";
                                break;
                            case ComfoAirBindingConstants.PROPERTY_OPTION_ENTHALPY:
                                if (intValue == 1) {
                                    value = ComfoAirBindingConstants.COMMON_OPTION_STATES[1];
                                } else if (intValue == 2) {
                                    value = "Installed w\\o sensor";
                                } else {
                                    value = ComfoAirBindingConstants.COMMON_OPTION_STATES[0];
                                }
                                break;
                            case ComfoAirBindingConstants.PROPERTY_OPTION_EWT:
                                if (intValue == 1) {
                                    value = "Regulated";
                                } else if (intValue == 2) {
                                    value = "Unregulated";
                                } else {
                                    value = ComfoAirBindingConstants.COMMON_OPTION_STATES[0];
                                }
                                break;
                            default:
                                value = intValue > 0 ? ComfoAirBindingConstants.COMMON_OPTION_STATES[1]
                                        : ComfoAirBindingConstants.COMMON_OPTION_STATES[0];
                                break;
                        }
                    }
                    properties.put(prop, value);
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
