package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

public class RoomHandler extends AbstractOmnilinkHandler implements UnitHandler {

    private Logger logger = LoggerFactory.getLogger(RoomHandler.class);

    public RoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel id: {}, command: {}", channelUID, command);
    }

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        logger.debug("need to handle status update{}", unitStatus);
        updateState(OmnilinkBindingConstants.CHANNEL_ROOM_STATUS,
                unitStatus.getStatus() == UNIT_ON ? OnOffType.ON : OnOffType.OFF);

    }

}
