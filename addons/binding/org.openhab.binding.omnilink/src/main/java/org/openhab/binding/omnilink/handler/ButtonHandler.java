package org.openhab.binding.omnilink.handler;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.UID;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
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
        String[] channelParts = channelUID.getAsString().split(UID.SEPARATOR);
        logger.debug("must handle command for button.  channel: {}, command: {}", channelUID, command);
        if (!(command instanceof RefreshType)) {
            getOmnilinkBridgeHander().sendOmnilinkCommand(CommandMessage.CMD_BUTTON, 0,
                    Integer.parseInt(channelParts[2]));
        }
    }

    public void buttonPressed() {
        logger.debug("buttonPressed");
        updateState(OmnilinkBindingConstants.CHANNEL_BUTTON_PRESS, OnOffType.ON);
    }
}
