/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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

package org.openhab.binding.freeathomesystem.internal.handler;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.freeathomesystem.internal.FreeAtHomeSystemBindingConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link FreeAtHomeBridgeHandler} is responsible for determining the device type
 * based on the received json string
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDeviceDescription {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceDescription.class);

    public static final int FID_UNKNOWN = 0xFFFFAAFF; // Control element

    // free@home constants
    public static final int FID_SWITCH_SENSOR = 0x0000; // Control element
    public static final int FID_DIMMING_SENSOR = 0x0001; // Dimming sensor
    public static final int FID_BLIND_SENSOR = 0x0003; // Blind sensor
    public static final int FID_STAIRCASE_LIGHT_SENSOR = 0x0004; // Stairwell light sensor
    public static final int FID_FORCE_ON_OFF_SENSOR = 0x0005;// Force On/Off sensor
    public static final int FID_SCENE_SENSOR = 0x0006; // Scene sensor
    public static final int FID_SWITCH_ACTUATOR = 0x0007;// Switch actuator
    public static final int FID_SHUTTER_ACTUATOR = 0x0009; // Blind actuator
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITH_FAN = 0x000A;// Room temperature controller
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_SLAVE = 0x000B; // Room temperature controller extension
    public static final int FID_WIND_ALARM_SENSOR = 0x000C; // Wind Alarm
    public static final int FID_FROST_ALARM_SENSOR = 0x000D; // Frost Alarm
    public static final int FID_RAIN_ALARM_SENSOR = 0x000E; // Rain Alarm
    public static final int FID_WINDOW_DOOR_SENSOR = 0x000F; // Window sensor
    public static final int FID_MOVEMENT_DETECTOR = 0x0011; // Movement Detector
    public static final int FID_DIMMING_ACTUATOR = 0x0012; // Dim actuator
    public static final int FID_RADIATOR_ACTUATOR = 0x0014; // Radiator
    public static final int FID_UNDERFLOOR_HEATING = 0x0015; // Underfloor heating
    public static final int FID_FAN_COIL = 0x0016; // Fan Coil
    public static final int FID_TWO_LEVEL_CONTROLLER = 0x0017; // Two-level controller
    public static final int FID_DES_DOOR_OPENER_ACTUATOR = 0x001A; // Door opener
    public static final int FID_PROXY = 0x001B;// Proxy
    public static final int FID_DES_LEVEL_CALL_ACTUATOR = 0x001D;// Door Entry System Call Level Actuator
    public static final int FID_DES_LEVEL_CALL_SENSOR = 0x001E;// Door Entry System Call Level Sensor
    public static final int FID_DES_DOOR_RINGING_SENSOR = 0x001F;// Door call
    public static final int FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR = 0x0020;// Automatic door opener
    public static final int FID_DES_LIGHT_SWITCH_ACTUATOR = 0x0021;// Corridor light
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN = 0x0023;// Room temperature
    public static final int FID_COOLING_ACTUATOR = 0x0024;// Cooling mode
    public static final int FID_HEATING_ACTUATOR = 0x0027;// Heating mode
    public static final int FID_FORCE_UP_DOWN_SENSOR = 0x0028;// Force-position blind
    public static final int FID_HEATING_COOLING_ACTUATOR = 0x0029;// Auto. heating/cooling mode
    public static final int FID_HEATING_COOLING_SENSOR = 0x002A;// Switchover heating/cooling
    public static final int FID_DES_DEVICE_SETTINGS = 0x002B;// Device settings
    public static final int FID_RGB_W_ACTUATOR = 0x002E;// Dim actuator
    public static final int FID_RGB_ACTUATOR = 0x002F;// Dim actuator
    public static final int FID_PANEL_SWITCH_SENSOR = 0x0030;// Control element
    public static final int FID_PANEL_DIMMING_SENSOR = 0x0031;// Dimming sensor
    public static final int FID_PANEL_BLIND_SENSOR = 0x0033;// Blind sensor
    public static final int FID_PANEL_STAIRCASE_LIGHT_SENSOR = 0x0034;// Stairwell light sensor
    public static final int FID_PANEL_FORCE_ON_OFF_SENSOR = 0x0035;// Force On/Off sensor
    public static final int FID_PANEL_FORCE_UP_DOWN_SENSOR = 0x0036;// Force-position blind
    public static final int FID_PANEL_SCENE_SENSOR = 0x0037;// Scene sensor
    public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE = 0x0038;// Room temperature controller
    public static final int FID_PANEL_FAN_COIL_SENSOR = 0x0039;// Fan coil sensor
    public static final int FID_PANEL_RGB_CT_SENSOR = 0x003A;// RGB + warm white/cold white sensor
    public static final int FID_PANEL_RGB_SENSOR = 0x003B;// RGB sensor
    public static final int FID_PANEL_CT_SENSOR = 0x003C;// Warm white/cold white sensor
    public static final int FID_ADDITIONAL_HEATING_ACTUATOR = 0x003D;// Add. stagepublic static final String For
    public static final int FID_RADIATOR_ACTUATOR_MASTER = 0x003E;// Radiator thermostate
    public static final int FID_RADIATOR_ACTUATOR_SLAVE = 0x003F;// Room temperature controller extension unit
    public static final int FID_BRIGHTNESS_SENSOR = 0x0041;// Brightness sensor
    public static final int FID_RAIN_SENSOR = 0x0042;// Rain sensor
    public static final int FID_TEMPERATURE_SENSOR = 0x0043;// Temperature sensor
    public static final int FID_WIND_SENSOR = 0x0044;// Wind sensor
    public static final int FID_TRIGGER = 0x0045;// Trigger
    public static final int FID_WINDOW_DOOR_ACTUATOR = 0x004B;// Window/Door
    public static final int FID_INVERTER_INFO = 0x004E;// ABC
    public static final int FID_CENTRAL_HEATING_ACTUATOR = 0x0056;// Central heating actuator
    public static final int FID_CENTRAL_COOLING_ACTUATOR = 0x0057;// Central cooling actuator
    public static final int FID_MEDIA_PLAYER = 0x005A;// Media Player
    public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE = 0x005B;// Panel Room
    public static final int FID_PANEL_MUTE_ACTUATOR = 0x005D;// Panel Room
    public static final int FID_PANEL_MEDIA_PLAYER_SENSOR = 0x0060;// Media Player Sensor
    public static final int FID_BLIND_ACTUATOR = 0x0061;// Roller blind actuator
    public static final int FID_ATTIC_WINDOW_ACTUATOR = 0x0062;// Attic window actuator
    public static final int FID_AWNING_ACTUATOR = 0x0063;// Awning actuator
    public static final int FID_WINDOW_DOOR_POSITION_SENSOR = 0x0064; // WindowDoor Position Sensor
    public static final int FID_SCENE_TRIGGER = 0x4800; // Scene trigger
    public static final int FID_RULE_SWITCH = 0x4A00; // Scene trigger

    // channel type strings
    public static final String CHANNEL_TYPE_UNKNOWN = "Unknown Device";
    public static final String CHANNEL_TYPE_ACTUATOR = "Actuator";
    public static final String CHANNEL_TYPE_THERMOSTAT = "Thermostat";
    public static final String CHANNEL_TYPE_SCENE = "Scene";
    public static final String CHANNEL_TYPE_RULE = "Rule";
    public static final String CHANNEL_TYPE_WINDOWSENSOR = "Window Sensor";
    public static final String CHANNEL_TYPE_WINDOWPOSSENSOR = "Window Position Sensor";
    public static final String CHANNEL_TYPE_TRIGGER = "Trigger";
    public static final String CHANNEL_TYPE_DOOROPENERACTUATOR = "Door Opener Actuator";
    public static final String CHANNEL_TYPE_RINGSENSOR = "Ring Sensor";
    public static final String CHANNEL_TYPE_CORRIDORLIGHTSWITCH = "Corridor Light Switch";
    public static final String CHANNEL_TYPE_MUTEACTUATOR = "Mute Actor";
    public static final String CHANNEL_TYPE_DIMMINGACTUATOR = "Dimming Actuator";
    public static final String CHANNEL_TYPE_SHUTTERACTUATOR = "Shutter Actuator";

    // interface strings
    public static final String DEVICE_INTERFACE_UNKNOWN_TYPE = "unknown";
    public static final String DEVICE_INTERFACE_WIRELESS_TYPE = "wireless";
    public static final String DEVICE_INTERFACE_VIRTUAL_TYPE = "virtual";
    public static final String DEVICE_INTERFACE_WIRED_TYPE = "wired";
    public static final String DEVICE_INTERFACE_HUE_TYPE = "hue";

    public String deviceLabel = "";
    // public String deviceTypeString = "";
    public String deviceId = "";
    public String interfaceType = "";
    public boolean validDevice = false;

    public List<FreeAtHomeDeviceChannel> listOfThings = new ArrayList<>();

    public FreeAtHomeDeviceDescription() {
        validDevice = false;
    }

    public FreeAtHomeDeviceDescription(JsonObject jsonObject, String id) {

        // set the device ID
        deviceId = id;

        // set the device invalid at first
        validDevice = false;

        boolean sceneIsDetected = id.toLowerCase().startsWith("ffff48");
        boolean ruleIsDetected = id.toLowerCase().startsWith("ffff4a");

        JsonObject jsonObjectOfId = jsonObject.getAsJsonObject(id);

        if (null == jsonObjectOfId) {
            return;
        }

        JsonElement jsonObjectOfInterface = jsonObjectOfId.get("interface");

        if (null != jsonObjectOfInterface) {
            String interfaceString = jsonObjectOfInterface.getAsString();

            if (interfaceString.toLowerCase().startsWith("vdev:")) {
                interfaceType = DEVICE_INTERFACE_VIRTUAL_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("hue")) {
                interfaceType = DEVICE_INTERFACE_HUE_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("rf")) {
                interfaceType = DEVICE_INTERFACE_WIRELESS_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("tp")) {
                interfaceType = DEVICE_INTERFACE_WIRED_TYPE;
            } else {
                interfaceType = DEVICE_INTERFACE_UNKNOWN_TYPE;
            }
        } else {
            interfaceType = DEVICE_INTERFACE_UNKNOWN_TYPE;
        }

        JsonElement jsonObjectOfDeviceLabel = jsonObjectOfId.get("displayName");

        if (null == jsonObjectOfDeviceLabel) {
            this.deviceLabel = "NoName";
        } else {
            this.deviceLabel = jsonObjectOfDeviceLabel.getAsString();
        }

        if (this.deviceLabel.length() == 0) {
            this.deviceLabel = "NoName";
        }

        JsonObject jsonObjectOfChannels = jsonObjectOfId.getAsJsonObject("channels");

        if (null != jsonObjectOfChannels) {
            Set<String> keys = jsonObjectOfChannels.keySet();

            Iterator<String> iter = keys.iterator();

            // Scan channels for functions
            while (iter.hasNext()) {
                String nextChannel = iter.next();

                JsonObject channelObject = jsonObjectOfChannels.getAsJsonObject(nextChannel);

                String channelFunctionID = channelObject.get("functionID").getAsString();

                if (true == sceneIsDetected) {
                    channelFunctionID = channelFunctionID.substring(0, channelFunctionID.length() - 1) + "0";
                }

                if (false == channelFunctionID.isEmpty()) {

                    FreeAtHomeDeviceChannel newChannel = new FreeAtHomeDeviceChannel();

                    newChannel.channelLabel = channelObject.get("displayName").getAsString();

                    if (newChannel.channelLabel.length() == 0) {
                        newChannel.channelLabel = this.deviceLabel;
                    }

                    switch (getIntegerFromHex(channelFunctionID)) {

                        // increment trigger channel - this handled identically as actuator
                        case FID_TRIGGER:
                        case FID_SWITCH_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.ACTUATOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_ACTUATOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 1,
                                    "deviceIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 256,
                                    "deviceOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }
                        // increment thermostat channels
                        case FID_RADIATOR_ACTUATOR_MASTER: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.THERMOSTAT_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_THERMOSTAT;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 304,
                                    "measuredTempOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 333,
                                    "heatingDemandOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 331,
                                    "heatingActiveOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 51,
                                    "setpointTempOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 54,
                                    "statesOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 320,
                                    "setpointTempIdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 66,
                                    "onoffSwitchIdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 58,
                                    "ecoSwitchIdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 56,
                                    "onoffIndicationOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 68,
                                    "ecoIndicationOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }
                        // increment thermostat channels
                        case FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.THERMOSTAT_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_THERMOSTAT;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 304,
                                    "measuredTempOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 333,
                                    "heatingDemandOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 331,
                                    "heatingActiveOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 51,
                                    "setpointTempOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 54,
                                    "statesOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 320,
                                    "setpointTempIdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 66,
                                    "onoffSwitchIdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 58,
                                    "ecoSwitchIdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 56,
                                    "onoffIndicationOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 58,
                                    "ecoIndicationOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }
                        // increment window sensor channels
                        case FID_WINDOW_DOOR_SENSOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.WINDOWSENSOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_WINDOWSENSOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 53,
                                    "deviceStateOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 41,
                                    "devicePosOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_WINDOW_DOOR_POSITION_SENSOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.WINDOWSENSOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_WINDOWPOSSENSOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 53,
                                    "deviceStateOdp", channelObject);

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 41,
                                    "devicePosOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_SCENE_TRIGGER: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.SCENE_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_SCENE;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 4,
                                    "deviceOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_RULE_SWITCH: {
                            if (true == ruleIsDetected) {

                                newChannel.channelId = nextChannel;
                                newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.RULE_TYPE_ID;
                                newChannel.channelTypeString = CHANNEL_TYPE_RULE;

                                newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 61697,
                                        "deviceIdp", channelObject);
                                newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 61698,
                                        "deviceOdp", channelObject);

                                listOfThings.add(newChannel);

                                validDevice = true;
                            }

                            break;
                        }

                        case FID_DES_DOOR_OPENER_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.DOOROPENER_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_DOOROPENERACTUATOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 2,
                                    "deviceIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 256,
                                    "deviceOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_DES_LEVEL_CALL_SENSOR:
                        case FID_DES_DOOR_RINGING_SENSOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.DOORRINGSENSOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_RINGSENSOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 2,
                                    "deviceOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_DES_LIGHT_SWITCH_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.CORRIDORLIGHTSWITCH_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_CORRIDORLIGHTSWITCH;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 2,
                                    "deviceIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 256,
                                    "deviceOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_PANEL_MUTE_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.MUTEACTUATOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_MUTEACTUATOR;

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        case FID_DIMMING_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.DIMMINGACTUATOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_DIMMINGACTUATOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 17,
                                    "deviceDimIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 1,
                                    "deviceSwitchIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 272,
                                    "deviceDimOdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 256,
                                    "deviceSwitchOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }
                        case FID_SHUTTER_ACTUATOR: {
                            newChannel.channelId = nextChannel;
                            newChannel.thingTypeOfChannel = FreeAtHomeSystemBindingConstants.SHUTTERACTUATOR_TYPE_ID;
                            newChannel.channelTypeString = CHANNEL_TYPE_SHUTTERACTUATOR;

                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 32,
                                    "deviceStepIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 33,
                                    "deviceStopIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_INPUT, 35,
                                    "devicePosIdp", channelObject);
                            newChannel.searchForDatapoint(FreeAtHomeDeviceChannel.DATAPOINT_DIRECTION_OUTPUT, 289,
                                    "devicePosOdp", channelObject);

                            listOfThings.add(newChannel);

                            validDevice = true;

                            break;
                        }

                        default: {
                            logger.info("Unknown device found - device label: {} - Channel FID: {}", this.deviceLabel,
                                    channelFunctionID);

                            break;
                        }
                    }
                }
            }
        }
    }

    public int numberOfThings() {
        return listOfThings.size();
    }

    public int getIntegerFromHex(String strHexValue) {
        String digits = "0123456789ABCDEF";
        String str = strHexValue.toUpperCase();
        int hexval = 0;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            int d = digits.indexOf(c);
            hexval = 16 * hexval + d;
        }

        return hexval;
    }
}
