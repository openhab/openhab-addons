/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikohomecontrol.internal.protocol.nhc1;

import static org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.*;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.nikohomecontrol.internal.protocol.NhcAction;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlCommunication;
import org.openhab.binding.nikohomecontrol.internal.protocol.NikoHomeControlConstants.ActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NhcAction1} class represents the action Niko Home Control I communication object. It contains all fields
 * representing a Niko Home Control action and has methods to trigger the action in Niko Home Control and receive action
 * updates.
 *
 * @author Mark Herwege - Initial Contribution
 */
@NonNullByDefault
public class NhcAction1 extends NhcAction {

    private final Logger logger = LoggerFactory.getLogger(NhcAction1.class);

    @FunctionalInterface
    private interface Action {
        void execute();
    }

    private ScheduledExecutorService scheduler;

    private volatile @Nullable Action rollershutterTask;
    private volatile @Nullable ScheduledFuture<?> rollershutterStopTask;
    private volatile @Nullable ScheduledFuture<?> rollershutterMovingFlagTask;

    private volatile boolean filterEvent = false; // flag to filter first event from rollershutter on percent move to
                                                  // avoid wrong position update
    private volatile boolean rollershutterMoving = false; // flag to indicate if rollershutter is currently moving
    private volatile boolean waitForEvent = false; // flag to wait for position update rollershutter before doing next
                                                   // move

    NhcAction1(String id, String name, ActionType type, @Nullable String location, NikoHomeControlCommunication nhcComm,
            ScheduledExecutorService scheduler) {
        super(id, name, type, location, nhcComm);
        this.scheduler = scheduler;
    }

    /**
     * Sets state of action. This is the version for Niko Home Control I.
     *
     * @param newState - The allowed values depend on the action type.
     *            switch action: 0 or 100
     *            dimmer action: between 0 and 100
     *            rollershutter action: between 0 and 100
     */
    @Override
    public void setState(int newState) {
        if (getType() == ActionType.ROLLERSHUTTER) {
            if (filterEvent) {
                filterEvent = false;
                logger.debug("filtered event {} for {}", newState, id);
                return;
            }

            cancelRollershutterStop();

            if (((newState == 0) || (newState == 100)) && (newState != state)) {
                long duration = rollershutterMoveTime(state, newState);
                setRollershutterMovingTrue(duration);
            } else {
                setRollershutterMovingFalse();
            }
        }
        if (waitForEvent) {
            logger.debug("received requested rollershutter {} position event {}", id, newState);
            executeRollershutterTask();
        } else {
            state = newState;
            updateState();
        }
    }

    /**
     * Sends action to Niko Home Control. This version is used for Niko Home Control I.
     *
     * @param command - The allowed values depend on the action type.
     */
    @Override
    public void execute(String command) {
        logger.debug("execute action {} of type {} for {}", command, type, id);

        String value = "";
        switch (getType()) {
            case GENERIC:
            case TRIGGER:
            case RELAY:
                if (command.equals(NHCON)) {
                    value = "100";
                } else {
                    value = "0";
                }
                nhcComm.executeAction(id, value);
                break;
            case DIMMER:
                if (command.equals(NHCON)) {
                    value = "254";
                } else if (command.equals(NHCOFF)) {
                    value = "255";
                } else {
                    value = command;
                }
                nhcComm.executeAction(id, value);
                break;
            case ROLLERSHUTTER:
                executeRollershutter(command);
                break;
        }
    }

    private void executeRollershutter(String command) {
        if (logger.isTraceEnabled()) {
            logger.trace("handleRollerShutterCommand: rollershutter {} command {}", id, command);
            logger.trace("handleRollerShutterCommand: rollershutter {}, current position {}", id, state);
        }

        // first stop all current movement of rollershutter and wait until exact position is known
        if (rollershutterMoving) {
            if (logger.isTraceEnabled()) {
                logger.trace("handleRollerShutterCommand: rollershutter {} moving, therefore stop", id);
            }
            rollershutterPositionStop();
        }

        // task to be executed once exact position received from Niko Home Control
        rollershutterTask = () -> {
            if (logger.isTraceEnabled()) {
                logger.trace("handleRollerShutterCommand: rollershutter {} task running", id);
            }

            int currentValue = state;

            if (command.equals(NHCDOWN)) {
                executeRollershutterDown();
            } else if (command.equals(NHCUP)) {
                executeRollershutterUp();
            } else if (command.equals(NHCSTOP)) {
                executeRollershutterStop();
            } else {
                int newValue = Integer.parseInt(command);
                if (logger.isTraceEnabled()) {
                    logger.trace("handleRollerShutterCommand: rollershutter {} percent command, current {}, new {}", id,
                            currentValue, newValue);
                }
                if (currentValue == newValue) {
                    return;
                }
                if ((newValue > 0) && (newValue < 100)) {
                    scheduleRollershutterStop(currentValue, newValue);
                }
                if (newValue < currentValue) {
                    executeRollershutterUp();
                } else if (newValue > currentValue) {
                    executeRollershutterDown();
                }
            }
        };

        // execute immediately if not waiting for exact position
        if (!waitForEvent) {
            if (logger.isTraceEnabled()) {
                logger.trace("handleRollerShutterCommand: rollershutter {} task executing immediately", id);
            }
            executeRollershutterTask();
        }
    }

    private void executeRollershutterStop() {
        nhcComm.executeAction(id, "253");
    }

    private void executeRollershutterDown() {
        nhcComm.executeAction(id, "254");
    }

    private void executeRollershutterUp() {
        nhcComm.executeAction(id, "255");
    }

    /**
     * Method used to stop rollershutter when moving. This will then result in an exact position to be received, so next
     * percentage movements could be done accurately.
     */
    private void rollershutterPositionStop() {
        if (logger.isTraceEnabled()) {
            logger.trace("rollershutterPositionStop: rollershutter {} executing", id);
        }
        cancelRollershutterStop();
        rollershutterTask = null;
        filterEvent = false;
        waitForEvent = true;
        executeRollershutterStop();
    }

    private void executeRollershutterTask() {
        if (logger.isTraceEnabled()) {
            logger.trace("executeRollershutterTask: rollershutter {} task triggered", id);
        }
        waitForEvent = false;

        Action action = rollershutterTask;
        if (action != null) {
            action.execute();
            rollershutterTask = null;
        }
    }

    /**
     * Method used to schedule a rollershutter stop when moving. This allows stopping the rollershutter at a percent
     * position.
     *
     * @param currentValue current percent position
     * @param newValue new percent position
     *
     */
    private void scheduleRollershutterStop(int currentValue, int newValue) {
        // filter first event for a rollershutter coming from Niko Home Control if moving to an intermediate
        // position to avoid updating state to full open or full close
        filterEvent = true;

        long duration = rollershutterMoveTime(currentValue, newValue);
        setRollershutterMovingTrue(duration);

        if (logger.isTraceEnabled()) {
            logger.trace("scheduleRollershutterStop: schedule rollershutter {} stop in {}ms", id, duration);
        }
        rollershutterStopTask = scheduler.schedule(() -> {
            logger.trace("scheduleRollershutterStop: run rollershutter {} stop", id);
            executeRollershutterStop();
        }, duration, TimeUnit.MILLISECONDS);
    }

    private void cancelRollershutterStop() {
        ScheduledFuture<?> stopTask = rollershutterStopTask;
        if (stopTask != null) {
            if (logger.isTraceEnabled()) {
                logger.trace("cancelRollershutterStop: cancel rollershutter {} stop", id);
            }
            stopTask.cancel(true);
        }
        rollershutterStopTask = null;

        filterEvent = false;
    }

    private void setRollershutterMovingTrue(long duration) {
        if (logger.isTraceEnabled()) {
            logger.trace("setRollershutterMovingTrue: rollershutter {} moving", id);
        }
        rollershutterMoving = true;
        rollershutterMovingFlagTask = scheduler.schedule(() -> {
            if (logger.isTraceEnabled()) {
                logger.trace("setRollershutterMovingTrue: rollershutter {} stopped moving", id);
            }
            rollershutterMoving = false;
        }, duration, TimeUnit.MILLISECONDS);
    }

    private void setRollershutterMovingFalse() {
        if (logger.isTraceEnabled()) {
            logger.trace("setRollershutterMovingFalse: rollershutter {} not moving", id);
        }
        rollershutterMoving = false;
        ScheduledFuture<?> future = rollershutterMovingFlagTask;
        if (future != null) {
            future.cancel(true);
            rollershutterMovingFlagTask = null;
        }
    }

    private long rollershutterMoveTime(int currentValue, int newValue) {
        int totalTime = (newValue > currentValue) ? getOpenTime() : getCloseTime();
        long duration = Math.abs(newValue - currentValue) * totalTime * 10;
        if (logger.isTraceEnabled()) {
            logger.trace("rollershutterMoveTime: rollershutter {} move time {}", id, duration);
        }
        return duration;
    }
}
