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
package org.openhab.binding.roku.internal.handler;

import static org.openhab.binding.roku.internal.RokuBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.roku.internal.RokuConfiguration;
import org.openhab.binding.roku.internal.RokuHttpException;
import org.openhab.binding.roku.internal.RokuStateDescriptionOptionProvider;
import org.openhab.binding.roku.internal.communication.RokuCommunicator;
import org.openhab.binding.roku.internal.dto.Apps.App;
import org.openhab.binding.roku.internal.dto.DeviceInfo;
import org.openhab.binding.roku.internal.dto.Player;
import org.openhab.binding.roku.internal.dto.TvChannel;
import org.openhab.binding.roku.internal.dto.TvChannels.Channel;
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

        if (config.refresh >= 10) {
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
                activeAppId = communicator.getActiveApp().getApp().getId();

                // 562859 is now reported when on the home screen, reset to -1
                if (ROKU_HOME_ID_562859.equals(activeAppId)) {
                    activeAppId = ROKU_HOME_ID;
                }

                updateState(ACTIVE_APP, new StringType(activeAppId));
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
                updateStatus(ThingStatus.ONLINE);
            } catch (RokuHttpException e) {
                logger.debug("Unable to retrieve Roku active-app info. Exception: {}", e.getMessage(), e);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
            }

            // On the home app and when using the TV or TV inputs, do not update the play mode or time channels
            if (!ROKU_HOME_ID.equals(activeAppId) && !activeAppId.contains(TV_INPUT)) {
                try {
                    Player playerInfo = communicator.getPlayerInfo();
                    // When nothing playing, 'close' is reported, replace with 'stop'
                    updateState(PLAY_MODE, new StringType(playerInfo.getState().replaceAll(CLOSE, STOP)));

                    // Remove non-numeric from string, ie: ' ms'
                    String position = playerInfo.getPosition().replaceAll(NON_DIGIT_PATTERN, EMPTY);
                    if (!EMPTY.equals(position)) {
                        updateState(TIME_ELAPSED,
                                new QuantityType<>(Integer.parseInt(position) / 1000, API_SECONDS_UNIT));
                    } else {
                        updateState(TIME_ELAPSED, UnDefType.UNDEF);
                    }

                    String duration = playerInfo.getDuration().replaceAll(NON_DIGIT_PATTERN, EMPTY);
                    if (!EMPTY.equals(duration)) {
                        updateState(TIME_TOTAL,
                                new QuantityType<>(Integer.parseInt(duration) / 1000, API_SECONDS_UNIT));
                    } else {
                        updateState(TIME_TOTAL, UnDefType.UNDEF);
                    }
                } catch (RokuHttpException e) {
                    logger.debug("Unable to retrieve Roku media-player info. Exception: {}", e.getMessage(), e);
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR);
                }
            } else {
                updateState(PLAY_MODE, UnDefType.UNDEF);
                updateState(TIME_ELAPSED, UnDefType.UNDEF);
                updateState(TIME_TOTAL, UnDefType.UNDEF);
            }

            if (thingTypeUID.equals(THING_TYPE_ROKU_TV) && tvActive) {
                try {
                    TvChannel tvChannel = communicator.getActiveTvChannel();
                    updateState(ACTIVE_CHANNEL, new StringType(tvChannel.getChannel().getNumber()));
                    updateState(SIGNAL_MODE, new StringType(tvChannel.getChannel().getSignalMode()));
                    updateState(SIGNAL_QUALITY,
                            new QuantityType<>(tvChannel.getChannel().getSignalQuality(), API_PERCENT_UNIT));
                    updateState(CHANNEL_NAME, new StringType(tvChannel.getChannel().getName()));
                    updateState(PROGRAM_TITLE, new StringType(tvChannel.getChannel().getProgramTitle()));
                    updateState(PROGRAM_DESCRIPTION, new StringType(tvChannel.getChannel().getProgramDescription()));
                    updateState(PROGRAM_RATING, new StringType(tvChannel.getChannel().getProgramRatings()));
                } catch (RokuHttpException e) {
                    logger.debug("Unable to retrieve Roku tv-active-channel info. Exception: {}", e.getMessage(), e);
                }
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

                List<StateOption> appListOptions = new ArrayList<>();
                // Roku Home will be selected in the drop-down any time an app is not running.
                appListOptions.add(new StateOption(ROKU_HOME_ID, ROKU_HOME));

                appList.forEach(app -> {
                    appListOptions.add(new StateOption(app.getId(), app.getValue()));
                });

                stateDescriptionProvider.setStateOptions(new ChannelUID(getThing().getUID(), ACTIVE_APP),
                        appListOptions);

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
        } else {
            logger.debug("Unsupported command: {}", command);
        }
    }
}
