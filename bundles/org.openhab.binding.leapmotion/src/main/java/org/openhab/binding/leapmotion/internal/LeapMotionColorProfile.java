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
package org.openhab.binding.leapmotion.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LeapMotionColorProfile} class implements the behavior when being linked to a Color item.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class LeapMotionColorProfile implements TriggerProfile {

    private final Logger logger = LoggerFactory.getLogger(LeapMotionColorProfile.class);

    private ProfileCallback callback;
    private HSBType lastState = HSBType.BLACK;

    public LeapMotionColorProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return LeapMotionProfileFactory.UID_COLOR;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        if (state instanceof HSBType) {
            lastState = (HSBType) state;
        } else {
            PercentType currentBrightness = state.as(PercentType.class);
            if (currentBrightness != null) {
                lastState = new HSBType(lastState.getHue(), lastState.getSaturation(), currentBrightness);
            }
        }
    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (event.equals(LeapMotionBindingConstants.GESTURE_TAP)) {
            callback.sendCommand(lastState.getBrightness().equals(PercentType.ZERO) ? OnOffType.ON : OnOffType.OFF);
        } else if (event.equals(LeapMotionBindingConstants.GESTURE_CLOCKWISE)) {
            HSBType color = changeColor(lastState, true);
            callback.sendCommand(color);
            lastState = color;
        } else if (event.equals(LeapMotionBindingConstants.GESTURE_ANTICLOCKWISE)) {
            HSBType color = changeColor(lastState, false);
            callback.sendCommand(color);
            lastState = color;
        } else if (event.startsWith(LeapMotionBindingConstants.GESTURE_FINGERS)) {
            // the brightness is determined by the height of the palm over the sensor, where higher means brighter
            int fingers = Integer
                    .valueOf(Character.toString(event.charAt(LeapMotionBindingConstants.GESTURE_FINGERS.length())));
            if (fingers == 5) {
                try {
                    int height = Integer
                            .valueOf(event.substring(LeapMotionBindingConstants.GESTURE_FINGERS.length() + 2));
                    height = Math.min(100 * height / LeapMotionDimmerProfile.MAX_HEIGHT, 100); // don't use values over
                                                                                               // 100
                    PercentType brightness = new PercentType(height);
                    callback.sendCommand(brightness);
                    lastState = new HSBType(lastState.getHue(), lastState.getSaturation(), brightness);
                } catch (NumberFormatException e) {
                    logger.error("Found illegal format of finger event: {}", event, e);
                }
            }
        }
    }

    private HSBType changeColor(HSBType color, boolean clockwise) {
        int hue = clockwise ? (color.getHue().toBigDecimal().intValue() - 20 + 360) % 360
                : (color.getHue().toBigDecimal().intValue() + 20 + 360) % 360;
        logger.debug("New hue value: {}", hue);
        HSBType newState = new HSBType(new DecimalType(hue), color.getSaturation(), color.getBrightness());
        return newState;
    }
}
