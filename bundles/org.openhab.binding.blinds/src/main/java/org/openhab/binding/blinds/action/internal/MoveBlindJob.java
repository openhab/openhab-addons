/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.blinds.action.internal;

import org.eclipse.smarthome.core.library.items.DimmerItem;
import org.eclipse.smarthome.core.library.items.RollershutterItem;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.blinds.action.BlindDirection;
import org.openhab.binding.blinds.internal.BlindsConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Pfleger - Initial contribution
 *
 */
public class MoveBlindJob {
    private final Logger logger = LoggerFactory.getLogger(MoveBlindJob.class);

    private static enum JobState {
        Uninitialized,
        MoveRollershutter,
        WaitForRollershutter,
        MoveSlat,
        Finished
    }

    private int targetBlindPosition;
    private int targetSlatPosition;
    private final BlindItem blindItem;

    private final long startTime;
    private long lastLogTime;
    private JobState state;

    private BlindDirection direction;
    private BlindsConfiguration blindsConfiguration;

    public MoveBlindJob(BlindItem blindItem, int rollershutterPosition, int slatPosition, BlindDirection direction,
            BlindsConfiguration blindsConfiguration) {
        this.blindItem = blindItem;
        this.targetBlindPosition = rollershutterPosition;
        this.targetSlatPosition = slatPosition;
        this.direction = direction;
        this.blindsConfiguration = blindsConfiguration;

        this.startTime = System.currentTimeMillis();
        this.state = JobState.Uninitialized;
    }

    public String getRollershutterName() {
        return getRollershutterToMove().getName();
    }

    private RollershutterItem getRollershutterToMove() {
        return blindItem.getAutoRollershutterItem();
    }

    private RollershutterItem getRollershutterForState() {
        RollershutterItem rollershutterItemForState = blindItem.getRollershutterItem();
        if (rollershutterItemForState != null) {
            return rollershutterItemForState;
        }

        return getRollershutterToMove();
    }

    /**
     * @return true when there is immediately more work pending, false if there is currently nothing else to do. if
     *         there is still more to do in the future depends on whether isFinsihed returns true or not
     */
    public boolean execute() {
        RollershutterItem rollershutter = getRollershutterToMove();
        RollershutterItem rollershutterForState = getRollershutterForState();
        DimmerItem slat = blindItem.getSlatItem();

        switch (state) {
            case Uninitialized: {
                State rollershutterState = rollershutterForState.getState();
                State slatState = slat.getState();

                logger.debug("Trying to move {} (state item: {}) from {} (slat={}) to {} (slat={}) using direction {}",
                        getRollershutterName(), rollershutterForState.getName(), rollershutterState, slatState,
                        targetBlindPosition, targetSlatPosition, direction);

                validateTargetBlindPosition();

                // if we don't force a direction it is enough to know that the state is not equal
                if (isRollershutterMoveRequired()) {
                    state = JobState.MoveRollershutter;
                    return true;
                }

                if (!rollershutterState.equals(new PercentType(0))) {
                    // no need to do anything with the slats if the rollershutter is at position 0
                    if (isSlatMoveRequired(slat)) {
                        state = JobState.MoveSlat;
                        return true;
                    }
                } else {
                    logger.debug("Not required to move slat of {} as the blind is fully open", getRollershutterName());
                }

                state = JobState.Finished;
                return true;
            }

            case MoveRollershutter: {
                // check for a valid target position
                if (targetBlindPosition >= 0 && targetBlindPosition <= 100) {
                    logger.info("Moving {} from {} to {}", getRollershutterName(), rollershutterForState.getState(),
                            targetBlindPosition);

                    if (targetSlatPosition < 0) {
                        // we need to keep the current slat position to restore it after finishing rollershutter move
                        targetSlatPosition = getCurrentSlatPosition();
                        logger.debug(
                                "No target slat position specified for {}. Keeping current slat position of {} to reapply after blind move",
                                getRollershutterName(), targetSlatPosition);

                    }

                    blindItem.setLatestBlindMoveTimestamp(System.currentTimeMillis());
                    blindItem.setLatestSlatMoveTimestamp(System.currentTimeMillis());

                    rollershutter.send(new PercentType(targetBlindPosition));
                    // eventPublisher.sendCommand(rollershutter.getName(), rollershutterPositionCommand);
                    state = JobState.WaitForRollershutter;
                    return false; // no need to continue immediately, we have to wait until the raffstore has moved down
                }

                logger.info("Illegal target blind position for {}. Target {} is not valid. Aborting...",
                        getRollershutterName(), targetBlindPosition);
                state = JobState.Finished;
                return true;
            }

            case WaitForRollershutter: {
                boolean timeout = System.currentTimeMillis() - startTime > 120000;

                if (timeout) {
                    logger.warn(
                            "Target position for blind {} of {}"
                                    + " has not been reached within 2 minutes. Trying to update slat position now...",
                            getRollershutterName(), targetBlindPosition);
                } else {
                    if (System.currentTimeMillis() - lastLogTime > 5000) {
                        lastLogTime = System.currentTimeMillis();
                        logger.info("Waiting for {} to reach position " + "{}. Current position: {}",
                                getRollershutterName(), targetBlindPosition, rollershutterForState.getState());
                    }
                }
                // wait until the rollershutter has reached its position or a timeout occurred
                if (rollershutterForState.getState().equals(new PercentType(targetBlindPosition)) || timeout) {
                    logger.info("Finished waiting for {} to reach position: {} (current position: {}, timeout={})",
                            getRollershutterName(), targetBlindPosition, rollershutterForState.getState(), timeout);

                    // no check to isSlatMoveRequired valid as the rollershutter has moved --> the slat state is not
                    // valid anymore
                    // also not needed as the direction is already evaluated before we move the rollershutter
                    state = JobState.MoveSlat;
                    return true;
                }
                // do not change state, check again later
                return false;
            }

            case MoveSlat: {
                // check for a valid target position
                if (targetSlatPosition >= 0 || targetSlatPosition <= 100) {
                    logger.info("Moving slat of {} from {} to {}", getRollershutterName(), slat.getState(),
                            targetSlatPosition);

                    blindItem.setLatestSlatMoveTimestamp(System.currentTimeMillis());
                    slat.send(new PercentType(targetSlatPosition));
                } else {
                    logger.info("Illegal target slat position for {}. Target {} is not valid. Aborting...",
                            getRollershutterName(), targetSlatPosition);
                }
                state = JobState.Finished;
                return true;
            }

            case Finished:
            default:
                state = JobState.Finished;
                return false; // nothing to do
        }
    }

    private void validateTargetBlindPosition() {
        // make sure that the defined target blind position does not exceed the limit
        if (targetBlindPosition >= 0 && blindItem.getConfig().getBlindPositionLimit() > 0) {
            logger.debug(
                    "Limit defined for {}" + ". Ensuring target position does not exceed limit: limit={}"
                            + ", targetPosition={}",
                    blindItem.getRollershutterItemName(), blindItem.getConfig().getBlindPositionLimit(),
                    targetBlindPosition);
            targetBlindPosition = Math.min(targetBlindPosition, blindItem.getConfig().getBlindPositionLimit());
        }

    }

    private int getCurrentSlatPosition() {
        State slatState = blindItem.getSlatItem().getState();

        if (slatState instanceof DecimalType) {
            DecimalType currentSlatPosition = (DecimalType) slatState;
            return currentSlatPosition.intValue();
        }
        return -1;
    }

    private boolean isSlatMoveRequired(DimmerItem slat) {
        if (targetSlatPosition < 0) {
            logger.debug("Moving slat of {} not required for target position {}", getRollershutterName(),
                    targetSlatPosition);
            return false;
        }

        State slatState = slat.getState();
        int currentSlatPositionInt = getCurrentSlatPosition();
        if (currentSlatPositionInt < 0) {
            logger.warn("Current slat state of {} not available (state={}). Moving slat to target position {}",
                    getRollershutterName(), slatState, targetSlatPosition);
            // in case we don't have a current state, lets try to move it
            return true;
        }

        if (Math.abs(targetSlatPosition - currentSlatPositionInt) < 5) {
            logger.debug(
                    "Moving slat of {} not required as the target slat position ({}) does not differ enough from the current slat position ({})",
                    getRollershutterName(), targetSlatPosition, slatState);
            return false;
        }

        boolean openSlat = targetSlatPosition < currentSlatPositionInt;

        BlindDirection allowedBlindDirection = blindItem.getConfig().getAllowedBlindDirection();
        if ((allowedBlindDirection == BlindDirection.UP && !openSlat)
                || (allowedBlindDirection == BlindDirection.DOWN && openSlat)) {
            logger.debug("Moving slat of {} from {} to {} is not allowed. The movement is limited to direction {}",
                    getRollershutterName(), currentSlatPositionInt, targetSlatPosition, allowedBlindDirection);
            return false;
        }

        /*
         * // the position is different enough. first check if enough time since the last slat move has elapsed
         *
         * if (!enoughTimeElapsedSinceLastSlatMove(openSlat)) {
         * logger.debug(
         * "Moving slat of blind {} not required as not enough time elapsed since the last slat move. Time elapsed: {}",
         * getRollershutterName(), blindItem.getMsSinceLastSlatMove());
         * }
         */

        // check if the move direction is compatible with the defined direction
        if (direction == BlindDirection.ANY || (direction == BlindDirection.UP && openSlat)
                || (direction == BlindDirection.DOWN && !openSlat)) {

            return true;
        }

        logger.debug(
                "Moving slat of {} not possible for direction {}. Target position {} is not in the right direction from current position ({})",
                getRollershutterName(), direction, targetSlatPosition, slatState);
        return false;
    }

    /*
     * private boolean enoughTimeElapsedSinceLastSlatMove(boolean openSlat) {
     * if ((openSlat && System.currentTimeMillis() - blindItem.getMsSinceLastSlatMove() > TimeUnit.SECONDS
     * .toMillis(blindsConfiguration.openSlatDelay))
     *
     * || !openSlat && (System.currentTimeMillis() - blindItem.getMsSinceLastSlatMove()) > TimeUnit.SECONDS
     * .toMillis(blindsConfiguration.closeSlatDelay)) {
     *
     * return true;
     * }
     * return false;
     * }
     */

    /**
     * Checks the current rollershutter state and compares it to the target state and the expected direction
     *
     * @return
     */
    private boolean isRollershutterMoveRequired() {
        if (targetBlindPosition < 0) {
            logger.debug("Moving {} not required for target position {}", getRollershutterName(), targetBlindPosition);
            return false;
        }

        State rollershutterState = getRollershutterForState().getState();
        int currentRollershutterPositionInt = -1;
        if (rollershutterState instanceof DecimalType) {
            DecimalType currentRollershutterPosition = (DecimalType) rollershutterState;
            currentRollershutterPositionInt = currentRollershutterPosition.intValue();
        } else {
            logger.warn(
                    "Current state of {} is not of type DecimalType ({}), so the current state cannot be interpreted. Moving blind to target position {}",
                    getRollershutterName(), rollershutterState, targetBlindPosition);
            // in case we don't have a current state, lets try to move it
            return true;
        }

        if (Math.abs(targetBlindPosition - currentRollershutterPositionInt) < 5) {
            logger.debug(
                    "Moving blind {} not required as the target position ({}) does not differ enough from the current position ({})",
                    getRollershutterName(), targetBlindPosition, rollershutterState);
            return false;
        }

        boolean openBlind = targetBlindPosition < currentRollershutterPositionInt;
        BlindDirection allowedBlindDirection = blindItem.getConfig().getAllowedBlindDirection();
        if ((allowedBlindDirection == BlindDirection.UP && !openBlind)
                || (allowedBlindDirection == BlindDirection.DOWN && openBlind)) {
            logger.debug("Moving {} from {} to {} is not allowed. The movement is limited to direction {}",
                    getRollershutterName(), currentRollershutterPositionInt, targetBlindPosition,
                    allowedBlindDirection);
            return false;
        }

        // if we don't force a direction it is enough to know that the state is not equal
        if (direction == BlindDirection.ANY) {
            return true;
        } else {

            // in this case we have to know the numeric value of the state. if the state is undefined we don't know if
            // the direction would be up or down. There are two exceptions to this rule.
            // 1. if we move up and the target is 0 then we cannot have a value < 0, so we can move to 0
            // 2. if we move down and the target is 100 we cannot have a value > 100, so we can move to 100
            if (direction == BlindDirection.UP) {
                if (targetBlindPosition == 0) {
                    return true;
                }

                if (currentRollershutterPositionInt > targetBlindPosition) {
                    return true;
                }
            }

            if (direction == BlindDirection.DOWN) {
                if (targetBlindPosition == 100) {
                    return true;
                }

                if (currentRollershutterPositionInt < targetBlindPosition) {
                    return true;
                }
            }
        }

        logger.debug(
                "Moving {} not possible for direction {}. Target position {} is not in the right direction from current position ({})",
                getRollershutterName(), direction, targetBlindPosition, rollershutterState);
        return false;
    }

    public boolean isFinished() {
        return state == JobState.Finished;
    }

    @Override
    public String toString() {
        RollershutterItem rollershutter = getRollershutterToMove();
        RollershutterItem rollershutterForState = getRollershutterForState();
        DimmerItem slat = blindItem.getSlatItem();

        return "MoveBlindJob - Move " + getRollershutterName() + "+ (state item: " + rollershutterForState.getName()
                + ") from " + rollershutter.getState() + " (slat=" + slat.getState() + ") to " + targetBlindPosition
                + " (slat=" + targetSlatPosition + ") using direction " + direction;
    }
}
