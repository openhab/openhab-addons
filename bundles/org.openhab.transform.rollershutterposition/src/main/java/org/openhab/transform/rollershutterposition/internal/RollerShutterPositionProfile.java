/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Profile to offer the RollerShutterPosition ItemChannelLink
 *
 * @author Jeff James - Initial contribution
 *
 *         Core logic in this module has been heavily adapted from Tarag Gautier js script implementation
 *         VASRollershutter.js
 */
@NonNullByDefault
public class RollerShutterPositionProfile implements StateProfile {

    public static final ProfileTypeUID PROFILE_TYPE_UID = new ProfileTypeUID("rollershutter", "rollershutter-position");

    private final Logger logger = LoggerFactory.getLogger(RollerShutterPositionProfile.class);

    private final ProfileCallback callback;

    private static final String UPTIME_PARAM = "uptime";
    private static final String DOWNTIME_PARAM = "downtime";
    private static final String PRECISION_PARAM = "precision";

    @NonNullByDefault({})
    private long uptime; // uptime in ms (set by param)
    @NonNullByDefault({})
    private long downtime; // downtime in ms (set by param)
    @NonNullByDefault({})
    private int precision; // minimum movement

    @NonNullByDefault({})
    private int position = 0; // current position of the blind (assumes 0 when system starts)
    private int targetPosition;
    @Nullable
    private Optional<ZonedDateTime> movingSince = Optional.empty();
    private boolean isMoving = false;
    private UpDownType direction = UpDownType.DOWN;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool("profile-rollershutterposition");
    private Optional<ScheduledFuture<?>> stopTimer = Optional.empty();
    private Optional<ScheduledFuture<?>> updateTimer = Optional.empty();

    public RollerShutterPositionProfile(final ProfileCallback callback, final ProfileContext context) {
        this.callback = callback;

        Object uptimeParam = context.getConfiguration().get(UPTIME_PARAM);
        if (uptimeParam != null) {
            if (uptimeParam instanceof BigDecimal) {
                uptime = (long) (((BigDecimal) uptimeParam).doubleValue() * 1000);
            } else {
                logger.error("Parameter '{}' is not of type decimal.", UPTIME_PARAM);
            }
        } else {
            uptime = 0;
        }

        Object downtimeParam = context.getConfiguration().get(DOWNTIME_PARAM);
        if (downtimeParam != null) {
            if (downtimeParam instanceof BigDecimal) {
                downtime = (long) (((BigDecimal) downtimeParam).doubleValue() * 1000);
            } else {
                logger.error("Parameter '{}' is not of type decimal.", UPTIME_PARAM);
            }
        } else {
            downtime = uptime;
        }

        Object precisionParam = context.getConfiguration().get(PRECISION_PARAM);
        if (precisionParam != null) {
            if (precisionParam instanceof BigDecimal) {
                precision = ((BigDecimal) precisionParam).intValue();
            } else {
                logger.error("Parameter '{}' is not of type integer.", PRECISION_PARAM);
            }
        } else {
            precision = 1;
        }

        logger.debug("Profile configured with '{}'='{}' ms, '{}'={} ms, '{}'={}", UPTIME_PARAM, uptime, DOWNTIME_PARAM,
                downtime, PRECISION_PARAM, precision);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return PROFILE_TYPE_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        logger.trace("onStateUpdateFromItem: {}", state);
    }

    @Override
    public void onCommandFromItem(Command command) {
        logger.debug("onCommandFromItem: {}", command);
        logger.trace("uptime: {}, position: {}", uptime, position);

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

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
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

        if (targetPos == position && !isMoving) {
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
        } else if (Math.abs(posOffset) < precision) {
            callback.sendUpdate(new PercentType(position)); // update position because autoupdate will assume the
                                                            // movement happened
            logger.info("moveTo() is less than the precision setting of {}", precision);
            return;
        } else {
            newCmd = posOffset > 0 ? UpDownType.DOWN : UpDownType.UP;
        }

        logger.info("moveTo() targetPosition: {} from currentPosition: {}", targetPos, curPos);

        long time = (long) ((Math.abs(posOffset) / 100d) * (posOffset > 0 ? (double) downtime : (double) uptime));
        logger.debug("moveTo() computed movement offset: {} / {} / {} ms", posOffset, newCmd, time);

        if (isMoving) {
            position = curPos; // Update "starting" position if already in motion since the last move did not finish

            if (direction == newCmd) {
                alreadyMoving = true;
            }
        }

        stopTimer.ifPresent(job -> job.cancel(false));
        stopTimer = Optional.of(scheduler.schedule(stopTimeoutTask, time, TimeUnit.MILLISECONDS));
        targetPosition = targetPos;
        isMoving = true;
        direction = newCmd;
        movingSince = Optional.of(ZonedDateTime.now());

        updateTimer.ifPresent(job -> job.cancel(false));
        updateTimer = Optional.of(scheduler.scheduleAtFixedRate(updateTimeoutTask, 0, 2000, TimeUnit.MILLISECONDS));

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
        logger.trace("stop()");

        position = currentPosition();

        stopTimer.ifPresent(job -> job.cancel(false));
        stopTimer = Optional.empty();

        updateTimer.ifPresent(job -> job.cancel(false));
        updateTimer = Optional.empty();

        isMoving = false;

        callback.sendUpdate(new PercentType(position));
    }

    private int currentPosition() {
        if (isMoving) {
            logger.trace("currentPosition() while moving");
            if (movingSince == null) {
                return -1;
            }

            // movingSince should always be set if moving
            long millis = movingSince.get().until(ZonedDateTime.now(), ChronoUnit.MILLIS);

            double delta = 0;

            if (direction == UpDownType.UP) {
                delta = -(double) millis / uptime * 100d;
            } else {
                delta = (double) millis / (double) downtime * 100d;
            }

            return (int) Math.max(0, Math.min(100, Math.round(position + delta)));
        } else {
            return position;
        }
    }

    private Runnable stopTimeoutTask = new Runnable() {
        @Override
        public void run() {
            if (targetPosition == 0 || targetPosition == 100) {
                // Don't send stop command to re-sync position using the motor end stop
                logger.info("arrived at end position, not stopping for calibration");
            } else {
                callback.handleCommand(StopMoveType.STOP);
                logger.info("arrived at position, sending STOP command");
            }

            logger.trace("stopTimeoutTask() position: {}", targetPosition);

            updateTimer.ifPresent(job -> job.cancel(false));
            updateTimer = Optional.empty();

            isMoving = false;
            position = targetPosition;
            targetPosition = -1;
            callback.sendUpdate(new PercentType(position));
        }
    };

    private Runnable updateTimeoutTask = new Runnable() {
        @Override
        public void run() {
            if (isMoving) {
                int pos = currentPosition();
                if (pos < 0 || pos > 100) {
                    logger.trace("updateTimeTask() position not in range: {}", pos);
                }
                callback.sendUpdate(new PercentType(pos));
                logger.trace("updateTimeoutTask(): {}", pos);
            }
        }
    };
}
