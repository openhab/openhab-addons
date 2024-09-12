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

import static org.openhab.binding.knx.internal.KNXBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.profiles.ProfileAdvisor;
import org.openhab.core.thing.profiles.ProfileTypeUID;
import org.openhab.core.thing.profiles.SystemProfiles;
import org.openhab.core.thing.type.ChannelType;
import org.openhab.core.thing.type.ChannelTypeUID;
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
        return getSuggestedProfileTypeUID(channelTypeUID);
    }

    @Override
    public @Nullable ProfileTypeUID getSuggestedProfileTypeUID(ChannelType channelType, @Nullable String itemType) {
        return getSuggestedProfileTypeUID(channelType.getUID());
    }

    private ProfileTypeUID getSuggestedProfileTypeUID(ChannelTypeUID channelTypeUID) {
        if (isControl(channelTypeUID)) {
            if (CHANNEL_CONTACT_CONTROL.equals(channelTypeUID.getId())) {
                // Special handling for contact-control, as contact items do not send to bus:
                // contact-control need to send out on postUpdate, as contact-control switches external device
                return KNXProfileFactory.UID_CONTACT_CONTROL;
            }
            return SystemProfiles.FOLLOW;
        } else {
            return SystemProfiles.DEFAULT;
        }
    }

    public static boolean isControl(ChannelTypeUID channelTypeUID) {
        return CONTROL_CHANNEL_TYPES.contains(channelTypeUID.getId());
    }
}
