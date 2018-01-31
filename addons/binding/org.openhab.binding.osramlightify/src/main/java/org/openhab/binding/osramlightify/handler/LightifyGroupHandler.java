/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.handler;

import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.core.types.State;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_COLOR;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_DIMMER;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_SWITCH;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;

/**
 * The {@link org.eclipse.smarthome.core.thing.binding.ThingHandler} implementation
 * for groups defined on an OSRAM/Sylvania Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyGroupHandler extends LightifyDeviceHandler {

    private HSBType lastHSB = new HSBType();

    public LightifyGroupHandler(Thing thing) {
        super(thing);
    }

    @Override
    public boolean setOnline(LightifyBridgeHandler bridgeHandler) {
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // We have no state to send you.
        if (command instanceof RefreshType) {
            return;
        }

        super.handleCommand(channelUID, command);

        String channelID = channelUID.getId();

        // A group has more channels than is strictly necessary because creating a group
        // of, say, power switches and then having to link switch Items to the color channel
        // makes no sense to users. But we do need to keep the channels in sync because they
        // reflect the same command path to devices so, for instance, an "ON" is _just_ an
        // "ON" and turns on both lights and power switch members regardless of which channel
        // it was sent to.

        if (!channelID.equals(CHANNEL_SWITCH) && !channelID.equals(CHANNEL_DIMMER) && !channelID.equals(CHANNEL_COLOR)) {
            return;
        }

        Command percent;
        Command onoff;

        if (command instanceof HSBType) {
            lastHSB = (HSBType) command;
            percent = lastHSB.getBrightness();
            onoff = (((PercentType) percent).intValue() != 0 ? OnOffType.ON : OnOffType.OFF);
        } else if (command instanceof PercentType) {
            lastHSB = new HSBType(lastHSB.getHue(), lastHSB.getSaturation(), (PercentType) command);
            percent = command;
            onoff = (((PercentType) percent).intValue() != 0 ? OnOffType.ON : OnOffType.OFF);
        } else {
            // Must be OnOff...
            lastHSB = new HSBType(lastHSB.getHue(), lastHSB.getSaturation(), new PercentType(100));
            percent = command;
            onoff = command;
        }

        if (!channelID.equals(CHANNEL_SWITCH)) {
            postCommand(CHANNEL_SWITCH, onoff);
        }
        if (!channelID.equals(CHANNEL_DIMMER)) {
            postCommand(CHANNEL_DIMMER, percent);
        }
        if (!channelID.equals(CHANNEL_COLOR)) {
            postCommand(CHANNEL_COLOR, percent);
        }

        updateState(CHANNEL_SWITCH, (State) onoff);
        updateState(CHANNEL_DIMMER, (State) percent);
        updateState(CHANNEL_COLOR, (State) lastHSB);
    }
}
