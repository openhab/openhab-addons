/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Time;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.NextPreviousType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.PlayPauseType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.library.types.RewindFastforwardType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.library.unit.SmartHomeUnits;
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
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity.Description;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAscendingAlarm.AscendingAlarmModel;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushVolumeChange;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDeviceNotificationState.DeviceNotificationState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMediaState.QueueEntry;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonMusicProvider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationResponse;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonNotificationSound;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.InfoText;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.MainArt;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Progress;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Provider;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlayerState.PlayerInfo.Volume;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonPlaylists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link EchoHandler} is responsible for the handling of the echo device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class EchoHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(EchoHandler.class);
    private Gson gson = new Gson();
    private @Nullable Device device;
    private @Nullable AccountHandler account;
    private @Nullable ScheduledFuture<?> updateStateJob;
    private @Nullable ScheduledFuture<?> ignoreVolumeChange;
    private @Nullable ScheduledFuture<?> updateProgressJob;
    private Object progressLock = new Object();
    private @Nullable String wakeWord;
    private @Nullable String lastKnownRadioStationId;
    private @Nullable String lastKnownBluetoothMAC;
    private @Nullable String lastKnownAmazonMusicId;
    private String musicProviderId = "TUNEIN";
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private int lastKnownVolume = 25;
    private int textToSpeechVolume = 0;
    private @Nullable BluetoothState bluetoothState;
    private boolean disableUpdate = false;
    private boolean updateRemind = true;
    private boolean updateTextToSpeech = true;
    private boolean updateAlarm = true;
    private boolean updateRoutine = true;
    private boolean updatePlayMusicVoiceCommand = true;
    private boolean updateStartCommand = true;
    private @Nullable Integer noticationVolumeLevel;
    private @Nullable Boolean ascendingAlarm;
    private @Nullable JsonPlaylists playLists;
    private @Nullable JsonNotificationSound @Nullable [] alarmSounds;
    private @Nullable List<JsonMusicProvider> musicProviders;

    private @Nullable JsonNotificationResponse currentNotification;
    private @Nullable ScheduledFuture<?> currentNotifcationUpdateTimer;
    long mediaLengthMs;
    long mediaProgressMs;
    long mediaStartMs;
    String lastSpokenText = "";

    public EchoHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Amazon Echo Control Binding initialized");
        Bridge bridge = this.getBridge();
        if (bridge != null) {
            AccountHandler account = (AccountHandler) bridge.getHandler();
            if (account != null) {
                setDeviceAndUpdateThingState(account, this.device, null);
                account.addEchoHandler(this);
            }
        }
    }

    public boolean setDeviceAndUpdateThingState(AccountHandler accountHandler, @Nullable Device device,
            @Nullable String wakeWord) {
        this.account = accountHandler;
        if (wakeWord != null) {
            this.wakeWord = wakeWord;
        }
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
            this.disableUpdate = false;
            updateStateJob.cancel(false);
        }
        stopProgressTimer();
        super.dispose();
    }

    private void stopProgressTimer() {
        ScheduledFuture<?> updateProgressJob = this.updateProgressJob;
        this.updateProgressJob = null;
        if (updateProgressJob != null) {
            updateProgressJob.cancel(false);
        }
    }

    public @Nullable BluetoothState findBluetoothState() {
        return this.bluetoothState;
    }

    public @Nullable JsonPlaylists findPlaylists() {
        return this.playLists;
    }

    public @Nullable JsonNotificationSound @Nullable [] findAlarmSounds() {
        return this.alarmSounds;
    }

    public @Nullable List<JsonMusicProvider> findMusicProviders() {
        return this.musicProviders;
    }

    private @Nullable Connection findConnection() {
        AccountHandler accountHandler = this.account;
        if (accountHandler != null) {
            return accountHandler.findConnection();
        }
        return null;
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
                this.disableUpdate = false;
                updateStateJob.cancel(false);
            }
            AccountHandler account = this.account;
            if (account == null) {
                return;
            }
            Connection connection = account.findConnection();
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
            // Notification commands
            if (channelId.equals(CHANNEL_NOTIFICATION_VOLUME)) {
                if (command instanceof PercentType) {
                    int volume = ((PercentType) command).intValue();
                    connection.notificationVolume(device, volume);
                    this.noticationVolumeLevel = volume;
                    waitForUpdate = -1;
                    account.forceCheckData();
                }
            }
            if (channelId.equals(CHANNEL_ASCENDING_ALARM)) {
                if (command == OnOffType.OFF) {
                    connection.ascendingAlarm(device, false);
                    this.ascendingAlarm = false;
                    waitForUpdate = -1;
                    account.forceCheckData();
                }
                if (command == OnOffType.ON) {
                    connection.ascendingAlarm(device, true);
                    this.ascendingAlarm = true;
                    waitForUpdate = -1;
                    account.forceCheckData();
                }
            }
            // Media progress commands
            Long mediaPosition = null;
            if (channelId.equals(CHANNEL_MEDIA_PROGRESS)) {

                if (command instanceof PercentType) {
                    PercentType value = (PercentType) command;
                    int percent = value.intValue();
                    mediaPosition = Math.round((mediaLengthMs / 1000d) * (percent / 100d));
                }
            }
            if (channelId.equals(CHANNEL_MEDIA_PROGRESS_TIME)) {
                if (command instanceof DecimalType) {
                    DecimalType value = (DecimalType) command;
                    mediaPosition = value.longValue();
                }
                if (command instanceof QuantityType<?>) {
                    QuantityType<?> value = (QuantityType<?>) command;
                    @Nullable
                    QuantityType<?> seconds = value.toUnit(SmartHomeUnits.SECOND);
                    if (seconds != null) {
                        mediaPosition = seconds.longValue();
                    }
                }
            }
            if (mediaPosition != null) {
                waitForUpdate = -1;
                synchronized (progressLock) {
                    String seekCommand = "{\"type\":\"SeekCommand\",\"mediaPosition\":" + mediaPosition
                            + ",\"contentFocusClientId\":null}";
                    connection.command(device, seekCommand);
                    connection.command(device, seekCommand); // Must be sent twice, the first one is ignored sometimes
                    this.mediaProgressMs = mediaPosition * 1000;
                    mediaStartMs = System.currentTimeMillis() - this.mediaProgressMs;
                    updateMediaProgress(false);
                }

            }
            // Volume commands
            if (channelId.equals(CHANNEL_VOLUME)) {
                Integer volume = null;
                if (command instanceof PercentType) {
                    PercentType value = (PercentType) command;
                    volume = value.intValue();

                } else if (command == OnOffType.OFF) {
                    volume = 0;

                } else if (command == OnOffType.ON) {
                    volume = lastKnownVolume;
                } else if (command == IncreaseDecreaseType.INCREASE) {
                    if (lastKnownVolume < 100) {
                        lastKnownVolume++;
                        volume = lastKnownVolume;
                    }
                } else if (command == IncreaseDecreaseType.DECREASE) {
                    if (lastKnownVolume > 0) {
                        lastKnownVolume--;
                        volume = lastKnownVolume;
                    }
                }
                if (volume != null) {
                    if (StringUtils.equals(device.deviceFamily, "WHA")) {
                        connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + volume
                                + ",\"contentFocusClientId\":\"Default\"}");

                    } else {
                        Map<String, Object> parameters = new Hashtable<String, Object>();
                        parameters.put("value", volume);
                        connection.executeSequenceCommand(device, "Alexa.DeviceControls.Volume", parameters);
                    }
                    lastKnownVolume = volume;
                    updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                    waitForUpdate = -1;
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
                        startTextToSpeech(connection, device, text);
                    }
                }
            }
            if (channelId.equals(CHANNEL_TEXT_TO_SPEECH_VOLUME)) {
                if (command instanceof PercentType) {
                    PercentType value = (PercentType) command;
                    textToSpeechVolume = value.intValue();
                } else if (command == OnOffType.OFF) {
                    textToSpeechVolume = 0;
                } else if (command == OnOffType.ON) {
                    textToSpeechVolume = lastKnownVolume;
                } else if (command == IncreaseDecreaseType.INCREASE) {
                    if (textToSpeechVolume < 100) {
                        textToSpeechVolume++;
                    }
                } else if (command == IncreaseDecreaseType.DECREASE) {
                    if (textToSpeechVolume > 0) {
                        textToSpeechVolume--;
                    }
                }
                this.updateState(channelId, new PercentType(textToSpeechVolume));
            }
            if (channelId.equals(CHANNEL_LAST_VOICE_COMMAND)) {
                if (command instanceof StringType) {
                    String text = ((StringType) command).toFullString();
                    if (StringUtils.isNotEmpty(text)) {
                        waitForUpdate = -1;
                        startTextToSpeech(connection, device, text);
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

                            for (FlashBriefingProfileHandler flashBriefing : account
                                    .getFlashBriefingProfileHandlers()) {
                                ThingUID flashBriefingId = flashBriefing.getThing().getUID();
                                if (StringUtils.equals(flashBriefing.getThing().getUID().getId(), flashbriefing)) {
                                    flashBriefing.handleCommand(new ChannelUID(flashBriefingId, CHANNEL_PLAY_ON_DEVICE),
                                            new StringType(device.serialNumber));
                                    break;
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
            if (waitForUpdate < 0) {
                return;
            }
            // force update of the state
            this.disableUpdate = true;
            final boolean bluetoothRefresh = needBluetoothRefresh;
            Runnable doRefresh = () -> {
                this.disableUpdate = false;
                BluetoothState state = null;
                if (bluetoothRefresh) {
                    JsonBluetoothStates states;
                    states = connection.getBluetoothConnectionStates();
                    state = states.findStateByDevice(device);
                }

                updateState(account, device, state, null, null, null, null, null);
            };
            if (command instanceof RefreshType) {
                waitForUpdate = 0;
            }
            if (waitForUpdate == 0) {
                doRefresh.run();
            } else {
                this.updateStateJob = scheduler.schedule(doRefresh, waitForUpdate, TimeUnit.MILLISECONDS);
            }
        } catch (IOException |

                URISyntaxException e) {
            logger.info("handleCommand fails: {}", e);
        }
    }

    private void startTextToSpeech(Connection connection, Device device, String text)
            throws IOException, URISyntaxException {
        if (textToSpeechVolume != 0) {
            @Nullable
            ScheduledFuture<?> oldIgnoreVolumeChange = this.ignoreVolumeChange;
            if (oldIgnoreVolumeChange != null) {
                oldIgnoreVolumeChange.cancel(false);
            }
            this.ignoreVolumeChange = scheduler.schedule(this::stopIgnoreVolumeChange, 2000, TimeUnit.MILLISECONDS);
        }
        connection.textToSpeech(device, text, textToSpeechVolume, lastKnownVolume);
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
            Connection currentConnection = this.findConnection();
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
                Connection currentConnection = this.findConnection();
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

    public void updateState(AccountHandler accountHandler, @Nullable Device device,
            @Nullable BluetoothState bluetoothState, @Nullable DeviceNotificationState deviceNotificationState,
            @Nullable AscendingAlarmModel ascendingAlarmModel, @Nullable JsonPlaylists playlists,
            @Nullable JsonNotificationSound @Nullable [] alarmSounds,
            @Nullable List<JsonMusicProvider> musicProviders) {
        try {
            this.logger.debug("Handle updateState {}", this.getThing().getUID().getAsString());

            if (deviceNotificationState != null) {
                noticationVolumeLevel = deviceNotificationState.volumeLevel;
            }
            if (ascendingAlarmModel != null) {
                ascendingAlarm = ascendingAlarmModel.ascendingAlarmEnabled;
            }
            if (playlists != null) {
                this.playLists = playlists;
            }
            if (alarmSounds != null) {
                this.alarmSounds = alarmSounds;
            }
            if (musicProviders != null) {
                this.musicProviders = musicProviders;
            }
            if (!setDeviceAndUpdateThingState(accountHandler, device, null)) {
                this.logger.debug("Handle updateState {} aborted: Not online", this.getThing().getUID().getAsString());
                return;
            }
            if (device == null) {
                this.logger.debug("Handle updateState {} aborted: No device", this.getThing().getUID().getAsString());
                return;
            }

            if (this.disableUpdate) {
                this.logger.debug("Handle updateState {} aborted: Disabled", this.getThing().getUID().getAsString());
                return;
            }
            Connection connection = this.findConnection();
            if (connection == null) {
                return;
            }

            PlayerInfo playerInfo = null;
            Provider provider = null;
            InfoText infoText = null;
            MainArt mainArt = null;
            String musicProviderId = null;
            Progress progress = null;
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
                    if (provider != null) {
                        musicProviderId = provider.providerName;
                        // Map the music provider id to the one used for starting music with voice command
                        if (musicProviderId != null) {
                            musicProviderId = musicProviderId.toUpperCase();

                            if (StringUtils.equals(musicProviderId, "AMAZON MUSIC")) {
                                musicProviderId = "AMAZON_MUSIC";
                            }
                            if (StringUtils.equals(musicProviderId, "CLOUD_PLAYER")) {
                                musicProviderId = "AMAZON_MUSIC";
                            }
                            if (StringUtils.startsWith(musicProviderId, "TUNEIN")) {
                                musicProviderId = "TUNEIN";
                            }
                        }
                    }
                    progress = playerInfo.progress;
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
            // check playing
            isPlaying = (playerInfo != null && StringUtils.equals(playerInfo.state, "PLAYING"));
            // || (mediaState != null && StringUtils.equals(mediaState.currentState, "PLAYING"));

            isPaused = (playerInfo != null && StringUtils.equals(playerInfo.state, "PAUSED"));
            // || (mediaState != null && StringUtils.equals(mediaState.currentState, "PAUSED"));
            synchronized (progressLock) {
                Boolean showTime = null;
                Long mediaLength = null;
                Long mediaProgress = null;
                if (progress != null) {
                    showTime = progress.showTiming;
                    mediaLength = progress.mediaLength;
                    mediaProgress = progress.mediaProgress;
                }
                if (showTime != null && showTime && mediaProgress != null && mediaLength != null) {
                    mediaProgressMs = mediaProgress * 1000;
                    mediaLengthMs = mediaLength * 1000;
                    mediaStartMs = System.currentTimeMillis() - mediaProgressMs;
                    if (isPlaying) {
                        if (updateProgressJob == null) {
                            updateProgressJob = scheduler.scheduleWithFixedDelay(this::updateMediaProgress, 1000, 1000,
                                    TimeUnit.MILLISECONDS);
                        }
                    } else {
                        stopProgressTimer();
                    }

                } else {
                    stopProgressTimer();
                    mediaProgressMs = 0;
                    mediaStartMs = 0;
                    mediaLengthMs = 0;
                }
                updateMediaProgress(true);
            }

            JsonMediaState mediaState = null;
            try {

                if (StringUtils.equalsIgnoreCase(musicProviderId, "AMAZON_MUSIC")
                        || StringUtils.equalsIgnoreCase(musicProviderId, "TUNEIN")) {
                    mediaState = connection.getMediaState(device);
                }

            } catch (HttpException e) {
                if (e.getCode() == 400) {

                    updateState(CHANNEL_RADIO_STATION_ID, new StringType(""));

                } else {
                    logger.info("getMediaState fails: {}", e);
                }
            } catch (IOException | URISyntaxException e) {
                logger.info("getMediaState fails: {}", e);
            }

            // handle music provider id

            if (provider != null && isPlaying) {
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
                if (StringUtils.equalsIgnoreCase(musicProviderId, "TUNEIN")) {
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
            if (this.ignoreVolumeChange == null) {
                if (mediaState != null) {
                    volume = mediaState.volume;
                }
                if (playerInfo != null && volume == null) {

                    Volume volumnInfo = playerInfo.volume;
                    if (volumnInfo != null) {
                        volume = volumnInfo.volume;
                    }
                }
                if (volume != null && volume > 0) {
                    lastKnownVolume = volume;
                }
                if (volume == null) {
                    volume = lastKnownVolume;
                }
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
            updateState(CHANNEL_PROVIDER_DISPLAY_NAME, new StringType(providerDisplayName));
            updateState(CHANNEL_PLAYER, isPlaying ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
            updateState(CHANNEL_IMAGE_URL, new StringType(imageUrl));
            updateState(CHANNEL_TITLE, new StringType(title));
            if (volume != null) {
                updateState(CHANNEL_VOLUME, new PercentType(volume));
            }
            updateState(CHANNEL_SUBTITLE1, new StringType(subTitle1));
            updateState(CHANNEL_SUBTITLE2, new StringType(subTitle2));
            if (bluetoothState != null) {
                updateState(CHANNEL_BLUETOOTH, bluetoothIsConnected ? OnOffType.ON : OnOffType.OFF);
                updateState(CHANNEL_BLUETOOTH_MAC, new StringType(bluetoothMAC));
                updateState(CHANNEL_BLUETOOTH_DEVICE_NAME, new StringType(bluetoothDeviceName));
            }

            updateState(CHANNEL_ASCENDING_ALARM,
                    ascendingAlarm != null ? (ascendingAlarm ? OnOffType.ON : OnOffType.OFF) : UnDefType.UNDEF);

            if (noticationVolumeLevel != null) {
                updateState(CHANNEL_NOTIFICATION_VOLUME, new PercentType(noticationVolumeLevel));
            } else {
                updateState(CHANNEL_NOTIFICATION_VOLUME, UnDefType.UNDEF);
            }

        } catch (Exception e) {
            this.logger.debug("Handle updateState {} failed: {}", this.getThing().getUID().getAsString(), e);

            disableUpdate = false;
            throw e; // Rethrow same exception
        }
    }

    private void updateMediaProgress() {
        updateMediaProgress(false);
    }

    private void updateMediaProgress(boolean updateMediaLength) {
        synchronized (progressLock) {
            if (mediaStartMs > 0) {
                long currentPlayTimeMs = isPlaying ? System.currentTimeMillis() - mediaStartMs : mediaProgressMs;
                if (mediaLengthMs > 0) {
                    int progressPercent = (int) Math.min(100,
                            Math.round((double) currentPlayTimeMs / (double) mediaLengthMs * 100));
                    updateState(CHANNEL_MEDIA_PROGRESS, new PercentType(progressPercent));
                } else {
                    updateState(CHANNEL_MEDIA_PROGRESS, UnDefType.UNDEF);
                }
                updateState(CHANNEL_MEDIA_PROGRESS_TIME,
                        new QuantityType<Time>(currentPlayTimeMs / 1000, SmartHomeUnits.SECOND));
                if (updateMediaLength) {
                    updateState(CHANNEL_MEDIA_LENGTH,
                            new QuantityType<Time>(mediaLengthMs / 1000, SmartHomeUnits.SECOND));
                }
            } else {
                updateState(CHANNEL_MEDIA_PROGRESS, UnDefType.UNDEF);
                updateState(CHANNEL_MEDIA_LENGTH, UnDefType.UNDEF);
                updateState(CHANNEL_MEDIA_PROGRESS_TIME, UnDefType.UNDEF);
                if (updateMediaLength) {
                    updateState(CHANNEL_MEDIA_LENGTH, UnDefType.UNDEF);
                }
            }
        }
    }

    public void handlePushActivity(Activity pushActivity) {
        Description description = pushActivity.ParseDescription();
        if (StringUtils.isEmpty(description.firstUtteranceId)
                || StringUtils.startsWithIgnoreCase(description.firstUtteranceId, "TextClient:")) {
            return;
        }
        String spokenText = description.summary;
        if (spokenText != null && StringUtils.isNotEmpty(spokenText)) {
            // remove wake word
            String wakeWordPrefix = this.wakeWord;
            if (wakeWordPrefix != null) {
                wakeWordPrefix += " ";
                if (StringUtils.startsWithIgnoreCase(spokenText, wakeWordPrefix)) {
                    spokenText = spokenText.substring(wakeWordPrefix.length());
                }
            }

            if (lastSpokenText.equals(spokenText)) {
                updateState(CHANNEL_LAST_VOICE_COMMAND, new StringType(""));
            }
            lastSpokenText = spokenText;
            updateState(CHANNEL_LAST_VOICE_COMMAND, new StringType(spokenText));
        }
    }

    private void stopIgnoreVolumeChange() {
        this.ignoreVolumeChange = null;
    }

    public void handlePushCommand(String command, String payload) {
        this.logger.debug("Handle push command {}", command);
        switch (command) {
            case "PUSH_VOLUME_CHANGE":
                JsonCommandPayloadPushVolumeChange volumeChange = gson.fromJson(payload,
                        JsonCommandPayloadPushVolumeChange.class);
                @Nullable
                Integer volumeSetting = volumeChange.volumeSetting;
                @Nullable
                Boolean muted = volumeChange.isMuted;
                if (muted != null && muted) {
                    updateState(CHANNEL_VOLUME, new PercentType(0));
                } else if (volumeSetting != null) {
                    if (ignoreVolumeChange != null) {
                        return;
                    }
                    lastKnownVolume = volumeSetting;
                    updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                }
                break;
            case "PUSH_EQUALIZER_STATE_CHANGE":
                // Currently ignored
                break;
            default:
                AccountHandler account = this.account;
                Device device = this.device;
                if (account != null && device != null) {
                    this.disableUpdate = false;
                    updateState(account, device, null, null, null, null, null, null);
                }
        }
    }
}
