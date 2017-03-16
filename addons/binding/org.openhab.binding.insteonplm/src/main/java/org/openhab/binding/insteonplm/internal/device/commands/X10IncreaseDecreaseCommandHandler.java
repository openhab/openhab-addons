package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.device.X10Address;
import org.openhab.binding.insteonplm.internal.device.X10CommandHandler;
import org.openhab.binding.insteonplm.internal.device.X10DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
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
        try {
            byte houseCode = conf.getHouseCode();
            byte houseUnitCode = (byte) (houseCode << 4 | conf.getUnitCode());
            if (cmd == IncreaseDecreaseType.INCREASE || cmd == IncreaseDecreaseType.DECREASE) {
                byte houseCommandCode = (byte) (houseCode << 4 | (cmd == IncreaseDecreaseType.INCREASE
                        ? X10Address.Command.BRIGHT.code() : X10Address.Command.DIM.code()));
                SendX10Message munit = new SendX10Message(conf.getAddress());
                conf.enqueueMessage(munit);
                Message mcmd = conf.getMessageFactory().makeX10Message(houseCommandCode, (byte) 0x80); // send command
                                                                                                       // code
                conf.enqueueMessage(mcmd);
                String bd = cmd == IncreaseDecreaseType.INCREASE ? "BRIGHTEN" : "DIM";
                logger.info("{}: sent msg to switch {} {}", nm(), conf.getAddress(), bd);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
