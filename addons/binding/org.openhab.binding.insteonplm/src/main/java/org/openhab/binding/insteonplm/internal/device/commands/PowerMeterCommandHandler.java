package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * Handler to set the power meter pieces,.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class PowerMeterCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(PowerMeterCommandHandler.class);

    PowerMeterCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        String cmdParam = conf.getThing().getProperties().get("cmd");
        if (cmdParam == null) {
            logger.error("{} ignoring cmd {} because no cmd= is configured!", nm(), cmd);
            return;
        }
        try {
            if (cmd == OnOffType.ON) {
                if (cmdParam.equals("reset")) {
                    Message m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, (byte) 0x80, (byte) 0x00,
                            conf.getAddress());
                    conf.enqueueMessage(m);
                    logger.info("{}: sent reset msg to power meter {}", nm(), conf.getAddress());
                    conf.handleUpdate(channelId, OnOffType.OFF);
                } else if (cmdParam.equals("update")) {
                    Message m = conf.getMessageFactory().makeStandardMessage((byte) 0x0f, (byte) 0x82, (byte) 0x00,
                            conf.getAddress());
                    conf.enqueueMessage(m);
                    logger.info("{}: sent update msg to power meter {}", nm(), conf.getAddress());
                    conf.handleUpdate(channelId, OnOffType.ON);
                } else {
                    logger.error("{}: ignoring unknown cmd {} for power meter {}", nm(), cmdParam, conf.getAddress());
                }
            } else if (cmd == OnOffType.OFF) {
                logger.info("{}: ignoring off request for power meter {}", nm(), conf.getAddress());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
