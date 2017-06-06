package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
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
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        handler.updateFeatureState(f, OpenClosedType.CLOSED);
        SendInsteonMessage mess = new SendInsteonMessage(handler.getAddress(), new InsteonFlags(),
                StandardInsteonMessages.LightOnWithRamp, (byte) 00);
        mess.setQuietTime(500);
        handler.enqueueMessage(mess);
    }
}
