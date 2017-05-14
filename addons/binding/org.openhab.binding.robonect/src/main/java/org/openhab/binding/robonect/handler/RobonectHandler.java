/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect.handler;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.smarthome.core.library.types.DateTimeType;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.robonect.RobonectClient;
import org.openhab.binding.robonect.RobonectCommunicationException;
import org.openhab.binding.robonect.RobonectEndpoint;
import org.openhab.binding.robonect.config.RobonectConfig;
import org.openhab.binding.robonect.model.ErrorEntry;
import org.openhab.binding.robonect.model.MowerInfo;
import org.openhab.binding.robonect.model.MowerStatus;
import org.openhab.binding.robonect.model.Name;
import org.openhab.binding.robonect.model.RobonectAnswer;
import org.openhab.binding.robonect.model.VersionInfo;
import org.openhab.binding.robonect.model.cmd.ModeCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_ERROR_CODE;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_ERROR_DATE;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_ERROR_MESSAGE;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_JOB_AFTER_MODE;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_JOB_END;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_JOB_REMOTE_START;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_JOB_START;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_MOWER_NAME;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_STATUS;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_STATUS_BATTERY;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_STATUS_DURATION;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_STATUS_HOURS;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_STATUS_MODE;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_TIMER_NEXT_TIMER;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_TIMER_STATUS;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_VERSION_COMMENT;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_VERSION_COMPILED;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_VERSION_SERIAL;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_VERSION_VERSION;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_WLAN_SIGNAL;
import static org.openhab.binding.robonect.RobonectBindingConstants.CHANNEL_MOWER_STATUS_STARTED;

/**
 * The {@link RobonectHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(RobonectHandler.class);

    public RobonectHandler(Thing thing) {
        super(thing);
    }

    private ScheduledFuture<?> pollingJob;
    private HttpClient httpClient;
    private RobonectClient robonectClient;

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        try {
            if (command instanceof RefreshType) {
                switch (channelUID.getId()) {
                    case CHANNEL_MOWER_NAME:
                    case CHANNEL_STATUS_BATTERY:
                    case CHANNEL_STATUS:
                    case CHANNEL_STATUS_DURATION:
                    case CHANNEL_STATUS_HOURS:
                    case CHANNEL_STATUS_MODE:
                    case CHANNEL_MOWER_STATUS_STARTED:
                    case CHANNEL_TIMER_NEXT_TIMER:
                    case CHANNEL_TIMER_STATUS:
                    case CHANNEL_WLAN_SIGNAL:
                        refreshMowerInfo();
                        break;
                    case CHANNEL_VERSION_COMMENT:
                    case CHANNEL_VERSION_COMPILED:
                    case CHANNEL_VERSION_SERIAL:
                    case CHANNEL_VERSION_VERSION:
                        refreshVersionInfo();
                        break;

                }
            } else {
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
                    case CHANNEL_MOWER_STATUS_STARTED:
                        if (command instanceof OnOffType) {
                            handleStartStop((OnOffType) command);
                        } else {
                            logger.debug("Got stopped update of type {} but StringType is expected.",
                                    command.getClass().getName());
                        }
                        break;
                    case CHANNEL_JOB_REMOTE_START:
                        if (command instanceof StringType) {
                            setRemoteStartJobSetting(command);
                        } else {
                            logger.debug("Got job remote start update of type {} but StringType is expected.",
                                    command.getClass().getName());
                        }
                        break;
                    case CHANNEL_JOB_AFTER_MODE:
                        if (command instanceof StringType) {
                            setAfterModeJobSetting(command);
                        } else {
                            logger.debug("Got job after mode update of type {} but StringType is expected.",
                                    command.getClass().getName());
                        }
                        break;
                    case CHANNEL_JOB_START:
                        if (command instanceof StringType) {
                            setStartJobSetting(command);
                        } else {
                            logger.debug("Got job start update of type {} but StringType is expected.",
                                    command.getClass().getName());
                        }
                        break;
                    case CHANNEL_JOB_END:
                        if (command instanceof StringType) {
                            setEndJobSetting(command);
                        } else {
                            logger.debug("Got job end update of type {} but StringType is expected.",
                                    command.getClass().getName());
                        }
                        break;
                }

            }
            updateStatus(ThingStatus.ONLINE);
        } catch (RobonectCommunicationException rce){
            logger.error("Failed to communicate with the mower. Taking it offline.", rce);
            updateState(CHANNEL_STATUS, new DecimalType(MowerStatus.OFFLINE.getStatusCode()));
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected exception. Setting thing offline", e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
        }
    }

    private void setMowerMode(Command command) {
        String modeStr = command.toFullString();
        ModeCommand.Mode newMode = ModeCommand.Mode.valueOf(modeStr.toUpperCase());
        robonectClient.setMode(newMode);
        refreshMowerInfo();
    }

    private void setRemoteStartJobSetting(Command command) {
        String remoteStartStr = command.toFullString();
        ModeCommand.RemoteStart remoteStart = ModeCommand.RemoteStart.valueOf(remoteStartStr.toUpperCase());
        robonectClient.getJobSettings().setRemoteStart(remoteStart);
        updateState(CHANNEL_JOB_REMOTE_START, new StringType(robonectClient.getJobSettings().getRemoteStart().name()));
    }

    private void setAfterModeJobSetting(Command command) {
        String afterModeStr = command.toFullString();
        ModeCommand.Mode afterMode = ModeCommand.Mode.valueOf(afterModeStr.toUpperCase());
        robonectClient.getJobSettings().setAfter(afterMode);
        updateState(CHANNEL_JOB_AFTER_MODE, new StringType(robonectClient.getJobSettings().getAfter().name()));
    }

    private void setStartJobSetting(Command command) {
        String start = command.toFullString();
        robonectClient.getJobSettings().setStart(start);
        updateState(CHANNEL_JOB_START, new StringType(robonectClient.getJobSettings().getStart()));
    }

    private void setEndJobSetting(Command command) {
        String end = command.toFullString();
        robonectClient.getJobSettings().setEnd(end);
        updateState(CHANNEL_JOB_END, new StringType(robonectClient.getJobSettings().getEnd()));
    }

    private void logErrorFromResponse(RobonectAnswer result) {
        if (!result.isSuccessful()) {
            logger.error("Could not send EOD Trigger. Robonect error message: {}", result.getErrorMessage());
        }
    }

    private void handleStartStop(OnOffType command) {
        OnOffType started = command;
        RobonectAnswer answer = null;
        boolean currentlyStopped = robonectClient.getMowerInfo().getStatus().isStopped();
        if (started == OnOffType.ON && currentlyStopped) {
            answer = robonectClient.start();
        } else if (!currentlyStopped) {
            answer = robonectClient.stop();
        }
        if (answer != null) {
            logErrorFromResponse(answer);
        }
        refreshMowerInfo();
    }

    private void updateName(StringType command) {
        String newName = command.toFullString();
        Name name = robonectClient.setName(newName);
        if (name.isSuccessful()) {
            updateState(CHANNEL_MOWER_NAME, new StringType(name.getName()));
        } else {
            logErrorFromResponse(name);
        }
        refreshMowerInfo();
    }

    private void refreshMowerInfo() {

        MowerInfo info = robonectClient.getMowerInfo();
        if (info.isSuccessful()) {
            updateState(CHANNEL_MOWER_NAME, new StringType(info.getName()));
            updateState(CHANNEL_STATUS_BATTERY, new DecimalType(info.getStatus().getBattery()));
            updateState(CHANNEL_STATUS, new DecimalType(info.getStatus().getStatus().getStatusCode()));
            updateState(CHANNEL_STATUS_DURATION, new DecimalType(info.getStatus().getDuration()));
            updateState(CHANNEL_STATUS_HOURS, new DecimalType(info.getStatus().getHours()));
            updateState(CHANNEL_STATUS_MODE, new StringType(info.getStatus().getMode().name()));
            updateState(CHANNEL_MOWER_STATUS_STARTED, info.getStatus().isStopped() ? OnOffType.OFF : OnOffType.ON);
            if (info.getTimer() != null) {
                if (info.getTimer().getNext() != null) {
                    updateNextTimer(info);
                }
                updateState(CHANNEL_TIMER_STATUS, new StringType(info.getTimer().getStatus().name()));
            }
            if(info.getError() != null){
                updateErrorInfo(info.getError());
            }else {
                clearErrorInfo();
            }
            updateState(CHANNEL_WLAN_SIGNAL, new DecimalType(info.getWlan().getSignal()));
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
        if(error.getDate() != null){
            State dateTime = convertDateTimeType(error.getDate(), error.getTime());
            updateState(CHANNEL_ERROR_DATE, dateTime);
        }
        if(error.getErrorCode() != null){
            updateState(CHANNEL_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if(error.getErrorMessage() != null){
            updateState(CHANNEL_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
    }

    private void updateNextTimer(MowerInfo info) {
        State dateTime = convertDateTimeType(info.getTimer().getNext().getDate(), info.getTimer().getNext().getTime());
        updateState(CHANNEL_TIMER_NEXT_TIMER, dateTime);
    }

    private State convertDateTimeType(String date, String time) {
        try{
            Date nextTimer = new SimpleDateFormat("dd.MM.yy'T'HH:mm:ss")
                    .parse(date + "T" + time);
            GregorianCalendar calendar = new GregorianCalendar();
            calendar.setTime(nextTimer);
            return new DateTimeType(calendar);
        } catch (ParseException e) {
            logger.error("Could not parse date: {}T{} with pattern 'dd.MM.yy'T'HH:mm:ss'",
                            date, time, e);
            return UnDefType.UNDEF;
            
        }
    }

    private void refreshVersionInfo() {

        VersionInfo info = robonectClient.getVersionInfo();
        if (info.isSuccessful()) {
            updateState(CHANNEL_VERSION_VERSION, new StringType(info.getRobonect().getVersion()));
            updateState(CHANNEL_VERSION_COMPILED, new StringType(info.getRobonect().getCompiled()));
            updateState(CHANNEL_VERSION_COMMENT, new StringType(info.getRobonect().getComment()));
            updateState(CHANNEL_VERSION_SERIAL, new StringType(info.getRobonect().getSerial()));
        } else {
            logger.error("Could not retrieve mower version info. Robonect error response message: {}",
                    info.getErrorMessage());
        }

    }

    @Override
    public void initialize() {
        try {
            RobonectConfig robonectConfig = getConfigAs(RobonectConfig.class);
            RobonectEndpoint endpoint = new RobonectEndpoint(robonectConfig.getHost(), robonectConfig.getUser(),
                    robonectConfig.getPassword());
            httpClient = new HttpClient();
            httpClient.start();
            robonectClient = new RobonectClient(httpClient, endpoint);
            refreshVersionInfo();
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        refreshMowerInfo();
                        updateStatus(ThingStatus.ONLINE);
                    } catch (RobonectCommunicationException rce) {
                        logger.error("Failed to communicate with the mower. Taking it offline.", rce);
                        updateState(CHANNEL_STATUS, new DecimalType(MowerStatus.OFFLINE.getStatusCode()));
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, rce.getMessage());
                    } catch (Exception e) {
                        logger.error("Unexpected exception. Setting thing offline", e);
                        updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.NONE, e.getMessage());
                    }
                }
            };
            int pollInterval = robonectConfig.getPollInterval() > 0 ? robonectConfig.getPollInterval() : 30;
            pollingJob = scheduler.scheduleAtFixedRate(runnable, 0, pollInterval, TimeUnit.SECONDS);

            updateStatus(ThingStatus.ONLINE);

        } catch (Exception e) {
            logger.error("Exception when trying to initialize", e);
            updateStatus(ThingStatus.UNINITIALIZED);

        }

    }

    @Override
    public void dispose() {
        pollingJob.cancel(true);
        try {
            httpClient.stop();
        } catch (Exception e) {
            logger.error("Could not stop http client", e);
        }
    }

    protected ScheduledFuture<?> getPollingJob() {
        return pollingJob;
    }

    protected void setPollingJob(ScheduledFuture<?> pollingJob) {
        this.pollingJob = pollingJob;
    }

    protected HttpClient getHttpClient() {
        return httpClient;
    }

    protected void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    protected RobonectClient getRobonectClient() {
        return robonectClient;
    }

    protected void setRobonectClient(RobonectClient robonectClient) {
        this.robonectClient = robonectClient;
    }
}
