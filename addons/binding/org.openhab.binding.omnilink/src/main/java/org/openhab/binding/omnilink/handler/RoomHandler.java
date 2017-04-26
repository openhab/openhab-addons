package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

public class RoomHandler extends AbstractOmnilinkHandler implements UnitHandler {

    public RoomHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void handleUnitStatus(UnitStatus unitStatus) {
        // TODO Auto-generated method stub

    }

}
