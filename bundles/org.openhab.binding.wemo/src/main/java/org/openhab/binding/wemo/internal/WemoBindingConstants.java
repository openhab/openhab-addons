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
package org.openhab.binding.wemo.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WemoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Jörg Merk - Initial contribution
 * @author Mihir Patil - Added standby switch
 */
@NonNullByDefault
public class WemoBindingConstants {

    public static final String BINDING_ID = "wemo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SOCKET = new ThingTypeUID(BINDING_ID, "socket");
    public static final ThingTypeUID THING_TYPE_INSIGHT = new ThingTypeUID(BINDING_ID, "insight");
    public static final ThingTypeUID THING_TYPE_LIGHTSWITCH = new ThingTypeUID(BINDING_ID, "lightswitch");
    public static final ThingTypeUID THING_TYPE_MOTION = new ThingTypeUID(BINDING_ID, "motion");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_MZ100 = new ThingTypeUID(BINDING_ID, "MZ100");
    public static final ThingTypeUID THING_TYPE_MAKER = new ThingTypeUID(BINDING_ID, "Maker");
    public static final ThingTypeUID THING_TYPE_COFFEE = new ThingTypeUID(BINDING_ID, "CoffeeMaker");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_CROCKPOT = new ThingTypeUID(BINDING_ID, "Crockpot");
    public static final ThingTypeUID THING_TYPE_PURIFIER = new ThingTypeUID(BINDING_ID, "Purifier");
    public static final ThingTypeUID THING_TYPE_HUMIDIFIER = new ThingTypeUID(BINDING_ID, "Humidifier");
    public static final ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "Heater");

    // List of all Channel ids
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_MOTION_DETECTION = "motionDetection";
    public static final String CHANNEL_LAST_MOTION_DETECTED = "lastMotionDetected";
    public static final String CHANNEL_LAST_CHANGED_AT = "lastChangedAt";
    public static final String CHANNEL_LAST_ON_FOR = "lastOnFor";
    public static final String CHANNEL_ON_TODAY = "onToday";
    public static final String CHANNEL_ON_TOTAL = "onTotal";
    public static final String CHANNEL_TIMESPAN = "timespan";
    public static final String CHANNEL_AVERAGE_POWER = "averagePower";
    public static final String CHANNEL_CURRENT_POWER = "currentPower";
    public static final String CHANNEL_CURRENT_POWER_RAW = "currentPowerRaw";
    public static final String CHANNEL_ENERGY_TODAY = "energyToday";
    public static final String CHANNEL_ENERGY_TOTAL = "energyTotal";
    public static final String CHANNEL_STAND_BY_LIMIT = "standByLimit";
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_RELAY = "relay";
    public static final String CHANNEL_SENSOR = "sensor";
    public static final String CHANNEL_ON_STAND_BY = "onStandBy";

    public static final String CHANNEL_COFFEE_MODE = "coffeeMode";
    public static final String CHANNEL_MODE_TIME = "modeTime";
    public static final String CHANNEL_TIME_REMAINING = "timeRemaining";
    public static final String CHANNEL_WATER_LEVEL_REACHED = "waterLevelReached";
    public static final String CHANNEL_CLEAN_ADVISE = "cleanAdvise";
    public static final String CHANNEL_FILTER_ADVISE = "filterAdvise";
    public static final String CHANNEL_BREWED = "brewed";
    public static final String CHANNEL_LAST_CLEANED = "lastCleaned";

    public static final String CHANNEL_FADER_ENABLED = "faderEnabled";
    public static final String CHANNEL_TIMER_START = "timerStart";
    public static final String CHANNEL_FADER_COUNT_DOWN_TIME = "faderCountDownTime";
    public static final String CHANNEL_NIGHT_MODE = "nightMode";
    public static final String CHANNEL_START_TIME = "startTime";
    public static final String CHANNEL_END_TIME = "endTime";
    public static final String CHANNEL_NIGHT_MODE_BRIGHTNESS = "nightModeBrightness";

    public static final String CHANNEL_COOK_MODE = "cookMode";
    public static final String CHANNEL_LOW_COOK_TIME = "lowCookTime";
    public static final String CHANNEL_WARM_COOK_TIME = "warmCooktime";
    public static final String CHANNEL_HIGHCOOKTIME = "highCooktime";
    public static final String CHANNEL_COOKED_TIME = "cookedtime";

    public static final String CHANNEL_PURIFIER_MODE = "purifierMode";
    public static final String CHANNEL_AIR_QUALITY = "airQuality";
    public static final String CHANNEL_IONIZER = "ionizer";
    public static final String CHANNEL_FILTER_LIFE = "filterLife";
    public static final String CHANNEL_EXPIRED_FILTER_TIME = "expiredFilterTime";
    public static final String CHANNEL_FILTER_PRESENT = "filterPresent";

    public static final String CHANNEL_HUMIDIFIER_MODE = "humidifierMode";
    public static final String CHANNEL_CURRENT_HUMIDITY = "currentHumidity";
    public static final String CHANNEL_DESIRED_HUMIDITY = "desiredHumidity";
    public static final String CHANNEL_WATER_LEVEL = "waterLEvel";

    public static final String CHANNEL_HEATER_MODE = "heaterMode";
    public static final String CHANNEL_CURRENT_TEMPERATURE = "currentTemperature";
    public static final String CHANNEL_TARGET_TEMPERATURE = "targetTemperature";
    public static final String CHANNEL_AUTO_OFF_TIME = "autoOffTime";
    public static final String CHANNEL_HEATING_REMAINING = "heatingRemaining";

    // List of thing configuration properties
    public static final String UDN = "udn";
    public static final String DEVICE_ID = "deviceID";
    public static final String POLLING_INTERVAL = "pollingInterval";
    public static final int DEFAULT_REFRESH_INTERVAL_SECONDS = 60;
    public static final int SUBSCRIPTION_DURATION_SECONDS = 1800;
    public static final int LINK_DISCOVERY_SERVICE_INITIAL_DELAY = 5;
    public static final String HTTP_CALL_CONTENT_HEADER = "text/xml; charset=utf-8";

    public static final String BASICACTION = "basicevent";
    public static final String BASICEVENT = "basicevent1";
    public static final String BRIDGEACTION = "bridge";
    public static final String BRIDGEEVENT = "bridge1";
    public static final String DEVICEACTION = "deviceevent";
    public static final String DEVICEEVENT = "deviceevent1";
    public static final String INSIGHTACTION = "insight";
    public static final String INSIGHTEVENT = "insight1";

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES = Set.of(THING_TYPE_BRIDGE);

    public static final Set<ThingTypeUID> SUPPORTED_LIGHT_THING_TYPES = Set.of(THING_TYPE_MZ100);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_SOCKET, THING_TYPE_INSIGHT,
            THING_TYPE_LIGHTSWITCH, THING_TYPE_MOTION, THING_TYPE_BRIDGE, THING_TYPE_MZ100, THING_TYPE_MAKER,
            THING_TYPE_COFFEE, THING_TYPE_DIMMER, THING_TYPE_CROCKPOT, THING_TYPE_PURIFIER, THING_TYPE_HUMIDIFIER,
            THING_TYPE_HEATER);
}
