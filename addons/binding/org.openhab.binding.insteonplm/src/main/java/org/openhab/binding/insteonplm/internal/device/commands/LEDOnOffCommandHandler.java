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
 * This Handler was supposed to set the LEDs of the 2487S, but it doesn't work.
 * The parameters were modeled after the 2486D, it may work for that one,
 * leaving it in for now.
 *
 * From the HouseLinc PLM traffic log, the following commands (in the D2 data field)
 * of the 2486D are supported:
 *
 * 0x02: LED follow mask may work or not
 * 0x03: LED OFF mask
 * 0x04: X10 addr setting
 * 0x05: ramp rate
 * 0x06: on Level for button
 * 0x07: global LED brightness (could not see any effect during testing)
 * 0x0B: set nontoggle on/off command
 *
 * crucially, the 0x09 command does not work (NACK from device)
 *
 * @author Bernd Pfrommer
 */
public class LEDOnOffCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(LEDOnOffCommandHandler.class);

    LEDOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            int button = this.getIntParameter("button", -1);
            if (cmd == OnOffType.ON) {
                Msg m = dev.makeExtendedMessage((byte) 0x1f, (byte) 0x2e, (byte) 0x00,
                        new byte[] { (byte) button, (byte) 0x09, (byte) 0x01 });
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to switch {} on", nm(), dev.getAddress());
            } else if (cmd == OnOffType.OFF) {
                Msg m = dev.makeExtendedMessage((byte) 0x1f, (byte) 0x2e, (byte) 0x00,
                        new byte[] { (byte) button, (byte) 0x09, (byte) 0x00 });
                dev.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to switch {} off", nm(), dev.getAddress());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
