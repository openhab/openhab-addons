package org.openhab.binding.supla.internal.channels;

import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ChannelBuilder {
    Map<Channel, SuplaChannel> buildChannels(ThingUID thing, Collection<SuplaChannel> channel);

    Optional<Map.Entry<Channel, SuplaChannel>> buildChannel(ThingUID thing, SuplaChannel channel);
}
