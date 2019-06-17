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
package org.openhab.binding.siemensrds.internal;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RdsBindingConstants} contains the constants
 * used by the binding 
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
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
     *  Thing Type UIDs
     */
    public static final ThingTypeUID THING_TYPE_CLOUD = 
            new ThingTypeUID(BINDING_ID, DEVICE_ID_CLOUD);

    public static final ThingTypeUID THING_TYPE_RDS = 
            new ThingTypeUID(BINDING_ID, DEVICE_ID_STAT);

    
// ==========================  URLs and HTTP stuff =========================
     

    private static final String API = "https://api.climatixic.com/";
    
    private static final String ARG_RDS =
            "?filterId=[" +
                "{\"asn\":\"RDS110\"}," +
                "{\"asn\":\"RDS120\"}," +
                "{\"asn\":\"RDS110.R\"}," + 
                "{\"asn\":\"RDS120.B\"}" +
            "]";

    private static final String ARG_PARENT = "?parentId=[\"%s\"]&take=100";
    private static final String ARG_POINT = "?filterId=[\"%s\"]";
    
    public static final String URL_TOKEN  = API + "Token"; 
    public static final String URL_PLANTS = API + "Plants" + ARG_RDS; 
    public static final String URL_POINTS = API + "DataPoints" + ARG_PARENT; 
    public static final String URL_SETVAL = API + "DataPoints/%s"; 
    public static final String URL_GETVAL = API + "DataPoints/Values" + ARG_POINT; 
    
    public static final String HTTP_POST = "POST"; 
    public static final String HTTP_GET  = "GET"; 
    public static final String HTTP_PUT  = "PUT"; 

    public static final String HDR_USER_AGENT = "User-Agent";
    public static final String VAL_USER_AGENT = "Mozilla/5.0";

    public static final String HDR_ACCEPT = "Accept";
    public static final String VAL_ACCEPT = "application/json";
    
    public static final String HDR_CONT_TYPE = "Content-Type";
    public static final String VAL_CONT_PLAIN = "text/plain;charset=UTF-8";
    public static final String VAL_CONT_JSON = "application/json;charset=UTF-8";
    
    public static final String HDR_SUB_KEY = "Ocp-Apim-Subscription-Key";

    public static final String HDR_AUTHORIZE = "Authorization";
    public static final String VAL_AUTHORIZE = "Bearer %s";
    
    public static final String TOKEN_REQ = 
        "grant_type=password&username=%s&password=%s&expire_minutes=20160"; 

    /* 
     * setup parameters for de-bouncing of state changes (time in seconds) 
     * so state changes that occur within this time window are ignored
     */
    public static final long DEBOUNCE_DELAY = 15;

    /*
     * setup parameters for lazy polling  
     */
    public static final int LAZY_POLL_INTERVAL = 60;

    /* 
     * setup parameters for fast polling bursts 
     * a burst comprises FAST_POLL_CYCLES polling calls spaced at 
     * FAST_POLL_INTERVAL for example 5 polling calls made at 4 second 
     * intervals (e.g. 6 x 4 => 24 seconds)   
     */
    public static final int FAST_POLL_CYCLES = 4;
    public static final int FAST_POLL_INTERVAL = 6;
    
    /*
     * setup parameters for device discovery 
     */
    public static final int DISCOVERY_TIMEOUT = 5;
    public static final int DISCOVERY_START_DELAY = 30;
    public static final int DISCOVERY_REFRESH_PERIOD = 600;

    public static final String PROP_PLANT_ID = "plantId";
    
   /*
    * ==================== USED DATA POINTS ==========================
    * where:  
    *   OBJ_ = the Object Name id string in the ClimatixIc server
    *   CHA_ = the Channel ID string in the OpenHAB binding
    */
    
    // device name (id=70) 
    public static final String OBJ_DESCRIPTION = "R(1)'Description";

    // room (actual) temperature (id=86) (read-only)
    private static final String CHA_ROOM_TEMP = "roomTemperature";
    private static final String OBJ_ROOM_TEMP = "R(1)'RHvacCoo'RTemp";

    // room relative humidity (id=85) (read-only)
    private static final String CHA_ROOM_HUMIDITY = "roomHumidity";
    private static final String OBJ_ROOM_HUMIDITY = "R(1)'RHvacCoo'RHuRel";

    // room air quality (low/med/high) (id=74) (read-only)
    private static final String CHA_ROOM_AIR_QUALITY = "roomAirQuality";
    private static final String OBJ_ROOM_AIR_QUALITY = "R(1)'RHvacCoo'RAQualInd";

    // green leaf state (poor..excellent) (id=52) (read-write)
    // note: writing the value "5" forces the device to green mode 
    private static final String CHA_GREEN_LEAF = "greenLeaf";
    private static final String OBJ_GREEN_LEAF = "R(1)'RGrnLf'REei";

    // outside air temperature (id=0E) (read-only)
    private static final String CHA_OUTSIDE_TEMP = "outsideTemperature";
    private static final String OBJ_OUTSIDE_TEMP = "R(1)'TOa";

    // set-point for temporary override (id=83) (read-write)
    private static final String CHA_SETPOINT = "setTemperature";
    private static final String OBJ_SETPOINT = "R(1)'RHvacCoo'SpTRDtr'SpTR";

    // occupancy mode (id=51) (read-write)
    private static final String CHA_ABSENT_PRESENT = "absentPresent";
    private static final String OBJ_ABSENT_PRESENT = "R(1)'ROpModDtr'OccMod";

    // domestic hot water mode (id=53) (read-write)
    private static final String CHA_DHW_AUTO_OFF_ON = "dhwAutoOffOn";
    private static final String OBJ_DHW_AUTO_OFF_ON = "R(1)'RHvacCoo'DhwOp'DhwMod";

    // heating/cooling state (id=56) (read-only)
    private static final String CHA_HEAT_OFF_COOL = "heatingOffCooling";
    private static final String OBJ_HEAT_OFF_COOL = 
            "R(1)'RHvacCoo'HCStaDtr'HCSta";

    public static final ChannelMap[] CHAN_MAP = {
        new ChannelMap(CHA_ROOM_TEMP,        OBJ_ROOM_TEMP,        Ptype.VALUE),
        new ChannelMap(CHA_ROOM_HUMIDITY,    OBJ_ROOM_HUMIDITY,    Ptype.VALUE),
        new ChannelMap(CHA_ROOM_AIR_QUALITY, OBJ_ROOM_AIR_QUALITY, Ptype.ENUM),
        new ChannelMap(CHA_GREEN_LEAF,       OBJ_GREEN_LEAF,       Ptype.ENUM),
        new ChannelMap(CHA_OUTSIDE_TEMP,     OBJ_OUTSIDE_TEMP,     Ptype.VALUE),
        new ChannelMap(CHA_SETPOINT,         OBJ_SETPOINT,         Ptype.VALUE),
        new ChannelMap(CHA_ABSENT_PRESENT,   OBJ_ABSENT_PRESENT,   Ptype.ENUM),
        new ChannelMap(CHA_DHW_AUTO_OFF_ON,  OBJ_DHW_AUTO_OFF_ON,  Ptype.ENUM),
        new ChannelMap(CHA_HEAT_OFF_COOL,    OBJ_HEAT_OFF_COOL,    Ptype.ENUM)
    };        

    
/*    
    // ==================== UNUSED DATA POINTS ==========================

    // room air quality (numeric value) 
    private static final String OBJ_ROOM_AIR_QUALITY_VAL = "R(1)'RHvacCoo'RAQual";

    // other set-points for phases of the time program mode
    private static final String OBJ_CMF_SETPOINT = "R(1)'RHvacCoo'TCtlH'SpHCmf";
    private static final String OBJ_PCF_SETPOINT = "R(1)'RHvacCoo'TCtlH'SpHPcf";
    private static final String OBJ_ECO_SETPOINT = "R(1)'RHvacCoo'TCtlH'SpHEco";
    private static final String OBJ_PRT_SETPOINT = "R(1)'RHvacCoo'TCtlH'SpHPrt";

    // enable heating control
    private static final String OBJ_ENABLE_HEATING = "R(1)'RHvacCoo'TCtlH'EnHCtl";

    // comfort button
    private static final String OBJ_COMFORT_BUTTON = "R(1)'ROpModDtr'CmfBtn";

*/
}

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
enum Ptype {
    ENUM,
    VALUE
}

/**
 * @author Andrew Fiddian-Green - Initial contribution
 */
class ChannelMap {
    public String channelId;
    public String objectName;
    public Ptype pType;

    public ChannelMap(String channelId, String objectName, Ptype pType) {
        this.channelId = channelId;
        this.objectName = objectName;
        this.pType = pType;
    }
}
    
