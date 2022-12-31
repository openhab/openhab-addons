/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.androiddebugbridge.internal;

import static org.openhab.binding.androiddebugbridge.internal.AndroidDebugBridgeBindingConstants.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.CommandOption;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link AndroidDebugBridgeHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Miguel Álvarez - Initial contribution
 */
@NonNullByDefault
public class AndroidDebugBridgeHandler extends BaseThingHandler {
    public static final String KEY_EVENT_PLAY = "126";
    public static final String KEY_EVENT_PAUSE = "127";
    public static final String KEY_EVENT_NEXT = "87";
    public static final String KEY_EVENT_PREVIOUS = "88";
    public static final String KEY_EVENT_MEDIA_REWIND = "89";
    public static final String KEY_EVENT_MEDIA_FAST_FORWARD = "90";
    private static final String SHUTDOWN_POWER_OFF = "POWER_OFF";
    private static final String SHUTDOWN_REBOOT = "REBOOT";
    private static final Gson GSON = new Gson();
    private static final Pattern RECORD_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]*$");
    private final Logger logger = LoggerFactory.getLogger(AndroidDebugBridgeHandler.class);

    private final AndroidDebugBridgeDynamicCommandDescriptionProvider commandDescriptionProvider;
    private final AndroidDebugBridgeDevice adbConnection;
    private int maxMediaVolume = 0;
    private AndroidDebugBridgeConfiguration config = new AndroidDebugBridgeConfiguration();
    private @Nullable ScheduledFuture<?> connectionCheckerSchedule;
    private AndroidDebugBridgeMediaStatePackageConfig @Nullable [] packageConfigs = null;
    private boolean deviceAwake = false;

    public AndroidDebugBridgeHandler(Thing thing,
            AndroidDebugBridgeDynamicCommandDescriptionProvider commandDescriptionProvider) {
        super(thing);
        this.commandDescriptionProvider = commandDescriptionProvider;
        this.adbConnection = new AndroidDebugBridgeDevice(scheduler);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        AndroidDebugBridgeConfiguration currentConfig = config;
        try {
            if (!adbConnection.isConnected()) {
                // try reconnect
                adbConnection.connect();
            }
            handleCommandInternal(channelUID, command);
        } catch (InterruptedException ignored) {
        } catch (AndroidDebugBridgeDeviceException | ExecutionException e) {
            if (!(e.getCause() instanceof InterruptedException)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                adbConnection.disconnect();
            }
        } catch (AndroidDebugBridgeDeviceReadException e) {
            logger.warn("{} - read error: {}", currentConfig.ip, e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("{} - timeout error", currentConfig.ip);
        }
    }

    private void handleCommandInternal(ChannelUID channelUID, Command command)
            throws InterruptedException, AndroidDebugBridgeDeviceException, AndroidDebugBridgeDeviceReadException,
            TimeoutException, ExecutionException {
        if (!isLinked(channelUID)) {
            return;
        }
        String channelId = channelUID.getId();
        switch (channelId) {
            case KEY_EVENT_CHANNEL:
                adbConnection.sendKeyEvent(command.toFullString());
                break;
            case TEXT_CHANNEL:
                adbConnection.sendText(command.toFullString());
                break;
            case TAP_CHANNEL:
                adbConnection.sendTap(command.toFullString());
                break;
            case URL_CHANNEL:
                adbConnection.openUrl(command.toFullString());
                break;
            case MEDIA_VOLUME_CHANNEL:
                handleMediaVolume(channelUID, command);
                break;
            case MEDIA_CONTROL_CHANNEL:
                handleMediaControlCommand(channelUID, command);
                break;
            case START_PACKAGE_CHANNEL:
                adbConnection.startPackage(command.toFullString());
                updateState(new ChannelUID(this.thing.getUID(), CURRENT_PACKAGE_CHANNEL),
                        new StringType(command.toFullString()));
                break;
            case STOP_PACKAGE_CHANNEL:
                adbConnection.stopPackage(command.toFullString());
                break;
            case STOP_CURRENT_PACKAGE_CHANNEL:
                if (OnOffType.from(command.toFullString()).equals(OnOffType.OFF)) {
                    adbConnection.stopPackage(adbConnection.getCurrentPackage());
                }
                break;
            case CURRENT_PACKAGE_CHANNEL:
                if (command instanceof RefreshType) {
                    var packageName = adbConnection.getCurrentPackage();
                    updateState(channelUID, new StringType(packageName));
                }
                break;
            case WAKE_LOCK_CHANNEL:
                if (command instanceof RefreshType) {
                    int lock = adbConnection.getPowerWakeLock();
                    updateState(channelUID, new DecimalType(lock));
                }
                break;
            case AWAKE_STATE_CHANNEL:
                if (command instanceof RefreshType) {
                    boolean awakeState = adbConnection.isAwake();
                    updateState(channelUID, OnOffType.from(awakeState));
                }
                break;
            case SCREEN_STATE_CHANNEL:
                if (command instanceof RefreshType) {
                    boolean screenState = adbConnection.isScreenOn();
                    updateState(channelUID, OnOffType.from(screenState));
                }
                break;
            case SHUTDOWN_CHANNEL:
                switch (command.toFullString()) {
                    case SHUTDOWN_POWER_OFF:
                        adbConnection.powerOffDevice();
                        updateStatus(ThingStatus.OFFLINE);
                        break;
                    case SHUTDOWN_REBOOT:
                        adbConnection.rebootDevice();
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.GONE, "Rebooting");
                        break;
                }
                break;
            case START_INTENT_CHANNEL:
                if (command instanceof RefreshType) {
                    return;
                }
                adbConnection.startIntent(command.toFullString());
                break;
            case RECORD_INPUT_CHANNEL:
                recordDeviceInput(command);
                break;
            case RECORDED_INPUT_CHANNEL:
                String recordName = getRecordPropertyName(command);
                var inputCommand = this.getThing().getProperties().get(recordName);
                if (inputCommand != null) {
                    adbConnection.sendInputEvents(inputCommand);
                }
                break;
        }
    }

    private void recordDeviceInput(Command recordNameCommand)
            throws AndroidDebugBridgeDeviceException, InterruptedException, TimeoutException, ExecutionException {
        var recordName = recordNameCommand.toFullString();
        if (!RECORD_NAME_PATTERN.matcher(recordName).matches()) {
            logger.warn("Invalid record name, accepts alphanumeric values with '_'.");
            return;
        }
        String recordPropertyName = getRecordPropertyName(recordName);
        logger.debug("RECORD: {}", recordPropertyName);
        var eventCommand = adbConnection.recordInputEvents();
        if (eventCommand.isEmpty()) {
            logger.debug("No events recorded");
            if (this.getThing().getProperties().containsKey(recordPropertyName)) {
                this.getThing().setProperty(recordPropertyName, null);
                updateProperties(editProperties());
                logger.debug("Record {} deleted", recordName);
            }
        } else {
            updateProperty(recordPropertyName, eventCommand);
            logger.debug("New record {}: {}", recordName, eventCommand);
        }
    }

    private String getRecordPropertyName(String recordName) {
        return String.format("input-record:%s", recordName);
    }

    private String getRecordPropertyName(Command recordNameCommand) {
        return getRecordPropertyName(recordNameCommand.toFullString());
    }

    private void handleMediaVolume(ChannelUID channelUID, Command command)
            throws InterruptedException, AndroidDebugBridgeDeviceReadException, AndroidDebugBridgeDeviceException,
            TimeoutException, ExecutionException {
        if (command instanceof RefreshType) {
            var volumeInfo = adbConnection.getMediaVolume();
            maxMediaVolume = volumeInfo.max;
            updateState(channelUID, new PercentType((int) Math.round(toPercent(volumeInfo.current, volumeInfo.max))));
        } else {
            if (maxMediaVolume == 0) {
                return; // We can not transform percentage
            }
            int targetVolume = Integer.parseInt(command.toFullString());
            adbConnection.setMediaVolume((int) Math.round(fromPercent(targetVolume, maxMediaVolume)));
            updateState(channelUID, new PercentType(targetVolume));
        }
    }

    private double toPercent(double value, double maxValue) {
        return (value / maxValue) * 100;
    }

    private double fromPercent(double value, double maxValue) {
        return (value / 100) * maxValue;
    }

    private void handleMediaControlCommand(ChannelUID channelUID, Command command)
            throws InterruptedException, AndroidDebugBridgeDeviceException, AndroidDebugBridgeDeviceReadException,
            TimeoutException, ExecutionException {
        if (command instanceof RefreshType) {
            boolean playing;
            String currentPackage = adbConnection.getCurrentPackage();
            var currentPackageConfig = packageConfigs != null ? Arrays.stream(packageConfigs)
                    .filter(pc -> pc.name.equals(currentPackage)).findFirst().orElse(null) : null;
            if (currentPackageConfig != null) {
                logger.debug("media stream config found for {}, mode: {}", currentPackage, currentPackageConfig.mode);
                switch (currentPackageConfig.mode) {
                    case "idle":
                        playing = false;
                        break;
                    case "wake_lock":
                        int wakeLockState = adbConnection.getPowerWakeLock();
                        playing = currentPackageConfig.wakeLockPlayStates.contains(wakeLockState);
                        break;
                    case "media_state":
                        playing = adbConnection.isPlayingMedia(currentPackage);
                        break;
                    case "audio":
                        playing = adbConnection.isPlayingAudio();
                        break;
                    default:
                        logger.warn("media state config: package {} unsupported mode", currentPackage);
                        playing = false;
                }
            } else {
                logger.debug("media stream config not found for {}", currentPackage);
                playing = adbConnection.isPlayingMedia(currentPackage);
            }
            updateState(channelUID, playing ? PlayPauseType.PLAY : PlayPauseType.PAUSE);
        } else if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                adbConnection.sendKeyEvent(KEY_EVENT_PLAY);
                updateState(channelUID, PlayPauseType.PLAY);
            } else if (command == PlayPauseType.PAUSE) {
                adbConnection.sendKeyEvent(KEY_EVENT_PAUSE);
                updateState(channelUID, PlayPauseType.PAUSE);
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                adbConnection.sendKeyEvent(KEY_EVENT_NEXT);
            } else if (command == NextPreviousType.PREVIOUS) {
                adbConnection.sendKeyEvent(KEY_EVENT_PREVIOUS);
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                adbConnection.sendKeyEvent(KEY_EVENT_MEDIA_FAST_FORWARD);
            } else if (command == RewindFastforwardType.REWIND) {
                adbConnection.sendKeyEvent(KEY_EVENT_MEDIA_REWIND);
            }
        } else {
            logger.warn("Unknown media control command: {}", command);
        }
    }

    @Override
    public void initialize() {
        AndroidDebugBridgeConfiguration currentConfig = getConfigAs(AndroidDebugBridgeConfiguration.class);
        config = currentConfig;
        var mediaStateJSONConfig = currentConfig.mediaStateJSONConfig;
        if (mediaStateJSONConfig != null && !mediaStateJSONConfig.isEmpty()) {
            loadMediaStateConfig(mediaStateJSONConfig);
        }
        adbConnection.configure(currentConfig);
        var androidVersion = thing.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION);
        if (androidVersion != null) {
            // configure android implementation to use
            adbConnection.setAndroidVersion(androidVersion);
        }
        updateStatus(ThingStatus.UNKNOWN);
        connectionCheckerSchedule = scheduler.scheduleWithFixedDelay(this::checkConnection, 0,
                currentConfig.refreshTime, TimeUnit.SECONDS);
    }

    private void loadMediaStateConfig(String mediaStateJSONConfig) {
        List<CommandOption> commandOptions;
        try {
            packageConfigs = GSON.fromJson(mediaStateJSONConfig, AndroidDebugBridgeMediaStatePackageConfig[].class);
            commandOptions = Arrays.stream(packageConfigs)
                    .map(AndroidDebugBridgeMediaStatePackageConfig::toCommandOption)
                    .collect(Collectors.toUnmodifiableList());
        } catch (JsonSyntaxException e) {
            logger.warn("unable to parse media state config: {}", e.getMessage());
            commandOptions = List.of();
        }
        commandDescriptionProvider.setCommandOptions(new ChannelUID(getThing().getUID(), START_PACKAGE_CHANNEL),
                commandOptions);
    }

    @Override
    public void dispose() {
        var schedule = connectionCheckerSchedule;
        if (schedule != null) {
            schedule.cancel(true);
            connectionCheckerSchedule = null;
        }
        packageConfigs = null;
        adbConnection.disconnect();
        super.dispose();
    }

    public void checkConnection() {
        AndroidDebugBridgeConfiguration currentConfig = config;
        try {
            logger.debug("Refresh device {} status", currentConfig.ip);
            if (adbConnection.isConnected()) {
                if (!ThingStatus.ONLINE.equals(getThing().getStatus())) {
                    // refresh properties only on state changes
                    refreshProperties();
                }
                updateStatus(ThingStatus.ONLINE);
                refreshStatus();
            } else {
                try {
                    adbConnection.connect();
                } catch (AndroidDebugBridgeDeviceException e) {
                    logger.debug("Error connecting to device; [{}]: {}", e.getClass().getCanonicalName(),
                            e.getMessage());
                    adbConnection.disconnect();
                    updateStatus(ThingStatus.OFFLINE);
                    return;
                }
                if (adbConnection.isConnected()) {
                    updateStatus(ThingStatus.ONLINE);
                    refreshProperties();
                    refreshStatus();
                }
            }
        } catch (InterruptedException ignored) {
        } catch (AndroidDebugBridgeDeviceException | AndroidDebugBridgeDeviceReadException | ExecutionException e) {
            logger.debug("Connection checker error: {}", e.getMessage());
            adbConnection.disconnect();
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private void refreshProperties() throws InterruptedException, AndroidDebugBridgeDeviceException,
            AndroidDebugBridgeDeviceReadException, ExecutionException {
        // Add some information about the device
        try {
            Map<String, String> editProperties = editProperties();
            editProperties.put(Thing.PROPERTY_SERIAL_NUMBER, adbConnection.getSerialNo());
            editProperties.put(Thing.PROPERTY_MODEL_ID, adbConnection.getModel());
            var androidVersion = adbConnection.getAndroidVersion();
            editProperties.put(Thing.PROPERTY_FIRMWARE_VERSION, androidVersion);
            // refresh android version to use
            adbConnection.setAndroidVersion(androidVersion);
            editProperties.put(Thing.PROPERTY_VENDOR, adbConnection.getBrand());
            try {
                editProperties.put(Thing.PROPERTY_MAC_ADDRESS, adbConnection.getMacAddress());
            } catch (AndroidDebugBridgeDeviceReadException e) {
                logger.debug("Refresh properties error: {}", e.getMessage());
            }
            updateProperties(editProperties);
        } catch (TimeoutException e) {
            logger.debug("Refresh properties error: Timeout");
            return;
        }
    }

    private void refreshStatus() throws InterruptedException, AndroidDebugBridgeDeviceException, ExecutionException {
        boolean awakeState;
        boolean prevDeviceAwake = deviceAwake;
        try {
            awakeState = adbConnection.isAwake();
            deviceAwake = awakeState;
        } catch (TimeoutException e) {
            // happen a lot when device is sleeping; abort refresh other channels
            logger.debug("Unable to refresh awake state: Timeout; aborting channels refresh");
            return;
        }
        var awakeStateChannelUID = new ChannelUID(this.thing.getUID(), AWAKE_STATE_CHANNEL);
        if (isLinked(awakeStateChannelUID)) {
            updateState(awakeStateChannelUID, OnOffType.from(awakeState));
        }
        if (!awakeState && !prevDeviceAwake) {
            // abort refresh channels while device is sleeping, throws many timeouts
            logger.debug("device {} is sleeping", config.ip);
            return;
        }
        try {
            handleCommandInternal(new ChannelUID(this.thing.getUID(), MEDIA_VOLUME_CHANNEL), RefreshType.REFRESH);
        } catch (AndroidDebugBridgeDeviceReadException e) {
            logger.warn("Unable to refresh media volume: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("Unable to refresh media volume: Timeout");
        }
        try {
            handleCommandInternal(new ChannelUID(this.thing.getUID(), MEDIA_CONTROL_CHANNEL), RefreshType.REFRESH);
        } catch (AndroidDebugBridgeDeviceReadException e) {
            logger.warn("Unable to refresh play status: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("Unable to refresh play status: Timeout");
        }
        try {
            handleCommandInternal(new ChannelUID(this.thing.getUID(), CURRENT_PACKAGE_CHANNEL), RefreshType.REFRESH);
        } catch (AndroidDebugBridgeDeviceReadException e) {
            logger.warn("Unable to refresh current package: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("Unable to refresh current package: Timeout");
        }
        try {
            handleCommandInternal(new ChannelUID(this.thing.getUID(), WAKE_LOCK_CHANNEL), RefreshType.REFRESH);
        } catch (AndroidDebugBridgeDeviceReadException e) {
            logger.warn("Unable to refresh wake lock: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("Unable to refresh wake lock: Timeout");
        }
        try {
            handleCommandInternal(new ChannelUID(this.thing.getUID(), SCREEN_STATE_CHANNEL), RefreshType.REFRESH);
        } catch (AndroidDebugBridgeDeviceReadException e) {
            logger.warn("Unable to refresh screen state: {}", e.getMessage());
        } catch (TimeoutException e) {
            logger.warn("Unable to refresh screen state: Timeout");
        }
    }

    static class AndroidDebugBridgeMediaStatePackageConfig {
        public String name = "";
        public @Nullable String label;
        public String mode = "";
        public List<Integer> wakeLockPlayStates = List.of();

        public CommandOption toCommandOption() {
            return new CommandOption(name, label == null ? name : label);
        }
    }
}
