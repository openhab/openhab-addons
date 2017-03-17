package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
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
 * Handler to do the x10 version of increase/decrease.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10IncreaseDecreaseCommandHandler extends X10CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(X10IncreaseDecreaseCommandHandler.class);

    X10IncreaseDecreaseCommandHandler(X10DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(X10ThingHandler conf, ChannelUID id, Command cmd) {
        if (cmd == IncreaseDecreaseType.INCREASE || cmd == IncreaseDecreaseType.DECREASE) {
            X10Command houseCommandCode = (cmd == IncreaseDecreaseType.INCREASE ? X10Command.Bright : X10Command.Dim);
            SendX10Message munit = new SendX10Message(conf.getAddress());
            conf.enqueueMessage(munit);
            SendX10Message mcmd = new SendX10Message(houseCommandCode, conf.getAddress().getHouseCode());
            // code
            conf.enqueueMessage(mcmd);
            String bd = cmd == IncreaseDecreaseType.INCREASE ? "BRIGHTEN" : "DIM";
            logger.info("{}: sent msg to switch {} {}", nm(), conf.getAddress(), bd);
        }

    }
}
