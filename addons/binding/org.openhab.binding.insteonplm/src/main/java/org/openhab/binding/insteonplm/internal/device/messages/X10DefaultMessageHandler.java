package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.device.X10DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.X10MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.X10MessageReceived;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10DefaultMessageHandler extends X10MessageHandler {

    public X10DefaultMessageHandler(X10DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(X10ThingHandler handler, X10MessageReceived message, Channel channel) {
        // Do Nothing.
    }

}
