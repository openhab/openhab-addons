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
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.leapmotion.LeapMotionBindingConstants;

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
            callback.sendCommand(lastState ? OnOffType.OFF : OnOffType.ON);
        }
    }

}
