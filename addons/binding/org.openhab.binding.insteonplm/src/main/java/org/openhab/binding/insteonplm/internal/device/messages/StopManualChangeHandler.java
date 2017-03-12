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
public class StopManualChangeHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(StopManualChangeHandler.class);

    StopManualChangeHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        logger.info("{}: dev {} manual state change: {}", nm(), handler.getAddress(), 0);
        handler.updateFeatureState(f, new DecimalType(1));

    }
}
