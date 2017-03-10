package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles Dimmer replies to status requests.
 * In the dimmers case the command2 byte represents the light level from 0-255
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class DimmerRequestReplyHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(DimmerRequestReplyHandler.class);

    DimmerRequestReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        int cmd2 = msg.getCmd2();
        if (cmd2 == 0xfe) {
            // sometimes dimmer devices are returning 0xfe when on instead of 0xff
            cmd2 = 0xff;
        }

        if (cmd2 == 0) {
            logger.info("{}: set device {} to level 0", nm(), handler.getAddress());
            handler.updateFeatureState(f, PercentType.ZERO);
        } else if (cmd2 == 0xff) {
            logger.info("{}: set device {} to level 100", nm(), handler.getAddress());
            handler.updateFeatureState(f, PercentType.HUNDRED);
        } else {
            int level = cmd2 * 100 / 255;
            if (level == 0) {
                level = 1;
            }
            logger.info("{}: set device {} to level {}", nm(), handler.getAddress(), level);
            handler.updateFeatureState(f, new PercentType(level));
        }
    }
}
