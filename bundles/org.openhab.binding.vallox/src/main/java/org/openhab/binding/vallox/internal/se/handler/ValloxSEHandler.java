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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.serial.SerialPortManager;
import org.openhab.binding.vallox.internal.se.cache.ValloxExpiringCacheMap;
import org.openhab.binding.vallox.internal.se.configuration.ValloxSEConfiguration;
import org.openhab.binding.vallox.internal.se.connection.ConnectorFactory;
import org.openhab.binding.vallox.internal.se.connection.ValloxConnector;
import org.openhab.binding.vallox.internal.se.connection.ValloxEventListener;
import org.openhab.binding.vallox.internal.se.mapper.ChannelMapper;
import org.openhab.binding.vallox.internal.se.mapper.ValloxChannel;
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
    private @NonNullByDefault({}) ValloxConnector connector;
    private @NonNullByDefault({}) ScheduledFuture<?> refreshJob;
    private @NonNullByDefault({}) ScheduledFuture<?> watchDog;
    private SerialPortManager portManager;
    private boolean reconnect = false;
    private byte panelNumber;

    public ValloxSEHandler(Thing thing, SerialPortManager portManager) {
        super(thing);
        this.portManager = portManager;
    }

    @Override
    public void dispose() {
        logger.debug("Disposing vallox");
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
        try {
            this.connector = ConnectorFactory.getConnector(thing.getThingTypeUID(), portManager, scheduler);
        } catch (Exception ex) {
            String message = "Failed to initialize: ";
            logger.debug(message, ex);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, ex.toString());
            return;
        }
        if (watchDog == null || watchDog.isCancelled()) {
            watchDog = scheduler.scheduleWithFixedDelay(this.checkConnection, 0, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Connect to Vallox using the connector received from {@link ConnectorFactory}.
     * Start refresher only after successful connection.
     */
    private void connect() {
        if (!isConnected()) {
            try {
                ValloxSEConfiguration config = getConfigAs(ValloxSEConfiguration.class);
                this.panelNumber = config.getPanelAsByte();
                connector.addListener(this);
                connector.connect(config);
                if (refreshJob == null || refreshJob.isCancelled()) {
                    refreshJob = scheduler.scheduleWithFixedDelay(this.refreshChannels, 1, 5, TimeUnit.MINUTES);
                }
                updateStatus(ThingStatus.ONLINE);
            } catch (IOException e) {
                if (logger.isTraceEnabled()) {
                    logger.debug("Connection failed", e);
                } else {
                    logger.debug("Connection failed -> {}", e.getMessage());
                }
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                reconnect = true;
            }
        } else {
            logger.trace("Connection already open");
        }
    }

    /**
     * Close the connection and stop refresh job
     */
    private void closeConnection() {
        logger.debug("Closing connection");
        if (connector != null) {
            connector.removeListener(this);
            connector.close();
        }
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
    }

    /**
     * Check if connection is initialized and open
     */
    private boolean isConnected() {
        if (connector != null) {
            if (connector.isConnected()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (this.thing.getStatus() == ThingStatus.ONLINE && isConnected()) {
            String channelID = channelUID.getId();
            Byte channelAsByte = ChannelMapper.getVariable(channelID);
            if (command instanceof RefreshType) {
                handleRefreshTypeCommand(command, channelID, channelAsByte);
            } else if (command instanceof DecimalType) {
                handleDecimalCommand((DecimalType) command, channelID, channelAsByte);
            } else if (command instanceof OnOffType) {
                handleOnOffCommand(command, channelID, channelAsByte);
            } else {
                logger.debug("Unsupported command '{}'", command);
            }
        }
    }

    /**
     * Handle refresh type command. CO2 value and set point are 16bit values and high and low bytes are handled
     * separately.
     * Check if channel value is cached and return it if not expired.
     *
     * @param command the command to send
     * @param channelID the channel where the command is sent
     * @param channelAsByte the channel as byte value
     */
    private void handleRefreshTypeCommand(Command command, String channelID, Byte channelAsByte) {
        if (cache.isValid(channelAsByte)) {
            logger.debug("Cache hasn't expired yet. Updating state with cached value for channel: {}", channelID);
            ValloxChannel valloxChannel = ChannelMapper.getValloxChannel(channelID);
            updateState(channelID, valloxChannel.convertToState(cache.getValue(channelAsByte)));
            return;
        } else if (channelID.equals("setting#co2SetPoint")) {
            sendPoll(ChannelMapper.getVariable("setting#co2SetPointHigh"));
            sendPoll(ChannelMapper.getVariable("setting#co2SetPointLow"));
        } else if (channelID.equals("status#CO2")) {
            sendPoll(ChannelMapper.getVariable("status#co2High"));
            sendPoll(ChannelMapper.getVariable("status#co2Low"));
        } else {
            sendPoll(channelAsByte);
        }
    }

    /**
     * Handle OnOff type commands
     *
     * @param command the command to send
     * @param channelID the channel where the command is sent
     * @param channelAsByte the channel as byte value
     */
    private void handleOnOffCommand(Command command, String channelID, Byte channelAsByte) {
        String parentChannel = ChannelMapper.getChannelForVariable(channelAsByte);
        if (!cache.containsKey(channelAsByte)) {
            logger.debug("Couldn't handle OnOff command because cache doesn't contain any value for channel '{}'",
                    parentChannel);
            return;
        }
        byte cachedValue = cache.getValue(channelAsByte);
        BitSet bits = BitSet.valueOf(new byte[] { cachedValue });
        switch (parentChannel) {
            case "select":
                switch (channelID) {
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
                    case "setting#postHeatingState":
                        bits.set(3, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#humidityAdjustState":
                        bits.set(2, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#co2AdjustState":
                        bits.set(1, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#powerState":
                        bits.set(0, (command == OnOffType.ON) ? true : false);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'select'", channelID);
                        return;
                }
                break;
            case "program1":
                switch (channelID) {
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
                    case "setting#automaticHumidityLevelSeekerState":
                        bits.set(4, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#boostSwitchMode":
                        bits.set(5, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#radiatorType":
                        bits.set(6, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#cascadeAdjust":
                        bits.set(7, (command == OnOffType.ON) ? true : false);
                        break;
                    case "setting#adjustmentIntervalMinutes":
                        byte temp = (byte) (Integer.parseInt(command.toString()) & 0x0F);
                        BitSet aim = BitSet.valueOf(new byte[] { temp });
                        bits.set(0, aim.get(0));
                        bits.set(1, aim.get(1));
                        bits.set(2, aim.get(2));
                        bits.set(3, aim.get(3));
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'program1'", channelID);
                        return;
                }
                break;
            case "flags5":
                switch (channelID) {
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
                    case "setting#preHeatingState":
                        bits.set(7, (command == OnOffType.ON) ? true : false);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'flags5'", channelID);
                        return;
                }
                break;
            case "program2":
                switch (channelID) {
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
                    case "setting#maxSpeedLimitMode":
                        bits.set(0, (command == OnOffType.ON) ? true : false);
                        break;
                    default:
                        logger.debug("Unsupported OnOff type channel '{}' with parentChannel 'program2'", channelID);
                        return;
                }
                break;
            default:
                logger.debug("Unsupported command '{}' to channel '{}' received", command, channelID);
                break;
        }
        // Ensure that byte array length is always 8 even if all bits are 0.
        byte[] cmd = Arrays.copyOf(bits.toByteArray(), 8);
        if (cmd != null) {
            sendCommand(channelAsByte, cmd[0]);
        }
    }

    /**
     * Handle decimal type commands. CO2 set point is 16bit values and high and low bytes are handled separately.
     *
     * @param command the command to send
     * @param channelID the channel where the command is sent
     * @param channelAsByte the channel as byte value
     */
    private void handleDecimalCommand(DecimalType command, String channelID, Byte channelAsByte) {
        if (channelID.equals("setting#co2SetPoint")) {
            int commandValue = command.intValue();
            byte lowByte = (byte) (commandValue & 0xFF);
            byte highByte = (byte) ((commandValue >>> 8) & 0xFF);
            sendCommand(ChannelMapper.getVariable("setting#co2SetPointHigh"), highByte);
            sendCommand(ChannelMapper.getVariable("setting#co2SetPointLow"), lowByte);
            return;
        }
        if (channelID.equals("setting#adjustmentIntervalMinutes")) {
            handleOnOffCommand(command, channelID, channelAsByte);
            return;
        }
        ValloxChannel valloxChannel = ChannelMapper.getValloxChannel(channelID);
        sendCommand(channelAsByte, valloxChannel.convertFromState(command.byteValue()));
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
    Runnable checkConnection = () -> {
        if (reconnect) {
            reconnect = false;
            closeConnection();
        }
        connect();
    };

    /**
     * Ensure that all linked channels have been polled at least once and has a value.
     * OnOffType or DecimalType commands needs a cached value.
     */
    Runnable refreshChannels = () -> {
        if (isConnected()) {
            try {
                for (String channel : linkedChannels()) {
                    Byte channelAsByte = ChannelMapper.getVariable(channel);
                    if (channelAsByte != 00 && cache.isExpired(channelAsByte)) {
                        sendPoll(channelAsByte);
                        logger.debug("Refreshing channel: {}", channel);
                    }
                }
            } catch (Exception ex) {
                logger.error("Exception sending heartbeat poll: ", ex);
                Thread.currentThread().interrupt();
                reconnect = true;
            }
        }
    };

    /**
     * Forward poll telegram to connection handler.
     *
     * @param channel the channel to be polled
     */
    public void sendPoll(byte channel) {
        if (connector != null) {
            connector.sendTelegram(TelegramFactory.createPoll(panelNumber, channel));
        }
    }

    /**
     * Forward command telegram to connection handler.
     *
     * @param channel the channel which the command is sent to
     * @param value the command to send
     */
    public void sendCommand(byte channel, byte value) {
        if (connector != null) {
            connector.sendTelegram(TelegramFactory.createCommand(panelNumber, channel, value));
        }
    }

    /**
     * Handle telegram received from connection handler.
     */
    @Override
    public void telegramReceived(Telegram telegram) {
        switch (telegram.state) {
            case ACK:
                logger.debug("Received ack byte '{}'", telegram.toString());
                break;
            case OK:
                if (logger.isTraceEnabled()) {
                    logger.trace("{} {}", telegram.stateDetails(), telegram.toString());
                }
                String channelID = ChannelMapper.getChannelForVariable(telegram.getVariable());
                try {
                    telegram.parse(channelID, cache).forEach((channel, state) -> {
                        updateState(channel, state);
                    });
                } catch (Exception e) {
                    logger.warn("Telegram parsing failed: {}", e.getMessage(), e);
                    break;
                }
                cache.put(telegram);
                break;
            case CRC_ERROR:
            case EMPTY:
            case NOT_DOMAIN:
            case NOT_FOR_US:
            case CORRUPTED:
                if (logger.isTraceEnabled()) {
                    logger.trace("{} {}", telegram.stateDetails(), telegram.toString());
                }
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
        if (exception != null && logger.isTraceEnabled()) {
            logger.trace("{}", error, exception);
        }
        logger.debug("Reconnecting after error: {}", error);
        reconnect = true;
        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, error);
    }
}
