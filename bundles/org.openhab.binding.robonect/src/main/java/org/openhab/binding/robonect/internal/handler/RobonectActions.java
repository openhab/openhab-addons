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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.robonect.internal.RobonectClient.JobSettings;
import org.openhab.binding.robonect.internal.model.cmd.DirectCommand;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand.Mode;
import org.openhab.binding.robonect.internal.model.cmd.ModeCommand.RemoteStart;
import org.openhab.core.automation.annotation.ActionInput;
import org.openhab.core.automation.annotation.ActionOutput;
import org.openhab.core.automation.annotation.RuleAction;
import org.openhab.core.thing.binding.ThingActions;
import org.openhab.core.thing.binding.ThingActionsScope;
import org.openhab.core.thing.binding.ThingHandler;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Rule actions for a Robonect mower.
 *
 * @author Nico Brüttner - Initial contribution
 */
@Component(scope = ServiceScope.PROTOTYPE, service = RobonectActions.class)
@ThingActionsScope(name = "robonect")
@NonNullByDefault
public class RobonectActions implements ThingActions {
    private volatile @Nullable RobonectHandler handler;

    /**
     * Associates the action service with its mower.
     *
     * @param handler the Robonect handler, or {@code null} to clear the association
     */
    @Override
    public void setThingHandler(@Nullable ThingHandler handler) {
        this.handler = (RobonectHandler) handler;
    }

    /**
     * Returns the mower handler associated with this action service.
     *
     * @return the handler, or {@code null} if no mower is associated
     */
    @Override
    public @Nullable RobonectHandler getThingHandler() {
        return handler;
    }

    /**
     * Submits a mowing job to the associated mower.
     *
     * @param durationMinutes the duration in minutes from 0 to 1439, or {@code null} to omit it
     * @param startTime the requested start in local {@code HH:mm} format, or {@code null} to omit it
     * @param endTime the requested end in local {@code HH:mm} format, or {@code null} to omit it
     * @param remoteStart the remote start option, or {@code null} to use {@code STANDARD}
     * @param afterMode the mode to apply when the job ends, or {@code null} to leave the choice to the mower
     * @return {@code true} after the mower accepts the request
     * @throws IllegalArgumentException if a supplied setting is invalid
     * @throws IllegalStateException if the mower handler is unavailable or the mower rejects the request
     */
    @RuleAction(label = "@text/action-start-job-label", description = "@text/action-start-job-description")
    public @ActionOutput(name = "success", label = "@text/action-output-start-job-success-label", type = "java.lang.Boolean") boolean startJob(
            @ActionInput(name = "durationMinutes", label = "@text/action-input-duration-minutes-label", description = "@text/action-input-duration-minutes-description", required = false) @Nullable Integer durationMinutes,
            @ActionInput(name = "startTime", label = "@text/action-input-start-time-label", description = "@text/action-input-start-time-description", required = false) @Nullable String startTime,
            @ActionInput(name = "endTime", label = "@text/action-input-end-time-label", description = "@text/action-input-end-time-description", required = false) @Nullable String endTime,
            @ActionInput(name = "remoteStart", label = "@text/action-input-remote-start-label", description = "@text/action-input-remote-start-description", required = false) @Nullable String remoteStart,
            @ActionInput(name = "afterMode", label = "@text/action-input-after-mode-label", description = "@text/action-input-after-mode-description", required = false) @Nullable String afterMode) {
        if (durationMinutes != null && (durationMinutes < 1 || durationMinutes >= 1440)) {
            throw new IllegalArgumentException("durationMinutes must be between 1 and 1439");
        }
        Mode mode = afterMode == null ? null : Mode.valueOf(afterMode);
        if (mode == Mode.JOB) {
            throw new IllegalArgumentException("afterMode must be AUTO, HOME, EOD, or MANUAL");
        }
        JobSettings settings = new JobSettings().withDuration(durationMinutes).withStart(startTime).withEnd(endTime)
                .withRemoteStart(remoteStart == null ? null : RemoteStart.valueOf(remoteStart)).withAfterMode(mode);
        RobonectHandler mower = handler;
        if (mower == null) {
            throw new IllegalStateException("Robonect Thing is unavailable");
        }
        mower.startJob(settings);
        return true;
    }

    /**
     * Sends a timed direct wheel command to the associated mower.
     *
     * @param left the left wheel value from -1000 to 1000
     * @param right the right wheel value from -1000 to 1000
     * @param timeout the duration from 1 to 5000 milliseconds
     * @return {@code true} after the mower accepts the command
     * @throws IllegalArgumentException if a required input is missing or out of range
     * @throws IllegalStateException if the mower is unavailable or rejects the command
     */
    @RuleAction(label = "@text/action-direct-label", description = "@text/action-direct-description")
    public @ActionOutput(name = "success", label = "@text/action-output-direct-success-label", type = "java.lang.Boolean") boolean drive(
            @ActionInput(name = "left", label = "@text/action-input-direct-left-label", description = "@text/action-input-direct-left-description", required = true) @Nullable Integer left,
            @ActionInput(name = "right", label = "@text/action-input-direct-right-label", description = "@text/action-input-direct-right-description", required = true) @Nullable Integer right,
            @ActionInput(name = "timeout", label = "@text/action-input-direct-timeout-label", description = "@text/action-input-direct-timeout-description", required = true) @Nullable Integer timeout) {
        if (left == null || right == null || timeout == null) {
            throw new IllegalArgumentException("Left, right, and timeout are required");
        }
        RobonectHandler mower = handler;
        if (mower == null) {
            throw new IllegalStateException("Robonect Thing is unavailable");
        }
        mower.drive(new DirectCommand(left, right, timeout));
        return true;
    }
}
