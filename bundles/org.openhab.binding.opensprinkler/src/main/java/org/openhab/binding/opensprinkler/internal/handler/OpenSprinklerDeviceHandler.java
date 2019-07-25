package org.openhab.binding.opensprinkler.internal.handler;

import static org.openhab.binding.opensprinkler.internal.OpenSprinklerBindingConstants.SENSOR_RAIN;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenSprinklerDeviceHandler extends OpenSprinklerBaseHandler {
    private final Logger logger = LoggerFactory.getLogger(OpenSprinklerDeviceHandler.class);

    public OpenSprinklerDeviceHandler(Thing thing) {
        super(thing);
    }

    @Override
    protected void updateChannel(@NonNull ChannelUID channel) {
        switch (channel.getIdWithoutGroup()) {
            case SENSOR_RAIN:
                try {
                    if (getApi().isRainDetected()) {
                        updateState(channel, OnOffType.ON);
                    } else {
                        updateState(channel, OnOffType.OFF);
                    }
                } catch (Exception e) {
                    logger.debug("Could not update rainsensor", e);
                }
            default:
                logger.debug("Not updating unknown channel " + channel.getAsString());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // nothing to do here
    }

}
