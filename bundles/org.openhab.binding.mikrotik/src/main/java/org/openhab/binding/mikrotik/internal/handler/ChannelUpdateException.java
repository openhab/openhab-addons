package org.openhab.binding.mikrotik.internal.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;

public class ChannelUpdateException extends RuntimeException {
    private final ThingUID thingUID;
    private final ChannelUID channelID;
    private final Throwable innerException;

    public ChannelUpdateException(ThingUID thingUID, ChannelUID channelUID, Throwable innerEx) {
        super(innerEx);
        this.innerException = innerEx;
        this.thingUID = thingUID;
        this.channelID = channelUID;
    }

    @Override
    public String getMessage() {
        return String.format("%s @ %s/%s", super.getMessage(), thingUID, channelID);
    }

    public Throwable getInnerException() {
        return this.innerException;
    }
}
