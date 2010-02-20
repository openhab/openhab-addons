/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CHANNEL_TYPE_REGION;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.*;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link GPSTrackerProfileFactory} class defines and provides switch profile and its type of this binding.
 *
 * @author Gabor Bicskei - Initial contribution
 */
@NonNullByDefault
@Component
public class GPSTrackerProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {
    /**
     * Profile UID for trigger events
     */
    static final ProfileTypeUID UID_TRIGGER_SWITCH = new ProfileTypeUID(GPSTrackerBindingConstants.BINDING_ID,
            "trigger-geofence");

    /**
     * Profile type for trigger events
     */
    private static final TriggerProfileType TRIGGER_SWITCH_TYPE = ProfileTypeBuilder
            .newTrigger(UID_TRIGGER_SWITCH, "Geofence").withSupportedItemTypes(CoreItemFactory.SWITCH)
            .withSupportedChannelTypeUIDs(CHANNEL_TYPE_REGION).build();

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Stream.of(UID_TRIGGER_SWITCH).collect(Collectors.toSet());
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Stream.of(TRIGGER_SWITCH_TYPE).collect(Collectors.toSet());
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channel.getChannelTypeUID(), itemType);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channelType.getUID(), itemType);
    }

    private @Nullable ProfileTypeUID getSuggestedProfileTypeUID(@Nullable ChannelTypeUID channelTypeUID,
            @Nullable String itemType) {
        if (CoreItemFactory.SWITCH.equals(itemType) && CHANNEL_TYPE_REGION.equals(channelTypeUID)) {
            return UID_TRIGGER_SWITCH;
        }
        return null;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (UID_TRIGGER_SWITCH.equals(profileTypeUID)) {
            return new GPSTrackerTriggerSwitchProfile(callback, profileContext);
        }
        return null;
    }
}
