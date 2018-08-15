/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.leapmotion.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.leapmotion.LeapMotionBindingConstants;
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
