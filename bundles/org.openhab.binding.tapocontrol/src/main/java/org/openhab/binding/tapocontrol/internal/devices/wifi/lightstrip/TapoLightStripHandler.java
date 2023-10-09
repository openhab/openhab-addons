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
package org.openhab.binding.tapocontrol.internal.devices.wifi.lightstrip;

import static org.openhab.binding.tapocontrol.internal.constants.TapoComConstants.*;
import static org.openhab.binding.tapocontrol.internal.constants.TapoThingConstants.*;
import static org.openhab.binding.tapocontrol.internal.helpers.TapoUtils.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tapocontrol.internal.devices.dto.TapoLightEffectData;
import org.openhab.binding.tapocontrol.internal.devices.wifi.TapoBaseDeviceHandler;
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

/**
 * TAPO Smart-Plug-Device.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoLightStripHandler extends TapoBaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoLightStripHandler.class);
    private TapoLightStripData lightStripData = new TapoLightStripData();

    /**
     * Constructor
     * 
     * @param thing Thing object representing device
     */
    public TapoLightStripHandler(Thing thing) {
        super(thing);
    }

    /**
     * Function called by {@link TapoDeviceConnector} if new data were received
     * 
     * @param queryCommand command where new data belong to
     */
    @Override
    public void newDataResult(String queryCommand) {
        super.newDataResult(queryCommand);
        if (DEVICE_CMD_GETINFO.equals(queryCommand)) {
            lightStripData = connector.getResponseData(TapoLightStripData.class);
            updateChannels(lightStripData);
        }
    }

    /**
     * handle command sent to device
     * 
     * @param channelUID channelUID command is sent to
     * @param command command to be sent
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        String channel = channelUID.getIdWithoutGroup();
        String group = channelUID.getGroupId();
        if (command instanceof RefreshType) {
            queryDeviceData();
        } else if (CHANNEL_GROUP_EFFECTS.equals(group)) {
            setLightEffect(channel, command);
        } else {
            switch (channel) {
                case CHANNEL_OUTPUT:
                    switchOnOff(command == OnOffType.ON ? Boolean.TRUE : Boolean.FALSE);
                    break;
                case CHANNEL_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        Float percent = ((PercentType) command).floatValue();
                        setBrightness(percent.intValue()); // 0..100% = 0..100
                    } else if (command instanceof DecimalType) {
                        setBrightness(((DecimalType) command).intValue());
                    }
                    break;
                case CHANNEL_COLOR_TEMP:
                    if (command instanceof DecimalType) {
                        setColorTemp(((DecimalType) command).intValue());
                    }
                    break;
                case CHANNEL_COLOR:
                    if (command instanceof HSBType) {
                        setColor((HSBType) command);
                    }
                    break;
                default:
                    logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command,
                            channelUID.getId());
            }
        }
    }

    /**
     * Switch device On or Off
     * 
     * @param on if true device will switch on. Otherwise switch off
     */
    protected void switchOnOff(boolean on) {
        lightStripData.switchOnOff(on);
        connector.sendDeviceCommand(lightStripData);
        queryDeviceData();
    }

    /**
     * SET BRIGHTNESS
     * 
     * @param newBrightness percentage 0-100 of new brightness
     */
    protected void setBrightness(Integer newBrightness) {
        /* switch off if 0 */
        if (newBrightness == 0) {
            lightStripData.switchOff();
        } else {
            lightStripData.switchOn();
            lightStripData.setBrightness(newBrightness);
        }
        connector.sendDeviceCommand(lightStripData);
        queryDeviceData();
    }

    /**
     * SET COLOR
     * 
     * @param command HSBType
     */
    protected void setColor(HSBType command) {
        lightStripData.switchOn();
        lightStripData.setHue(command.getHue().intValue());
        lightStripData.setSaturation(command.getSaturation().intValue());
        lightStripData.setBrightness(command.getBrightness().intValue());
        connector.sendDeviceCommand(lightStripData);
        queryDeviceData();
    }

    /**
     * SET COLORTEMP
     * 
     * @param colorTemp (Integer) in Kelvin
     */
    protected void setColorTemp(Integer colorTemp) {
        lightStripData.switchOn();
        lightStripData.setColorTemp(colorTemp);
        connector.sendDeviceCommand(lightStripData);
        queryDeviceData();
    }

    /**
     * set Light Effect from channel/command
     * 
     * @param channel channel (effect) to set
     * @param command command (value) to set
     */
    protected void setLightEffect(String channel, Command command) {
        TapoLightEffectData lightEffect = lightStripData.getLightEffect();
        switch (channel) {
            case CHANNEL_FX_BRIGHTNESS:
                if (command instanceof PercentType) {
                    Float percent = ((PercentType) command).floatValue();
                    lightEffect.setBrightness(percent.intValue()); // 0..100% = 0..100
                } else if (command instanceof DecimalType) {
                    lightEffect.setBrightness(((DecimalType) command).intValue());
                }
                break;
            case CHANNEL_FX_COLORS:
                // comming soon
                break;
            case CHANNEL_FX_NAME:
                lightEffect.setId(command.toString());
                break;
        }
        connector.sendDeviceCommand(DEVICE_CMD_SET_LIGHT_FX, lightEffect);
        queryDeviceData();
    }

    /**
     * UPDATE PROPERTIES
     * 
     * @param TapoDeviceInfo
     */
    protected void updateChannels(TapoLightStripData deviceInfo) {
        TapoLightEffectData lightEffect = deviceInfo.getLightEffect();
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceInfo.isOn()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                getPercentType(deviceInfo.getBrightness()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR_TEMP),
                getDecimalType(deviceInfo.getColorTemp()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR), deviceInfo.getHSB());
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceInfo.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceInfo.getOnTime(), Units.SECOND));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(deviceInfo.isOverheated()));
        // light effect
        updateState(getChannelID(CHANNEL_GROUP_EFFECTS, CHANNEL_FX_BRIGHTNESS),
                getPercentType(lightEffect.getBrightness()));
        updateState(getChannelID(CHANNEL_GROUP_EFFECTS, CHANNEL_FX_NAME), getStringType(lightEffect.getName()));
    }
}
