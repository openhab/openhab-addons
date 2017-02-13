package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.PercentType;
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
 * Handler to do the x10 version of percentage.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10PercentCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(X10PercentCommandHandler.class);

    X10PercentCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            //
            // I did not have hardware that would respond to the PRESET_DIM codes.
            // This code path needs testing.
            //
            byte houseCode = dev.getX10HouseCode();
            byte houseUnitCode = (byte) (houseCode << 4 | dev.getX10UnitCode());
            Msg munit = dev.makeX10Message(houseUnitCode, (byte) 0x00); // send unit code
            dev.enqueueMessage(munit, getFeature());
            PercentType pc = (PercentType) cmd;
            logger.debug("{}: changing level of {} to {}", nm(), dev.getAddress(), pc.intValue());
            int level = (pc.intValue() * 32) / 100;
            byte cmdCode = (level >= 16) ? X10.Command.PRESET_DIM_2.code() : X10.Command.PRESET_DIM_1.code();
            level = level % 16;
            if (level <= 0) {
                level = 0;
            }
            houseCode = (byte) s_X10CodeForLevel[level];
            cmdCode |= (houseCode << 4);
            Msg mcmd = dev.makeX10Message(cmdCode, (byte) 0x80); // send command code
            dev.enqueueMessage(mcmd, getFeature());
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }

    static private final int[] s_X10CodeForLevel = { 0, 8, 4, 12, 2, 10, 6, 14, 1, 9, 5, 13, 3, 11, 7, 15 };
}
