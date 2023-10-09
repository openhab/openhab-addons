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
package org.openhab.binding.tapocontrol.internal.devices.wifi.bulb;

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
public class TapoBulbHandler extends TapoBaseDeviceHandler {
    private final Logger logger = LoggerFactory.getLogger(TapoBulbHandler.class);
    private TapoBulbData bulbData = new TapoBulbData();

    /**
     * Constructor
     * 
     * @param thing Thing object representing device
     */
    public TapoBulbHandler(Thing thing) {
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
            bulbData = connector.getResponseData(TapoBulbData.class);
            updateChannels(bulbData);
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
        if (command instanceof RefreshType) {
            queryDeviceData();
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
                case CHANNEL_FX_NAME:
                    setLightEffect(command.toString());
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
        bulbData.switchOnOff(on);
        connector.sendDeviceCommand(bulbData);
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
            bulbData.switchOff();
        } else {
            bulbData.switchOn();
            bulbData.setBrightness(newBrightness);
        }
        connector.sendDeviceCommand(bulbData);
        queryDeviceData();
    }

    /**
     * SET COLOR
     * 
     * @param command HSBType
     */
    protected void setColor(HSBType command) {
        bulbData.switchOn();
        bulbData.setHue(command.getHue().intValue());
        bulbData.setSaturation(command.getSaturation().intValue());
        bulbData.setBrightness(command.getBrightness().intValue());
        connector.sendDeviceCommand(bulbData);
        queryDeviceData();
    }

    /**
     * SET COLORTEMP
     * 
     * @param colorTemp (Integer) in Kelvin
     */
    protected void setColorTemp(Integer colorTemp) {
        bulbData.switchOn();
        bulbData.setColorTemp(colorTemp);
        connector.sendDeviceCommand(bulbData);
        queryDeviceData();
    }

    /**
     * Set light effect
     * 
     * @param fxName (String) id of LightEffect
     */
    protected void setLightEffect(String fxName) {
        connector.sendDeviceCommand(DEVICE_CMD_SET_LIGHT_FX, new TapoLightEffectData(fxName));
        queryDeviceData();
    }

    /**
     * Update Channels
     */
    protected void updateChannels(TapoBulbData deviceData) {
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_OUTPUT), getOnOffType(deviceData.isOn()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_BRIGHTNESS),
                getPercentType(deviceData.getBrightness()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR_TEMP),
                getDecimalType(deviceData.getColorTemp()));
        updateState(getChannelID(CHANNEL_GROUP_ACTUATOR, CHANNEL_COLOR), deviceData.getHSB());
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_WIFI_STRENGTH),
                getDecimalType(deviceData.getSignalLevel()));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_ONTIME),
                getTimeType(deviceData.getOnTime(), Units.SECOND));
        updateState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT), getOnOffType(deviceData.isOverheated()));

        updateLightEffectChannels(deviceData);
    }

    /**
     * Set light effect channels
     * 
     * @param lightEffect
     */
    protected void updateLightEffectChannels(TapoBulbData deviceData) {
        String fxId = "";
        if (deviceData.dynamicLightEffectEnabled()) {
            fxId = deviceData.dynamicLightEffectId();
        }
        updateState(getChannelID(CHANNEL_GROUP_EFFECTS, CHANNEL_FX_NAME), getStringType(fxId));
    }
}
