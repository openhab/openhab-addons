/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.haywardomnilogic.internal.handler;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Body of Water Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardBowHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardBowHandler.class);

    public HaywardBowHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.ONLINE);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (!(command instanceof RefreshType)) {
            String systemID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_SYSTEM_ID);
            String poolID = getThing().getProperties().get(HaywardBindingConstants.PROPERTY_BOWID);
            HaywardBridgeHandler haywardOmniLogixBridgeHandler = getHaywardOmniLogixBridgeHandler();
            if (haywardOmniLogixBridgeHandler != null) {
                haywardOmniLogixBridgeHandler.haywardCommand(channelUID, command, systemID, poolID);
            }
        }
    }

    public void updateBowData(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            String acceptedItemType = chan.getAcceptedItemType();
            if (acceptedItemType != null) {
                State state = toState(acceptedItemType, data);
                updateState(chan.getUID(), state);
                logger.trace("Updated Hayward BOW {} {} to: {}", systemID, channelID, data);
            }
        }
    }

    private State toState(String type, String value) throws NumberFormatException {
        if ("Number".equals(type)) {
            return new DecimalType(value);
        } else if ("Switch".equals(type)) {
            return Integer.parseInt(value) > 0 ? OnOffType.ON : OnOffType.OFF;
        } else {
            return StringType.valueOf(value);
        }
    }

    @SuppressWarnings("null")
    private @Nullable HaywardBridgeHandler getHaywardOmniLogixBridgeHandler() {
        return (HaywardBridgeHandler) getBridge().getHandler();
    }
}
