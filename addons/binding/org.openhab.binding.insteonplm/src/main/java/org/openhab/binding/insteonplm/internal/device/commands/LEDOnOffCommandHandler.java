package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.ExtendedInsteonMessage;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.Message;
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
    private byte buttonNumber = 0;

    LEDOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    public void setButtonNumber(String button) {
        try {
            buttonNumber = Byte.valueOf(button);
        } catch (NumberFormatException e) {
            logger.error("Unable to parse {}", e, button);
        }
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            // Get from the channel properties, default 0 if no button exists.
            if (cmd == OnOffType.ON) {
                Message m = conf.getMessageFactory().makeExtendedMessage(new InsteonFlags(),
                        ExtendedInsteonMessage.ExtendedGetSet, new byte[] { buttonNumber, (byte) 0x09, (byte) 0x01 },
                        conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: sent msg to switch {} on", nm(), conf.getAddress());
            } else if (cmd == OnOffType.OFF) {
                Message m = conf.getMessageFactory().makeExtendedMessage(new InsteonFlags(),
                        ExtendedInsteonMessage.ExtendedGetSet, new byte[] { buttonNumber, (byte) 0x09, (byte) 0x00 },
                        conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: sent msg to switch {} off", nm(), conf.getAddress());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
