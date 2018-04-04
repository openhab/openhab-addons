/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.milight.MilightBindingConstants;
import org.openhab.binding.milight.internal.MilightThingState;
import org.openhab.binding.milight.internal.protocol.AbstractBulbInterface;
import org.openhab.binding.milight.internal.protocol.MilightV2RGB;
import org.openhab.binding.milight.internal.protocol.MilightV3RGBW;
import org.openhab.binding.milight.internal.protocol.MilightV3White;
import org.openhab.binding.milight.internal.protocol.MilightV6;
import org.openhab.binding.milight.internal.protocol.MilightV6RGB_CW_WW;
import org.openhab.binding.milight.internal.protocol.MilightV6RGB_IBOX;
import org.openhab.binding.milight.internal.protocol.MilightV6RGB_W;
import org.openhab.binding.milight.internal.protocol.QueuedSend;
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
    private AbstractBulbInterface bulbCom;
    private int zone;

    public MilightLedHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (state == null) {
            logger.debug("Not initalized");
            return;
        }

        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case MilightBindingConstants.CHANNEL_COLOR:
                    updateState(channelUID, new HSBType(new DecimalType(state.hue360),
                            new PercentType(state.saturation), new PercentType(state.brightness)));
                    break;
                case MilightBindingConstants.CHANNEL_BRIGHTNESS:
                    updateState(channelUID, new PercentType(state.brightness));
                    break;
                case MilightBindingConstants.CHANNEL_SATURATION:
                    updateState(channelUID, new PercentType(state.saturation));
                    break;
                case MilightBindingConstants.CHANNEL_TEMP:
                    updateState(channelUID, new PercentType(state.colorTemperature));
                    break;
                case MilightBindingConstants.CHANNEL_ANIMATION_MODE:
                    updateState(channelUID, new DecimalType(state.animationMode));
                    break;
            }
            return;
        }

        switch (channelUID.getId()) {
            case MilightBindingConstants.CHANNEL_COLOR: {
                if (command instanceof HSBType) {
                    HSBType hsb = (HSBType) command;
                    bulbCom.setHSB(hsb.getHue().intValue(), hsb.getSaturation().intValue(),
                            hsb.getBrightness().intValue(), state);
                    updateState(MilightBindingConstants.CHANNEL_SATURATION, new PercentType(state.saturation));
                    updateState(MilightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(state.brightness));
                } else if (command instanceof OnOffType) {
                    OnOffType hsb = (OnOffType) command;
                    bulbCom.setPower(hsb == OnOffType.ON, state);
                } else if (command instanceof PercentType) {
                    PercentType p = (PercentType) command;
                    bulbCom.setBrightness(p.intValue(), state);
                    updateState(MilightBindingConstants.CHANNEL_BRIGHTNESS, new PercentType(state.brightness));
                } else {
                    logger.error("CHANNEL_COLOR channel only supports OnOffType/HSBType/PercentType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_NIGHTMODE: {
                bulbCom.nightMode(state);
                break;
            }
            case MilightBindingConstants.CHANNEL_WHITEMODE: {
                bulbCom.whiteMode(state);
                break;
            }
            case MilightBindingConstants.CHANNEL_LINKLED: {
                if (bulbCom instanceof MilightV6) {
                    ((MilightV6) bulbCom).link(zone);
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_UNLINKLED: {
                if (bulbCom instanceof MilightV6) {
                    ((MilightV6) bulbCom).unlink(zone);
                }
                break;
            }

            case MilightBindingConstants.CHANNEL_BRIGHTNESS: {
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    bulbCom.setBrightness((s == OnOffType.ON) ? 100 : 0, state);
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    bulbCom.setBrightness(d.intValue(), state);
                } else if (command instanceof IncreaseDecreaseType) {
                    bulbCom.changeBrightness((IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? 1 : -1,
                            state);
                } else {
                    logger.error("CHANNEL_BRIGHTNESS channel only supports OnOffType/IncreaseDecreaseType/DecimalType");
                }
                updateState(MilightBindingConstants.CHANNEL_COLOR, new HSBType(new DecimalType(state.hue360),
                        new PercentType(state.saturation), new PercentType(state.brightness)));

                break;
            }
            case MilightBindingConstants.CHANNEL_SATURATION: {
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    bulbCom.setSaturation((s == OnOffType.ON) ? 100 : 0, state);
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    bulbCom.setSaturation(d.intValue(), state);
                } else {
                    logger.error("CHANNEL_SATURATION channel only supports OnOffType/IncreaseDecreaseType/DecimalType");
                }
                updateState(MilightBindingConstants.CHANNEL_COLOR, new HSBType(new DecimalType(state.hue360),
                        new PercentType(state.saturation), new PercentType(state.brightness)));

                break;
            }
            case MilightBindingConstants.CHANNEL_TEMP: {
                if (command instanceof OnOffType) {
                    OnOffType s = (OnOffType) command;
                    bulbCom.setColorTemperature((s == OnOffType.ON) ? 100 : 0, state);
                } else if (command instanceof IncreaseDecreaseType) {
                    bulbCom.changeColorTemperature(
                            (IncreaseDecreaseType) command == IncreaseDecreaseType.INCREASE ? 1 : -1, state);
                } else if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    bulbCom.setColorTemperature(d.intValue(), state);
                } else {
                    logger.error("CHANNEL_TEMP channel only supports OnOffType/IncreaseDecreaseType/DecimalType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_SPEED_REL: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        bulbCom.changeSpeed(1, state);
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        bulbCom.changeSpeed(-1, state);
                    }
                } else {
                    logger.error("CHANNEL_SPEED channel only supports IncreaseDecreaseType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_ANIMATION_MODE: {
                if (command instanceof DecimalType) {
                    DecimalType d = (DecimalType) command;
                    bulbCom.setLedMode(d.intValue(), state);
                } else {
                    logger.error("Animation mode channel only supports DecimalType");
                }
                break;
            }
            case MilightBindingConstants.CHANNEL_ANIMATION_MODE_REL: {
                if (command instanceof IncreaseDecreaseType) {
                    IncreaseDecreaseType id = (IncreaseDecreaseType) command;
                    if (id == IncreaseDecreaseType.INCREASE) {
                        bulbCom.nextAnimationMode(state);
                    } else if (id == IncreaseDecreaseType.DECREASE) {
                        bulbCom.previousAnimationMode(state);
                    }
                } else {
                    logger.error("Relative animation mode channel only supports IncreaseDecreaseType");
                }
                break;
            }
            default:
                logger.error("Channel unknown {}", channelUID.getId());
        }
    }

    @Override
    public void initialize() {
        zone = Integer.valueOf(thing.getUID().getId());
        if (getBridge() != null) {
            AbstractMilightBridgeHandler brHandler = (AbstractMilightBridgeHandler) getBridge().getHandler();
            QueuedSend com = brHandler.getCommunication();
            state = new MilightThingState();

            if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_V2_THING_TYPE)) {
                bulbCom = new MilightV2RGB(com, zone);
            } else if (thing.getThingTypeUID().equals(MilightBindingConstants.WHITE_THING_TYPE)) {
                bulbCom = new MilightV3White(com, zone);
            } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_THING_TYPE)) {
                bulbCom = new MilightV3RGBW(com, zone);
            } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_IBOX_THING_TYPE)) {
                bulbCom = new MilightV6RGB_IBOX(com, ((MilightBridgeV6Handler) brHandler).getSessionManager());
            } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_CW_WW_THING_TYPE)) {
                bulbCom = new MilightV6RGB_CW_WW(com, ((MilightBridgeV6Handler) brHandler).getSessionManager(), zone);
            } else if (thing.getThingTypeUID().equals(MilightBindingConstants.RGB_W_THING_TYPE)) {
                bulbCom = new MilightV6RGB_W(com, ((MilightBridgeV6Handler) brHandler).getSessionManager(), zone);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Bulb type not supported!");
            }

            if (brHandler.getThing().getStatus() == ThingStatus.ONLINE) {
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
            }
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE);
        }
    }
}
