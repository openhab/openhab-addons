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
package org.openhab.binding.lgthinq.internal;

import java.io.File;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.lgthinq.lgservices.LGServicesConstants;
import org.openhab.binding.lgthinq.lgservices.model.DeviceTypes;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LGThinQBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQBindingConstants extends LGServicesConstants {

    public static final String BINDING_ID = "lgthinq";

    // =============== Thing Configuration Constants ===========
    public static final String CFG_POLLING_PERIOD_POWER_ON_SEC = "pollingPeriodPowerOnSeconds";
    public static final String CFG_POLLING_PERIOD_POWER_OFF_SEC = "pollingPeriodPowerOffSeconds";
    public static final String CFG_POLLING_EXTRA_INFO_PERIOD_SEC = "pollingExtraInfoPeriodSeconds";
    public static final String CFG_POLLING_EXTRA_INFO_ON_POWER_OFF = "pollExtraInfoOnPowerOff";
    // =============== Thing Type IDs ==================
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "cloud-account");
    public static final ThingTypeUID THING_TYPE_AIR_CONDITIONER = new ThingTypeUID(BINDING_ID,
            DeviceTypes.AIR_CONDITIONER.thingTypeId());
    public static final ThingTypeUID THING_TYPE_WASHING_MACHINE = new ThingTypeUID(BINDING_ID,
            DeviceTypes.WASHERDRYER_MACHINE.thingTypeId());
    public static final ThingTypeUID THING_TYPE_WASHING_TOWER = new ThingTypeUID(BINDING_ID,
            DeviceTypes.WASHER_TOWER.thingTypeId());
    public static final ThingTypeUID THING_TYPE_DRYER = new ThingTypeUID(BINDING_ID, DeviceTypes.DRYER.thingTypeId());
    public static final ThingTypeUID THING_TYPE_HEAT_PUMP = new ThingTypeUID(BINDING_ID,
            DeviceTypes.HEAT_PUMP.thingTypeId());
    public static final ThingTypeUID THING_TYPE_DRYER_TOWER = new ThingTypeUID(BINDING_ID,
            DeviceTypes.DRYER_TOWER.thingTypeId());
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_WASHING_MACHINE, THING_TYPE_WASHING_TOWER, THING_TYPE_DRYER, THING_TYPE_DRYER_TOWER,
            THING_TYPE_HEAT_PUMP);
    public static final ThingTypeUID THING_TYPE_FRIDGE = new ThingTypeUID(BINDING_ID, DeviceTypes.FRIDGE.thingTypeId());
    public static final ThingTypeUID THING_TYPE_DISHWASHER = new ThingTypeUID(BINDING_ID,
            DeviceTypes.DISH_WASHER.thingTypeId());
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_AIR_CONDITIONER,
            THING_TYPE_WASHING_MACHINE, THING_TYPE_WASHING_TOWER, THING_TYPE_DRYER_TOWER, THING_TYPE_DRYER,
            THING_TYPE_FRIDGE, THING_TYPE_BRIDGE, THING_TYPE_HEAT_PUMP, THING_TYPE_DISHWASHER);
    // ======== Common Channels & Constants ========
    public static final String CHANNEL_DASHBOARD_GRP_ID = "dashboard";
    public static final String CHANNEL_DASHBOARD_GRP_WITH_SEP = "dashboard#";
    public static final String CHANNEL_EXTENDED_INFO_GRP_ID = "extended-information";
    public static final String CHANNEL_EXTENDED_INFO_COLLECTOR_ID = "extra-info-collector";
    // Max number of retries trying to get the monitor (V1) until consider ERROR in the connection
    public static final int MAX_GET_MONITOR_RETRIES = 3;
    public static final int DISCOVERY_SEARCH_TIMEOUT = 20;
    // === Biding property info
    public static final String PROP_INFO_DEVICE_ALIAS = "deviceAlias";
    public static final String PROP_INFO_DEVICE_ID = "deviceId";
    public static final String PROP_INFO_MODEL_URL_INFO = "modelUrlInfo";
    public static final String PROP_INFO_PLATFORM_TYPE = "platformType";
    public static final String PROP_INFO_MODEL_ID = "modelId";

    // === UserData Directory and File Format
    private static final String DEFAULT_USER_DATA_FOLDER = OpenHAB.getUserDataFolder() + File.separator + "thinq";

    public static String getThinqUserDataFolder() {
        return Objects.requireNonNullElse(System.getProperty("THINQ_USER_DATA_FOLDER"), DEFAULT_USER_DATA_FOLDER);
    }

    public static String getThinqConnectionDataFile() {
        return Objects.requireNonNullElse(System.getProperty("THINQ_CONNECTION_DATA_FILE"),
                getThinqUserDataFolder() + File.separator + "thinqbridge-%s.json");
    }

    public static String getBaseCapConfigDataFile() {
        return Objects.requireNonNullElse(System.getProperty("BASE_CAP_CONFIG_DATA_FILE"),
                getThinqUserDataFolder() + File.separator + "thinq-%s-cap.json");
    }

    // ====================================================

    /**
     * ============ Air Conditioner Channels & Constant Definition =============
     */
    public static final String CHANNEL_AC_AIR_CLEAN_ID = "air-clean";
    public static final String CHANNEL_AC_AIR_WATER_SWITCH_ID = "air-water-switch";
    public static final String CHANNEL_AC_AUTO_DRY_ID = "auto-dry";
    public static final String CHANNEL_AC_COOL_JET_ID = "cool-jet";
    public static final String CHANNEL_AC_CURRENT_ENERGY_ID = "current-energy";
    public static final String CHANNEL_AC_CURRENT_TEMP_ID = "current-temperature";
    public static final String CHANNEL_AC_ENERGY_SAVING_ID = "energy-saving";
    public static final String CHANNEL_AC_FAN_SPEED_ID = "fan-speed";
    public static final String CHANNEL_AC_MAX_TEMP_ID = "max-temperature";
    public static final String CHANNEL_AC_MIN_TEMP_ID = "min-temperature";
    public static final String CHANNEL_AC_MOD_OP_ID = "op-mode";
    public static final String CHANNEL_AC_POWER_ID = "power";
    public static final String CHANNEL_AC_REMAINING_FILTER_ID = "remaining-filter";
    public static final String CHANNEL_AC_STEP_LEFT_RIGHT_ID = "fan-step-left-right";
    public static final String CHANNEL_AC_STEP_UP_DOWN_ID = "fan-step-up-down";
    public static final String CHANNEL_AC_TARGET_TEMP_ID = "target-temperature";

    /**
     * ============ Refrigerator's Channels & Constant Definition =============
     */
    public static final String CHANNEL_FR_ACTIVE_SAVING = "active-saving";
    public static final String CHANNEL_FR_DOOR_OPEN = "door-open";
    public static final String CHANNEL_FR_EXPRESS_COOL_MODE = "express-cool-mode";
    public static final String CHANNEL_FR_EXPRESS_FREEZE_MODE = "express-mode";
    public static final String CHANNEL_FR_FREEZER_TEMP_ID = "freezer-temperature";
    public static final String CHANNEL_FR_FRESH_AIR_FILTER = "fresh-air-filter";
    public static final String CHANNEL_FR_FRIDGE_TEMP_ID = "fridge-temperature";
    public static final String CHANNEL_FR_ICE_PLUS = "ice-plus";
    public static final String CHANNEL_FR_REF_TEMP_UNIT = "temp-unit";
    public static final String CHANNEL_FR_SMART_SAVING_MODE_V2 = "smart-saving-mode";
    public static final String CHANNEL_FR_SMART_SAVING_SWITCH_V1 = "smart-saving-switch";
    public static final String CHANNEL_FR_VACATION_MODE = "eco-friendly-mode";
    public static final String CHANNEL_FR_WATER_FILTER = "water-filter";

    /**
     * ============ Washing Machine/Dryer and DishWasher Channels & Constant Definition =============
     * DishWasher, Washing Machine and Dryer have the same channel core and features
     */
    public static final String CHANNEL_WMD_CHILD_LOCK_ID = "child-lock";
    public static final String CHANNEL_WMD_DRY_LEVEL_ID = "dry-level";
    public static final String CHANNEL_WMD_COURSE_ID = "course";
    public static final String CHANNEL_WMD_DELAY_TIME_ID = "delay-time";
    public static final String CHANNEL_WMD_DOOR_LOCK_ID = "door-lock";
    public static final String CHANNEL_WMD_PROCESS_STATE_ID = "process-state";
    public static final String CHANNEL_WMD_REMAIN_TIME_ID = "remain-time";
    public static final String CHANNEL_WMD_REMOTE_COURSE = "rs-course";
    public static final String CHANNEL_WMD_REMOTE_START_GRP_ID = "rs-grp";
    public static final String CHANNEL_WMD_REMOTE_START_ID = "rs-flag";
    public static final String CHANNEL_WMD_REMOTE_START_START_STOP = "rs-start-stop";
    public static final String CHANNEL_WMD_RINSE_ID = "rinse";
    public static final String CHANNEL_WMD_SMART_COURSE_ID = "smart-course";
    public static final String CHANNEL_WMD_SPIN_ID = "spin";
    public static final String CHANNEL_WMD_STAND_BY_ID = "stand-by";
    public static final String CHANNEL_WMD_STATE_ID = "state";
    public static final String CHANNEL_WMD_TEMP_LEVEL_ID = "temperature-level";
    public static final String CHANNEL_WMD_REMOTE_START_RINSE = "rs-rinse";
    public static final String CHANNEL_WMD_REMOTE_START_SPIN = "rs-spin";
    public static final String CHANNEL_WMD_REMOTE_START_TEMP = "rs-temperature-level";

    // ==============================================================================
    // DIGEST CONSTANTS
    public static final String MESSAGE_DIGEST_ALGORITHM = "SHA-512";
    public static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";
}
