package org.openhab.binding.insteonplm.internal.device.commands;

import java.io.IOException;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.message.FieldException;
import org.openhab.binding.insteonplm.internal.message.InsteonFlags;
import org.openhab.binding.insteonplm.internal.message.Message;
import org.openhab.binding.insteonplm.internal.message.StandardInsteonMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manual change from the system.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class ManualChangeCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(ManualChangeCommandHandler.class);

    ManualChangeCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        try {
            if (cmd instanceof DecimalType) {
                int v = ((DecimalType) cmd).intValue();
                StandardInsteonMessages cmd1 = (v != 1) ? StandardInsteonMessages.StartManualChange
                        : StandardInsteonMessages.StopManualChange; // start or stop
                int cmd2 = (v == 2) ? 0x01 : 0; // up or down
                Message m = conf.getMessageFactory().makeStandardMessage(new InsteonFlags(), cmd1, (byte) cmd2,
                        conf.getInsteonGroup(), conf.getAddress());
                conf.enqueueMessage(m);
                logger.info("{}: cmd {} sent manual change {} {} to {}", nm(), v, cmd1.toString(),
                        (cmd2 == 0x01) ? "UP" : "DOWN", conf.getAddress());
            } else {
                logger.error("{}: invalid command type: {}", nm(), cmd);
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
