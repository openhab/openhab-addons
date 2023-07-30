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

import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Helper class to find the matching {@link KNXChannel} for any given {@link ChannelTypeUID}.
 *
 * @author Simon Kaufmann - Initial contribution
 * @author Jan N. Klug - Refactored to factory class
 *
 */
@NonNullByDefault
public final class KNXChannelFactory {

    private static final Map<Set<String>, Function<Channel, KNXChannel>> TYPES = Map.ofEntries( //
            Map.entry(TypeColor.SUPPORTED_CHANNEL_TYPES, TypeColor::new), //
            Map.entry(TypeContact.SUPPORTED_CHANNEL_TYPES, TypeContact::new), //
            Map.entry(TypeDateTime.SUPPORTED_CHANNEL_TYPES, TypeDateTime::new), //
            Map.entry(TypeDimmer.SUPPORTED_CHANNEL_TYPES, TypeDimmer::new), //
            Map.entry(TypeNumber.SUPPORTED_CHANNEL_TYPES, TypeNumber::new), //
            Map.entry(TypeRollershutter.SUPPORTED_CHANNEL_TYPES, TypeRollershutter::new), //
            Map.entry(TypeString.SUPPORTED_CHANNEL_TYPES, TypeString::new), //
            Map.entry(TypeSwitch.SUPPORTED_CHANNEL_TYPES, TypeSwitch::new));

    private KNXChannelFactory() {
        // prevent instantiation
    }

    public static KNXChannel createKnxChannel(Channel channel) throws IllegalArgumentException {
        ChannelTypeUID channelTypeUID = channel.getChannelTypeUID();
        if (channelTypeUID == null) {
            throw new IllegalArgumentException("Could not determine ChannelTypeUID for channel " + channel.getUID());
        }

        String channelType = channelTypeUID.getId();

        Function<Channel, KNXChannel> supplier = TYPES.entrySet().stream().filter(e -> e.getKey().contains(channelType))
                .map(Map.Entry::getValue).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(channelTypeUID + " is not a valid channel type ID"));

        return supplier.apply(channel);
    }
}
