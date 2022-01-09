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
package org.openhab.binding.leapmotion.internal;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LeapMotionDimmerProfile} class implements the behavior when being linked to a Dimmer item.
 * It supports two modes: Either the dim level is determined by the number of shown fingers or by the height of the hand
 * over the controller.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class LeapMotionDimmerProfile implements TriggerProfile {

    static final int MAX_HEIGHT = 400; // in mm over controller
    private static final String MODE = "mode";

    private final Logger logger = LoggerFactory.getLogger(LeapMotionDimmerProfile.class);

    private ProfileCallback callback;
    private BigDecimal lastState = BigDecimal.ZERO;
    private boolean fingerMode = false;

    public LeapMotionDimmerProfile(ProfileCallback callback, ProfileContext profileContext) {
        this.callback = callback;
        fingerMode = "fingers".equals(profileContext.getConfiguration().get(MODE));
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return LeapMotionProfileFactory.UID_DIMMER;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        PercentType currentState = state.as(PercentType.class);
        if (currentState != null) {
            lastState = currentState.toBigDecimal();
        }
    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (event.equals(LeapMotionBindingConstants.GESTURE_TAP)) {
            callback.sendCommand(lastState.equals(BigDecimal.ZERO) ? OnOffType.ON : OnOffType.OFF);
        } else if (event.startsWith(LeapMotionBindingConstants.GESTURE_FINGERS)) {
            int fingers = Integer
                    .valueOf(Character.toString(event.charAt(LeapMotionBindingConstants.GESTURE_FINGERS.length())));
            if (fingerMode) {
                // the brightness is determined by the number of shown fingers, 20% for each.
                callback.sendCommand(new PercentType(fingers * 20));
            } else if (fingers == 5) {
                // the brightness is determined by the height of the palm over the sensor, where higher means brighter
                try {
                    int height = Integer
                            .valueOf(event.substring(LeapMotionBindingConstants.GESTURE_FINGERS.length() + 2));
                    height = Math.min(100 * height / MAX_HEIGHT, 100); // don't use values over 100
                    callback.sendCommand(new PercentType(height));
                } catch (NumberFormatException e) {
                    logger.error("Found illegal format of finger event: {}", event, e);
                }
            }
        }
    }
}
