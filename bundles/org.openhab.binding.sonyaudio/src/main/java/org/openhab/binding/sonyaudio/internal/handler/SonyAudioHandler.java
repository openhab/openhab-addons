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
package org.openhab.binding.sonyaudio.internal.handler;

import static org.openhab.binding.sonyaudio.internal.SonyAudioBindingConstants.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.openhab.binding.sonyaudio.internal.SonyAudioBindingConstants;
import org.openhab.binding.sonyaudio.internal.SonyAudioEventListener;
import org.openhab.binding.sonyaudio.internal.protocol.SonyAudioConnection;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SonyAudioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Åberg - Initial contribution
 */
abstract class SonyAudioHandler extends BaseThingHandler implements SonyAudioEventListener {

    private final Logger logger = LoggerFactory.getLogger(SonyAudioHandler.class);

    private WebSocketClient webSocketClient;

    protected SonyAudioConnection connection;
    private ScheduledFuture<?> connectionCheckerFuture;
    private ScheduledFuture<?> refreshJob;

    private int currentRadioStation = 0;
    private final Map<Integer, String> input_zone = new HashMap<>();

    private static final long CACHE_EXPIRY = TimeUnit.SECONDS.toMillis(5);

    protected ExpiringCache<Boolean>[] powerCache;
    protected ExpiringCache<SonyAudioConnection.SonyAudioInput>[] inputCache;
    protected ExpiringCache<SonyAudioConnection.SonyAudioVolume>[] volumeCache;
    protected ExpiringCache<Map<String, String>> soundSettingsCache;

    protected Supplier<Boolean>[] powerSupplier;
    protected Supplier<SonyAudioConnection.SonyAudioInput>[] inputSupplier;
    protected Supplier<SonyAudioConnection.SonyAudioVolume>[] volumeSupplier;
    protected Supplier<Map<String, String>> soundSettingsSupplier;

    @SuppressWarnings("unchecked")
    public SonyAudioHandler(Thing thing, WebSocketClient webSocketClient) {
        super(thing);

        this.webSocketClient = webSocketClient;

        powerCache = new ExpiringCache[5];
        powerSupplier = new Supplier[5];
        inputCache = new ExpiringCache[5];
        inputSupplier = new Supplier[5];
        volumeCache = new ExpiringCache[5];
        volumeSupplier = new Supplier[5];

        for (int i = 0; i < 5; i++) {
            final int index = i;

            inputSupplier[i] = () -> {
                try {
                    return connection.getInput(index);
                } catch (IOException ex) {
                    throw new CompletionException(ex);
                }
            };

            powerSupplier[i] = () -> {
                try {
                    return connection.getPower(index);
                } catch (IOException ex) {
                    throw new CompletionException(ex);
                }
            };

            volumeSupplier[i] = () -> {
                try {
                    return connection.getVolume(index);
                } catch (IOException ex) {
                    throw new CompletionException(ex);
                }
            };

            powerCache[i] = new ExpiringCache<>(CACHE_EXPIRY, powerSupplier[i]);
            inputCache[i] = new ExpiringCache<>(CACHE_EXPIRY, inputSupplier[i]);
            volumeCache[i] = new ExpiringCache<>(CACHE_EXPIRY, volumeSupplier[i]);
        }

        soundSettingsSupplier = () -> {
            try {
                return connection.getSoundSettings();
            } catch (IOException ex) {
                throw new CompletionException(ex);
            }
        };

        soundSettingsCache = new ExpiringCache<>(CACHE_EXPIRY, soundSettingsSupplier);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (connection == null || !connection.checkConnection()) {
            logger.debug("Thing not yet initialized!");
            return;
        }

        String id = channelUID.getId();

        logger.debug("Handle command {} {}", channelUID, command);

        if (getThing().getStatusInfo().getStatus() != ThingStatus.ONLINE) {
            switch (id) {
                case CHANNEL_POWER:
                case CHANNEL_MASTER_POWER:
                    logger.debug("Device powered off sending {} {}", channelUID, command);
                    break;
                default:
                    logger.debug("Device powered off ignore command {} {}", channelUID, command);
                    return;
            }
        }

        try {
            switch (id) {
                case CHANNEL_POWER:
                case CHANNEL_MASTER_POWER:
                    handlePowerCommand(command, channelUID);
                    break;
                case CHANNEL_ZONE1_POWER:
                    handlePowerCommand(command, channelUID, 1);
                    break;
                case CHANNEL_ZONE2_POWER:
                    handlePowerCommand(command, channelUID, 2);
                    break;
                case CHANNEL_ZONE3_POWER:
                    handlePowerCommand(command, channelUID, 3);
                    break;
                case CHANNEL_ZONE4_POWER:
                    handlePowerCommand(command, channelUID, 4);
                    break;
                case CHANNEL_INPUT:
                    handleInputCommand(command, channelUID);
                    break;
                case CHANNEL_ZONE1_INPUT:
                    handleInputCommand(command, channelUID, 1);
                    break;
                case CHANNEL_ZONE2_INPUT:
                    handleInputCommand(command, channelUID, 2);
                    break;
                case CHANNEL_ZONE3_INPUT:
                    handleInputCommand(command, channelUID, 3);
                    break;
                case CHANNEL_ZONE4_INPUT:
                    handleInputCommand(command, channelUID, 4);
                    break;
                case CHANNEL_VOLUME:
                    handleVolumeCommand(command, channelUID);
                    break;
                case CHANNEL_ZONE1_VOLUME:
                    handleVolumeCommand(command, channelUID, 1);
                    break;
                case CHANNEL_ZONE2_VOLUME:
                    handleVolumeCommand(command, channelUID, 2);
                    break;
                case CHANNEL_ZONE3_VOLUME:
                    handleVolumeCommand(command, channelUID, 3);
                    break;
                case CHANNEL_ZONE4_VOLUME:
                    handleVolumeCommand(command, channelUID, 4);
                    break;
                case CHANNEL_MUTE:
                    handleMuteCommand(command, channelUID);
                    break;
                case CHANNEL_ZONE1_MUTE:
                    handleMuteCommand(command, channelUID, 1);
                    break;
                case CHANNEL_ZONE2_MUTE:
                    handleMuteCommand(command, channelUID, 2);
                    break;
                case CHANNEL_ZONE3_MUTE:
                    handleMuteCommand(command, channelUID, 3);
                    break;
                case CHANNEL_ZONE4_MUTE:
                    handleMuteCommand(command, channelUID, 4);
                    break;
                case CHANNEL_MASTER_SOUND_FIELD:
                case CHANNEL_SOUND_FIELD:
                    handleSoundSettings(command, channelUID);
                    break;
                case CHANNEL_RADIO_FREQ:
                    handleRadioCommand(command, channelUID);
                    break;
                case CHANNEL_RADIO_STATION:
                    handleRadioStationCommand(command, channelUID);
                    break;
                case CHANNEL_RADIO_SEEK_STATION:
                    handleRadioSeekStationCommand(command, channelUID);
                    break;
                case CHANNEL_NIGHTMODE:
                    handleNightMode(command, channelUID);
                    break;
                default:
                    logger.error("Command {}, {} not supported by {}!", id, command, channelUID);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void handleSoundSettings(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            logger.debug("handleSoundSettings RefreshType");
            Map<String, String> result = soundSettingsCache.getValue();
            if (result != null) {
                updateState(channelUID, new StringType(result.get("soundField")));
            }
        }
        if (command instanceof StringType stringCommand) {
            logger.debug("handleSoundSettings set {}", command);
            connection.setSoundSettings("soundField", stringCommand.toString());
        }
    }

    public void handleNightMode(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            logger.debug("handleNightMode RefreshType");
            Map<String, String> result = soundSettingsCache.getValue();
            if (result != null) {
                updateState(channelUID, new StringType(result.get("nightMode")));
            }
        }
        if (command instanceof OnOffType onOffCommand) {
            logger.debug("handleNightMode set {}", command);
            connection.setSoundSettings("nightMode", onOffCommand == OnOffType.ON ? "on" : "off");
        }
    }

    public void handlePowerCommand(Command command, ChannelUID channelUID) throws IOException {
        handlePowerCommand(command, channelUID, 0);
    }

    public void handlePowerCommand(Command command, ChannelUID channelUID, int zone) throws IOException {
        if (command instanceof RefreshType) {
            try {
                logger.debug("handlePowerCommand RefreshType {}", zone);
                Boolean result = powerCache[zone].getValue();
                updateState(channelUID, result ? OnOffType.ON : OnOffType.OFF);
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof OnOffType onOffCommand) {
            logger.debug("handlePowerCommand set {} {}", zone, command);
            connection.setPower(onOffCommand == OnOffType.ON, zone);
        }
    }

    public void handleInputCommand(Command command, ChannelUID channelUID) throws IOException {
        handleInputCommand(command, channelUID, 0);
    }

    public void handleInputCommand(Command command, ChannelUID channelUID, int zone) throws IOException {
        if (command instanceof RefreshType) {
            logger.debug("handleInputCommand RefreshType {}", zone);
            try {
                SonyAudioConnection.SonyAudioInput result = inputCache[zone].getValue();
                if (result != null) {
                    if (zone > 0) {
                        input_zone.put(zone, result.input);
                    }
                    updateState(channelUID, inputSource(result.input));

                    if (result.radio_freq.isPresent()) {
                        updateState(SonyAudioBindingConstants.CHANNEL_RADIO_FREQ,
                                new DecimalType(result.radio_freq.get() / 1000000.0));
                    }
                }
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof StringType) {
            logger.debug("handleInputCommand set {} {}", zone, command);
            connection.setInput(setInputCommand(command), zone);
        }
    }

    public void handleVolumeCommand(Command command, ChannelUID channelUID) throws IOException {
        handleVolumeCommand(command, channelUID, 0);
    }

    public void handleVolumeCommand(Command command, ChannelUID channelUID, int zone) throws IOException {
        if (command instanceof RefreshType) {
            try {
                logger.debug("handleVolumeCommand RefreshType {}", zone);
                SonyAudioConnection.SonyAudioVolume result = volumeCache[zone].getValue();
                if (result != null) {
                    updateState(channelUID, new PercentType(result.volume));
                }
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof PercentType percentCommand) {
            logger.debug("handleVolumeCommand PercentType set {} {}", zone, command);
            connection.setVolume(percentCommand.intValue(), zone);
        }
        if (command instanceof IncreaseDecreaseType) {
            logger.debug("handleVolumeCommand IncreaseDecreaseType set {} {}", zone, command);
            String change = command == IncreaseDecreaseType.INCREASE ? "+1" : "-1";
            connection.setVolume(change, zone);
        }
        if (command instanceof OnOffType onOffCommand) {
            logger.debug("handleVolumeCommand OnOffType set {} {}", zone, command);
            connection.setMute(onOffCommand == OnOffType.ON, zone);
        }
    }

    public void handleMuteCommand(Command command, ChannelUID channelUID) throws IOException {
        handleMuteCommand(command, channelUID, 0);
    }

    public void handleMuteCommand(Command command, ChannelUID channelUID, int zone) throws IOException {
        if (command instanceof RefreshType) {
            try {
                logger.debug("handleMuteCommand RefreshType {}", zone);
                SonyAudioConnection.SonyAudioVolume result = volumeCache[zone].getValue();
                if (result != null) {
                    updateState(channelUID, result.mute ? OnOffType.ON : OnOffType.OFF);
                }
            } catch (CompletionException ex) {
                throw new IOException(ex.getCause());
            }
        }
        if (command instanceof OnOffType onOffCommand) {
            logger.debug("handleMuteCommand set {} {}", zone, command);
            connection.setMute(onOffCommand == OnOffType.ON, zone);
        }
    }

    public void handleRadioCommand(Command command, ChannelUID channelUID) throws IOException {
    }

    public void handleRadioStationCommand(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            updateState(channelUID, new DecimalType(currentRadioStation));
        }
        if (command instanceof DecimalType decimalCommand) {
            currentRadioStation = decimalCommand.intValue();
            String radioCommand = "radio:fm?contentId=" + currentRadioStation;

            for (int i = 1; i <= 4; i++) {
                String input = input_zone.get(i);
                if (input != null && input.startsWith("radio:fm")) {
                    connection.setInput(radioCommand, i);
                }
            }
        }
    }

    public void handleRadioSeekStationCommand(Command command, ChannelUID channelUID) throws IOException {
        if (command instanceof RefreshType) {
            updateState(channelUID, new StringType(""));
        }
        if (command instanceof StringType stringCommand) {
            switch (stringCommand.toString()) {
                case "fwdSeeking":
                    connection.radioSeekFwd();
                    break;
                case "bwdSeeking":
                    connection.radioSeekBwd();
                    break;
            }
        }
    }

    public abstract String setInputCommand(Command command);

    @Override
    public void initialize() {
        Configuration config = getThing().getConfiguration();
        String ipAddress = (String) config.get(SonyAudioBindingConstants.HOST_PARAMETER);
        String path = (String) config.get(SonyAudioBindingConstants.SCALAR_PATH_PARAMETER);
        Object port_o = config.get(SonyAudioBindingConstants.SCALAR_PORT_PARAMETER);
        int port = 10000;
        if (port_o instanceof BigDecimal decimalValue) {
            port = decimalValue.intValue();
        } else if (port_o instanceof Integer) {
            port = (int) port_o;
        }

        Object refresh_o = config.get(SonyAudioBindingConstants.REFRESHINTERVAL);
        int refresh = 0;
        if (refresh_o instanceof BigDecimal decimalValue) {
            refresh = decimalValue.intValue();
        } else if (refresh_o instanceof Integer) {
            refresh = (int) refresh_o;
        }

        try {
            connection = new SonyAudioConnection(ipAddress, port, path, this, scheduler, webSocketClient);

            Runnable connectionChecker = () -> {
                try {
                    if (!connection.checkConnection()) {
                        if (getThing().getStatus() != ThingStatus.OFFLINE) {
                            logger.debug("Lost connection");
                            updateStatus(ThingStatus.OFFLINE);
                        }
                    }
                } catch (Exception ex) {
                    logger.warn("Exception in check connection to @{}. Cause: {}", connection.getConnectionName(),
                            ex.getMessage(), ex);
                }
            };

            connectionCheckerFuture = scheduler.scheduleWithFixedDelay(connectionChecker, 1, 10, TimeUnit.SECONDS);

            // Start the status updater
            startAutomaticRefresh(refresh);
        } catch (URISyntaxException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing SonyAudioHandler");
        super.dispose();
        if (connectionCheckerFuture != null) {
            connectionCheckerFuture.cancel(true);
            connectionCheckerFuture = null;
        }
        if (refreshJob != null) {
            refreshJob.cancel(true);
            refreshJob = null;
        }
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    @Override
    public void updateConnectionState(boolean connected) {
        logger.debug("Changing connection status to {}", connected);
        if (connected) {
            updateStatus(ThingStatus.ONLINE);
        } else {
            updateStatus(ThingStatus.OFFLINE);
        }
    }

    @Override
    public void updateInput(int zone, SonyAudioConnection.SonyAudioInput input) {
        inputCache[zone].putValue(input);
        switch (zone) {
            case 0:
                updateState(SonyAudioBindingConstants.CHANNEL_INPUT, inputSource(input.input));
                break;
            case 1:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE1_INPUT, inputSource(input.input));
                break;
            case 2:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE2_INPUT, inputSource(input.input));
                break;
            case 3:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE3_INPUT, inputSource(input.input));
                break;
            case 4:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE4_INPUT, inputSource(input.input));
                break;
        }

        if (input.radio_freq.isPresent()) {
            updateState(SonyAudioBindingConstants.CHANNEL_RADIO_FREQ,
                    new DecimalType(input.radio_freq.get() / 1000000.0));
        }
    }

    public abstract StringType inputSource(String input);

    @Override
    public void updateCurrentRadioStation(int radioStation) {
        currentRadioStation = radioStation;
        updateState(SonyAudioBindingConstants.CHANNEL_RADIO_STATION, new DecimalType(currentRadioStation));
    }

    @Override
    public void updateSeekStation(String seek) {
        updateState(SonyAudioBindingConstants.CHANNEL_RADIO_SEEK_STATION, new StringType(seek));
    }

    @Override
    public void updateVolume(int zone, SonyAudioConnection.SonyAudioVolume volume) {
        volumeCache[zone].putValue(volume);
        switch (zone) {
            case 0:
                updateState(SonyAudioBindingConstants.CHANNEL_VOLUME, new PercentType(volume.volume));
                updateState(SonyAudioBindingConstants.CHANNEL_MUTE, volume.mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 1:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE1_VOLUME, new PercentType(volume.volume));
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE1_MUTE, volume.mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 2:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE2_VOLUME, new PercentType(volume.volume));
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE2_MUTE, volume.mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 3:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE3_VOLUME, new PercentType(volume.volume));
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE3_MUTE, volume.mute ? OnOffType.ON : OnOffType.OFF);
                break;
            case 4:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE4_VOLUME, new PercentType(volume.volume));
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE4_MUTE, volume.mute ? OnOffType.ON : OnOffType.OFF);
                break;
        }
    }

    @Override
    public void updatePowerStatus(int zone, boolean power) {
        powerCache[zone].invalidateValue();
        switch (zone) {
            case 0:
                updateState(SonyAudioBindingConstants.CHANNEL_POWER, power ? OnOffType.ON : OnOffType.OFF);
                updateState(SonyAudioBindingConstants.CHANNEL_MASTER_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 1:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE1_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 2:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE2_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 3:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE3_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
            case 4:
                updateState(SonyAudioBindingConstants.CHANNEL_ZONE4_POWER, power ? OnOffType.ON : OnOffType.OFF);
                break;
        }
    }

    private void startAutomaticRefresh(int refresh) {
        if (refresh <= 0) {
            return;
        }

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            List<Channel> channels = getThing().getChannels();
            for (Channel channel : channels) {
                if (!isLinked(channel.getUID())) {
                    continue;
                }
                handleCommand(channel.getUID(), RefreshType.REFRESH);
            }
        }, 5, refresh, TimeUnit.SECONDS);
    }
}
