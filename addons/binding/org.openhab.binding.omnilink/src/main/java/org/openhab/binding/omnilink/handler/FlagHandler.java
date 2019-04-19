/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.omnilink.handler;

import static org.openhab.binding.omnilink.OmnilinkBindingConstants.*;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.digitaldan.jomnilinkII.MessageTypes.statuses.UnitStatus;

/**
 *
 * @author Craig Hamilton
 *
 */
public class FlagHandler extends UnitHandler {

    public FlagHandler(Thing thing) {
        super(thing);
    }

    private Logger logger = LoggerFactory.getLogger(FlagHandler.class);

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        switch (channelUID.getId()) {
            case CHANNEL_FLAG_VALUE:
                handleFlagValue(channelUID.getId(), (DecimalType) command);
                break;
            case CHANNEL_FLAG_SWITCH:
                handleFlagSwitch(channelUID.getId(), (OnOffType) command);
                break;
            default:
                super.handleCommand(channelUID, command);
        }
    }

    private void handleFlagSwitch(@NonNull String id, OnOffType command) {
        int flagID = getThingNumber();
        super.handleOnOff(command, flagID);
    }

    private void handleFlagValue(@NonNull String id, DecimalType command) {
        logger.debug("updating omnilink flag change: open command: {}", command);
        sendOmnilinkCommand(OmniLinkCmd.CMD_UNIT_SET_COUNTER.getNumber(), command.intValue(), getThingNumber());

    }

    @Override
    public void updateChannels(UnitStatus unitStatus) {
        logger.debug("need to handle status update{}", unitStatus);
        updateState(CHANNEL_FLAG_VALUE, DecimalType.valueOf(Integer.toString(unitStatus.getStatus())));
        updateState(CHANNEL_FLAG_SWITCH, unitStatus.getStatus() == 0 ? OnOffType.OFF : OnOffType.ON);

    }

}
