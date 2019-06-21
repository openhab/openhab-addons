/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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

    // SSE Event types
    public static final String EVENT_ELAPSED_PROGRAM_TIME = "BSH.Common.Option.ElapsedProgramTime";
    public static final String EVENT_OVEN_CAVITY_TEMPERATURE = "Cooking.Oven.Status.CurrentCavityTemperature";
    public static final String EVENT_POWER_STATE = "BSH.Common.Setting.PowerState";
    public static final String EVENT_CONNECTED = "CONNECTED";
    public static final String EVENT_DISCONNECTED = "DISCONNECTED";
    public static final String EVENT_DOOR_STATE = "BSH.Common.Status.DoorState";
    public static final String EVENT_OPERATION_STATE = "BSH.Common.Status.OperationState";
    public static final String EVENT_ACTIVE_PROGRAM = "BSH.Common.Root.ActiveProgram";
    public static final String EVENT_SELECTED_PROGRAM = "BSH.Common.Root.SelectedProgram";
    public static final String EVENT_REMOTE_CONTROL_START_ALLOWED = "BSH.Common.Status.RemoteControlStartAllowed";
    public static final String EVENT_REMOTE_CONTROL_ACTIVE = "BSH.Common.Status.RemoteControlActive";
    public static final String EVENT_LOCAL_CONTROL_ACTIVE = "BSH.Common.Status.LocalControlActive";
    public static final String EVENT_REMAINING_PROGRAM_TIME = "BSH.Common.Option.RemainingProgramTime";
    public static final String EVENT_PROGRAM_PROGRESS = "BSH.Common.Option.ProgramProgress";
    public static final String EVENT_SETPOINT_TEMPERATURE = "Cooking.Oven.Option.SetpointTemperature";
    public static final String EVENT_DURATION = "BSH.Common.Option.Duration";
    public static final String EVENT_WASHER_TEMPERATURE = "LaundryCare.Washer.Option.Temperature";
    public static final String EVENT_WASHER_SPIN_SPEED = "LaundryCare.Washer.Option.SpinSpeed";
    public static final String EVENT_WASHER_IDOS_1_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos1DosingLevel";
    public static final String EVENT_WASHER_IDOS_2_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos2DosingLevel";
    public static final String EVENT_FREEZER_SETPOINT_TEMPERATURE = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureFreezer";
    public static final String EVENT_FRIDGE_SETPOINT_TEMPERATURE = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureRefrigerator";
    public static final String EVENT_FREEZER_SUPER_MODE = "Refrigeration.FridgeFreezer.Setting.SuperModeFreezer";
    public static final String EVENT_FRIDGE_SUPER_MODE = "Refrigeration.FridgeFreezer.Setting.SuperModeRefrigerator";
    public static final String EVENT_DRYER_DRYING_TARGET = "LaundryCare.Dryer.Option.DryingTarget";
    public static final String EVENT_COFFEEMAKER_BEAN_CONTAINER_EMPTY = "ConsumerProducts.CoffeeMaker.Event.BeanContainerEmpty";
    public static final String EVENT_COFFEEMAKER_WATER_TANK_EMPTY = "ConsumerProducts.CoffeeMaker.Event.WaterTankEmpty";
    public static final String EVENT_COFFEEMAKER_DRIP_TRAY_FULL = "ConsumerProducts.CoffeeMaker.Event.DripTrayFull";
    public static final String EVENT_HOOD_VENTING_LEVEL = "Cooking.Common.Option.Hood.VentingLevel";
    public static final String EVENT_HOOD_INTENSIVE_LEVEL = "Cooking.Common.Option.Hood.IntensiveLevel";

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
    public static final String CHANNEL_WASHER_IDOS1 = "laundry_care_washer_idos1";
    public static final String CHANNEL_WASHER_IDOS2 = "laundry_care_washer_idos2";
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

    // List of all supported devices
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Stream.of(THING_TYPE_API_BRIDGE,
            THING_TYPE_DISHWASHER, THING_TYPE_OVEN, THING_TYPE_WASHER, THING_TYPE_DRYER, THING_TYPE_WASHER_DRYER,
            THING_TYPE_FRIDGE_FREEZER, THING_TYPE_COFFEE_MAKER, THING_TYPE_HOOD, THING_TYPE_COOKTOP)
            .collect(Collectors.toSet());

    // Discoverable devices
    public static final Set<ThingTypeUID> DISCOVERABLE_DEVICE_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_DISHWASHER, THING_TYPE_OVEN, THING_TYPE_WASHER, THING_TYPE_DRYER, THING_TYPE_WASHER_DRYER,
                    THING_TYPE_FRIDGE_FREEZER, THING_TYPE_COFFEE_MAKER, THING_TYPE_HOOD, THING_TYPE_COOKTOP)
            .collect(Collectors.toSet());

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
    public static final String OPTION_REMAINING_PROGRAM_TIME = "BSH.Common.Option.RemainingProgramTime";
    public static final String OPTION_PROGRAM_PROGRESS = "BSH.Common.Option.ProgramProgress";
    public static final String OPTION_ELAPSED_PROGRAM_TIME = "BSH.Common.Option.ElapsedProgramTime";
    public static final String OPTION_SETPOINT_TEMPERATURE = "Cooking.Oven.Option.SetpointTemperature";
    public static final String OPTION_DURATION = "BSH.Common.Option.Duration";
    public static final String OPTION_WASHER_TEMPERATURE = "LaundryCare.Washer.Option.Temperature";
    public static final String OPTION_WASHER_SPIN_SPEED = "LaundryCare.Washer.Option.SpinSpeed";
    public static final String OPTION_WASHER_IDOS_1_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos1DosingLevel";
    public static final String OPTION_WASHER_IDOS_2_DOSING_LEVEL = "LaundryCare.Washer.Option.IDos2DosingLevel";
    public static final String OPTION_DRYER_DRYING_TARGET = "LaundryCare.Dryer.Option.DryingTarget";
    public static final String OPTION_HOOD_VENTING_LEVEL = "Cooking.Common.Option.Hood.VentingLevel";
    public static final String OPTION_HOOD_INTENSIVE_LEVEL = "Cooking.Common.Option.Hood.IntensiveLevel";

    // Network and oAuth constants
    public static final String API_BASE_URL = "https://api.home-connect.com";
    public static final String API_SIMULATOR_BASE_URL = "https://simulator.home-connect.com";
    public static final String OAUTH_TOKEN_PATH = "/security/oauth/token";
    public static final String OAUTH_AUTHORIZE_PATH = "/security/oauth/authorize";
    public static final String OAUTH_SCOPE = "IdentifyAppliance Monitor Settings Dishwasher-Control Washer-Control Dryer-Control WasherDryer-Control CoffeeMaker-Control Hood-Control CleaningRobot-Control";

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

    // proxy settings
    public static final boolean HTTP_PROXY_ENABLED = false;
    public static final String HTTP_PROXY_HOST = "localhost";
    public static final int HTTP_PROXY_PORT = 8888;
}
