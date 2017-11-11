/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.jenocean.profiles;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfileType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link JEnOceanProfiles} hold the profiles types and profile type UIDs of all JEnOcean profiles.
 *
 * @author Jan Kemmler - Initial contribution
 */
public interface JEnOceanProfiles {

    ProfileTypeUID ROCKER_TO_ON_OFF = new ProfileTypeUID("jenocean", "rocker-to-ONOFF");

    ProfileTypeUID ROCKER_TO_DIMMER = new ProfileTypeUID("jenocean", "rocker-to-dimmer");

    public static final TriggerProfileType ROCKER_TO_ON_OFF_TYPE = new TriggerProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return ROCKER_TO_ON_OFF;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return Collections.singleton("Switch");
        }

        @Override
        public Collection<ChannelTypeUID> getSupportedChannelTypeUIDs() {
            return Collections.singleton(new ChannelTypeUID("jenocean", "rocker_channel"));
        }

        @Override
        public String getLabel() {
            return "Rocker to ON and OFF Profile";
        }
    };

    public static final TriggerProfileType ROCKER_TO_DIMMER_TYPE = new TriggerProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return ROCKER_TO_DIMMER;
        }

        @Override
        public Collection<String> getSupportedItemTypes() {
            return Collections.singleton("Dimmer");
        }

        @Override
        public Collection<ChannelTypeUID> getSupportedChannelTypeUIDs() {
            return Collections.singleton(new ChannelTypeUID("jenocean", "rocker_channel"));
        }

        @Override
        public String getLabel() {
            return "Rocker to Dimmer Profile";
        }
    };

}
