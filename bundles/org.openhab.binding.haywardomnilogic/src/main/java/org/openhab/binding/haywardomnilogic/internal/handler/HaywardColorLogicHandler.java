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
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The ColorLogic Handler
 *
 * @author Matt Myers - Initial Contribution
 */
@NonNullByDefault
public class HaywardColorLogicHandler extends BaseThingHandler {
    private final Logger logger = LoggerFactory.getLogger(HaywardColorLogicHandler.class);

    public HaywardColorLogicHandler(Thing thing) {
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

    public void updateColorLogicData(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            updateState(chan.getUID(), new DecimalType(data));
            logger.trace("Updated Hayward ColorLogic {} {} to: {}", systemID, channelID, data);
        }
    }

    @SuppressWarnings("null")
    private @Nullable HaywardBridgeHandler getHaywardOmniLogixBridgeHandler() {
        return (HaywardBridgeHandler) getBridge().getHandler();
    }
}
