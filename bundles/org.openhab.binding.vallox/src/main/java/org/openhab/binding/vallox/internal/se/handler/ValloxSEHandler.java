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
package org.openhab.binding.vallox.internal.se.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.vallox.internal.se.cache.ValloxExpiringCacheMap;
import org.openhab.binding.vallox.internal.se.configuration.ValloxSEConfiguration;
import org.openhab.binding.vallox.internal.se.connection.ValloxConnector;
import org.openhab.binding.vallox.internal.se.connection.ValloxEventListener;
import org.openhab.binding.vallox.internal.se.mapper.ChannelDescriptor;
import org.openhab.binding.vallox.internal.se.telegram.Telegram;
import org.openhab.binding.vallox.internal.se.telegram.TelegramFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxSEHandler} is responsible for handling commands, which
 * are sent to one of the channels
 *
 * @author Hauke Fuhrmann - Initial contribution
 * @author Miika Jukka - Rewrite
 */
@NonNullByDefault
public class ValloxSEHandler extends BaseThingHandler implements ValloxEventListener {

    private final Logger logger = LoggerFactory.getLogger(ValloxSEHandler.class);

    private final ValloxExpiringCacheMap cache = new ValloxExpiringCacheMap(Duration.ofMinutes(15));
    private final ValloxConnector connector;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> watchDog;

    private boolean reconnect = false;
    private byte panelNumber;

    public ValloxSEHandler(Thing thing, ValloxConnector connector) {
        super(thing);
        this.connector = connector;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Vallox SE handler");
        if (watchDog != null) {
            watchDog.cancel(true);
            watchDog = null;
        }
        closeConnection();
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Vallox SE handler");
        updateStatus(ThingStatus.UNKNOWN);
        cache.clear();
        if (watchDog == null) {
            watchDog = scheduler.scheduleWithFixedDelay(this::checkConnection, 0, 30, TimeUnit.SECONDS);
        }
    }

    /**
     * Connect to Vallox unit and start refresher only after successful connection.
     */
    private void connect() {
        if (!isConnected()) {
            try {
                ValloxSEConfiguration config = getConfigAs(ValloxSEConfiguration.class);
                this.panelNumber = config.getPanelAsByte();
                connector.addListener(this);
                connector.connect(config);
                if (refreshJob == null) {
                    refreshJob = scheduler.scheduleWithFixedDelay(this::refreshChannels, 1, 5, TimeUnit.MINUTES);
                }
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                logger.debug("Connection failed ", e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                reconnect = true;
            }
        }
    }

    /**
     * Close the connection and stop refresh job
     */
    private void closeConnection() {
        connector.removeListener(this);
        connector.close();
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Check if connection is open
     */
    private boolean isConnected() {
        return connector.isConnected();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (this.thing.getStatus() == ThingStatus.ONLINE && isConnected()) {
            ChannelDescriptor descriptor = ChannelDescriptor.get(channelUID.getId());
            if (descriptor == ChannelDescriptor.NULL) {
                logger.debug("Channel descriptor returned null");
                return;
            }
            if (command instanceof RefreshType) {
                handleRefreshTypeCommand(command, descriptor);
            } else if (command instanceof DecimalType) {
                handleDecimalCommand(((DecimalType) command).intValue(), descriptor);
            } else if (command instanceof QuantityType) {
                handleDecimalCommand(((QuantityType<?>) command).intValue(), descriptor);
            } else if (command instanceof OnOffType) {
                handleOnOffCommand(command, descriptor);
            } else {
                logger.debug("Unsupported command '{}'", command);
            }
        }
    }

    /**
     * Handle refresh type command.
     * Check if channel value is cached and return it if not expired.
     *
     * @param command the command to send
     * @param channelID the channel where the command is sent
     * @param descriptor the descriptor of the channel
     */
    private void handleRefreshTypeCommand(Command command, ChannelDescriptor descriptor) {
        Byte cachedValue = cache.getIfValid(ChannelDescriptor.getParentOrReturn(descriptor));
        if (cachedValue != null) {
            logger.debug("Cache hasn't expired yet. Updating '{}' with cached value", descriptor);
            updateState(descriptor.channelID, descriptor.convertToState(cachedValue));
            return;
        }
        sendPoll(descriptor);
    }

    /**
     * Handle OnOff type commands
     *
     * @param command the command to send
     * @param channelID the channel where the command is sent
     * @param descriptor the descriptor of the channel
     */
    private void handleOnOffCommand(Command command, ChannelDescriptor descriptor) {
        ChannelDescriptor parentChannelDescriptor = ChannelDescriptor.getParentOrReturn(descriptor);
        Byte cachedValue = cache.getValue(parentChannelDescriptor);
        if (cachedValue == null) {
            logger.debug("Couldn't handle OnOff command because cache doesn't contain any value for channel '{}'",
                    parentChannelDescriptor.channelID);
            return;
        }
        BitSet bits = BitSet.valueOf(new byte[] { cachedValue });
        switch (parentChannelDescriptor) {
            case SELECT:
                switch (descriptor) {
                    // send the first 4 bits of the Select byte; others are read only
                    // 1 1 1 1 1 1 1 1
                    // | | | | | | | |
                    // | | | | | | | +- 0 Power state
                    // | | | | | | +--- 1 CO2 Adjust state
                    // | | | | | +----- 2 %RH adjust state
                    // | | | | +------- 3 Heating state
                    // | | | +--------- 4 Filter guard indicator
                    // | | +----------- 5 Heating indicator
                    // | +------------- 6 Fault indicator
                    // +--------------- 7 service reminder
                    case POST_HEATING_STATE:
                        bits.set(3, command == OnOffType.ON);
                        break;
                    case HUMIDITY_ADJUST_STATE:
                        bits.set(2, command == OnOffType.ON);
                        break;
                    case CO2_ADJUST_STATE:
                        bits.set(1, command == OnOffType.ON);
                        break;
                    case POWER_STATE:
                        bits.set(0, command == OnOffType.ON);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'select'",
                                descriptor.channelID);
                        return;
                }
                break;
            case PROGRAM_1:
                switch (descriptor) {
                    // 1 1 1 1 1 1 1 1
                    // | | | | _______
                    // | | | | |
                    // | | | | +--- 0-3 set adjustment interval of CO2 and %RH in minutes
                    // | | | |
                    // | | | |
                    // | | | |
                    // | | | +--------- 4 automatic RH basic level seeker state
                    // | | +----------- 5 boost switch mode (1=boost, 0( (byte)fireplace)
                    // | +------------- 6 radiator type 0( (byte)electric, 1( (byte)water
                    // +--------------- 7 cascade adjust 0( (byte)off, 1( (byte)on
                    case AUTOMATIC_HUMIDITY_LEVEL_SEEKER_STATE:
                        bits.set(4, command == OnOffType.ON);
                        break;
                    case BOOST_SWITCH_MODE:
                        bits.set(5, command == OnOffType.ON);
                        break;
                    case RADIATOR_TYPE:
                        bits.set(6, command == OnOffType.ON);
                        break;
                    case CASCADE_ADJUST:
                        bits.set(7, command == OnOffType.ON);
                        break;
                    case ADJUSTMENT_INTERVAL:
                        byte temp = (byte) (Integer.parseInt(command.toString()) & 0x0F);
                        BitSet aim = BitSet.valueOf(new byte[] { temp });
                        bits.set(0, aim.get(0));
                        bits.set(1, aim.get(1));
                        bits.set(2, aim.get(2));
                        bits.set(3, aim.get(3));
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'program1'",
                                descriptor.channelID);
                        return;
                }
                break;
            case FLAGS_5:
                switch (descriptor) {
                    // 1 1 1 1 1 1 1 1
                    // | | | | | | | |
                    // | | | | | | | +- 0
                    // | | | | | | +--- 1
                    // | | | | | +----- 2
                    // | | | | +------- 3
                    // | | | +--------- 4
                    // | | +----------- 5
                    // | +------------- 6
                    // +--------------- 7 Preheating state
                    case PRE_HEATING_STATE:
                        bits.set(7, command == OnOffType.ON);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'flags5'",
                                descriptor.channelID);
                        return;
                }
                break;
            case PROGRAM_2:
                switch (descriptor) {
                    // 1 1 1 1 1 1 1 1
                    // | | | | | | | |
                    // | | | | | | | +- 0 Maximum speed limit mode
                    // | | | | | | +--- 1
                    // | | | | | +----- 2
                    // | | | | +------- 3
                    // | | | +--------- 4
                    // | | +----------- 5
                    // | +------------- 6
                    // +--------------- 7
                    case MAX_SPEED_LIMIT_MODE:
                        bits.set(0, command == OnOffType.ON);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'program2'",
                                descriptor.channelID);
                        return;
                }
                break;
            case IO_MULTIPURPOSE_2:
                switch (descriptor) {
                    // 1 1 1 1 1 1 1 1
                    // | | | | | | | |
                    // | | | | | | | +- 0
                    // | | | | | | +--- 1
                    // | | | | | +----- 2
                    // | | | | +------- 3 Supply fan off
                    // | | | +--------- 4
                    // | | +----------- 5 Exhaust fan off
                    // | +------------- 6
                    // +--------------- 7
                    case SUPPLY_FAN_OFF:
                        bits.set(3, command == OnOffType.ON);
                        break;
                    case EXHAUST_FAN_OFF:
                        bits.set(5, command == OnOffType.ON);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'ioMultiPurpose2'",
                                descriptor.channelID);
                        return;
                }
                break;
            default:
                logger.debug("Unsupported command '{}' to channel '{}' received", command, descriptor.channelID);
                return;
        }
        // Ensure that byte array length is always 8 even if all bits are 0.
        byte[] cmd = Arrays.copyOf(bits.toByteArray(), 8);
        if (cmd != null) {
            sendCommand(descriptor, cmd[0]);
        }
    }

    /**
     * Handle decimal type commands. CO2 set point is 16bit values and high and low bytes are handled separately.
     * adjustmentIntervallMinutes is passed to handleOnOffCommand to
     *
     * @param command the command to send
     * @param channelID the channel where the command is sent
     * @param descriptor the descriptor of the channel
     */
    private void handleDecimalCommand(int command, ChannelDescriptor descriptor) {
        if (descriptor.equals(ChannelDescriptor.CO2_SETPOINT)) {
            int commandValue = command;
            byte lowByte = (byte) (commandValue & 0xFF);
            byte highByte = (byte) ((commandValue >>> 8) & 0xFF);
            sendCommand(ChannelDescriptor.CO2_SETPOINT_HIGH, highByte);
            sendCommand(ChannelDescriptor.CO2_SETPOINT_LOW, lowByte);
            return;
        }
        if (descriptor.equals(ChannelDescriptor.ADJUSTMENT_INTERVAL)) {
            handleOnOffCommand(new DecimalType(command), descriptor);
            return;
        }
        sendCommand(descriptor, descriptor.convertFromState((byte) command));
    }

    /**
     * Get a collection containing all linked channels of a thing
     */
    private Collection<String> linkedChannels() {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId).filter(this::isLinked)
                .collect(Collectors.toList());
    }

    /**
     * Check if reconnection is requested and verify connection is still alive.
     */
    private void checkConnection() {
        if (reconnect) {
            reconnect = false;
            closeConnection();
        }
        connect();
    }

    /**
     * Ensure that all linked channels have been polled at least once and has a value.
     * OnOffType or DecimalType commands needs a cached value.
     */
    private void refreshChannels() {
        if (isConnected()) {
            Collection<ChannelDescriptor> channelsToRefresh = linkedChannels().stream().map(ChannelDescriptor::get)
                    .map(ChannelDescriptor::getParentOrReturn).filter(cache::isExpired).distinct()
                    .collect(Collectors.toList());
            logger.debug("Refreshing channels: {}", channelsToRefresh);
            channelsToRefresh.forEach(this::sendPoll);
        }
    }

    /**
     * CO2 value and set point are 16bit values so high and low bytes are handled separately.
     *
     * @param descriptor the descriptor of the channel to be polled
     */
    public void sendPoll(ChannelDescriptor descriptor) {
        if (descriptor.equals(ChannelDescriptor.CO2_SETPOINT)) {
            sendPollToConnector(ChannelDescriptor.CO2_SETPOINT_HIGH);
            sendPollToConnector(ChannelDescriptor.CO2_SETPOINT_LOW);
        } else if (descriptor.equals(ChannelDescriptor.CO2)) {
            sendPollToConnector(ChannelDescriptor.CO2_HIGH);
            sendPollToConnector(ChannelDescriptor.CO2_LOW);
        } else {
            sendPollToConnector(descriptor);
        }
    }

    /**
     * Forward poll telegram to connection handler.
     *
     * @param descriptor the descriptor of the channel to be polled
     */
    public void sendPollToConnector(ChannelDescriptor descriptor) {
        connector.sendTelegram(TelegramFactory.createPoll(panelNumber, descriptor.getVariable()));
    }

    /**
     * Forward command telegram to connection handler.
     *
     * @param channel the channel which the command is sent to
     * @param descriptor the descriptor of the channel where the command is sent
     */
    public void sendCommand(ChannelDescriptor descriptor, byte value) {
        connector.sendTelegram(TelegramFactory.createCommand(panelNumber, descriptor.getVariable(), value));
    }

    /**
     * Handle telegram received from connection handler.
     */
    @Override
    public void telegramReceived(Telegram telegram) {
        switch (telegram.state) {
            case ACK:
                logger.debug("Received ack byte for telegram '{}'", telegram);
                cache.put(telegram);
                break;
            case OK:
                logger.trace("{} {}", telegram.stateDetails(), telegram);
                ChannelDescriptor descriptor = ChannelDescriptor.get(telegram.getVariable());
                if (descriptor != ChannelDescriptor.NULL) {
                    telegram.parse(descriptor, cache).forEach(this::updateState);
                    cache.put(telegram);
                } else {
                    logger.debug("Null channel descriptor returned with telegram '{}'", telegram);
                }
                break;
            case CRC_ERROR:
            case EMPTY:
            case NOT_FOR_US:
                logger.trace("{} {}", telegram.stateDetails(), telegram);
                break;
            case RESUME:
                logger.debug("Resuming normal traffic");
                break;
            case SUSPEND:
                logger.debug("Suspending traffic while CO2 sensors are read");
                break;
            default:
                logger.debug("Unknown telegram received");
                break;
        }
    }

    @Override
    public void errorOccurred(String error, @Nullable Exception exception) {
        if (exception != null) {
            logger.trace("{}", error, exception);
        }
        logger.debug("Reconnecting after error: {}", error);
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
        reconnect = true;
    }
}
