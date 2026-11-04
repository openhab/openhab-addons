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
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.robonect.internal.RobonectClient.JobSettings;
import org.openhab.binding.robonect.internal.model.AjaxInfo;
import org.openhab.binding.robonect.internal.model.DoorInfo;
import org.openhab.binding.robonect.internal.model.ModelParser;
import org.openhab.binding.robonect.internal.model.MowerInfo;
import org.openhab.binding.robonect.internal.model.MowerMode;
import org.openhab.binding.robonect.internal.model.Name;
import org.openhab.binding.robonect.internal.model.RobonectAnswer;
import org.openhab.binding.robonect.internal.model.Status;
import org.openhab.binding.robonect.internal.model.cmd.AjaxCommand;
import org.openhab.binding.robonect.internal.model.cmd.Command;
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

/**
 * API operations for mower, job, door, and service endpoints.
 *
 * @author Nico Brüttner - Initial contribution
 */
@NonNullByDefault
public final class RobonectMowerApi {
    private final RobonectTransport transport;
    private boolean jobRunning;

    /**
     * Creates an endpoint API using the shared transport.
     *
     * @param transport the transport used for requests and parsing
     */
    RobonectMowerApi(RobonectTransport transport) {
        this.transport = transport;
    }

    /**
     * Returns general mower information. See {@link MowerInfo} for the detailed information.
     *
     * @return - the general mower information including a general success status.
     */
    public MowerInfo getMowerInfo() {
        String responseString = transport.sendCommand(new StatusCommand());
        MowerInfo mowerInfo = ModelParser.parse(responseString, MowerInfo.class);
        Status status = mowerInfo.getStatus();
        if (jobRunning && status != null) {
            // mode might have been changed on the mower. Also Mode JOB does not really exist on the mower, thus cannot
            // be checked here
            if (status.getMode() == MowerMode.AUTO || status.getMode() == MowerMode.HOME) {
                jobRunning = false;
            } else if (mowerInfo.getError() != null) {
                jobRunning = false;
            }
        }
        return mowerInfo;
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
     * Sends a mower mode command and clears the locally tracked job state.
     *
     * @param mode - the desired mower mode.
     * @return - a general answer with success status.
     */
    public RobonectAnswer setMode(ModeCommand.Mode mode) {
        String responseString = transport.sendCommand(createCommand(mode));
        if (jobRunning) {
            jobRunning = false;
        }
        return ModelParser.parse(responseString, RobonectAnswer.class);
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
     * Reports the binding's locally tracked job state.
     *
     * @return {@code true} when a job is tracked as running
     */
    public boolean isJobRunning() {
        return jobRunning;
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
            jobRunning = true;
        } else {
            jobRunning = false;
        }
        return answer;
    }

    /**
     * Applies the after-mode if a job is tracked as running.
     *
     * @param settings the settings containing the after-mode
     * @return the command response, or a successful response when no job is tracked
     * @throws NullPointerException if a job is tracked and the after-mode is unset
     */
    public RobonectAnswer stopJob(JobSettings settings) {
        RobonectAnswer answer = null;
        if (jobRunning) {
            answer = setMode(Objects.requireNonNull(settings.getAfterMode()));
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
