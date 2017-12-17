/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.handler;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;
import org.openhab.binding.cbus.internal.cgate.Application;
import org.openhab.binding.cbus.internal.cgate.CGateException;
import org.openhab.binding.cbus.internal.cgate.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link CBusDaliHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusDaliHandler extends CBusGroupHandler {

    private Logger logger = LoggerFactory.getLogger(CBusDaliHandler.class);

    public CBusDaliHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_LEVEL)) {
            logger.debug("Channel command {}: {}", channelUID.getAsString(), command.toString());
            try {
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {
                        group.on();
                    } else if (command.equals(OnOffType.OFF)) {
                        group.off();
                    }
                } else if (command instanceof PercentType) {
                    PercentType value = (PercentType) command;
                    group.ramp((int) Math.round(value.doubleValue() / 100 * 255), 0);
                } else if (command instanceof IncreaseDecreaseType) {
                    logger.warn("Increase/Decrease not implemented for {}", channelUID.getAsString());
                }
            } catch (CGateException e) {
                logger.error("Cannot send command {} to {}", command.toString(), group.toString(), e);
            }
        }
    }

    @Override
    protected Group getGroup(int groupID) throws CGateException {
        Application lighting = cBusNetworkHandler.getNetwork()
                .getApplication(Integer.parseInt(CBusBindingConstants.CBUS_APPLICATION_DALI));
        return lighting.getGroup(groupID);
    }

}
