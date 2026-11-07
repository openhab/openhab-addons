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
import static org.openhab.binding.robonect.internal.handler.RobonectHandler.read;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectClient;
import org.openhab.binding.robonect.internal.RobonectMowerApi;
import org.openhab.binding.robonect.internal.config.JobChannelConfig;
import org.openhab.binding.robonect.internal.model.Blades;
import org.openhab.binding.robonect.internal.model.Clock;
import org.openhab.binding.robonect.internal.model.Door;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.DoorState;
import org.openhab.binding.robonect.internal.model.ErrorEntry;
import org.openhab.binding.robonect.internal.model.Health;
import org.openhab.binding.robonect.internal.model.Job;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.Wlan;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.openhab.core.library.types.DateTimeType;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles mower, job, door, and service endpoints.
 *
 * @author Nico Brüttner - Initial mower endpoint handling
 * @author Nico Brüttner - Reported mode fallback when stopping a mowing job
 */
@NonNullByDefault
final class RobonectMowerHandler {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private final Logger logger = LoggerFactory.getLogger(RobonectMowerHandler.class);
    private final RobonectHandler handler;
    private final RobonectMowerApi mowerApi;
    private final RobonectDiagnosticsHandler diagnostics;
    private volatile @Nullable MowerMode latestMowerMode;

    /**
     * Creates a mower endpoint component with access to Thing state and diagnostics.
     *
     * @param handler the {@link RobonectHandler} providing state updates
     * @param mowerApi the mower API for the current client
     * @param diagnostics the diagnostics component used to refresh error information
     */
    RobonectMowerHandler(RobonectHandler handler, RobonectMowerApi mowerApi, RobonectDiagnosticsHandler diagnostics) {
        this.handler = handler;
        this.mowerApi = mowerApi;
        this.diagnostics = diagnostics;
    }

    /** Updates door channels from the optional door-system response. */
    void refreshDoorInfo() {
        applyDoorInfo(mowerApi.getDoorInfo(), true);
    }

    /**
     * Publishes door fields present in a successful door-system response.
     *
     * @param info the door response to apply
     * @param fullResponse whether to clear missing values
     */
    private void applyDoorInfo(DoorInfo info, boolean fullResponse) {
        Door door = info.getDoor();
        if (!info.isSuccessful()) {
            return;
        }
        handler.setState(CHANNEL_DOOR_ENABLED, read(door, Door::getEnabled).map(OnOffType::from).orElse(null),
                fullResponse);
        handler.setState(CHANNEL_DOOR_ARRESTED, read(door, Door::getArrested).map(OnOffType::from).orElse(null),
                fullResponse);
        DoorState state = read(door, Door::getState).orElse(null);
        DoorState pending = read(door, Door::getPending).orElse(null);
        if (fullResponse || state != null || pending != null) {
            updateDoorState(state, pending, fullResponse);
        }
    }

    /**
     * Maps reported and pending door movement to the door channel state.
     *
     * @param state the current door state, or {@code null} if absent
     * @param pending the pending door state, or {@code null} if absent
     * @param fullResponse whether an absent state should be published as undefined
     */
    private void updateDoorState(@Nullable DoorState state, @Nullable DoorState pending, boolean fullResponse) {
        if (pending != null && Boolean.TRUE.equals(pending.getOpen()) && !Boolean.TRUE.equals(pending.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, new PercentType(75), fullResponse);
        } else if (pending != null && !Boolean.TRUE.equals(pending.getOpen())
                && Boolean.TRUE.equals(pending.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, new PercentType(25), fullResponse);
        } else if (state != null && Boolean.TRUE.equals(state.getOpen()) && !Boolean.TRUE.equals(state.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, PercentType.ZERO, fullResponse);
        } else if (state != null && !Boolean.TRUE.equals(state.getOpen()) && Boolean.TRUE.equals(state.getClose())) {
            handler.setState(CHANNEL_DOOR_STATE, PercentType.HUNDRED, fullResponse);
        } else {
            handler.setState(CHANNEL_DOOR_STATE, null, fullResponse);
        }
    }

    /**
     * Validates and applies a mower-name command.
     *
     * @param command the command received for the channel
     */
    void handleNameCommand(Command command) {
        if (command instanceof StringType stringCommand) {
            updateName(stringCommand);
        } else {
            logger.debug("Got name update of type {} but StringType is expected.", command.getClass().getName());
        }
    }

    /**
     * Starts or stops a job using the channel configuration, then refreshes mower state.
     *
     * @param channelUID the channel identifier
     * @param command the command received for the channel
     */
    void handleJobCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof OnOffType onOffCommand)) {
            logger.debug("Got job update of type {} but OnOffType is expected.", command.getClass().getName());
            return;
        }
        handler.cancelCommandRefresh("status");
        JobChannelConfig jobConfig = handler.getThing().getChannel(channelUID.getId()).getConfiguration()
                .as(JobChannelConfig.class);
        if (onOffCommand == OnOffType.ON) {
            String configuredAfterMode = jobConfig.getAfterMode();
            String configuredRemoteStart = jobConfig.getRemoteStart();
            ModeCommand.@Nullable Mode afterMode;
            ModeCommand.@Nullable RemoteStart remoteStart;
            try {
                afterMode = configuredAfterMode == null ? null : ModeCommand.Mode.valueOf(configuredAfterMode);
            } catch (IllegalArgumentException e) {
                logger.debug("Invalid after mode: {}", configuredAfterMode);
                return;
            }
            try {
                remoteStart = configuredRemoteStart == null ? null
                        : ModeCommand.RemoteStart.valueOf(configuredRemoteStart);
            } catch (IllegalArgumentException e) {
                logger.debug("Invalid remote start: {}", configuredRemoteStart);
                return;
            }

            mowerApi.startJob(new RobonectClient.JobSettings().withAfterMode(afterMode).withRemoteStart(remoteStart)
                    .withDuration(jobConfig.getDuration()).withStart(jobConfig.getStart()).withEnd(jobConfig.getEnd()));
        } else if (onOffCommand == OnOffType.OFF) {
            ModeCommand.@Nullable Mode afterMode = switch (jobConfig.getAfterMode()) {
                case "AUTO" -> ModeCommand.Mode.AUTO;
                case "HOME" -> ModeCommand.Mode.HOME;
                case "EOD" -> ModeCommand.Mode.EOD;
                case "MANUAL" -> ModeCommand.Mode.MANUAL;
                case null, default -> null;
            };
            mowerApi.stopJob(afterMode);
        } else {
            return;
        }
        refreshMowerInfo();
    }

    /**
     * Validates, sends a mower mode command, and refreshes mower state.
     *
     * @param command the command received for the channel
     */
    void handleModeCommand(Command command) {
        if (!(command instanceof StringType)) {
            logger.debug("Got job remote start update of type {} but StringType is expected.",
                    command.getClass().getName());
            return;
        }
        String modeStr = command.toFullString();
        ModeCommand.Mode newMode = ModeCommand.Mode.valueOf(modeStr.toUpperCase());
        handler.cancelCommandRefresh("status");
        mowerApi.setMode(newMode);
        refreshMowerInfo();
    }

    /**
     * Logs an unsuccessful mower command response at debug level.
     *
     * @param result the response to inspect for an error
     */
    private void logErrorFromResponse(RobonectAnswer result) {
        if (!result.isSuccessful()) {
            logger.debug("Could not send EOD Trigger. Robonect error message: {}", result.getErrorMessage());
        }
    }

    /** Requests an error reset and schedules a status refresh after success. */
    void resetErrors() {
        handler.cancelCommandRefresh("status");
        RobonectAnswer answer = mowerApi.resetErrors();
        if (answer.isSuccessful()) {
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo);
        } else {
            logger.debug("Could not reset mower errors. Robonect error message: {}", answer.getErrorMessage());
        }
    }

    /**
     * Validates the error-reset switch command before resetting mower errors.
     *
     * @param command the command received for the channel
     */
    void handleResetErrorCommand(Command command) {
        if (command == OnOffType.ON) {
            resetErrors();
        } else {
            logger.debug("Got error reset command {} but On is expected.", command);
        }
    }

    /**
     * Performs a previously validated service operation.
     *
     * @param operation the requested service operation
     */
    void performServiceOperation(Operation operation) {
        RobonectAnswer answer = mowerApi.performServiceOperation(operation);
        if (!answer.isSuccessful()) {
            logger.debug("Could not perform service operation {}. Robonect error message: {}", operation,
                    answer.getErrorMessage());
        }
    }

    /**
     * Validates a service switch command and maps its channel to an operation.
     *
     * @param channelUID the service channel identifier
     * @param command the command received for the channel
     */
    void handleServiceCommand(ChannelUID channelUID, Command command) {
        if (command != OnOffType.ON) {
            logger.debug("Got service operation command {} but On is expected.", command);
            return;
        }
        Operation operation;
        switch (channelUID.getId()) {
            case CHANNEL_SERVICE_REBOOT -> operation = Operation.REBOOT;
            case CHANNEL_SERVICE_SHUTDOWN -> operation = Operation.SHUTDOWN;
            case CHANNEL_SERVICE_SLEEP -> operation = Operation.SLEEP;
            default -> {
                logger.debug("Ignoring unsupported service channel {}.", channelUID.getId());
                return;
            }
        }
        performServiceOperation(operation);
    }

    /**
     * Controls the optional door system.
     *
     * @param command the command received for the channel
     */
    void handleDoorCommand(Command command) {
        handler.cancelCommandRefresh("door");
        DoorInfo answer;
        if (command == UpDownType.UP) {
            answer = mowerApi.setDoorDirection(Direction.OPEN);
        } else if (command == UpDownType.DOWN) {
            answer = mowerApi.setDoorDirection(Direction.CLOSE);
        } else if (command == StopMoveType.STOP) {
            answer = mowerApi.releaseDoor();
        } else {
            logger.debug("Got door command of type {} but Up, Down, or Stop is expected.",
                    command.getClass().getName());
            return;
        }
        if (answer.isSuccessful()) {
            applyDoorInfo(answer, false);
            handler.scheduleCommandRefresh("door", this::refreshDoorInfo);
        } else {
            logger.debug("Could not control the door. Robonect error message: {}", answer.getErrorMessage());
        }
    }

    /**
     * Starts or stops the mower and refreshes its state.
     *
     * @param command the command received for the channel
     */
    void handleStartCommand(Command command) {
        if (!(command instanceof OnOffType onOffCommand)) {
            logger.debug("Got stopped update of type {} but OnOffType is expected.", command.getClass().getName());
            return;
        }
        handler.cancelCommandRefresh("status");
        if (onOffCommand == OnOffType.ON) {
            mowerApi.start();
        } else {
            mowerApi.stop();
        }
        refreshMowerInfo();
    }

    /**
     * Updates the mower name and applies the returned name before a delayed refresh.
     *
     * @param command the command received for the channel
     */
    void updateName(StringType command) {
        String newName = command.toFullString();
        handler.cancelCommandRefresh("status");
        Name name = mowerApi.setName(newName);
        if (name.isSuccessful()) {
            handler.setState(CHANNEL_MOWER_NAME, read(name, Name::getName).map(StringType::new).orElse(null), false);
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo);
        } else {
            logErrorFromResponse(name);
            refreshMowerInfo();
        }
    }

    /**
     * Reads mower status and publishes the available mower and job channels.
     *
     * @return the reported mower status, or {@code null} if unavailable
     */
    @Nullable
    MowerStatus refreshMowerInfo() {
        MowerInfo info = mowerApi.getMowerInfo(handler.isAjaxUiScraping());
        if (!handler.canPublishState()) {
            return null;
        }
        if (!info.isSuccessful()) {
            logger.debug("Could not retrieve mower information: {}", info.getErrorMessage());
            return null;
        }

        handler.updateSwitch(CHANNEL_JOB, read(info, MowerInfo::getJob).map(Job::isJobActiv).orElse(null), true);
        updateJobTime(CHANNEL_JOB_START, read(info, MowerInfo::getJob).map(Job::getJobStart).orElse(null));
        updateJobTime(CHANNEL_JOB_END, read(info, MowerInfo::getJob).map(Job::getJobEnd).orElse(null));

        ErrorEntry error = info.getError();
        updateErrorInfo(error);
        if (error != null) {
            diagnostics.refreshLastErrorInfo();
        }
        Status status = info.getStatus();
        var mowerStatus = read(status, Status::getStatus);
        handler.setState(CHANNEL_STATUS_MODE,
                read(status, Status::getMode).map(Enum::name).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_MOWER_NAME, read(info, MowerInfo::getName).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_MOWER_ID, read(info, MowerInfo::getId).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_STATUS_BATTERY, read(status, Status::getBattery).map(DecimalType::new).orElse(null),
                true);
        handler.setState(CHANNEL_STATUS_PREVENT, read(status, Status::getPrevent).map(DecimalType::new).orElse(null),
                true);
        handler.updateSwitch(CHANNEL_STATUS_HOME, read(status, Status::getHome).orElse(null), true);
        handler.setState(CHANNEL_STATUS, mowerStatus.map(MowerStatus::getStatusCode).map(DecimalType::new).orElse(null),
                true);
        handler.setState(CHANNEL_STATUS_DURATION,
                read(status, Status::getDuration).map(value -> new QuantityType<>(value, Units.SECOND)).orElse(null),
                true);
        handler.setState(CHANNEL_STATUS_DISTANCE,
                read(status, Status::getDistance).map(value -> new QuantityType<>(value, SIUnits.METRE)).orElse(null),
                true);
        handler.setState(CHANNEL_STATUS_HOURS,
                read(status, Status::getHours).map(value -> new QuantityType<>(value, Units.HOUR)).orElse(null), true);
        handler.setState(CHANNEL_MOWER_START,
                read(status, Status::isStopped).map(stopped -> OnOffType.from(!stopped)).orElse(null), true);
        handler.setState(CHANNEL_HEALTH_TEMP, read(info, MowerInfo::getHealth).map(Health::getTemperature)
                .map(value -> new QuantityType<>(value, SIUnits.CELSIUS)).orElse(null), true);
        handler.setState(CHANNEL_HEALTH_HUM, read(info, MowerInfo::getHealth).map(Health::getHumidity)
                .map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null), true);
        handler.setState(CHANNEL_BLADES_QUALITY, read(info, MowerInfo::getBlades).map(Blades::getQuality)
                .map(value -> new QuantityType<>(value, Units.PERCENT)).orElse(null), true);
        handler.setState(CHANNEL_BLADES_REPL_DAYS, read(info, MowerInfo::getBlades).map(Blades::getDays)
                .map(value -> new QuantityType<>(value, Units.DAY)).orElse(null), true);
        handler.setState(CHANNEL_BLADES_USAGE_HOURS, read(info, MowerInfo::getBlades).map(Blades::getHours)
                .map(value -> new QuantityType<>(value, Units.HOUR)).orElse(null), true);
        handler.setState(CHANNEL_TIMESTAMP, read(info, MowerInfo::getClock).map(Clock::getUnix)
                .map(handler::convertUnixToDateTimeType).orElse(null), true);
        handler.setState(CHANNEL_TIMER_NEXT_TIMER, read(info, MowerInfo::getTimer).map(Timer::getNext)
                .map(NextTimer::getUnix).map(handler::convertUnixToDateTimeType).orElse(null), true);
        handler.setState(CHANNEL_TIMER_STATUS,
                read(info, MowerInfo::getTimer).map(Timer::getStatus).map(Enum::name).map(StringType::new).orElse(null),
                true);
        handler.setState(CHANNEL_WLAN_SIGNAL_STRENGTH, read(info, MowerInfo::getWlan).map(Wlan::getSignal)
                .map(value -> new QuantityType<>(value, Units.DECIBEL_MILLIWATTS)).orElse(null), true);
        handler.setState(CHANNEL_WLAN_SIGNAL, read(info, MowerInfo::getWlan).map(Wlan::getSignal)
                .map(this::convertSignalStrength).map(DecimalType::new).orElse(null), true);
        return mowerStatus.orElse(null);
    }

    /**
     * Updates one job time channel using the Unix epoch date in the configured time zone.
     *
     * @param channelId job time channel identifier
     * @param value UI value in {@code HH:mm} format, or {@code null}
     */
    private void updateJobTime(String channelId, @Nullable String value) {
        DateTimeType dateTime = null;
        if (value != null) {
            Matcher matcher = TIME_PATTERN.matcher(value);
            if (matcher.matches()) {
                int hours = Integer.parseInt(matcher.group(1));
                int minutes = Integer.parseInt(matcher.group(2));
                if (hours >= 0 && hours < 24 && minutes >= 0 && minutes < 60) {
                    dateTime = handler.toTimeOnlyDateTime(LocalTime.of(hours, minutes));
                }
            }
        }

        handler.setState(channelId, dateTime, true);
    }

    /**
     * Maps the mower WLAN signal level to the binding signal-strength scale.
     *
     * @param signal the WLAN signal strength
     * @return the signal strength level from 0 through 4
     */
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

    /**
     * Publishes the available fields of the current mower error.
     *
     * @param error the error information to publish
     */
    private void updateErrorInfo(@Nullable ErrorEntry error) {
        handler.setState(CHANNEL_ERROR_MESSAGE,
                read(error, ErrorEntry::getErrorMessage).map(StringType::new).orElse(null), true);
        handler.setState(CHANNEL_ERROR_CODE, read(error, ErrorEntry::getErrorCode).map(DecimalType::new).orElse(null),
                true);
        handler.setState(CHANNEL_ERROR_DATE,
                read(error, ErrorEntry::getUnix).map(handler::convertUnixToDateTimeType).orElse(null), true);
    }
}
