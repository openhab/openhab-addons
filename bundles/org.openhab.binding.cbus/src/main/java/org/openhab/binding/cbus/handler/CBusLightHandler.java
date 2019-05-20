/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.cbus.handler;

import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.cbus.CBusBindingConstants;

import com.daveoxley.cbus.Application;
import com.daveoxley.cbus.CGateException;
import com.daveoxley.cbus.Group;
import com.daveoxley.cbus.Network;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link CBusLightHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Scott Linton - Initial contribution
 */
@NonNullByDefault
public class CBusLightHandler extends CBusGroupHandler {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    public CBusLightHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
	Group group = this.group;
	if (group == null)
	    return;
        if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_STATE)) {
            logger.debug("Channel command {}: {}", channelUID.getAsString(), command.toString());
            if (command instanceof OnOffType) {
                try {
                    if (command.equals(OnOffType.ON)) {
                        group.on();
                    } else if (command.equals(OnOffType.OFF)) {
                        group.off();
                    }
                } catch (CGateException e) {
                    logger.error("Failed to send command {} to {}", command.toString(), group.toString(), e);
                }
            }
        } else if (channelUID.getId().equals(CBusBindingConstants.CHANNEL_LEVEL)) {
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
                logger.error("Failed to send command {} to {}", command.toString(), group.toString(), e);
            }
        }
    }

    @Override
    protected @Nullable Group getGroup(int groupID) throws CGateException {
	CBusNetworkHandler networkHandler = cBusNetworkHandler;
	if (networkHandler == null)
	    return null;
        Network network = networkHandler.getNetwork();
        if (network != null)
        {
            Application lighting = network
                .getApplication(Integer.parseInt(CBusBindingConstants.CBUS_APPLICATION_LIGHTING));
            return lighting.getGroup(groupID);
        }
        return null;
    }
}
