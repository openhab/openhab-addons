package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;

import java.util.Optional;

public interface ChannelManager {
    void turnOn(long channelId);

    void turnOff(long channelId);

    Optional<SuplaChannelStatus> obtainChannelStatus(long channelId);
}
