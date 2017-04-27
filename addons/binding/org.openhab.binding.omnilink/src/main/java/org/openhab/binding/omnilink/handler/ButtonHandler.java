package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;

public class ButtonHandler extends AbstractOmnilinkHandler {
    private Logger logger = LoggerFactory.getLogger(ButtonHandler.class);

    public ButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("handleCommand, channel: {}, command: {}", channelUID, command);

    }

    @Override
    public void handleUpdate(ChannelUID channelUID, State newState) {
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        getOmnilinkBridgeHander().sendOmnilinkCommand(CommandMessage.CMD_BUTTON, 0, Integer.parseInt(channelParts[2]));
    }

}
