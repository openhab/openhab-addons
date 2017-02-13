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
 * Command handler that sends a command with a numerical value to a device.
 * The handler is very parameterizable so it can be reused for different devices.
 * First used for setting thermostat parameters.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */

public class NumberCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(NumberCommandHandler.class);

    NumberCommandHandler(DeviceFeature f) {
        super(f);
    }

    public int transform(int cmd) {
        return (cmd);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            int dc = transform(((DecimalType) cmd).intValue());
            int intFactor = getIntParameter("factor", 1);
            //
            // determine what level should be, and what field it should be in
            //
            int ilevel = dc * intFactor;
            byte level = (byte) (ilevel > 255 ? 0xFF : ((ilevel < 0) ? 0 : ilevel));
            String vfield = getStringParameter("value", "");
            if (vfield == "") {
                logger.error("{} has no value field specified", nm());
            }
            //
            // figure out what cmd1, cmd2, d1, d2, d3 are supposed to be
            // to form a proper message
            //
            int cmd1 = getIntParameter("cmd1", -1);
            if (cmd1 < 0) {
                logger.error("{} has no cmd1 specified!", nm());
                return;
            }
            int cmd2 = getIntParameter("cmd2", 0);
            int ext = getIntParameter("ext", 0);
            Msg m = null;
            if (ext == 1 || ext == 2) {
                byte[] data = new byte[] { (byte) getIntParameter("d1", 0), (byte) getIntParameter("d2", 0),
                        (byte) getIntParameter("d3", 0) };
                m = dev.makeExtendedMessage((byte) 0x0f, (byte) cmd1, (byte) cmd2, data);
                m.setByte(vfield, level);
                if (ext == 1) {
                    m.setCRC();
                } else if (ext == 2) {
                    m.setCRC2();
                }
            } else {
                m = dev.makeStandardMessage((byte) 0x0f, (byte) cmd1, (byte) cmd2);
                m.setByte(vfield, level);
            }
            dev.enqueueMessage(m, getFeature());
            logger.info("{}: sent msg to change level to {}", nm(), ((DecimalType) cmd).intValue());
            m = null;
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
