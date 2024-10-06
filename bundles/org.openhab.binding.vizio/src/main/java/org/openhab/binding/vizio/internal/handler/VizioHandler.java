/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.vizio.internal.handler;

import static org.openhab.binding.vizio.internal.VizioBindingConstants.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.vizio.internal.VizioConfiguration;
import org.openhab.binding.vizio.internal.VizioException;
import org.openhab.binding.vizio.internal.VizioStateDescriptionOptionProvider;
import org.openhab.binding.vizio.internal.communication.VizioCommunicator;
import org.openhab.binding.vizio.internal.dto.app.CurrentApp;
import org.openhab.binding.vizio.internal.dto.applist.VizioApp;
import org.openhab.binding.vizio.internal.dto.applist.VizioApps;
import org.openhab.binding.vizio.internal.dto.audio.Audio;
import org.openhab.binding.vizio.internal.dto.audio.ItemAudio;
import org.openhab.binding.vizio.internal.dto.input.CurrentInput;
import org.openhab.binding.vizio.internal.dto.inputlist.InputList;
import org.openhab.binding.vizio.internal.dto.power.PowerMode;
import org.openhab.binding.vizio.internal.enums.KeyCommand;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.thing.util.ThingWebClientUtil;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link VizioHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class VizioHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(VizioHandler.class);
    private final HttpClientFactory httpClientFactory;
    private @Nullable HttpClient httpClient;
    private final VizioStateDescriptionOptionProvider stateDescriptionProvider;
    private final String dbAppsJson;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> metadataRefreshJob;

    private VizioCommunicator communicator;
    private List<VizioApp> userConfigApps = new ArrayList<>();
    private Object sequenceLock = new Object();

    private int failCount = 0;
    private int pairingDeviceId = -1;
    private int pairingToken = -1;
    private Long currentInputHash = 0L;
    private Long currentVolumeHash = 0L;
    private String currentApp = EMPTY;
    private String currentInput = EMPTY;
    private boolean currentMute = false;
    private int currentVolume = -1;
    private boolean powerOn = false;
    private boolean debounce = true;

    public VizioHandler(Thing thing, HttpClientFactory httpClientFactory,
            VizioStateDescriptionOptionProvider stateDescriptionProvider, String vizioAppsJson) {
        super(thing);
        this.httpClientFactory = httpClientFactory;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.dbAppsJson = vizioAppsJson;
        this.communicator = new VizioCommunicator(httpClientFactory.getCommonHttpClient(), EMPTY, -1, EMPTY);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Vizio handler");
        final Gson gson = new Gson();
        VizioConfiguration config = getConfigAs(VizioConfiguration.class);

        @Nullable
        String host = config.hostName;
        final @Nullable String authToken = config.authToken;
        @Nullable
        String appListJson = config.appListJson;

        if (host == null || host.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-hostname");
            return;
        } else if (host.contains(":")) {
            // format for ipv6
            host = "[" + host + "]";
        }

        final String httpClientName = ThingWebClientUtil.buildWebClientConsumerName(thing.getUID(), null);
        try {
            httpClient = httpClientFactory.createHttpClient(httpClientName, new SslContextFactory.Client(true));
            final HttpClient localHttpClient = this.httpClient;
            if (localHttpClient != null) {
                localHttpClient.start();
                this.communicator = new VizioCommunicator(localHttpClient, host, config.port,
                        authToken != null ? authToken : EMPTY);
            }
        } catch (Exception e) {
            logger.error(
                    "Long running HttpClient for Vizio handler {} cannot be started. Creating Handler failed. Exception: {}",
                    httpClientName, e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            return;
        }

        if (authToken == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_PENDING,
                    "@text/offline.configuration-error-authtoken");
            return;
        }

        // if app list is not supplied in thing configuration, populate it from the json db
        if (appListJson == null) {
            appListJson = dbAppsJson;

            // Update thing configuration (persistent) - store app list from db into thing so the user can update it
            Configuration configuration = this.getConfig();
            configuration.put(PROPERTY_APP_LIST_JSON, appListJson);
            this.updateConfiguration(configuration);
        }

        try {
            VizioApps appsFromJson = gson.fromJson(appListJson, VizioApps.class);
            if (appsFromJson != null && !appsFromJson.getApps().isEmpty()) {
                userConfigApps = appsFromJson.getApps();

                List<StateOption> appListOptions = new ArrayList<>();
                userConfigApps.forEach(app -> {
                    appListOptions.add(new StateOption(app.getName(), app.getName()));
                });

                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), ACTIVE_APP),
                        appListOptions);
            }
        } catch (JsonSyntaxException e) {
            logger.debug("Invalid App List Configuration in thing configuration. Exception: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/offline.configuration-error-applist");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        startVizioStateRefresh();
        startPeriodicRefresh();
    }

    /**
     * Start the job that queries the Vizio TV every 10 seconds to get its current status
     */
    private void startVizioStateRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::refreshVizioState, 5, 10, TimeUnit.SECONDS);
        }
    }

    /**
     * Get current status from the Vizio TV and update the channels
     */
    private void refreshVizioState() {
        synchronized (sequenceLock) {
            try {
                PowerMode polledPowerMode = communicator.getPowerMode();

                if (debounce && !polledPowerMode.getItems().isEmpty()) {
                    int powerMode = polledPowerMode.getItems().get(0).getValue();
                    if (powerMode == 1) {
                        powerOn = true;
                        updateState(POWER, OnOffType.ON);
                    } else if (powerMode == 0) {
                        powerOn = false;
                        updateState(POWER, OnOffType.OFF);
                    } else {
                        logger.debug("Unknown power mode {}, for response object: {}", powerMode, polledPowerMode);
                    }
                }
                updateStatus(ThingStatus.ONLINE);
                failCount = 0;
            } catch (VizioException e) {
                logger.debug("Unable to retrieve Vizio TV power mode info. Exception: {}", e.getMessage(), e);
                // A communication error must occur 3 times before updating the thing status
                failCount++;
                if (failCount > 2) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                            "@text/offline.communication-error-polling");
                }
                return;
            }

            if (powerOn && (isLinked(VOLUME) || isLinked(MUTE))) {
                try {
                    Audio audioSettings = communicator.getCurrentAudioSettings();

                    Optional<ItemAudio> volumeItem = audioSettings.getItems().stream()
                            .filter(i -> VOLUME.equals(i.getCname())).findFirst();
                    if (debounce && volumeItem.isPresent()) {
                        currentVolumeHash = volumeItem.get().getHashval();

                        try {
                            int polledVolume = Integer.parseInt(volumeItem.get().getValue());
                            if (polledVolume != currentVolume) {
                                currentVolume = polledVolume;
                                updateState(VOLUME, new PercentType(BigDecimal.valueOf(currentVolume)));
                            }
                        } catch (NumberFormatException e) {
                            logger.debug("Unable to parse volume value {} as int", volumeItem.get().getValue());
                        }
                    }

                    Optional<ItemAudio> muteItem = audioSettings.getItems().stream()
                            .filter(i -> MUTE.equals(i.getCname())).findFirst();
                    if (debounce && muteItem.isPresent()) {
                        String polledMute = muteItem.get().getValue().toUpperCase(Locale.ENGLISH);

                        if (ON.equals(polledMute) || OFF.equals(polledMute)) {
                            if (ON.equals(polledMute) && !currentMute) {
                                updateState(MUTE, OnOffType.ON);
                                currentMute = true;
                            } else if (OFF.equals(polledMute) && currentMute) {
                                updateState(MUTE, OnOffType.OFF);
                                currentMute = false;
                            }
                        } else {
                            logger.debug("Unknown mute mode {}, for response object: {}", polledMute, audioSettings);
                        }
                    }
                } catch (VizioException e) {
                    logger.debug("Unable to retrieve Vizio TV current audio settings. Exception: {}", e.getMessage(),
                            e);
                }
            }

            if (powerOn && isLinked(SOURCE)) {
                try {
                    CurrentInput polledInputState = communicator.getCurrentInput();

                    if (debounce && !polledInputState.getItems().isEmpty()
                            && !currentInput.equals(polledInputState.getItems().get(0).getValue())) {
                        currentInput = polledInputState.getItems().get(0).getValue();
                        currentInputHash = polledInputState.getItems().get(0).getHashval();
                        updateState(SOURCE, new StringType(currentInput));
                    }
                } catch (VizioException e) {
                    logger.debug("Unable to retrieve Vizio TV current input. Exception: {}", e.getMessage(), e);
                }
            }

            if (powerOn && isLinked(ACTIVE_APP)) {
                try {
                    if (debounce) {
                        CurrentApp polledApp = communicator.getCurrentApp();
                        Optional<VizioApp> currentAppData = userConfigApps.stream()
                                .filter(a -> a.getConfig().getAppId().equals(polledApp.getItem().getValue().getAppId())
                                        && a.getConfig().getNameSpace()
                                                .equals(polledApp.getItem().getValue().getNameSpace()))
                                .findFirst();

                        if (currentAppData.isPresent()) {
                            if (!currentApp.equals(currentAppData.get().getName())) {
                                currentApp = currentAppData.get().getName();
                                updateState(ACTIVE_APP, new StringType(currentApp));
                            }
                        } else {
                            currentApp = EMPTY;
                            try {
                                int appId = Integer.parseInt(polledApp.getItem().getValue().getAppId());
                                updateState(ACTIVE_APP, new StringType(String.format(UNKNOWN_APP_STR, appId,
                                        polledApp.getItem().getValue().getNameSpace())));
                            } catch (NumberFormatException nfe) {
                                // Non-numeric appId received, eg: hdmi1
                                updateState(ACTIVE_APP, UnDefType.UNDEF);
                            }

                            logger.debug("Unknown app_id: {}, name_space: {}",
                                    polledApp.getItem().getValue().getAppId(),
                                    polledApp.getItem().getValue().getNameSpace());
                        }
                    }
                } catch (VizioException e) {
                    logger.debug("Unable to retrieve Vizio TV current running app. Exception: {}", e.getMessage(), e);
                }
            }
        }
        debounce = true;
    }

    /**
     * Start the job to periodically retrieve various metadata from the Vizio TV every 10 minutes
     */
    private void startPeriodicRefresh() {
        ScheduledFuture<?> metadataRefreshJob = this.metadataRefreshJob;
        if (metadataRefreshJob == null || metadataRefreshJob.isCancelled()) {
            this.metadataRefreshJob = scheduler.scheduleWithFixedDelay(this::refreshVizioMetadata, 1, 600,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Update source list (hashes) and other metadata from the Vizio TV
     */
    private void refreshVizioMetadata() {
        synchronized (sequenceLock) {
            try {
                InputList inputList = communicator.getSourceInputList();

                List<StateOption> sourceListOptions = new ArrayList<>();
                inputList.getItems().forEach(source -> {
                    sourceListOptions.add(new StateOption(source.getName(), source.getValue().getName()));
                });

                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), SOURCE),
                        sourceListOptions);
            } catch (VizioException e) {
                logger.debug("Unable to retrieve the Vizio TV input list. Exception: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void dispose() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob != null) {
            refreshJob.cancel(true);
            this.refreshJob = null;
        }

        ScheduledFuture<?> metadataRefreshJob = this.metadataRefreshJob;
        if (metadataRefreshJob != null) {
            metadataRefreshJob.cancel(true);
            this.metadataRefreshJob = null;
        }

        try {
            HttpClient localHttpClient = this.httpClient;
            if (localHttpClient != null) {
                localHttpClient.stop();
            }
            this.httpClient = null;
        } catch (Exception e) {
            logger.debug("Unable to stop Vizio httpClient. Exception: {}", e.getMessage(), e);
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Unsupported refresh command: {}", command);
        } else {
            switch (channelUID.getId()) {
                case POWER:
                    debounce = false;
                    synchronized (sequenceLock) {
                        try {
                            if (command == OnOffType.ON) {
                                communicator.sendKeyPress(KeyCommand.POWERON.getJson());
                                powerOn = true;
                            } else {
                                communicator.sendKeyPress(KeyCommand.POWEROFF.getJson());
                                powerOn = false;
                            }
                        } catch (VizioException e) {
                            logger.warn("Unable to send power {} command to the Vizio TV, Exception: {}", command,
                                    e.getMessage());
                        }
                    }
                    break;
                case VOLUME:
                    debounce = false;
                    synchronized (sequenceLock) {
                        try {
                            int volume = Integer.parseInt(command.toString());

                            // volume changed again before polling has run, get current volume hash from the TV first
                            if (currentVolumeHash.equals(0L)) {
                                Audio audioSettings = communicator.getCurrentAudioSettings();

                                Optional<ItemAudio> volumeItem = audioSettings.getItems().stream()
                                        .filter(i -> VOLUME.equals(i.getCname())).findFirst();
                                if (volumeItem.isPresent()) {
                                    currentVolumeHash = volumeItem.get().getHashval();
                                } else {
                                    logger.debug("Unable to get current volume hash on the Vizio TV");
                                }
                            }
                            communicator
                                    .changeVolume(String.format(MODIFY_INT_SETTING_JSON, volume, currentVolumeHash));
                            currentVolumeHash = 0L;
                        } catch (VizioException e) {
                            logger.warn("Unable to set volume on the Vizio TV, command volume: {}, Exception: {}",
                                    command, e.getMessage());
                        } catch (NumberFormatException e) {
                            logger.warn("Unable to parse command volume value {} as int", command);
                        }
                    }
                    break;
                case MUTE:
                    debounce = false;
                    synchronized (sequenceLock) {
                        try {
                            if (command == OnOffType.ON && !currentMute) {
                                communicator.sendKeyPress(KeyCommand.MUTETOGGLE.getJson());
                                currentMute = true;
                            } else if (command == OnOffType.OFF && currentMute) {
                                communicator.sendKeyPress(KeyCommand.MUTETOGGLE.getJson());
                                currentMute = false;
                            }
                        } catch (VizioException e) {
                            logger.warn("Unable to send mute {} command to the Vizio TV, Exception: {}", command,
                                    e.getMessage());
                        }
                    }
                    break;
                case SOURCE:
                    debounce = false;
                    synchronized (sequenceLock) {
                        try {
                            // if input changed again before polling has run, get current input hash from the TV
                            // first
                            if (currentInputHash.equals(0L)) {
                                CurrentInput polledInput = communicator.getCurrentInput();
                                if (!polledInput.getItems().isEmpty()) {
                                    currentInputHash = polledInput.getItems().get(0).getHashval();
                                }
                            }
                            communicator
                                    .changeInput(String.format(MODIFY_STRING_SETTING_JSON, command, currentInputHash));
                            currentInputHash = 0L;
                        } catch (VizioException e) {
                            logger.warn("Unable to set current source on the Vizio TV, source: {}, Exception: {}",
                                    command, e.getMessage());
                        }
                    }
                    break;
                case ACTIVE_APP:
                    debounce = false;
                    synchronized (sequenceLock) {
                        try {
                            Optional<VizioApp> selectedApp = userConfigApps.stream()
                                    .filter(a -> command.toString().equals(a.getName())).findFirst();

                            if (selectedApp.isPresent()) {
                                communicator.launchApp(selectedApp.get().getConfig());
                            } else {
                                logger.warn("Unknown app name: '{}', check that it exists in App List configuration",
                                        command);
                            }
                        } catch (VizioException e) {
                            logger.warn("Unable to launch app name: '{}' on the Vizio TV, Exception: {}", command,
                                    e.getMessage());
                        }
                    }
                    break;
                case CONTROL:
                    debounce = false;
                    synchronized (sequenceLock) {
                        try {
                            handleControlCommand(command);
                        } catch (VizioException e) {
                            logger.warn("Unable to send control command: '{}' to the Vizio TV, Exception: {}", command,
                                    e.getMessage());
                        }
                    }
                    break;
                case BUTTON:
                    synchronized (sequenceLock) {
                        try {
                            KeyCommand keyCommand = KeyCommand.valueOf(command.toString().toUpperCase(Locale.ENGLISH));
                            communicator.sendKeyPress(keyCommand.getJson());
                        } catch (IllegalArgumentException | VizioException e) {
                            logger.warn("Unable to send keypress to the Vizio TV, key: {}, Exception: {}", command,
                                    e.getMessage());
                        }
                    }
                    break;
                default:
                    logger.warn("Unknown channel: '{}'", channelUID.getId());
                    break;
            }
        }
    }

    private void handleControlCommand(Command command) throws VizioException {
        if (command instanceof PlayPauseType) {
            if (command == PlayPauseType.PLAY) {
                communicator.sendKeyPress(KeyCommand.PLAY.getJson());
            } else if (command == PlayPauseType.PAUSE) {
                communicator.sendKeyPress(KeyCommand.PAUSE.getJson());
            }
        } else if (command instanceof NextPreviousType) {
            if (command == NextPreviousType.NEXT) {
                communicator.sendKeyPress(KeyCommand.RIGHT.getJson());
            } else if (command == NextPreviousType.PREVIOUS) {
                communicator.sendKeyPress(KeyCommand.LEFT.getJson());
            }
        } else if (command instanceof RewindFastforwardType) {
            if (command == RewindFastforwardType.FASTFORWARD) {
                communicator.sendKeyPress(KeyCommand.SEEKFWD.getJson());
            } else if (command == RewindFastforwardType.REWIND) {
                communicator.sendKeyPress(KeyCommand.SEEKBACK.getJson());
            }
        } else {
            logger.warn("Unknown control command: {}", command);
        }
    }

    @Override
    public boolean isLinked(String channelName) {
        Channel channel = this.thing.getChannel(channelName);
        if (channel != null) {
            return isLinked(channel.getUID());
        } else {
            return false;
        }
    }

    // The remaining methods are used by the console when obtaining the auth token from the TV.
    public int startPairing(String deviceName) throws VizioException {
        Random rng = new Random();
        pairingDeviceId = rng.nextInt(100000);

        pairingToken = communicator.startPairing(deviceName, pairingDeviceId).getItem().getPairingReqToken();

        return pairingToken;
    }

    public String submitPairingCode(String pairingCode) throws IllegalStateException, VizioException {
        if (pairingDeviceId < 0 || pairingToken < 0) {
            throw new IllegalStateException();
        }

        return communicator.submitPairingCode(pairingDeviceId, pairingCode, pairingToken).getItem().getAuthToken();
    }

    public void saveAuthToken(String authToken) {
        pairingDeviceId = -1;
        pairingToken = -1;

        // Store the auth token in the configuration and restart the thing
        Configuration configuration = this.getConfig();
        configuration.put(PROPERTY_AUTH_TOKEN, authToken);
        this.updateConfiguration(configuration);
        this.thingUpdated(this.getThing());
    }
}
