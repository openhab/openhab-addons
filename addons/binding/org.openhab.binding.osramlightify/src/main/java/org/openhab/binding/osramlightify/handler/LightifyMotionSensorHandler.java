/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.osramlightify.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_BATTERY;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_ENABLED;
import static org.openhab.binding.osramlightify.LightifyBindingConstants.CHANNEL_TRIGGERED;

import org.openhab.binding.osramlightify.handler.LightifyBridgeHandler;


/**
 * The {@link org.eclipse.smarthome.core.thing.binding.ThingHandler} implementation
 * for motion sensors paired with an OSRAM/Sylvania Lightify gateway.
 *
 * @author Mike Jagdis - Initial contribution
 */
public final class LightifyMotionSensorHandler extends LightifyDeviceHandler {

    private final Logger logger = LoggerFactory.getLogger(LightifyMotionSensorHandler.class);

    public LightifyMotionSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void thingUpdated(Thing thing) {
    }

    @Override
    public boolean setOnline(LightifyBridgeHandler bridgeHandler) {
        updateStatus(ThingStatus.ONLINE);
        return true;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("{}, Command: {} {}", channelUID, command.getClass().getSimpleName(), command);

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_BATTERY:
                    updateState(CHANNEL_BATTERY, lightifyDeviceState.getBattery());
                    break;

                case CHANNEL_ENABLED:
                    updateState(CHANNEL_ENABLED, lightifyDeviceState.getEnabled());
                    break;

                case CHANNEL_TRIGGERED:
                    updateState(CHANNEL_TRIGGERED, lightifyDeviceState.getTriggered());
                    break;
            }
        }
    }

    public void changedBattery(DecimalType battery) {
        logger.debug("{}: update: battery {}", getThing().getUID(), battery);

        updateState(CHANNEL_BATTERY, battery);
    }

    public void changedEnabled(OnOffType enabled) {
        logger.debug("{}: update: enabled {}", getThing().getUID(), enabled);

        updateState(CHANNEL_ENABLED, enabled);
    }

    public void changedTriggered(OnOffType triggered) {
        logger.debug("{}: update: triggered {}", getThing().getUID(), triggered);

        updateState(CHANNEL_TRIGGERED, triggered);
    }
}
