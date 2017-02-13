package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
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
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            if (cmd == OnOffType.ON) {
                int level = getMaxLightLevel(conf, 0xff);
                Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x12, (byte) level, getGroup(conf));
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent fast on to switch {} level {}", nm(), dev.getAddress(),
                        level == 0xff ? "on" : level);
            } else if (cmd == OnOffType.OFF) {
                Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x14, (byte) 0x00, getGroup(conf));
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent fast off to switch {}", nm(), dev.getAddress());
            }
            // expect to get a direct ack after this!
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
