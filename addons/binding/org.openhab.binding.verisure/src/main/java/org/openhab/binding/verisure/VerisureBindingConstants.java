/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VerisureBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author l3rum - Initial contribution
 */
public class VerisureBindingConstants {

    public static final String LOCK = "lock";
    public static final String BINDING_ID = "verisure";
    public static final String CLIMATESENSOR_ID = "climatesensor";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "VerisureAlarm");
    public final static ThingTypeUID THING_TYPE_SMARTPLUG = new ThingTypeUID(BINDING_ID, "smartplug");
    public final static ThingTypeUID THING_TYPE_CLIMATESENSOR = new ThingTypeUID(BINDING_ID, "climatesensor");
    public final static ThingTypeUID THING_TYPE_DOORWINDOW = new ThingTypeUID(BINDING_ID, "doorwindow");
    public final static ThingTypeUID THING_TYPE_USERPRESENCE = new ThingTypeUID(BINDING_ID, "userpresence");
    public final static ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, LOCK);

    // List of all Channel ids
    public final static String CHANNEL_STATUS_NUMERIC = "statusnumeric";
    public final static String CHANNEL_TEMPERATURE = "temperature";
    public final static String CHANNEL_HUMIDITY = "humidity";
    public final static String CHANNEL_LASTUPDATE = "lastupdate";
    public final static String CHANNEL_LOCATION = "location";
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_STATE = "state";
    public final static String CHANNEL_LABEL = "label";
    public final static String CHANNEL_LOCATIONSTATUS = "locationStatus";
    public final static String CHANNEL_ISHOME = "isHome";
    public final static String CHANNEL_WEBACCOUNT = "webAccount";
    public final static String CHANNEL_LOCATIONNAME = "locationName";
    public final static String CHANNEL_SETSTATUS = "setstatus";
    public static final String CHANNEL_STATUS_LOCALIZED = "alarmstatuslocalized";
    public static final String CHANNEL_DOORLOCK = "doorlock";

    public final static String CHANNEL_CHANGERNAME = "changername";
    public final static String CHANNEL_TIMESTAMP = "timestamp";

    // REST URI constants
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String BASEURL = "https://mypages.verisure.com";
    public static final String LOGON_SUF = "/j_spring_security_check?locale=en_GB";
    public static final String ALARM_COMMAND = "/remotecontrol/armstatechange.cmd";
    public static final String LOCK_COMMAND = "/remotecontrol/lockunlock.cmd";
    public static final String START_SUF = "/uk/start.html";

    public static final String ALARMSTATUS_PATH = "/remotecontrol?_=";
    public static final String DOORWINDOW_PATH = "/settings/doorwindow?_=";
    public static final String USERTRACKING_PATH = "/overview/usertrackingcontacts?_=";
    public static final String CLIMATEDATA_PATH = "/overview/climatedevice?_=";
    public static final String SMARTPLUGDATA_PATH = "/settings/smartplug?_=";

}
