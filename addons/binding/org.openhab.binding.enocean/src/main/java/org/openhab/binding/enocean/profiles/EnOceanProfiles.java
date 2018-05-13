/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.profiles;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.TriggerProfileType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link EnOceanProfiles} hold the profiles types and profile type UIDs of all EnOcean profiles.
 *
 * @author Jan Kemmler - Initial contribution
 */
@NonNullByDefault
public interface EnOceanProfiles {

    ProfileTypeUID ROCKER_TO_ON_OFF = new ProfileTypeUID("enocean", "rocker-to-ONOFF");

    ProfileTypeUID ROCKER_TO_DIMMER = new ProfileTypeUID("enocean", "rocker-to-dimmer");

    public static final TriggerProfileType ROCKER_TO_ON_OFF_TYPE = new TriggerProfileType() {
        @Override
        public ProfileTypeUID getUID() {
            return ROCKER_TO_ON_OFF;
        }

        @Override
        @SuppressWarnings("null")
        public Collection<String> getSupportedItemTypes() {
            return Collections.singleton("Switch");
        }

        @Override
        @SuppressWarnings("null")
        public Collection<ChannelTypeUID> getSupportedChannelTypeUIDs() {
            return Collections.singleton(new ChannelTypeUID("enocean", "rocker_channel"));
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
        @SuppressWarnings("null")
        public Collection<String> getSupportedItemTypes() {
            return Collections.singleton("Dimmer");
        }

        @Override
        @SuppressWarnings("null")
        public Collection<ChannelTypeUID> getSupportedChannelTypeUIDs() {
            return Collections.singleton(new ChannelTypeUID("enocean", "rocker_channel"));
        }

        @Override
        public String getLabel() {
            return "Rocker to Dimmer Profile";
        }
    };

}
