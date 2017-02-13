package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;
import org.openhab.binding.insteonplm.internal.device.X10;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Msg;
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
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            byte houseCode = dev.getX10HouseCode();
            byte houseUnitCode = (byte) (houseCode << 4 | dev.getX10UnitCode());
            if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                byte houseCommandCode = (byte) (houseCode << 4
                        | (cmd == OnOffType.ON ? X10.Command.ON.code() : X10.Command.OFF.code()));
                Msg munit = dev.makeX10Message(houseUnitCode, (byte) 0x00); // send unit code
                dev.enqueueMessage(munit, getFeature());
                Msg mcmd = dev.makeX10Message(houseCommandCode, (byte) 0x80); // send command code
                dev.enqueueMessage(mcmd, getFeature());
                String onOff = cmd == OnOffType.ON ? "ON" : "OFF";
                logger.info("{}: sent msg to switch {} {}", nm(), dev.getAddress(), onOff);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
