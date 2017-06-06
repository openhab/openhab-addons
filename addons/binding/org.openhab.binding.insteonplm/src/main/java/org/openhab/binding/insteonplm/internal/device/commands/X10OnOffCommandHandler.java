package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.device.X10CommandHandler;
import org.openhab.binding.insteonplm.internal.device.X10DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.X10Command;
import org.openhab.binding.insteonplm.internal.message.modem.SendX10Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to do the x10 version of on/off.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10OnOffCommandHandler extends X10CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(X10OnOffCommandHandler.class);

    X10OnOffCommandHandler(X10DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(X10ThingHandler conf, ChannelUID channelId, Command cmd) {
        if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
            X10Command houseCommandCode = (cmd == OnOffType.ON ? X10Command.On : X10Command.Off);
            SendX10Message munit = new SendX10Message(conf.getAddress()); // send unit code
            conf.enqueueMessage(munit);
            SendX10Message mcmd = new SendX10Message(houseCommandCode, conf.getAddress().getHouseCode());// send
                                                                                                         // command
            // code
            conf.enqueueMessage(mcmd);
            String onOff = cmd == OnOffType.ON ? "ON" : "OFF";
            logger.info("{}: sent msg to switch {} {}", nm(), conf.getAddress(), onOff);
        }
    }
}
