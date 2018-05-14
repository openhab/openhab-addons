/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 * @author Marco Meyer - Initial contribution
 */
public class RobonectBindingConstants {

    private static final String BINDING_ID = "robonect";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "mower");

    // List of all Channel ids
    public static final String CHANNEL_MOWER_NAME = "name";
    public static final String CHANNEL_STATUS_BATTERY = "battery";
    public static final String CHANNEL_STATUS_DURATION = "status-duration";
    public static final String CHANNEL_STATUS_HOURS = "mowing-hours";
    public static final String CHANNEL_STATUS_MODE = "mode";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_MOWER_START = "start";
    public static final String CHANNEL_MOWER_STATUS_OFFLINE_TRIGGER = "offlineTrigger";
    public static final String CHANNEL_TIMER_STATUS = "timer-status";
    public static final String CHANNEL_TIMER_NEXT_TIMER = "timer-next";
    public static final String CHANNEL_WLAN_SIGNAL = "wlan-signal";
    
    public static final String CHANNEL_JOB = "job";
    
    public static final String CHANNEL_ERROR_CODE = "error-code";
    public static final String CHANNEL_ERROR_MESSAGE = "error-message";
    public static final String CHANNEL_ERROR_DATE = "error-date";
    
    public static final String CHANNEL_LAST_ERROR_CODE = "last-error-code";
    public static final String CHANNEL_LAST_ERROR_MESSAGE = "last-error-message";
    public static final String CHANNEL_LAST_ERROR_DATE = "last-error-date";
    
    public static final String CHANNEL_HEALTH_TEMP = "health-temperature";
    public static final String CHANNEL_HEALTH_HUM = "health-humidity";
    
    public static final String PROPERTY_COMPILED = "compiled";
    public static final String PROPERTY_COMMENT = "comment";

    
}
