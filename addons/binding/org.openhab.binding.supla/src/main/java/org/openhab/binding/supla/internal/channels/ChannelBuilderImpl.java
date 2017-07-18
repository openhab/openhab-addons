/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.supla.internal.channels;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder.create;
import static org.openhab.binding.supla.SuplaBindingConstants.*;

public final class ChannelBuilderImpl implements ChannelBuilder {

    @Override
    public Map<Channel, SuplaChannel> buildChannels(ThingUID thingUID, Collection<SuplaChannel> channel) {
        return channel.stream().map(c -> buildChannel(thingUID, c)).filter(Optional::isPresent).map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Optional<Map.Entry<Channel, SuplaChannel>> buildChannel(ThingUID thingUID, SuplaChannel suplaChannel) {
        if (RELAY_CHANNEL_TYPE.equals(suplaChannel.getType().getName())) {
            final String channelId = findChannelId(suplaChannel);

            final ChannelUID channelUID = new ChannelUID(thingUID, channelId);
            final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, channelId);
            final Channel channel = create(channelUID, "Switch")
                    .withType(channelTypeUID)
                    .build();
            return Optional.of(new Entry(channel, suplaChannel));
        }

        return Optional.empty();
    }

    private static String findChannelId(SuplaChannel suplaChannel) {
        final String channelId;
        if (LIGHT_CHANNEL_FUNCTION.equals(suplaChannel.getFunction().getName())) {
            channelId = LIGHT_CHANNEL_ID;
        } else {
            channelId = SWITCH_CHANNEL_ID;
        }
        return channelId;
    }

    private static class Entry implements Map.Entry<Channel, SuplaChannel> {
        private final Channel channel;
        private SuplaChannel suplaChannel;

        private Entry(Channel channel, SuplaChannel suplaChannel) {
            this.channel = channel;
            this.suplaChannel = suplaChannel;
        }

        @Override
        public Channel getKey() {
            return channel;
        }

        @Override
        public SuplaChannel getValue() {
            return suplaChannel;
        }

        @Override
        public SuplaChannel setValue(SuplaChannel value) {
            SuplaChannel old = suplaChannel;
            suplaChannel = value;
            return old;
        }
    }
}
