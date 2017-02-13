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
 * Sends ALLLink broadcast commands to group
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class GroupBroadcastCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(GroupBroadcastCommandHandler.class);

    GroupBroadcastCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        try {
            if (cmd == OnOffType.ON || cmd == OnOffType.OFF) {
                byte cmd1 = (byte) ((cmd == OnOffType.ON) ? 0x11 : 0x13);
                byte value = (byte) ((cmd == OnOffType.ON) ? 0xFF : 0x00);
                int group = getGroup(conf);
                if (group == -1) {
                    logger.error("no group=xx specified in item {}", conf.getThing().getLabel());
                    return;
                }
                logger.info("{}: sending {} broadcast to group {}", nm(), (cmd1 == 0x11) ? "ON" : "OFF",
                        getGroup(conf));
                Msg m = dev.makeStandardMessage((byte) 0x0f, cmd1, value, group);
                dev.enqueueMessage(m, getFeature());
            }
        } catch (IOException e) {
            logger.error("{}: command send i/o error: ", nm(), e);
        } catch (FieldException e) {
            logger.error("{}: command send message creation error ", nm(), e);
        }
    }
}
