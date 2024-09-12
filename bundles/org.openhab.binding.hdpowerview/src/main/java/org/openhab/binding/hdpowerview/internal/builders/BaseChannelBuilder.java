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
package org.openhab.binding.hdpowerview.internal.builders;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.hdpowerview.internal.HDPowerViewBindingConstants;
import org.openhab.binding.hdpowerview.internal.HDPowerViewTranslationProvider;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link BaseChannelBuilder} class is super class for
 * channel builders.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class BaseChannelBuilder {

    protected final HDPowerViewTranslationProvider translationProvider;
    protected final ChannelGroupUID channelGroupUid;
    protected final ChannelTypeUID channelTypeUid;

    @Nullable
    protected List<Channel> channels;

    protected BaseChannelBuilder(HDPowerViewTranslationProvider translationProvider, ChannelGroupUID channelGroupUid,
            String channelTypeId) {
        this.translationProvider = translationProvider;
        this.channelGroupUid = channelGroupUid;
        this.channelTypeUid = new ChannelTypeUID(HDPowerViewBindingConstants.BINDING_ID, channelTypeId);
    }

    protected List<Channel> getChannelList(int initialCapacity) {
        List<Channel> channels = this.channels;
        return channels != null ? channels : new ArrayList<>(initialCapacity);
    }
}
