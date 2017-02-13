package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.PercentType;
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
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            PercentType pc = (PercentType) cmd;
            logger.debug("changing level of {} to {}", dev.getAddress(), pc.intValue());
            int level = (int) Math.ceil((pc.intValue() * 255.0) / 100); // round up
            if (level > 0) { // make light on message with given level
                level = getMaxLightLevel(conf, level);
                Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) level);
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to set {} to {}", nm(), dev.getAddress(), level);
            } else { // switch off
                Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00);
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to set {} to zero by switching off", nm(), dev.getAddress());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
