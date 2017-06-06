package org.openhab.binding.insteonplm.internal.device.commands;

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
        if (cmd == OnOffType.ON) {
            if (cmdParam.equals("reset")) {
                SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                        StandardInsteonMessages.ResetPowerMeter, (byte) 0x00);
                conf.enqueueMessage(m);
                logger.info("{}: sent reset msg to power meter {}", nm(), conf.getAddress());
                conf.handleUpdate(channelId, OnOffType.OFF);
            } else if (cmdParam.equals("update")) {
                SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(),
                        StandardInsteonMessages.UpdatePowerMeter, (byte) 0x00);
                conf.enqueueMessage(m);
                logger.info("{}: sent update msg to power meter {}", nm(), conf.getAddress());
                conf.handleUpdate(channelId, OnOffType.ON);
            } else {
                logger.error("{}: ignoring unknown cmd {} for power meter {}", nm(), cmdParam, conf.getAddress());
            }
        } else if (cmd == OnOffType.OFF) {
            logger.info("{}: ignoring off request for power meter {}", nm(), conf.getAddress());
        }
    }
}
