/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.milight.handler;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.HSBType;
import org.eclipse.smarthome.core.library.types.IncreaseDecreaseType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.PercentType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
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
                } else if (command instanceof PercentType) {
                    PercentType p = (PercentType) command;
                    state.setBrightness(p.intValue());
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_NIGHTMODE: {
                state.setNightMode();
                break;
            }
            case MilightBindingConstants.CHANNEL_BRIGHTNESS: {
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    if (s == OnOffType.OFF) {
                        state.setOff();
                    } else {
                        state.setOn();
                    }
                } else if (command instanceof PercentType) {
                    PercentType p = (PercentType) command;
                    state.setBrightness(p.intValue());
                }

                break;
            }
            case MilightBindingConstants.CHANNEL_TEMP: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        state.warmer();
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        state.cooler();
                    }
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    state.setColorTemperature(d.intValue());
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_SPEED: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        state.increaseSpeed();
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        state.decreaseSpeed();
                    }
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    state.setDiscoSpeed(d.intValue());
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_MODE: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        state.nextDiscoMode();
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        state.previousDiscoMode();
                    }
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    state.setDiscoMode(d.intValue());
                } else if (command instanceof StringType) {
                    StringType d = (StringType) command;
                    state.setDiscoMode(Integer.valueOf(d.toString()));
                }
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
            MilightBridgeHandler brHandler = (MilightBridgeHandler) getBridge().getHandler();
            state = new MilightThingState(bulbid, brHandler.getCommunication());
            updateStatus(brHandler.getThing().getStatus());
        }
    }
}
