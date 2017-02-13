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
 * Handler to turn the light on or off.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class LightOnOffCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(LightOnOffCommandHandler.class);

    LightOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            int ext = getIntParameter("ext", 0);
            int direc = 0x00;
            int level = 0x00;
            Msg m = null;
            if (cmd == OnOffType.ON) {
                level = getMaxLightLevel(conf, 0xff);
                direc = 0x11;
                logger.info("{}: sent msg to switch {} to {}", nm(), dev.getAddress(), level == 0xff ? "on" : level);
            } else if (cmd == OnOffType.OFF) {
                direc = 0x13;
                logger.info("{}: sent msg to switch {} off", nm(), dev.getAddress());
            }
            if (ext == 1 || ext == 2) {
                byte[] data = new byte[] { (byte) getIntParameter("d1", 0), (byte) getIntParameter("d2", 0),
                        (byte) getIntParameter("d3", 0) };
                m = dev.makeExtendedMessage((byte) 0x0f, (byte) direc, (byte) level, data);
                logger.info("{}: was an extended message for device {}", nm(), dev.getAddress());
                if (ext == 1) {
                    m.setCRC();
                } else if (ext == 2) {
                    m.setCRC2();
                }
            } else {
                m = dev.makeStandardMessage((byte) 0x0f, (byte) direc, (byte) level, getGroup(conf));
            }
            logger.info("Sending message to {}", dev.getAddress());
            dev.enqueueMessage(m, getFeature());
            // expect to get a direct ack after this!
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
