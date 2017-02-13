package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.InsteonThingHandler;
import org.openhab.binding.insteonplm.internal.device.CommandHandler;
import org.openhab.binding.insteonplm.internal.device.DeviceFeature;
import org.openhab.binding.insteonplm.internal.device.InsteonThing;

/**
 * Do nothing.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class NoOpCommandHandler extends CommandHandler {
    public NoOpCommandHandler(DeviceFeature f) {
        super(f);
    }

    @Override
    public void handleCommand(InsteonThingHandler conf, Command cmd, InsteonThing dev) {
        // do nothing, not even log
    }
}
