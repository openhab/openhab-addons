package org.openhab.binding.insteonplm.internal.device.commands;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.insteonplm.handler.X10ThingHandler;
import org.openhab.binding.insteonplm.internal.device.X10CommandHandler;
import org.openhab.binding.insteonplm.internal.device.X10DeviceFeature;

/**
 * Do nothing.
 *
 * @author Daniel Pfrommer
 * @author Bernd Pfrommer
 */
public class X10NoOpCommandHandler extends X10CommandHandler {

    public X10NoOpCommandHandler(X10DeviceFeature feature) {
        super(feature);
    }

    @Override
    public void handleCommand(X10ThingHandler handler, ChannelUID channel, Command cmd) {
        // No op. Nothing to see here.
    }

}
