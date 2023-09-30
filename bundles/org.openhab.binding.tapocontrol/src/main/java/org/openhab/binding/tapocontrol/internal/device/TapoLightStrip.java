/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.device;

import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import java.util.HashMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.structures.TapoDeviceInfo;
import org.openhab.binding.tapocontrol.internal.structures.TapoLightEffect;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * TAPO Smart-Plug-Device.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightStrip extends TapoDevice {
    private final Logger logger = LoggerFactory.getLogger(TapoLightStrip.class);

    /**
     * Constructor
     * 
     * @param thing Thing object representing device
     */
    public TapoLightStrip(Thing thing) {
        super(thing);
    }

    /**
     * handle command sent to device
     * 
     * @param channelUID channelUID command is sent to
     * @param command command to be sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        Boolean refreshInfo = false;

        String channel = channelUID.getIdWithoutGroup();
        String group = channelUID.getGroupId();
        if (command instanceof RefreshType) {
            refreshInfo = true;
        } else if (group == CHANNEL_GROUP_EFFECTS) {
            setLightEffect(channel, command);
            refreshInfo = true;
        } else {
            switch (channel) {
                case CHANNEL_OUTPUT:
                    connector.sendDeviceCommand(JSON_KEY_ON, command == OnOffType.ON);
                    refreshInfo = true;
                    break;
                case CHANNEL_BRIGHTNESS:
                    if (command instanceof PercentType percentCommand) {
                        Float percent = percentCommand.floatValue();
                        setBrightness(percent.intValue()); // 0..100% = 0..100
                        refreshInfo = true;
                    } else if (command instanceof DecimalType decimalCommand) {
                        setBrightness(decimalCommand.intValue());
                        refreshInfo = true;
                    }
                    break;
                case CHANNEL_COLOR_TEMP:
                    if (command instanceof DecimalType decimalCommand) {
                        setColorTemp(decimalCommand.intValue());
                        refreshInfo = true;
                    }
                    break;
                case CHANNEL_COLOR:
                    if (command instanceof HSBType hsbCommand) {
                        setColor(hsbCommand);
                        refreshInfo = true;
                    }
                    break;
                default:
                    logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command.toString(),
                            channelUID.getId());
            }
        }

        /* refreshInfo */
        if (refreshInfo) {
            queryDeviceInfo(true);
        }
    }

    /**
     * SET BRIGHTNESS
     * 
     * @param newBrightness percentage 0-100 of new brightness
     */
    protected void setBrightness(Integer newBrightness) {
        /* switch off if 0 */
        if (newBrightness == 0) {
            connector.sendDeviceCommand(JSON_KEY_ON, false);
        } else {
            HashMap<String, Object> newState = new HashMap<>();
            newState.put(JSON_KEY_ON, true);
            newState.put(JSON_KEY_BRIGHTNESS, newBrightness);
            connector.sendDeviceCommands(newState);
        }
    }

    /**
     * SET COLOR
     * 
     * @param command
     */
    protected void setColor(HSBType command) {
        HashMap<String, Object> newState = new HashMap<>();
        newState.put(JSON_KEY_ON, true);
        newState.put(JSON_KEY_HUE, command.getHue().intValue());
        newState.put(JSON_KEY_SATURATION, command.getSaturation().intValue());
        newState.put(JSON_KEY_BRIGHTNESS, command.getBrightness().intValue());
        connector.sendDeviceCommands(newState);
    }

    /**
     * SET COLORTEMP
     * 
     * @param colorTemp (Integer) in Kelvin
     */
    protected void setColorTemp(Integer colorTemp) {
        HashMap<String, Object> newState = new HashMap<>();
        colorTemp = limitVal(colorTemp, BULB_MIN_COLORTEMP, BULB_MAX_COLORTEMP);
        newState.put(JSON_KEY_ON, true);
        newState.put(JSON_KEY_COLORTEMP, colorTemp);
        connector.sendDeviceCommands(newState);
    }

    /**
     * set Light Effect from channel/command
     * 
     * @param channel channel (effect) to set
     * @param command command (value) to set
     */
    protected void setLightEffect(String channel, Command command) {
        TapoLightEffect lightEffect = deviceInfo.getLightEffect();
        switch (channel) {
            case CHANNEL_FX_BRIGHTNESS:
                if (command instanceof PercentType percentCommand) {
                    Float percent = percentCommand.floatValue();
                    lightEffect.setBrightness(percent.intValue()); // 0..100% = 0..100
                } else if (command instanceof DecimalType decimalCommand) {
                    lightEffect.setBrightness(decimalCommand.intValue());
                }
                break;
            case CHANNEL_FX_COLORS:
                // comming soon
                break;
            case CHANNEL_FX_NAME:
                lightEffect.setName(command.toString());
                break;
        }
        setLightEffects(lightEffect);
    }

    /**
     * SET LIGHTNING EFFECTS
     * 
     * @param lightEffect new lightEffect
     */
    protected void setLightEffects(TapoLightEffect lightEffect) {
        JsonObject newEffect = new JsonObject();
        newEffect.addProperty(JSON_KEY_LIGHTNING_EFFECT_ENABLE, lightEffect.getEnable());
        newEffect.addProperty(JSON_KEY_LIGHTNING_EFFECT_NAME, lightEffect.getName());
        newEffect.addProperty(JSON_KEY_LIGHTNING_EFFECT_BRIGHNTESS, lightEffect.getBrightness());
        newEffect.addProperty(JSON_KEY_LIGHTNING_EFFECT_COLORTEMPRANGE, lightEffect.getColorTempRange().toString());
        newEffect.addProperty(JSON_KEY_LIGHTNING_EFFECT_DISPLAYCOLORS, lightEffect.getDisplayColors().toString());
        newEffect.addProperty(JSON_KEY_LIGHTNING_EFFECT_CUSTOM, lightEffect.getCustom());

        connector.sendDeviceCommand(JSON_KEY_LIGHTNING_EFFECT, newEffect.toString());
    }

    /**
     * UPDATE PROPERTIES
     * 
     * @param deviceInfo TapoDeviceInfo
     */
    @Override
    protected void devicePropertiesChanged(TapoDeviceInfo deviceInfo) {
        TapoLightEffect lightEffect = deviceInfo.getLightEffect();
        super.devicePropertiesChanged(deviceInfo);
        publishState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceInfo.isOn()));
        publishState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                getPercentType(deviceInfo.getBrightness()));
        publishState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR_TEMP),
                getDecimalType(deviceInfo.getColorTemp()));
        publishState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR), deviceInfo.getHSB());
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceInfo.getSignalLevel()));
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceInfo.getOnTime(), Units.SECOND));
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(deviceInfo.isOverheated()));
        // light effect
        publishState(getChannelID(CHANNEL_GROUP_EFFECTS, CHANNEL_FX_BRIGHTNESS),
                getPercentType(lightEffect.getBrightness()));
        publishState(getChannelID(CHANNEL_GROUP_EFFECTS, CHANNEL_FX_NAME), getStringType(lightEffect.getName()));
    }
}
