package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * Handler to do the fast on/off call.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class FastOnOffCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(FastOnOffCommandHandler.class);

    FastOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channel, Command cmd) {
        if (cmd == OnOffType.ON) {
            int level = getMaxLightLevel(conf, 0xff);
            // In this case the address on the thing is the group address.
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.LightOnFast, (byte) level);
            conf.enqueueMessage(m);
            logger.info("{}: sent fast on to switch {} level {}", nm(), conf.getAddress(),
                    level == 0xff ? "on" : level);
        } else if (cmd == OnOffType.OFF) {
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.LightOffFast, (byte) 0x00);
            conf.enqueueMessage(m);
            logger.info("{}: sent fast off to switch {}", nm(), conf.getAddress());
        }
        // expect to get a direct ack after this!
    }
}
