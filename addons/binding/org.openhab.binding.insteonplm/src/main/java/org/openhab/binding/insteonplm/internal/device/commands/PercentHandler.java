package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to set the value as a percentage.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class PercentHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PercentHandler.class);

    PercentHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        PercentType pc = (PercentType) cmd;
        logger.debug("changing level of {} to {}", conf.getAddress(), pc.intValue());
        int level = (int) Math.ceil((pc.intValue() * 255.0) / 100); // round up
        if (level > 0) { // make light on message with given level
            level = getMaxLightLevel(conf, level);
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.LightOn, (byte) level);
            conf.enqueueMessage(m);
            logger.info("{}: sent msg to set {} to {}", nm(), conf.getAddress(), level);
        } else { // switch off
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.LightOff, (byte) 0x00);
            conf.enqueueMessage(m);
            logger.info("{}: sent msg to set {} to zero by switching off", nm(), conf.getAddress());
        }
    }
}
