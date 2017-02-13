package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.DeviceFeatureListener.StateChangeType;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.Msg;
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
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        String cmdParam = conf.getThing().getProperties().get("cmd");
        if (cmdParam == null) {
            logger.error("{} ignoring cmd {} because no cmd= is configured!", nm(), cmd);
            return;
        }
        try {
            if (cmd == OnOffType.ON) {
                if (cmdParam.equals("reset")) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x80, (byte) 0x00);
                    dev.enqueueMessage(m, getFeature());
                    logger.info("{}: sent reset msg to power meter {}", nm(), dev.getAddress());
                    getFeature().publish(OnOffType.OFF, StateChangeType.ALWAYS, "cmd", "reset");
                } else if (cmdParam.equals("update")) {
                    Msg m = dev.makeStandardMessage((byte) 0x0f, (byte) 0x82, (byte) 0x00);
                    dev.enqueueMessage(m, getFeature());
                    logger.info("{}: sent update msg to power meter {}", nm(), dev.getAddress());
                    getFeature().publish(OnOffType.OFF, StateChangeType.ALWAYS, "cmd", "update");
                } else {
                    logger.error("{}: ignoring unknown cmd {} for power meter {}", nm(), cmdParam, dev.getAddress());
                }
            } else if (cmd == OnOffType.OFF) {
                logger.info("{}: ignoring off request for power meter {}", nm(), dev.getAddress());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
