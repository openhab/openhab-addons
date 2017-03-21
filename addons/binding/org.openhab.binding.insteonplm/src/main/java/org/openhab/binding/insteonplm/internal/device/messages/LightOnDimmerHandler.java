package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonAddress;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags.MessageType;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class LightOnDimmerHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LightOnDimmerHandler.class);

    LightOnDimmerHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        if (!isMybutton(msg)) {
            return;
        }
        InsteonAddress a = handler.getAddress();
        if (msg.getFlags().getMessageType() == MessageType.AckOfDirect) {
            logger.error("{}: device {}: ignoring ack of direct.", nm(), a);
        } else {
            logger.info("{}: device {} was turned on. Sending poll request to get actual level", nm(), a);

            handler.updateFeatureState(f, PercentType.HUNDRED);
            // need to poll to find out what level the dimmer is at now.
            // it may not be at 100% because dimmers can be configured
            // to switch to e.g. 75% when turned on.
            handler.pollChannel(f, false);
        }
    }
}
