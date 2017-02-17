package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.Message;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class DimmerStopManualChangeHandler extends MessageHandler {
    DimmerStopManualChangeHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        handler.pollChannel(f, true);
    }
}
