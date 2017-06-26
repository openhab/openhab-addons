package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;

public interface ChannelManager {
    void turnOn(SuplaChannel channel);

    void turnOff(SuplaChannel channel);
}
