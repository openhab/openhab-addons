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
package org.openhab.binding.robonect.internal;

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RobonectBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marco Meyer - Initial contribution
 */
public class RobonectBindingConstants {

    public static final String BINDING_ID = "robonect";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "mower");

    // List of all Channel ids
    public static final String CHANNEL_MOWER_NAME = "name";
    public static final String CHANNEL_STATUS_BATTERY = "battery";
    public static final String CHANNEL_STATUS_DURATION = "status-duration";
    public static final String CHANNEL_STATUS_DISTANCE = "status-distance";
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

    public static final String CHANNEL_BLADES_QUALITY = "blades-quality";
    public static final String CHANNEL_BLADES_REPL_DAYS = "blades-replacement-days";
    public static final String CHANNEL_BLADES_USAGE_HOURS = "blades-usage-hours";

    public static final String PROPERTY_COMPILED = "compiled";
    public static final String PROPERTY_COMMENT = "comment";
}
