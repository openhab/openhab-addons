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
package org.openhab.binding.mideaac.internal;

import java.util.Collections;
import java.util.Set;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MideaACBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jacek Dobrowolski - Initial contribution
 * @author Bob Eckhoff - OH naming conventions and capability properties
 */
@NonNullByDefault
public class MideaACBindingConstants {

    private static final String BINDING_ID = "mideaac";

    /**
     * Thing Type
     */
    public static final ThingTypeUID THING_TYPE_MIDEAAC = new ThingTypeUID(BINDING_ID, "ac");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_MIDEAAC);

    /**
     * List of all channel IDS
     */
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_APPLIANCE_ERROR = "appliance-error";
    public static final String CHANNEL_TARGET_TEMPERATURE = "target-temperature";
    public static final String CHANNEL_OPERATIONAL_MODE = "operational-mode";
    public static final String CHANNEL_FAN_SPEED = "fan-speed";
    public static final String CHANNEL_ON_TIMER = "on-timer";
    public static final String CHANNEL_OFF_TIMER = "off-timer";
    public static final String CHANNEL_SWING_MODE = "swing-mode";
    public static final String CHANNEL_AUXILIARY_HEAT = "auxiliary-heat";
    public static final String CHANNEL_ECO_MODE = "eco-mode";
    public static final String CHANNEL_TEMPERATURE_UNIT = "temperature-unit";
    public static final String CHANNEL_SLEEP_FUNCTION = "sleep-function";
    public static final String CHANNEL_TURBO_MODE = "turbo-mode";
    public static final String CHANNEL_INDOOR_TEMPERATURE = "indoor-temperature";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_ALTERNATE_TARGET_TEMPERATURE = "alternate-target-temperature";
    public static final String CHANNEL_SCREEN_DISPLAY = "screen-display";
    public static final String DROPPED_COMMANDS = "dropped-commands";

    public static final Unit<Temperature> API_TEMPERATURE_UNIT = SIUnits.CELSIUS;

    /**
     * Commands sent to/from AC wall unit are ASCII
     */
    public static final String CHARSET = "US-ASCII";

    /**
     * List of all AC thing properties
     */
    public static final String CONFIG_IP_ADDRESS = "ipAddress";
    public static final String CONFIG_IP_PORT = "ipPort";
    public static final String CONFIG_DEVICEID = "deviceId";
    public static final String CONFIG_CLOUD = "cloud";
    public static final String CONFIG_EMAIL = "email";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_TOKEN = "token";
    public static final String CONFIG_KEY = "key";
    public static final String CONFIG_POLLING_TIME = "pollingTime";
    public static final String CONFIG_KEY_TOKEN_UPDATE = "keyTokenUpdate";
    public static final String CONFIG_CONNECTING_TIMEOUT = "timeout";
    public static final String CONFIG_PROMPT_TONE = "promptTone";
    public static final String CONFIG_VERSION = "version";

    // Properties from LAN Discovery
    public static final String PROPERTY_SN = "sn";
    public static final String PROPERTY_SSID = "ssid";
    public static final String PROPERTY_TYPE = "type";

    // Capabilities properties
    public static final String PROPERTY_ANION = "anion";
    public static final String PROPERTY_AUX_ELECTRIC_HEAT = "auxElectricHeat";
    public static final String PROPERTY_BREEZE_AWAY = "breezeAway";
    public static final String PROPERTY_BREEZE_CONTROL = "breezeControl";
    public static final String PROPERTY_BREEZELESS = "breezeless";
    public static final String PROPERTY_BUZZER = "buzzer";
    public static final String PROPERTY_DISPLAY_CONTROL = "displayControl";
    public static final String PROPERTY_ENERGY_STATS = "energyStats";
    public static final String PROPERTY_ENERGY_SETTING = "energySetting";
    public static final String PROPERTY_ENERGY_BCD = "energyBCD";
    public static final String PROPERTY_FAHRENHEIT = "fahrenheit";
    public static final String PROPERTY_FAN_SPEED_CONTROL_LOW = "fanLow";
    public static final String PROPERTY_FAN_SPEED_CONTROL_MEDIUM = "fanMedium";
    public static final String PROPERTY_FAN_SPEED_CONTROL_HIGH = "fanHigh";
    public static final String PROPERTY_FAN_SPEED_CONTROL_AUTO = "fanAuto";
    public static final String PROPERTY_FAN_SPEED_CONTROL_SILENT = "fanSilent";
    public static final String PROPERTY_FAN_SPEED_CONTROL_CUSTOM = "fanCustom";
    public static final String PROPERTY_FILTER_REMIND_NOTICE = "filterNotice";
    public static final String PROPERTY_FILTER_REMIND_CLEAN = "filterClean";
    public static final String PROPERTY_HUMIDITY_AUTO_SET = "humidityAutoSet";
    public static final String PROPERTY_HUMIDITY_MANUAL_SET = "humidityManualSet";
    public static final String PROPERTY_MODES_AUTO = "modeAuto";
    public static final String PROPERTY_MODES_AUX = "modeAux";
    public static final String PROPERTY_MODES_AUX_HEAT = "modeAuxHeat";
    public static final String PROPERTY_MODES_COOL = "modeCool";
    public static final String PROPERTY_MODES_DRY = "modeDry";
    public static final String PROPERTY_MODES_FAN_ONLY = "modeFanOnly";
    public static final String PROPERTY_MODES_HEAT = "modeHeat";
    public static final String PROPERTY_PRESET_ECO = "ecoCool";
    public static final String PROPERTY_PRESET_FREEZE_PROTECTION = "freezeProtection";
    public static final String PROPERTY_PRESET_IECO = "ieco";
    public static final String PROPERTY_PRESET_TURBO_COOL = "turboCool";
    public static final String PROPERTY_PRESET_TURBO_HEAT = "turboHeat";
    public static final String PROPERTY_RATE_SELECT = "rateSelect5Level";
    public static final String PROPERTY_SELF_CLEAN = "selfClean";
    public static final String PROPERTY_SMART_EYE = "smartEye";
    public static final String PROPERTY_SWING_LR_ANGLE = "swingHorizontalAngle";
    public static final String PROPERTY_SWING_UD_ANGLE = "swingVerticalAngle";
    public static final String PROPERTY_SWING_MODES_HORIZONTAL = "swingHorizontal";
    public static final String PROPERTY_SWING_MODES_VERTICAL = "swingVertical";
    public static final String PROPERTY_TEMPERATURES_MIN_DEFAULT = "minTargetTemperature";
    public static final String PROPERTY_TEMPERATURES_MAX_DEFAULT = "maxTargetTemperature";
    public static final String PROPERTY_TEMPERATURES_COOL_MIN = "coolMinTemperature";
    public static final String PROPERTY_TEMPERATURES_COOL_MAX = "coolMaxTemperature";
    public static final String PROPERTY_TEMPERATURES_AUTO_MIN = "autoMinTemperature";
    public static final String PROPERTY_TEMPERATURES_AUTO_MAX = "autoMaxTemperature";
    public static final String PROPERTY_TEMPERATURES_HEAT_MIN = "heatMinTemperature";
    public static final String PROPERTY_TEMPERATURES_HEAT_MAX = "heatMaxTemperature";
    public static final String PROPERTY_WIND_OFF_ME = "windOffMe";
    public static final String PROPERTY_WIND_ON_ME = "windOnMe";
}
