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
package org.openhab.binding.robonect.internal.handler;

import static org.openhab.binding.robonect.internal.RobonectBindingConstants.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.RobonectEndpoint;
import org.openhab.binding.robonect.internal.config.JobChannelConfig;
import org.openhab.binding.robonect.internal.config.RobonectConfig;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.ErrorList;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.VersionInfo;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * The {@link RobonectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * The channels are periodically updated by polling the mower via HTTP in a separate thread.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectHandler.class);

    private ScheduledFuture<?> pollingJob;

    private HttpClient httpClient;
    private TimeZoneProvider timeZoneProvider;

    private ZoneId timeZone;

    private RobonectClient robonectClient;

    public RobonectHandler(Thing thing, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(thing);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                refreshChannels(channelUID);
            } else {
                sendCommand(channelUID, command);
            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RobonectCommunicationException rce) {
            logger.debug("Failed to communicate with the mower. Taking it offline.", rce);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
        }
    }

    private void sendCommand(ChannelUID channelUID, Command command) {
        switch (channelUID.getId()) {
            case CHANNEL_MOWER_NAME:
                if (command instanceof StringType) {
                    updateName((StringType) command);
                } else {
                    logger.debug("Got name update of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_STATUS_MODE:
                if (command instanceof StringType) {
                    setMowerMode(command);
                } else {
                    logger.debug("Got job remote start update of type {} but StringType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_MOWER_START:
                if (command instanceof OnOffType) {
                    handleStartStop((OnOffType) command);
                } else {
                    logger.debug("Got stopped update of type {} but OnOffType is expected.",
                            command.getClass().getName());
                }
                break;

            case CHANNEL_JOB:
                if (command instanceof OnOffType) {
                    handleJobCommand(channelUID, command);
                } else {
                    logger.debug("Got job update of type {} but OnOffType is expected.", command.getClass().getName());
                }
                break;
        }
    }

    private void handleJobCommand(ChannelUID channelUID, Command command) {
        JobChannelConfig jobConfig = getThing().getChannel(channelUID.getId()).getConfiguration()
                .as(JobChannelConfig.class);
        if (command == OnOffType.ON) {
            robonectClient.startJob(
                    new RobonectClient.JobSettings().withAfterMode(ModeCommand.Mode.valueOf(jobConfig.getAfterMode()))
                            .withRemoteStart(ModeCommand.RemoteStart.valueOf(jobConfig.getRemoteStart()))
                            .withDuration(jobConfig.getDuration()));
        } else if (command == OnOffType.OFF) {
            robonectClient.stopJob(
                    new RobonectClient.JobSettings().withAfterMode(ModeCommand.Mode.valueOf(jobConfig.getAfterMode())));
        }
    }

    private void refreshChannels(ChannelUID channelUID) {
        switch (channelUID.getId()) {
            case CHANNEL_MOWER_NAME:
            case CHANNEL_STATUS_BATTERY:
            case CHANNEL_STATUS:
            case CHANNEL_STATUS_DURATION:
            case CHANNEL_STATUS_HOURS:
            case CHANNEL_STATUS_MODE:
            case CHANNEL_MOWER_START:
            case CHANNEL_TIMER_NEXT_TIMER:
            case CHANNEL_TIMER_STATUS:
            case CHANNEL_WLAN_SIGNAL:
            case CHANNEL_JOB:
                refreshMowerInfo();
                break;
            default:
            case CHANNEL_LAST_ERROR_CODE:
            case CHANNEL_LAST_ERROR_DATE:
            case CHANNEL_LAST_ERROR_MESSAGE:
                refreshLastErrorInfo();
                break;
        }
    }

    private void setMowerMode(Command command) {
        String modeStr = command.toFullString();
        ModeCommand.Mode newMode = ModeCommand.Mode.valueOf(modeStr.toUpperCase());
        if (robonectClient.setMode(newMode).isSuccessful()) {
            updateState(CHANNEL_STATUS_MODE, new StringType(newMode.name()));
        } else {
            refreshMowerInfo();
        }
    }

    private void logErrorFromResponse(RobonectAnswer result) {
        if (!result.isSuccessful()) {
            logger.debug("Could not send EOD Trigger. Robonect error message: {}", result.getErrorMessage());
        }
    }

    private void handleStartStop(final OnOffType command) {
        RobonectAnswer answer = null;
        boolean currentlyStopped = robonectClient.getMowerInfo().getStatus().isStopped();
        if (command == OnOffType.ON && currentlyStopped) {
            answer = robonectClient.start();
        } else if (command == OnOffType.OFF && !currentlyStopped) {
            answer = robonectClient.stop();
        }
        if (answer != null) {
            if (answer.isSuccessful()) {
                updateState(CHANNEL_MOWER_START, command);
            } else {
                logErrorFromResponse(answer);
                refreshMowerInfo();
            }
        }
    }

    private void updateName(StringType command) {
        String newName = command.toFullString();
        Name name = robonectClient.setName(newName);
        if (name.isSuccessful()) {
            updateState(CHANNEL_MOWER_NAME, new StringType(name.getName()));
        } else {
            logErrorFromResponse(name);
            refreshMowerInfo();
        }
    }

    private void refreshMowerInfo() {
        MowerInfo info = robonectClient.getMowerInfo();
        if (info.isSuccessful()) {
            if (info.getError() != null) {
                updateErrorInfo(info.getError());
                refreshLastErrorInfo();
            } else {
                clearErrorInfo();
            }
            updateState(CHANNEL_MOWER_NAME, new StringType(info.getName()));
            updateState(CHANNEL_STATUS_BATTERY, new DecimalType(info.getStatus().getBattery()));
            updateState(CHANNEL_STATUS, new DecimalType(info.getStatus().getStatus().getStatusCode()));
            updateState(CHANNEL_STATUS_DURATION, new QuantityType<>(info.getStatus().getDuration(), Units.SECOND));
            updateState(CHANNEL_STATUS_DISTANCE, new QuantityType<>(info.getStatus().getDistance(), SIUnits.METRE));
            updateState(CHANNEL_STATUS_HOURS, new QuantityType<>(info.getStatus().getHours(), Units.HOUR));
            updateState(CHANNEL_STATUS_MODE, new StringType(info.getStatus().getMode().name()));
            updateState(CHANNEL_MOWER_START, info.getStatus().isStopped() ? OnOffType.OFF : OnOffType.ON);
            if (info.getHealth() != null) {
                updateState(CHANNEL_HEALTH_TEMP,
                        new QuantityType<>(info.getHealth().getTemperature(), SIUnits.CELSIUS));
                updateState(CHANNEL_HEALTH_HUM, new QuantityType(info.getHealth().getHumidity(), Units.PERCENT));
            }
            if (info.getTimer() != null) {
                if (info.getTimer().getNext() != null) {
                    updateNextTimer(info);
                }
                updateState(CHANNEL_TIMER_STATUS, new StringType(info.getTimer().getStatus().name()));
            }
            updateState(CHANNEL_WLAN_SIGNAL, new DecimalType(info.getWlan().getSignal()));
            updateState(CHANNEL_JOB, robonectClient.isJobRunning() ? OnOffType.ON : OnOffType.OFF);
        } else {
            logger.error("Could not retrieve mower info. Robonect error response message: {}", info.getErrorMessage());
        }
    }

    private void clearErrorInfo() {
        updateState(CHANNEL_ERROR_DATE, UnDefType.UNDEF);
        updateState(CHANNEL_ERROR_CODE, UnDefType.UNDEF);
        updateState(CHANNEL_ERROR_MESSAGE, UnDefType.UNDEF);
    }

    private void updateErrorInfo(ErrorEntry error) {
        if (error.getErrorMessage() != null) {
            updateState(CHANNEL_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
        if (error.getErrorCode() != null) {
            updateState(CHANNEL_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if (error.getDate() != null) {
            State dateTime = convertUnixToDateTimeType(error.getUnix());
            updateState(CHANNEL_ERROR_DATE, dateTime);
        }
    }

    private void updateNextTimer(MowerInfo info) {
        State dateTime = convertUnixToDateTimeType(info.getTimer().getNext().getUnix());
        updateState(CHANNEL_TIMER_NEXT_TIMER, dateTime);
    }

    private State convertUnixToDateTimeType(String unixTimeSec) {
        // the value in unixTimeSec represents the time on the robot in its configured timezone. However, it does not
        // provide which zone this is. Thus we have to add the zone information from the Thing configuration in order to
        // provide correct results.
        Instant rawInstant = Instant.ofEpochMilli(Long.valueOf(unixTimeSec) * 1000);

        ZoneId timeZoneOfThing = timeZone;
        if (timeZoneOfThing == null) {
            timeZoneOfThing = timeZoneProvider.getTimeZone();
        }
        ZoneOffset offsetToConfiguredZone = timeZoneOfThing.getRules().getOffset(rawInstant);
        long adjustedTime = rawInstant.getEpochSecond() - offsetToConfiguredZone.getTotalSeconds();
        Instant adjustedInstant = Instant.ofEpochMilli(adjustedTime * 1000);

        // we provide the time in the format as configured in the openHAB settings
        ZonedDateTime zdt = adjustedInstant.atZone(timeZoneProvider.getTimeZone());
        return new DateTimeType(zdt);
    }

    private void refreshVersionInfo() {
        VersionInfo info = robonectClient.getVersionInfo();
        if (info.isSuccessful()) {
            Map<String, String> properties = editProperties();
            properties.put(Thing.PROPERTY_SERIAL_NUMBER, info.getRobonect().getSerial());
            properties.put(Thing.PROPERTY_FIRMWARE_VERSION, info.getRobonect().getVersion());
            properties.put(PROPERTY_COMPILED, info.getRobonect().getCompiled());
            properties.put(PROPERTY_COMMENT, info.getRobonect().getComment());
            updateProperties(properties);
        } else {
            logger.debug("Could not retrieve mower version info. Robonect error response message: {}",
                    info.getErrorMessage());
        }
    }

    private void refreshLastErrorInfo() {
        ErrorList errorList = robonectClient.errorList();
        if (errorList.isSuccessful()) {
            List<ErrorEntry> errors = errorList.getErrors();
            if (errors != null && !errors.isEmpty()) {
                ErrorEntry lastErrorEntry = errors.get(0);
                updateLastErrorChannels(lastErrorEntry);
            }
        } else {
            logger.debug("Could not retrieve mower error list. Robonect error response message: {}",
                    errorList.getErrorMessage());
        }
    }

    private void updateLastErrorChannels(ErrorEntry error) {
        if (error.getErrorMessage() != null) {
            updateState(CHANNEL_LAST_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
        if (error.getErrorCode() != null) {
            updateState(CHANNEL_LAST_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if (error.getDate() != null) {
            State dateTime = convertUnixToDateTimeType(error.getUnix());
            updateState(CHANNEL_LAST_ERROR_DATE, dateTime);
        }
    }

    @Override
    public void initialize() {
        RobonectConfig robonectConfig = getConfigAs(RobonectConfig.class);
        RobonectEndpoint endpoint = new RobonectEndpoint(robonectConfig.getHost(), robonectConfig.getUser(),
                robonectConfig.getPassword());

        String timeZoneString = robonectConfig.getTimezone();
        try {
            if (timeZoneString != null) {
                timeZone = ZoneId.of(timeZoneString);
            } else {
                logger.warn("No timezone provided, falling back to the default timezone configured in openHAB: '{}'",
                        timeZoneProvider.getTimeZone());
            }
        } catch (DateTimeException e) {
            logger.warn("Error setting timezone '{}', falling back to the default timezone configured in openHAB: '{}'",
                    timeZoneString, timeZoneProvider.getTimeZone(), e);
        }

        try {
            httpClient.start();
            robonectClient = new RobonectClient(httpClient, endpoint);
        } catch (Exception e) {
            logger.error("Exception while trying to start http client", e);
            throw new RuntimeException("Exception while trying to start http client", e);
        }
        Runnable runnable = new MowerChannelPoller(TimeUnit.SECONDS.toMillis(robonectConfig.getOfflineTimeout()));
        int pollInterval = robonectConfig.getPollInterval();
        pollingJob = scheduler.scheduleWithFixedDelay(runnable, 0, pollInterval, TimeUnit.SECONDS);
    }

    @Override
    public void dispose() {
        if (pollingJob != null) {
            pollingJob.cancel(true);
            pollingJob = null;
        }

        httpClient = null;
    }

    /**
     * method to inject the robonect client to be used in test cases to allow mocking.
     *
     * @param robonectClient
     */
    protected void setRobonectClient(RobonectClient robonectClient) {
        this.robonectClient = robonectClient;
    }

    private class MowerChannelPoller implements Runnable {

        private long offlineSince;
        private long offlineTriggerDelay;
        private boolean offlineTimeoutTriggered;
        private boolean loadVersionInfo = true;

        public MowerChannelPoller(long offlineTriggerDelay) {
            offlineSince = -1;
            this.offlineTriggerDelay = offlineTriggerDelay;
            offlineTimeoutTriggered = false;
        }

        @Override
        public void run() {
            try {
                if (loadVersionInfo) {
                    refreshVersionInfo();
                    loadVersionInfo = false;
                }
                refreshMowerInfo();
                updateStatus(ThingStatus.ONLINE);
                offlineSince = -1;
                offlineTimeoutTriggered = false;
            } catch (RobonectCommunicationException rce) {
                if (offlineSince < 0) {
                    offlineSince = System.currentTimeMillis();
                }
                if (!offlineTimeoutTriggered && System.currentTimeMillis() - offlineSince > offlineTriggerDelay) {
                    // trigger offline
                    updateState(CHANNEL_MOWER_STATUS_OFFLINE_TRIGGER, new StringType("OFFLINE_TIMEOUT"));
                    offlineTimeoutTriggered = true;
                }
                logger.debug("Failed to communicate with the mower. Taking it offline.", rce);
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
                loadVersionInfo = true;
            } catch (JsonSyntaxException jse) {
                // the module sporadically sends invalid json responses. As this is usually recovered with the
                // next poll interval, we just log it to debug here.
                logger.debug("Failed to parse response.", jse);
            }
        }
    }
}
