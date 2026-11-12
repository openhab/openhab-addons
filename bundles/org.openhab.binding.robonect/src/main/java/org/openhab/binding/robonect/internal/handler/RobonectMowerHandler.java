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
import static org.openhab.binding.robonect.internal.handler.RobonectUtils.read;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
import org.openhab.binding.robonect.internal.model.MowerStatus;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.NextTimer;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.Timer;
import org.openhab.binding.robonect.internal.model.Wlan;
import org.openhab.binding.robonect.internal.model.cmd.DirectCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.openhab.core.config.core.Configuration;
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
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
final class RobonectMowerHandler {
    private static final Pattern TIME_PATTERN = Pattern.compile("^(\\d{1,2}):(\\d{2})$");
    private final Logger logger = LoggerFactory.getLogger(RobonectMowerHandler.class);
    private final RobonectHandler handler;
    private final RobonectMowerApi mowerApi;
    private final RobonectDiagnosticsHandler diagnostics;
    private @Nullable LocalJob localJob;

    record LocalJob(@Nullable String channelId, ZonedDateTime start, ZonedDateTime end) {
    }

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
     * Sends the configured timed wheel command when the custom switch receives ON.
     *
     * @param configuration the required left, right, and timeout channel settings
     * @param command the command received on the switch
     */
    void handleDirectCommand(Configuration configuration, Command command) {
        if (command != OnOffType.ON || !handler.canPublishState()) {
            return;
        }
        DirectCommand direct;
        try {
            int left = readDirectSetting(configuration, "left");
            int right = readDirectSetting(configuration, "right");
            if (left < -100 || left > 100 || right < -100 || right > 100) {
                throw new IllegalArgumentException("Wheel speeds must be between -100 and 100 percent");
            }
            direct = new DirectCommand(left, right, readDirectSetting(configuration, "timeout"));
        } catch (IllegalArgumentException e) {
            logger.debug("Invalid direct wheel channel configuration: {}", e.getMessage());
            return;
        }
        if (!drive(direct).isSuccessful()) {
            logger.debug("Robonect rejected the direct wheel command");
        }
    }

    /**
     * Reads a required integer setting without rounding a fractional value.
     *
     * @param configuration the channel settings
     * @param key the required setting name
     * @return the exact integer value of the setting
     * @throws IllegalArgumentException if the setting is missing or is not a valid integer
     */
    private int readDirectSetting(Configuration configuration, String key) {
        if (configuration.get(key) instanceof Number number) {
            try {
                return new BigDecimal(number.toString()).intValueExact();
            } catch (ArithmeticException | NumberFormatException e) {
                throw new IllegalArgumentException(key + " must be an integer", e);
            }
        }
        throw new IllegalArgumentException(key + " must be an integer");
    }

    /**
     * Sends a timed direct wheel command through the mower API.
     *
     * @param command the requested wheel values and timeout
     * @return the mower's response to the command
     */
    RobonectAnswer drive(DirectCommand command) {
        return mowerApi.drive(command);
    }

    /**
     * Starts or stops a job using the channel configuration, then refreshes mower state.
     *
     * @param channelId the channel identifier
     * @param jobConfig the job channel configuration
     * @param command the command received for the channel
     */
    synchronized void handleJobCommand(String channelId, JobChannelConfig jobConfig, Command command) {
        if (!(command instanceof OnOffType onOffCommand)) {
            logger.debug("Got job update of type {} but OnOffType is expected.", command.getClass().getName());
            return;
        }
        handler.cancelCommandRefresh("status");
        if (onOffCommand == OnOffType.ON) {
            Integer configuredDuration = jobConfig.getDurationMinutes();
            if (configuredDuration != null && (configuredDuration < 1 || configuredDuration >= 1440)) {
                logger.debug("Invalid job duration: {}. Expected a value from 1 to 1439 minutes.", configuredDuration);
                return;
            }
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

            RobonectClient.JobSettings settings = new RobonectClient.JobSettings().withAfterMode(afterMode)
                    .withRemoteStart(remoteStart).withDuration(configuredDuration);
            try {
                settings.withStart(jobConfig.getStartTime()).withEnd(jobConfig.getEndTime());
            } catch (IllegalArgumentException e) {
                logger.debug("Invalid mowing job time: {}", e.getMessage());
                return;
            }
            startJob(channelId, settings);
            return;
        } else if (onOffCommand == OnOffType.OFF) {
            ModeCommand.@Nullable Mode afterMode = switch (jobConfig.getAfterMode()) {
                case "AUTO" -> ModeCommand.Mode.AUTO;
                case "HOME" -> ModeCommand.Mode.HOME;
                case "EOD" -> ModeCommand.Mode.EOD;
                case "MANUAL" -> ModeCommand.Mode.MANUAL;
                case null, default -> null;
            };
            expireLocalJob();
            if (mowerApi.stopJob(afterMode).isSuccessful() && !handler.isAjaxApiEnabled()) {
                // Only when ajax scraping is not active,
                // clear the local job tracking since the job was successfully stopped
                localJob = null;
            }

            // delay refresh by 5 seconds to allow the mower to update its status
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo, 5);

            return;
        } else {
            return;
        }
    }

    /**
     * Submits a job and, while the session is active, records an accepted start and refreshes mower status.
     *
     * @param channelId the originating job channel, or {@code null} for a Thing Action
     * @param settings the requested job settings
     * @return the mower's response to the start command
     */
    synchronized RobonectAnswer startJob(@Nullable String channelId, RobonectClient.JobSettings settings) {
        expireLocalJob();
        RobonectAnswer answer = mowerApi.startJob(settings);
        if (handler.canPublishState()) {
            if (answer.isSuccessful()) {
                localJob = estimateJob(channelId, settings, ZonedDateTime.now(handler.getTimeZone()));
            }
            // delay refresh by 5 seconds to allow the mower to update its status
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo, 5);
        }
        return answer;
    }

    /**
     * Estimates the accepted mowing job window using Robonect's scheduling rules.
     *
     * @param channelId the originating job channel, or {@code null} if none was used
     * @param settings the settings sent to the mower
     * @param now the current time in the mower's configured time zone
     * @return the locally tracked job with its channel and dated start and end times
     */
    static LocalJob estimateJob(@Nullable String channelId, RobonectClient.JobSettings settings, ZonedDateTime now) {
        LocalTime configuredStart = settings.getStartLocalTime();
        LocalTime configuredEnd = settings.getEndLocalTime();
        Integer configuredDuration = settings.getDuration();
        int duration = configuredDuration == null ? 60 : configuredDuration;
        LocalDate today = now.toLocalDate();

        if (configuredStart == null) {
            if (configuredEnd == null) {
                return new LocalJob(channelId, now, now.plusMinutes(duration));
            }
            // An end time on its own always refers to today, even if the job has already expired.
            ZonedDateTime end = today.atTime(configuredEnd).atZone(now.getZone());
            ZonedDateTime start = configuredDuration == null ? now : end.minusMinutes(duration);
            return new LocalJob(channelId, start, end);
        }

        LocalDate startDate = today;
        if (configuredEnd == null) {
            // A start time on its own schedules the next occurrence of that time.
            if (configuredStart.isBefore(now.toLocalTime())) {
                startDate = today.plusDays(1);
            }
            ZonedDateTime start = startDate.atTime(configuredStart).atZone(now.getZone());
            return new LocalJob(channelId, start, start.plusMinutes(duration));
        }

        // Two explicit times define the whole window. A same-day window that has passed moves to tomorrow.
        if (!configuredEnd.isBefore(configuredStart) && configuredEnd.isBefore(now.toLocalTime())) {
            startDate = today.plusDays(1);
        }
        // Earlier end times wrap across midnight. Equal times define a full local day.
        LocalDate endDate = configuredEnd.isAfter(configuredStart) ? startDate : startDate.plusDays(1);
        ZonedDateTime start = startDate.atTime(configuredStart).atZone(now.getZone());
        ZonedDateTime end = endDate.atTime(configuredEnd).atZone(now.getZone());
        return new LocalJob(channelId, start, end);
    }

    /** Clears local job tracking once the estimated end is reached. */
    private void expireLocalJob() {
        LocalJob job = localJob;
        if (!handler.isAjaxApiEnabled() && job != null && !Instant.now().isBefore(job.end().toInstant())) {
            clearLocalJob();
        }
    }

    /** Clears the local job tracking and notifies the mower API. */
    private void clearLocalJob() {
        localJob = null;
        mowerApi.clearTrackedJob();
    }

    /** Publishes the estimated job window or resets the main and custom channels if no local job is tracked. */
    private void updateLocalJobChannels() {
        LocalJob job = localJob;
        handler.updateSwitch(CHANNEL_JOB, job != null, true);
        handler.setState(CHANNEL_JOB_START, job == null ? null : handler.toTimeOnlyDateTime(job.start().toLocalTime()),
                true);
        handler.setState(CHANNEL_JOB_END, job == null ? null : handler.toTimeOnlyDateTime(job.end().toLocalTime()),
                true);
        handler.updateCustomJobChannels(job == null ? null : job.channelId());
    }

    /**
     * Validates, sends a mower mode command, and refreshes mower state.
     *
     * @param command the command received for the channel
     */
    synchronized void handleModeCommand(Command command) {
        if (!(command instanceof StringType)) {
            logger.debug("Got job remote start update of type {} but StringType is expected.",
                    command.getClass().getName());
            return;
        }
        String modeStr = command.toFullString();
        ModeCommand.Mode newMode = ModeCommand.Mode.valueOf(modeStr.toUpperCase());
        handler.cancelCommandRefresh("status");
        expireLocalJob();
        if (mowerApi.setMode(newMode).isSuccessful() && !handler.isAjaxApiEnabled()) {
            localJob = newMode == ModeCommand.Mode.JOB
                    ? estimateJob(null, new RobonectClient.JobSettings(), ZonedDateTime.now(handler.getTimeZone()))
                    : null;
        }

        // delay refresh by 5 seconds to allow the mower to update its status
        handler.scheduleCommandRefresh("status", this::refreshMowerInfo, 5);
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
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo, 5);
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
        if (!(command instanceof StringType stringCommand)) {
            logger.debug("Got service operation command of type {} but StringType is expected.",
                    command.getClass().getName());
            return;
        }
        if (!CHANNEL_SERVICE_OPERATION.equals(channelUID.getId())) {
            return;
        }
        Operation operation;
        switch (stringCommand.toString()) {
            case "SLEEP" -> operation = Operation.SLEEP;
            case "SHUTDOWN" -> operation = Operation.SHUTDOWN;
            case "REBOOT" -> operation = Operation.REBOOT;
            default -> {
                logger.debug("Ignoring unsupported service operation {}.", stringCommand);
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
            handler.scheduleCommandRefresh("door", this::refreshDoorInfo, 5);
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
            handler.scheduleCommandRefresh("status", this::refreshMowerInfo, 5);
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
        MowerInfo info;
        // Keep the status read and job update ordered with job commands.
        synchronized (this) {
            if (!handler.canPublishState()) {
                return null;
            }
            boolean ajax = handler.isAjaxApiEnabled();
            info = mowerApi.getMowerInfo(ajax);
            if (!handler.canPublishState()) {
                return null;
            }
            if (!info.isSuccessful()) {
                logger.debug("Could not retrieve mower information: {}", info.getErrorMessage());
                return null;
            }

            if (ajax) {
                Job job = info.getJob();
                Job activeJob = job != null && Boolean.TRUE.equals(job.isJobActiv()) ? job : null;

                handler.updateSwitch(CHANNEL_JOB, activeJob != null, true);
                updateJobTime(CHANNEL_JOB_START, activeJob == null ? null : activeJob.getJobStart());
                updateJobTime(CHANNEL_JOB_END, activeJob == null ? null : activeJob.getJobEnd());
                if (activeJob == null) {
                    clearLocalJob();
                }
                LocalJob trackedJob = localJob;
                handler.updateCustomJobChannels(trackedJob == null ? null : trackedJob.channelId());
            } else {
                expireLocalJob();
                updateLocalJobChannels();
            }
        }

        if (!handler.canPublishState()) {
            return null;
        }

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
