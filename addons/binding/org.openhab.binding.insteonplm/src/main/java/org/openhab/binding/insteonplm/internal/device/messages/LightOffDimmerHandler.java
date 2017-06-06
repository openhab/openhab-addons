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
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class LightOffDimmerHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(LightOffDimmerHandler.class);

    LightOffDimmerHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        if (isMybutton(msg)) {
            logger.info("{}: device {} was turned off {}.", nm(), handler.getAddress(), f.getLabel());
        }
        handler.updateFeatureState(f, PercentType.ZERO);
    }
}
