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
package org.openhab.binding.dmx.internal.action;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.dmx.internal.Util;
import org.openhab.binding.dmx.internal.multiverse.DmxChannel;
import org.openhab.core.library.types.PercentType;

/**
 * The {@link FadeAction} fades a given channel from its current state to the requested
 * state in the given amount of time. After the fade, the new state is held for
 * a given or indefinite time.
 *
 * @author Davy Vanherbergen - Initial contribution
 * @author Jan N. Klug - Refactoring for ESH
 */
@NonNullByDefault
public class FadeAction extends BaseAction {
    /** Time in ms to hold the target value. -1 is indefinite */
    private long holdTime;

    /** Time in ms to fade from current value to new target value */
    private long fadeTime;

    /** Channel output value on action start. **/
    private int startValue;

    /** Desired channel output value. **/
    private final int targetValue;

    private float stepDuration;

    private FadeDirection fadeDirection = FadeDirection.DOWN;

    /**
     * Create new fading action.
     *
     * @param fadeTime time in ms to fade from the current value to the new value.
     * @param targetValue new value 0-255 for this channel.
     * @param holdTime time in ms to hold the color before moving to the next action. -1 is indefinite.
     */
    public FadeAction(int fadeTime, int targetValue, int holdTime) {

        this.fadeTime = fadeTime;
        this.targetValue = Util.toDmxValue(targetValue) << 8;
        this.holdTime = holdTime;

        if (holdTime < -1) {
            this.holdTime = -1;
        }
        if (fadeTime < 0) {
            this.fadeTime = 0;
        }
    }

    public FadeAction(int fadeTime, PercentType targetValue, int holdTime) {
        this(fadeTime, Util.toDmxValue(targetValue), holdTime);
    }

    public FadeAction(int fadeTime, int currentValue, int targetValue, int holdTime) {
        this(Util.fadeTimeFraction(currentValue, targetValue, fadeTime), targetValue, holdTime);
    }

    @Override
    public int getNewValue(DmxChannel channel, long currentTime) {
        int newValue = channel.getHiResValue();

        if (startTime == 0) {
            startTime = currentTime;
            state = ActionState.RUNNING;

            if (fadeTime != 0) {
                startValue = channel.getHiResValue();

                // calculate fade details
                if (startValue == targetValue) {
                    stepDuration = 1;
                } else if (startValue > targetValue) {
                    fadeDirection = FadeDirection.DOWN;
                    stepDuration = (float) fadeTime / (startValue - targetValue);
                } else {
                    fadeDirection = FadeDirection.UP;
                    stepDuration = (float) fadeTime / (targetValue - startValue);
                }
            } else {
                newValue = targetValue;
            }
        }

        long duration = currentTime - startTime;

        if ((fadeTime != 0) && (newValue != targetValue)) {
            // calculate new fade value
            if (stepDuration == 0) {
                stepDuration = 1;
            }
            int currentStep = (int) (duration / stepDuration);

            if (fadeDirection == FadeDirection.UP) {
                newValue = startValue + currentStep;
                if (newValue > targetValue) {
                    newValue = targetValue;
                }
            } else {
                newValue = startValue - currentStep;
                if (newValue < targetValue) {
                    newValue = targetValue;
                }
            }
        } else {
            newValue = targetValue;
        }

        if (newValue == targetValue) {
            if (holdTime > -1) {
                // we reached the target already, check if we need to hold longer
                if (((holdTime > 0 || fadeTime > 0) && (duration >= fadeTime + holdTime))
                        || (holdTime == 0 && fadeTime == 0)) {
                    // mark action as completed
                    state = ActionState.COMPLETED;
                }
            } else {
                state = ActionState.COMPLETEDFINAL;
            }
        }

        return newValue;
    }

    @Override
    public String toString() {
        return "FadeAction: " + targetValue + ", fade time " + fadeTime + "ms, hold time " + holdTime + "ms";
    }
}
