package org.openhab.binding.supla.internal.api;

import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannelStatus;

import java.util.Optional;

public interface ChannelManager {
    void turnOn(SuplaChannel channel);

    void turnOff(SuplaChannel channel);

    Optional<SuplaChannelStatus> obtainChannelStatus(SuplaChannel channel);
}
