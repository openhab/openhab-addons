/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
    public static final int FID_DES_LEVEL_CALL_ACTUATOR = 0x001D; // Door Map.entry System Call Level Actuator
    public static final int FID_DES_LEVEL_CALL_SENSOR = 0x001E; // Door Map.entry System Call Level Sensor
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

    // FID added based on tests
    public static final int FID_AIRQUALITYSENSOR_PRESSURE = 0x0E017;
    public static final int FID_AIRQUALITYSENSOR_CO2 = 0x0E018;
    public static final int FID_AIRQUALITYSENSOR_CO = 0x0E019;
    public static final int FID_AIRQUALITYSENSOR_NO2 = 0x0E01A;
    public static final int FID_AIRQUALITYSENSOR_O3 = 0x0E01B;
    public static final int FID_AIRQUALITYSENSOR_PM10 = 0x0E01C;
    public static final int FID_AIRQUALITYSENSOR_PM25 = 0x0E01D;
    public static final int FID_AIRQUALITYSENSOR_VOC = 0x0E01E;
    public static final int FID_AIRQUALITYSENSOR_HUMIDITY = 0x0B03F;

    public static final int FID_MOVEMENT_DETECTOR_FLEX = 0x1090;
    public static final int FID_DIMMING_ACTUATOR_FLEX = 0x1810;

    private static final Map<String, String> mapFunctionId = Map.ofEntries(Map.entry("0x0000", "@text/fid-control-element"), // FID_SWITCH_SENSOR
            Map.entry("0x0001", "@text/fid-dimming-sensor"), // FID_DIMMING_SENSOR
            Map.entry("0x0003", "@text/fid-blind-sensor"), // FID_BLIND_SENSOR
            Map.entry("0x0004", "@text/fid-stairwell-light-sensor"), // FID_STAIRCASE_LIGHT_SENSOR
            Map.entry("0x0005", "@text/fid-force-on/off-sensor"), // FID_FORCE_ON_OFF_SENSOR
            Map.entry("0x0006", "@text/fid-scene-sensor"), // FID_SCENE_SENSOR
            Map.entry("0x0007", "@text/fid-switch-actuator"), // FID_SWITCH_ACTUATOR
            Map.entry("0x0009", "@text/fid-blind-actuator"), // FID_SHUTTER_ACTUATOR
            Map.entry("0x000A", "@text/fid-room-temperature-controller-with-fan-speed-level"), // FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITH_FAN
            Map.entry("0x000B", "@text/fid-room-temperature-controller-extension-unit"), // FID_ROOM_TEMPERATURE_CONTROLLER_SLAVE
            Map.entry("0x000C", "@text/fid-wind-alarm"), // FID_WIND_ALARM_SENSOR
            Map.entry("0x000D", "@text/fid-frost-alarm"), // FID_FROST_ALARM_SENSOR
            Map.entry("0x000E", "@text/fid-rain-alarm"), // FID_RAIN_ALARM_SENSOR
            Map.entry("0x000F", "@text/fid-window-sensor"), // FID_WINDOW_DOOR_SENSOR
            Map.entry("0x0011", "@text/fid-movement-detector"), // FID_MOVEMENT_DETECTOR
            Map.entry("0x0012", "@text/fid-dim-actuator"), // FID_DIMMING_ACTUATOR
            Map.entry("0x0014", "@text/fid-radiator"), // FID_RADIATOR_ACTUATOR
            Map.entry("0x0015", "@text/fid-underfloor-heating"), // FID_UNDERFLOOR_HEATING
            Map.entry("0x0016", "@text/fid-fan-coil"), // FID_FAN_COIL
            Map.entry("0x0017", "@text/fid-two-level-controller"), // FID_TWO_LEVEL_CONTROLLER
            Map.entry("0x001A", "@text/fid-door-opener"), // FID_DES_DOOR_OPENER_ACTUATOR
            Map.entry("0x001B", "@text/fid-proxy"), // FID_PROXY
            Map.entry("0x001D", "@text/fid-door-map.entry-system-call-level-actuator"), // FID_DES_LEVEL_CALL_ACTUATOR
            Map.entry("0x001E", "@text/fid-door-map.entry-system-call-level-sensor"), // FID_DES_LEVEL_CALL_SENSOR
            Map.entry("0x001F", "@text/fid-door-call"), // FID_DES_DOOR_RINGING_SENSOR
            Map.entry("0x0020", "@text/fid-automatic-door-opener"), // FID_DES_AUTOMATIC_DOOR_OPENER_ACTUATOR
            Map.entry("0x0021", "@text/fid-corridor-light"), // FID_DES_LIGHT_SWITCH_ACTUATOR
            Map.entry("0x0023", "@text/fid-room-temperature-controller"), // FID_ROOM_TEMPERATURE_CONTROLLER_MASTER_WITHOUT_FAN
            Map.entry("0x0024", "@text/fid-cooling-mode"), // FID_COOLING_ACTUATOR
            Map.entry("0x0027", "@text/fid-heating-mode"), // FID_HEATING_ACTUATOR
            Map.entry("0x0028", "@text/fid-force-position-blind"), // FID_FORCE_UP_DOWN_SENSOR
            Map.entry("0x0029", "@text/fid-auto.-heating/cooling-mode"), // FID_HEATING_COOLING_ACTUATOR
            Map.entry("0x002A", "@text/fid-switchover-heating/cooling"), // FID_HEATING_COOLING_SENSOR
            Map.entry("0x002B", "@text/fid-device-settings"), // FID_DES_DEVICE_SETTINGS
            Map.entry("0x002E", "@text/fid-dim-actuator"), // FID_RGB_W_ACTUATOR
            Map.entry("0x002F", "@text/fid-dim-actuator"), // FID_RGB_ACTUATOR
            Map.entry("0x0030", "@text/fid-control-element"), // FID_PANEL_SWITCH_SENSOR
            Map.entry("0x0031", "@text/fid-dimming-sensor"), // FID_PANEL_DIMMING_SENSOR
            Map.entry("0x0033", "@text/fid-blind-sensor"), // FID_PANEL_BLIND_SENSOR
            Map.entry("0x0034", "@text/fid-stairwell-light-sensor"), // FID_PANEL_STAIRCASE_LIGHT_SENSOR
            Map.entry("0x0035", "@text/fid-force-on/off-sensor"), // FID_PANEL_FORCE_ON_OFF_SENSOR
            Map.entry("0x0036", "@text/fid-force-position-blind"), // FID_PANEL_FORCE_UP_DOWN_SENSOR
            Map.entry("0x0037", "@text/fid-scene-sensor"), // FID_PANEL_SCENE_SENSOR
            Map.entry("0x0038", "@text/fid-room-temperature-controller-extension-unit"), // FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE
            Map.entry("0x0039", "@text/fid-fan-coil-sensor"), // FID_PANEL_FAN_COIL_SENSOR
            Map.entry("0x003A", "@text/fid-rgb-+-warm-white/cold-white-sensor"), // FID_PANEL_RGB_CT_SENSOR
            Map.entry("0x003B", "@text/fid-rgb-sensor"), // FID_PANEL_RGB_SENSOR
            Map.entry("0x003C", "@text/fid-warm-white/cold-white-sensor"), // FID_PANEL_CT_SENSOR
            Map.entry("0x003D", "@text/fid-add.-stage-for-heating-mode"), // FID_ADDITIONAL_HEATING_ACTUATOR
            Map.entry("0x003E", "@text/fid-radiator-thermostate"), // FID_RADIATOR_ACTUATOR_MASTER
            Map.entry("0x003F", "@text/fid-room-temperature-controller-extension-unit"), // FID_RADIATOR_ACTUATOR_SLAVE
            Map.entry("0x0041", "@text/fid-brightness-sensor"), // FID_BRIGHTNESS_SENSOR
            Map.entry("0x0042", "@text/fid-rain-sensor"), // FID_RAIN_SENSOR
            Map.entry("0x0043", "@text/fid-temperature-sensor"), // FID_TEMPERATURE_SENSOR
            Map.entry("0x0044", "@text/fid-wind-sensor"), // FID_WIND_SENSOR
            Map.entry("0x0045", "@text/fid-trigger"), // FID_TRIGGER
            Map.entry("0x0047", "@text/fid-heating-mode"), // FID_FCA_2_PIPE_HEATING
            Map.entry("0x0048", "@text/fid-cooling-mode"), // FID_FCA_2_PIPE_COOLING
            Map.entry("0x0049", "@text/fid-auto.-heating/cooling-mode"), // FID_FCA_2_PIPE_HEATING_COOLING
            Map.entry("0x004A", "@text/fid-two-valves-for-heating-and-cooling"), // FID_FCA_4_PIPE_HEATING_AND_COOLING
            Map.entry("0x004B", "@text/fid-window/door"), // FID_WINDOW_DOOR_ACTUATOR
            Map.entry("0x004E", "@text/fid-abc"), // FID_INVERTER_INFO
            Map.entry("0x004F", "@text/fid-abd"), // FID_METER_INFO
            Map.entry("0x0050", "@text/fid-acd"), // FID_BATTERY_INFO
            Map.entry("0x0051", "@text/fid-timer-program-switch-sensor"), // FID_PANEL_TIMER_PROGRAM_SWITCH_SENSOR
            Map.entry("0x0055", "@text/fid-zone"), // FID_DOMUSTECH_ZONE
            Map.entry("0x0056", "@text/fid-central-heating-actuator"), // FID_CENTRAL_HEATING_ACTUATOR
            Map.entry("0x0057", "@text/fid-central-cooling-actuator"), // FID_CENTRAL_COOLING_ACTUATOR
            Map.entry("0x0059", "@text/fid-housekeeping"), // FID_HOUSE_KEEPING
            Map.entry("0x005A", "@text/fid-media-player"), // FID_MEDIA_PLAYER
            Map.entry("0x005B", "@text/fid-panel-room-temperature-controller-slave-for-battery-device"), // FID_PANEL_ROOM_TEMPERATURE_CONTROLLER_SLAVE_FOR_BATTERY_DEVICE
            Map.entry("0x0060", "@text/fid-media-player-sensor"), // FID_PANEL_MEDIA_PLAYER_SENSOR
            Map.entry("0x0061", "@text/fid-roller-blind-actuator"), // FID_BLIND_ACTUATOR
            Map.entry("0x0062", "@text/fid-attic-window-actuator"), // FID_ATTIC_WINDOW_ACTUATOR
            Map.entry("0x0063", "@text/fid-awning-actuator"), // FID_AWNING_ACTUATOR
            Map.entry("0x0064", "@text/fid-windowdoor-position-sensor"), // FID_WINDOW_DOOR_POSITION_SENSOR
            Map.entry("0x0065", "@text/fid-window/door-position"), // FID_WINDOW_DOOR_POSITION_ACTUATOR
            Map.entry("0x0066", "@text/fid-media-playback-control-sensor"), // FID_MEDIA_PLAYBACK_CONTROL_SENSOR
            Map.entry("0x0067", "@text/fid-media-volume-sensor"), // FID_MEDIA_VOLUME_SENSOR
            Map.entry("0x0068", "@text/fid-dishwasher"), // FID_DISHWASHER
            Map.entry("0x0069", "@text/fid-laundry"), // FID_LAUNDRY
            Map.entry("0x006A", "@text/fid-dryer"), // FID_DRYER
            Map.entry("0x006B", "@text/fid-oven"), // FID_OVEN
            Map.entry("0x006C", "@text/fid-fridge"), // FID_FRIDGE
            Map.entry("0x006D", "@text/fid-freezer"), // FID_FREEZER
            Map.entry("0x006E", "@text/fid-hood"), // FID_HOOD
            Map.entry("0x006F", "@text/fid-coffee-machine"), // FID_COFFEE_MACHINE
            Map.entry("0x0070", "@text/fid-fridge/freezer"), // FID_FRIDGE_FREEZER
            Map.entry("0x0071", "@text/fid-timer-program-switch-sensor"), // FID_TIMER_PROGRAM_OR_ALERT_SWITCH_SENSOR
            Map.entry("0x0073", "@text/fid-ceiling-fan-actuator"), // FID_CEILING_FAN_ACTUATOR
            Map.entry("0x0074", "@text/fid-ceiling-fan-sensor"), // FID_CEILING_FAN_SENSOR
            Map.entry("0x0075", "@text/fid-room-temperature-controller-with-fan-speed-level"), // FID_SPLIT_UNIT_GATEWAY
            Map.entry("0x0076", "@text/fid-zone"), // FID_ZONE
            Map.entry("0x0077", "@text/fid-safety"), // FID_24H_ZONE
            Map.entry("0x0078", "@text/fid-external-ir-sensor-bx80"), // FID_EXTERNAL_IR_SENSOR_BX80
            Map.entry("0x0079", "@text/fid-external-ir-sensor-vxi"), // FID_EXTERNAL_IR_SENSOR_VXI
            Map.entry("0x007A", "@text/fid-external-ir-sensor-mini"), // FID_EXTERNAL_IR_SENSOR_MINI
            Map.entry("0x007B", "@text/fid-external-ir-sensor-high-altitude"), // FID_EXTERNAL_IR_SENSOR_HIGH_ALTITUDE
            Map.entry("0x007C", "@text/fid-external-ir-sensor-curtain"), // FID_EXTERNAL_IR_SENSOR_CURTAIN
            Map.entry("0x007D", "@text/fid-smoke-detector"), // FID_SMOKE_DETECTOR
            Map.entry("0x007E", "@text/fid-carbon-monoxide-sensor"), // FID_CARBON_MONOXIDE_SENSOR
            Map.entry("0x007F", "@text/fid-methane-detector"), // FID_METHANE_DETECTOR
            Map.entry("0x0080", "@text/fid-gas-sensor-lpg"), // FID_GAS_SENSOR_LPG
            Map.entry("0x0081", "@text/fid-flood-detection"), // FID_FLOOD_DETECTION
            Map.entry("0x0082", "@text/fid-secure@home-central-unit"), // FID_DOMUS_CENTRAL_UNIT_NEXTGEN
            Map.entry("0x0083", "@text/fid-thermostat"), // FID_THERMOSTAT
            Map.entry("0x0084", "@text/fid-secure@home-zone-sensor"), // FID_PANEL_DOMUS_ZONE_SENSOR
            Map.entry("0x0085", "@text/fid-slave-thermostat"), // FID_THERMOSTAT_SLAVE
            Map.entry("0x0086", "@text/fid-secure@home-integration-logic"), // FID_DOMUS_SECURE_INTEGRATION
            Map.entry("0x0087", "@text/fid-add.-stage-for-cooling-mode"), // FID_ADDITIONAL_COOLING_ACTUATOR
            Map.entry("0x0088", "@text/fid-two-level-heating-actuator"), // FID_TWO_LEVEL_HEATING_ACTUATOR
            Map.entry("0x0089", "@text/fid-two-level-cooling-actuator"), // FID_TWO_LEVEL_COOLING_ACTUATOR
            Map.entry("0x008E", "@text/fid-zone"), // FID_GLOBAL_ZONE
            Map.entry("0x008F", "@text/fid-volume-up"), // FID_VOLUME_UP_SENSOR
            Map.entry("0x0090", "@text/fid-volume-down"), // FID_VOLUME_DOWN_SENSOR
            Map.entry("0x0091", "@text/fid-play/pause"), // FID_PLAY_PAUSE_SENSOR
            Map.entry("0x0092", "@text/fid-next-favorite"), // FID_NEXT_FAVORITE_SENSOR
            Map.entry("0x0093", "@text/fid-next-song"), // FID_NEXT_SONG_SENSOR
            Map.entry("0x0094", "@text/fid-previous-song"), // FID_PREVIOUS_SONG_SENSOR
            Map.entry("0x0095", "@text/fid-home-appliance-sensor"), // FID_HOME_APPLIANCE_SENSOR
            Map.entry("0x0096", "@text/fid-heat-sensor"), // FID_HEAT_SENSOR
            Map.entry("0x0097", "@text/fid-zone-switching"), // FID_ZONE_SWITCHING
            Map.entry("0x0098", "@text/fid-button-function"), // FID_SECURE_AT_HOME_FUNCTION
            Map.entry("0x0099", "@text/fid-advanced-configuration"), // FID_COMPLEX_CONFIGURATION
            Map.entry("0x009A", "@text/fid-secure@home-central-unit-basic"), // FID_DOMUS_CENTRAL_UNIT_BASIC
            Map.entry("0x009B", "@text/fid-repeater"), // FID_DOMUS_REPEATER
            Map.entry("0x009C", "@text/fid-remote-scene-control"), // FID_DOMUS_SCENE_TRIGGER
            Map.entry("0x009D", "@text/fid-window-sensor"), // FID_DOMUSWINDOWCONTACT
            Map.entry("0x009E", "@text/fid-movement-detector"), // FID_DOMUSMOVEMENTDETECTOR
            Map.entry("0x009F", "@text/fid-external-ir-sensor-curtain"), // FID_DOMUSCURTAINDETECTOR
            Map.entry("0x00A0", "@text/fid-smoke-detector"), // FID_DOMUSSMOKEDETECTOR
            Map.entry("0x00A1", "@text/fid-flood-detection"), // FID_DOMUSFLOODDETECTOR
            Map.entry("0x00A3", "@text/fid-sensor-for-air-conditioning-unit"), // FID_PANEL_SUG_SENSOR
            Map.entry("0x00A4", "@text/fid-two-point-controller-for-heating-or-cooling"), // FID_TWO_LEVEL_HEATING_COOLING_ACTUATOR
            Map.entry("0x00A5", "@text/fid-slave-thermostat"), // FID_PANEL_THERMOSTAT_CONTROLLER_SLAVE
            Map.entry("0x00A6", "@text/fid-wallbox"), // FID_WALLBOX
            Map.entry("0x00A7", "@text/fid-wallbox"), // FID_PANEL_WALLBOX
            Map.entry("0x00A8", "@text/fid-door-lock-control"), // FID_DOOR_LOCK_CONTROL
            Map.entry("0x00AA", "@text/fid-room-temperature-controller-with-fan-speed-level"), // FID_VRV_GATEWAY
            Map.entry("0x4800", "@text/fid-scene-trigger"), // FID_SCENE_TRIGGER
            Map.entry("0x4A00", "@text/fid-rule-switch"), // FID_RULE_SWITCH
            Map.entry("0xE017", "@text/fid-air-quality-sensor-pressure"), // FID_AIRQUALITYSENSOR_PRESSURE
            Map.entry("0xE018", "@text/fid-air-quality-sensor-co2"), // FID_AIRQUALITYSENSOR_CO2
            Map.entry("0xE019", "@text/fid-air-quality-sensor-co"), // FID_AIRQUALITYSENSOR_CO
            Map.entry("0xE01A", "@text/fid-air-quality-sensor-no2"), // FID_AIRQUALITYSENSOR_NO2
            Map.entry("0xE01B", "@text/fid-air-quality-sensor-o3"), // FID_AIRQUALITYSENSOR_O3
            Map.entry("0xE01C", "@text/fid-air-quality-sensor-pm10"), // FID_AIRQUALITYSENSOR_PM10
            Map.entry("0xE01D", "@text/fid-air-quality-sensor-pm25"), // FID_AIRQUALITYSENSOR_PM25
            Map.entry("0xE01E", "@text/fid-air-quality-sensor-voc"), // FID_AIRQUALITYSENSOR_VOC
            Map.entry("0xB03F", "@text/fid-air-quality-sensor-humidity"), // FID_AIRQUALITYSENSOR_HUMIDITY
            Map.entry("0x1090", "@text/fid-movement-detector-flex"), // FID_MOVEMENT_DETECTOR_FLEX
            Map.entry("0x1810", "@text/fid-dim-actuator-flex") // FID_SWITCH_ACTUATOR_FLEX
    );

    @Nullable
    public static String getFunctionIdText(String Key) {
        return mapFunctionId.get(Key);
    }
}
