/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.internal.profiles;

import static org.openhab.binding.knx.KNXBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.profiles.ProfileAdvisor;
import org.eclipse.smarthome.core.thing.profiles.ProfileTypeUID;
import org.eclipse.smarthome.core.thing.profiles.SystemProfiles;
import org.eclipse.smarthome.core.thing.type.ChannelType;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.osgi.service.component.annotations.Component;

/**
 * Advisor, provider and factory for the specialized KNX profiles.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@Component
@NonNullByDefault
public class KNXProfileAdvisor implements ProfileAdvisor {

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(Channel channel, @Nullable String itemType) {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null || !channelTypeUID.getBindingId().equals(BINDING_ID)) {
            return null;
        }
        return getSuggestedProfieTypeUID(channelTypeUID);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return getSuggestedProfieTypeUID(channelType.getUID());
    }

    private ProfileTypeUID getSuggestedProfieTypeUID(ChannelTypeUID channelTypeUID) {
        if (isControl(channelTypeUID)) {
            return SystemProfiles.FOLLOW;
        } else {
            return SystemProfiles.DEFAULT;
        }
    }

    public static boolean isControl(ChannelTypeUID channelTypeUID) {
        return CONTROL_CHANNEL_TYPES.contains(channelTypeUID.getId());
    }

}
