package org.openhab.binding.supla.internal.channels;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.openhab.binding.supla.SuplaBindingConstants.LIGHT_CHANNEL_FUNCTION;
import static org.openhab.binding.supla.SuplaBindingConstants.RELAY_CHANNEL_TYPE;

public final class ChannelBuilderImpl implements ChannelBuilder {

    @Override
    public Map<Channel, SuplaChannel> buildChannels(Collection<SuplaChannel> channel) {
        return channel.stream()
                .map(this::buildChannel)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Optional<Map.Entry<Channel, SuplaChannel>> buildChannel(SuplaChannel channel) {
        if (RELAY_CHANNEL_TYPE.equals(channel.getType().getName())) {
            if (LIGHT_CHANNEL_FUNCTION.equals(channel.getFunction().getName())) {
                // TODO return light-channel
            } else {
                // TODO return switch-channel
            }
        }

        return Optional.empty();
    }
}
