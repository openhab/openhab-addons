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
package org.openhab.transform.basicprofiles.internal.profiles;

import static org.openhab.transform.basicprofiles.internal.factory.BasicProfilesFactory.GENERIC_TOGGLE_SWITCH_UID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.types.State;

/**
 * The {@link GenericToggleSwitchTriggerProfile} class implements the behavior when being linked to an item.
 *
 * @author Christoph Weitkamp - Initial contribution
 */
@NonNullByDefault
public class GenericToggleSwitchTriggerProfile extends AbstractTriggerProfile {

    private @Nullable State previousState;

    public GenericToggleSwitchTriggerProfile(ProfileCallback callback, ProfileContext context) {
        super(callback, context);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return GENERIC_TOGGLE_SWITCH_UID;
    }

    @Override
    public void onTriggerFromHandler(String payload) {
        if (events.contains(payload)) {
            OnOffType state = OnOffType.ON.equals(previousState) ? OnOffType.OFF : OnOffType.ON;
            callback.sendCommand(state);
            previousState = state;
        }
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        previousState = state.as(OnOffType.class);
    }
}
