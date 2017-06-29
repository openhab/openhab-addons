package org.openhab.binding.supla.internal.channels;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ChannelBuilder {
    List<Channel> buildChannels(Collection<SuplaChannel> channel);

    Optional<Channel> buildChannel(SuplaChannel channel);
}
