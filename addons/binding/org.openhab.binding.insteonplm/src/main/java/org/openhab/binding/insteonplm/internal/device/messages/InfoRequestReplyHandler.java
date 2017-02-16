package org.openhab.binding.insteonplm.internal.device.messages;

import org.eclipse.smarthome.core.thing.Channel;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.MessageHandler;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class InfoRequestReplyHandler extends MessageHandler {
    private static final Logger logger = LoggerFactory.getLogger(HiddenDoorSensorDataBatteryWatermarkHandler.class);

    InfoRequestReplyHandler(DeviceFeature p) {
        super(p);
    }

    @Override
    public void handleMessage(InsteonThingHandler handler, int group, byte cmd1, Message msg, Channel f) {
        if (!msg.isExtended()) {
            logger.warn("{} device {} expected extended msg as info reply, got {}", nm(), handler.getAddress(), msg);
            return;
        }
        try {
            int cmd2 = msg.getByte("command2") & 0xff;
            switch (cmd2) {
                case 0x00: // this is a product data response message
                    int prodKey = msg.getInt24("userData2", "userData3", "userData4");
                    int devCat = msg.getByte("userData5");
                    int subCat = msg.getByte("userData6");
                    logger.info("{} {} got product data: cat: {} subcat: {} key: {} ", nm(), handler.getAddress(),
                            devCat, subCat, Utils.getHexString(prodKey));
                    break;
                case 0x02: // this is a device text string response message
                    logger.info("{} {} got text str {} ", nm(), handler.getAddress(), msg);
                    break;
                default:
                    logger.warn("{} unknown cmd2 = {} in info reply message {}", nm(), cmd2, msg);
                    break;
            }
        } catch (FieldException e) {
            logger.error("error parsing {}: ", msg, e);
        }
    }
}
