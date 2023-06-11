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
package org.openhab.binding.homeconnect.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link HomeConnectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectBindingConstants {

    public static final String BINDING_ID = "homeconnect";

    public static final String HA_ID = "haId";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_API_BRIDGE = new ThingTypeUID(BINDING_ID, "api_bridge");
    public static final ThingTypeUID THING_TYPE_DISHWASHER = new ThingTypeUID(BINDING_ID, "dishwasher");
    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, "oven");
    public static final ThingTypeUID THING_TYPE_WASHER = new ThingTypeUID(BINDING_ID, "washer");
    public static final ThingTypeUID THING_TYPE_WASHER_DRYER = new ThingTypeUID(BINDING_ID, "washerdryer");
    public static final ThingTypeUID THING_TYPE_FRIDGE_FREEZER = new ThingTypeUID(BINDING_ID, "fridgefreezer");
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID, "dryer");
    public static final ThingTypeUID THING_TYPE_COFFEE_MAKER = new ThingTypeUID(BINDING_ID, "coffeemaker");
    public static final ThingTypeUID THING_TYPE_HOOD = new ThingTypeUID(BINDING_ID, "hood");
    public static final ThingTypeUID THING_TYPE_COOKTOP = new ThingTypeUID(BINDING_ID, "hob");

    // Setting
    public static final String SETTING_POWER_STATE = "BSH.Common.Setting.PowerState";
    public static final String SETTING_LIGHTING = "Cooking.Common.Setting.Lighting";
    public static final String SETTING_AMBIENT_LIGHT_ENABLED = "BSH.Common.Setting.AmbientLightEnabled";
    public static final String SETTING_LIGHTING_BRIGHTNESS = "Cooking.Common.Setting.LightingBrightness";
    public static final String SETTING_AMBIENT_LIGHT_BRIGHTNESS = "BSH.Common.Setting.AmbientLightBrightness";
    public static final String SETTING_AMBIENT_LIGHT_COLOR = "BSH.Common.Setting.AmbientLightColor";
    public static final String SETTING_AMBIENT_LIGHT_CUSTOM_COLOR = "BSH.Common.Setting.AmbientLightCustomColor";
    public static final String SETTING_FREEZER_SETPOINT_TEMPERATURE = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureFreezer";
    public static final String SETTING_REFRIGERATOR_SETPOINT_TEMPERATURE = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureRefrigerator";
    public static final String SETTING_REFRIGERATOR_SUPER_MODE = "Refrigeration.FridgeFreezer.Setting.SuperModeRefrigerator";
    public static final String SETTING_FREEZER_SUPER_MODE = "Refrigeration.FridgeFreezer.Setting.SuperModeFreezer";

    // Status
    public static final String STATUS_DOOR_STATE = "BSH.Common.Status.DoorState";
    public static final String STATUS_OPERATION_STATE = "BSH.Common.Status.OperationState";
    public static final String STATUS_OVEN_CURRENT_CAVITY_TEMPERATURE = "Cooking.Oven.Status.CurrentCavityTemperature";
    public static final String STATUS_REMOTE_CONTROL_START_ALLOWED = "BSH.Common.Status.RemoteControlStartAllowed";
    public static final String STATUS_REMOTE_CONTROL_ACTIVE = "BSH.Common.Status.RemoteControlActive";
    public static final String STATUS_LOCAL_CONTROL_ACTIVE = "BSH.Common.Status.LocalControlActive";

    // SSE Event types
    public static final String EVENT_ELAPSED_PROGRAM_TIME = "BSH.Common.Option.ElapsedProgramTime";
    public static final String EVENT_OVEN_CAVITY_TEMPERATURE = STATUS_OVEN_CURRENT_CAVITY_TEMPERATURE;
    public static final String EVENT_POWER_STATE = SETTING_POWER_STATE;
    public static final String EVENT_CONNECTED = "CONNECTED";
    public static final String EVENT_DISCONNECTED = "DISCONNECTED";
    public static final String EVENT_DOOR_STATE = STATUS_DOOR_STATE;
    public static final String EVENT_OPERATION_STATE = STATUS_OPERATION_STATE;
    public static final String EVENT_ACTIVE_PROGRAM = "BSH.Common.Root.ActiveProgram";
    public static final String EVENT_SELECTED_PROGRAM = "BSH.Common.Root.SelectedProgram";
    public static final String EVENT_REMOTE_CONTROL_START_ALLOWED = STATUS_REMOTE_CONTROL_START_ALLOWED;
    public static final String EVENT_REMOTE_CONTROL_ACTIVE = STATUS_REMOTE_CONTROL_ACTIVE;
    public static final String EVENT_LOCAL_CONTROL_ACTIVE = STATUS_LOCAL_CONTROL_ACTIVE;
    public static final String EVENT_FINISH_IN_RELATIVE = "BSH.Common.Option.FinishInRelative";
    public static final String EVENT_REMAINING_PROGRAM_TIME = "BSH.Common.Option.RemainingProgramTime";
    public static final String EVENT_PROGRAM_PROGRESS = "BSH.Common.Option.ProgramProgress";
    public static final String EVENT_SETPOINT_TEMPERATURE = "Cooking.Oven.Option.SetpointTemperature";
    public static final String EVENT_DURATION = "BSH.Common.Option.Duration";
    public static final String EVENT_WASHER_TEMPERATURE = "LaundryCare.Washer.Option.Temperature";
    public static final String EVENT_WASHER_SPIN_SPEED = "LaundryCare.Washer.Option.SpinSpeed";
    public static final String EVENT_WASHER_IDOS_1_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos1DosingLevel";
    public static final String EVENT_WASHER_IDOS_2_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos2DosingLevel";
    public static final String EVENT_FREEZER_SETPOINT_TEMPERATURE = SETTING_FREEZER_SETPOINT_TEMPERATURE;
    public static final String EVENT_FRIDGE_SETPOINT_TEMPERATURE = SETTING_REFRIGERATOR_SETPOINT_TEMPERATURE;
    public static final String EVENT_FREEZER_SUPER_MODE = SETTING_FREEZER_SUPER_MODE;
    public static final String EVENT_FRIDGE_SUPER_MODE = SETTING_REFRIGERATOR_SUPER_MODE;
    public static final String EVENT_DRYER_DRYING_TARGET = "LaundryCare.Dryer.Option.DryingTarget";
    public static final String EVENT_COFFEEMAKER_BEAN_CONTAINER_EMPTY = "ConsumerProducts.CoffeeMaker.Event.BeanContainerEmpty";
    public static final String EVENT_COFFEEMAKER_WATER_TANK_EMPTY = "ConsumerProducts.CoffeeMaker.Event.WaterTankEmpty";
    public static final String EVENT_COFFEEMAKER_DRIP_TRAY_FULL = "ConsumerProducts.CoffeeMaker.Event.DripTrayFull";
    public static final String EVENT_HOOD_VENTING_LEVEL = "Cooking.Common.Option.Hood.VentingLevel";
    public static final String EVENT_HOOD_INTENSIVE_LEVEL = "Cooking.Common.Option.Hood.IntensiveLevel";
    public static final String EVENT_FUNCTIONAL_LIGHT_STATE = SETTING_LIGHTING;
    public static final String EVENT_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE = SETTING_LIGHTING_BRIGHTNESS;
    public static final String EVENT_AMBIENT_LIGHT_STATE = SETTING_AMBIENT_LIGHT_ENABLED;
    public static final String EVENT_AMBIENT_LIGHT_BRIGHTNESS_STATE = SETTING_AMBIENT_LIGHT_BRIGHTNESS;
    public static final String EVENT_AMBIENT_LIGHT_COLOR_STATE = SETTING_AMBIENT_LIGHT_COLOR;
    public static final String EVENT_AMBIENT_LIGHT_CUSTOM_COLOR_STATE = SETTING_AMBIENT_LIGHT_CUSTOM_COLOR;

    // Channel IDs
    public static final String CHANNEL_DOOR_STATE = "door_state";
    public static final String CHANNEL_ELAPSED_PROGRAM_TIME = "elapsed_program_time";
    public static final String CHANNEL_POWER_STATE = "power_state";
    public static final String CHANNEL_OPERATION_STATE = "operation_state";
    public static final String CHANNEL_ACTIVE_PROGRAM_STATE = "active_program_state";
    public static final String CHANNEL_SELECTED_PROGRAM_STATE = "selected_program_state";
    public static final String CHANNEL_BASIC_ACTIONS_STATE = "basic_actions_state";
    public static final String CHANNEL_REMOTE_START_ALLOWANCE_STATE = "remote_start_allowance_state";
    public static final String CHANNEL_REMOTE_CONTROL_ACTIVE_STATE = "remote_control_active_state";
    public static final String CHANNEL_LOCAL_CONTROL_ACTIVE_STATE = "local_control_active_state";
    public static final String CHANNEL_REMAINING_PROGRAM_TIME_STATE = "remaining_program_time_state";
    public static final String CHANNEL_PROGRAM_PROGRESS_STATE = "program_progress_state";
    public static final String CHANNEL_OVEN_CURRENT_CAVITY_TEMPERATURE = "oven_current_cavity_temperature";
    public static final String CHANNEL_SETPOINT_TEMPERATURE = "setpoint_temperature";
    public static final String CHANNEL_DURATION = "duration";
    public static final String CHANNEL_WASHER_TEMPERATURE = "laundry_care_washer_temperature";
    public static final String CHANNEL_WASHER_SPIN_SPEED = "laundry_care_washer_spin_speed";
    public static final String CHANNEL_WASHER_IDOS1_LEVEL = "laundry_care_washer_idos1_level";
    public static final String CHANNEL_WASHER_IDOS2_LEVEL = "laundry_care_washer_idos2_level";
    public static final String CHANNEL_WASHER_IDOS1 = "laundry_care_washer_idos1";
    public static final String CHANNEL_WASHER_IDOS2 = "laundry_care_washer_idos2";
    public static final String CHANNEL_WASHER_VARIO_PERFECT = "laundry_care_washer_vario_perfect";
    public static final String CHANNEL_WASHER_LESS_IRONING = "laundry_care_washer_less_ironing";
    public static final String CHANNEL_WASHER_PRE_WASH = "laundry_care_washer_pre_wash";
    public static final String CHANNEL_WASHER_RINSE_PLUS = "laundry_care_washer_rinse_plus";
    public static final String CHANNEL_WASHER_RINSE_HOLD = "laundry_care_washer_rinse_hold";
    public static final String CHANNEL_WASHER_SOAK = "laundry_care_washer_soak";
    public static final String CHANNEL_WASHER_LOAD_RECOMMENDATION = "laundry_care_washer_load_recommendation";
    public static final String CHANNEL_PROGRAM_ENERGY = "program_energy";
    public static final String CHANNEL_PROGRAM_WATER = "program_water";
    public static final String CHANNEL_REFRIGERATOR_SETPOINT_TEMPERATURE = "setpoint_temperature_refrigerator";
    public static final String CHANNEL_REFRIGERATOR_SUPER_MODE = "super_mode_refrigerator";
    public static final String CHANNEL_FREEZER_SETPOINT_TEMPERATURE = "setpoint_temperature_freezer";
    public static final String CHANNEL_FREEZER_SUPER_MODE = "super_mode_freezer";
    public static final String CHANNEL_DRYER_DRYING_TARGET = "dryer_drying_target";
    public static final String CHANNEL_COFFEEMAKER_DRIP_TRAY_FULL_STATE = "coffeemaker_drip_tray_full_state";
    public static final String CHANNEL_COFFEEMAKER_WATER_TANK_EMPTY_STATE = "coffeemaker_water_tank_empty_state";
    public static final String CHANNEL_COFFEEMAKER_BEAN_CONTAINER_EMPTY_STATE = "coffeemaker_bean_container_empty_state";
    public static final String CHANNEL_HOOD_VENTING_LEVEL = "hood_venting_level";
    public static final String CHANNEL_HOOD_INTENSIVE_LEVEL = "hood_intensive_level";
    public static final String CHANNEL_HOOD_ACTIONS_STATE = "hood_program_state";
    public static final String CHANNEL_FUNCTIONAL_LIGHT_STATE = "functional_light_state";
    public static final String CHANNEL_FUNCTIONAL_LIGHT_BRIGHTNESS_STATE = "functional_light_brightness_state";
    public static final String CHANNEL_AMBIENT_LIGHT_STATE = "ambient_light_state";
    public static final String CHANNEL_AMBIENT_LIGHT_BRIGHTNESS_STATE = "ambient_light_brightness_state";
    public static final String CHANNEL_AMBIENT_LIGHT_COLOR_STATE = "ambient_light_color_state";
    public static final String CHANNEL_AMBIENT_LIGHT_CUSTOM_COLOR_STATE = "ambient_light_custom_color_state";

    // List of all supported devices
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_API_BRIDGE,
            THING_TYPE_DISHWASHER, THING_TYPE_OVEN, THING_TYPE_WASHER, THING_TYPE_DRYER, THING_TYPE_WASHER_DRYER,
            THING_TYPE_FRIDGE_FREEZER, THING_TYPE_COFFEE_MAKER, THING_TYPE_HOOD, THING_TYPE_COOKTOP);

    // Discoverable devices
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_THING_TYPES_UIDS = Set.of(THING_TYPE_DISHWASHER,
            THING_TYPE_OVEN, THING_TYPE_WASHER, THING_TYPE_DRYER, THING_TYPE_WASHER_DRYER, THING_TYPE_FRIDGE_FREEZER,
            THING_TYPE_COFFEE_MAKER, THING_TYPE_HOOD, THING_TYPE_COOKTOP);

    // List of state values
    public static final String STATE_POWER_OFF = "BSH.Common.EnumType.PowerState.Off";
    public static final String STATE_POWER_ON = "BSH.Common.EnumType.PowerState.On";
    public static final String STATE_POWER_STANDBY = "BSH.Common.EnumType.PowerState.Standby";
    public static final String STATE_DOOR_OPEN = "BSH.Common.EnumType.DoorState.Open";
    public static final String STATE_DOOR_LOCKED = "BSH.Common.EnumType.DoorState.Locked";
    public static final String STATE_DOOR_CLOSED = "BSH.Common.EnumType.DoorState.Closed";
    public static final String STATE_OPERATION_READY = "BSH.Common.EnumType.OperationState.Ready";
    public static final String STATE_OPERATION_FINISHED = "BSH.Common.EnumType.OperationState.Finished";
    public static final String STATE_OPERATION_RUN = "BSH.Common.EnumType.OperationState.Run";
    public static final String STATE_EVENT_PRESENT_STATE_OFF = "BSH.Common.EnumType.EventPresentState.Off";

    // List of program options
    public static final String OPTION_FINISH_IN_RELATIVE = "BSH.Common.Option.FinishInRelative";
    public static final String OPTION_REMAINING_PROGRAM_TIME = "BSH.Common.Option.RemainingProgramTime";
    public static final String OPTION_PROGRAM_PROGRESS = "BSH.Common.Option.ProgramProgress";
    public static final String OPTION_ELAPSED_PROGRAM_TIME = "BSH.Common.Option.ElapsedProgramTime";
    public static final String OPTION_SETPOINT_TEMPERATURE = "Cooking.Oven.Option.SetpointTemperature";
    public static final String OPTION_DURATION = "BSH.Common.Option.Duration";
    public static final String OPTION_WASHER_TEMPERATURE = "LaundryCare.Washer.Option.Temperature";
    public static final String OPTION_WASHER_SPIN_SPEED = "LaundryCare.Washer.Option.SpinSpeed";
    public static final String OPTION_WASHER_IDOS_1_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos1DosingLevel";
    public static final String OPTION_WASHER_IDOS_2_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos2DosingLevel";
    public static final String OPTION_WASHER_IDOS_1_ACTIVE = "LaundryCare.Washer.Option.IDos1.Active";
    public static final String OPTION_WASHER_IDOS_2_ACTIVE = "LaundryCare.Washer.Option.IDos2.Active";
    public static final String OPTION_WASHER_VARIO_PERFECT = "LaundryCare.Common.Option.VarioPerfect";
    public static final String OPTION_WASHER_LESS_IRONING = "LaundryCare.Washer.Option.LessIroning";
    public static final String OPTION_WASHER_PRE_WASH = "LaundryCare.Washer.Option.Prewash";
    public static final String OPTION_WASHER_RINSE_PLUS = "LaundryCare.Washer.Option.RinsePlus";
    public static final String OPTION_WASHER_RINSE_HOLD = "LaundryCare.Washer.Option.RinseHold";
    public static final String OPTION_WASHER_SOAK = "LaundryCare.Washer.Option.Soak";
    public static final String OPTION_WASHER_LOAD_RECOMMENDATION = "LaundryCare.Common.Option.LoadRecommendation";
    public static final String OPTION_WASHER_ENERGY_FORECAST = "BSH.Common.Option.EnergyForecast";
    public static final String OPTION_WASHER_WATER_FORECAST = "BSH.Common.Option.WaterForecast";
    public static final String OPTION_DRYER_DRYING_TARGET = "LaundryCare.Dryer.Option.DryingTarget";
    public static final String OPTION_HOOD_VENTING_LEVEL = "Cooking.Common.Option.Hood.VentingLevel";
    public static final String OPTION_HOOD_INTENSIVE_LEVEL = "Cooking.Common.Option.Hood.IntensiveLevel";

    // List of washer temperatures
    public static final String TEMPERATURE_PREFIX = "LaundryCare.Washer.EnumType.Temperature.";
    public static final String TEMPERATURE_AUTO = TEMPERATURE_PREFIX + "Auto";
    public static final String TEMPERATURE_COLD = TEMPERATURE_PREFIX + "Cold";
    public static final String TEMPERATURE_20 = TEMPERATURE_PREFIX + "GC20";
    public static final String TEMPERATURE_30 = TEMPERATURE_PREFIX + "GC30";
    public static final String TEMPERATURE_40 = TEMPERATURE_PREFIX + "GC40";
    public static final String TEMPERATURE_60 = TEMPERATURE_PREFIX + "GC60";
    public static final String TEMPERATURE_90 = TEMPERATURE_PREFIX + "GC90";

    // List of spin speeds
    public static final String SPIN_SPEED_PREFIX = "LaundryCare.Washer.EnumType.SpinSpeed.";
    public static final String SPIN_SPEED_AUTO = SPIN_SPEED_PREFIX + "Auto";
    public static final String SPIN_SPEED_OFF = SPIN_SPEED_PREFIX + "Off";
    public static final String SPIN_SPEED_400 = SPIN_SPEED_PREFIX + "RPM400";
    public static final String SPIN_SPEED_600 = SPIN_SPEED_PREFIX + "RPM600";
    public static final String SPIN_SPEED_800 = SPIN_SPEED_PREFIX + "RPM800";
    public static final String SPIN_SPEED_1200 = SPIN_SPEED_PREFIX + "RPM1200";
    public static final String SPIN_SPEED_1400 = SPIN_SPEED_PREFIX + "RPM1400";

    // List of stages
    public static final String STAGE_FAN_OFF = "Cooking.Hood.EnumType.Stage.FanOff";
    public static final String STAGE_FAN_STAGE_01 = "Cooking.Hood.EnumType.Stage.FanStage01";
    public static final String STAGE_FAN_STAGE_02 = "Cooking.Hood.EnumType.Stage.FanStage02";
    public static final String STAGE_FAN_STAGE_03 = "Cooking.Hood.EnumType.Stage.FanStage03";
    public static final String STAGE_FAN_STAGE_04 = "Cooking.Hood.EnumType.Stage.FanStage04";
    public static final String STAGE_FAN_STAGE_05 = "Cooking.Hood.EnumType.Stage.FanStage05";
    public static final String STAGE_INTENSIVE_STAGE_OFF = "Cooking.Hood.EnumType.IntensiveStage.IntensiveStageOff";
    public static final String STAGE_INTENSIVE_STAGE_1 = "Cooking.Hood.EnumType.IntensiveStage.IntensiveStage1";
    public static final String STAGE_INTENSIVE_STAGE_2 = "Cooking.Hood.EnumType.IntensiveStage.IntensiveStage2";
    public static final String STATE_AMBIENT_LIGHT_COLOR_CUSTOM_COLOR = "BSH.Common.EnumType.AmbientLightColor.CustomColor";

    // List of programs
    public static final String PROGRAM_HOOD_AUTOMATIC = "Cooking.Common.Program.Hood.Automatic";
    public static final String PROGRAM_HOOD_VENTING = "Cooking.Common.Program.Hood.Venting";
    public static final String PROGRAM_HOOD_DELAYED_SHUT_OFF = "Cooking.Common.Program.Hood.DelayedShutOff";

    // Network and oAuth constants
    public static final String API_BASE_URL = "https://api.home-connect.com";
    public static final String API_SIMULATOR_BASE_URL = "https://simulator.home-connect.com";
    public static final String OAUTH_TOKEN_PATH = "/security/oauth/token";
    public static final String OAUTH_AUTHORIZE_PATH = "/security/oauth/authorize";
    public static final String OAUTH_SCOPE = "IdentifyAppliance Monitor Settings Dishwasher-Control Washer-Control Dryer-Control WasherDryer-Control CoffeeMaker-Control Hood-Control Oven-Control CleaningRobot-Control";

    // Operation states
    public static final String OPERATION_STATE_INACTIVE = "BSH.Common.EnumType.OperationState.Inactive";
    public static final String OPERATION_STATE_READY = "BSH.Common.EnumType.OperationState.Ready";
    public static final String OPERATION_STATE_DELAYED_START = "BSH.Common.EnumType.OperationState.DelayedStart";
    public static final String OPERATION_STATE_RUN = "BSH.Common.EnumType.OperationState.Run";
    public static final String OPERATION_STATE_PAUSE = "BSH.Common.EnumType.OperationState.Pause";
    public static final String OPERATION_STATE_ACTION_REQUIRED = "BSH.Common.EnumType.OperationState.ActionRequired";
    public static final String OPERATION_STATE_FINISHED = "BSH.Common.EnumType.OperationState.Finished";
    public static final String OPERATION_STATE_ERROR = "BSH.Common.EnumType.OperationState.Error";
    public static final String OPERATION_STATE_ABORTING = "BSH.Common.EnumType.OperationState.Aborting";

    // Commands
    public static final String COMMAND_START = "start";
    public static final String COMMAND_STOP = "stop";
    public static final String COMMAND_SELECTED = "selected";
    public static final String COMMAND_VENTING_1 = "venting1";
    public static final String COMMAND_VENTING_2 = "venting2";
    public static final String COMMAND_VENTING_3 = "venting3";
    public static final String COMMAND_VENTING_4 = "venting4";
    public static final String COMMAND_VENTING_5 = "venting5";
    public static final String COMMAND_VENTING_INTENSIVE_1 = "ventingIntensive1";
    public static final String COMMAND_VENTING_INTENSIVE_2 = "ventingIntensive2";
    public static final String COMMAND_AUTOMATIC = "automatic";
    public static final String COMMAND_DELAYED_SHUT_OFF = "delayed";

    // light
    public static final int BRIGHTNESS_MIN = 10;
    public static final int BRIGHTNESS_MAX = 100;
    public static final int BRIGHTNESS_DIM_STEP = 10;
}
