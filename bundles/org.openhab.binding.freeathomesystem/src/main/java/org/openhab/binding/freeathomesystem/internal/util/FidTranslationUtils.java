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
package org.openhab.binding.freeathomesystem.internal.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The {@link FidTranslationUtils} having constant values for json parsing
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
public class FidTranslationUtils {

    private static final Map<String, String> mapFunctionId;

    public static final int FID_UNKNOWN = 0xFFFFAAFF; // Control element

    // free@home constants
    public static final int FID_SWITCH_SENSOR = 0x0000; // Control element
    public static final int FID_DIMMING_SENSOR = 0x0001; // Dimming sensor
    public static final int FID_BLIND_SENSOR = 0x0003; // Blind sensor
    public static final int FID_STAIRCASE_LIGHT_SENSOR = 0x0004; // Stairwell light sensor
    public static final int FID_FORCE_ON_OFF_SENSOR = 0x0005; // Force On/Off sensor
    public static final int FID_SCENE_SENSOR = 0x0006; // Scene sensor
    public static final int FID_SWITCH_ACTUATOR = 0x0007; // Switch actuator
    public static final int FID_SHUTTER_ACTUATOR = 0x0009; // Blind actuator
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITH_FAN = 0x000A; // Room temperature controller
                                                                                      // with fan speed level
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_SLAVE = 0x000B; // Room temperature controller extension
                                                                            // unit
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
    public static final int FID_PROXY = 0x001B; // Proxy
    public static final int FID_DES_LEVEL_CALL_ACTUATOR = 0x001D; // Door Entry System Call Level Actuator
    public static final int FID_DES_LEVEL_CALL_SENSOR = 0x001E; // Door Entry System Call Level Sensor
    public static final int FID_DES_DOOR_RINGING_SENSOR = 0x001F; // Door call
    public static final int FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR = 0x0020; // Automatic door opener
    public static final int FID_DES_LIGHT_SWITCH_ACTUATOR = 0x0021; // Corridor light
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN = 0x0023; // Room temperature controller
    public static final int FID_COOLING_ACTUATOR = 0x0024; // Cooling mode
    public static final int FID_HEATING_ACTUATOR = 0x0027; // Heating mode
    public static final int FID_FORCE_UP_DOWN_SENSOR = 0x0028; // Force-position blind
    public static final int FID_HEATING_COOLING_ACTUATOR = 0x0029; // Auto. heating/cooling mode
    public static final int FID_HEATING_COOLING_SENSOR = 0x002A; // Switchover heating/cooling
    public static final int FID_DES_DEVICE_SETTINGS = 0x002B; // Device settings
    public static final int FID_RGB_W_ACTUATOR = 0x002E; // Dim actuator
    public static final int FID_RGB_ACTUATOR = 0x002F; // Dim actuator
    public static final int FID_PANEL_SWITCH_SENSOR = 0x0030; // Control element
    public static final int FID_PANEL_DIMMING_SENSOR = 0x0031; // Dimming sensor
    public static final int FID_PANEL_BLIND_SENSOR = 0x0033; // Blind sensor
    public static final int FID_PANEL_STAIRCASE_LIGHT_SENSOR = 0x0034; // Stairwell light sensor
    public static final int FID_PANEL_FORCE_ON_OFF_SENSOR = 0x0035; // Force On/Off sensor
    public static final int FID_PANEL_FORCE_UP_DOWN_SENSOR = 0x0036; // Force-position blind
    public static final int FID_PANEL_SCENE_SENSOR = 0x0037; // Scene sensor
    public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE = 0x0038; // Room temperature controller
                                                                                  // extension unit
    public static final int FID_PANEL_FAN_COIL_SENSOR = 0x0039; // Fan coil sensor
    public static final int FID_PANEL_RGB_CT_SENSOR = 0x003A; // RGB + warm white/cold white sensor
    public static final int FID_PANEL_RGB_SENSOR = 0x003B; // RGB sensor
    public static final int FID_PANEL_CT_SENSOR = 0x003C; // Warm white/cold white sensor
    public static final int FID_ADDITIONAL_HEATING_ACTUATOR = 0x003D; // Add. stage for heating mode
    public static final int FID_RADIATOR_ACTUATOR_MASTER = 0x003E; // Radiator thermostate
    public static final int FID_RADIATOR_ACTUATOR_SLAVE = 0x003F; // Room temperature controller extension unit
    public static final int FID_BRIGHTNESS_SENSOR = 0x0041; // Brightness sensor
    public static final int FID_RAIN_SENSOR = 0x0042; // Rain sensor
    public static final int FID_TEMPERATURE_SENSOR = 0x0043; // Temperature sensor
    public static final int FID_WIND_SENSOR = 0x0044; // Wind sensor
    public static final int FID_TRIGGER = 0x0045; // Trigger
    public static final int FID_FCA_2_PIPE_HEATING = 0x0047; // Heating mode
    public static final int FID_FCA_2_PIPE_COOLING = 0x0048; // Cooling mode
    public static final int FID_FCA_2_PIPE_HEATING_COOLING = 0x0049; // Auto. heating/cooling mode
    public static final int FID_FCA_4_PIPE_HEATING_AND_COOLING = 0x004A; // Two valves for heating and cooling
    public static final int FID_WINDOW_DOOR_ACTUATOR = 0x004B; // Window/Door
    public static final int FID_INVERTER_INFO = 0x004E; // ABC
    public static final int FID_METER_INFO = 0x004F; // ABD
    public static final int FID_BATTERY_INFO = 0x0050; // ACD
    public static final int FID_PANEL_TIMER_PROGRAM_SWITCH_SENSOR = 0x0051; // Timer program switch sensor
    public static final int FID_DOMUSTECH_ZONE = 0x0055; // Zone
    public static final int FID_CENTRAL_HEATING_ACTUATOR = 0x0056; // Central heating actuator
    public static final int FID_CENTRAL_COOLING_ACTUATOR = 0x0057; // Central cooling actuator
    public static final int FID_HOUSE_KEEPING = 0x0059; // Housekeeping
    public static final int FID_MEDIA_PLAYER = 0x005A; // Media Player
    public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE = 0x005B; // Panel Room
                                                                                                     // Temperature
                                                                                                     // Controller Slave
                                                                                                     // For Battery
                                                                                                     // Device
    public static final int FID_PANEL_MEDIA_PLAYER_SENSOR = 0x0060; // Media Player Sensor
    public static final int FID_BLIND_ACTUATOR = 0x0061; // Roller blind actuator
    public static final int FID_ATTIC_WINDOW_ACTUATOR = 0x0062; // Attic window actuator
    public static final int FID_AWNING_ACTUATOR = 0x0063; // Awning actuator
    public static final int FID_WINDOW_DOOR_POSITION_SENSOR = 0x0064; // WindowDoor Position Sensor
    public static final int FID_WINDOW_DOOR_POSITION_ACTUATOR = 0x0065; // Window/Door position
    public static final int FID_MEDIA_PLAYBACK_CONTROL_SENSOR = 0x0066; // Media playback control sensor
    public static final int FID_MEDIA_VOLUME_SENSOR = 0x0067; // Media volume sensor
    public static final int FID_DISHWASHER = 0x0068; // Dishwasher
    public static final int FID_LAUNDRY = 0x0069; // Laundry
    public static final int FID_DRYER = 0x006A; // Dryer
    public static final int FID_OVEN = 0x006B; // Oven
    public static final int FID_FRIDGE = 0x006C; // Fridge
    public static final int FID_FREEZER = 0x006D; // Freezer
    public static final int FID_HOOD = 0x006E; // Hood
    public static final int FID_COFFEE_MACHINE = 0x006F; // Coffee machine
    public static final int FID_FRIDGE_FREEZER = 0x0070; // Fridge/Freezer
    public static final int FID_TIMER_PROGRAM_OR_ALERT_SWITCH_SENSOR = 0x0071; // Timer program switch sensor
    public static final int FID_CEILING_FAN_ACTUATOR = 0x0073; // Ceiling fan actuator
    public static final int FID_CEILING_FAN_SENSOR = 0x0074; // Ceiling fan sensor
    public static final int FID_SPLIT_UNIT_GATEWAY = 0x0075; // Room temperature controller with fan speed level
    public static final int FID_ZONE = 0x0076; // Zone
    public static final int FID_24H_ZONE = 0x0077; // Safety
    public static final int FID_EXTERNAL_IR_SENSOR_BX80 = 0x0078; // External IR Sensor BX80
    public static final int FID_EXTERNAL_IR_SENSOR_VXI = 0x0079; // External IR Sensor VXI
    public static final int FID_EXTERNAL_IR_SENSOR_MINI = 0x007A; // External IR Sensor Mini
    public static final int FID_EXTERNAL_IR_SENSOR_HIGH_ALTITUDE = 0x007B; // External IR Sensor High Altitude
    public static final int FID_EXTERNAL_IR_SENSOR_CURTAIN = 0x007C; // External IR Sensor Curtain
    public static final int FID_SMOKE_DETECTOR = 0x007D; // Smoke Detector
    public static final int FID_CARBON_MONOXIDE_SENSOR = 0x007E; // Carbon Monoxide Sensor
    public static final int FID_METHANE_DETECTOR = 0x007F; // Methane Detector
    public static final int FID_GAS_SENSOR_LPG = 0x0080; // Gas Sensor LPG
    public static final int FID_FLOOD_DETECTION = 0x0081; // Flood Detection
    public static final int FID_DOMUS_CENTRAL_UNIT_NEXTGEN = 0x0082; // secure@home Central Unit
    public static final int FID_THERMOSTAT = 0x0083; // Thermostat
    public static final int FID_PANEL_DOMUS_ZONE_SENSOR = 0x0084; // secure@home Zone Sensor
    public static final int FID_THERMOSTAT_SLAVE = 0x0085; // Slave thermostat
    public static final int FID_DOMUS_SECURE_INTEGRATION = 0x0086; // secure@home Integration Logic
    public static final int FID_ADDITIONAL_COOLING_ACTUATOR = 0x0087; // Add. stage for cooling mode
    public static final int FID_TWO_LEVEL_HEATING_ACTUATOR = 0x0088; // Two Level Heating Actuator
    public static final int FID_TWO_LEVEL_COOLING_ACTUATOR = 0x0089; // Two Level Cooling Actuator
    public static final int FID_GLOBAL_ZONE = 0x008E; // Zone
    public static final int FID_VOLUME_UP_SENSOR = 0x008F; // Volume up
    public static final int FID_VOLUME_DOWN_SENSOR = 0x0090; // Volume down
    public static final int FID_PLAY_PAUSE_SENSOR = 0x0091; // Play/pause
    public static final int FID_NEXT_FAVORITE_SENSOR = 0x0092; // Next favorite
    public static final int FID_NEXT_SONG_SENSOR = 0x0093; // Next song
    public static final int FID_PREVIOUS_SONG_SENSOR = 0x0094; // Previous song
    public static final int FID_HOME_APPLIANCE_SENSOR = 0x0095; // Home appliance sensor
    public static final int FID_HEAT_SENSOR = 0x0096; // Heat sensor
    public static final int FID_ZONE_SWITCHING = 0x0097; // Zone switching
    public static final int FID_SECURE_AT_HOME_FUNCTION = 0x0098; // Button function
    public static final int FID_COMPLEX_CONFIGURATION = 0x0099; // Advanced configuration
    public static final int FID_DOMUS_CENTRAL_UNIT_BASIC = 0x009A; // secure@home Central Unit Basic
    public static final int FID_DOMUS_REPEATER = 0x009B; // Repeater
    public static final int FID_DOMUS_SCENE_TRIGGER = 0x009C; // Remote scene control
    public static final int FID_DOMUSWINDOWCONTACT = 0x009D; // Window sensor
    public static final int FID_DOMUSMOVEMENTDETECTOR = 0x009E; // Movement Detector
    public static final int FID_DOMUSCURTAINDETECTOR = 0x009F; // External IR Sensor Curtain
    public static final int FID_DOMUSSMOKEDETECTOR = 0x00A0; // Smoke Detector
    public static final int FID_DOMUSFLOODDETECTOR = 0x00A1; // Flood Detection
    public static final int FID_PANEL_SUG_SENSOR = 0x00A3; // Sensor for air-conditioning unit
    public static final int FID_TWO_LEVEL_HEATING_COOLING_ACTUATOR = 0x00A4; // Two-point controller for heating or
                                                                             // cooling
    public static final int FID_PANEL_THERMOSTAT_CONTROLLER_SLAVE = 0x00A5; // Slave thermostat
    public static final int FID_WALLBOX = 0x00A6; // Wallbox
    public static final int FID_PANEL_WALLBOX = 0x00A7; // Wallbox
    public static final int FID_DOOR_LOCK_CONTROL = 0x00A8; // Door lock control
    public static final int FID_VRV_GATEWAY = 0x00AA; // Room temperature controller with fan speed level

    public static final int FID_SCENE_TRIGGER = 0x4800; // Scene trigger
    public static final int FID_RULE_SWITCH = 0x4A00; // Scene trigger

    static {
        Map<String, String> mapDesc = new HashMap<String, String>();

        mapDesc.put("0x0000", "Control element"); // FID_SWITCH_SENSOR
        mapDesc.put("0x0001", "Dimming sensor"); // FID_DIMMING_SENSOR
        mapDesc.put("0x0003", "Blind sensor"); // FID_BLIND_SENSOR
        mapDesc.put("0x0004", "Stairwell light sensor"); // FID_STAIRCASE_LIGHT_SENSOR
        mapDesc.put("0x0005", "Force On/Off sensor"); // FID_FORCE_ON_OFF_SENSOR
        mapDesc.put("0x0006", "Scene sensor"); // FID_SCENE_SENSOR
        mapDesc.put("0x0007", "Switch actuator"); // FID_SWITCH_ACTUATOR
        mapDesc.put("0x0009", "Blind actuator"); // FID_SHUTTER_ACTUATOR
        mapDesc.put("0x000A", "Room temperature controller with fan speed level"); // FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITH_FAN
        mapDesc.put("0x000B", "Room temperature controller extension unit"); // FID_ROOM_TEMPERATURE_CONTROLLER_SLAVE
        mapDesc.put("0x000C", "Wind Alarm"); // FID_WIND_ALARM_SENSOR
        mapDesc.put("0x000D", "Frost Alarm"); // FID_FROST_ALARM_SENSOR
        mapDesc.put("0x000E", "Rain Alarm"); // FID_RAIN_ALARM_SENSOR
        mapDesc.put("0x000F", "Window sensor"); // FID_WINDOW_DOOR_SENSOR
        mapDesc.put("0x0011", "Movement Detector"); // FID_MOVEMENT_DETECTOR
        mapDesc.put("0x0012", "Dim actuator"); // FID_DIMMING_ACTUATOR
        mapDesc.put("0x0014", "Radiator"); // FID_RADIATOR_ACTUATOR
        mapDesc.put("0x0015", "Underfloor heating"); // FID_UNDERFLOOR_HEATING
        mapDesc.put("0x0016", "Fan Coil"); // FID_FAN_COIL
        mapDesc.put("0x0017", "Two-level controller"); // FID_TWO_LEVEL_CONTROLLER
        mapDesc.put("0x001A", "Door opener"); // FID_DES_DOOR_OPENER_ACTUATOR
        mapDesc.put("0x001B", "Proxy"); // FID_PROXY
        mapDesc.put("0x001D", "Door Entry System Call Level Actuator"); // FID_DES_LEVEL_CALL_ACTUATOR
        mapDesc.put("0x001E", "Door Entry System Call Level Sensor"); // FID_DES_LEVEL_CALL_SENSOR
        mapDesc.put("0x001F", "Door call"); // FID_DES_DOOR_RINGING_SENSOR
        mapDesc.put("0x0020", "Automatic door opener"); // FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR
        mapDesc.put("0x0021", "Corridor light"); // FID_DES_LIGHT_SWITCH_ACTUATOR
        mapDesc.put("0x0023", "Room temperature controller"); // FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN
        mapDesc.put("0x0024", "Cooling mode"); // FID_COOLING_ACTUATOR
        mapDesc.put("0x0027", "Heating mode"); // FID_HEATING_ACTUATOR
        mapDesc.put("0x0028", "Force-position blind"); // FID_FORCE_UP_DOWN_SENSOR
        mapDesc.put("0x0029", "Auto. heating/cooling mode"); // FID_HEATING_COOLING_ACTUATOR
        mapDesc.put("0x002A", "Switchover heating/cooling"); // FID_HEATING_COOLING_SENSOR
        mapDesc.put("0x002B", "Device settings"); // FID_DES_DEVICE_SETTINGS
        mapDesc.put("0x002E", "Dim actuator"); // FID_RGB_W_ACTUATOR
        mapDesc.put("0x002F", "Dim actuator"); // FID_RGB_ACTUATOR
        mapDesc.put("0x0030", "Control element"); // FID_PANEL_SWITCH_SENSOR
        mapDesc.put("0x0031", "Dimming sensor"); // FID_PANEL_DIMMING_SENSOR
        mapDesc.put("0x0033", "Blind sensor"); // FID_PANEL_BLIND_SENSOR
        mapDesc.put("0x0034", "Stairwell light sensor"); // FID_PANEL_STAIRCASE_LIGHT_SENSOR
        mapDesc.put("0x0035", "Force On/Off sensor"); // FID_PANEL_FORCE_ON_OFF_SENSOR
        mapDesc.put("0x0036", "Force-position blind"); // FID_PANEL_FORCE_UP_DOWN_SENSOR
        mapDesc.put("0x0037", "Scene sensor"); // FID_PANEL_SCENE_SENSOR
        mapDesc.put("0x0038", "Room temperature controller extension unit"); // FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE
        mapDesc.put("0x0039", "Fan coil sensor"); // FID_PANEL_FAN_COIL_SENSOR
        mapDesc.put("0x003A", "RGB + warm white/cold white sensor"); // FID_PANEL_RGB_CT_SENSOR
        mapDesc.put("0x003B", "RGB sensor"); // FID_PANEL_RGB_SENSOR
        mapDesc.put("0x003C", "Warm white/cold white sensor"); // FID_PANEL_CT_SENSOR
        mapDesc.put("0x003D", "Add. stage for heating mode"); // FID_ADDITIONAL_HEATING_ACTUATOR
        mapDesc.put("0x003E", "Radiator thermostate"); // FID_RADIATOR_ACTUATOR_MASTER
        mapDesc.put("0x003F", "Room temperature controller extension unit"); // FID_RADIATOR_ACTUATOR_SLAVE
        mapDesc.put("0x0041", "Brightness sensor"); // FID_BRIGHTNESS_SENSOR
        mapDesc.put("0x0042", "Rain sensor"); // FID_RAIN_SENSOR
        mapDesc.put("0x0043", "Temperature sensor"); // FID_TEMPERATURE_SENSOR
        mapDesc.put("0x0044", "Wind sensor"); // FID_WIND_SENSOR
        mapDesc.put("0x0045", "Trigger"); // FID_TRIGGER
        mapDesc.put("0x0047", "Heating mode"); // FID_FCA_2_PIPE_HEATING
        mapDesc.put("0x0048", "Cooling mode"); // FID_FCA_2_PIPE_COOLING
        mapDesc.put("0x0049", "Auto. heating/cooling mode"); // FID_FCA_2_PIPE_HEATING_COOLING
        mapDesc.put("0x004A", "Two valves for heating and cooling"); // FID_FCA_4_PIPE_HEATING_AND_COOLING
        mapDesc.put("0x004B", "Window/Door"); // FID_WINDOW_DOOR_ACTUATOR
        mapDesc.put("0x004E", "ABC"); // FID_INVERTER_INFO
        mapDesc.put("0x004F", "ABD"); // FID_METER_INFO
        mapDesc.put("0x0050", "ACD"); // FID_BATTERY_INFO
        mapDesc.put("0x0051", "Timer program switch sensor"); // FID_PANEL_TIMER_PROGRAM_SWITCH_SENSOR
        mapDesc.put("0x0055", "Zone"); // FID_DOMUSTECH_ZONE
        mapDesc.put("0x0056", "Central heating actuator"); // FID_CENTRAL_HEATING_ACTUATOR
        mapDesc.put("0x0057", "Central cooling actuator"); // FID_CENTRAL_COOLING_ACTUATOR
        mapDesc.put("0x0059", "Housekeeping"); // FID_HOUSE_KEEPING
        mapDesc.put("0x005A", "Media Player"); // FID_MEDIA_PLAYER
        mapDesc.put("0x005B", "Panel Room Temperature Controller Slave For Battery Device"); // FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE
        mapDesc.put("0x0060", "Media Player Sensor"); // FID_PANEL_MEDIA_PLAYER_SENSOR
        mapDesc.put("0x0061", "Roller blind actuator"); // FID_BLIND_ACTUATOR
        mapDesc.put("0x0062", "Attic window actuator"); // FID_ATTIC_WINDOW_ACTUATOR
        mapDesc.put("0x0063", "Awning actuator"); // FID_AWNING_ACTUATOR
        mapDesc.put("0x0064", "WindowDoor Position Sensor"); // FID_WINDOW_DOOR_POSITION_SENSOR
        mapDesc.put("0x0065", "Window/Door position"); // FID_WINDOW_DOOR_POSITION_ACTUATOR
        mapDesc.put("0x0066", "Media playback control sensor"); // FID_MEDIA_PLAYBACK_CONTROL_SENSOR
        mapDesc.put("0x0067", "Media volume sensor"); // FID_MEDIA_VOLUME_SENSOR
        mapDesc.put("0x0068", "Dishwasher"); // FID_DISHWASHER
        mapDesc.put("0x0069", "Laundry"); // FID_LAUNDRY
        mapDesc.put("0x006A", "Dryer"); // FID_DRYER
        mapDesc.put("0x006B", "Oven"); // FID_OVEN
        mapDesc.put("0x006C", "Fridge"); // FID_FRIDGE
        mapDesc.put("0x006D", "Freezer"); // FID_FREEZER
        mapDesc.put("0x006E", "Hood"); // FID_HOOD
        mapDesc.put("0x006F", "Coffee machine"); // FID_COFFEE_MACHINE
        mapDesc.put("0x0070", "Fridge/Freezer"); // FID_FRIDGE_FREEZER
        mapDesc.put("0x0071", "Timer program switch sensor"); // FID_TIMER_PROGRAM_OR_ALERT_SWITCH_SENSOR
        mapDesc.put("0x0073", "Ceiling fan actuator"); // FID_CEILING_FAN_ACTUATOR
        mapDesc.put("0x0074", "Ceiling fan sensor"); // FID_CEILING_FAN_SENSOR
        mapDesc.put("0x0075", "Room temperature controller with fan speed level"); // FID_SPLIT_UNIT_GATEWAY
        mapDesc.put("0x0076", "Zone"); // FID_ZONE
        mapDesc.put("0x0077", "Safety"); // FID_24H_ZONE
        mapDesc.put("0x0078", "External IR Sensor BX80"); // FID_EXTERNAL_IR_SENSOR_BX80
        mapDesc.put("0x0079", "External IR Sensor VXI"); // FID_EXTERNAL_IR_SENSOR_VXI
        mapDesc.put("0x007A", "External IR Sensor Mini"); // FID_EXTERNAL_IR_SENSOR_MINI
        mapDesc.put("0x007B", "External IR Sensor High Altitude"); // FID_EXTERNAL_IR_SENSOR_HIGH_ALTITUDE
        mapDesc.put("0x007C", "External IR Sensor Curtain"); // FID_EXTERNAL_IR_SENSOR_CURTAIN
        mapDesc.put("0x007D", "Smoke Detector"); // FID_SMOKE_DETECTOR
        mapDesc.put("0x007E", "Carbon Monoxide Sensor"); // FID_CARBON_MONOXIDE_SENSOR
        mapDesc.put("0x007F", "Methane Detector"); // FID_METHANE_DETECTOR
        mapDesc.put("0x0080", "Gas Sensor LPG"); // FID_GAS_SENSOR_LPG
        mapDesc.put("0x0081", "Flood Detection"); // FID_FLOOD_DETECTION
        mapDesc.put("0x0082", "secure@home Central Unit"); // FID_DOMUS_CENTRAL_UNIT_NEXTGEN
        mapDesc.put("0x0083", "Thermostat"); // FID_THERMOSTAT
        mapDesc.put("0x0084", "secure@home Zone Sensor"); // FID_PANEL_DOMUS_ZONE_SENSOR
        mapDesc.put("0x0085", "Slave thermostat"); // FID_THERMOSTAT_SLAVE
        mapDesc.put("0x0086", "secure@home Integration Logic"); // FID_DOMUS_SECURE_INTEGRATION
        mapDesc.put("0x0087", "Add. stage for cooling mode"); // FID_ADDITIONAL_COOLING_ACTUATOR
        mapDesc.put("0x0088", "Two Level Heating Actuator"); // FID_TWO_LEVEL_HEATING_ACTUATOR
        mapDesc.put("0x0089", "Two Level Cooling Actuator"); // FID_TWO_LEVEL_COOLING_ACTUATOR
        mapDesc.put("0x008E", "Zone"); // FID_GLOBAL_ZONE
        mapDesc.put("0x008F", "Volume up"); // FID_VOLUME_UP_SENSOR
        mapDesc.put("0x0090", "Volume down"); // FID_VOLUME_DOWN_SENSOR
        mapDesc.put("0x0091", "Play/pause"); // FID_PLAY_PAUSE_SENSOR
        mapDesc.put("0x0092", "Next favorite"); // FID_NEXT_FAVORITE_SENSOR
        mapDesc.put("0x0093", "Next song"); // FID_NEXT_SONG_SENSOR
        mapDesc.put("0x0094", "Previous song"); // FID_PREVIOUS_SONG_SENSOR
        mapDesc.put("0x0095", "Home appliance sensor"); // FID_HOME_APPLIANCE_SENSOR
        mapDesc.put("0x0096", "Heat sensor"); // FID_HEAT_SENSOR
        mapDesc.put("0x0097", "Zone switching"); // FID_ZONE_SWITCHING
        mapDesc.put("0x0098", "Button function"); // FID_SECURE_AT_HOME_FUNCTION
        mapDesc.put("0x0099", "Advanced configuration"); // FID_COMPLEX_CONFIGURATION
        mapDesc.put("0x009A", "secure@home Central Unit Basic"); // FID_DOMUS_CENTRAL_UNIT_BASIC
        mapDesc.put("0x009B", "Repeater"); // FID_DOMUS_REPEATER
        mapDesc.put("0x009C", "Remote scene control"); // FID_DOMUS_SCENE_TRIGGER
        mapDesc.put("0x009D", "Window sensor"); // FID_DOMUSWINDOWCONTACT
        mapDesc.put("0x009E", "Movement Detector"); // FID_DOMUSMOVEMENTDETECTOR
        mapDesc.put("0x009F", "External IR Sensor Curtain"); // FID_DOMUSCURTAINDETECTOR
        mapDesc.put("0x00A0", "Smoke Detector"); // FID_DOMUSSMOKEDETECTOR
        mapDesc.put("0x00A1", "Flood Detection"); // FID_DOMUSFLOODDETECTOR
        mapDesc.put("0x00A3", "Sensor for air-conditioning unit"); // FID_PANEL_SUG_SENSOR
        mapDesc.put("0x00A4", "Two-point controller for heating or cooling"); // FID_TWO_LEVEL_HEATING_COOLING_ACTUATOR
        mapDesc.put("0x00A5", "Slave thermostat"); // FID_PANEL_THERMOSTAT_CONTROLLER_SLAVE
        mapDesc.put("0x00A6", "Wallbox"); // FID_WALLBOX
        mapDesc.put("0x00A7", "Wallbox"); // FID_PANEL_WALLBOX
        mapDesc.put("0x00A8", "Door lock control"); // FID_DOOR_LOCK_CONTROL
        mapDesc.put("0x00AA", "Room temperature controller with fan speed level"); // FID_VRV_GATEWAY

        mapFunctionId = Collections.unmodifiableMap(mapDesc);

    }

    public static String getFunctionIdText(String Key) {
        String functionIdString = mapFunctionId.get(Key);

        return functionIdString;
    }
}
