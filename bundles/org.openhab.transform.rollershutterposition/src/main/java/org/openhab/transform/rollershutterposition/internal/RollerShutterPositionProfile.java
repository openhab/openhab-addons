/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.transform.rollershutterposition.internal;

import static org.openhab.transform.rollershutterposition.internal.RollerShutterPositionConstants.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfile;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.transform.rollershutterposition.internal.config.RollerShutterPositionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile to implement the RollerShutterPosition ItemChannelLink
 *
 * @author Jeff James - Initial contribution
 *
 *         Core logic in this module has been heavily adapted from Tarag Gautier js script implementation
 *         VASRollershutter.js
 */
@NonNullByDefault
public class RollerShutterPositionProfile implements StateProfile {
    private static final String PROFILE_THREADPOOL_NAME = "profile-rollershutterposition";
    private final Logger logger = LoggerFactory.getLogger(RollerShutterPositionProfile.class);

    private final ProfileCallback callback;
    RollerShutterPositionConfig configuration;

    private int position = 0; // current position of the roller shutter (assumes 0 when system starts)
    private int targetPosition = -1;
    private boolean isValidConfiguration = false;
    private Instant movingSince = Instant.MIN;
    private UpDownType direction = UpDownType.DOWN;

    private final ScheduledExecutorService scheduler = ThreadPoolManager.getScheduledPool(PROFILE_THREADPOOL_NAME);
    protected @Nullable ScheduledFuture<?> stopTimer = null;
    protected @Nullable ScheduledFuture<?> updateTimer = null;

    public RollerShutterPositionProfile(final ProfileCallback callback, final ProfileContext context) {
        this.callback = callback;
        this.configuration = context.getConfiguration().as(RollerShutterPositionConfig.class);

        if (configuration.uptime == 0) {
            logger.info("Profile parameter {} must not be 0", UPTIME_PARAM);
            return;
        }

        configuration.downtime = configuration.downtime == 0 ? configuration.uptime : configuration.downtime;
        configuration.precision = configuration.precision == 0 ? DEFAULT_PRECISION : configuration.precision;
        this.isValidConfiguration = true;

        logger.debug("Profile configured with '{}'='{}' ms, '{}'={} ms, '{}'={}", UPTIME_PARAM, configuration.uptime,
                DOWNTIME_PARAM, configuration.downtime, PRECISION_PARAM, configuration.precision);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

    private boolean isMoving() {
        return !movingSince.equals(Instant.MIN);
    }

    private void moveTo(int targetPos) {
        if (targetPos < 0 || targetPos > 100) {
            logger.debug("moveTo() position is invalid: {}", targetPos);
            return;
        }

        int curPos = currentPosition();
        int posOffset = targetPos - curPos;

        UpDownType newCmd;

        if (targetPos == position && !isMoving()) {
            logger.debug("moveTo() position already current: {}", targetPos);
            if (targetPos == 0) { // always send command if either 0 or 100 in case it is not already in that position
                callback.handleCommand(UpDownType.UP);
            } else if (targetPos == 100) {
                callback.handleCommand(UpDownType.DOWN);
            }
            return;
        } else if (targetPos == 0 || targetPos == 100) {
            logger.debug("moveTo() bounding position");
            newCmd = targetPos == 0 ? UpDownType.UP : UpDownType.DOWN;
        } else if (Math.abs(posOffset) < configuration.precision) {
            callback.sendUpdate(new PercentType(position)); // update position because autoupdate will assume the
                                                            // movement happened
            logger.info("moveTo() is less than the precision setting of {}", configuration.precision);
            return;
        } else {
            newCmd = posOffset > 0 ? UpDownType.DOWN : UpDownType.UP;
        }

        logger.debug("moveTo() targetPosition: {} from currentPosition: {}", targetPos, curPos);

        long time = (long) ((Math.abs(posOffset) / 100d)
                * (posOffset > 0 ? (double) configuration.downtime * 1000 : (double) configuration.uptime * 1000));
        logger.debug("moveTo() computed movement offset: {} / {} / {} ms", posOffset, newCmd, time);

        this.targetPosition = targetPos;
        if (isMoving()) {
            stopStopTimer();
            if (this.direction != newCmd) {
                logger.debug("moveTo() reversing direction: {}, timer set in {} ms", direction, time);
                position = curPos; // Update "starting" position if already in motion since the last move did not finish
                this.direction = newCmd; // Update direction to the new command
                this.movingSince = Instant.now();
                callback.handleCommand(direction);
            } else {
                logger.debug("moveTo() already moving in right direction: {}, timer set in {} ms", direction, time);
            }
            startStopTimer(time);
        } else {
            logger.debug("moveTo() sending command for movement: {}, timer set in {} ms", direction, time);
            this.movingSince = Instant.now();
            startUpdateTimer();
            startStopTimer(time);
            this.direction = newCmd; // Update direction to the new command
            callback.handleCommand(direction);
        }
    }

    private synchronized void startUpdateTimer() {
        ScheduledFuture<?> lUpdateTimer = updateTimer;
        if (lUpdateTimer == null || lUpdateTimer.isDone()) {
            logger.trace("startUpdateTimer() actually");
            this.updateTimer = scheduler.scheduleWithFixedDelay(updateTimeoutTask, 0,
                    POSITION_UPDATE_PERIOD_MILLISECONDS, TimeUnit.MILLISECONDS);
        }
    }

    private synchronized void stopUpdateTimer() {
        logger.trace("stopUpdateTimer() called, isMoving: {}", isMoving());
        ScheduledFuture<?> lUpdateTimer = updateTimer;
        if (lUpdateTimer != null) {
            lUpdateTimer.cancel(true);
            this.updateTimer = null;
        }
    }

    private synchronized void startStopTimer(long time) {
        logger.trace("startStopTimer() called with time: {}", time);
        ScheduledFuture<?> lStopTimer = stopTimer;
        if (lStopTimer != null) {
            lStopTimer.cancel(true);
        }

        this.stopTimer = scheduler.schedule(stopTimeoutTask, time, TimeUnit.MILLISECONDS);
    }

    private synchronized void stopStopTimer() {
        logger.trace("stopStopTimer() called, isMoving: {}", isMoving());
        ScheduledFuture<?> lStopTimer = stopTimer;
        if (lStopTimer != null) {
            lStopTimer.cancel(true);
            this.stopTimer = null;
        }
    }

    private void stop(boolean handlerInitiated) {
        logger.trace("stop() called, isMoving: {}, handlerInitiated: {}", isMoving(), handlerInitiated);
        if (!handlerInitiated) {
            callback.handleCommand(StopMoveType.STOP);
        }

        this.position = currentPosition();
        stopUpdateTimer();
        stopStopTimer();

        callback.sendUpdate(new PercentType(position));
        this.movingSince = Instant.MIN;
        this.targetPosition = -1; // reset target position
    }

    private int currentPosition() {
        if (isMoving()) {
            long millis = movingSince.until(Instant.now(), ChronoUnit.MILLIS);
            double delta = 0;

            if (direction == UpDownType.UP) {
                delta = -(millis / (configuration.uptime * 1000)) * 100d;
            } else {
                delta = (millis / (configuration.downtime * 1000)) * 100d;
            }

            return (int) Math.max(0, Math.min(100, Math.round(position + delta)));
        } else {
            return position;
        }
    }

    // Runnable task to time duration of the move to make
    private Runnable stopTimeoutTask = new Runnable() {
        @Override
        public void run() {
            stopUpdateTimer();
            if (targetPosition == 0 || targetPosition == 100) {
                // Don't send stop command to re-sync position using the motor end stop
                logger.debug("arrived at end position, not stopping for calibration");
            } else {
                callback.handleCommand(StopMoveType.STOP);
                logger.debug("arrived at position, sending STOP command");
            }

            logger.trace("stopTimeoutTask() position: {}", targetPosition);
            position = currentPosition();
            callback.sendUpdate(new PercentType(position));
            movingSince = Instant.MIN;
            targetPosition = -1;
        }
    };

    // Runnable task to update the item on position while the roller shutter is moving
    private Runnable updateTimeoutTask = new Runnable() {
        @Override
        public void run() {
            logger.trace("updateTimeoutTask() called, isMoving: {}, currentPosition: {}", isMoving(),
                    currentPosition());
            if (isMoving()) {
                int pos = currentPosition();
                if ((pos <= 0 && direction == UpDownType.UP) || (pos >= 100 && direction == UpDownType.DOWN)) {
                    logger.debug("updateTimeoutTask() reached end position, stopping update timer");
                    stopUpdateTimer();
                }
                pos = Math.max(0, Math.min(100, pos));
                callback.sendUpdate(new PercentType(pos));
            }
        }
    };

    @Override
    public void onCommandFromItem(Command command) {
        logger.debug("onCommandFromItem: {}", command);

        // pass through command if profile has not been configured properly
        if (!isValidConfiguration) {
            callback.handleCommand(command);
            return;
        }

        if (command instanceof UpDownType) {
            if (command == UpDownType.UP) {
                moveTo(0);
            } else if (command == UpDownType.DOWN) {
                moveTo(100);
            }
        } else if (command instanceof StopMoveType) {
            stop(false);
        } else if (command instanceof PercentType ptCommand) {
            moveTo(ptCommand.intValue());
        } else {
            logger.warn("onCommandFromItem() received unexpected command type: {} - {}", command.getClass(), command);
        }
    }

    // Handle restoreOnStartup update of the item position
    // Note, this will also be called when the profile sendUpdate, no harm in setting the position to the current value
    @Override
    public void onStateUpdateFromItem(State state) {
        if (!isValidConfiguration) {
            return;
        }

        if (isMoving()) {
            logger.debug("onStateUpdateFromItem() called while moving, ignoring state update: {}", state);
            return;
        }

        logger.debug("onStateUpdateFromItem() called with state: {}, isMoving: {}", state, isMoving());
        if (state instanceof PercentType percentType) {
            int pos = percentType.intValue();
            if (pos < 0 || pos > 100) {
                logger.warn("onStateUpdateFromItem() position is invalid: {}", pos);
                return;
            }
            this.position = pos;
        } else {
            logger.warn("onStateUpdateFromItem() received unexpected state type: {} - {}", state.getClass(), state);
        }
    }

    @Override
    public synchronized void onCommandFromHandler(Command command) {
        if (!isValidConfiguration) {
            return;
        }

        logger.debug("onCommandFromHandler() called with command: {}", command);
        if (command instanceof StopMoveType) {
            stop(true);
        } else if (command instanceof UpDownType upDownType) {
            stopStopTimer(); // manual control
            targetPosition = -1;
            if (isMoving()) {
                // update timer is already running
                if (upDownType != direction) {
                    logger.trace("reverse direction from {} to {}", direction, upDownType);
                    this.position = currentPosition();
                    this.direction = upDownType;
                    this.movingSince = Instant.now();
                } else {
                    logger.trace("continue in current direction: {}", upDownType);
                }
                callback.sendUpdate(new PercentType(position));
            } else {
                this.direction = upDownType; // update direction to the new command if we are not already moving
                this.movingSince = Instant.now(); // reset movingSince to now if we are not already moving
                startUpdateTimer();
            }
        }
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
    }
}
