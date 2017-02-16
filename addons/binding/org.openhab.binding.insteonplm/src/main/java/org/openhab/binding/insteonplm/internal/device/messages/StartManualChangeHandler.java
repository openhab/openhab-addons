package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
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
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        try {
            int cmd2 = msg.getByte("command2") & 0xff;
            int upDown = (cmd2 == 0) ? 0 : 2;
            logger.info("{}: dev {} manual state change: {}", nm(), handler.getAddress(),
                    (upDown == 0) ? "DOWN" : "UP");
            handler.updateFeatureState(f, new DecimalType(upDown));
        } catch (FieldException e) {
            logger.error("{} error parsing {}: ", nm(), msg, e);
        }
    }
}
