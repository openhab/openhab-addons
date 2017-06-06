package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
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
 * Handler to increase of decrease the value of the iterm.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class IncreaseDecreaseCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(IncreaseDecreaseCommandHandler.class);

    IncreaseDecreaseCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channel, Command cmd) {
        if (cmd == IncreaseDecreaseType.INCREASE) {
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.Bright, (byte) 0x00);
            conf.enqueueMessage(m);
            logger.info("{}: sent msg to brighten {}", nm(), conf.getAddress());
        } else if (cmd == IncreaseDecreaseType.DECREASE) {
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.Dim, (byte) 0x00);
            conf.enqueueMessage(m);
            logger.info("{}: sent msg to dimm {}", nm(), conf.getAddress());
        }
    }
}
