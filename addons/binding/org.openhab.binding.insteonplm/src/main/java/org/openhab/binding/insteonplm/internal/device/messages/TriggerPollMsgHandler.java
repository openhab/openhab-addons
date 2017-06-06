package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.modem.StandardMessageReceived;

/**
 * Triggers a poll when a message comes in. Use this handler to react
 * to messages that notify of a status update, but don't carry the information
 * that you are interested in. Example: you send a command to change a setting,
 * get a DIRECT ack back, but the ack does not have the value of the updated setting.
 * Then connect this handler to the ACK, such that the device will be polled, and
 * the settings updated.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class TriggerPollMsgHandler extends MessageHandler {
    TriggerPollMsgHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, StandardMessageReceived msg, Channel f) {
        handler.pollChannel(f, false);
    }
}
