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
package org.openhab.binding.siemensrds.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RdsBindingConstants} contains the constants used by the binding
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public class RdsBindingConstants {

    /*
     * binding id
     */
    public static final String BINDING_ID = "siemensrds";

    /*
     * device id's
     */
    public static final String DEVICE_ID_CLOUD = "climatixic";
    public static final String DEVICE_ID_STAT = "rds";

    /*
     * Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_CLOUD = new ThingTypeUID(BINDING_ID, DEVICE_ID_CLOUD);

    public static final ThingTypeUID THING_TYPE_RDS = new ThingTypeUID(BINDING_ID, DEVICE_ID_STAT);

    // ========================== URLs and HTTP stuff =========================

    private static final String API = "https://api.climatixic.com/";

    private static final String ARG_RDS = "?filterId=[" + "{\"asn\":\"RDS110\"}," + "{\"asn\":\"RDS120\"},"
            + "{\"asn\":\"RDS110.R\"}," + "{\"asn\":\"RDS120.B\"}" + "]";

    private static final String ARG_PARENT = "?parentId=[\"%s\"]&take=100";
    private static final String ARG_POINT = "?filterId=[%s]";

    public static final String URL_TOKEN = API + "Token";
    public static final String URL_PLANTS = API + "Plants" + ARG_RDS;
    public static final String URL_POINTS = API + "DataPoints" + ARG_PARENT;
    public static final String URL_SETVAL = API + "DataPoints/%s";
    public static final String URL_VALUES = API + "DataPoints/Values" + ARG_POINT;

    public static final String HTTP_POST = "POST";
    public static final String HTTP_GET = "GET";
    public static final String HTTP_PUT = "PUT";

    public static final String USER_AGENT = "User-Agent";
    public static final String ACCEPT = "Accept";
    public static final String CONTENT_TYPE = "Content-Type";

    public static final String MOZILLA = "Mozilla/5.0";
    public static final String APPLICATION_JSON = "application/json;charset=UTF-8";
    public static final String TEXT_PLAIN = "text/plain;charset=UTF-8";
    public static final String SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    public static final String AUTHORIZATION = "Authorization";
    public static final String BEARER = "Bearer %s";

    public static final String TOKEN_REQUEST = "grant_type=password&username=%s&password=%s&expire_minutes=20160";

    /*
     * setup parameters for de-bouncing of state changes (time in seconds) so state
     * changes that occur within this time window are ignored
     */
    public static final long DEBOUNCE_DELAY = 15;

    /*
     * setup parameters for lazy polling
     */
    public static final int LAZY_POLL_INTERVAL = 60;

    /*
     * setup parameters for fast polling bursts a burst comprises FAST_POLL_CYCLES
     * polling calls spaced at FAST_POLL_INTERVAL for example 5 polling calls made
     * at 4 second intervals (e.g. 6 x 4 => 24 seconds)
     */
    public static final int FAST_POLL_CYCLES = 6;
    public static final int FAST_POLL_INTERVAL = 8;

    /*
     * setup parameters for device discovery
     */
    public static final int DISCOVERY_TIMEOUT = 5;
    public static final int DISCOVERY_START_DELAY = 30;
    public static final int DISCOVERY_REFRESH_PERIOD = 600;

    public static final String PROP_PLANT_ID = "plantId";

    /*
     * ==================== USED DATA POINTS ==========================
     * 
     * where: HIE_xxx = the point class suffix part of the hierarchy name in the
     * ClimatixIc server, and CHA_xxx = the Channel ID in the OpenHAB binding
     * 
     */
    // device name
    public static final String HIE_DESCRIPTION = "'Description";

    // online state
    public static final String HIE_ONLINE = "#Online";

    // room (actual) temperature (read-only)
    protected static final String CHA_ROOM_TEMP = "roomTemperature";
    public static final String HIE_ROOM_TEMP = "'RTemp";

    // room relative humidity (read-only)
    protected static final String CHA_ROOM_HUMIDITY = "roomHumidity";
    public static final String HIE_ROOM_HUMIDITY = "'RHuRel";

    // room air quality (low/med/high) (read-only)
    protected static final String CHA_ROOM_AIR_QUALITY = "roomAirQuality";
    public static final String HIE_ROOM_AIR_QUALITY = "'RAQualInd";

    // energy savings level (green leaf) (poor..excellent) (read-write)
    // note: writing the value "5" forces the device to green leaf mode
    protected static final String CHA_ENERGY_SAVINGS_LEVEL = "energySavingsLevel";
    public static final String HIE_ENERGY_SAVINGS_LEVEL = "'REei";

    // outside air temperature (read-only)
    protected static final String CHA_OUTSIDE_TEMP = "outsideTemperature";
    public static final String HIE_OUTSIDE_TEMP = "'TOa";

    // set-point override (read-write)
    protected static final String CHA_TARGET_TEMP = "targetTemperature";
    public static final String HIE_TARGET_TEMP = "'SpTR";

    // heating/cooling state (read-only)
    protected static final String CHA_OUTPUT_STATE = "thermostatOutputState";
    public static final String HIE_OUTPUT_STATE = "'HCSta";

    /*
     * thermostat occupancy state (absent, present) (read-write) NOTE: uses
     * different parameters as follows.. OccMod = 2, 3 to read, and command to, the
     * absent, present states
     */
    protected static final String CHA_STAT_OCC_MODE_PRESENT = "occupancyModePresent";
    public static final String HIE_STAT_OCC_MODE_PRESENT = "'OccMod";

    /*
     * thermostat program mode (read-write) NOTE: uses different parameters as
     * follows.. PrOpModRsn presentPriority < / > 13 combined with OccMod = 2 to
     * read the manual, auto mode CmfBtn = 1 to command to the manual mode REei = 5
     * to command to the auto mode
     */
    protected static final String CHA_STAT_AUTO_MODE = "thermostatAutoMode";
    public static final String HIE_PR_OP_MOD_RSN = "'PrOpModRsn";
    public static final String HIE_STAT_CMF_BTN = "'CmfBtn";

    /*
     * domestic hot water state (off, on) (read-write) NOTE: uses different
     * parameters as follows.. DhwMod = 1, 2 to read, and command to, the off, on
     * states
     */
    protected static final String CHA_DHW_OUTPUT_STATE = "hotWaterOutputState";
    public static final String HIE_DHW_OUTPUT_STATE = "'DhwMod";

    /*
     * domestic hot water program mode (manual, auto) (read-write) NOTE: uses
     * different parameters as follows.. DhwMod presentPriority < / > 13 to read the
     * manual, auto mode DhwMod = 0 to command to the auto mode DhwMod = its current
     * value to command it's resp. manual states
     */
    protected static final String CHA_DHW_AUTO_MODE = "hotWaterAutoMode";

    /*
     * openHAB status strings
     */
    protected static final String STATE_NEITHER = "Neither";
    protected static final String STATE_OFF = "Off";

    public static final ChannelMap[] CHAN_MAP = { new ChannelMap(CHA_ROOM_TEMP, HIE_ROOM_TEMP),
            new ChannelMap(CHA_ROOM_HUMIDITY, HIE_ROOM_HUMIDITY), new ChannelMap(CHA_OUTSIDE_TEMP, HIE_OUTSIDE_TEMP),
            new ChannelMap(CHA_TARGET_TEMP, HIE_TARGET_TEMP),
            new ChannelMap(CHA_ROOM_AIR_QUALITY, HIE_ROOM_AIR_QUALITY),
            new ChannelMap(CHA_ENERGY_SAVINGS_LEVEL, HIE_ENERGY_SAVINGS_LEVEL),
            new ChannelMap(CHA_OUTPUT_STATE, HIE_OUTPUT_STATE),
            new ChannelMap(CHA_STAT_OCC_MODE_PRESENT, HIE_STAT_OCC_MODE_PRESENT),
            new ChannelMap(CHA_STAT_AUTO_MODE, HIE_PR_OP_MOD_RSN),
            new ChannelMap(CHA_DHW_OUTPUT_STATE, HIE_DHW_OUTPUT_STATE),
            new ChannelMap(CHA_DHW_AUTO_MODE, HIE_DHW_OUTPUT_STATE) };

    /*
     * ==================== UNUSED DATA POINTS ======================
     * 
     * room air quality (numeric value)
     * 
     * private static final String HIE_ROOM_AIR_QUALITY_VAL = "RAQual";
     * 
     * other set-points for phases of the time program mode
     * 
     * private static final String HIE_CMF_SETPOINT = "SpHCmf";
     * 
     * private static final String HIE_PCF_SETPOINT = "SpHPcf";
     * 
     * private static final String HIE_ECO_SETPOINT = "SpHEco";
     * 
     * private static final String HIE_PRT_SETPOINT = "SpHPrt";
     * 
     * enable heating control
     * 
     * private static final String HIE_ENABLE_HEATING = "EnHCtl";
     * 
     * comfort button
     * 
     * private static final String HIE_COMFORT_BUTTON = "CmfBtn";
     * 
     */

    /*
     * logger strings
     */
    public static final String LOG_HTTP_COMMAND = "{} for url {} characters long";
    public static final String LOG_CONTENT_LENGTH = "{} {} characters..";
    public static final String LOG_PAYLOAD_FMT = "{} {}";

    public static final String LOG_HTTP_COMMAND_ABR = "{} for url {} characters long (set log level to TRACE to see full url)..";
    public static final String LOG_CONTENT_LENGTH_ABR = "{} {} characters (set log level to TRACE to see full string)..";
    public static final String LOG_PAYLOAD_FMT_ABR = "{} {} ...";

    public static final String LOG_RECEIVED_MSG = "received";
    public static final String LOG_RECEIVED_MARK = "<<";

    public static final String LOG_SENDING_MSG = "sending";
    public static final String LOG_SENDING_MARK = ">>";

    public static final String LOG_SYSTEM_EXCEPTION = "system exception in {}, type={}, message=\"{}\"";
    public static final String LOG_RUNTIME_EXCEPTION = "runtime exception in {}, type={}, message=\"{}\"";
}

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
class ChannelMap {
    public String id;
    public String clazz;

    public ChannelMap(String channelId, String pointClass) {
        this.id = channelId;
        this.clazz = pointClass;
    }
}
