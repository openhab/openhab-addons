/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.omnilink.OmnilinkBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.CommandMessage;

/**
 *
 * @author Craig Hamilton
 *
 */
public class UpbUnitHandler extends DimmableUnitHandler {

    private final static Logger logger = LoggerFactory.getLogger(UpbUnitHandler.class);

    public UpbUnitHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(@NonNull ChannelUID channelUID, @NonNull Command command) {
        switch (channelUID.getId()) {
            case OmnilinkBindingConstants.CHANNEL_UPB_STATUS:
                handleUPBStatus(channelUID, command);
                break;
            default:
                super.handleCommand(channelUID, command);
        }
    }

    private void handleUPBStatus(@NonNull ChannelUID channelUID, @NonNull Command command) {
        int unitNumber = getThingNumber();
        logger.debug("Requesting Status for UPB Unit: {}", unitNumber);
        sendOmnilinkCommand(CommandMessage.CMD_UNIT_UPB_REQ_STATUS, 0, unitNumber);
        updateState(OmnilinkBindingConstants.CHANNEL_UPB_STATUS, UnDefType.UNDEF);

    }

}
