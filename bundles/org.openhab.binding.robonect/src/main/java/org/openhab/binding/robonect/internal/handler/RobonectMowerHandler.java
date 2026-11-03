/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.time.LocalTime;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectCommunicationException;
import org.openhab.binding.robonect.internal.config.JobChannelConfig;
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.Door;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.DoorState;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * Handles mower, job, door, and service endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectMowerHandler {
    private static final Set<String> JOB_TIMER_LABELS = Set.of("Auftrag", "Job", "Travail", "Ordine", "Jobb");
    private static final Set<String> END_OF_DAY_LABELS = Set.of("Feierabend", "End of day", "Fin de journée de travail",
            "Fine lavoro giornata", "Slutet av dagen");
    private static final Pattern JOB_TIME_PATTERN = Pattern.compile("^(?:(\\d{1,2}:\\d{2})\\D+)?(\\d{1,2}:\\d{2})$");
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private final Logger logger = LoggerFactory.getLogger(RobonectMowerHandler.class);
    private final RobonectHandler handler;
    private final RobonectDiagnosticsHandler diagnostics;

    RobonectMowerHandler(RobonectHandler handler, RobonectDiagnosticsHandler diagnostics) {
        this.handler = handler;
        this.diagnostics = diagnostics;
    }

    /** Updates door channels from the optional door-system response. */
    void refreshDoorInfo() {
        applyDoorInfo(handler.getRobonectClient().getDoorInfo());
    }

    private void applyDoorInfo(DoorInfo info) {
        Door door = info.getDoor();
        if (!info.isSuccessful() || door == null) {
            return;
        }
        if (door.getEnabled() != null) {
            handler.setState(CHANNEL_DOOR_ENABLED, OnOffType.from(door.getEnabled()));
        }
        if (door.getArrested() != null) {
            handler.setState(CHANNEL_DOOR_ARRESTED, OnOffType.from(door.getArrested()));
        }
        if (door.getState() != null || door.getPending() != null) {
            updateDoorState(door.getState(), door.getPending());
        }
    }

    private void updateDoorState(@Nullable DoorState state, @Nullable DoorState pending) {
        if (pending != null && Boolean.TRUE.equals(pending.getOpen()) && !Boolean.TRUE.equals(pending.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, new PercentType(75));
        } else if (pending != null && !Boolean.TRUE.equals(pending.getOpen())
                && Boolean.TRUE.equals(pending.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, new PercentType(25));
        } else if (state != null && Boolean.TRUE.equals(state.getOpen()) && !Boolean.TRUE.equals(state.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, PercentType.ZERO);
        } else if (state != null && !Boolean.TRUE.equals(state.getOpen()) && Boolean.TRUE.equals(state.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, PercentType.HUNDRED);
        } else {
            handler.setState(CHANNEL_DOOR_STATE, UnDefType.UNDEF);
        }
    }

    void handleJobCommand(ChannelUID channelUID, OnOffType command) {
        handler.cancelCommandRefresh("status");
        JobChannelConfig jobConfig = handler.getThing().getChannel(channelUID.getId()).getConfiguration()
                .as(JobChannelConfig.class);
        if (command == OnOffType.ON) {
            handler.getRobonectClient().startJob(new RobonectClient.JobSettings()
                    .withAfterMode(ModeCommand.Mode.valueOf(jobConfig.getAfterMode()))
                    .withRemoteStart(ModeCommand.RemoteStart.valueOf(jobConfig.getRemoteStart()))
                    .withDuration(jobConfig.getDuration()).withStart(jobConfig.getStart()).withEnd(jobConfig.getEnd()));
        } else if (command == OnOffType.OFF) {
            handler.getRobonectClient().stopJob(
                    new RobonectClient.JobSettings().withAfterMode(ModeCommand.Mode.valueOf(jobConfig.getAfterMode())));
        } else {
            return;
        }
        refreshMowerInfo();
    }

    void setMowerMode(Command command) {
        String modeStr = command.toFullString();
        ModeCommand.Mode newMode = ModeCommand.Mode.valueOf(modeStr.toUpperCase());
        handler.cancelCommandRefresh("status");
        handler.getRobonectClient().setMode(newMode);
        refreshMowerInfo();
    }

    private void logErrorFromResponse(RobonectAnswer result) {
        if (!result.isSuccessful()) {
            logger.debug("Could not send EOD Trigger. Robonect error message: {}", result.getErrorMessage());
        }
    }

    void resetErrors() {
        handler.cancelCommandRefresh("status");
        RobonectAnswer answer = handler.getRobonectClient().resetErrors();
        if (answer.isSuccessful()) {
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo);
        } else {
            logger.debug("Could not reset mower errors. Robonect error message: {}", answer.getErrorMessage());
        }
    }

    /** Performs a service operation when the corresponding switch is turned on. */
    void performServiceOperation(Command command, Operation operation) {
        if (command != OnOffType.ON) {
            logger.debug("Got service operation command {} but On is expected.", command);
            return;
        }
        RobonectAnswer answer = handler.getRobonectClient().performServiceOperation(operation);
        if (!answer.isSuccessful()) {
            logger.debug("Could not perform service operation {}. Robonect error message: {}", operation,
                    answer.getErrorMessage());
        }
    }

    /** Controls the optional door system. */
    void handleDoorCommand(Command command) {
        handler.cancelCommandRefresh("door");
        DoorInfo answer;
        if (command == UpDownType.UP) {
            answer = handler.getRobonectClient().setDoorDirection(Direction.OPEN);
        } else if (command == UpDownType.DOWN) {
            answer = handler.getRobonectClient().setDoorDirection(Direction.CLOSE);
        } else if (command == StopMoveType.STOP) {
            answer = handler.getRobonectClient().releaseDoor();
        } else {
            logger.debug("Got door command of type {} but Up, Down, or Stop is expected.",
                    command.getClass().getName());
            return;
        }
        if (answer.isSuccessful()) {
            applyDoorInfo(answer);
            handler.scheduleCommandRefresh("door", this::refreshDoorInfo);
        } else {
            logger.debug("Could not control the door. Robonect error message: {}", answer.getErrorMessage());
        }
    }

    void handleStartStop(final OnOffType command) {
        handler.cancelCommandRefresh("status");
        if (command == OnOffType.ON) {
            handler.getRobonectClient().start();
        } else {
            handler.getRobonectClient().stop();
        }
        refreshMowerInfo();
    }

    void updateName(StringType command) {
        String newName = command.toFullString();
        handler.cancelCommandRefresh("status");
        Name name = handler.getRobonectClient().setName(newName);
        if (name.isSuccessful()) {
            if (name.getName() != null) {
                handler.setState(CHANNEL_MOWER_NAME, new StringType(name.getName()));
            }
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo);
        } else {
            logErrorFromResponse(name);
            refreshMowerInfo();
        }
    }

    void refreshMowerInfo() {
        MowerInfo info = handler.getRobonectClient().getMowerInfo();
        if (!handler.canPublishState()) {
            return;
        }
        Status status = info.getStatus();
        if (info.isSuccessful() && status != null) {
            if (info.getError() != null) {
                updateErrorInfo(info.getError());
                diagnostics.refreshLastErrorInfo();
            } else {
                clearErrorInfo();
            }
            handler.setState(CHANNEL_MOWER_NAME, new StringType(info.getName()));
            if (info.getId() != null) {
                handler.setState(CHANNEL_MOWER_ID, new StringType(info.getId()));
            }
            handler.setState(CHANNEL_STATUS_BATTERY, new DecimalType(status.getBattery()));
            Integer prevent = status.getPrevent();
            if (prevent != null) {
                handler.setState(CHANNEL_STATUS_PREVENT, new DecimalType(prevent));
            }
            Boolean home = status.getHome();
            if (home != null) {
                handler.setState(CHANNEL_STATUS_HOME, OnOffType.from(home));
            }
            if (status.getStatus() != null) {
                handler.setState(CHANNEL_STATUS, new DecimalType(status.getStatus().getStatusCode()));
            }
            handler.setState(CHANNEL_STATUS_DURATION, new QuantityType<>(status.getDuration(), Units.SECOND));
            handler.setState(CHANNEL_STATUS_DISTANCE, new QuantityType<>(status.getDistance(), SIUnits.METRE));
            handler.setState(CHANNEL_STATUS_HOURS, new QuantityType<>(status.getHours(), Units.HOUR));
            updateMowerModeAndJob(status.getMode());
            handler.setState(CHANNEL_MOWER_START, OnOffType.from(!status.isStopped()));
            if (info.getHealth() != null) {
                handler.setState(CHANNEL_HEALTH_TEMP,
                        new QuantityType<>(info.getHealth().getTemperature(), SIUnits.CELSIUS));
                handler.setState(CHANNEL_HEALTH_HUM, new QuantityType<>(info.getHealth().getHumidity(), Units.PERCENT));
            }
            if (info.getBlades() != null) {
                handler.setState(CHANNEL_BLADES_QUALITY,
                        new QuantityType<>(info.getBlades().getQuality(), Units.PERCENT));
                handler.setState(CHANNEL_BLADES_REPL_DAYS, new QuantityType<>(info.getBlades().getDays(), Units.DAY));
                handler.setState(CHANNEL_BLADES_USAGE_HOURS,
                        new QuantityType<>(info.getBlades().getHours(), Units.HOUR));
            }
            if (info.getClock() != null && info.getClock().getUnix() != null) {
                handler.setState(CHANNEL_TIMESTAMP, handler.convertUnixToDateTimeType(info.getClock().getUnix()));
            }
            if (info.getTimer() != null) {
                if (info.getTimer().getNext() != null) {
                    updateNextTimer(info);
                }
                if (info.getTimer().getStatus() != null) {
                    handler.setState(CHANNEL_TIMER_STATUS, new StringType(info.getTimer().getStatus().name()));
                }
            }
            if (info.getWlan() != null) {
                Integer signal = info.getWlan().getSignal();
                if (signal != null) {
                    handler.setState(CHANNEL_WLAN_SIGNAL_STRENGTH,
                            new QuantityType<>(signal, Units.DECIBEL_MILLIWATTS));
                    handler.setState(CHANNEL_WLAN_SIGNAL, new DecimalType(convertSignalStrength(signal)));
                } else {
                    handler.setState(CHANNEL_WLAN_SIGNAL, UnDefType.UNDEF);
                    handler.setState(CHANNEL_WLAN_SIGNAL_STRENGTH, UnDefType.UNDEF);
                }
            } else {
                handler.setState(CHANNEL_WLAN_SIGNAL, UnDefType.UNDEF);
                handler.setState(CHANNEL_WLAN_SIGNAL_STRENGTH, UnDefType.UNDEF);
            }
        } else {
            logger.debug("Could not retrieve complete mower information: {}", info.getErrorMessage());
        }
    }

    /**
     * Updates the job and mode channels using the optional localized web-interface response.
     *
     * The public JSON API does not expose end-of-day mode or the active job time window. UI scraping is therefore
     * opt-in and falls back to the JSON API when the UI response changes or cannot be read.
     *
     * @param mowerMode mode reported by the JSON status endpoint
     */
    private void updateMowerModeAndJob(@Nullable MowerMode mowerMode) {
        if (!handler.isAjaxUiScraping()) {
            if (mowerMode != null) {
                handler.setState(CHANNEL_STATUS_MODE, new StringType(mowerMode.name()));
            }
            handler.setState(CHANNEL_JOB, OnOffType.from(handler.getRobonectClient().isJobRunning()));
            handler.setState(CHANNEL_JOB_START, UnDefType.NULL);
            handler.setState(CHANNEL_JOB_END, UnDefType.NULL);
            return;
        }

        try {
            AjaxInfo ajaxInfo = handler.getRobonectClient().getAjaxInfo();
            if (ajaxInfo == null) {
                throw new JsonSyntaxException("Robonect web-interface response is empty");
            }
            String controlText = ajaxInfo.getControlMenuText();
            String timerText = ajaxInfo.getTimerMenuText();
            if (timerText != null && JOB_TIMER_LABELS.contains(timerText)) {
                handler.setState(CHANNEL_JOB, OnOffType.ON);
                updateJobTimes(controlText);
            } else {
                handler.setState(CHANNEL_JOB, OnOffType.OFF);
                handler.setState(CHANNEL_JOB_START, UnDefType.NULL);
                handler.setState(CHANNEL_JOB_END, UnDefType.NULL);
            }
            if (mowerMode != null) {
                String mode = mowerMode == MowerMode.HOME && END_OF_DAY_LABELS.contains(controlText) ? "EOD"
                        : mowerMode.name();
                handler.setState(CHANNEL_STATUS_MODE, new StringType(mode));
            }
        } catch (RobonectCommunicationException | JsonSyntaxException | IllegalArgumentException e) {
            logger.debug("Could not retrieve Robonect web-interface status. Falling back to the JSON API.", e);
            if (mowerMode != null) {
                handler.setState(CHANNEL_STATUS_MODE, new StringType(mowerMode.name()));
            }
            handler.setState(CHANNEL_JOB, OnOffType.from(handler.getRobonectClient().isJobRunning()));
            handler.setState(CHANNEL_JOB_START, UnDefType.NULL);
            handler.setState(CHANNEL_JOB_END, UnDefType.NULL);
        }
    }

    /**
     * Updates the active job start and end channels from the localized menu text.
     *
     * @param controlText localized menu text that contains the job time window
     */
    private void updateJobTimes(@Nullable String controlText) {
        if (controlText == null) {
            handler.setState(CHANNEL_JOB_START, UnDefType.UNDEF);
            handler.setState(CHANNEL_JOB_END, UnDefType.UNDEF);
            return;
        }
        Matcher matcher = JOB_TIME_PATTERN.matcher(controlText);
        if (!matcher.matches()) {
            handler.setState(CHANNEL_JOB_START, UnDefType.UNDEF);
            handler.setState(CHANNEL_JOB_END, UnDefType.UNDEF);
            return;
        }
        updateJobTime(CHANNEL_JOB_START, matcher.group(1));
        updateJobTime(CHANNEL_JOB_END, matcher.group(2));
    }

    /**
     * Updates one job time channel using the Unix epoch date and UTC to represent the time of day.
     *
     * @param channelId job time channel identifier
     * @param value UI value in {@code HH:mm} format, or {@code null}
     */
    private void updateJobTime(String channelId, @Nullable String value) {
        if (value == null) {
            handler.setState(channelId, UnDefType.UNDEF);
            return;
        }
        Matcher matcher = TIME_PATTERN.matcher(value);
        if (!matcher.matches()) {
            handler.setState(channelId, UnDefType.UNDEF);
            return;
        }
        int hours = Integer.parseInt(matcher.group(1));
        int minutes = Integer.parseInt(matcher.group(2));
        if (hours >= 24 || minutes >= 60) {
            handler.setState(channelId, UnDefType.UNDEF);
            return;
        }
        handler.setState(channelId, handler.toTimeOnlyDateTime(LocalTime.of(hours, minutes)));
    }

    private void clearErrorInfo() {
        handler.setState(CHANNEL_ERROR_DATE, UnDefType.UNDEF);
        handler.setState(CHANNEL_ERROR_CODE, UnDefType.UNDEF);
        handler.setState(CHANNEL_ERROR_MESSAGE, UnDefType.UNDEF);
    }

    private int convertSignalStrength(int signal) {
        if (signal >= -70) {
            return 4;
        }
        if (signal >= -78) {
            return 3;
        }
        if (signal >= -87) {
            return 2;
        }
        return signal >= -120 ? 1 : 0;
    }

    private void updateErrorInfo(ErrorEntry error) {
        if (error.getErrorMessage() != null) {
            handler.setState(CHANNEL_ERROR_MESSAGE, new StringType(error.getErrorMessage()));
        }
        if (error.getErrorCode() != null) {
            handler.setState(CHANNEL_ERROR_CODE, new DecimalType(error.getErrorCode().intValue()));
        }
        if (error.getDate() != null) {
            State dateTime = handler.convertUnixToDateTimeType(error.getUnix());
            handler.setState(CHANNEL_ERROR_DATE, dateTime);
        }
    }

    private void updateNextTimer(MowerInfo info) {
        State dateTime = handler.convertUnixToDateTimeType(info.getTimer().getNext().getUnix());
        handler.setState(CHANNEL_TIMER_NEXT_TIMER, dateTime);
    }
}
