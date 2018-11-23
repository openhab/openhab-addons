/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.profiles;

import static org.openhab.binding.enocean.internal.EnOceanBindingConstants.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.CoreItemFactory;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.DefaultSystemChannelTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.Profile;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileCallback;
import org.eclipse.smarthome.core.thing.profiles.ProfileContext;
import org.eclipse.smarthome.core.thing.profiles.ProfileFactory;
import org.eclipse.smarthome.core.thing.profiles.ProfileType;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeProvider;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Factory class for creating and advising profiles
 *
 * @author Daniel Weber - Initial contribution
 */
@Component(service = { ProfileFactory.class, ProfileTypeProvider.class, ProfileAdvisor.class })
public class EnOceanProfileFactory implements ProfileFactory, ProfileAdvisor, ProfileTypeProvider {

    private ChannelTypeRegistry channelTypeRegistry;

    @Override
    public @Nullable Profile createProfile(ProfileTypeUID profileTypeUID, ProfileCallback callback,
            ProfileContext profileContext) {

        if (profileTypeUID.equals(EnOceanProfileTypes.RockerSwitchToPlayPause)) {
            return new RockerSwitchToPlayPauseProfile(callback);
        } else if (profileTypeUID.equals(EnOceanProfileTypes.RockerSwitchFromOnOff)) {
            return new RockerSwitchFromOnOffProfile(callback, profileContext);
        } else if (profileTypeUID.equals(EnOceanProfileTypes.RockerSwitchFromRollershutter)) {
            return new RockerSwitchFromUpDownProfile(callback, profileContext);
        }

        return null;
    }

    @Override
    public Collection<@NonNull ProfileTypeUID> getSupportedProfileTypeUIDs() {
        return SUPPORTED_PROFILETYPES_UIDS;
    }

    @Nullable
    @Override
    public ProfileTypeUID getSuggestedProfileTypeUID(@Nullable ChannelType channelType, @Nullable String itemType) {

        if (channelType == null) {
            return null;
        }

        if (DefaultSystemChannelTypeProvider.SYSTEM_RAWROCKER.getUID().equals(channelType.getUID())) {
            if (CoreItemFactory.PLAYER.equalsIgnoreCase(itemType)) {
                return EnOceanProfileTypes.RockerSwitchToPlayPause;
            }
        } else if (VirtualRockerSwitchChannelType.equals(channelType.getUID())) {
            if (CoreItemFactory.SWITCH.equalsIgnoreCase(itemType)) {
                return EnOceanProfileTypes.RockerSwitchFromOnOff;
            } else if (CoreItemFactory.ROLLERSHUTTER.equalsIgnoreCase(itemType)) {
                return EnOceanProfileTypes.RockerSwitchFromRollershutter;
            }
        }

        return null;
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        ChannelType channelType = channelTypeRegistry.getChannelType(channel.getChannelTypeUID());
        return getSuggestedProfileTypeUID(channelType, itemType);
    }

    @Override
    public Collection<@NonNull ProfileType> getProfileTypes(@Nullable Locale locale) {
        return new HashSet<ProfileType>(Arrays.asList(EnOceanProfileTypes.RockerSwitchToPlayPauseType,
                EnOceanProfileTypes.RockerSwitchFromOnOffType, EnOceanProfileTypes.RockerSwitchFromRollershutterType));
    }

    @Reference
    protected void setChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = channelTypeRegistry;
    }

    protected void unsetChannelTypeRegistry(ChannelTypeRegistry channelTypeRegistry) {
        this.channelTypeRegistry = null;
    }
}
