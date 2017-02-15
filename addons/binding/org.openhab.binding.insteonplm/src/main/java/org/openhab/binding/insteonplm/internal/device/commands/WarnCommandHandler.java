package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A command handler translates an openHAB command into a insteon message
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class WarnCommandHandler extends CommandHandler {
    private static final Logger logger = LoggerFactory.getLogger(WarnCommandHandler.class);

    WarnCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, ChannelUID channelId, Command cmd) {
        logger.warn("{}: command {} is not implemented yet!", nm(), cmd);
    }
}
