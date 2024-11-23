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
package org.openhab.binding.leapmotion.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;

/**
 * The {@link LeapMotionSwitchProfile} class implements the behavior when being linked to a Switch item.
 *
 * @author Kai Kreuzer - Initial contribution
 */
@NonNullByDefault
public class LeapMotionSwitchProfile implements TriggerProfile {

    private ProfileCallback callback;
    private boolean lastState = false;

    public LeapMotionSwitchProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return LeapMotionProfileFactory.UID_SWITCH;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        lastState = OnOffType.ON.equals(state.as(OnOffType.class));
    }

    @Override
    public void onTriggerFromHandler(String event) {
        if (event.equals(LeapMotionBindingConstants.GESTURE_TAP)) {
            callback.sendCommand(OnOffType.from(!lastState));
        }
    }
}
