package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler to change the io linc values.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class IOLincOnOffCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(IOLincOnOffCommandHandler.class);

    IOLincOnOffCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channel, Command cmd) {
        try {
            if (cmd == OnOffType.ON) {
                Message m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, (byte) 0x11, (byte) 0xff,
                        conf.getAddress());
                conf.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to switch {} on", nm(), conf.getAddress());
            } else if (cmd == OnOffType.OFF) {
                Message m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, (byte) 0x13, (byte) 0x00,
                        conf.getAddress());
                conf.enqueueMessage(m, getFeature());
                logger.info("{}: sent msg to switch {} off", nm(), conf.getAddress());
            }
            // This used to be configurable, but was made static to make
            // the architecture of the binding cleaner.
            int delay = 2000;
            delay = Math.max(1000, delay);
            delay = Math.min(10000, delay);
            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    Message m = getFeature().makePollMsg();
                    InsteonThing dev = getFeature().getDevice();
                    if (m != null) {
                        dev.enqueueMessage(m, getFeature());
                    }
                }
            }, delay);
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error: ", nm(), e);
        }
    }
}
