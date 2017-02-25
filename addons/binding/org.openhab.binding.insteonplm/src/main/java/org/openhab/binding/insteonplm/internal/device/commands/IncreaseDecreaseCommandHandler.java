package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
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
        try {
            if (cmd == IncreaseDecreaseType.INCREASE) {
                Message m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, (byte) 0x15, (byte) 0x00,
                        conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: sent msg to brighten {}", nm(), conf.getAddress());
            } else if (cmd == IncreaseDecreaseType.DECREASE) {
                Message m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, (byte) 0x16, (byte) 0x00,
                        conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: sent msg to dimm {}", nm(), conf.getAddress());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
