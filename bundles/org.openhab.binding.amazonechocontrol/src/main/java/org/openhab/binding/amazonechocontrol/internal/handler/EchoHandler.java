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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.eclipse.jetty.util.StringUtil.isNotBlank;
import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;
import static org.openhab.binding.amazonechocontrol.internal.dto.push.PushAudioPlayerStateTO.AudioPlayerState.*;
import static org.openhab.binding.amazonechocontrol.internal.util.Util.findIn;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlStateDescriptionProvider;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.connection.Connection;
import org.openhab.binding.amazonechocontrol.internal.dto.AscendingAlarmModelTO;
import org.openhab.binding.amazonechocontrol.internal.dto.BluetoothPairedDeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceNotificationStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DeviceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.DoNotDisturbDeviceStatusTO;
import org.openhab.binding.amazonechocontrol.internal.dto.EqualizerTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationSoundTO;
import org.openhab.binding.amazonechocontrol.internal.dto.NotificationTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateInfoTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateInfoTextTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateMainArtTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateProgressTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateProviderTO;
import org.openhab.binding.amazonechocontrol.internal.dto.PlayerStateVolumeTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushAudioPlayerStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushEqualizerStateChangeTO;
import org.openhab.binding.amazonechocontrol.internal.dto.push.PushVolumeChangeTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.PlayerSeekMediaTO;
import org.openhab.binding.amazonechocontrol.internal.dto.request.WHAVolumeLevelTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.BluetoothStateTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.CustomerHistoryRecordVoiceTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.MediaSessionTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.MusicProviderTO;
import org.openhab.binding.amazonechocontrol.internal.dto.response.PlayerStateTO;
import org.openhab.binding.amazonechocontrol.internal.types.Announcement;
import org.openhab.binding.amazonechocontrol.internal.types.Notification;
import org.openhab.core.cache.ExpiringCacheMap;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.StateDescriptionFragmentBuilder;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link EchoHandler} is responsible for the handling of the echo device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class EchoHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EchoHandler.class);
    private final Gson gson;
    private final AmazonEchoControlStateDescriptionProvider dynamicStateDescriptionProvider;

    private @Nullable DeviceTO device;
    private Set<String> capabilities = new HashSet<>();
    private @Nullable AccountHandler account = null;
    private @Nullable ScheduledFuture<?> updateStateJob;
    private @Nullable ScheduledFuture<?> updateProgressJob;
    private final Object progressLock = new Object();
    private @Nullable String wakeWord;
    private @Nullable String lastKnownBluetoothMAC;
    private long lastCustomerHistoryRecordTimestamp = System.currentTimeMillis();
    private String musicProviderId = "TUNEIN";
    private boolean isPlaying = false;
    private boolean isPaused = false;
    private int lastKnownVolume = 25;
    private int textToSpeechVolume = 0;
    private @Nullable EqualizerTO lastKnownEqualizer = null;
    private boolean disableUpdate = false;

    private @Nullable NotificationTO currentNotification;
    private @Nullable ScheduledFuture<?> currentNotificationUpdateTimer;
    private long mediaLengthMs;
    private long mediaProgressMs;
    private long mediaStartMs;

    private String currentlyPlayingQueueId = "";

    // used to block further updates when an update is already taking place
    private final AtomicBoolean waitingForUpdate = new AtomicBoolean(false);

    private final ExpiringCacheMap<String, State> stateCache = new ExpiringCacheMap<>(Duration.ofSeconds(30));

    public EchoHandler(Thing thing, Gson gson,
            AmazonEchoControlStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing);
        this.gson = gson;
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
    }

    @Override
    public void initialize() {
        if (this.getBridge() instanceof Bridge bridge && bridge.getHandler() instanceof AccountHandler handler) {
            account = handler;
            updateStatus(ThingStatus.UNKNOWN);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_UNINITIALIZED, "Bridge handler not found.");
        }

        lastCustomerHistoryRecordTimestamp = System.currentTimeMillis();
    }

    public boolean setDeviceAndUpdateThingStatus(DeviceTO device, @Nullable String wakeWord) {
        if (wakeWord != null) {
            this.wakeWord = wakeWord;
        }

        this.device = device;
        this.capabilities = device.capabilities;
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
        stopUpdateStateJob();
        stopProgressTimer();
    }

    private void stopUpdateStateJob() {
        ScheduledFuture<?> updateStateJob = this.updateStateJob;
        this.updateStateJob = null;
        if (updateStateJob != null) {
            this.disableUpdate = false;
            updateStateJob.cancel(false);
        }
    }

    private void stopProgressTimer() {
        ScheduledFuture<?> updateProgressJob = this.updateProgressJob;
        this.updateProgressJob = null;
        if (updateProgressJob != null) {
            updateProgressJob.cancel(false);
        }
    }

    private Optional<AccountHandler> getAccountHandler() {
        return Optional.ofNullable(account);
    }

    private Optional<Connection> findConnection() {
        return getAccountHandler().map(AccountHandler::getConnection);
    }

    public String getSerialNumber() {
        return Objects.requireNonNullElse((String) getConfig().get(DEVICE_PROPERTY_SERIAL_NUMBER), "");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            State state = stateCache.get(channelUID.getId());
            if (state != null) {
                // if we have cached value use that and return
                // call the original update method to prevent prolonging the same value in cache
                super.updateState(channelUID, state);
                return;
            } else {
                getAccountHandler().ifPresent(accountHandler -> {
                    if (waitingForUpdate.compareAndSet(false, true)) {
                        accountHandler.forceCheckData();
                    }
                });
            }
        }
        try {
            logger.trace("Command '{}' received for channel '{}'", command, channelUID);
            int waitForUpdate = 1000;
            boolean needBluetoothRefresh = false;

            ScheduledFuture<?> updateStateJob = this.updateStateJob;
            this.updateStateJob = null;
            if (updateStateJob != null) {
                this.disableUpdate = false;
                updateStateJob.cancel(false);
            }

            AccountHandler accountHandler = getAccountHandler().orElse(null);
            if (accountHandler == null) {
                return;
            }
            Connection connection = findConnection().orElse(null);
            if (connection == null) {
                return;
            }
            DeviceTO device = this.device;
            if (device == null) {
                return;
            }

            String channelId = channelUID.getId();
            if (channelId.equals(CHANNEL_ANNOUNCEMENT) && command instanceof StringType) {
                String commandValue = command.toFullString();
                String body = commandValue;
                String title = null;
                String speak = commandValue;
                Integer volume = null;
                if (commandValue.startsWith("{") && commandValue.endsWith("}")) {
                    try {
                        Announcement request = gson.fromJson(commandValue, Announcement.class);
                        if (request != null) {
                            speak = isNotBlank(request.speak) ? request.speak : "."; // generate beep if no text
                            Objects.requireNonNull(speak); // fix the null-checker
                            volume = request.volume;
                            title = request.title;
                            body = request.body != null ? request.body : speak;
                            Boolean sound = request.sound;
                            if (sound != null) {
                                if (!sound && !speak.startsWith("<speak>")) {
                                    speak = "<speak>" + speak + "</speak>";
                                }
                                if (sound && speak.startsWith("<speak>")) {
                                    body = "Error: The combination of sound and speak in SSML syntax is not allowed";
                                    title = "Error";
                                    speak = "<speak><lang xml:lang=\"en-UK\">Error: The combination of sound and speak in <prosody rate=\"x-slow\"><say-as interpret-as=\"characters\">SSML</say-as></prosody> syntax is not allowed</lang></speak>";
                                }
                            }
                            if ("<speak> </speak>".equals(speak)) {
                                volume = -1; // Do not change volume
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        body = "Invalid Json." + e.getLocalizedMessage();
                        title = "Error";
                        speak = "<speak><lang xml:lang=\"en-US\">" + body + "</lang></speak>";
                        body = e.getLocalizedMessage();
                    }
                }
                Integer vol;
                if (volume == null && textToSpeechVolume != 0) {
                    vol = textToSpeechVolume;
                } else if (volume != null && volume < 0) {
                    vol = null;// the meaning of negative values is 'do not use'. The api requires null in this case.
                } else {
                    vol = volume;
                }
                String finalSpeak = speak;
                String finalBody = Objects.requireNonNullElse(body, "");
                String finalTitle = title;
                connection.announcement(device, finalSpeak, finalBody, finalTitle, vol, lastKnownVolume);
            }

            // Player commands
            if (channelId.equals(CHANNEL_PLAYER)) {
                if (command == PlayPauseType.PAUSE || command == OnOffType.OFF) {
                    connection.command(device, Map.of("type", "PauseCommand"));
                } else if (command == PlayPauseType.PLAY || command == OnOffType.ON) {
                    if (isPaused) {
                        connection.command(device, Map.of("type", "PlayCommand"));
                    } else {
                        connection.playMusicVoiceCommand(device, this.musicProviderId, "!");
                        waitForUpdate = 3000;
                    }
                } else if (command == NextPreviousType.NEXT) {
                    connection.command(device, Map.of("type", "NextCommand"));
                } else if (command == NextPreviousType.PREVIOUS) {
                    connection.command(device, Map.of("type", "PreviousCommand"));
                } else if (command == RewindFastforwardType.FASTFORWARD) {
                    connection.command(device, Map.of("type", "ForwardCommand"));
                } else if (command == RewindFastforwardType.REWIND) {
                    connection.command(device, Map.of("type", "RewindCommand"));
                }
            }
            // Notification commands
            if (channelId.equals(CHANNEL_NOTIFICATION_VOLUME)) {
                if (command instanceof PercentType percent) {
                    connection.setNotificationVolume(device, percent.intValue());
                    waitForUpdate = -1;
                    accountHandler.forceCheckData();
                }
            }
            if (channelId.equals(CHANNEL_ASCENDING_ALARM)) {
                boolean ascendingAlarm = command == OnOffType.ON;
                connection.setAscendingAlarm(device, ascendingAlarm);
                waitForUpdate = -1;
                accountHandler.forceCheckData();
            }
            // Do Not Disturb command
            if (channelId.equals(CHANNEL_DO_NOT_DISTURB) && command instanceof OnOffType) {
                boolean newDnd = command == OnOffType.ON;
                connection.setDoNotDisturb(device, newDnd);
                waitForUpdate = -1;
                accountHandler.forceCheckData();
            }
            // Media progress commands
            Long mediaPosition = null;
            if (channelId.equals(CHANNEL_MEDIA_PROGRESS)) {
                if (command instanceof PercentType percent) {
                    mediaPosition = Math.round((mediaLengthMs / 1000d) * (percent.intValue() / 100d));
                }
            }
            if (channelId.equals(CHANNEL_MEDIA_PROGRESS_TIME)) {
                if (command instanceof DecimalType decimal) {
                    mediaPosition = decimal.longValue();
                }
                if (command instanceof QuantityType<?> quantity) {
                    QuantityType<?> seconds = quantity.toUnit(Units.SECOND);
                    if (seconds != null) {
                        mediaPosition = seconds.longValue();
                    }
                }
            }
            if (mediaPosition != null) {
                waitForUpdate = -1;
                synchronized (progressLock) {
                    PlayerSeekMediaTO seekCommand = new PlayerSeekMediaTO();
                    seekCommand.mediaPosition = mediaPosition;
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
                if (command instanceof PercentType value) {
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
                    if ("WHA".equals(device.deviceFamily)) {
                        WHAVolumeLevelTO volumeCommand = new WHAVolumeLevelTO();
                        volumeCommand.volumeLevel = volume;
                        connection.command(device, volumeCommand);
                    } else {
                        connection.setVolume(device, volume);
                    }
                    lastKnownVolume = volume;
                    updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                    waitForUpdate = -1;
                }
            }
            // equalizer commands
            if (channelId.equals(CHANNEL_EQUALIZER_BASS) || channelId.equals(CHANNEL_EQUALIZER_MIDRANGE)
                    || channelId.equals(CHANNEL_EQUALIZER_TREBLE)) {
                if (handleEqualizerCommands(channelId, command, connection, device)) {
                    waitForUpdate = -1;
                }
            }

            // shuffle command
            if (channelId.equals(CHANNEL_SHUFFLE)) {
                if (command instanceof OnOffType onOff) {
                    connection.command(device, Map.of("type", "ShuffleCommand", "shuffle", onOff == OnOffType.ON));
                }
            }

            // play music command
            if (channelId.equals(CHANNEL_MUSIC_PROVIDER_ID) && command instanceof StringType) {
                waitForUpdate = 0;
                String musicProviderId = command.toFullString();
                if (!musicProviderId.equals(this.musicProviderId)) {
                    this.musicProviderId = musicProviderId;
                    if (this.isPlaying) {
                        connection.playMusicVoiceCommand(device, this.musicProviderId, "!");
                        waitForUpdate = 3000;
                    }
                }
            }
            if (channelId.equals(CHANNEL_PLAY_MUSIC_VOICE_COMMAND) && command instanceof StringType) {
                String voiceCommand = command.toFullString();
                if (!this.musicProviderId.isEmpty()) {
                    connection.playMusicVoiceCommand(device, this.musicProviderId, voiceCommand);
                    waitForUpdate = 3000;
                }
            }

            // bluetooth commands
            if (channelId.equals(CHANNEL_BLUETOOTH_MAC)) {
                needBluetoothRefresh = true;
                if (command instanceof StringType) {
                    String address = command.toFullString();
                    if (!address.isEmpty()) {
                        waitForUpdate = 4000;
                    }
                    connection.bluetooth(device, address);
                }
            }
            if (channelId.equals(CHANNEL_BLUETOOTH)) {
                needBluetoothRefresh = true;
                String lastKnownBluetoothMAC = this.lastKnownBluetoothMAC;
                if (command == OnOffType.ON) {
                    waitForUpdate = 4000;
                    if (lastKnownBluetoothMAC != null && !lastKnownBluetoothMAC.isEmpty()) {
                        connection.bluetooth(device, lastKnownBluetoothMAC);
                    }
                } else if (command == OnOffType.OFF) {
                    connection.bluetooth(device, null);
                }
            }
            if (channelId.equals(CHANNEL_BLUETOOTH_DEVICE_NAME)) {
                needBluetoothRefresh = true;
            }

            // notification
            if (channelId.equals(CHANNEL_REMIND) && command instanceof StringType) {
                stopCurrentNotification();
                String reminder = command.toFullString();
                if (!reminder.isBlank()) {
                    waitForUpdate = 3000;
                    currentNotification = connection.createNotification(device, "Reminder", reminder, null);
                    currentNotificationUpdateTimer = scheduler
                            .scheduleWithFixedDelay(this::updateNotificationTimerState, 1, 1, TimeUnit.SECONDS);
                }
            }
            if (channelId.equals(CHANNEL_PLAY_ALARM_SOUND) && command instanceof StringType) {
                stopCurrentNotification();
                String alarmSound = command.toFullString();
                if (!alarmSound.isEmpty()) {
                    waitForUpdate = 3000;
                    String[] parts = alarmSound.split(":", 2);
                    NotificationSoundTO sound = new NotificationSoundTO();
                    if (parts.length == 2) {
                        sound.providerId = parts[0];
                        sound.id = parts[1];
                    } else {
                        sound.providerId = "ECHO";
                        sound.id = alarmSound;
                    }
                    currentNotification = connection.createNotification(device, "Alarm", null, sound);
                    currentNotificationUpdateTimer = scheduler
                            .scheduleWithFixedDelay(this::updateNotificationTimerState, 1, 1, TimeUnit.SECONDS);
                }
            }

            // routine commands
            if (channelId.equals(CHANNEL_TEXT_TO_SPEECH) && command instanceof StringType) {
                String text = command.toFullString();
                if (!text.isEmpty()) {
                    waitForUpdate = 1000;
                    startTextToSpeech(connection, device, text);
                }
            }
            if (channelId.equals(CHANNEL_TEXT_TO_SPEECH_VOLUME)) {
                if (command instanceof PercentType percent) {
                    textToSpeechVolume = percent.intValue();
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
            if (channelId.equals(CHANNEL_TEXT_COMMAND) && command instanceof StringType) {
                String text = command.toFullString();
                if (!text.isEmpty()) {
                    waitForUpdate = 1000;
                    startTextCommand(connection, device, text);
                }
            }
            if (channelId.equals(CHANNEL_LAST_VOICE_COMMAND) && command instanceof StringType) {
                String text = command.toFullString();
                if (!text.isEmpty()) {
                    waitForUpdate = -1;
                    startTextToSpeech(connection, device, text);
                }
            }
            if (channelId.equals(CHANNEL_START_COMMAND) && command instanceof StringType) {
                String commandText = command.toFullString();
                if (commandText.startsWith(FLASH_BRIEFING_COMMAND_PREFIX)) {
                    // Handle custom flashbriefings commands
                    String flashBriefingId = commandText.substring(FLASH_BRIEFING_COMMAND_PREFIX.length());
                    for (FlashBriefingProfileHandler flashBriefingHandler : accountHandler
                            .getFlashBriefingProfileHandlers()) {
                        ThingUID flashBriefingUid = flashBriefingHandler.getThing().getUID();
                        if (flashBriefingId.equals(flashBriefingHandler.getThing().getUID().getId())) {
                            flashBriefingHandler.handleCommand(new ChannelUID(flashBriefingUid, CHANNEL_PLAY_ON_DEVICE),
                                    new StringType(device.serialNumber));
                            break;
                        }
                    }
                } else if (!commandText.isBlank()) {
                    // Handle standard commands
                    if (!commandText.startsWith("Alexa.")) {
                        commandText = "Alexa." + commandText + ".Play";
                    }
                    waitForUpdate = 1000;
                    connection.executeSequenceCommand(device, commandText, Map.of());
                }
            }
            if (channelId.equals(CHANNEL_START_ROUTINE) && command instanceof StringType) {
                String utterance = command.toFullString();
                if (!utterance.isEmpty()) {
                    waitForUpdate = 1000;
                    connection.startRoutine(device, utterance);
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
                BluetoothStateTO state = null;
                if (bluetoothRefresh) {
                    List<BluetoothStateTO> states = connection.getBluetoothConnectionStates();
                    state = findIn(states, a -> a.deviceSerialNumber, device.serialNumber).orElse(null);
                }
                updateState(device, state, null, null, null, null);
            };
            if (waitForUpdate == 0) {
                doRefresh.run();
            } else {
                this.updateStateJob = scheduler.schedule(doRefresh, waitForUpdate, TimeUnit.MILLISECONDS);
            }
        } catch (ConnectionException e) {
            logger.warn("Failed to handle command '{}' to '{}': {}", command, channelUID, e.getMessage(), e);
        } catch (RuntimeException e) {
            logger.warn("RuntimeException in handle command for channel '{}': {}", channelUID, e.getMessage(), e);
        }
    }

    private boolean handleEqualizerCommands(String channelId, Command command, Connection connection, DeviceTO device) {
        if (command instanceof DecimalType decimal) {
            if (lastKnownEqualizer == null) {
                updateEqualizerState();
            }
            EqualizerTO oldEqualizer = lastKnownEqualizer;
            if (oldEqualizer != null) {
                EqualizerTO newEqualizer = new EqualizerTO();
                newEqualizer.bass = channelId.equals(CHANNEL_EQUALIZER_BASS) ? decimal.intValue() : oldEqualizer.bass;
                newEqualizer.mid = channelId.equals(CHANNEL_EQUALIZER_MIDRANGE) ? decimal.intValue() : oldEqualizer.mid;
                newEqualizer.treble = channelId.equals(CHANNEL_EQUALIZER_TREBLE) ? decimal.intValue()
                        : oldEqualizer.treble;
                return connection.setEqualizer(device, newEqualizer);
            }
        }
        return false;
    }

    private void startTextToSpeech(Connection connection, DeviceTO device, String text) {
        Integer volume = textToSpeechVolume != 0 ? textToSpeechVolume : null;
        connection.textToSpeech(device, text, volume, lastKnownVolume);
    }

    private void startTextCommand(Connection connection, DeviceTO device, String text) {
        Integer volume = textToSpeechVolume != 0 ? textToSpeechVolume : null;
        connection.textCommand(device, text, volume, lastKnownVolume);
    }

    private void stopCurrentNotification() {
        ScheduledFuture<?> currentNotificationUpdateTimer = this.currentNotificationUpdateTimer;
        if (currentNotificationUpdateTimer != null) {
            this.currentNotificationUpdateTimer = null;
            // do not interrupt the current set, otherwise the DELETE request will be aborted
            currentNotificationUpdateTimer.cancel(false);
        }
        NotificationTO currentNotification = this.currentNotification;
        if (currentNotification != null) {
            this.currentNotification = null;
            findConnection().ifPresent(connection -> connection.deleteNotification(currentNotification.id));
        }
    }

    private void updateNotificationTimerState() {
        boolean stopCurrentNotification = true;
        NotificationTO currentNotification = this.currentNotification;
        Connection currentConnection = this.findConnection().orElse(null);
        try {
            if (currentNotification != null && currentConnection != null) {
                String status = currentConnection.getNotification(currentNotification.id).status;
                if ("ON".equals(status)) {
                    stopCurrentNotification = false;
                }
            }
        } catch (ConnectionException e) {
            logger.warn("Failed to update notification state: {}", e.getMessage(), e);
        }
        if (stopCurrentNotification) {
            stopCurrentNotification();
        }
    }

    private void createMusicProviderStateDescription(List<MusicProviderTO> musicProviders) {
        List<StateOption> options = new ArrayList<>();
        for (MusicProviderTO musicProvider : musicProviders) {
            if (!musicProvider.supportedProperties.contains("Alexa.Music.PlaySearchPhrase")) {
                continue;
            }
            String providerId = musicProvider.id;
            String displayName = musicProvider.displayName;
            if (isNotBlank(providerId) && "AVAILABLE".equals(musicProvider.availability)) {
                options.add(new StateOption(providerId, isNotBlank(displayName) ? displayName : providerId));
            }
        }
        ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_MUSIC_PROVIDER_ID);
        StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withOptions(options).build()
                .toStateDescription();

        if (stateDescription != null) {
            dynamicStateDescriptionProvider.setDescription(channelUID, stateDescription);
        }
    }

    private void createBluetoothMACStateDescription(BluetoothStateTO bluetoothState) {
        List<StateOption> options = new ArrayList<>();
        options.add(new StateOption("", ""));
        for (BluetoothPairedDeviceTO device : bluetoothState.pairedDeviceList) {
            final String value = device.address;
            if (value != null && device.friendlyName != null) {
                options.add(new StateOption(value, device.friendlyName));
            }
        }
        ChannelUID channelUID = new ChannelUID(thing.getUID(), CHANNEL_BLUETOOTH_MAC);
        StateDescription stateDescription = StateDescriptionFragmentBuilder.create().withOptions(options).build()
                .toStateDescription();

        if (stateDescription != null) {
            dynamicStateDescriptionProvider.setDescription(channelUID, stateDescription);
        }
    }

    private void updateMediaPlayerState(PlayerStateInfoTO playerInfo, boolean sequenceNodeRunning, int timeFactor) {
        PlayerStateProviderTO provider = playerInfo.provider;
        PlayerStateInfoTextTO infoText = playerInfo.infoText != null ? playerInfo.infoText : playerInfo.miniInfoText;
        PlayerStateMainArtTO mainArt = playerInfo.mainArt;
        String musicProviderId = null;
        PlayerStateProgressTO progress = playerInfo.progress;
        if (provider != null) {
            musicProviderId = provider.providerName;
            // Map the music provider id to the one used for starting music with voice command
            if (musicProviderId != null) {
                musicProviderId = musicProviderId.toUpperCase();

                if ("AMAZON MUSIC".equals(musicProviderId) || "CLOUD_PLAYER".equals(musicProviderId)) {
                    musicProviderId = "AMAZON_MUSIC";
                }
                if (musicProviderId.startsWith("TUNEIN")) {
                    musicProviderId = "TUNEIN";
                }
                if (musicProviderId.startsWith("IHEARTRADIO")) {
                    musicProviderId = "I_HEART_RADIO";
                }
                if (musicProviderId.startsWith("APPLE") && musicProviderId.contains("MUSIC")) {
                    musicProviderId = "APPLE_MUSIC";
                }
            }
        }

        // check playing
        isPlaying = "PLAYING".equals(playerInfo.state);
        isPaused = "PAUSED".equals(playerInfo.state);

        if (isPlaying) {
            currentlyPlayingQueueId = playerInfo.queueId;
        }

        synchronized (progressLock) {
            if (isPlaying) {
                if (progress != null) {
                    mediaProgressMs = progress.mediaProgress * timeFactor;
                    mediaLengthMs = progress.mediaLength * timeFactor;
                    mediaStartMs = System.currentTimeMillis() - mediaProgressMs;
                }
                if (updateProgressJob == null) {
                    updateProgressJob = scheduler.scheduleWithFixedDelay(() -> updateMediaProgress(false), 1000, 1000,
                            TimeUnit.MILLISECONDS);
                }
            } else {
                stopProgressTimer();
                mediaProgressMs = 0;
                mediaStartMs = 0;
                mediaLengthMs = 0;
            }
            updateMediaProgress(true);
        }

        // handle music provider id
        if (musicProviderId != null && isPlaying) {
            this.musicProviderId = musicProviderId;
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

        // handle provider
        String providerDisplayName = "";
        if (provider != null) {
            providerDisplayName = Objects.requireNonNullElse(provider.providerDisplayName, providerDisplayName);
            String providerName = provider.providerName;
            if (isNotBlank(providerName) && providerDisplayName.isEmpty()) {
                providerDisplayName = providerName;
            }
        }

        // handle volume
        if (!sequenceNodeRunning) {
            Integer volume = null;
            PlayerStateVolumeTO volumeInfo = playerInfo.volume;
            if (volumeInfo != null) {
                volume = volumeInfo.volume;
            }
            if (volume != null && volume > 0) {
                lastKnownVolume = volume;
                updateState(CHANNEL_VOLUME, new PercentType(volume));
            }
        }

        // Update states
        updateState(CHANNEL_MUSIC_PROVIDER_ID, new StringType(musicProviderId));
        updateState(CHANNEL_PROVIDER_DISPLAY_NAME, new StringType(providerDisplayName));
        updateState(CHANNEL_PLAYER, isPlaying ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        updateState(CHANNEL_IMAGE_URL, new StringType(imageUrl));
        updateState(CHANNEL_TITLE, new StringType(title));
        updateState(CHANNEL_SUBTITLE1, new StringType(subTitle1));
        updateState(CHANNEL_SUBTITLE2, new StringType(subTitle2));
    }

    public void updateState(DeviceTO device, @Nullable BluetoothStateTO bluetoothState,
            @Nullable DeviceNotificationStateTO deviceNotificationState,
            @Nullable AscendingAlarmModelTO ascendingAlarmModel,
            @Nullable DoNotDisturbDeviceStatusTO doNotDisturbDeviceStatus,
            @Nullable List<MusicProviderTO> musicProviders) {
        try {
            waitingForUpdate.set(false);
            logger.debug("Handle updateState {}", this.getThing().getUID());

            if (deviceNotificationState != null) {
                int notificationVolumeLevel = deviceNotificationState.volumeLevel;
                updateState(CHANNEL_NOTIFICATION_VOLUME, new PercentType(notificationVolumeLevel));
            }

            if (ascendingAlarmModel != null) {
                boolean ascendingAlarm = ascendingAlarmModel.ascendingAlarmEnabled;
                updateState(CHANNEL_ASCENDING_ALARM, OnOffType.from(ascendingAlarm));
            }

            if (doNotDisturbDeviceStatus != null) {
                boolean doNotDisturb = doNotDisturbDeviceStatus.enabled;
                updateState(CHANNEL_DO_NOT_DISTURB, OnOffType.from(doNotDisturb));
            }

            if (musicProviders != null) {
                createMusicProviderStateDescription(musicProviders);
            }

            if (!setDeviceAndUpdateThingStatus(device, null)) {
                logger.debug("Handle updateState {} aborted: Not online", this.getThing().getUID());
                return;
            }

            if (disableUpdate) {
                logger.debug("Handle updateState {} aborted: Disabled", this.getThing().getUID());
                return;
            }

            Connection connection = findConnection().orElse(null);
            if (connection == null) {
                return;
            }

            if (lastKnownEqualizer == null) {
                updateEqualizerState();
            }

            try {
                PlayerStateTO playerState = connection.getPlayerState(device);
                updateMediaPlayerState(playerState.playerInfo, connection.isSequenceNodeQueueRunning(), 1000);
            } catch (ConnectionException e) {
                logger.debug("Failed to update player state: {}", e.getMessage(), e);
            }

            // handle bluetooth
            if (bluetoothState != null) {
                String bluetoothMAC = "";
                String bluetoothDeviceName = "";
                boolean bluetoothIsConnected = false;
                for (BluetoothPairedDeviceTO paired : bluetoothState.pairedDeviceList) {
                    String pairedAddress = paired.address;
                    if (paired.connected && pairedAddress != null) {
                        bluetoothIsConnected = true;
                        bluetoothMAC = pairedAddress;
                        lastKnownBluetoothMAC = pairedAddress;
                        bluetoothDeviceName = paired.friendlyName;
                        if (bluetoothDeviceName == null || bluetoothDeviceName.isEmpty()) {
                            bluetoothDeviceName = pairedAddress;
                        }
                        break;
                    }
                }
                createBluetoothMACStateDescription(bluetoothState);
                updateState(CHANNEL_BLUETOOTH, OnOffType.from(bluetoothIsConnected));
                updateState(CHANNEL_BLUETOOTH_MAC, new StringType(bluetoothMAC));
                updateState(CHANNEL_BLUETOOTH_DEVICE_NAME, new StringType(bluetoothDeviceName));
            }
        } catch (RuntimeException e) {
            this.logger.debug("Handle updateState {} failed: {}", this.getThing().getUID(), e.getMessage(), e);
            disableUpdate = false;
        }
    }

    private void updateEqualizerState() {
        if (!this.capabilities.contains("SOUND_SETTINGS")) {
            return;
        }

        DeviceTO device = this.device;
        if (device == null) {
            return;

        }

        findConnection().flatMap(connection -> connection.getEqualizer(device)).ifPresent(equalizer -> {
            updateState(CHANNEL_EQUALIZER_BASS, new DecimalType(equalizer.bass));
            updateState(CHANNEL_EQUALIZER_MIDRANGE, new DecimalType(equalizer.mid));
            updateState(CHANNEL_EQUALIZER_TREBLE, new DecimalType(equalizer.treble));
            this.lastKnownEqualizer = equalizer;
        });
    }

    private void updateMediaProgress(boolean updateMediaLength) {
        synchronized (progressLock) {
            if (isPlaying && mediaStartMs > 0) {
                long currentPlayTimeMs = System.currentTimeMillis() - mediaStartMs;
                if (mediaLengthMs > 0) {
                    int progressPercent = (int) Math.min(100,
                            Math.round((double) currentPlayTimeMs / (double) mediaLengthMs * 100));
                    updateState(CHANNEL_MEDIA_PROGRESS, new PercentType(progressPercent));
                } else {
                    updateState(CHANNEL_MEDIA_PROGRESS, UnDefType.UNDEF);
                }
                updateState(CHANNEL_MEDIA_PROGRESS_TIME, new QuantityType<>(currentPlayTimeMs / 1000, Units.SECOND));
                if (updateMediaLength) {
                    updateState(CHANNEL_MEDIA_LENGTH, new QuantityType<>(mediaLengthMs / 1000, Units.SECOND));
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

    public synchronized void handlePushActivity(CustomerHistoryRecordTO customerHistoryRecord) {
        long recordTimestamp = customerHistoryRecord.timestamp;
        if (recordTimestamp <= lastCustomerHistoryRecordTimestamp) {
            return;
        }
        lastCustomerHistoryRecordTimestamp = recordTimestamp;
        List<CustomerHistoryRecordVoiceTO> voiceHistoryRecordItems = customerHistoryRecord.voiceHistoryRecordItems;
        for (CustomerHistoryRecordVoiceTO voiceHistoryRecordItem : voiceHistoryRecordItems) {
            String recordItemType = voiceHistoryRecordItem.recordItemType;
            if ("CUSTOMER_TRANSCRIPT".equals(recordItemType) || "ASR_REPLACEMENT_TEXT".equals(recordItemType)) {
                String customerTranscript = voiceHistoryRecordItem.transcriptText;
                if (!customerTranscript.isEmpty()) {
                    // REMOVE WAKE WORD
                    String wakeWordPrefix = this.wakeWord;
                    if (wakeWordPrefix != null
                            && customerTranscript.toLowerCase().startsWith(wakeWordPrefix.toLowerCase())) {
                        customerTranscript = customerTranscript.substring(wakeWordPrefix.length()).trim();
                        // STOP IF WAKE WORD ONLY
                        if (customerTranscript.isEmpty()) {
                            return;
                        }
                    }
                    updateState(CHANNEL_LAST_VOICE_COMMAND, new StringType(customerTranscript));
                }
            } else if ("ALEXA_RESPONSE".equals(recordItemType) || "TTS_REPLACEMENT_TEXT".equals(recordItemType)) {
                String alexaResponse = voiceHistoryRecordItem.transcriptText;
                if (alexaResponse != null && !alexaResponse.isEmpty()) {
                    updateState(CHANNEL_LAST_SPOKEN_TEXT, new StringType(alexaResponse));
                }
            }
        }
    }

    public void handleNowPlayingUpdated(PlayerStateInfoTO playerState) {
        findConnection().ifPresent(connection -> {
            if (currentlyPlayingQueueId.equals(playerState.queueId)) {
                // update when the queueId is the same
                updateMediaPlayerState(playerState, connection.isSequenceNodeQueueRunning(), 1);
            }
        });
    }

    public void updateMediaSessions() {
        findConnection().ifPresent(connection -> {
            DeviceTO device = this.device;
            if (device == null || !isPlaying) {
                return;
            }
            List<MediaSessionTO> mediaSessions = connection.getMediaSessions(device);
            for (MediaSessionTO mediaSession : mediaSessions) {
                if (findIn(mediaSession.endpointList, e -> e.id.deviceSerialNumber, device.serialNumber).isPresent()) {
                    updateMediaPlayerState(mediaSession.nowPlayingData, connection.isSequenceNodeQueueRunning(), 1000);
                }
            }
        });
    }

    private void refreshAudioPlayerState() {
        findConnection().ifPresent(connection -> {
            try {
                DeviceTO device = this.device;
                if (device != null) {
                    PlayerStateTO playerState = connection.getPlayerState(device);
                    updateMediaPlayerState(playerState.playerInfo, connection.isSequenceNodeQueueRunning(), 1000);
                }
            } catch (ConnectionException e) {
                logger.debug("Failed to refresh audio player state: {}", e.getMessage(), e);
            }
        });
    }

    public void handlePushCommand(String command, String payload) {
        this.logger.debug("Handle push command {}", command);
        Connection connection = this.findConnection().orElse(null);

        switch (command) {
            case "PUSH_VOLUME_CHANGE":
                PushVolumeChangeTO volumeChange = Objects
                        .requireNonNull(gson.fromJson(payload, PushVolumeChangeTO.class));

                if (volumeChange.isMuted) {
                    updateState(CHANNEL_VOLUME, new PercentType(0));
                }
                if (connection != null && !connection.isSequenceNodeQueueRunning()) {
                    lastKnownVolume = volumeChange.volumeSetting;
                    updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                }
                break;
            case "PUSH_EQUALIZER_STATE_CHANGE":
                PushEqualizerStateChangeTO equalizerStateChange = Objects
                        .requireNonNull(gson.fromJson(payload, PushEqualizerStateChangeTO.class));
                updateState(CHANNEL_EQUALIZER_BASS, new DecimalType(equalizerStateChange.bass));
                updateState(CHANNEL_EQUALIZER_MIDRANGE, new DecimalType(equalizerStateChange.midrange));
                updateState(CHANNEL_EQUALIZER_TREBLE, new DecimalType(equalizerStateChange.treble));
                break;
            case "PUSH_AUDIO_PLAYER_STATE":
                PushAudioPlayerStateTO audioPlayerState = Objects
                        .requireNonNull(gson.fromJson(payload, PushAudioPlayerStateTO.class));
                // FINISHED is emitted when the track finished, but the player continues with the next track
                // PLAYING is emitted when a track starts (either first nextAlarmTime or next track)
                // INTERRUPTED is emitted when the player finally stops
                if (audioPlayerState.audioPlayerState == INTERRUPTED
                        || (!isPlaying && audioPlayerState.audioPlayerState == PLAYING)
                        || ("SPOTIFY".equals(musicProviderId))) {
                    // we only need to update the state when the player stops or starts, not on track changes
                    // except for spotify
                    refreshAudioPlayerState();
                }
                break;
            case "PUSH_MEDIA_QUEUE_CHANGE":
                // update the media state with a request to get the new queue id
                refreshAudioPlayerState();
                break;
            default:
                DeviceTO device = this.device;
                if (device != null) {
                    this.disableUpdate = false;
                    updateState(device, null, null, null, null, null);
                }
        }
    }

    public void updateNotifications(List<Notification> notifications) {
        DeviceTO device = this.device;
        if (device == null) {
            return;
        }

        ZonedDateTime nextReminder = null;
        ZonedDateTime nextAlarm = null;
        ZonedDateTime nextMusicAlarm = null;
        ZonedDateTime nextTimer = null;
        for (Notification notification : notifications) {
            if (Objects.equals(notification.deviceSerial(), device.serialNumber)) {
                switch (notification.type()) {
                    case "Reminder":
                        if (nextReminder == null || notification.nextAlarmTime().isBefore(nextReminder)) {
                            nextReminder = notification.nextAlarmTime();
                        }
                        break;
                    case "Timer":
                        if (nextTimer == null || notification.nextAlarmTime().isBefore(nextTimer)) {
                            nextTimer = notification.nextAlarmTime();
                        }
                        break;
                    case "Alarm":
                        if (nextAlarm == null || notification.nextAlarmTime().isBefore(nextAlarm)) {
                            nextAlarm = notification.nextAlarmTime();
                        }
                        break;
                    case "MusicAlarm":
                        if (nextMusicAlarm == null || notification.nextAlarmTime().isBefore(nextMusicAlarm)) {
                            nextMusicAlarm = notification.nextAlarmTime();
                        }
                        break;
                    default:
                }
            }
        }

        updateState(CHANNEL_NEXT_REMINDER, nextReminder == null ? UnDefType.UNDEF : new DateTimeType(nextReminder));
        updateState(CHANNEL_NEXT_ALARM, nextAlarm == null ? UnDefType.UNDEF : new DateTimeType(nextAlarm));
        updateState(CHANNEL_NEXT_MUSIC_ALARM,
                nextMusicAlarm == null ? UnDefType.UNDEF : new DateTimeType(nextMusicAlarm));
        updateState(CHANNEL_NEXT_TIMER, nextTimer == null ? UnDefType.UNDEF : new DateTimeType(nextTimer));
    }

    @Override
    protected void updateState(String channelId, State state) {
        stateCache.put(channelId, () -> state);
        super.updateState(channelId, state);
    }
}
