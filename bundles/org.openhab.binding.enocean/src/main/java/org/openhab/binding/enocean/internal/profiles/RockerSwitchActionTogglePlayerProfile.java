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
package org.openhab.binding.enocean.internal.profiles;

import static org.openhab.binding.enocean.internal.profiles.EnOceanProfiles.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.thing.CommonTriggerEvents;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.State;

/**
 * The {@link RockerSwitchActionTogglePlayerProfile} is used for channel rockerSwitchAction to be able to bind this
 * trigger channel directly to a player item
 * 
 * @author Daniel Weber - Initial contribution
 */
@NonNullByDefault
public class RockerSwitchActionTogglePlayerProfile extends RockerSwitchActionBaseProfile {

    public RockerSwitchActionTogglePlayerProfile(ProfileCallback callback, ProfileContext context) {
        super(callback, context);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return ROCKERSWITCHACTION_TOGGLE_PLAYER;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        previousState = state.as(PlayPauseType.class);
    }

    @Override
    public void onTriggerFromHandler(String event) {
        // Ignore released event
        if (!CommonTriggerEvents.RELEASED.equals(event) && isEventValid(event)) {
            PlayPauseType newState = PlayPauseType.PLAY.equals(previousState) ? PlayPauseType.PAUSE
                    : PlayPauseType.PLAY;
            callback.sendCommand(newState);
            previousState = newState;
        }
    }
}
