package org.openhab.binding.insteonplm.internal.device.messages;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
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
public class ClosedSleepingContactHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClosedSleepingContactHandler.class);

    ClosedSleepingContactHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        handler.updateFeatureState(f, OpenClosedType.CLOSED);
        try {
            Message mess = handler.getMessageFactory().makeExtendedMessage((byte) 0x1F, (byte) 0x2e, (byte) 00,
                    handler.getAddress());
            mess.setQuietTime(500);
            handler.enqueueMessage(mess);
        } catch (FieldException | IOException e) {
            logger.error("i/o issues sending the message to device {}", e, handler.getAddress());
        }
    }
}
