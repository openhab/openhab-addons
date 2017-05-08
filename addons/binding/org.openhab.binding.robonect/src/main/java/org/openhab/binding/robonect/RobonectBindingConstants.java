/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.robonect;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RobonectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author marco.meyer - Initial contribution
 */
public class RobonectBindingConstants {

    private static final String BINDING_ID = "robonect";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "mower");

    // List of all Channel ids
    public static final String CHANNEL_MOWER_NAME = "mowerInfo#name";
    public static final String CHANNEL_STATUS_BATTERY = "mowerStatus#battery";
    public static final String CHANNEL_STATUS_DURATION = "mowerStatus#duration";
    public static final String CHANNEL_STATUS_HOURS = "mowerStatus#hours";
    public static final String CHANNEL_STATUS_MODE = "mowerStatus#mode";
    public static final String CHANNEL_STATUS = "mowerStatus#status";
    public static final String MOWER_STATUS_STARTED = "mowerStatus#started";
    public static final String CHANNEL_TIMER_STATUS = "timer#status";
    public static final String CHANNEL_TIMER_NEXT_DATE = "timer#nextDate";
    public static final String CHANNEL_TIMER_NEXT_TIME = "timer#nextTime";
    public static final String CHANNEL_TIMER_NEXT_UNIX_TS = "timer#nextUnixTime";
    public static final String CHANNEL_WLAN_SIGNAL = "wlan#signal";
    
    public static final String CHANNEL_VERSION_SERIAL = "version#serial";
    public static final String CHANNEL_VERSION_VERSION = "version#version";
    public static final String CHANNEL_VERSION_COMPILED = "version#compiled";
    public static final String CHANNEL_VERSION_COMMENT = "version#comment";
    
    public static final String CHANNEL_JOB_REMOTE_START = "job#remoteStart";
    public static final String CHANNEL_JOB_AFTER_MODE = "job#afterMode";
    public static final String CHANNEL_JOB_START = "job#start";
    public static final String CHANNEL_JOB_END = "job#end";
    

}
