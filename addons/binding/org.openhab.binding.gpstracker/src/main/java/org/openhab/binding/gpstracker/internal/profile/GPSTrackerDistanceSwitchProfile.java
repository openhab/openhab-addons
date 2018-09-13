/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.gpstracker.internal.profile;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.QuantityType;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.StateProfile;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.gpstracker.internal.config.ConfigHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link GPSTrackerDistanceSwitchProfile} class implements the behavior when being linked to a Switch item.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
public class GPSTrackerDistanceSwitchProfile implements StateProfile {
    /**
     * Class logger
     */
    private final Logger logger = LoggerFactory.getLogger(GPSTrackerDistanceSwitchProfile.class);

    /**
     * Callback
     */
    private ProfileCallback callback;

    /**
     * Link radius value
     */
    private Integer regionRadius;

    /**
     * Constructor.
     *
     * @param callback Callback
     * @param context Context
     */
    GPSTrackerDistanceSwitchProfile(ProfileCallback callback, ProfileContext context) {
        this.callback = callback;
        this.regionRadius = ConfigHelper.getRegionRadius(context.getConfiguration());
        logger.debug("Distance switch profile created for region with radius {}", regionRadius);
    }

    @Override
    public ProfileTypeUID getProfileTypeUID() {
        return GPSTrackerProfileFactory.UID_DISTANCE_SWITCH;
    }

    @Override
    public void onStateUpdateFromItem(State state) {
    }

    @Override
    public void onCommandFromItem(Command command) {
    }

    @Override
    public void onCommandFromHandler(Command command) {
    }

    @Override
    public void onStateUpdateFromHandler(State state) {
        QuantityType d = (QuantityType) state;
        boolean inside = regionRadius > d.intValue();
        callback.sendCommand(inside ? OnOffType.ON : OnOffType.OFF);
        logger.debug("Distance channel value update handled by profile. Tracker inside region with radius {}m: {}", regionRadius, inside);
    }
}
