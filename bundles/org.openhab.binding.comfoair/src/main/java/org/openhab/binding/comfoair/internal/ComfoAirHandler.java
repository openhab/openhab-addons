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
    private @Nullable ScheduledFuture<?> poller;
    private SerialPortManager serialPortManager;
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
            try {
                Set<ChannelUID> channelsLinked = getThing().getChannels().stream().map(Channel::getUID)
                        .filter(this::isLinked).collect(Collectors.toSet());
                Set<String> keysToUpdate = channelsLinked.stream().map(ChannelUID::getId).collect(Collectors.toSet());

                ComfoAirCommandType comfoAirCommandType = ComfoAirCommandType.getCommandTypeByKey(channelId);
                ComfoAirDataType dataType = comfoAirCommandType.getDataType();
                State state = null;

                if (dataType instanceof DataTypeBoolean) {
                    state = (OnOffType) command;
                } else if (dataType instanceof DataTypeNumber || dataType instanceof DataTypeRPM) {
                    state = (DecimalType) command;
                } else if (dataType instanceof DataTypeTemperature) {
                    if (command instanceof QuantityType<?>) {
                        QuantityType<?> celsius = ((QuantityType<?>) command).toUnit(SIUnits.CELSIUS);
                        if (celsius != null) {
                            state = new DecimalType(celsius.doubleValue());
                        }
                    } else {
                        state = (DecimalType) command;
                    }
                } else if (dataType instanceof DataTypeVolt) {
                    if (command instanceof QuantityType<?>) {
                        QuantityType<?> volts = ((QuantityType<?>) command).toUnit(SmartHomeUnits.VOLT);
                        if (volts != null) {
                            state = new DecimalType(volts.doubleValue());
                        }
                    } else {
                        state = (DecimalType) command;
                    }
                }

                if (state != null) {
                    ComfoAirCommand changeCommand = ComfoAirCommandType.getChangeCommand(channelId, state);
                    sendCommand(changeCommand, channelId);

                    Collection<ComfoAirCommand> affectedReadCommands = ComfoAirCommandType
                            .getAffectedReadCommands(channelId, keysToUpdate);

                    if (affectedReadCommands.size() > 0) {
                        Runnable updateThread = new AffectedItemsUpdateThread(affectedReadCommands);
                        scheduler.schedule(updateThread, 3, TimeUnit.SECONDS);
                    }
                } else {
                    logger.warn("Unhandled command type: {}", command.toString());
                }
            } catch (final RuntimeException e) {
                logger.warn("Updating ComfoAir failed: {}", e.getMessage());
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
            }
        }
    }

    @Override
    public void initialize() {
        ComfoAirConfiguration config = getConfigAs(ComfoAirConfiguration.class);
        String serialPort = config.serialPort;

        if (StringUtils.isNotEmpty(serialPort)) {
            comfoAirConnector = new ComfoAirSerialConnector(serialPortManager, serialPort, BAUDRATE);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }
        if (comfoAirConnector != null) {
            comfoAirConnector.open();
            updateStatus(ThingStatus.ONLINE);

            List<Channel> channels = this.thing.getChannels();

            poller = scheduler.scheduleWithFixedDelay(() -> {
                for (Channel channel : channels) {
                    updateChannelState(channel);
                }
            }, 0, (config.refreshInterval > 0) ? config.refreshInterval : DEFAULT_REFRESH_INTERVAL, TimeUnit.SECONDS);
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
    }

    private void updateChannelState(Channel channel) {
        try {
            if (!isLinked(channel.getUID())) {
                return;
            }
            String commandKey = channel.getUID().getId();
            ComfoAirCommand readCommand = ComfoAirCommandType.getReadCommand(commandKey);
            State state = sendCommand(readCommand, commandKey);

            updateState(channel.getUID(), state);
        } catch (IllegalArgumentException e) {
            logger.warn("Unknown channel {}", channel.getUID().getId());
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
            int[] preResponse = new int[0];

            if (requestCmd != null) {
                switch (requestCmd) {
                    case 0x9f:
                        preRequestCmd = 0x9d;
                        preReplyCmd = 0x9e;
                        break;
                    case 0xcb:
                        preRequestCmd = 0xc9;
                        preReplyCmd = 0xca;
                        break;
                    case 0xcf:
                        preRequestCmd = 0xcd;
                        preReplyCmd = 0xce;
                        break;
                    case 0xd7:
                        preRequestCmd = 0xd5;
                        preReplyCmd = 0xd6;
                        break;
                    case 0xed:
                        preRequestCmd = 0xeb;
                        preReplyCmd = 0xec;
                        break;
                    default:
                        preRequestCmd = requestCmd;
                        preReplyCmd = replyCmd;
                }

                if (!preRequestCmd.equals(requestCmd)) {
                    command.setRequestCmd(preRequestCmd);
                    command.setReplyCmd(preReplyCmd);
                    command.setRequestData(new int[0]);

                    preResponse = comfoAirConnector.sendCommand(command, new int[0]);

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

                    ComfoAirDataType dataType = comfoAirCommandType.getDataType();
                    State value = dataType.convertToState(response, comfoAirCommandType);

                    if (value == null) {
                        if (logger.isWarnEnabled()) {
                            logger.warn("unexpected value for DATA: {}", ComfoAirSerialConnector.dumpData(response));
                        }
                        return UnDefType.UNDEF;
                    } else {
                        return value;
                    }
                }
            }
        }
        return UnDefType.UNDEF;
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
