/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.freeathome.internal.util;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link FidTranslationUtils} having constant values for json parsing
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FidTranslationUtils {

    public static final int FID_UNKNOWN = 0xFFFFAAFF; // Unknown

    // free@home constants
    // Function IDs https://developer.eu.mybuildings.abb.com/fah_cloud/reference/functionids
    // Pairing IDs https://developer.eu.mybuildings.abb.com/fah_cloud/references/pairingids
    public static final int FID_SWITCH_SENSOR = 0x0000; // Control element
    public static final int FID_DIMMING_SENSOR = 0x0001; // Dimming sensor
    public static final int FID_SHUTTER_SENSOR = 0x0002; // Shutter Sensor
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
    public static final int FID_PUSH_BUTTON_SENSOR = 0x0018; // Push button
    public static final int FID_RING_INDICATION_SENSOR = 0x0019; // Ring indicator
    public static final int FID_DES_DOOR_OPENER_ACTUATOR = 0x001A; // Door opener
    public static final int FID_PROXY = 0x001B; // Proxy
    public static final int FID_DES_LEVEL_CALL_ACTUATOR = 0x001D; // Door Map.entry System Call Level Actuator
    public static final int FID_DES_LEVEL_CALL_SENSOR = 0x001E; // Door Map.entry System Call Level Sensor
    public static final int FID_DES_DOOR_RINGING_SENSOR = 0x001F; // Door call
    public static final int FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR = 0x0020; // Automatic door opener
    public static final int FID_DES_LIGHT_SWITCH_ACTUATOR = 0x0021; // Corridor light
    public static final int FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN = 0x0023; // Room temperature controller
    public static final int FID_COOLING_ACTUATOR = 0x0024; // Cooling mode
    public static final int FID_DAY_NIGHT_SENSOR = 0x0025; // Day/night sensor
    public static final int FID_HEATING_ACTUATOR = 0x0027; // Heating mode
    public static final int FID_FORCE_UP_DOWN_SENSOR = 0x0028; // Force-position blind
    public static final int FID_HEATING_COOLING_ACTUATOR = 0x0029; // Auto. heating/cooling mode
    public static final int FID_HEATING_COOLING_SENSOR = 0x002A; // Switchover heating/cooling
    public static final int FID_DES_DEVICE_SETTINGS = 0x002B; // Device settings
    public static final int FID_SACE_BLIND_ACTUATOR = 0x002C; // Space blind
    public static final int FID_RGB_SENSOR = 0x002D; // RGB sensor
    public static final int FID_RGB_W_ACTUATOR = 0x002E; // Dim actuator
    public static final int FID_RGB_ACTUATOR = 0x002F; // Dim actuator
    public static final int FID_PANEL_SWITCH_SENSOR = 0x0030; // Control element
    public static final int FID_PANEL_DIMMING_SENSOR = 0x0031; // Dimming sensor
    public static final int FID_PANEL_SHUTTER_SENSOR = 0x0032; // Shutter
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
    public static final int FID_COLORTEMPERATURE_ACTUATOR = 0x0040; // Color temperature
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
    public static final int FID_INVERTER_INFO = 0x004E; // Inverter information
    public static final int FID_METER_INFO = 0x004F; // Meter information
    public static final int FID_BATTERY_INFO = 0x0050; // Battery information
    public static final int FID_PANEL_TIMER_PROGRAM_SWITCH_SENSOR = 0x0051; // Timer program switch sensor
    public static final int FID_SAFETY_SENSOR = 0x0053; // Safety sensor
    public static final int FID_DOMUSTECH_ZONE = 0x0055; // Zone
    public static final int FID_CENTRAL_HEATING_ACTUATOR = 0x0056; // Central heating actuator
    public static final int FID_CENTRAL_COOLING_ACTUATOR = 0x0057; // Central cooling actuator
    public static final int FID_LINK_ACTUATOR = 0x0058; // Link actuator
    public static final int FID_HOUSE_KEEPING = 0x0059; // Housekeeping
    public static final int FID_MEDIA_PLAYER = 0x005A; // Media Player
    public static final int FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE = 0x005B; // Panel Room
                                                                                                     // Temperature
                                                                                                     // Controller Slave
                                                                                                     // For Battery
                                                                                                     // Device
    public static final int FID_WELCOME_IP_MUTE_SENSOR = 0x005C; // Welcome-IP mute sensor
    public static final int FID_WELCOME_IP_MUTE_ACTUATOR = 0x005D; // Welcome-IP mute
    public static final int FID_WELCOME_IP_DOOR_OPEN_SENSOR = 0x005E; // Welcome-IP doors open sensor
    public static final int FID_WELCOME_IP_SWITCH_SENSOR = 0x005F; // Welcome-IP switch
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
    public static final int FID_WELCOME_IP_BELL_INDICATOR_SENSOR = 0x0072; // Welcome-IP bell indicator
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
    public static final int FID_DOOR_LOCK_SENSOR = 0x008A; // Door lock sensor
    public static final int FID_DOOR_LOCK_ACTUATOR = 0x008B; // Door lock actuator
    public static final int FID_AC_ROUTING = 0x008C; // AC routing
    public static final int FID_EXTERNAL_SIREN = 0x008D; // External siren
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
    public static final int FID_HOB = 0x00A2; // HOB
    public static final int FID_PANEL_SUG_SENSOR = 0x00A3; // Sensor for air-conditioning unit
    public static final int FID_TWO_LEVEL_HEATING_COOLING_ACTUATOR = 0x00A4; // Two-point controller for heating or
                                                                             // cooling
    public static final int FID_PANEL_THERMOSTAT_CONTROLLER_SLAVE = 0x00A5; // Slave thermostat
    public static final int FID_WALLBOX = 0x00A6; // Wallbox
    public static final int FID_PANEL_WALLBOX = 0x00A7; // Wallbox
    public static final int FID_DOOR_LOCK_CONTROL = 0x00A8; // Door lock control
    public static final int FID_DOOR_LOCK_SETTINGS = 0x00A9; // Door lock settings
    public static final int FID_VRV_GATEWAY = 0x00AA; // Room temperature controller with fan speed level
    public static final int FID_CO_2 = 0x00BA; // Co2
    public static final int FID_VOC = 0x00BB; // Voc
    public static final int FID_AIRQUALITY_SENSOR = 0x00BD; // Airquality sensor
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE0 = 0x1008; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE1 = 0x1009; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE2 = 0x100A; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE3 = 0x100B; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE4 = 0x100C; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE5 = 0x100D; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE6 = 0x100E; // Switch sensor push button
    public static final int FID_SWITCH_SENSOR_PUSHBUTTON_TYPE7 = 0x100F; // Switch sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE0 = 0x1018; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE1 = 0x1019; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE2 = 0x101A; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE3 = 0x101B; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE4 = 0x101C; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE5 = 0x101D; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE6 = 0x101E; // Dimming sensor push button
    public static final int FID_DIMMING_SENSOR_PUSHBUTTON_TYPE7 = 0x101F; // Dimming sensor push button
    public static final int FID_MOVEMENT_DETECTOR_FLEX = 0x1090; // Movement detector flex
    public static final int FID_MOVEMENT_DETECTOR_TYPE0 = 0x1090; // Movement Detector
    public static final int FID_DIMMING_ACTUATOR_FLEX = 0x1810; // Switch actuator flex
    public static final int FID_DIMMING_ACTUATOR_TYPE0 = 0x1810; // Dim actuator
    public static final int FID_BLIND_ACTUATOR_WIRELESS = 0x1821; // Wireless blind actuator
    public static final int FID_SCENE_TRIGGER = 0x4800; // Scene trigger
    public static final int FID_RULE_SWITCH = 0x4A00; // Rule Switch
    public static final int FID_AIRQUALITYSENSOR_HUMIDITY = 0xB03F; // Air quality sensor humidity
    public static final int FID_AIRQUALITYSENSOR_PRESSURE = 0xE017; // Air quality sensor Pressure
    public static final int FID_AIRQUALITYSENSOR_CO2 = 0xE018; // Air quality sensor CO2
    public static final int FID_AIRQUALITYSENSOR_CO = 0xE019; // Air quality sensor CO
    public static final int FID_AIRQUALITYSENSOR_NO2 = 0xE01A; // Air quality sensor NO2
    public static final int FID_AIRQUALITYSENSOR_O3 = 0xE01B; // Air quality sensor O3
    public static final int FID_AIRQUALITYSENSOR_PM10 = 0xE01C; // Air quality sensor PM10
    public static final int FID_AIRQUALITYSENSOR_PM25 = 0xE01D; // Air quality sensor PM25
    public static final int FID_AIRQUALITYSENSOR_VOC = 0xE01E; // Air quality sensor VOC

    private static final Map<String, String> MAP_FUNCTION_ID = Map.ofEntries(Map.entry("0x0000", "fid-control-element"), // FID_SWITCH_SENSOR
            Map.entry("0x0001", "fid-dimming-sensor"), // FID_DIMMING_SENSOR
            Map.entry("0x0002", "fid-shutter-sensor"), // FID_SHUTTER_SENSOR
            Map.entry("0x0003", "fid-blind-sensor"), // FID_BLIND_SENSOR
            Map.entry("0x0004", "fid-stairwell-light-sensor"), // FID_STAIRCASE_LIGHT_SENSOR
            Map.entry("0x0005", "fid-force-on/off-sensor"), // FID_FORCE_ON_OFF_SENSOR
            Map.entry("0x0006", "fid-scene-sensor"), // FID_SCENE_SENSOR
            Map.entry("0x0007", "fid-switch-actuator"), // FID_SWITCH_ACTUATOR
            Map.entry("0x0009", "fid-blind-actuator"), // FID_SHUTTER_ACTUATOR
            Map.entry("0x000A", "fid-room-temperature-controller-with-fan-speed-level"), // FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITH_FAN
            Map.entry("0x000B", "fid-room-temperature-controller-extension-unit"), // FID_ROOM_TEMPERATURE_CONTROLLER_SLAVE
            Map.entry("0x000C", "fid-wind-alarm"), // FID_WIND_ALARM_SENSOR
            Map.entry("0x000D", "fid-frost-alarm"), // FID_FROST_ALARM_SENSOR
            Map.entry("0x000E", "fid-rain-alarm"), // FID_RAIN_ALARM_SENSOR
            Map.entry("0x000F", "fid-window-sensor"), // FID_WINDOW_DOOR_SENSOR
            Map.entry("0x0011", "fid-movement-detector"), // FID_MOVEMENT_DETECTOR
            Map.entry("0x0012", "fid-dim-actuator"), // FID_DIMMING_ACTUATOR
            Map.entry("0x0014", "fid-radiator"), // FID_RADIATOR_ACTUATOR
            Map.entry("0x0015", "fid-underfloor-heating"), // FID_UNDERFLOOR_HEATING
            Map.entry("0x0016", "fid-fan-coil"), // FID_FAN_COIL
            Map.entry("0x0017", "fid-two-level-controller"), // FID_TWO_LEVEL_CONTROLLER
            Map.entry("0x0018", "fid-push-button"), // FID_PUSH_BUTTON_SENSOR
            Map.entry("0x0019", "fid-ring-indicator"), // FID_RING_INDICATION_SENSOR
            Map.entry("0x001A", "fid-door-opener"), // FID_DES_DOOR_OPENER_ACTUATOR
            Map.entry("0x001B", "fid-proxy"), // FID_PROXY
            Map.entry("0x001D", "fid-door-map.entry-system-call-level-actuator"), // FID_DES_LEVEL_CALL_ACTUATOR
            Map.entry("0x001E", "fid-door-map.entry-system-call-level-sensor"), // FID_DES_LEVEL_CALL_SENSOR
            Map.entry("0x001F", "fid-door-call"), // FID_DES_DOOR_RINGING_SENSOR
            Map.entry("0x0020", "fid-automatic-door-opener"), // FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR
            Map.entry("0x0021", "fid-corridor-light"), // FID_DES_LIGHT_SWITCH_ACTUATOR
            Map.entry("0x0023", "fid-room-temperature-controller"), // FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN
            Map.entry("0x0024", "fid-cooling-mode"), // FID_COOLING_ACTUATOR
            Map.entry("0x0025", "fid-day/night-sensor"), // FID_DAY_NIGHT_SENSOR
            Map.entry("0x0027", "fid-heating-mode"), // FID_HEATING_ACTUATOR
            Map.entry("0x0028", "fid-force-position-blind"), // FID_FORCE_UP_DOWN_SENSOR
            Map.entry("0x0029", "fid-auto.-heating/cooling-mode"), // FID_HEATING_COOLING_ACTUATOR
            Map.entry("0x002A", "fid-switchover-heating/cooling"), // FID_HEATING_COOLING_SENSOR
            Map.entry("0x002B", "fid-device-settings"), // FID_DES_DEVICE_SETTINGS
            Map.entry("0x002C", "fid-space-blind"), // FID_SACE_BLIND_ACTUATOR
            Map.entry("0x002D", "fid-rgb-sensor"), // FID_RGB_SENSOR
            Map.entry("0x002E", "fid-dim-actuator"), // FID_RGB_W_ACTUATOR
            Map.entry("0x002F", "fid-dim-actuator"), // FID_RGB_ACTUATOR
            Map.entry("0x0030", "fid-control-element"), // FID_PANEL_SWITCH_SENSOR
            Map.entry("0x0031", "fid-dimming-sensor"), // FID_PANEL_DIMMING_SENSOR
            Map.entry("0x0032", "fid-shutter"), // FID_PANEL_SHUTTER_SENSOR
            Map.entry("0x0033", "fid-blind-sensor"), // FID_PANEL_BLIND_SENSOR
            Map.entry("0x0034", "fid-stairwell-light-sensor"), // FID_PANEL_STAIRCASE_LIGHT_SENSOR
            Map.entry("0x0035", "fid-force-on/off-sensor"), // FID_PANEL_FORCE_ON_OFF_SENSOR
            Map.entry("0x0036", "fid-force-position-blind"), // FID_PANEL_FORCE_UP_DOWN_SENSOR
            Map.entry("0x0037", "fid-scene-sensor"), // FID_PANEL_SCENE_SENSOR
            Map.entry("0x0038", "fid-room-temperature-controller-extension-unit"), // FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE
            Map.entry("0x0039", "fid-fan-coil-sensor"), // FID_PANEL_FAN_COIL_SENSOR
            Map.entry("0x003A", "fid-rgb-+-warm-white/cold-white-sensor"), // FID_PANEL_RGB_CT_SENSOR
            Map.entry("0x003B", "fid-rgb-sensor"), // FID_PANEL_RGB_SENSOR
            Map.entry("0x003C", "fid-warm-white/cold-white-sensor"), // FID_PANEL_CT_SENSOR
            Map.entry("0x003D", "fid-add.-stage-for-heating-mode"), // FID_ADDITIONAL_HEATING_ACTUATOR
            Map.entry("0x003E", "fid-radiator-thermostate"), // FID_RADIATOR_ACTUATOR_MASTER
            Map.entry("0x003F", "fid-room-temperature-controller-extension-unit"), // FID_RADIATOR_ACTUATOR_SLAVE
            Map.entry("0x0040", "fid-color-temperature"), // FID_COLORTEMPERATURE_ACTUATOR
            Map.entry("0x0041", "fid-brightness-sensor"), // FID_BRIGHTNESS_SENSOR
            Map.entry("0x0042", "fid-rain-sensor"), // FID_RAIN_SENSOR
            Map.entry("0x0043", "fid-temperature-sensor"), // FID_TEMPERATURE_SENSOR
            Map.entry("0x0044", "fid-wind-sensor"), // FID_WIND_SENSOR
            Map.entry("0x0045", "fid-trigger"), // FID_TRIGGER
            Map.entry("0x0047", "fid-heating-mode"), // FID_FCA_2_PIPE_HEATING
            Map.entry("0x0048", "fid-cooling-mode"), // FID_FCA_2_PIPE_COOLING
            Map.entry("0x0049", "fid-auto.-heating/cooling-mode"), // FID_FCA_2_PIPE_HEATING_COOLING
            Map.entry("0x004A", "fid-two-valves-for-heating-and-cooling"), // FID_FCA_4_PIPE_HEATING_AND_COOLING
            Map.entry("0x004B", "fid-window/door"), // FID_WINDOW_DOOR_ACTUATOR
            Map.entry("0x004E", "fid-inverter-information"), // FID_INVERTER_INFO
            Map.entry("0x004F", "fid-meter-information"), // FID_METER_INFO
            Map.entry("0x0050", "fid-battery-information"), // FID_BATTERY_INFO
            Map.entry("0x0051", "fid-timer-program-switch-sensor"), // FID_PANEL_TIMER_PROGRAM_SWITCH_SENSOR
            Map.entry("0x0053", "fid-safety-sensor"), // FID_SAFETY_SENSOR
            Map.entry("0x0055", "fid-zone"), // FID_DOMUSTECH_ZONE
            Map.entry("0x0056", "fid-central-heating-actuator"), // FID_CENTRAL_HEATING_ACTUATOR
            Map.entry("0x0057", "fid-central-cooling-actuator"), // FID_CENTRAL_COOLING_ACTUATOR
            Map.entry("0x0058", "fid-link-actuator"), // FID_LINK_ACTUATOR
            Map.entry("0x0059", "fid-housekeeping"), // FID_HOUSE_KEEPING
            Map.entry("0x005A", "fid-media-player"), // FID_MEDIA_PLAYER
            Map.entry("0x005B", "fid-panel-room-temperature-controller-slave-for-battery-device"), // FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE
            Map.entry("0x005C", "fid-welcome-ip-mute-sensor"), // FID_WELCOME_IP_MUTE_SENSOR
            Map.entry("0x005D", "fid-welcome-ip-mute"), // FID_WELCOME_IP_MUTE_ACTUATOR
            Map.entry("0x005E", "fid-welcome-ip-doors-open-sensor"), // FID_WELCOME_IP_DOOR_OPEN_SENSOR
            Map.entry("0x005F", "fid-welcome-ip-switch"), // FID_WELCOME_IP_SWITCH_SENSOR
            Map.entry("0x0060", "fid-media-player-sensor"), // FID_PANEL_MEDIA_PLAYER_SENSOR
            Map.entry("0x0061", "fid-roller-blind-actuator"), // FID_BLIND_ACTUATOR
            Map.entry("0x0062", "fid-attic-window-actuator"), // FID_ATTIC_WINDOW_ACTUATOR
            Map.entry("0x0063", "fid-awning-actuator"), // FID_AWNING_ACTUATOR
            Map.entry("0x0064", "fid-windowdoor-position-sensor"), // FID_WINDOW_DOOR_POSITION_SENSOR
            Map.entry("0x0065", "fid-window/door-position"), // FID_WINDOW_DOOR_POSITION_ACTUATOR
            Map.entry("0x0066", "fid-media-playback-control-sensor"), // FID_MEDIA_PLAYBACK_CONTROL_SENSOR
            Map.entry("0x0067", "fid-media-volume-sensor"), // FID_MEDIA_VOLUME_SENSOR
            Map.entry("0x0068", "fid-dishwasher"), // FID_DISHWASHER
            Map.entry("0x0069", "fid-laundry"), // FID_LAUNDRY
            Map.entry("0x006A", "fid-dryer"), // FID_DRYER
            Map.entry("0x006B", "fid-oven"), // FID_OVEN
            Map.entry("0x006C", "fid-fridge"), // FID_FRIDGE
            Map.entry("0x006D", "fid-freezer"), // FID_FREEZER
            Map.entry("0x006E", "fid-hood"), // FID_HOOD
            Map.entry("0x006F", "fid-coffee-machine"), // FID_COFFEE_MACHINE
            Map.entry("0x0070", "fid-fridge/freezer"), // FID_FRIDGE_FREEZER
            Map.entry("0x0071", "fid-timer-program-switch-sensor"), // FID_TIMER_PROGRAM_OR_ALERT_SWITCH_SENSOR
            Map.entry("0x0072", "fid-welcome-ip-bell-indicator"), // FID_WELCOME_IP_BELL_INDICATOR_SENSOR
            Map.entry("0x0073", "fid-ceiling-fan-actuator"), // FID_CEILING_FAN_ACTUATOR
            Map.entry("0x0074", "fid-ceiling-fan-sensor"), // FID_CEILING_FAN_SENSOR
            Map.entry("0x0075", "fid-room-temperature-controller-with-fan-speed-level"), // FID_SPLIT_UNIT_GATEWAY
            Map.entry("0x0076", "fid-zone"), // FID_ZONE
            Map.entry("0x0077", "fid-safety"), // FID_24H_ZONE
            Map.entry("0x0078", "fid-external-ir-sensor-bx80"), // FID_EXTERNAL_IR_SENSOR_BX80
            Map.entry("0x0079", "fid-external-ir-sensor-vxi"), // FID_EXTERNAL_IR_SENSOR_VXI
            Map.entry("0x007A", "fid-external-ir-sensor-mini"), // FID_EXTERNAL_IR_SENSOR_MINI
            Map.entry("0x007B", "fid-external-ir-sensor-high-altitude"), // FID_EXTERNAL_IR_SENSOR_HIGH_ALTITUDE
            Map.entry("0x007C", "fid-external-ir-sensor-curtain"), // FID_EXTERNAL_IR_SENSOR_CURTAIN
            Map.entry("0x007D", "fid-smoke-detector"), // FID_SMOKE_DETECTOR
            Map.entry("0x007E", "fid-carbon-monoxide-sensor"), // FID_CARBON_MONOXIDE_SENSOR
            Map.entry("0x007F", "fid-methane-detector"), // FID_METHANE_DETECTOR
            Map.entry("0x0080", "fid-gas-sensor-lpg"), // FID_GAS_SENSOR_LPG
            Map.entry("0x0081", "fid-flood-detection"), // FID_FLOOD_DETECTION
            Map.entry("0x0082", "fid-secure@home-central-unit"), // FID_DOMUS_CENTRAL_UNIT_NEXTGEN
            Map.entry("0x0083", "fid-thermostat"), // FID_THERMOSTAT
            Map.entry("0x0084", "fid-secure@home-zone-sensor"), // FID_PANEL_DOMUS_ZONE_SENSOR
            Map.entry("0x0085", "fid-slave-thermostat"), // FID_THERMOSTAT_SLAVE
            Map.entry("0x0086", "fid-secure@home-integration-logic"), // FID_DOMUS_SECURE_INTEGRATION
            Map.entry("0x0087", "fid-add.-stage-for-cooling-mode"), // FID_ADDITIONAL_COOLING_ACTUATOR
            Map.entry("0x0088", "fid-two-level-heating-actuator"), // FID_TWO_LEVEL_HEATING_ACTUATOR
            Map.entry("0x0089", "fid-two-level-cooling-actuator"), // FID_TWO_LEVEL_COOLING_ACTUATOR
            Map.entry("0x008A", "fid-door-lock-sensor"), // FID_DOOR_LOCK_SENSOR
            Map.entry("0x008B", "fid-door-lock-actuator"), // FID_DOOR_LOCK_ACTUATOR
            Map.entry("0x008C", "fid-ac-routing"), // FID_AC_ROUTING
            Map.entry("0x008D", "fid-external-siren"), // FID_EXTERNAL_SIREN
            Map.entry("0x008E", "fid-zone"), // FID_GLOBAL_ZONE
            Map.entry("0x008F", "fid-volume-up"), // FID_VOLUME_UP_SENSOR
            Map.entry("0x0090", "fid-volume-down"), // FID_VOLUME_DOWN_SENSOR
            Map.entry("0x0091", "fid-play/pause"), // FID_PLAY_PAUSE_SENSOR
            Map.entry("0x0092", "fid-next-favorite"), // FID_NEXT_FAVORITE_SENSOR
            Map.entry("0x0093", "fid-next-song"), // FID_NEXT_SONG_SENSOR
            Map.entry("0x0094", "fid-previous-song"), // FID_PREVIOUS_SONG_SENSOR
            Map.entry("0x0095", "fid-home-appliance-sensor"), // FID_HOME_APPLIANCE_SENSOR
            Map.entry("0x0096", "fid-heat-sensor"), // FID_HEAT_SENSOR
            Map.entry("0x0097", "fid-zone-switching"), // FID_ZONE_SWITCHING
            Map.entry("0x0098", "fid-button-function"), // FID_SECURE_AT_HOME_FUNCTION
            Map.entry("0x0099", "fid-advanced-configuration"), // FID_COMPLEX_CONFIGURATION
            Map.entry("0x009A", "fid-secure@home-central-unit-basic"), // FID_DOMUS_CENTRAL_UNIT_BASIC
            Map.entry("0x009B", "fid-repeater"), // FID_DOMUS_REPEATER
            Map.entry("0x009C", "fid-remote-scene-control"), // FID_DOMUS_SCENE_TRIGGER
            Map.entry("0x009D", "fid-window-sensor"), // FID_DOMUSWINDOWCONTACT
            Map.entry("0x009E", "fid-movement-detector"), // FID_DOMUSMOVEMENTDETECTOR
            Map.entry("0x009F", "fid-external-ir-sensor-curtain"), // FID_DOMUSCURTAINDETECTOR
            Map.entry("0x00A0", "fid-smoke-detector"), // FID_DOMUSSMOKEDETECTOR
            Map.entry("0x00A1", "fid-flood-detection"), // FID_DOMUSFLOODDETECTOR
            Map.entry("0x00A2", "fid-hob"), // FID_HOB
            Map.entry("0x00A3", "fid-sensor-for-air-conditioning-unit"), // FID_PANEL_SUG_SENSOR
            Map.entry("0x00A4", "fid-two-point-controller-for-heating-or-cooling"), // FID_TWO_LEVEL_HEATING_COOLING_ACTUATOR
            Map.entry("0x00A5", "fid-slave-thermostat"), // FID_PANEL_THERMOSTAT_CONTROLLER_SLAVE
            Map.entry("0x00A6", "fid-wallbox"), // FID_WALLBOX
            Map.entry("0x00A7", "fid-wallbox"), // FID_PANEL_WALLBOX
            Map.entry("0x00A8", "fid-door-lock-control"), // FID_DOOR_LOCK_CONTROL
            Map.entry("0x00A9", "fid-door-lock-settings"), // FID_DOOR_LOCK_SETTINGS
            Map.entry("0x00AA", "fid-room-temperature-controller-with-fan-speed-level"), // FID_VRV_GATEWAY
            Map.entry("0x00BA", "fid-co2"), // FID_CO_2
            Map.entry("0x00BB", "fid-voc"), // FID_VOC
            Map.entry("0x00BD", "fid-airquality-sensor"), // FID_AIRQUALITY_SENSOR
            Map.entry("0x1008", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE0
            Map.entry("0x1009", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE1
            Map.entry("0x100A", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE2
            Map.entry("0x100B", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE3
            Map.entry("0x100C", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE4
            Map.entry("0x100D", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE5
            Map.entry("0x100E", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE6
            Map.entry("0x100F", "fid-switch-sensor-push-button"), // FID_SWITCH_SENSOR_PUSHBUTTON_TYPE7
            Map.entry("0x1018", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE0
            Map.entry("0x1019", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE1
            Map.entry("0x101A", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE2
            Map.entry("0x101B", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE3
            Map.entry("0x101C", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE4
            Map.entry("0x101D", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE5
            Map.entry("0x101E", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE6
            Map.entry("0x101F", "fid-dimming-sensor-push-button"), // FID_DIMMING_SENSOR_PUSHBUTTON_TYPE7
            Map.entry("0x1090", "fid-movement-detector-flex"), // FID_MOVEMENT_DETECTOR_FLEX
            Map.entry("0x1090", "fid-movement-detector"), // FID_MOVEMENT_DETECTOR_TYPE0
            Map.entry("0x1810", "fid-switch-actuator-flex"), // FID_DIMMING_ACTUATOR_FLEX
            Map.entry("0x1810", "fid-dim-actuator"), // FID_DIMMING_ACTUATOR_TYPE0
            Map.entry("0x1821", "fid-wireless-blind-actuator"), // FID_BLIND_ACTUATOR_WIRELESS
            Map.entry("0x4800", "fid-scene-trigger"), // FID_SCENE_TRIGGER
            Map.entry("0x4A00", "fid-rule-switch"), // FID_RULE_SWITCH
            Map.entry("0xB03F", "fid-air-quality-sensor-humidity"), // FID_AIRQUALITYSENSOR_HUMIDITY
            Map.entry("0xE017", "fid-air-quality-sensor-pressure"), // FID_AIRQUALITYSENSOR_PRESSURE
            Map.entry("0xE018", "fid-air-quality-sensor-co2"), // FID_AIRQUALITYSENSOR_CO2
            Map.entry("0xE019", "fid-air-quality-sensor-co"), // FID_AIRQUALITYSENSOR_CO
            Map.entry("0xE01A", "fid-air-quality-sensor-no2"), // FID_AIRQUALITYSENSOR_NO2
            Map.entry("0xE01B", "fid-air-quality-sensor-o3"), // FID_AIRQUALITYSENSOR_O3
            Map.entry("0xE01C", "fid-air-quality-sensor-pm10"), // FID_AIRQUALITYSENSOR_PM10
            Map.entry("0xE01D", "fid-air-quality-sensor-pm25"), // FID_AIRQUALITYSENSOR_PM25
            Map.entry("0xE01E", "fid-air-quality-sensor-voc") // FID_AIRQUALITYSENSOR_VOC

    );

    @Nullable
    public static String getFunctionIdText(String Key) throws FreeAtHomeGeneralException {
        String result = MAP_FUNCTION_ID.get(Key);

        if (result != null) {
            return MAP_FUNCTION_ID.get(Key);
        } else {
            throw new FreeAtHomeGeneralException(0,
                    String.format("%s - Key:%s", "FID is not in the translation table", Key));
        }
    }
}
