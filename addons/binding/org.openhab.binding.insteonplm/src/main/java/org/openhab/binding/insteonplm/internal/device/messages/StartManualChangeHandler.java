package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class StartManualChangeHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(StartManualChangeHandler.class);

    StartManualChangeHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        int cmd2 = msg.getCmd2();
        int upDown = (cmd2 == 0) ? 0 : 2;
        logger.info("{}: dev {} manual state change: {}", nm(), handler.getAddress(), (upDown == 0) ? "DOWN" : "UP");
        handler.updateFeatureState(f, new DecimalType(upDown));
    }
}
