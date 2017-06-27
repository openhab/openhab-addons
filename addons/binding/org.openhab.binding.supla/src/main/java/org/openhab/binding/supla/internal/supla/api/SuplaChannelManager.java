package org.openhab.binding.supla.internal.supla.api;

import org.openhab.binding.supla.internal.api.ChannelManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SuplaChannelManager implements ChannelManager {
    private final Logger logger = LoggerFactory.getLogger(SuplaChannelManager.class);

    @Override
    public void turnOn(long channelId) {
        logger.warn("turnOn({}) not implemented!", channelId);
    }

    @Override
    public void turnOff(long channelId) {
        logger.warn("turnOff({}) not implemented!", channelId);
    }
}
