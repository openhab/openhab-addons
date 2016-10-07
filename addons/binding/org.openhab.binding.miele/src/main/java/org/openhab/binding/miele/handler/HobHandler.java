/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miele.handler;

import static org.openhab.binding.miele.MieleBindingConstants.APPLIANCE_ID;

import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;

/**
 * The {@link HobHandler} is responsible for handling commands,
 * which are sent to one of the channels
 *
 * @author Karel Goderis - Initial contribution
 */
public class HobHandler extends MieleApplianceHandler<HobChannelSelector> {

    protected Logger logger = LoggerFactory.getLogger(HobHandler.class);

    public HobHandler(Thing thing) {
        super(thing, HobChannelSelector.class, "Hob");
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

        super.handleCommand(channelUID, command);

        String channelID = channelUID.getId();
        String uid = (String) getThing().getConfiguration().getProperties().get(APPLIANCE_ID);

        OvenChannelSelector selector = (OvenChannelSelector) getValueSelectorFromChannelID(channelID);
        JsonElement result = null;

        try {
            if (selector != null) {
                switch (selector) {
                    default: {
                        logger.debug("{} is a read-only channel that does not accept commands",
                                selector.getChannelID());
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            logger.warn(
                    "An error occurred while trying to set the read-only variable associated with channel '{}' to '{}'",
                    channelID, command.toString());
        }
    }

}
