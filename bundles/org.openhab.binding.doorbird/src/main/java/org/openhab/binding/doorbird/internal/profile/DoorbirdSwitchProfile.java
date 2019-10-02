/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.doorbird.internal.profile;

import static org.openhab.binding.doorbird.internal.DoorbirdBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.CommonTriggerEvents;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfile;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorbirdSwitchProfile} class implements the switch behavior
 * when a profile is linked to a Switch item.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class DoorbirdSwitchProfile implements TriggerProfile {
    private final Logger logger = LoggerFactory.getLogger(DoorbirdSwitchProfile.class);

    private ProfileCallback callback;

    public DoorbirdSwitchProfile(ProfileCallback callback) {
        this.callback = callback;
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return DoorbirdProfiles.DOORBELL_SWITCH_UID;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
        callback.handleUpdate(state);
    }

    @Override
    public void onTriggerFromHandler(String event) {
        logger.debug("DoorbirdSwitchProfile handling trigger event {} ", event);
        if (CommonTriggerEvents.PRESSED.equals(event)) {
            callback.sendCommand(OnOffType.ON);
        } else if (CommonTriggerEvents.RELEASED.equals(event)) {
            callback.sendCommand(OnOffType.OFF);
        } else if (EVENT_TRIGGERED.equals(event)) {
            callback.sendCommand(OnOffType.ON);
        } else if (EVENT_UNTRIGGERED.equals(event)) {
            callback.sendCommand(OnOffType.OFF);
        }
    }
}
