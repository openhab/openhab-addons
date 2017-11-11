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
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang.NotImplementedException;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link JEnOceanProfileFactory} is responsible for creating profiles.
 *
 * @author Jan Kemmler - Initial contribution
 */
@Component(service = ProfileFactory.class)
public class JEnOceanProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {

    private static final Set<ProfileTypeUID> SUPPORTED_PROFILE_TYPE_UIDS = Stream
            .of(JEnOceanProfiles.ROCKER_TO_ON_OFF, JEnOceanProfiles.ROCKER_TO_DIMMER).collect(Collectors.toSet());

    private static final Set<ProfileType> SUPPORTED_PROFILE_TYPES = Stream
            .of(JEnOceanProfiles.ROCKER_TO_ON_OFF_TYPE, JEnOceanProfiles.ROCKER_TO_DIMMER_TYPE)
            .collect(Collectors.toSet());

    @Override
    @Nullable
    public Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback, Configuration configuration) {
        if (JEnOceanProfiles.ROCKER_TO_ON_OFF.equals(profileTypeUID)) {
            return new RockerChannelToOnOffProfile(callback);
        }
        if (JEnOceanProfiles.ROCKER_TO_DIMMER.equals(profileTypeUID)) {
            return new RockerChannelToDimmerProfile(callback);
        } else {
            return null;
        }
    }

    /**
     * Return the identifiers of all supported profile types
     *
     * @return a collection of all profile type identifier which this class is capable of creating
     */
    @Override
    public Collection<ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILE_TYPE_UIDS;
    }

    @Override
    public Collection<ProfileType> getProfileTypes(@Nullable Locale locale) {
        return SUPPORTED_PROFILE_TYPES;
    }

    /*
     * @Override
     * public @NonNull Collection<@NonNull ProfileTypeUID> getApplicableProfileTypeUIDs(@NonNull ItemChannelLink link,
     *
     * @NonNull Item item, @NonNull Channel channel) {
     * switch (channel.getKind()) {
     * case STATE:
     * return Collections.emptyList();
     * case TRIGGER:
     * if (JEnOceanBindingConstants.THING_TYPE_ROCKER_SWITCH.equals(channel.getChannelTypeUID())) {
     * return Collections.singletonList(RockerChannelToOnOffProfile.UID);
     * }
     * break;
     * default:
     * throw new NotImplementedException();
     * }
     * return Collections.emptyList();
     * }
     */

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(@NonNull Channel channel, @Nullable String itemType) {
        switch (channel.getKind()) {
            case STATE:
                return null;
            case TRIGGER:
                if (new ChannelTypeUID("jenocean", "rocker_channel").equals(channel.getChannelTypeUID())) {
                    if ("Switch".equalsIgnoreCase(itemType)) {
                        return JEnOceanProfiles.ROCKER_TO_ON_OFF;
                    } else if ("Dimmer".equalsIgnoreCase(itemType)) {
                        return JEnOceanProfiles.ROCKER_TO_DIMMER;
                    }
                }
                break;
            default:
                throw new NotImplementedException();
        }
        return null;
    }

}
