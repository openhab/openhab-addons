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
package org.openhab.binding.leapmotion.internal;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
 * This class defines and provides all profiles and their types of this binding.
 *
 * @author Kai Kreuzer - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class LeapMotionProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {

    static final ProfileTypeUID UID_SWITCH = new ProfileTypeUID(LeapMotionBindingConstants.BINDING_ID, "switch");
    static final ProfileTypeUID UID_DIMMER = new ProfileTypeUID(LeapMotionBindingConstants.BINDING_ID, "dimmer");
    static final ProfileTypeUID UID_COLOR = new ProfileTypeUID(LeapMotionBindingConstants.BINDING_ID, "color");

    private static final TriggerProfileType SWITCH_TYPE = ProfileTypeBuilder.newTrigger(UID_SWITCH, "Toggle Switch")
            .withSupportedItemTypes(CoreItemFactory.SWITCH, CoreItemFactory.DIMMER, CoreItemFactory.COLOR)
            .withSupportedChannelTypeUIDs(LeapMotionBindingConstants.CHANNEL_GESTURE_UID).build();

    private static final TriggerProfileType DIMMER_TYPE = ProfileTypeBuilder.newTrigger(UID_DIMMER, "Dimmer Control")
            .withSupportedItemTypes(CoreItemFactory.DIMMER, CoreItemFactory.COLOR)
            .withSupportedChannelTypeUIDs(LeapMotionBindingConstants.CHANNEL_GESTURE_UID).build();

    private static final TriggerProfileType COLOR_TYPE = ProfileTypeBuilder.newTrigger(UID_COLOR, "Color Control")
            .withSupportedItemTypes(CoreItemFactory.COLOR)
            .withSupportedChannelTypeUIDs(LeapMotionBindingConstants.CHANNEL_GESTURE_UID).build();

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Stream.of(UID_SWITCH, UID_DIMMER, UID_COLOR).collect(Collectors.toSet());
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Stream.of(SWITCH_TYPE, DIMMER_TYPE, COLOR_TYPE).collect(Collectors.toSet());
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
        if (LeapMotionBindingConstants.CHANNEL_GESTURE_UID.equals(channelTypeUID) && itemType != null) {
            switch (itemType) {
                case CoreItemFactory.SWITCH:
                    return UID_SWITCH;
                case CoreItemFactory.DIMMER:
                    return UID_DIMMER;
                case CoreItemFactory.COLOR:
                    return UID_COLOR;
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (UID_SWITCH.equals(profileTypeUID)) {
            return new LeapMotionSwitchProfile(callback);
        } else if (UID_DIMMER.equals(profileTypeUID)) {
            return new LeapMotionDimmerProfile(callback, profileContext);
        } else if (UID_COLOR.equals(profileTypeUID)) {
            return new LeapMotionColorProfile(callback);
        } else {
            return null;
        }
    }
}
