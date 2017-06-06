package org.openhab.binding.insteonplm.internal.device.commands;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.openhab.binding.insteonplm.internal.message.modem.SendInsteonMessage;
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
    public void handleCommand(final InsteonThingHandler conf, final ChannelUID channel, Command cmd) {
        if (cmd == OnOffType.ON) {
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.LightOn, (byte) 0xff);
            conf.enqueueMessage(m);
            logger.info("{}: sent msg to switch {} on", nm(), conf.getAddress());
        } else if (cmd == OnOffType.OFF) {
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                    StandardInsteonMessages.LightOff, (byte) 0x00);
            conf.enqueueMessage(m);
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
                conf.pollChannel(conf.getThing().getChannel(channel.getId()), true);
            }
        }, delay);
    }
}
