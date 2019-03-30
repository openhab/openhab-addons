/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import java.util.Optional;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;
import com.digitaldan.jomnilinkII.MessageTypes.statuses.Status;

/**
 *
 * @author Craig Hamilton
 *
 */
public class ButtonHandler extends AbstractOmnilinkStatusHandler {
    private Logger logger = LoggerFactory.getLogger(ButtonHandler.class);

    public ButtonHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            int buttonNumber = getThingNumber();
            logger.debug("Executing Button (macro) {}", buttonNumber);
            sendOmnilinkCommand(CommandMessage.CMD_BUTTON, 0, buttonNumber);
            updateState(OmnilinkBindingConstants.CHANNEL_BUTTON_PRESS, UnDefType.UNDEF);

        }
    }

    public void buttonActivated() {
        ChannelUID activateChannel = new ChannelUID(getThing().getUID(),
                OmnilinkBindingConstants.TRIGGER_CHANNEL_BUTTON_ACTIVATED_EVENT);
        triggerChannel(activateChannel);
    }

    @Override
    protected Optional retrieveStatus() {
        return Optional.empty();
    }

    @Override
    protected void updateChannels(Status t) {
        // No links for buttons
    }
}
