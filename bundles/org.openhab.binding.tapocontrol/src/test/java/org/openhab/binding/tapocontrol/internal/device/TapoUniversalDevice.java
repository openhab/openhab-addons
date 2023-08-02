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
 * TAPO Universal-Device
 * universal device for testing pruposes
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoUniversalDevice extends TapoDevice {
    private final Logger logger = LoggerFactory.getLogger(TapoUniversalDevice.class);

    // CHANNEL LIST
    public static final String CHANNEL_GROUP_DEBUG = "debug";
    public static final String CHANNEL_RESPONSE = "deviceResponse";
    public static final String CHANNEL_COMMAND = "deviceCommand";

    /**
     * Constructor
     *
     * @param thing Thing object representing device
     */
    public TapoUniversalDevice(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("({}) handleCommand '{}' for channelUID {}", uid, command.toString(), channelUID.getId());
        Boolean refreshInfo = false;

        String channel = channelUID.getIdWithoutGroup();
        if (command instanceof RefreshType) {
            refreshInfo = true;
        } else {
            switch (channel) {
                case CHANNEL_OUTPUT:
                    connector.sendDeviceCommand(JSON_KEY_ON, command == OnOffType.ON);
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
                case CHANNEL_COMMAND:
                    String[] cmd = command.toString().split(":");
                    if (cmd.length == 1) {
                        connector.sendCustomQuery(cmd[0]);
                    } else if (cmd.length == 2) {
                        connector.sendDeviceCommand(cmd[0], cmd[1]);
                    } else {
                        logger.warn("({}) wrong command format '{}'", uid, command.toString());
                    }
                    break;
                default:
                    logger.warn("({}) command type '{}' not supported for channel '{}'", uid, command.toString(),
                            channelUID.getId());
            }
        }

        /* refreshInfo */
        if (refreshInfo) {
            queryDeviceInfo();
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
        newState.put(JSON_KEY_HUE, command.getHue());
        newState.put(JSON_KEY_SATURATION, command.getSaturation());
        newState.put(JSON_KEY_BRIGHTNESS, command.getBrightness());
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
     * SET DEVICE INFOs to device
     * 
     * @param deviceInfo
     */
    @Override
    public void setDeviceInfo(TapoDeviceInfo deviceInfo) {
        devicePropertiesChanged(deviceInfo);
        handleConnectionState();
    }

    /**
     * Handle full responsebody received from connector
     * 
     * @param responseBody
     */
    public void responsePasstrough(String responseBody) {
        logger.info("({}) received response {}", uid, responseBody);
        publishState(getChannelID(CHANNEL_GROUP_DEBUG, CHANNEL_RESPONSE), getStringType(responseBody));
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
        publishState(getChannelID(CHANNEL_GROUP_DEVICE, CHANNEL_OVERHEAT),
                getDecimalType(deviceInfo.isOverheated() ? 1 : 0));
    }

    /***********************************
     *
     * CHANNELS
     *
     ************************************/
    /**
     * Get ChannelID including group
     * 
     * @param group String channel-group
     * @param channel String channel-name
     * @return String channelID
     */
    @Override
    protected String getChannelID(String group, String channel) {
        return group + "#" + channel;
    }

    /**
     * Get Channel from ChannelID
     * 
     * @param channelID String channelID
     * @return String channel-name
     */
    protected String getChannelFromID(ChannelUID channelID) {
        String channel = channelID.getIdWithoutGroup();
        channel = channel.replace(CHANNEL_GROUP_ACTUATOR + "#", "");
        channel = channel.replace(CHANNEL_GROUP_DEVICE + "#", "");
        channel = channel.replace(CHANNEL_GROUP_DEBUG + "#", "");
        return channel;
    }
}
