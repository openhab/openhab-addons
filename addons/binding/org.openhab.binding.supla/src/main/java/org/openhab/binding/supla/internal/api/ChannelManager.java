package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;

public interface ChannelManager {
    void turnOn(long channelId);

    void turnOff(long channelId);

    SuplaChannelStatus obtainChannelStatus(long channelId);
}
