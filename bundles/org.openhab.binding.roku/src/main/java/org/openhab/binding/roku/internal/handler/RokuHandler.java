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
package org.openhab.binding.roku.internal.handler;

import static org.openhab.binding.roku.internal.RokuBindingConstants.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.roku.internal.RokuConfiguration;
import org.openhab.binding.roku.internal.RokuHttpException;
import org.openhab.binding.roku.internal.RokuLimitedModeException;
import org.openhab.binding.roku.internal.RokuStateDescriptionOptionProvider;
import org.openhab.binding.roku.internal.communication.RokuCommunicator;
import org.openhab.binding.roku.internal.dto.Apps.App;
import org.openhab.binding.roku.internal.dto.DeviceInfo;
import org.openhab.binding.roku.internal.dto.Player;
import org.openhab.binding.roku.internal.dto.TvChannel;
import org.openhab.binding.roku.internal.dto.TvChannels.Channel;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.StateOption;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RokuHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Michael Lobstein - Initial contribution
 */
@NonNullByDefault
public class RokuHandler extends BaseThingHandler {
    private static final int DEFAULT_REFRESH_PERIOD_SEC = 10;

    private final Logger logger = LoggerFactory.getLogger(RokuHandler.class);
    private final HttpClient httpClient;
    private final RokuStateDescriptionOptionProvider stateDescriptionProvider;

    private @Nullable ScheduledFuture<?> refreshJob;
    private @Nullable ScheduledFuture<?> appListJob;

    private ThingTypeUID thingTypeUID = THING_TYPE_ROKU_PLAYER;
    private RokuCommunicator communicator;
    private DeviceInfo deviceInfo = new DeviceInfo();
    private int refreshInterval = DEFAULT_REFRESH_PERIOD_SEC;
    private boolean tvActive = false;
    private int limitedMode = -1;
    private Map<String, String> appMap = new HashMap<>();

    private Object sequenceLock = new Object();

    public RokuHandler(Thing thing, HttpClient httpClient,
            RokuStateDescriptionOptionProvider stateDescriptionProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.stateDescriptionProvider = stateDescriptionProvider;
        this.communicator = new RokuCommunicator(httpClient, EMPTY, -1);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Roku handler");
        RokuConfiguration config = getConfigAs(RokuConfiguration.class);
        this.thingTypeUID = this.getThing().getThingTypeUID();

        final @Nullable String host = config.hostName;

        if (host != null && !EMPTY.equals(host)) {
            this.communicator = new RokuCommunicator(httpClient, host, config.port);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Host Name must be specified");
            return;
        }

        if (config.refresh >= 1) {
            refreshInterval = config.refresh;
        }

        updateStatus(ThingStatus.UNKNOWN);

        try {
            deviceInfo = communicator.getDeviceInfo();
            thing.setProperty(PROPERTY_MODEL_NAME, deviceInfo.getModelName());
            thing.setProperty(PROPERTY_MODEL_NUMBER, deviceInfo.getModelNumber());
            thing.setProperty(PROPERTY_DEVICE_LOCAITON, deviceInfo.getUserDeviceLocation());
            thing.setProperty(PROPERTY_SERIAL_NUMBER, deviceInfo.getSerialNumber());
            thing.setProperty(PROPERTY_DEVICE_ID, deviceInfo.getDeviceId());
            thing.setProperty(PROPERTY_SOFTWARE_VERSION, deviceInfo.getSoftwareVersion());
            thing.setProperty(PROPERTY_UUID, deviceInfo.getSerialNumber().toLowerCase());
            updateStatus(ThingStatus.ONLINE);
        } catch (RokuHttpException e) {
            logger.debug("Unable to retrieve Roku device-info. Exception: {}", e.getMessage(), e);
        }
        startAutomaticRefresh();
        startAppListRefresh();
    }

    /**
     * Start the job to periodically get status updates from the Roku
     */
    private void startAutomaticRefresh() {
        ScheduledFuture<?> refreshJob = this.refreshJob;
        if (refreshJob == null || refreshJob.isCancelled()) {
            this.refreshJob = scheduler.scheduleWithFixedDelay(this::refreshPlayerState, 0, refreshInterval,
                    TimeUnit.SECONDS);
        }
    }

    /**
     * Get a status update from the Roku and update the channels
     */
    private void refreshPlayerState() {
        synchronized (sequenceLock) {
            String activeAppId = ROKU_HOME_ID;
            try {
                if (thingTypeUID.equals(THING_TYPE_ROKU_TV)) {
                    try {
                        deviceInfo = communicator.getDeviceInfo();
                        String powerMode = deviceInfo.getPowerMode();
                        updateState(POWER_STATE, new StringType(powerMode));
                        updateState(POWER, OnOffType.from(POWER_ON.equalsIgnoreCase(powerMode)));
                    } catch (RokuHttpException e) {
                        logger.debug("Unable to retrieve Roku device-info.", e);
                    }
                }

                activeAppId = communicator.getActiveApp().getApp().getId();

                // 562859 is now reported when on the home screen, reset to -1
                if (ROKU_HOME_ID_562859.equals(activeAppId)) {
                    activeAppId = ROKU_HOME_ID;
                }

                updateState(ACTIVE_APP, new StringType(activeAppId));
                updateState(ACTIVE_APPNAME, new StringType(appMap.get(activeAppId)));

                if (TV_APP.equals(activeAppId)) {
                    tvActive = true;
                } else {
                    if (tvActive) {
                        updateState(SIGNAL_MODE, UnDefType.UNDEF);
                        updateState(SIGNAL_QUALITY, UnDefType.UNDEF);
                        updateState(CHANNEL_NAME, UnDefType.UNDEF);
                        updateState(PROGRAM_TITLE, UnDefType.UNDEF);
                        updateState(PROGRAM_DESCRIPTION, UnDefType.UNDEF);
                        updateState(PROGRAM_RATING, UnDefType.UNDEF);
                    }
                    tvActive = false;
                }
            } catch (RokuHttpException e) {
                logger.debug("Unable to retrieve Roku active-app info. Exception: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                return;
            }

            // On the home app and when using the TV or TV inputs, do not update the play mode or time channels
            // if in limitedMode, keep checking getPlayerInfo to see if the error goes away
            if ((!ROKU_HOME_ID.equals(activeAppId) && !activeAppId.contains(TV_INPUT)) || limitedMode != 0) {
                try {
                    Player playerInfo = communicator.getPlayerInfo();
                    limitedMode = 0;
                    // When nothing playing, 'close' is reported, replace with 'stop'
                    updateState(PLAY_MODE, new StringType(playerInfo.getState().replace(CLOSE, STOP)));
                    updateState(CONTROL,
                            PLAY.equalsIgnoreCase(playerInfo.getState()) ? PlayPauseType.PLAY : PlayPauseType.PAUSE);

                    // Remove non-numeric from string, ie: ' ms'
                    final String positionStr = playerInfo.getPosition().replaceAll(NON_DIGIT_PATTERN, EMPTY);
                    int position = -1;
                    if (!EMPTY.equals(positionStr)) {
                        position = Integer.parseInt(positionStr) / 1000;
                        updateState(TIME_ELAPSED, new QuantityType<>(position, API_SECONDS_UNIT));
                    } else {
                        updateState(TIME_ELAPSED, UnDefType.UNDEF);
                    }

                    final String durationStr = playerInfo.getDuration().replaceAll(NON_DIGIT_PATTERN, EMPTY);
                    int duration = -1;
                    if (!EMPTY.equals(durationStr)) {
                        duration = Integer.parseInt(durationStr) / 1000;
                        updateState(TIME_TOTAL, new QuantityType<>(duration, API_SECONDS_UNIT));
                    } else {
                        updateState(TIME_TOTAL, UnDefType.UNDEF);
                    }

                    if (duration > 0 && position >= 0 && position <= duration) {
                        updateState(END_TIME, new DateTimeType(Instant.now().plusSeconds(duration - position)));
                        updateState(PROGRESS,
                                new PercentType(BigDecimal.valueOf(Math.round(position / (double) duration * 100.0))));
                    } else {
                        updateState(END_TIME, UnDefType.UNDEF);
                        updateState(PROGRESS, UnDefType.UNDEF);
                    }
                } catch (NumberFormatException e) {
                    logger.debug("Unable to parse playerInfo integer value. Exception: {}", e.getMessage());
                } catch (RokuLimitedModeException e) {
                    logger.debug("RokuLimitedModeException: {}", e.getMessage());
                    limitedMode = 1;
                } catch (RokuHttpException e) {
                    logger.debug("Unable to retrieve Roku media-player info. Exception: {}", e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    return;
                }
            } else {
                updateState(PLAY_MODE, UnDefType.UNDEF);
                updateState(TIME_ELAPSED, UnDefType.UNDEF);
                updateState(TIME_TOTAL, UnDefType.UNDEF);
                updateState(END_TIME, UnDefType.UNDEF);
                updateState(PROGRESS, UnDefType.UNDEF);
            }

            if (thingTypeUID.equals(THING_TYPE_ROKU_TV) && tvActive) {
                try {
                    TvChannel tvChannel = communicator.getActiveTvChannel();
                    limitedMode = 0;
                    updateState(ACTIVE_CHANNEL, new StringType(tvChannel.getChannel().getNumber()));
                    updateState(SIGNAL_MODE, new StringType(tvChannel.getChannel().getSignalMode()));
                    updateState(SIGNAL_QUALITY,
                            new QuantityType<>(tvChannel.getChannel().getSignalQuality(), API_PERCENT_UNIT));
                    updateState(CHANNEL_NAME, new StringType(tvChannel.getChannel().getName()));
                    updateState(PROGRAM_TITLE, new StringType(tvChannel.getChannel().getProgramTitle()));
                    updateState(PROGRAM_DESCRIPTION, new StringType(tvChannel.getChannel().getProgramDescription()));
                    updateState(PROGRAM_RATING, new StringType(tvChannel.getChannel().getProgramRatings()));
                } catch (RokuLimitedModeException e) {
                    logger.debug("RokuLimitedModeException: {}", e.getMessage());
                    limitedMode = 1;
                } catch (RokuHttpException e) {
                    logger.debug("Unable to retrieve Roku tv-active-channel info. Exception: {}", e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    return;
                }
            }

            if (limitedMode < 1) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "@text/error.limited");
            }
        }
    }

    /**
     * Start the job to periodically update list of apps installed on the the Roku
     */
    private void startAppListRefresh() {
        ScheduledFuture<?> appListJob = this.appListJob;
        if (appListJob == null || appListJob.isCancelled()) {
            this.appListJob = scheduler.scheduleWithFixedDelay(this::refreshAppList, 10, 600, TimeUnit.SECONDS);
        }
    }

    /**
     * Update the dropdown that lists all apps installed on the Roku
     */
    private void refreshAppList() {
        synchronized (sequenceLock) {
            try {
                List<App> appList = communicator.getAppList();
                Map<String, String> appMap = new HashMap<>();

                List<StateOption> appListOptions = new ArrayList<>();
                // Roku Home will be selected in the drop-down any time an app is not running.
                appListOptions.add(new StateOption(ROKU_HOME_ID, ROKU_HOME));
                appMap.put(ROKU_HOME_ID, ROKU_HOME);

                appList.forEach(app -> {
                    appListOptions.add(new StateOption(app.getId(), app.getValue()));
                    appMap.put(app.getId(), app.getValue());
                });

                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), ACTIVE_APP),
                        appListOptions);

                this.appMap = appMap;
            } catch (RokuHttpException e) {
                logger.debug("Unable to retrieve Roku installed app-list. Exception: {}", e.getMessage(), e);
            }

            if (thingTypeUID.equals(THING_TYPE_ROKU_TV)) {
                try {
                    List<Channel> channelsList = communicator.getTvChannelList();

                    List<StateOption> channelListOptions = new ArrayList<>();
                    channelsList.forEach(channel -> {
                        if (!channel.isUserHidden()) {
                            channelListOptions.add(new StateOption(channel.getNumber(),
                                    channel.getNumber() + " - " + channel.getName()));
                        }
                    });

                    stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), ACTIVE_CHANNEL),
                            channelListOptions);

                } catch (RokuHttpException e) {
                    logger.debug("Unable to retrieve Roku tv-channels. Exception: {}", e.getMessage(), e);
                }
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

        ScheduledFuture<?> appListJob = this.appListJob;
        if (appListJob != null) {
            appListJob.cancel(true);
            this.appListJob = null;
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            logger.debug("Unsupported refresh command: {}", command);
        } else if (channelUID.getId().equals(BUTTON)) {
            synchronized (sequenceLock) {
                try {
                    communicator.keyPress(command.toString());
                } catch (RokuHttpException e) {
                    logger.debug("Unable to send keypress to Roku, key: {}, Exception: {}", command, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        } else if (channelUID.getId().equals(ACTIVE_APP)) {
            synchronized (sequenceLock) {
                try {
                    String appId = command.toString();
                    // Roku Home(-1) is not a real appId, just press the home button instead
                    if (!ROKU_HOME_ID.equals(appId)) {
                        communicator.launchApp(appId);
                    } else {
                        communicator.keyPress(ROKU_HOME_BUTTON);
                    }
                } catch (RokuHttpException e) {
                    logger.debug("Unable to launch app on Roku, appId: {}, Exception: {}", command, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        } else if (channelUID.getId().equals(ACTIVE_CHANNEL)) {
            synchronized (sequenceLock) {
                try {
                    communicator.launchTvChannel(command.toString());
                } catch (RokuHttpException e) {
                    logger.debug("Unable to change channel on Roku TV, channelNumber: {}, Exception: {}", command,
                            e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        } else if (POWER.equals(channelUID.getId())) {
            synchronized (sequenceLock) {
                if (command instanceof OnOffType) {
                    try {
                        if (command.equals(OnOffType.ON)) {
                            communicator.keyPress(POWER_ON);
                        } else {
                            communicator.keyPress("PowerOff");
                        }
                    } catch (RokuHttpException e) {
                        logger.debug("Unable to send keypress to Roku, key: {}, Exception: {}", command,
                                e.getMessage());
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                    }
                }
            }
        } else if (channelUID.getId().equals(CONTROL)) {
            synchronized (sequenceLock) {
                try {
                    if (command instanceof PlayPauseType) {
                        communicator.keyPress(ROKU_PLAY_BUTTON);
                    } else if (command instanceof NextPreviousType) {
                        if (command == NextPreviousType.NEXT) {
                            communicator.keyPress(ROKU_NEXT_BUTTON);
                        } else if (command == NextPreviousType.PREVIOUS) {
                            communicator.keyPress(ROKU_PREV_BUTTON);
                        }
                    } else {
                        logger.warn("Unknown control command: {}", command);
                    }
                } catch (RokuHttpException e) {
                    logger.debug("Unable to send control cmd to Roku, cmd: {}, Exception: {}", command, e.getMessage());
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            }
        } else {
            logger.debug("Unsupported command: {}", command);
        }
    }
}
