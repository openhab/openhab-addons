/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homeconnectdirect.internal;

import static org.openhab.binding.homeconnectdirect.internal.common.utils.StringUtils.SLASH;

import java.io.File;
import java.time.Duration;
import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * The {@link HomeConnectDirectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class HomeConnectDirectBindingConstants {

    private HomeConnectDirectBindingConstants() {
        // prevent instantiation
    }

    public static final String BINDING_ID = "homeconnectdirect";
    public static final String HOME_APPLIANCE_ID = "haId";
    public static final String CONFIGURATION_PID = "binding.homeconnectdirect";
    public static final ZoneId ZONE_ID = ZoneId.systemDefault();
    public static final Locale LOCALE = Locale.ENGLISH;

    // List of all appliances
    public static final String APPLIANCE_TYPE_GENERIC = "generic";
    public static final String APPLIANCE_TYPE_WASHER = "washer";
    public static final String APPLIANCE_TYPE_WASHER_AND_DRYER = "washerdryer";
    public static final String APPLIANCE_TYPE_DRYER = "dryer";
    public static final String APPLIANCE_TYPE_DISHWASHER = "dishwasher";
    public static final String APPLIANCE_TYPE_COOK_PROCESSOR = "cookprocessor";
    public static final String APPLIANCE_TYPE_COFFEE_MAKER = "coffeemaker";
    public static final String APPLIANCE_TYPE_OVEN = "oven";
    public static final String APPLIANCE_TYPE_WARMING_DRAWER = "warmingdrawer";
    public static final String APPLIANCE_TYPE_HOOD = "hood";
    public static final String APPLIANCE_TYPE_COOKTOP = "cooktop";
    public static final String APPLIANCE_TYPE_COOKTOP_ALTERNATIVE = "hob";
    public static final String APPLIANCE_TYPE_FRIDGE_FREEZER = "fridgefreezer";

    // List of all thing type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_GENERIC);
    public static final ThingTypeUID THING_TYPE_DISHWASHER = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_DISHWASHER);
    public static final ThingTypeUID THING_TYPE_WASHER = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_WASHER);
    public static final ThingTypeUID THING_TYPE_WASHER_DRYER = new ThingTypeUID(BINDING_ID,
            APPLIANCE_TYPE_WASHER_AND_DRYER);
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_DRYER);
    public static final ThingTypeUID THING_TYPE_COFFEE_MAKER = new ThingTypeUID(BINDING_ID,
            APPLIANCE_TYPE_COFFEE_MAKER);
    public static final ThingTypeUID THING_TYPE_COOK_PROCESSOR = new ThingTypeUID(BINDING_ID,
            APPLIANCE_TYPE_COOK_PROCESSOR);
    public static final ThingTypeUID THING_TYPE_OVEN = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_OVEN);
    public static final ThingTypeUID THING_TYPE_WARMING_DRAWER = new ThingTypeUID(BINDING_ID,
            APPLIANCE_TYPE_WARMING_DRAWER);
    public static final ThingTypeUID THING_TYPE_HOOD = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_HOOD);
    public static final ThingTypeUID THING_TYPE_COOKTOP = new ThingTypeUID(BINDING_ID, APPLIANCE_TYPE_COOKTOP);
    public static final ThingTypeUID THING_TYPE_FRIDGE_FREEZER = new ThingTypeUID(BINDING_ID,
            APPLIANCE_TYPE_FRIDGE_FREEZER);

    // List of all channel type UIDs
    public static final ChannelTypeUID CHANNEL_TYPE_SWITCH_VALUE = new ChannelTypeUID(BINDING_ID, "switch");
    public static final ChannelTypeUID CHANNEL_TYPE_STRING_VALUE = new ChannelTypeUID(BINDING_ID, "string");
    public static final ChannelTypeUID CHANNEL_TYPE_NUMBER_VALUE = new ChannelTypeUID(BINDING_ID, "number");
    public static final ChannelTypeUID CHANNEL_TYPE_TRIGGER_VALUE = new ChannelTypeUID(BINDING_ID, "trigger");
    public static final ChannelTypeUID CHANNEL_TYPE_ENUM_SWITCH_VALUE = new ChannelTypeUID(BINDING_ID, "enum-switch");
    public static final ChannelTypeUID CHANNEL_TYPE_SWITCH_DESCRIPTION = new ChannelTypeUID(BINDING_ID,
            "device-description-switch");
    public static final ChannelTypeUID CHANNEL_TYPE_STRING_DESCRIPTION = new ChannelTypeUID(BINDING_ID,
            "device-description-string");
    public static final ChannelTypeUID CHANNEL_TYPE_NUMBER_DESCRIPTION = new ChannelTypeUID(BINDING_ID,
            "device-description-number");

    // Supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_GENERIC, THING_TYPE_DISHWASHER,
            THING_TYPE_WASHER, THING_TYPE_WASHER_DRYER, THING_TYPE_DRYER, THING_TYPE_COFFEE_MAKER,
            THING_TYPE_COOK_PROCESSOR, THING_TYPE_OVEN, THING_TYPE_WARMING_DRAWER, THING_TYPE_HOOD, THING_TYPE_COOKTOP,
            THING_TYPE_FRIDGE_FREEZER);

    // Configuration properties
    public static final String PROPERTY_HOME_APPLIANCE_ID = HOME_APPLIANCE_ID;
    public static final String PROPERTY_ADDRESS = "address";

    // Configuration property values
    public static final int CONNECTION_TYPE_AES_PORT = 80;
    public static final int CONNECTION_TYPE_TLS_PORT = 443;

    // Misc
    public static final String USER_DATA_PATH = OpenHAB.getUserDataFolder();
    public static final String BINDING_PROFILES_PATH = USER_DATA_PATH + File.separator + BINDING_ID + File.separator
            + "profiles";
    public static final String BINDING_LOGS_PATH = USER_DATA_PATH + File.separator + BINDING_ID + File.separator
            + "logs";
    public static final String CONSCRYPT_PROVIDER = "Conscrypt";
    public static final String CONSCRYPT_REQUIRED_GLIBC_MIN_VERSION = "2.35";
    public static final String CONSCRYPT_SUPPORTED_SYSTEMS = "Linux (X64, ARM64), Windows (X64), Mac OS (X64, ARM64)";
    public static final String CONFIGURATION_VALUE_KEY = "valueKey";
    public static final String CONFIGURATION_UNIT_KEY = "unit";
    public static final String CONFIGURATION_ON_VALUE_KEY = "onValue";
    public static final String CONFIGURATION_DESCRIPTION_KEY = "descriptionKey";
    public static final String CONFIGURATION_ATTRIBUTE = "attribute";
    public static final Duration UPDATE_ALL_MANDATORY_VALUES_INTERVAL = Duration.ofMinutes(60);

    // Home Connect WebSocket Client
    public static final String WS_DEVICE_TYPE_APPLICATION_V2 = "Application";
    public static final Integer WS_DEVICE_TYPE_APPLICATION_V1 = 2;
    public static final String WS_DEVICE_NAME = "HC Direct";
    public static final String WS_AES_URI_TEMPLATE = "ws://%s:80/homeconnect";
    public static final String WS_TLS_URI_TEMPLATE = "wss://%s:443/homeconnect";
    public static final Duration WS_INACTIVITY_TIMEOUT = Duration.ofSeconds(60);
    public static final Duration WS_INACTIVITY_CHECK_INTERVAL = Duration.ofSeconds(10);
    public static final Duration WS_INACTIVITY_CHECK_INITIAL_DELAY = Duration.ofSeconds(5);
    public static final Duration WS_PING_INTERVAL = Duration.ofSeconds(30);
    public static final Duration WS_PING_INITIAL_DELAY = Duration.ofSeconds(5);

    // Servlet
    public static final String SERVLET_BASE_PATH = SLASH + BINDING_ID;
    public static final String SERVLET_ASSETS_PATH = SERVLET_BASE_PATH + "/assets";
    public static final String SERVLET_WEB_SOCKET_PATH = SERVLET_BASE_PATH + "/ws";
    public static final String SERVLET_WEB_SOCKET_PATTERN = SERVLET_WEB_SOCKET_PATH + "/*";

    // Appliance Keys
    public static final String POWER_STATE_KEY = "BSH.Common.Setting.PowerState";
    public static final String ROOT_OPTION_LIST_KEY = "BSH.Common.Root.OptionList";
    public static final String LAUNDRY_CARE_OPTION_LIST_KEY = "LaundryCare.Common.OptionList.Options";
    public static final String OPERATION_STATE_KEY = "BSH.Common.Status.OperationState";
    public static final String POWER_STATE_ENUM_KEY = "BSH.Common.EnumType.PowerState";
    public static final String ACTIVE_PROGRAM_KEY = "BSH.Common.Root.ActiveProgram";
    public static final String SELECTED_PROGRAM_KEY = "BSH.Common.Root.SelectedProgram";
    public static final String PAUSE_PROGRAM_KEY = "BSH.Common.Command.PauseProgram";
    public static final String RESUME_PROGRAM_KEY = "BSH.Common.Command.ResumeProgram";
    public static final String ABORT_PROGRAM_KEY = "BSH.Common.Command.AbortProgram";
    public static final String WASHER_I_DOS_1_ACTIVE_KEY = "LaundryCare.Washer.Option.IDos1.Active";
    public static final String WASHER_I_DOS_2_ACTIVE_KEY = "LaundryCare.Washer.Option.IDos2.Active";
    public static final String WASHER_I_DOS_1_FILL_LEVEL_POOR_KEY = "LaundryCare.Washer.Event.IDos1FillLevelPoor";
    public static final String WASHER_I_DOS_2_FILL_LEVEL_POOR_KEY = "LaundryCare.Washer.Event.IDos2FillLevelPoor";
    public static final String WASHER_TEMPERATURE_KEY = "LaundryCare.Washer.Option.Temperature";
    public static final String WASHER_SPIN_SPEED_KEY = "LaundryCare.Washer.Option.SpinSpeed";
    public static final String WASHER_SPEED_PERFECT_KEY = "LaundryCare.Washer.Option.SpeedPerfect";
    public static final String WASHER_WATER_PLUS_KEY = "LaundryCare.Washer.Option.WaterPlus";
    public static final String WASHER_PREWASH_KEY = "LaundryCare.Washer.Option.Prewash";
    public static final String WASHER_RINSE_HOLD_KEY = "LaundryCare.Washer.Option.RinseHold";
    public static final String WASHER_LESS_IRONING_KEY = "LaundryCare.Washer.Option.LessIroning";
    public static final String WASHER_SILENT_WASH_KEY = "LaundryCare.Washer.Option.SilentWash";
    public static final String WASHER_SOAK_KEY = "LaundryCare.Washer.Option.Soak";
    public static final String WASHER_RINSE_PLUS_KEY = "LaundryCare.Washer.Option.RinsePlus";
    public static final String WASHER_STAINS_KEY = "LaundryCare.Washer.Option.Stains";
    public static final String LAUNDRY_CARE_PROCESS_PHASE_KEY = "LaundryCare.Common.Option.ProcessPhase";
    public static final String LAUNDRY_LOAD_INFORMATION_KEY = "LaundryCare.Common.Status.LoadInformation";
    public static final String LAUNDRY_LOAD_RECOMMENDATION_KEY = "LaundryCare.Common.Option.LoadRecommendation";
    public static final String LAUNDRY_DRUM_CLEAN_REMINDER_KEY = "LaundryCare.Washer.Event.DrumCleanReminder";
    public static final String DRYER_PROCESS_PHASE_KEY = "LaundryCare.Dryer.Option.ProcessPhase";
    public static final String DRYER_WRINKLE_GUARD_KEY = "LaundryCare.Dryer.Option.WrinkleGuard";
    public static final String DRYER_DRYING_TARGET_KEY = "LaundryCare.Dryer.Option.DryingTarget";
    public static final String COOKING_LIGHTING_KEY = "Cooking.Common.Setting.Lighting";
    public static final String CHILD_LOCK_KEY = "BSH.Common.Setting.ChildLock";
    public static final String COOKING_LIGHTING_BRIGHTNESS_KEY = "Cooking.Common.Setting.LightingBrightness";
    public static final String COOKING_BUTTON_TONES_KEY = "Cooking.Common.Setting.ButtonTones";
    public static final String HOOD_VENTING_LEVEL_KEY = "Cooking.Common.Option.Hood.VentingLevel";
    public static final String HOOD_VENTING_LEVEL_ENUM_KEY = "Cooking.Hood.EnumType.Stage";
    public static final String HOOD_INTENSIVE_LEVEL_KEY = "Cooking.Common.Option.Hood.IntensiveLevel";
    public static final String HOOD_INTENSIVE_LEVEL_ENUM_KEY = "Cooking.Hood.EnumType.IntensiveStage";
    public static final String PROGRAM_PROGRESS_KEY = "BSH.Common.Option.ProgramProgress";
    public static final String REMAINING_PROGRAM_TIME_KEY = "BSH.Common.Option.RemainingProgramTime";
    public static final String REMOTE_CONTROL_START_ALLOWED_KEY = "BSH.Common.Status.RemoteControlStartAllowed";
    public static final String DOOR_STATE_KEY = "BSH.Common.Status.DoorState";
    public static final String DISHWASHER_SALT_LACK_KEY = "Dishcare.Dishwasher.Event.SaltLack";
    public static final String DISHWASHER_RINSE_AID_LACK_KEY = "Dishcare.Dishwasher.Event.RinseAidLack";
    public static final String DISHWASHER_SALT_NEARLY_EMPTY_KEY = "Dishcare.Dishwasher.Event.SaltNearlyEmpty";
    public static final String DISHWASHER_RINSE_AID_NEARLY_EMPTY_KEY = "Dishcare.Dishwasher.Event.RinseAidNearlyEmpty";
    public static final String DISHWASHER_MACHINE_CARE_REMINDER_KEY = "Dishcare.Dishwasher.Event.MachineCareReminder";
    public static final String DISHWASHER_PROGRAM_PHASE_KEY = "Dishcare.Dishwasher.Status.ProgramPhase";
    public static final String DISHWASHER_VARIO_SPEED_PLUS_KEY = "Dishcare.Dishwasher.Option.VarioSpeedPlus";
    public static final String DISHWASHER_INTENSIV_ZONE_KEY = "Dishcare.Dishwasher.Option.IntensivZone";
    public static final String DISHWASHER_BRILLIANCE_DRY_KEY = "Dishcare.Dishwasher.Option.BrillianceDry";
    public static final String COFFEE_MAKER_PROCESS_PHASE_KEY = "ConsumerProducts.CoffeeMaker.Status.ProcessPhase";
    public static final String COFFEE_MAKER_COUNTDOWN_CLEANING_KEY = "ConsumerProducts.CoffeeMaker.Status.BeverageCountdownCleaning";
    public static final String COFFEE_MAKER_COUNTDOWN_CALC_AND_CLEAN_KEY = "ConsumerProducts.CoffeeMaker.Status.BeverageCountdownCalcNClean";
    public static final String COFFEE_MAKER_COUNTDOWN_DESCALING_KEY = "ConsumerProducts.CoffeeMaker.Status.BeverageCountdownDescaling";
    public static final String COFFEE_MAKER_COUNTDOWN_WATER_FILTER_KEY = "ConsumerProducts.CoffeeMaker.Status.BeverageCountdownWaterfilter";
    public static final String COFFEE_MAKER_WATER_TANK_EMPTY_KEY = "ConsumerProducts.CoffeeMaker.Event.WaterTankEmpty";
    public static final String COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY_KEY = "ConsumerProducts.CoffeeMaker.Event.WaterTankNearlyEmpty";
    public static final String COFFEE_MAKER_DRIP_TRAY_FULL_KEY = "ConsumerProducts.CoffeeMaker.Event.DripTrayFull";
    public static final String COFFEE_MAKER_EMPTY_MILK_TANK_KEY = "ConsumerProducts.CoffeeMaker.Event.EmptyMilkTank";
    public static final String COFFEE_MAKER_BEAN_CONTAINER_EMPTY_KEY = "ConsumerProducts.CoffeeMaker.Event.BeanContainerEmpty";
    public static final String OVEN_DURATION_KEY = "BSH.Common.Option.Duration";
    public static final String OVEN_SET_POINT_TEMPERATURE_KEY = "Cooking.Oven.Option.SetpointTemperature";
    public static final String OVEN_CURRENT_TEMPERATURE_KEY = "Cooking.Oven.Status.CurrentCavityTemperature";
    public static final String OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_KEY = "Cooking.Oven.Status.CurrentMeatprobeTemperature";
    public static final String OVEN_MEAT_PROBE_PLUGGED_KEY = "Cooking.Oven.Status.MeatprobePlugged";
    public static final String OVEN_CAVITY_SELECTOR_ENUM_KEY = "Cooking.Oven.EnumType.CavitySelector";
    public static final String FRIDGE_DOOR_STATE_KEY = "Refrigeration.FridgeFreezer.Status.DoorRefrigerator";
    public static final String FRIDGE_DOOR_STATE_2_KEY = "Refrigeration.Common.Status.Door.Refrigerator";
    public static final String FRIDGE_CHILLER_DOOR_STATE_KEY = "Refrigeration.Common.Status.Door.ChillerCommon";
    public static final String FRIDGE_SET_POINT_TEMPERATURE_KEY = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureRefrigerator";
    public static final String FRIDGE_SET_POINT_TEMPERATURE_2_KEY = "Refrigeration.Common.Setting.Refrigerator.SetpointTemperature";
    public static final String FRIDGE_CHILLER_SET_POINT_TEMPERATURE_KEY = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureChiller";
    public static final String FRIDGE_CHILLER_SET_POINT_TEMPERATURE_2_KEY = "Refrigeration.Common.Setting.ChillerCommon.SetpointTemperature";
    public static final String FRIDGE_CHILLER_PRESET_KEY = "Refrigeration.Common.Setting.ChillerCommon.Preset";
    public static final String FRIDGE_SUPER_MODE_KEY = "Refrigeration.FridgeFreezer.Setting.SuperModeRefrigerator";
    public static final String FRIDGE_SUPER_MODE_2_KEY = "Refrigeration.Common.Setting.Refrigerator.SuperMode";
    public static final String FRIDGE_DISPENSER_WATER_FILTER_SATURATION_KEY = "Refrigeration.Common.Status.Dispenser.WaterFilterSaturation";
    public static final String FRIDGE_DISPENSER_PARTY_MODE_KEY = "Refrigeration.Common.Setting.Dispenser.PartyMode";
    public static final String FRIDGE_DISPENSER_ENABLED_KEY = "Refrigeration.Common.Setting.Dispenser.Enabled";
    public static final String FREEZER_SET_POINT_TEMPERATURE_KEY = "Refrigeration.FridgeFreezer.Setting.SetpointTemperatureFreezer";
    public static final String FREEZER_SET_POINT_TEMPERATURE_2_KEY = "Refrigeration.Common.Setting.Freezer.SetpointTemperature";
    public static final String FREEZER_DOOR_STATE_KEY = "Refrigeration.FridgeFreezer.Status.DoorFreezer";
    public static final String FREEZER_DOOR_STATE_2_KEY = "Refrigeration.Common.Status.Door.Freezer";
    public static final String FREEZER_SUPER_MODE_KEY = "Refrigeration.FridgeFreezer.Setting.SuperModeFreezer";
    public static final String FREEZER_SUPER_MODE_2_KEY = "Refrigeration.Common.Setting.Freezer.SuperMode";

    // Appliance Template Keys
    public static final String OVEN_DOOR_STATE_KEY_TEMPLATE = "Cooking.Oven.Status.Cavity.%03d.DoorState";
    public static final String OVEN_CURRENT_TEMPERATURE_KEY_TEMPLATE = "Cooking.Oven.Status.Cavity.%03d.CurrentTemperature";
    public static final String OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_KEY_TEMPLATE = "Cooking.Oven.Status.Cavity.%03d.CurrentMeatprobeTemperature";
    public static final String OVEN_CAVITY_LIGHT_KEY_TEMPLATE = "Cooking.Oven.Setting.Light.Cavity.%03d.Power";
    public static final String OVEN_MEAT_PROBE_PLUGGED_KEY_TEMPLATE = "Cooking.Oven.Status.Cavity.%03d.MeatprobePlugged";
    public static final String PROGRAM_FAVORITE_KEY_TEMPLATE = "BSH.Common.Program.Favorite.%03d";

    // Channels
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_REMOTE_CONTROL_OR_START_ALLOWED = "remote-control-start-allowed";
    public static final String CHANNEL_POWER_STATE = "power-state";
    public static final String CHANNEL_DOOR = "door";
    public static final String CHANNEL_OPERATION_STATE = "operation-state";
    public static final String CHANNEL_ACTIVE_PROGRAM = "active-program";
    public static final String CHANNEL_SELECTED_PROGRAM = "selected-program";
    public static final String CHANNEL_REMAINING_PROGRAM_TIME = "remaining-program-time";
    public static final String CHANNEL_PROGRAM_PROGRESS = "program-progress";
    public static final String CHANNEL_PROGRAM_COMMAND = "program-command";
    public static final String CHANNEL_WASHER_I_DOS_1_FILL_LEVEL_POOR = "idos1-fill-level-poor";
    public static final String CHANNEL_WASHER_I_DOS_2_FILL_LEVEL_POOR = "idos2-fill-level-poor";
    public static final String CHANNEL_WASHER_I_DOS_1_ACTIVE = "idos1-active";
    public static final String CHANNEL_WASHER_I_DOS_2_ACTIVE = "idos2-active";
    public static final String CHANNEL_WASHER_TEMPERATURE = "washer-temperature";
    public static final String CHANNEL_WASHER_SPIN_SPEED = "washer-spin-speed";
    public static final String CHANNEL_WASHER_SPEED_PERFECT = "washer-speed-perfect";
    public static final String CHANNEL_WASHER_WATER_PLUS = "washer-water-plus";
    public static final String CHANNEL_WASHER_PREWASH = "washer-prewash";
    public static final String CHANNEL_WASHER_RINSE_HOLD = "washer-rinse-hold";
    public static final String CHANNEL_WASHER_LESS_IRONING = "washer-less-ironing";
    public static final String CHANNEL_WASHER_SILENT_WASH = "washer-silent-wash";
    public static final String CHANNEL_WASHER_SOAK = "washer-soak";
    public static final String CHANNEL_WASHER_RINSE_PLUS = "washer-rinse-plus";
    public static final String CHANNEL_WASHER_STAINS = "washer-stains";
    public static final String CHANNEL_DRYER_DRYING_TARGET = "drying-target";
    public static final String CHANNEL_DRYER_WRINKLE_GUARD = "wrinkle-guard";
    public static final String CHANNEL_LAUNDRY_LOAD_INFORMATION = "laundry-load-information";
    public static final String CHANNEL_LAUNDRY_LOAD_RECOMMENDATION = "laundry-load-recommendation";
    public static final String CHANNEL_LAUNDRY_DRUM_CLEAN_REMINDER = "drum-clean-reminder";
    public static final String CHANNEL_LAUNDRY_CARE_PROCESS_PHASE = "process-phase";
    public static final String CHANNEL_DISHWASHER_SALT_LACK = "salt-lack";
    public static final String CHANNEL_DISHWASHER_RINSE_AID_LACK = "rinse-aid-lack";
    public static final String CHANNEL_DISHWASHER_SALT_NEARLY_EMPTY = "salt-nearly-empty";
    public static final String CHANNEL_DISHWASHER_RINSE_AID_NEARLY_EMPTY = "rinse-aid-nearly-empty";
    public static final String CHANNEL_DISHWASHER_MACHINE_CARE_REMINDER = "machine-care-reminder";
    public static final String CHANNEL_DISHWASHER_PROGRAM_PHASE = "program-phase";
    public static final String CHANNEL_DISHWASHER_VARIO_SPEED_PLUS = "dishwasher-vario-speed-plus";
    public static final String CHANNEL_DISHWASHER_INTENSIV_ZONE = "dishwasher-intensiv-zone";
    public static final String CHANNEL_DISHWASHER_BRILLIANCE_DRY = "dishwasher-brilliance-dry";
    public static final String CHANNEL_COFFEE_MAKER_COUNTDOWN_CLEANING = "cleaning";
    public static final String CHANNEL_COFFEE_MAKER_COUNTDOWN_CALC_AND_CLEAN = "calc-n-clean";
    public static final String CHANNEL_COFFEE_MAKER_COUNTDOWN_DESCALING = "descaling";
    public static final String CHANNEL_COFFEE_MAKER_COUNTDOWN_WATER_FILTER = "water-filter";
    public static final String CHANNEL_COFFEE_MAKER_WATER_TANK_EMPTY = "water-tank-empty";
    public static final String CHANNEL_COFFEE_MAKER_WATER_TANK_NEARLY_EMPTY = "water-tank-nearly-empty";
    public static final String CHANNEL_COFFEE_MAKER_DRIP_TRAY_FULL = "drip-tray-full";
    public static final String CHANNEL_COFFEE_MAKER_EMPTY_MILK_TANK = "empty-milk-tank";
    public static final String CHANNEL_COFFEE_MAKER_BEAN_CONTAINER_EMPTY = "bean-container-empty";
    public static final String CHANNEL_COFFEE_MAKER_PROCESS_PHASE = "process-phase";
    public static final String CHANNEL_COFFEE_MAKER_PROGRAM_COMMAND = "coffeemaker-program-command";
    public static final String CHANNEL_OVEN_DURATION = "duration";
    public static final String CHANNEL_OVEN_SET_POINT_TEMPERATURE = "setpoint-temperature";
    public static final String CHANNEL_OVEN_PROGRAM_COMMAND = "oven-program-command";
    public static final String CHANNEL_COOKING_LIGHT = "cooking-light";
    public static final String CHANNEL_COOKING_LIGHT_BRIGHTNESS = "cooking-light-brightness";
    public static final String CHANNEL_BUTTON_TONES = "button-tones";
    public static final String CHANNEL_HOOD_VENTING_LEVEL = "venting-level";
    public static final String CHANNEL_HOOD_INTENSIVE_LEVEL = "intensive-level";
    public static final String CHANNEL_CHILD_LOCK = "child-lock";
    public static final String CHANNEL_FRIDGE_DOOR_STATE = "refrigerator-door";
    public static final String CHANNEL_FREEZER_DOOR_STATE = "freezer-door";
    public static final String CHANNEL_FRIDGE_CHILLER_DOOR_STATE = "chiller-door";
    public static final String CHANNEL_FRIDGE_DISPENSER_WATER_FILTER_SATURATION = "dispenser-water-filter-saturation";
    public static final String CHANNEL_FRIDGE_DISPENSER_ENABLED = "dispenser-enabled";
    public static final String CHANNEL_FRIDGE_DISPENSER_PARTY_MODE = "dispenser-party-mode";
    public static final String CHANNEL_FRIDGE_SET_POINT_TEMPERATURE = "setpoint-temperature-refrigerator";
    public static final String CHANNEL_FREEZER_SET_POINT_TEMPERATURE = "setpoint-temperature-freezer";
    public static final String CHANNEL_FRIDGE_CHILLER_SET_POINT_TEMPERATURE = "setpoint-temperature-chiller";
    public static final String CHANNEL_FRIDGE_CHILLER_PRESET = "chiller-preset";
    public static final String CHANNEL_FRIDGE_SUPER_MODE = "super-mode-refrigerator";
    public static final String CHANNEL_FREEZER_SUPER_MODE = "super-mode-freezer";
    public static final String CHANNEL_RAW_MESSAGE = "raw-message";

    // Channel Templates
    public static final String CHANNEL_OVEN_DOOR_TEMPLATE = "door-%d";
    public static final String CHANNEL_OVEN_TEMPERATURE_TEMPLATE = "temperature-%d";
    public static final String CHANNEL_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE_TEMPLATE = "meat-probe-temperature-%d";
    public static final String CHANNEL_OVEN_MEAT_PROBE_PLUGGED_TEMPLATE = "meat-probe-plugged-%d";
    public static final String CHANNEL_OVEN_CAVITY_LIGHT_TEMPLATE = "cavity-light-%d";

    // Channel Types
    public static final String CHANNEL_TYPE_I_DOS_FILL_LEVEL_POOR = "washer-idos-fill-level-poor-channel";
    public static final String CHANNEL_TYPE_I_DOS_ACTIVE = "washer-idos-active-channel";
    public static final String CHANNEL_TYPE_DOOR = "door-channel";
    public static final String CHANNEL_TYPE_OVEN_CURRENT_TEMPERATURE = "oven-current-cavity-temperature-channel";
    public static final String CHANNEL_TYPE_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE = "oven-current-meat-probe-temperature-channel";
    public static final String CHANNEL_TYPE_OVEN_MEAT_PROBE_PLUGGED = "oven-meat-probe-plugged-channel";
    public static final String CHANNEL_TYPE_OVEN_CAVITY_LIGHT = "oven-cavity-light-channel";
    public static final String CHANNEL_TYPE_FRIDGE_FREEZER_SET_POINT_TEMPERATURE = "fridgefreezer-setpoint-temperature-channel";
    public static final String CHANNEL_TYPE_FRIDGE_FREEZER_SUPER_MODE = "fridgefreezer-super-mode-channel";
    public static final String CHANNEL_TYPE_FRIDGE_DISPENSER_WATER_FILTER_SATURATION = "fridgefreezer-dispenser-water-filter-saturation-channel";
    public static final String CHANNEL_TYPE_FRIDGE_DISPENSER_ENABLED = "fridgefreezer-dispenser-enabled-channel";
    public static final String CHANNEL_TYPE_FRIDGE_DISPENSER_PARTY_MODE = "fridgefreezer-dispenser-party-mode-channel";
    public static final String CHANNEL_TYPE_FRIDGE_CHILLER_PRESET = "fridgefreezer-chiller-preset-channel";

    // State values
    public static final String STATE_OPEN = "Open";
    public static final String STATE_AJAR = "Ajar";
    public static final String STATE_ON = "On";
    public static final String STATE_OFF = "Off";
    public static final String STATE_MAINS_OFF = "MainsOff";
    public static final String STATE_STANDBY = "Standby";
    public static final String STATE_FINISHED = "Finished";
    public static final String STATE_RUN = "Run";
    public static final String STATE_NO_PROGRAM = "0";
    public static final String STATE_HOOD_VENTING = "Cooking.Common.Program.Hood.Venting";
    public static final String STATE_PRESENT = "Present";
    public static final String STATE_CONFIRMED = "Confirmed";
    public static final String STATE_FAN_OFF = "FanOff";
    public static final String STATE_FAN_STAGE_1 = "FanStage01";
    public static final String STATE_FAN_STAGE_2 = "FanStage02";
    public static final String STATE_FAN_STAGE_3 = "FanStage03";
    public static final String STATE_FAN_STAGE_4 = "FanStage04";
    public static final String STATE_FAN_STAGE_5 = "FanStage05";
    public static final String STATE_INTENSIVE_STAGE_OFF = "IntensiveStageOff";
    public static final String STATE_INTENSIVE_STAGE_1 = "IntensiveStage1";
    public static final String STATE_INTENSIVE_STAGE_2 = "IntensiveStage2";
    public static final String STATE_POOR = "Poor";

    // Commands
    public static final String COMMAND_START = "start";
    public static final String COMMAND_PAUSE = "pause";
    public static final String COMMAND_RESUME = "resume";
    public static final String COMMAND_STOP = "stop";

    // Dimensions
    public static final String NUMBER_TEMPERATURE = CoreItemFactory.NUMBER + ":Temperature";
    public static final String NUMBER_DIMENSIONLESS = CoreItemFactory.NUMBER + ":Dimensionless";

    // i18n
    public static final String I18N_I_DOS_FILL_LEVEL_POOR = "channel.idos.fill-level-poor";
    public static final String I18N_I_DOS_ACTIVE = "channel.idos.active";
    public static final String I18N_START_PROGRAM = "command.program.start";
    public static final String I18N_PAUSE_PROGRAM = "command.program.pause";
    public static final String I18N_RESUME_PROGRAM = "command.program.resume";
    public static final String I18N_STOP_PROGRAM = "command.program.stop";
    public static final String I18N_OVEN_DOOR = "channel.oven.door";
    public static final String I18N_OVEN_CURRENT_TEMPERATURE = "channel.oven.current-temperature";
    public static final String I18N_OVEN_CURRENT_MEAT_PROBE_TEMPERATURE = "channel.oven.meat-probe-temperature";
    public static final String I18N_OVEN_MEAT_PROBE_PLUGGED = "channel.oven.meat-probe-plugged";
    public static final String I18N_OVEN_CAVITY_LIGHT = "channel.oven.cavity-light";
    public static final String I18N_FRIDGE_FREEZER_SET_POINT_TEMPERATURE = "channel.fridgefreezer.setpoint";
    public static final String I18N_FRIDGE_FREEZER_DOOR = "channel.fridgefreezer.door";
    public static final String I18N_FRIDGE_FREEZER_SUPER_MODE = "channel.fridgefreezer.super-mode";
    public static final String I18N_FRIDGE_DISPENSER_WATER_FILTER_SATURATION = "channel.fridgefreezer.water-filter-saturation";
    public static final String I18N_FRIDGE_DISPENSER_ENABLED = "channel.fridgefreezer.dispenser-enabled";
    public static final String I18N_FRIDGE_DISPENSER_PARTY_MODE = "channel.fridgefreezer.dispenser-party-mode";
    public static final String I18N_FRIDGE_CHILLER_PRESET = "channel.fridgefreezer.chiller-preset";
    public static final String I18N_DOOR = "channel.door";

    // Device Description Attributes
    public static final String ATTRIBUTE_ACCESS = "access";
    public static final String ATTRIBUTE_AVAILABLE = "available";
    public static final String ATTRIBUTE_MIN = "min";
    public static final String ATTRIBUTE_MAX = "max";
    public static final String ATTRIBUTE_STEP_SIZE = "stepSize";
    public static final String ATTRIBUTE_ENUMERATION_TYPE = "enumerationType";
    public static final String ATTRIBUTE_ENUMERATION_TYPE_KEY = "enumerationTypeKey";
}
