/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AutomowerBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Pfleger - Initial contribution
 * @author Marcin Czeczko - Added support for planner & calendar data
 */
@NonNullByDefault
public class AutomowerBindingConstants {
    private static final String BINDING_ID = "automower";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    // generic thing types
    public static final ThingTypeUID THING_TYPE_AUTOMOWER = new ThingTypeUID(BINDING_ID, "automower");

    // List of all Channel ids
    public static final String CHANNEL_STATUS_NAME = "name";
    public static final String CHANNEL_STATUS_MODE = "mode";
    public static final String CHANNEL_STATUS_ACTIVITY = "activity";
    public static final String CHANNEL_STATUS_STATE = "state";
    public static final String CHANNEL_STATUS_LAST_UPDATE = "last-update";
    public static final String CHANNEL_STATUS_BATTERY = "battery";
    public static final String CHANNEL_STATUS_ERROR_CODE = "error-code";
    public static final String CHANNEL_STATUS_ERROR_TIMESTAMP = "error-timestamp";
    public static final String CHANNEL_PLANNER_NEXT_START = "planner-next-start";
    public static final String CHANNEL_PLANNER_OVERRIDE_ACTION = "planner-override-action";
    public static final String CHANNEL_CALENDAR_TASKS = "calendar-tasks";

    // Command channels
    public static final String CHANNEL_COMMAND_START = "start";
    public static final String CHANNEL_COMMAND_RESUME_SCHEDULE = "resume_schedule";
    public static final String CHANNEL_COMMAND_PAUSE = "pause";
    public static final String CHANNEL_COMMAND_PARK = "park";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NEXT_SCHEDULE = "park_until_next_schedule";
    public static final String CHANNEL_COMMAND_PARK_UNTIL_NOTICE = "park_until_further_notice";

    // Automower properties
    public static final String AUTOMOWER_ID = "mowerId";
    public static final String AUTOMOWER_NAME = "mowerName";
    public static final String AUTOMOWER_MODEL = "mowerModel";
    public static final String AUTOMOWER_SERIAL_NUMBER = "mowerSerialNumber";
}
