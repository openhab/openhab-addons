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
package org.openhab.binding.knx.internal.profiles;

import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.knx.internal.KNXBindingConstants;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.profiles.Profile;
import org.openhab.core.thing.profiles.ProfileAdvisor;
import org.openhab.core.thing.profiles.ProfileCallback;
import org.openhab.core.thing.profiles.ProfileContext;
import org.openhab.core.thing.profiles.ProfileFactory;
import org.openhab.core.thing.profiles.ProfileType;
import org.openhab.core.thing.profiles.ProfileTypeBuilder;
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.StateProfileType;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * This class defines and provides specialized KNX profiles.
 *
 * @author Holger Friedrich - Initial contribution
 *
 */
@NonNullByDefault
@Component
public class KNXProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {

    static final ProfileTypeUID UID_CONTACT_CONTROL = new ProfileTypeUID(KNXBindingConstants.BINDING_ID,
            "contact-control");

    private static final StateProfileType CONTACT_CONTROL_TYPE = ProfileTypeBuilder
            .newState(UID_CONTACT_CONTROL, "contact-control").withSupportedItemTypes(CoreItemFactory.CONTACT)
            .withSupportedChannelTypeUIDs(KNXBindingConstants.CHANNEL_CONTACT_CONTROL_UID).build();

    private final ThingRegistry thingRegistry;

    @Activate
    public KNXProfileFactory(@Reference ThingRegistry thingRegistry) {
        this.thingRegistry = thingRegistry;
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Stream.of(UID_CONTACT_CONTROL).collect(Collectors.toSet());
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Stream.of(CONTACT_CONTROL_TYPE).collect(Collectors.toSet());
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
        if (KNXBindingConstants.CHANNEL_CONTACT_CONTROL_UID.equals(channelTypeUID) && itemType != null) {
            switch (itemType) {
                case CoreItemFactory.CONTACT:
                    return UID_CONTACT_CONTROL;
                default:
                    return null;
            }
        }
        return null;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (UID_CONTACT_CONTROL.equals(profileTypeUID)) {
            return new KNXContactControlProfile(callback, thingRegistry);
        } else {
            return null;
        }
    }
}
