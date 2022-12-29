/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
public class TapoSmartBulb extends TapoDevice {
    private final Logger logger = LoggerFactory.getLogger(TapoSmartBulb.class);

    /**
     * Constructor
     * 
     * @param thing Thing object representing device
     */
    public TapoSmartBulb(Thing thing) {
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
        if (command instanceof RefreshType) {
            refreshInfo = true;
        } else {
            switch (channel) {
                case CHANNEL_OUTPUT:
                    connector.sendDeviceCommand(DEVICE_PROPERTY_ON, command == OnOffType.ON);
                    refreshInfo = true;
                    break;
                case CHANNEL_BRIGHTNESS:
                    if (command instanceof PercentType) {
                        Float percent = ((PercentType) command).floatValue();
                        setBrightness(percent.intValue()); // 0..100% = 0..100
                        refreshInfo = true;
                    } else if (command instanceof DecimalType) {
                        setBrightness(((DecimalType) command).intValue());
                        refreshInfo = true;
                    }
                    break;
                case CHANNEL_COLOR_TEMP:
                    if (command instanceof DecimalType) {
                        setColorTemp(((DecimalType) command).intValue());
                        refreshInfo = true;
                    }
                    break;
                case CHANNEL_COLOR:
                    if (command instanceof HSBType) {
                        setColor((HSBType) command);
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
            connector.sendDeviceCommand(DEVICE_PROPERTY_ON, false);
        } else {
            HashMap<String, Object> newState = new HashMap<>();
            newState.put(DEVICE_PROPERTY_ON, true);
            newState.put(DEVICE_PROPERTY_BRIGHTNES, newBrightness);
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
        newState.put(DEVICE_PROPERTY_ON, true);
        newState.put(DEVICE_PROPERTY_HUE, command.getHue().intValue());
        newState.put(DEVICE_PROPERTY_SATURATION, command.getSaturation().intValue());
        newState.put(DEVICE_PROPERTY_BRIGHTNES, command.getBrightness().intValue());
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
        newState.put(DEVICE_PROPERTY_ON, true);
        newState.put(DEVICE_PROPERTY_COLORTEMP, colorTemp);
        connector.sendDeviceCommands(newState);
    }

    /**
     * UPDATE PROPERTIES
     * 
     * @param TapoDeviceInfo
     */
    @Override
    protected void devicePropertiesChanged(TapoDeviceInfo deviceInfo) {
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
    }
}
