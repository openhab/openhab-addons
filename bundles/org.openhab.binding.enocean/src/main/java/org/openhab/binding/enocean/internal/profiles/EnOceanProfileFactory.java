/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enocean.internal.profiles;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;
import static org.openhab.binding.enocean.internal.profiles.EnOceanProfiles.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

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
import org.openhab.core.thing.profiles.ProfileTypeProvider;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link EnOceanProfileFactory} class creates EnOceanProfiles
 *
 * @author Daniel Weber - Initial contribution
 */
@Component
@NonNullByDefault
public class EnOceanProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {

    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Set.of(ROCKERSWITCHACTION_TOGGLE_SWITCH_TYPE,
            ROCKERSWITCHACTION_TOGGLE_PLAYER_TYPE);

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Set.of(ROCKERSWITCHACTION_TOGGLE_SWITCH,
            ROCKERSWITCHACTION_TOGGLE_PLAYER);

    private final ChannelTypeRegistry channelTypeRegistry;

    @Activate
    public EnOceanProfileFactory(final @Reference ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return Collections.unmodifiableList(SUPPORTED_PROFILE_TYPES.stream().collect(Collectors.toList()));
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
        if (channelType == null) {
            return null;
        }

        return getSuggestedProfileTypeUID(channelType, itemType);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        if (CHANNELTYPE_ROCKERSWITCH_ACTION_UID.equals(channelType.getUID())) {
            if (CoreItemFactory.PLAYER.equalsIgnoreCase(itemType)) {
                return ROCKERSWITCHACTION_TOGGLE_PLAYER;
            } else if (CoreItemFactory.SWITCH.equalsIgnoreCase(itemType)) {
                return ROCKERSWITCHACTION_TOGGLE_SWITCH;
            }
        }

        return null;
    }

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {
        if (ROCKERSWITCHACTION_TOGGLE_PLAYER.equals(profileTypeUID)) {
            return new RockerSwitchActionTogglePlayerProfile(callback, profileContext);
        } else if (ROCKERSWITCHACTION_TOGGLE_SWITCH.equals(profileTypeUID)) {
            return new RockerSwitchActionToggleSwitchProfile(callback, profileContext);
        } else {
            return null;
        }
    }

    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return Collections.unmodifiableList(SUPPORTED_PROFILE_TYPE_UIDS.stream().collect(Collectors.toList()));
    }
}
