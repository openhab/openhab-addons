package org.openhab.binding.supla.internal.api;

public interface ChannelManager {
    void turnOn(long channelId);

    void turnOff(long channelId);
}
