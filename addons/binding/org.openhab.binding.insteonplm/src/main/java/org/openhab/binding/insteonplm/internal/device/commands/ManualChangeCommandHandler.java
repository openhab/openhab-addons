package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.library.types.DecimalType;
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
        if (cmd instanceof DecimalType) {
            int v = ((DecimalType) cmd).intValue();
            StandardInsteonMessages cmd1 = (v != 1) ? StandardInsteonMessages.StartManualChange
                    : StandardInsteonMessages.StopManualChange; // start or stop
            int cmd2 = (v == 2) ? 0x01 : 0; // up or down
            SendInsteonMessage m = new SendInsteonMessage(conf.getAddress(), conf.getDefaultFlags(), cmd1, (byte) cmd2);
            conf.enqueueMessage(m);
            logger.info("{}: cmd {} sent manual change {} {} to {}", nm(), v, cmd1.toString(),
                    (cmd2 == 0x01) ? "UP" : "DOWN", conf.getAddress());
        } else {
            logger.error("{}: invalid command type: {}", nm(), cmd);
        }
    }
}
