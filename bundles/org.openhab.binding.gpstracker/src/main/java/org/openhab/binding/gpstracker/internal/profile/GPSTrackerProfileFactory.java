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

import static org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants.CHANNEL_TYPE_REGION;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.gpstracker.internal.GPSTrackerBindingConstants;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileAdvisor;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.TriggerProfileType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
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
