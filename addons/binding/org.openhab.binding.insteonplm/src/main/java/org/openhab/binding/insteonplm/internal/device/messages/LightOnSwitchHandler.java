package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class LightOnSwitchHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LightOnSwitchHandler.class);

    LightOnSwitchHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        if (isMybutton(msg)) {
            logger.info("{}: device {} was switched on {}.", nm(), handler.getAddress(), f.getName());
            handler.updateFeatureState(f, OnOffType.ON);
        } else {
            logger.debug("ignored message: {}", isMybutton(msg));
        }
    }
}
