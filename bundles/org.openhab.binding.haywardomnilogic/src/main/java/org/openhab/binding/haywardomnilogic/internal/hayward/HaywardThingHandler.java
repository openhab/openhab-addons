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

package org.openhab.binding.haywardomnilogic.internal.hayward;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;
import org.openhab.binding.haywardomnilogic.internal.HaywardBindingConstants;
import org.openhab.binding.haywardomnilogic.internal.handler.HaywardBridgeHandler;

/**
 * The {@link HaywarThingHandler} is a subclass of the BaseThingHandler and a Super
 * Class to each Hayward Thing Handler
 *
 * @author Matt Myers - Initial contribution
 */
public class HaywardThingHandler extends BaseThingHandler {
    protected Thing thing;

    public HaywardThingHandler(Thing thing) {
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

    private HaywardBridgeHandler getHaywardOmniLogixBridgeHandler() {
        Bridge bridge = getBridge();
        if (bridge == null || bridge.getHandler() == null) {
            return null;
        } else {
            return (HaywardBridgeHandler) bridge.getHandler();
        }
    }

    public State toState(String type, String value) throws NumberFormatException {
        if ("Number".equals(type)) {
            return new DecimalType(value);
        } else if ("Switch".equals(type)) {
            return Integer.parseInt(value) > 0 ? OnOffType.ON : OnOffType.OFF;
        } else if ("Number:Dimensionless".equals(type)) {
            return new DecimalType(value);
        } else {
            return StringType.valueOf(value);
        }
    }

    public void updateData(String systemID, String channelID, String data) {
        Channel chan = getThing().getChannel(channelID);
        if (chan != null) {
            String acceptedItemType = chan.getAcceptedItemType();
            if (acceptedItemType != null) {
                State state = toState(acceptedItemType, data);
                updateState(chan.getUID(), state);
            }
        }
    }
}
