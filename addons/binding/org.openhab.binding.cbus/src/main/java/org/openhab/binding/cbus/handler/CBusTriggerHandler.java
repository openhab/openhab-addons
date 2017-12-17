/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.cbus.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
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
 * The {@link CBusTriggerHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
public class CBusTriggerHandler extends CBusGroupHandler {

    private Logger logger = LoggerFactory.getLogger(CBusTriggerHandler.class);

    public CBusTriggerHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_VALUE)) {
            logger.debug("Channel command {}: {}", channelUID.getAsString(), command.toString());
            try {
                if (command instanceof OnOffType) {
                    if (command.equals(OnOffType.ON)) {

                        group.ramp(255, 0);

                    } else if (command.equals(OnOffType.OFF)) {
                        group.ramp(0, 0);
                    }
                } else if (command instanceof DecimalType) {
                    group.ramp((int) Math.round(Double.parseDouble(command.toString())), 0);
                }
            } catch (CGateException e) {
                logger.error("Failed to send trigger command {} to {}", command.toString(), group.toString(), e);
            }
        }
    }

    @Override
    protected Group getGroup(int groupID) throws CGateException {
        Application application = cBusNetworkHandler.getNetwork()
                .getApplication(Integer.parseInt(CBusBindingConstants.CBUS_APPLICATION_TRIGGER));
        return application.getGroup(groupID);
    }
}
