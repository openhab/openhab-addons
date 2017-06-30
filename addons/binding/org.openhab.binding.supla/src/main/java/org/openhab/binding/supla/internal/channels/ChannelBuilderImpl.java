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
            if (LIGHT_CHANNEL_FUNCTION.equals(suplaChannel.getFunction().getName())) {
                final ChannelUID channelUID = new ChannelUID(thingUID, LIGHT_CHANNEL_ID);
                final ChannelTypeUID channelTypeUID = new ChannelTypeUID(BINDING_ID, LIGHT_CHANNEL_ID);
                final Channel channel = org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder .create(channelUID, "Switch")
                        .withType(channelTypeUID)
                        .build();
                return Optional.of(new Entry(channel, suplaChannel));
            } else {
                // TODO return switch-channel
            }
        }

        return Optional.empty();
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
