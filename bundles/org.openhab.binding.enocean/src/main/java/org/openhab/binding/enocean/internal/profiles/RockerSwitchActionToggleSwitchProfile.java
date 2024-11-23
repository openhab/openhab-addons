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
package org.openhab.binding.enocean.internal.profiles;

import static org.openhab.binding.enocean.internal.profiles.EnOceanProfiles.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.State;

/**
 * The {@link RockerSwitchActionToggleSwitchProfile} is used for channel rockerSwitchAction to be able to bind this
 * trigger channel directly to a switch item
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class RockerSwitchActionToggleSwitchProfile extends RockerSwitchActionBaseProfile {

    public RockerSwitchActionToggleSwitchProfile(ProfileCallback callback, ProfileContext context) {
        super(callback, context);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return ROCKERSWITCHACTION_TOGGLE_SWITCH;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        previousState = state.as(OnOffType.class);
    }

    @Override
    public void onTriggerFromHandler(String event) {
        // Ignore released event
        if (!CommonTriggerEvents.RELEASED.equals(event) && isEventValid(event)) {
            OnOffType newState = OnOffType.from(!OnOffType.ON.equals(previousState));
            callback.sendCommand(newState);
            previousState = newState;
        }
    }
}
