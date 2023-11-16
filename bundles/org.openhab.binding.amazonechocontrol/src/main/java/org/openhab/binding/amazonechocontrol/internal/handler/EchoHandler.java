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
package org.openhab.binding.amazonechocontrol.internal.handler;

import static org.openhab.binding.amazonechocontrol.internal.AmazonEchoControlBindingConstants.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.amazonechocontrol.internal.Connection;
import org.openhab.binding.amazonechocontrol.internal.ConnectionException;
import org.openhab.binding.amazonechocontrol.internal.HttpException;
import org.openhab.binding.amazonechocontrol.internal.channelhandler.ChannelHandler;
import org.openhab.binding.amazonechocontrol.internal.channelhandler.ChannelHandlerAnnouncement;
import org.openhab.binding.amazonechocontrol.internal.channelhandler.IEchoThingHandler;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonActivities.Activity.Description;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonAscendingAlarm.AscendingAlarmModel;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.BluetoothState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonBluetoothStates.PairedDevice;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushNotificationChange;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonCommandPayloadPushVolumeChange;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDeviceNotificationState.DeviceNotificationState;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonDevices.Device;
import org.openhab.binding.amazonechocontrol.internal.jsons.JsonEqualizer;
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
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * The {@link EchoHandler} is responsible for the handling of the echo device
 *
 * @author Michael Geramb - Initial contribution
 */
@NonNullByDefault
public class EchoHandler extends BaseThingHandler implements IEchoThingHandler {
    private final Logger logger = LoggerFactory.getLogger(EchoHandler.class);
    private Gson gson;
    private @Nullable Device device;
    private Set<String> capabilities = new HashSet<>();
    private @Nullable AccountHandler account;
    private @Nullable ScheduledFuture<?> updateStateJob;
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
    private @Nullable JsonEqualizer lastKnownEqualizer = null;
    private @Nullable BluetoothState bluetoothState;
    private boolean disableUpdate = false;
    private boolean updateRemind = true;
    private boolean updateTextToSpeech = true;
    private boolean updateTextCommand = true;
    private boolean updateAlarm = true;
    private boolean updateRoutine = true;
    private boolean updatePlayMusicVoiceCommand = true;
    private boolean updateStartCommand = true;
    private @Nullable Integer notificationVolumeLevel;
    private @Nullable Boolean ascendingAlarm;
    private @Nullable JsonPlaylists playLists;
    private List<JsonNotificationSound> alarmSounds = List.of();
    private List<JsonMusicProvider> musicProviders = List.of();
    private List<ChannelHandler> channelHandlers = new ArrayList<>();

    private @Nullable JsonNotificationResponse currentNotification;
    private @Nullable ScheduledFuture<?> currentNotifcationUpdateTimer;
    long mediaLengthMs;
    long mediaProgressMs;
    long mediaStartMs;
    String lastSpokenText = "";

    public EchoHandler(Thing thing, Gson gson) {
        super(thing);
        this.gson = gson;
        channelHandlers.add(new ChannelHandlerAnnouncement(this, this.gson));
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
        this.capabilities = device.getCapabilities();
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

    public List<JsonNotificationSound> findAlarmSounds() {
        return this.alarmSounds;
    }

    public List<JsonMusicProvider> findMusicProviders() {
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
            logger.trace("Command '{}' received for channel '{}'", command, channelUID);
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

            String channelId = channelUID.getId();
            for (ChannelHandler channelHandler : channelHandlers) {
                if (channelHandler.tryHandleCommand(device, connection, channelId, command)) {
                    return;
                }
            }

            // Player commands
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
                if (command instanceof PercentType percentCommand) {
                    int volume = percentCommand.intValue();
                    connection.notificationVolume(device, volume);
                    this.notificationVolumeLevel = volume;
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
                if (command instanceof PercentType percentCommand) {
                    int percent = percentCommand.intValue();
                    mediaPosition = Math.round((mediaLengthMs / 1000d) * (percent / 100d));
                }
            }
            if (channelId.equals(CHANNEL_MEDIA_PROGRESS_TIME)) {
                if (command instanceof DecimalType decimalCommand) {
                    mediaPosition = decimalCommand.longValue();
                }
                if (command instanceof QuantityType<?> quantityCommand) {
                    @Nullable
                    QuantityType<?> seconds = quantityCommand.toUnit(Units.SECOND);
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
                if (command instanceof PercentType percentCommand) {
                    volume = percentCommand.intValue();
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
                        connection.command(device, "{\"type\":\"VolumeLevelCommand\",\"volumeLevel\":" + volume
                                + ",\"contentFocusClientId\":\"Default\"}");
                    } else {
                        connection.volume(device, volume);
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
                if (command instanceof OnOffType value) {
                    connection.command(device, "{\"type\":\"ShuffleCommand\",\"shuffle\":\""
                            + (value == OnOffType.ON ? "true" : "false") + "\"}");
                }
            }

            // play music command
            if (channelId.equals(CHANNEL_MUSIC_PROVIDER_ID)) {
                if (command instanceof StringType) {
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
            }
            if (channelId.equals(CHANNEL_PLAY_MUSIC_VOICE_COMMAND)) {
                if (command instanceof StringType) {
                    String voiceCommand = command.toFullString();
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
                if (command instanceof StringType stringCommand) {
                    String address = stringCommand.toFullString();
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
                    if (state != null && (bluetoothId == null || bluetoothId.isEmpty())) {
                        for (PairedDevice paired : state.getPairedDeviceList()) {
                            String pairedAddress = paired.address;
                            if (pairedAddress != null && !pairedAddress.isEmpty()) {
                                lastKnownBluetoothMAC = pairedAddress;
                                break;
                            }
                        }
                    }
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
            // amazon music commands
            if (channelId.equals(CHANNEL_AMAZON_MUSIC_TRACK_ID)) {
                if (command instanceof StringType) {
                    String trackId = command.toFullString();
                    if (!trackId.isEmpty()) {
                        waitForUpdate = 3000;
                    }
                    connection.playAmazonMusicTrack(device, trackId);
                }
            }
            if (channelId.equals(CHANNEL_AMAZON_MUSIC_PLAY_LIST_ID)) {
                if (command instanceof StringType) {
                    String playListId = command.toFullString();
                    if (!playListId.isEmpty()) {
                        waitForUpdate = 3000;
                    }
                    connection.playAmazonMusicPlayList(device, playListId);
                }
            }
            if (channelId.equals(CHANNEL_AMAZON_MUSIC)) {
                if (command == OnOffType.ON) {
                    String lastKnownAmazonMusicId = this.lastKnownAmazonMusicId;
                    if (lastKnownAmazonMusicId != null && !lastKnownAmazonMusicId.isEmpty()) {
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
                    String stationId = command.toFullString();
                    if (!stationId.isEmpty()) {
                        waitForUpdate = 3000;
                    }
                    connection.playRadio(device, stationId);
                }
            }
            if (channelId.equals(CHANNEL_RADIO)) {
                if (command == OnOffType.ON) {
                    String lastKnownRadioStationId = this.lastKnownRadioStationId;
                    if (lastKnownRadioStationId != null && !lastKnownRadioStationId.isEmpty()) {
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
                    String reminder = command.toFullString();
                    if (!reminder.isEmpty()) {
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
                    String alarmSound = command.toFullString();
                    if (!alarmSound.isEmpty()) {
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
                    String text = command.toFullString();
                    if (!text.isEmpty()) {
                        waitForUpdate = 1000;
                        updateTextToSpeech = true;
                        startTextToSpeech(connection, device, text);
                    }
                }
            }
            if (channelId.equals(CHANNEL_TEXT_TO_SPEECH_VOLUME)) {
                if (command instanceof PercentType percentCommand) {
                    textToSpeechVolume = percentCommand.intValue();
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
            if (channelId.equals(CHANNEL_TEXT_COMMAND)) {
                if (command instanceof StringType) {
                    String text = command.toFullString();
                    if (!text.isEmpty()) {
                        waitForUpdate = 1000;
                        updateTextCommand = true;
                        startTextCommand(connection, device, text);
                    }
                }
            }
            if (channelId.equals(CHANNEL_LAST_VOICE_COMMAND)) {
                if (command instanceof StringType) {
                    String text = command.toFullString();
                    if (!text.isEmpty()) {
                        waitForUpdate = -1;
                        startTextToSpeech(connection, device, text);
                    }
                }
            }
            if (channelId.equals(CHANNEL_START_COMMAND)) {
                if (command instanceof StringType) {
                    String commandText = command.toFullString();
                    if (!commandText.isEmpty()) {
                        updateStartCommand = true;
                        if (commandText.startsWith(FLASH_BRIEFING_COMMAND_PREFIX)) {
                            // Handle custom flashbriefings commands
                            String flashBriefingId = commandText.substring(FLASH_BRIEFING_COMMAND_PREFIX.length());
                            for (FlashBriefingProfileHandler flashBriefingHandler : account
                                    .getFlashBriefingProfileHandlers()) {
                                ThingUID flashBriefingUid = flashBriefingHandler.getThing().getUID();
                                if (flashBriefingId.equals(flashBriefingHandler.getThing().getUID().getId())) {
                                    flashBriefingHandler.handleCommand(
                                            new ChannelUID(flashBriefingUid, CHANNEL_PLAY_ON_DEVICE),
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
                            connection.executeSequenceCommand(device, commandText, Map.of());
                        }
                    }
                }
            }
            if (channelId.equals(CHANNEL_START_ROUTINE)) {
                if (command instanceof StringType) {
                    String utterance = command.toFullString();
                    if (!utterance.isEmpty()) {
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
                    if (states != null) {
                        state = states.findStateByDevice(device);
                    }
                }

                updateState(account, device, state, null, null, null, null, null);
            };
            if (command instanceof RefreshType) {
                waitForUpdate = 0;
                account.forceCheckData();
            }
            if (waitForUpdate == 0) {
                doRefresh.run();
            } else {
                this.updateStateJob = scheduler.schedule(doRefresh, waitForUpdate, TimeUnit.MILLISECONDS);
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.info("handleCommand fails", e);
        }
    }

    private boolean handleEqualizerCommands(String channelId, Command command, Connection connection, Device device)
            throws URISyntaxException {
        if (command instanceof RefreshType) {
            this.lastKnownEqualizer = null;
        }
        if (command instanceof DecimalType decimalCommand) {
            if (this.lastKnownEqualizer == null) {
                updateEqualizerState();
            }
            JsonEqualizer lastKnownEqualizer = this.lastKnownEqualizer;
            if (lastKnownEqualizer != null) {
                JsonEqualizer newEqualizerSetting = lastKnownEqualizer.createClone();
                if (channelId.equals(CHANNEL_EQUALIZER_BASS)) {
                    newEqualizerSetting.bass = decimalCommand.intValue();
                }
                if (channelId.equals(CHANNEL_EQUALIZER_MIDRANGE)) {
                    newEqualizerSetting.mid = decimalCommand.intValue();
                }
                if (channelId.equals(CHANNEL_EQUALIZER_TREBLE)) {
                    newEqualizerSetting.treble = decimalCommand.intValue();
                }
                try {
                    connection.setEqualizer(device, newEqualizerSetting);
                    return true;
                } catch (HttpException | IOException | ConnectionException | InterruptedException e) {
                    logger.debug("Update equalizer failed", e);
                    this.lastKnownEqualizer = null;
                }
            }
        }
        return false;
    }

    private void startTextToSpeech(Connection connection, Device device, String text)
            throws IOException, URISyntaxException {
        Integer volume = null;
        if (textToSpeechVolume != 0) {
            volume = textToSpeechVolume;
        }
        connection.textToSpeech(device, text, volume, lastKnownVolume);
    }

    private void startTextCommand(Connection connection, Device device, String text)
            throws IOException, URISyntaxException {
        Integer volume = null;
        if (textToSpeechVolume != 0) {
            volume = textToSpeechVolume;
        }
        connection.textCommand(device, text, volume, lastKnownVolume);
    }

    @Override
    public void startAnnouncement(Device device, String speak, String bodyText, @Nullable String title,
            @Nullable Integer volume) throws IOException, URISyntaxException {
        Connection connection = this.findConnection();
        if (connection == null) {
            return;
        }
        if (volume == null && textToSpeechVolume != 0) {
            volume = textToSpeechVolume;
        }
        if (volume != null && volume < 0) {
            volume = null; // the meaning of negative values is 'do not use'. The api requires null in this case.
        }
        connection.announcement(device, speak, bodyText, title, volume, lastKnownVolume);
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
                } catch (IOException | URISyntaxException | InterruptedException e) {
                    logger.warn("Stop notification failed", e);
                }
            }
        }
    }

    private void updateNotificationTimerState() {
        boolean stopCurrentNotification = true;
        JsonNotificationResponse currentNotification = this.currentNotification;
        try {
            if (currentNotification != null) {
                Connection currentConnection = this.findConnection();
                if (currentConnection != null) {
                    JsonNotificationResponse newState = currentConnection.getNotificationState(currentNotification);
                    if (newState != null && "ON".equals(newState.status)) {
                        stopCurrentNotification = false;
                    }
                }
            }
        } catch (IOException | URISyntaxException | InterruptedException e) {
            logger.warn("update notification state fails", e);
        }
        if (stopCurrentNotification) {
            if (currentNotification != null) {
                String type = currentNotification.type;
                if (type != null) {
                    if ("Reminder".equals(type)) {
                        updateState(CHANNEL_REMIND, StringType.EMPTY);
                        updateRemind = false;
                    }
                    if ("Alarm".equals(type)) {
                        updateState(CHANNEL_PLAY_ALARM_SOUND, StringType.EMPTY);
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
            @Nullable List<JsonNotificationSound> alarmSounds, @Nullable List<JsonMusicProvider> musicProviders) {
        try {
            this.logger.debug("Handle updateState {}", this.getThing().getUID());

            if (deviceNotificationState != null) {
                notificationVolumeLevel = deviceNotificationState.volumeLevel;
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
                this.logger.debug("Handle updateState {} aborted: Not online", this.getThing().getUID());
                return;
            }
            if (device == null) {
                this.logger.debug("Handle updateState {} aborted: No device", this.getThing().getUID());
                return;
            }

            if (this.disableUpdate) {
                this.logger.debug("Handle updateState {} aborted: Disabled", this.getThing().getUID());
                return;
            }
            Connection connection = this.findConnection();
            if (connection == null) {
                return;
            }

            if (this.lastKnownEqualizer == null) {
                updateEqualizerState();
            }

            PlayerInfo playerInfo = null;
            Provider provider = null;
            InfoText infoText = null;
            MainArt mainArt = null;
            String musicProviderId = null;
            Progress progress = null;
            try {
                JsonPlayerState playerState = connection.getPlayer(device);
                if (playerState != null) {
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

                                if ("AMAZON MUSIC".equals(musicProviderId)) {
                                    musicProviderId = "AMAZON_MUSIC";
                                }
                                if ("CLOUD_PLAYER".equals(musicProviderId)) {
                                    musicProviderId = "AMAZON_MUSIC";
                                }
                                if (musicProviderId.startsWith("TUNEIN")) {
                                    musicProviderId = "TUNEIN";
                                }
                                if (musicProviderId.startsWith("IHEARTRADIO")) {
                                    musicProviderId = "I_HEART_RADIO";
                                }
                                if ("APPLE".equals(musicProviderId) && musicProviderId.contains("MUSIC")) {
                                    musicProviderId = "APPLE_MUSIC";
                                }
                            }
                        }
                        progress = playerInfo.progress;
                    }
                }
            } catch (HttpException e) {
                if (e.getCode() != 400) {
                    logger.info("getPlayer fails", e);
                }
            } catch (IOException | URISyntaxException | InterruptedException e) {
                logger.info("getPlayer fails", e);
            }
            // check playing
            isPlaying = (playerInfo != null && "PLAYING".equals(playerInfo.state));

            isPaused = (playerInfo != null && "PAUSED".equals(playerInfo.state));
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
                if ("AMAZON_MUSIC".equalsIgnoreCase(musicProviderId) || "TUNEIN".equalsIgnoreCase(musicProviderId)) {
                    mediaState = connection.getMediaState(device);
                }
            } catch (HttpException e) {
                if (e.getCode() == 400) {
                    updateState(CHANNEL_RADIO_STATION_ID, StringType.EMPTY);
                } else {
                    logger.info("getMediaState fails", e);
                }
            } catch (IOException | URISyntaxException | InterruptedException e) {
                logger.info("getMediaState fails", e);
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
            if (mediaState != null) {
                String contentId = mediaState.contentId;
                if (isPlaying && "CLOUD_PLAYER".equals(mediaState.providerId) && contentId != null
                        && !contentId.isEmpty()) {
                    amazonMusicTrackId = contentId;
                    lastKnownAmazonMusicId = amazonMusicTrackId;
                    amazonMusic = true;
                }
            }

            // handle bluetooth
            String bluetoothMAC = "";
            String bluetoothDeviceName = "";
            boolean bluetoothIsConnected = false;
            if (bluetoothState != null) {
                this.bluetoothState = bluetoothState;
                for (PairedDevice paired : bluetoothState.getPairedDeviceList()) {
                    String pairedAddress = paired.address;
                    if (paired.connected && pairedAddress != null) {
                        bluetoothIsConnected = true;
                        bluetoothMAC = pairedAddress;
                        bluetoothDeviceName = paired.friendlyName;
                        if (bluetoothDeviceName == null || bluetoothDeviceName.isEmpty()) {
                            bluetoothDeviceName = pairedAddress;
                        }
                        break;
                    }
                }

            }
            if (!bluetoothMAC.isEmpty()) {
                lastKnownBluetoothMAC = bluetoothMAC;
            }

            // handle radio
            boolean isRadio = false;
            String radioStationId = "";
            if (mediaState != null) {
                radioStationId = Objects.requireNonNullElse(mediaState.radioStationId, "");
                if (!radioStationId.isEmpty()) {
                    lastKnownRadioStationId = radioStationId;
                    if ("TUNEIN".equalsIgnoreCase(musicProviderId)) {
                        isRadio = true;
                        if (!"PLAYING".equals(mediaState.currentState)) {
                            radioStationId = "";
                        }
                    }
                }
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
                List<QueueEntry> queueEntries = Objects.requireNonNullElse(mediaState.queue, List.of());
                if (!queueEntries.isEmpty()) {
                    QueueEntry entry = queueEntries.get(0);
                    if (isRadio) {
                        if ((imageUrl == null || imageUrl.isEmpty()) && entry.imageURL != null) {
                            imageUrl = entry.imageURL;
                        }
                        if ((subTitle1 == null || subTitle1.isEmpty()) && entry.radioStationSlogan != null) {
                            subTitle1 = entry.radioStationSlogan;
                        }
                        if ((subTitle2 == null || subTitle2.isEmpty()) && entry.radioStationLocation != null) {
                            subTitle2 = entry.radioStationLocation;
                        }
                    }

                }
            }

            // handle provider
            String providerDisplayName = "";
            if (provider != null) {
                if (provider.providerDisplayName != null) {
                    providerDisplayName = Objects.requireNonNullElse(provider.providerDisplayName, providerDisplayName);
                }
                String providerName = provider.providerName;
                if (providerName != null && !providerName.isEmpty() && providerDisplayName.isEmpty()) {
                    providerDisplayName = provider.providerName;
                }
            }

            // handle volume
            Integer volume = null;
            if (!connection.isSequenceNodeQueueRunning()) {
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
                updateState(CHANNEL_REMIND, StringType.EMPTY);
            }
            if (updateAlarm && currentNotifcationUpdateTimer == null) {
                updateAlarm = false;
                updateState(CHANNEL_PLAY_ALARM_SOUND, StringType.EMPTY);
            }
            if (updateRoutine) {
                updateRoutine = false;
                updateState(CHANNEL_START_ROUTINE, StringType.EMPTY);
            }
            if (updateTextToSpeech) {
                updateTextToSpeech = false;
                updateState(CHANNEL_TEXT_TO_SPEECH, StringType.EMPTY);
            }
            if (updateTextCommand) {
                updateTextCommand = false;
                updateState(CHANNEL_TEXT_COMMAND, StringType.EMPTY);
            }
            if (updatePlayMusicVoiceCommand) {
                updatePlayMusicVoiceCommand = false;
                updateState(CHANNEL_PLAY_MUSIC_VOICE_COMMAND, StringType.EMPTY);
            }
            if (updateStartCommand) {
                updateStartCommand = false;
                updateState(CHANNEL_START_COMMAND, StringType.EMPTY);
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

            final Integer notificationVolumeLevel = this.notificationVolumeLevel;
            if (notificationVolumeLevel != null) {
                updateState(CHANNEL_NOTIFICATION_VOLUME, new PercentType(notificationVolumeLevel));
            } else {
                updateState(CHANNEL_NOTIFICATION_VOLUME, UnDefType.UNDEF);
            }
        } catch (Exception e) {
            this.logger.debug("Handle updateState {} failed: {}", this.getThing().getUID(), e.getMessage(), e);

            disableUpdate = false;
            throw e; // Rethrow same exception
        }
    }

    private void updateEqualizerState() {
        if (!this.capabilities.contains("SOUND_SETTINGS")) {
            return;
        }

        Connection connection = findConnection();
        if (connection == null) {
            return;
        }
        Device device = findDevice();
        if (device == null) {
            return;
        }
        Integer bass = null;
        Integer midrange = null;
        Integer treble = null;
        try {
            JsonEqualizer equalizer = connection.getEqualizer(device);
            if (equalizer != null) {
                bass = equalizer.bass;
                midrange = equalizer.mid;
                treble = equalizer.treble;
            }
            this.lastKnownEqualizer = equalizer;
        } catch (IOException | URISyntaxException | HttpException | ConnectionException | InterruptedException e) {
            logger.debug("Get equalizer failes", e);
            return;
        }
        if (bass != null) {
            updateState(CHANNEL_EQUALIZER_BASS, new DecimalType(bass));
        }
        if (midrange != null) {
            updateState(CHANNEL_EQUALIZER_MIDRANGE, new DecimalType(midrange));
        }
        if (treble != null) {
            updateState(CHANNEL_EQUALIZER_TREBLE, new DecimalType(treble));
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

    public void handlePushActivity(Activity pushActivity) {
        if ("DISCARDED_NON_DEVICE_DIRECTED_INTENT".equals(pushActivity.activityStatus)) {
            return;
        }
        Description description = pushActivity.parseDescription();
        String firstUtteranceId = description.firstUtteranceId;
        if (firstUtteranceId == null || firstUtteranceId.isEmpty()
                || firstUtteranceId.toLowerCase().startsWith("textclient:")) {
            return;
        }
        String firstStreamId = description.firstStreamId;
        if (firstStreamId == null || firstStreamId.isEmpty()) {
            return;
        }
        String spokenText = description.summary;
        if (spokenText != null && !spokenText.isEmpty()) {
            // remove wake word
            String wakeWordPrefix = this.wakeWord;
            if (wakeWordPrefix != null) {
                wakeWordPrefix += " ";
                if (spokenText.toLowerCase().startsWith(wakeWordPrefix.toLowerCase())) {
                    spokenText = spokenText.substring(wakeWordPrefix.length());
                }
            }

            if (lastSpokenText.isEmpty() || lastSpokenText.equals(spokenText)) {
                updateState(CHANNEL_LAST_VOICE_COMMAND, StringType.EMPTY);
            }
            lastSpokenText = spokenText;
            updateState(CHANNEL_LAST_VOICE_COMMAND, new StringType(spokenText));
        }
    }

    public void handlePushCommand(String command, String payload) {
        this.logger.debug("Handle push command {}", command);
        switch (command) {
            case "PUSH_VOLUME_CHANGE":
                JsonCommandPayloadPushVolumeChange volumeChange = Objects
                        .requireNonNull(gson.fromJson(payload, JsonCommandPayloadPushVolumeChange.class));
                Connection connection = this.findConnection();
                Integer volumeSetting = volumeChange.volumeSetting;
                Boolean muted = volumeChange.isMuted;
                if (muted != null && muted) {
                    updateState(CHANNEL_VOLUME, new PercentType(0));
                }
                if (volumeSetting != null && connection != null && !connection.isSequenceNodeQueueRunning()) {
                    lastKnownVolume = volumeSetting;
                    updateState(CHANNEL_VOLUME, new PercentType(lastKnownVolume));
                }
                break;
            case "PUSH_EQUALIZER_STATE_CHANGE":
                updateEqualizerState();
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

    public void updateNotifications(ZonedDateTime currentTime, ZonedDateTime now,
            @Nullable JsonCommandPayloadPushNotificationChange pushPayload,
            List<JsonNotificationResponse> notifications) {
        Device device = this.device;
        if (device == null) {
            return;
        }

        ZonedDateTime nextReminder = null;
        ZonedDateTime nextAlarm = null;
        ZonedDateTime nextMusicAlarm = null;
        ZonedDateTime nextTimer = null;
        for (JsonNotificationResponse notification : notifications) {
            if (Objects.equals(notification.deviceSerialNumber, device.serialNumber)) {
                // notification for this device
                if ("ON".equals(notification.status)) {
                    if ("Reminder".equals(notification.type)) {
                        String offset = ZoneId.systemDefault().getRules().getOffset(Instant.now()).toString();
                        String date = notification.originalDate != null ? notification.originalDate
                                : ZonedDateTime.now().toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
                        String time = notification.originalTime != null ? notification.originalTime : "00:00:00";
                        ZonedDateTime alarmTime = ZonedDateTime.parse(date + "T" + time + offset,
                                DateTimeFormatter.ISO_DATE_TIME);
                        String recurringPattern = notification.recurringPattern;
                        if (recurringPattern != null && !recurringPattern.isBlank() && alarmTime.isBefore(now)) {
                            continue; // Ignore recurring entry if alarm time is before now
                        }
                        if (nextReminder == null || alarmTime.isBefore(nextReminder)) {
                            nextReminder = alarmTime;
                        }
                    } else if ("Timer".equals(notification.type)) {
                        // use remaining time
                        ZonedDateTime alarmTime = currentTime.plus(notification.remainingTime, ChronoUnit.MILLIS);
                        if (nextTimer == null || alarmTime.isBefore(nextTimer)) {
                            nextTimer = alarmTime;
                        }
                    } else if ("Alarm".equals(notification.type)) {
                        String offset = ZoneId.systemDefault().getRules().getOffset(Instant.now()).toString();
                        ZonedDateTime alarmTime = ZonedDateTime
                                .parse(notification.originalDate + "T" + notification.originalTime + offset);
                        String recurringPattern = notification.recurringPattern;
                        if (recurringPattern != null && !recurringPattern.isBlank() && alarmTime.isBefore(now)) {
                            continue; // Ignore recurring entry if alarm time is before now
                        }
                        if (nextAlarm == null || alarmTime.isBefore(nextAlarm)) {
                            nextAlarm = alarmTime;
                        }
                    } else if ("MusicAlarm".equals(notification.type)) {
                        String offset = ZoneId.systemDefault().getRules().getOffset(Instant.now()).toString();
                        ZonedDateTime alarmTime = ZonedDateTime
                                .parse(notification.originalDate + "T" + notification.originalTime + offset);
                        String recurringPattern = notification.recurringPattern;
                        if (recurringPattern != null && !recurringPattern.isBlank() && alarmTime.isBefore(now)) {
                            continue; // Ignore recurring entry if alarm time is before now
                        }
                        if (nextMusicAlarm == null || alarmTime.isBefore(nextMusicAlarm)) {
                            nextMusicAlarm = alarmTime;
                        }
                    }
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
    public void updateChannelState(String channelId, State state) {
        updateState(channelId, state);
    }
}
