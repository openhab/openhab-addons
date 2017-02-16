package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Process X10 messages that are generated when another controller
 * changes the state of an X10 device.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10OnHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(X10OnHandler.class);

    X10OnHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        InsteonAddress a = handler.getAddress();
        logger.info("{}: set X10 device {} to ON", nm(), a);
        handler.updateFeatureState(f, OnOffType.ON);
    }
}
