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
package org.openhab.binding.gpstracker.internal.profile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfile;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GPSTrackerTriggerSwitchProfile} class implements the behavior when being linked to a Switch item.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class GPSTrackerTriggerSwitchProfile implements TriggerProfile {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(GPSTrackerTriggerSwitchProfile.class);

    /**
     * Callback
     */
    private ProfileCallback callback;

    /**
     * Link region name
     */
    private String regionName;

    /**
     * Constructor.
     *
     * @param callback Callback
     * @param context Context
     */
    GPSTrackerTriggerSwitchProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.regionName = ConfigHelper.getRegionName(context.getConfiguration());
        logger.debug("Trigger switch profile created for region {}", regionName);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return GPSTrackerProfileFactory.UID_TRIGGER_SWITCH;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onTriggerFromHandler(String payload) {
        if (payload.startsWith(regionName)) {
            OnOffType state = payload.endsWith("enter") ? OnOffType.ON : OnOffType.OFF;
            callback.sendCommand(state);
            logger.debug("Transition trigger {} handled for region {} by profile: {}", payload, regionName, state);
        }
    }
}
