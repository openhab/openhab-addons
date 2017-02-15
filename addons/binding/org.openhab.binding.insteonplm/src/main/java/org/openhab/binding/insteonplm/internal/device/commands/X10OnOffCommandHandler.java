package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.X10;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to do the x10 version of on/off.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10OnOffCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(X10OnOffCommandHandler.class);

    X10OnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            byte houseCode = conf.getX10HouseCode();
            byte houseUnitCode = (byte) (houseCode << 4 | conf.getX10UnitCode());
            if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                byte houseCommandCode = (byte) (houseCode << 4
                        | (cmd == OnOffType.ON ? X10.Command.ON.code() : X10.Command.OFF.code()));
                Message munit = conf.getMessageFactory().makeX10Message(houseUnitCode, (byte) 0x00); // send unit code
                conf.enqueueMessage(munit, getFeature());
                Message mcmd = conf.getMessageFactory().makeX10Message(houseCommandCode, (byte) 0x80); // send command
                                                                                                       // code
                conf.enqueueMessage(mcmd, getFeature());
                String onOff = cmd == OnOffType.ON ? "ON" : "OFF";
                logger.info("{}: sent msg to switch {} {}", nm(), conf.getAddress(), onOff);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
