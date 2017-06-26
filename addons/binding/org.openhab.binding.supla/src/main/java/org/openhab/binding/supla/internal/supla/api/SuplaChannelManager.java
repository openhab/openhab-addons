package org.openhab.binding.supla.internal.supla.api;

import org.openhab.binding.supla.internal.api.ChannelManager;
import org.openhab.binding.supla.internal.supla.entities.SuplaChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuplaChannelManager implements ChannelManager {
    private final Logger logger = LoggerFactory.getLogger(SuplaChannelManager.class);

    @Override
    public void turnOn(SuplaChannel channel) {
        logger.warn("turnOn({}) not implemented!", channel);
    }

    @Override
    public void turnOff(SuplaChannel channel) {
        logger.warn("turnOff({}) not implemented!", channel);
    }
}
