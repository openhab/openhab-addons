package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.ZoneStatus;

public class ZoneHandler extends AbstractOmnilinkHandler {
    private Logger logger = LoggerFactory.getLogger(ZoneHandler.class);

    public ZoneHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("must handle command");
    }

    public void handleZoneStatus(ZoneStatus status) {
        State newState = status.getStatus() == 1 ? OnOffType.ON : OnOffType.OFF;
        logger.debug("handle Zone Status Change to: " + newState);
        updateState(OmnilinkBindingConstants.CHANNEL_CONTACTSENSOR, newState);

    }
}
