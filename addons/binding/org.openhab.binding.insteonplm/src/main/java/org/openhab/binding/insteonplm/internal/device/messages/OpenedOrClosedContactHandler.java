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
public class OpenedOrClosedContactHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(OpenedOrClosedContactHandler.class);

    OpenedOrClosedContactHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        try {
            byte cmd2 = msg.getByte("command2");
            switch (cmd1) {
                case 0x11:
                    switch (cmd2) {
                        case 0x02:
                            handler.updateFeatureState(f, OpenClosedType.CLOSED);
                            break;
                        case 0x01:
                        case 0x04:
                            handler.updateFeatureState(f, OpenClosedType.OPEN);
                            break;
                        default: // do nothing
                            break;
                    }
                    break;
                case 0x13:
                    switch (cmd2) {
                        case 0x04:
                            handler.updateFeatureState(f, OpenClosedType.CLOSED);
                            break;
                        default: // do nothing
                            break;
                    }
                    break;
            }
        } catch (FieldException e) {
            logger.debug("{} no cmd2 found, dropping msg {}", nm(), msg);
            return;
        }
    }
}
