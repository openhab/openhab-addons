package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Msg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manual change from the system.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ManualChangeCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ManualChangeCommandHandler.class);

    ManualChangeCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            if (cmd instanceof DecimalType) {
                int v = ((DecimalType) cmd).intValue();
                int cmd1 = (v != 1) ? 0x17 : 0x18; // start or stop
                int cmd2 = (v == 2) ? 0x01 : 0; // up or down
                Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) cmd1, (byte) cmd2, getGroup(conf));
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: cmd {} sent manual change {} {} to {}", nm(), v, (cmd1 == 0x17) ? "START" : "STOP",
                        (cmd2 == 0x01) ? "UP" : "DOWN", dev.getAddress());
            } else {
                logger.error("{}: invalid command type: {}", nm(), cmd);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
