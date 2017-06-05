/**
 * Copyright (c) 2014 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.knx.handler.physical;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.link.ItemChannelLinkRegistry;
import org.eclipse.smarthome.core.types.Type;

import tuwien.auto.calimero.GroupAddress;

/**
 * The {@link RollerShutterSwitchThingHandler} is responsible for handling commands, which are
 * sent to one of the channels. It implements a KNX roller/shutter/blind actor that
 * is also capable of handling ON and OFF commands, that get translated in a 100% and
 * 0% position
 *
 * @author Karel Goderis - Initial contribution
 */
public class RollerShutterSwitchThingHandler extends RollerShutterThingHandler {

    // List of all Channel ids
    public final static String CHANNEL_SWITCH = "switch";

    public RollerShutterSwitchThingHandler(Thing thing, ItemChannelLinkRegistry itemChannelLinkRegistry) {
        super(thing, itemChannelLinkRegistry);
    }

    @Override
    public void processDataReceived(GroupAddress destination, Type state) {

        if (state instanceof PercentType) {
            lastPosition = (PercentType) state;

            try {
                GroupAddress address = new GroupAddress((String) getConfig().get(POSITION_STATUS_GA));
                if (address.equals(destination)) {
                    if (((PercentType) state).intValue() == 100) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SWITCH), OnOffType.ON);
                    }
                    if (((PercentType) state).intValue() == 0) {
                        updateState(new ChannelUID(getThing().getUID(), CHANNEL_SWITCH), OnOffType.OFF);
                    }
                }
            } catch (Exception e) {
                // do nothing, we move on (either config parameter null, or wrong address format)
            }

        }

        super.processDataReceived(destination, state);

    }

    @Override
    public String getDPT(ChannelUID channelUID, Type command) {

        if (command instanceof OnOffType) {

            PercentType newCommand = null;

            if (command == OnOffType.ON) {
                newCommand = new PercentType(100);
            }

            if (command == OnOffType.OFF) {
                newCommand = new PercentType(0);
            }

            return super.getDPT(channelUID, newCommand);
        }

        return super.getDPT(channelUID, command);
    }

    @Override
    public String getAddress(ChannelUID channelUID, Type command) {

        if (command instanceof OnOffType) {
            return (String) getConfig().get(RollerShutterThingHandler.POSITION_GA);
        }

        return super.getAddress(channelUID, command);
    }

    @Override
    public Type getType(ChannelUID channelUID, Type command) {

        if (command instanceof OnOffType) {
            PercentType newCommand = null;
            if (command == OnOffType.ON) {
                newCommand = new PercentType(100);
            }

            if (command == OnOffType.OFF) {
                newCommand = new PercentType(0);
            }

            return newCommand;
        }

        return super.getType(channelUID, command);
    }
}
