/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.util.Objects;
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
    private int targetPosition;
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
            logger.info("Profile paramater {} must not be 0", UPTIME_PARAM);
            return;
        }

        if (configuration.downtime == 0) {
            configuration.downtime = configuration.uptime;
        }

        if (configuration.precision == 0) {
            configuration.precision = DEFAULT_PRECISION;
        }

        this.isValidConfiguration = true;

        logger.debug("Profile configured with '{}'='{}' ms, '{}'={} ms, '{}'={}", UPTIME_PARAM, configuration.uptime,
                DOWNTIME_PARAM, configuration.downtime, PRECISION_PARAM, configuration.precision);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

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
            stop();
        } else {
            moveTo(((PercentType) command).intValue());
        }
    }

    private boolean isMoving() {
        return (!movingSince.equals(Instant.MIN));
    }

    private void moveTo(int targetPos) {
        boolean alreadyMoving = false;

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

        if (isMoving()) {
            position = curPos; // Update "starting" position if already in motion since the last move did not finish

            if (direction == newCmd) {
                alreadyMoving = true;
            }
        }

        this.targetPosition = targetPos;
        this.direction = newCmd;
        this.movingSince = Instant.now();

        if (stopTimer != null) {
            Objects.requireNonNull(stopTimer).cancel(true);
        }
        this.stopTimer = scheduler.schedule(stopTimeoutTask, time, TimeUnit.MILLISECONDS);

        if (updateTimer != null) {
            Objects.requireNonNull(updateTimer).cancel(true);
        }
        this.updateTimer = scheduler.scheduleWithFixedDelay(updateTimeoutTask, 0, POSITION_UPDATE_PERIOD_MILLISECONDS,
                TimeUnit.MILLISECONDS);

        if (!alreadyMoving) {
            logger.debug("moveTo() sending command for movement: {}, timer set in {} ms", direction, time);
            callback.handleCommand(direction);
        } else {
            logger.debug("moveTo() updating timing but already moving in right directio: {}, timer set in {} ms",
                    direction, time);
        }
    }

    private void stop() {
        callback.handleCommand(StopMoveType.STOP);

        this.position = currentPosition();
        this.movingSince = Instant.MIN;

        if (stopTimer != null) {
            Objects.requireNonNull(stopTimer).cancel(true);
            this.stopTimer = null;
        }
        if (updateTimer != null) {
            Objects.requireNonNull(updateTimer).cancel(true);
            this.updateTimer = null;
        }

        callback.sendUpdate(new PercentType(position));
    }

    private int currentPosition() {
        if (isMoving()) {
            logger.trace("currentPosition() while moving");

            // movingSince is always set if moving
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
            if (targetPosition == 0 || targetPosition == 100) {
                // Don't send stop command to re-sync position using the motor end stop
                logger.debug("arrived at end position, not stopping for calibration");
            } else {
                callback.handleCommand(StopMoveType.STOP);
                logger.debug("arrived at position, sending STOP command");
            }

            logger.trace("stopTimeoutTask() position: {}", targetPosition);

            if (updateTimer != null) {
                Objects.requireNonNull(updateTimer).cancel(true);
                updateTimer = null;
            }

            movingSince = Instant.MIN;
            position = targetPosition;
            targetPosition = -1;
            callback.sendUpdate(new PercentType(position));
        }
    };

    // Runnable task to update the item on position while the roller shutter is moving
    private Runnable updateTimeoutTask = new Runnable() {
        @Override
        public void run() {
            if (isMoving()) {
                int pos = currentPosition();
                if (pos < 0 || pos > 100) {
                    return;
                }
                callback.sendUpdate(new PercentType(pos));
                logger.trace("updateTimeoutTask(): {}", pos);
            }
        }
    };

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
    }
}
