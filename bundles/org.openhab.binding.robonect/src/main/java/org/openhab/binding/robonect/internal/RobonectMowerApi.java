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
package org.openhab.binding.robonect.internal;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectClient.JobSettings;
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.Job;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.cmd.AjaxCommand;
import org.openhab.binding.robonect.internal.model.cmd.Command;
import org.openhab.binding.robonect.internal.model.cmd.DirectCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand;
import org.openhab.binding.robonect.internal.model.cmd.DoorCommand.Direction;
import org.openhab.binding.robonect.internal.model.cmd.ErrorCommand;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand;
import org.openhab.binding.robonect.internal.model.cmd.NameCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand;
import org.openhab.binding.robonect.internal.model.cmd.ServiceCommand.Operation;
import org.openhab.binding.robonect.internal.model.cmd.StartCommand;
import org.openhab.binding.robonect.internal.model.cmd.StatusCommand;
import org.openhab.binding.robonect.internal.model.cmd.StopCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;

/**
 * API operations for mower, job, door, and service endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectMowerApi {

    private static final Set<String> JOB_TIMER_LABELS = Set.of("Auftrag", "Job", "Travail", "Ordine", "Jobb");
    private static final Set<String> END_OF_DAY_LABELS = Set.of("Feierabend", "End of day", "Fin de journée de travail",
            "Fine lavoro giornata", "Slutet av dagen");
    private static final Pattern JOB_TIME_PATTERN = Pattern.compile("^(?:(\\d{1,2}:\\d{2})\\D+)?(\\d{1,2}:\\d{2})$");

    private final Logger logger = LoggerFactory.getLogger(RobonectMowerApi.class);

    private final RobonectTransport transport;
    private boolean jobRunning;
    private @Nullable MowerMode lastKnownMode;
    private @Nullable MowerMode lastModeBeforeJob;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectMowerApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /** Clears the local job used by the stop command. */
    public void clearTrackedJob() {
        jobRunning = false;
        lastModeBeforeJob = null;
    }

    /**
     * Reads general mower status and attaches job information from the selected source.
     *
     * @param isAjaxScrapingEnabled whether to obtain job information and end-of-day mode from the web interface
     * @return the status response, including its success flag and any job information
     */
    public MowerInfo getMowerInfo(boolean isAjaxScrapingEnabled) {
        String responseString = transport.sendCommand(new StatusCommand());
        MowerInfo mowerInfo = ModelParser.parse(responseString, MowerInfo.class);
        Status status = mowerInfo.getStatus();
        if (isAjaxScrapingEnabled) {
            mowerInfo.setJob(null);
            if (mowerInfo.isSuccessful() && status != null) {
                getAdditionalMowerInfo(mowerInfo);
            }
            Job job = mowerInfo.getJob();
            if (job != null && job.isJobActiv() != null) {
                jobRunning = Boolean.TRUE.equals(job.isJobActiv());
            }
        } else {
            Job job = new Job();
            job.setJobActiv(jobRunning);
            mowerInfo.setJob(job);
        }
        lastKnownMode = status == null ? null : status.getMode();
        return mowerInfo;
    }

    /**
     * Enriches a successful status response with job information and end-of-day mode from the web interface.
     *
     * @param mowerInfo the response to update, with a non-null status
     */
    private void getAdditionalMowerInfo(MowerInfo mowerInfo) {
        try {
            AjaxInfo ajaxInfo = getAjaxInfo();
            if (ajaxInfo == null) {
                throw new JsonSyntaxException("Robonect web-interface response is empty");
            }
            String controlText = ajaxInfo.getControlMenuText();
            String timerText = ajaxInfo.getTimerMenuText();

            MowerMode apiMowerMode = mowerInfo.getStatus().getMode();
            Job job = new Job();
            if (apiMowerMode == MowerMode.HOME && controlText != null && END_OF_DAY_LABELS.contains(controlText)) {
                mowerInfo.getStatus().setMode(MowerMode.EOD);
            }
            if ((apiMowerMode == MowerMode.AUTO || apiMowerMode == MowerMode.HOME) && timerText != null
                    && !timerText.isBlank()) {
                if (JOB_TIMER_LABELS.contains(timerText)) {
                    job.setJobActiv(true);

                    if (controlText != null) {
                        Matcher matcher = JOB_TIME_PATTERN.matcher(controlText);
                        if (matcher.matches()) {
                            job.setJobStart(matcher.group(1));
                            job.setJobEnd(matcher.group(2));
                        }
                    }

                } else {
                    job.setJobActiv(false);
                }
            } else if ((apiMowerMode == MowerMode.HOME || apiMowerMode == MowerMode.MANUAL
                    || apiMowerMode == MowerMode.DEMO) && (controlText != null || timerText != null)) {
                job.setJobActiv(false);
            }

            mowerInfo.setJob(job);
        } catch (RobonectCommunicationException | JsonSyntaxException | IllegalArgumentException e) {
            logger.debug("Could not retrieve Robonect web-interface status. Falling back to the JSON API.", e);
        }
    }

    /**
     * Reads the localized Ajax status information used by the Robonect web interface.
     *
     * @return the parsed Ajax information
     */
    public AjaxInfo getAjaxInfo() {
        String response = transport.sendCommand(new AjaxCommand(1, 2), transport.ajaxUrl);
        return ModelParser.parse(URLDecoder.decode(response, StandardCharsets.ISO_8859_1), AjaxInfo.class);
    }

    /**
     * Sends a start command to the mower.
     *
     * @return - a general answer with success status.
     */
    public RobonectAnswer start() {
        String responseString = transport.sendCommand(new StartCommand());
        return ModelParser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * Sends a stop command to the mower.
     *
     * @return - a general answer with success status.
     */
    public RobonectAnswer stop() {
        String responseString = transport.sendCommand(new StopCommand());
        return ModelParser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * Sends a timed direct wheel command to the mower.
     *
     * @param command the requested wheel values and timeout
     * @return the mower's response to the command
     */
    public RobonectAnswer drive(DirectCommand command) {
        return ModelParser.parse(transport.sendCommand(command), RobonectAnswer.class);
    }

    /**
     * Resets the errors on the mower.
     *
     * @return - a general answer with success status.
     */
    public RobonectAnswer resetErrors() {
        String responseString = transport.sendCommand(new ErrorCommand().withReset(true));
        return ModelParser.parse(responseString, RobonectAnswer.class);
    }

    /**
     * Changes the Robonect module's operating state.
     *
     * @param operation the requested service operation
     * @return the operation result including its success status
     */
    public RobonectAnswer performServiceOperation(Operation operation) {
        return ModelParser.parse(transport.sendCommand(new ServiceCommand(operation)), RobonectAnswer.class);
    }

    /**
     * Returns the state of the optional door system.
     *
     * @return the door information including its success status
     */
    public DoorInfo getDoorInfo() {
        return ModelParser.parse(transport.sendCommand(new DoorCommand()), DoorInfo.class);
    }

    /**
     * Opens or closes the optional door system.
     *
     * @param direction the requested movement direction
     * @return the door information including its success status
     */
    public DoorInfo setDoorDirection(Direction direction) {
        return ModelParser.parse(transport.sendCommand(new DoorCommand().withDirection(direction)), DoorInfo.class);
    }

    /**
     * Releases an arrested optional door system.
     *
     * @return the door information including its success status
     */
    public DoorInfo releaseDoor() {
        return ModelParser.parse(transport.sendCommand(new DoorCommand().withRelease()), DoorInfo.class);
    }

    /**
     * Sends a mower mode command and updates job tracking on success. Selecting any mode other than
     * {@link ModeCommand.Mode#JOB} clears the running flag.
     *
     * @param mode the desired mower mode
     * @return the mower's response to the mode command
     */
    public RobonectAnswer setMode(ModeCommand.Mode mode) {
        String responseString = transport.sendCommand(createCommand(mode));
        RobonectAnswer answer = ModelParser.parse(responseString, RobonectAnswer.class);

        if (answer.isSuccessful()) {
            jobRunning = mode == ModeCommand.Mode.JOB;
        }
        return answer;
    }

    /**
     * Creates a command for the requested mower mode.
     *
     * @param mode the requested mower mode
     * @return the mode command
     */
    private ModeCommand createCommand(ModeCommand.Mode mode) {
        return new ModeCommand(mode);
    }

    /**
     * Returns the name of the mower.
     *
     * @return - The name including a general answer with success status.
     */
    public Name getName() {
        String responseString = transport.sendCommand(new NameCommand());
        return ModelParser.parse(responseString, Name.class);
    }

    /**
     * Allows to set the name of the mower.
     *
     * @param name - the desired name.
     * @return - The resulting name including a general answer with success status.
     */
    public Name setName(String name) {
        String responseString = transport.sendCommand(new NameCommand().withNewName(name));
        return ModelParser.parse(responseString, Name.class);
    }

    /**
     * Starts a job and updates the locally tracked job state.
     *
     * @param settings the requested job settings
     * @return the command response and its success status
     */
    public RobonectAnswer startJob(JobSettings settings) {

        Command jobCommand = new ModeCommand(ModeCommand.Mode.JOB).withRemoteStart(settings.getRemoteStart())
                .withAfter(settings.getAfterMode()).withDuration(settings.getDuration()).withStart(settings.getStart())
                .withEnd(settings.getEnd());
        String responseString = transport.sendCommand(jobCommand);
        RobonectAnswer answer = ModelParser.parse(responseString, RobonectAnswer.class);
        if (answer.isSuccessful()) {
            if (!jobRunning) {
                lastModeBeforeJob = lastKnownMode;
            }
            jobRunning = true;
        }
        return answer;
    }

    /**
     * Applies the after-mode if a job is tracked as running.
     *
     * @param afterMode the after-mode to apply, or {@code null} to restore the pre-job mode and fall back to
     *            {@link ModeCommand.Mode#HOME} if that mode is unavailable or unsupported
     * @return the command response, or a successful response when no job is tracked
     */
    public RobonectAnswer stopJob(ModeCommand.@Nullable Mode afterMode) {
        RobonectAnswer answer = null;
        if (jobRunning) {
            if (afterMode == null) {
                afterMode = switch (lastModeBeforeJob) {
                    case AUTO -> ModeCommand.Mode.AUTO;
                    case HOME -> ModeCommand.Mode.HOME;
                    case EOD -> ModeCommand.Mode.EOD;
                    case MANUAL -> ModeCommand.Mode.MANUAL;
                    case null, default -> ModeCommand.Mode.HOME;
                };
            }
            answer = setMode(afterMode);
            if (answer.isSuccessful()) {
                jobRunning = false;
            }
        } else {
            answer = new RobonectAnswer();
            // this is not an error, thus return success
            answer.setSuccessful(true);
        }
        return answer;
    }
}
