package org.openhab.binding.insteonplm.internal.device.messages;

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
public class ContactRequestReplyHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(ContactRequestReplyHandler.class);

    ContactRequestReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        byte cmd = 0x00;
        byte cmd2 = 0x00;
        try {
            cmd = msg.getByte("Cmd");
            cmd2 = msg.getByte("command2");
        } catch (FieldException e) {
            logger.debug("{} no cmd found, dropping msg {}", nm(), msg);
            return;
        }
        if (msg.isAckOfDirect() && (f.getQueryStatus() == DeviceFeature.QueryStatus.QUERY_PENDING) && cmd == 0x50) {
            OpenClosedType oc = (cmd2 == 0) ? OpenClosedType.OPEN : OpenClosedType.CLOSED;
            logger.info("{}: set contact {} to: {}", nm(), handler.getAddress(), oc);
            handler.updateFeatureState(f, oc);
        }
    }
}
