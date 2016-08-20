/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.MilightThingState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MilightLedHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author David Graeff - Initial contribution
 */
public class MilightLedHandler extends BaseThingHandler {

    private Logger logger = LoggerFactory.getLogger(MilightLedHandler.class);
    private MilightThingState state;
    private int bulbid;

    public MilightLedHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (state == null) {
            logger.debug("Not initalized");
            return;
        }

        switch (channelUID.getId()) {
            case MilightBindingConstants.CHANNEL_COLOR: {
                if (command instanceof HSBType) {
                    HSBType hsb = (HSBType) command;
                    if (hsb.getSaturation().intValue() < 50) {
                        state.setWhiteMode();
                    } else {
                        state.setColor(hsb.getHue().intValue());
                    }
                    state.setBrightness(hsb.getBrightness().intValue());
                } else if (command instanceof OnOffType) {
                    OnOffType hsb = (OnOffType) command;
                    if (hsb == OnOffType.OFF) {
                        state.setOff();
                    } else {
                        state.setOn();
                    }
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_NIGHTMODE: {
                state.setNightMode();
                break;
            }
            case MilightBindingConstants.CHANNEL_BRIGHTNESS: {
                DecimalType d = (DecimalType) command;
                state.setBrightness(d.intValue());
                break;
            }
            case MilightBindingConstants.CHANNEL_TEMP: {
                DecimalType d = (DecimalType) command;
                state.setColorTemperature(d.intValue());
                break;
            }
            case MilightBindingConstants.CHANNEL_SPEED: {
                DecimalType d = (DecimalType) command;
                state.setDiscoSpeed(d.intValue());
                break;
            }
            case MilightBindingConstants.CHANNEL_MODE: {
                StringType d = (StringType) command;
                state.setDiscoMode(Integer.valueOf(d.toString()));
                break;
            }
            default:
                logger.error("Channel unknown " + channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        bulbid = Integer.valueOf(thing.getUID().getId());
        if (getBridge() != null) {
            bridgeHandlerInitialized(null, getBridge());
        }
    }

    @Override
    public void bridgeHandlerInitialized(ThingHandler thingHandler, Bridge bridge) {
        MilightBridgeHandler brHandler = (MilightBridgeHandler) bridge.getHandler();
        state = new MilightThingState(bulbid, brHandler.getCommunication());
        updateStatus(brHandler.getThing().getStatus());
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo bridgeStatusInfo) {
        updateStatus(bridgeStatusInfo.getStatus());
        state = new MilightThingState(bulbid, ((MilightBridgeHandler) getBridge().getHandler()).getCommunication());
    }
}
