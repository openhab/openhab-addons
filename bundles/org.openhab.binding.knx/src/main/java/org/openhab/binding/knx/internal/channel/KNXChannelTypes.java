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
package org.openhab.binding.knx.internal.channel;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Helper class to find the matching {@link KNXChannelType} for any given {@link ChannelTypeUID}.
 *
 * @author Simon Kaufmann - initial contribution and API.
 *
 */
@NonNullByDefault
public final class KNXChannelTypes {

    private static final Set<KNXChannelType> TYPES = Set.of(//
            new TypeColor(), //
            new TypeContact(), //
            new TypeDateTime(), //
            new TypeDimmer(), //
            new TypeNumber(), //
            new TypeRollershutter(), //
            new TypeString(), //
            new TypeSwitch() //
    );

    private KNXChannelTypes() {
        // prevent instantiation
    }

    public static KNXChannelType getKnxChannelType(Channel channel) throws IllegalArgumentException {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            throw new IllegalArgumentException("Could not determine ChannelTypeUID for channel " + channel.getUID());
        }

        for (KNXChannelType c : TYPES) {
            if (c.getChannelIDs().contains(channelTypeUID.getId())) {
                return c;
            }
        }
        throw new IllegalArgumentException(channelTypeUID.getId() + " is not a valid value channel type ID");
    }
}
