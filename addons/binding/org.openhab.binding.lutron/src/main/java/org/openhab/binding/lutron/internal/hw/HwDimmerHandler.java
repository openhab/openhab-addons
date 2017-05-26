package org.openhab.binding.lutron.internal.hw;

import static org.openhab.binding.lutron.LutronBindingConstants.CHANNEL_LIGHTLEVEL;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;

public class HwDimmerHandler extends BaseThingHandler {
    public HwDimmerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CHANNEL_LIGHTLEVEL)) {
            if (command instanceof Number) {
                int level = ((Number) command).intValue();

            } else if (command.equals(OnOffType.ON)) {

            } else if (command.equals(OnOffType.OFF)) {

            }
        }
    }
}
