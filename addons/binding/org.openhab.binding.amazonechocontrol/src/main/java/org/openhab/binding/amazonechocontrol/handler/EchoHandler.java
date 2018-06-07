/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.amazonechocontrol.handler;

import static org.openhab.binding.amazonechocontrol.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.HttpException;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState.QueueEntry;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.InfoText;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.MainArt;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Provider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Volume;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link EchoHandler} is responsible for the handling of the echo device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class EchoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EchoHandler.class);

    private @Nullable Device device;
    private @Nullable Connection connection;
    private @Nullable AccountHandler account;
    private @Nullable ScheduledFuture<?> updateStateJob;
    private @Nullable String lastKnownRadioStationId;
    private @Nullable String lastKnownBluetoothMAC;
    private @Nullable String lastKnownAmazonMusicId;
    private String musicProviderId = "TUNEIN";
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private int lastKnownVolume = 25;
    private @Nullable BluetoothState bluetoothState;
    private boolean disableUpdate = false;
    private boolean updateRemind = true;
    private boolean updateTextToSpeech = true;
    private boolean updateAlarm = true;
    private boolean updateRoutine = true;
    private boolean updatePlayMusicVoiceCommand = true;
    private boolean updateStartCommand = true;
    private @Nullable JsonNotificationResponse currentNotification;
    private @Nullable ScheduledFuture<?> currentNotifcationUpdateTimer;

    public EchoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Amazon Echo Control Binding initialized");

        if (this.connection != null) {
            setDeviceAndUpdateThingState(this.device);
        } else {
            updateStatus(ThingStatus.UNKNOWN);
            Bridge bridge = this.getBridge();
            if (bridge != null) {
                AccountHandler account = (AccountHandler) bridge.getHandler();
                if (account != null) {
                    this.account = account;
                    account.addEchoHandler(this);
                }
            }
        }
    }

    public void intialize(Connection connection, @Nullable Device deviceJson) {
        this.connection = connection;
        setDeviceAndUpdateThingState(deviceJson);
    }

    boolean setDeviceAndUpdateThingState(@Nullable Device device) {
        if (device == null) {
            updateStatus(ThingStatus.UNKNOWN);
            return false;
        }
        this.device = device;
        if (!device.online) {
            updateStatus(ThingStatus.OFFLINE);
            return false;
        }
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    @Override
    public void dispose() {
        stopCurrentNotification();
        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            updateStateJob.cancel(false);
        }
        super.dispose();
    }

    public @Nullable BluetoothState findBluetoothState() {
        return this.bluetoothState;
    }

    public @Nullable Connection findConnection() {
        return this.connection;
    }

    public @Nullable AccountHandler findAccount() {
        return this.account;
    }

    public @Nullable Device findDevice() {
        return this.device;
    }

    public String findSerialNumber() {
        String id = (String) getConfig().get(DEVICE_PROPERTY_SERIAL_NUMBER);
        if (id == null) {
            return "";
        }
        return id;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            int waitForUpdate = 1000;
            boolean needBluetoothRefresh = false;
            String lastKnownBluetoothMAC = this.lastKnownBluetoothMAC;

            ScheduledFuture<?> updateStateJob = this.updateStateJob;
            this.updateStateJob = null;
            if (updateStateJob != null) {
                updateStateJob.cancel(false);
            }

            Connection connection = this.connection;
            if (connection == null) {
                return;
            }
            Device device = this.device;
            if (device == null) {
                return;
            }

            // Player commands
            String channelId = channelUID.getId();
            if (channelId.equals(CHANNEL_PLAYER)) {
                if (command == PlayPauseType.PAUSE || command == OnOffType.OFF) {
                    connection.command(device, "{\"type\":\"PauseCommand\"}");
                } else if (command == PlayPauseType.PLAY || command == OnOffType.ON) {
                    if (isPaused) {
                        connection.command(device, "{\"type\":\"PlayCommand\"}");
                    } else {
                        connection.playMusicVoiceCommand(device, this.musicProviderId, "!");
                        waitForUpdate = 3000;
                    }
                } else if (command == NextPreviousType.NEXT) {
                    connection.command(device, "{\"type\":\"NextCommand\"}");
                } else if (command == NextPreviousType.PREVIOUS) {
                    connection.command(device, "{\"type\":\"PreviousCommand\"}");
                } else if (command == RewindFastforwardType.FASTFORWARD) {
                    connection.command(device, "{\"type\":\"ForwardCommand\"}");
                } else if (command == RewindFastforwardType.REWIND) {
                    connection.command(device, "{\"type\":\"RewindCommand\"}");
                }
            }
            // Volume commands
            if (channelId.equals(CHANNEL_VOLUME)) {
                if (command instanceof PercentType) {
                    PercentType value = (PercentType) command;
                    int volume = value.intValue();
                    connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + volume
                            + ",\"contentFocusClientId\":\"Default\"}");
                } else if (command == OnOffType.OFF) {
                    connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + 0
                            + ",\"contentFocusClientId\":\"Default\"}");
                } else if (command == OnOffType.ON) {
                    connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + lastKnownVolume
                            + ",\"contentFocusClientId\":\"Default\"}");
                } else if (command == IncreaseDecreaseType.INCREASE) {
                    if (lastKnownVolume < 100) {
                        lastKnownVolume++;
                        updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                        connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + lastKnownVolume
                                + ",\"contentFocusClientId\":\"Default\"}");
                    }
                } else if (command == IncreaseDecreaseType.DECREASE) {
                    if (lastKnownVolume > 0) {
                        lastKnownVolume--;
                        updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                        connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + lastKnownVolume
                                + ",\"contentFocusClientId\":\"Default\"}");
                    }
                }
            }
            // shuffle command
            if (channelId.equals(CHANNEL_SHUFFLE)) {
                if (command instanceof OnOffType) {
                    OnOffType value = (OnOffType) command;

                    connection.command(device, "{\"type\":\"ShuffleCommand\",\"shuffle\":\""
                            + (value == OnOffType.ON ? "true" : "false") + "\"}");
                }
            }

            // play music command
            if (channelId.equals(CHANNEL_MUSIC_PROVIDER_ID)) {
                if (command instanceof StringType) {
                    waitForUpdate = 0;
                    String musicProviderId = ((StringType) command).toFullString();
                    if (!StringUtils.equals(musicProviderId, this.musicProviderId)) {
                        this.musicProviderId = musicProviderId;
                        if (this.isPlaying) {
                            connection.playMusicVoiceCommand(device, this.musicProviderId, "!");
                            waitForUpdate = 3000;
                        }
                    }

                }
            }
            if (channelId.equals(CHANNEL_PLAY_MUSIC_VOICE_COMMAND)) {
                if (command instanceof StringType) {
                    String voiceCommand = ((StringType) command).toFullString();
                    if (!this.musicProviderId.isEmpty()) {
                        connection.playMusicVoiceCommand(device, this.musicProviderId, voiceCommand);
                        waitForUpdate = 3000;
                        updatePlayMusicVoiceCommand = true;
                    }
                }
            }

            // bluetooth commands
            if (channelId.equals(CHANNEL_BLUETOOTH_MAC)) {
                needBluetoothRefresh = true;
                if (command instanceof StringType) {
                    String address = ((StringType) command).toFullString();
                    if (!address.isEmpty()) {
                        waitForUpdate = 4000;
                    }
                    connection.bluetooth(device, address);
                }
            }
            if (channelId.equals(CHANNEL_BLUETOOTH)) {
                needBluetoothRefresh = true;
                if (command == OnOffType.ON) {
                    waitForUpdate = 4000;
                    String bluetoothId = lastKnownBluetoothMAC;
                    BluetoothState state = bluetoothState;
                    if (state != null && (StringUtils.isEmpty(bluetoothId))) {
                        PairedDevice[] pairedDeviceList = state.pairedDeviceList;
                        if (pairedDeviceList != null) {
                            for (PairedDevice paired : pairedDeviceList) {
                                if (paired == null) {
                                    continue;
                                }
                                if (StringUtils.isNotEmpty(paired.address)) {
                                    lastKnownBluetoothMAC = paired.address;
                                    break;
                                }
                            }
                        }
                    }
                    if (StringUtils.isNotEmpty(lastKnownBluetoothMAC)) {
                        connection.bluetooth(device, lastKnownBluetoothMAC);
                    }
                } else if (command == OnOffType.OFF) {
                    connection.bluetooth(device, null);
                }
            }
            if (channelId.equals(CHANNEL_BLUETOOTH_DEVICE_NAME)) {
                needBluetoothRefresh = true;
            }
            // amazon music commands
            if (channelId.equals(CHANNEL_AMAZON_MUSIC_TRACK_ID)) {
                if (command instanceof StringType) {

                    String trackId = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(trackId)) {
                        waitForUpdate = 3000;
                    }
                    connection.playAmazonMusicTrack(device, trackId);

                }
            }
            if (channelId.equals(CHANNEL_AMAZON_MUSIC_PLAY_LIST_ID)) {
                if (command instanceof StringType) {

                    String playListId = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(playListId)) {
                        waitForUpdate = 3000;
                    }
                    connection.playAmazonMusicPlayList(device, playListId);

                }
            }
            if (channelId.equals(CHANNEL_AMAZON_MUSIC)) {

                if (command == OnOffType.ON) {
                    String lastKnownAmazonMusicId = this.lastKnownAmazonMusicId;
                    if (StringUtils.isNotEmpty(lastKnownAmazonMusicId)) {
                        waitForUpdate = 3000;
                    }
                    connection.playAmazonMusicTrack(device, lastKnownAmazonMusicId);
                } else if (command == OnOffType.OFF) {
                    connection.playAmazonMusicTrack(device, "");
                }
            }

            // radio commands
            if (channelId.equals(CHANNEL_RADIO_STATION_ID)) {
                if (command instanceof StringType) {
                    String stationId = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(stationId)) {
                        waitForUpdate = 3000;
                    }
                    connection.playRadio(device, stationId);
                }
            }
            if (channelId.equals(CHANNEL_RADIO)) {
                if (command == OnOffType.ON) {
                    String lastKnownRadioStationId = this.lastKnownRadioStationId;
                    if (StringUtils.isNotEmpty(lastKnownRadioStationId)) {
                        waitForUpdate = 3000;
                    }
                    connection.playRadio(device, lastKnownRadioStationId);
                } else if (command == OnOffType.OFF) {
                    connection.playRadio(device, "");
                }
            }

            // notification
            if (channelId.equals(CHANNEL_REMIND)) {
                if (command instanceof StringType) {
                    stopCurrentNotification();
                    String reminder = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(reminder)) {
                        waitForUpdate = 3000;
                        updateRemind = true;
                        currentNotification = connection.notification(device, "Reminder", reminder, null);
                        currentNotifcationUpdateTimer = scheduler.scheduleWithFixedDelay(() -> {
                            updateNotificationTimerState();
                        }, 1, 1, TimeUnit.SECONDS);
                    }
                }
            }
            if (channelId.equals(CHANNEL_PLAY_ALARM_SOUND)) {
                if (command instanceof StringType) {
                    stopCurrentNotification();
                    String alarmSound = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(alarmSound)) {
                        waitForUpdate = 3000;
                        updateAlarm = true;
                        String[] parts = alarmSound.split(":", 2);
                        JsonNotificationSound sound = new JsonNotificationSound();
                        if (parts.length == 2) {
                            sound.providerId = parts[0];
                            sound.id = parts[1];
                        } else {
                            sound.providerId = "ECHO";
                            sound.id = alarmSound;
                        }
                        currentNotification = connection.notification(device, "Alarm", null, sound);
                        currentNotifcationUpdateTimer = scheduler.scheduleWithFixedDelay(() -> {
                            updateNotificationTimerState();
                        }, 1, 1, TimeUnit.SECONDS);

                    }
                }
            }

            // routine commands
            if (channelId.equals(CHANNEL_TEXT_TO_SPEECH)) {
                if (command instanceof StringType) {
                    String text = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(text)) {
                        waitForUpdate = 1000;
                        updateTextToSpeech = true;
                        connection.textToSpeech(device, text);
                    }
                }
            }
            if (channelId.equals(CHANNEL_START_COMMAND)) {
                if (command instanceof StringType) {
                    String commandText = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(commandText)) {
                        updateStartCommand = true;
                        if (commandText.startsWith(FLASH_BRIEFING_COMMAND_PREFIX)) {
                            // Handle custom flashbriefings commands
                            String flashbriefing = commandText.substring(FLASH_BRIEFING_COMMAND_PREFIX.length());

                            AccountHandler account = this.account;
                            if (account != null) {
                                for (FlashBriefingProfileHandler flashBriefing : account
                                        .getFlashBriefingProfileHandlers()) {
                                    ThingUID flashBriefingId = flashBriefing.getThing().getUID();
                                    if (StringUtils.equals(flashBriefing.getThing().getUID().getId(), flashbriefing)) {
                                        flashBriefing.handleCommand(
                                                new ChannelUID(flashBriefingId, CHANNEL_PLAY_ON_DEVICE),
                                                new StringType(device.serialNumber));
                                        break;
                                    }
                                }
                            }
                        } else {
                            // Handle standard commands
                            if (!commandText.startsWith("Alexa.")) {
                                commandText = "Alexa." + commandText + ".Play";
                            }
                            waitForUpdate = 1000;
                            connection.executeSequenceCommand(device, commandText, null);
                        }
                    }
                }
            }
            if (channelId.equals(CHANNEL_START_ROUTINE)) {
                if (command instanceof StringType) {
                    String utterance = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(utterance)) {
                        waitForUpdate = 1000;
                        updateRoutine = true;
                        connection.startRoutine(device, utterance);
                    }
                }
            }

            // force update of the state
            this.disableUpdate = true;
            final boolean bluetoothRefresh = needBluetoothRefresh;
            Runnable doRefresh = () -> {
                BluetoothState state = null;
                if (bluetoothRefresh) {
                    JsonBluetoothStates states;
                    states = connection.getBluetoothConnectionStates();
                    state = states.findStateByDevice(device);

                }
                this.disableUpdate = false;
                updateState(device, state);
            };
            if (command instanceof RefreshType) {
                waitForUpdate = 0;
            }
            if (waitForUpdate == 0) {
                doRefresh.run();
            } else {
                this.updateStateJob = scheduler.schedule(doRefresh, waitForUpdate, TimeUnit.MILLISECONDS);
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("handleCommand fails: {}", e);
        }
    }

    private void stopCurrentNotification() {
        ScheduledFuture<?> currentNotifcationUpdateTimer = this.currentNotifcationUpdateTimer;
        if (currentNotifcationUpdateTimer != null) {
            this.currentNotifcationUpdateTimer = null;
            currentNotifcationUpdateTimer.cancel(true);
        }
        JsonNotificationResponse currentNotification = this.currentNotification;
        if (currentNotification != null) {
            this.currentNotification = null;
            Connection currentConnection = this.connection;
            if (currentConnection != null) {
                try {
                    currentConnection.stopNotification(currentNotification);
                } catch (IOException | URISyntaxException e) {
                    logger.warn("Stop notification failed: {}", e);
                }
            }
        }
    }

    private void updateNotificationTimerState() {
        boolean stopCurrentNotifcation = true;
        JsonNotificationResponse currentNotification = this.currentNotification;
        try {
            if (currentNotification != null) {
                Connection currentConnection = connection;
                if (currentConnection != null) {
                    JsonNotificationResponse newState = currentConnection.getNotificationState(currentNotification);
                    if (StringUtils.equals(newState.status, "ON")) {
                        stopCurrentNotifcation = false;
                    }
                }
            }
        } catch (IOException | URISyntaxException e) {
            logger.warn("update notification state fails: {}", e);
        }
        if (stopCurrentNotifcation) {
            if (currentNotification != null) {
                String type = currentNotification.type;
                if (type != null) {
                    if (type.equals("Reminder")) {
                        updateState(CHANNEL_REMIND, new StringType(""));
                        updateRemind = false;
                    }
                    if (type.equals("Alarm")) {
                        updateState(CHANNEL_PLAY_ALARM_SOUND, new StringType(""));
                        updateAlarm = false;
                    }
                }
            }
            stopCurrentNotification();
        }
    }

    public void updateState(@Nullable Device device, @Nullable BluetoothState bluetoothState) {
        if (this.disableUpdate) {
            return;
        }
        if (!setDeviceAndUpdateThingState(device)) {
            return;
        }
        if (device == null) {
            return;
        }

        Connection connection = this.connection;
        if (connection == null) {
            return;
        }

        PlayerInfo playerInfo = null;
        Provider provider = null;
        InfoText infoText = null;
        MainArt mainArt = null;
        try {
            JsonPlayerState playerState = connection.getPlayer(device);
            playerInfo = playerState.playerInfo;
            if (playerInfo != null) {
                infoText = playerInfo.infoText;
                if (infoText == null) {
                    infoText = playerInfo.miniInfoText;
                }
                mainArt = playerInfo.mainArt;
                provider = playerInfo.provider;
            }
        } catch (HttpException e) {
            if (e.getCode() == 400) {
                // Ignore
            } else {
                logger.info("getPlayer fails: {}", e);
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("getPlayer fails: {}", e);
        }
        JsonMediaState mediaState = null;
        try {
            mediaState = connection.getMediaState(device);

        } catch (HttpException e) {
            if (e.getCode() == 400) {

                updateState(CHANNEL_RADIO_STATION_ID, new StringType(""));

            } else {
                logger.info("getMediaState fails: {}", e);
            }
        } catch (IOException | URISyntaxException e) {
            logger.info("getMediaState fails: {}", e);
        }
        // check playing
        isPlaying = (playerInfo != null && StringUtils.equals(playerInfo.state, "PLAYING"))
                || (mediaState != null && StringUtils.equals(mediaState.currentState, "PLAYING"));

        isPaused = (playerInfo != null && StringUtils.equals(playerInfo.state, "PAUSED"))
                || (mediaState != null && StringUtils.equals(mediaState.currentState, "PAUSED"));
        // handle music provider id

        if (provider != null && isPlaying) {
            String musicProviderId;
            if (mediaState != null && StringUtils.equals(mediaState.currentState, "PLAYING")) {
                musicProviderId = mediaState.providerId;
            } else {
                musicProviderId = provider.providerName;
            }
            // Map the music provider id to the one used for starting music with voice command
            if (musicProviderId != null) {
                musicProviderId = musicProviderId.toUpperCase();
            }
            if (StringUtils.equals(musicProviderId, "CLOUD_PLAYER")) {
                musicProviderId = "AMAZON_MUSIC";
            }
            if (StringUtils.equals(musicProviderId, "TUNE_IN")) {
                musicProviderId = "TUNEIN";
            }
            if (musicProviderId != null) {
                this.musicProviderId = musicProviderId;
            }
        }

        // handle amazon music
        String amazonMusicTrackId = "";
        String amazonMusicPlayListId = "";
        boolean amazonMusic = false;
        if (mediaState != null && isPlaying && StringUtils.equals(mediaState.providerId, "CLOUD_PLAYER")
                && StringUtils.isNotEmpty(mediaState.contentId)) {
            amazonMusicTrackId = mediaState.contentId;
            lastKnownAmazonMusicId = amazonMusicTrackId;
            amazonMusic = true;
        }

        // handle bluetooth
        String bluetoothMAC = "";
        String bluetoothDeviceName = "";
        boolean bluetoothIsConnected = false;
        if (bluetoothState != null) {
            this.bluetoothState = bluetoothState;
            PairedDevice[] pairedDeviceList = bluetoothState.pairedDeviceList;
            if (pairedDeviceList != null) {
                for (PairedDevice paired : pairedDeviceList) {
                    if (paired == null) {
                        continue;
                    }
                    if (paired.connected && paired.address != null) {
                        bluetoothIsConnected = true;
                        bluetoothMAC = paired.address;
                        bluetoothDeviceName = paired.friendlyName;
                        if (StringUtils.isEmpty(bluetoothDeviceName)) {
                            bluetoothDeviceName = paired.address;
                        }
                        break;
                    }
                }
            }
        }
        if (StringUtils.isNotEmpty(bluetoothMAC)) {
            lastKnownBluetoothMAC = bluetoothMAC;
        }

        // handle radio
        boolean isRadio = false;
        if (mediaState != null && StringUtils.isNotEmpty(mediaState.radioStationId)) {
            lastKnownRadioStationId = mediaState.radioStationId;
            if (provider != null && StringUtils.equalsIgnoreCase(provider.providerName, "TuneIn Live-Radio")) {
                isRadio = true;
            }
        }
        String radioStationId = "";
        if (isRadio && mediaState != null && StringUtils.equals(mediaState.currentState, "PLAYING")
                && mediaState.radioStationId != null) {
            radioStationId = mediaState.radioStationId;
        }

        // handle title, subtitle, imageUrl
        String title = "";
        String subTitle1 = "";
        String subTitle2 = "";
        String imageUrl = "";
        if (infoText != null) {
            if (infoText.title != null) {
                title = infoText.title;
            }
            if (infoText.subText1 != null) {
                subTitle1 = infoText.subText1;
            }

            if (infoText.subText2 != null) {
                subTitle2 = infoText.subText2;
            }
        }
        if (mainArt != null) {
            if (mainArt.url != null) {
                imageUrl = mainArt.url;
            }
        }
        if (mediaState != null) {
            QueueEntry[] queueEntries = mediaState.queue;
            if (queueEntries != null && queueEntries.length > 0) {
                QueueEntry entry = queueEntries[0];
                if (entry != null) {

                    if (isRadio) {
                        if (StringUtils.isEmpty(imageUrl) && entry.imageURL != null) {
                            imageUrl = entry.imageURL;
                        }
                        if (StringUtils.isEmpty(subTitle1) && entry.radioStationSlogan != null) {
                            subTitle1 = entry.radioStationSlogan;
                        }
                        if (StringUtils.isEmpty(subTitle2) && entry.radioStationLocation != null) {
                            subTitle2 = entry.radioStationLocation;
                        }
                    }
                }
            }
        }

        // handle provider
        String providerDisplayName = "";
        if (provider != null) {
            if (provider.providerDisplayName != null) {
                providerDisplayName = provider.providerDisplayName;
            }
            if (StringUtils.isNotEmpty(provider.providerName) && StringUtils.isEmpty(providerDisplayName)) {
                providerDisplayName = provider.providerName;
            }
        }

        // handle volume
        Integer volume = null;
        if (mediaState != null) {
            volume = mediaState.volume;
        } else if (playerInfo != null) {

            Volume volumnInfo = playerInfo.volume;
            if (volumnInfo != null) {
                volume = volumnInfo.volume;
            }
        }
        if (volume != null && volume > 0) {
            lastKnownVolume = volume;
        }

        // Update states
        if (updateRemind && currentNotifcationUpdateTimer == null) {
            updateRemind = false;
            updateState(CHANNEL_REMIND, new StringType(""));
        }
        if (updateAlarm && currentNotifcationUpdateTimer == null) {
            updateAlarm = false;
            updateState(CHANNEL_PLAY_ALARM_SOUND, new StringType(""));
        }
        if (updateRoutine) {
            updateRoutine = false;
            updateState(CHANNEL_START_ROUTINE, new StringType(""));
        }
        if (updateTextToSpeech) {
            updateTextToSpeech = false;
            updateState(CHANNEL_TEXT_TO_SPEECH, new StringType(""));
        }
        if (updatePlayMusicVoiceCommand) {
            updatePlayMusicVoiceCommand = false;
            updateState(CHANNEL_PLAY_MUSIC_VOICE_COMMAND, new StringType(""));
        }
        if (updateStartCommand) {
            updateStartCommand = false;
            updateState(CHANNEL_START_COMMAND, new StringType(""));
        }
        updateState(CHANNEL_MUSIC_PROVIDER_ID, new StringType(musicProviderId));
        updateState(CHANNEL_AMAZON_MUSIC_TRACK_ID, new StringType(amazonMusicTrackId));
        updateState(CHANNEL_AMAZON_MUSIC, isPlaying && amazonMusic ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_AMAZON_MUSIC_PLAY_LIST_ID, new StringType(amazonMusicPlayListId));
        updateState(CHANNEL_RADIO_STATION_ID, new StringType(radioStationId));
        updateState(CHANNEL_RADIO, isPlaying && isRadio ? OnOffType.ON : OnOffType.OFF);
        updateState(CHANNEL_VOLUME, volume != null ? new PercentType(volume) : UnDefType.UNDEF);
        updateState(CHANNEL_PROVIDER_DISPLAY_NAME, new StringType(providerDisplayName));
        updateState(CHANNEL_PLAYER, isPlaying ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateState(CHANNEL_IMAGE_URL, new StringType(imageUrl));
        updateState(CHANNEL_TITLE, new StringType(title));
        updateState(CHANNEL_SUBTITLE1, new StringType(subTitle1));
        updateState(CHANNEL_SUBTITLE2, new StringType(subTitle2));
        if (bluetoothState != null) {
            updateState(CHANNEL_BLUETOOTH, bluetoothIsConnected ? OnOffType.ON : OnOffType.OFF);
            updateState(CHANNEL_BLUETOOTH_MAC, new StringType(bluetoothMAC));
            updateState(CHANNEL_BLUETOOTH_DEVICE_NAME, new StringType(bluetoothDeviceName));
        }
    }
}
