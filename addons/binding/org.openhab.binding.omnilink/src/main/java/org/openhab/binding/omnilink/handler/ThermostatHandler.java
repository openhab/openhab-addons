package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThermostatHandler extends AbstractOmnilinkHandler {

    private Logger logger = LoggerFactory.getLogger(ThermostatHandler.class);

    public ThermostatHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // TODO Auto-generated method stub

    }

}
