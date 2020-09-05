/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.icalendar.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelGroupTypeUID;
import org.eclipse.smarthome.core.thing.type.ChannelTypeUID;

/**
 * The {@link ICalendarBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Michael Wodniok - Initial contribution
 */
@NonNullByDefault
public class ICalendarBindingConstants {

    public static final String BINDING_ID = "icalendar";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CALENDAR = new ThingTypeUID(BINDING_ID, "calendar");
    public static final ThingTypeUID THING_TYPE_FILTERED_EVENTS = new ThingTypeUID(BINDING_ID, "eventfilter");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_EVENT_TITLE = "current_title";
    public static final String CHANNEL_CURRENT_EVENT_START = "current_start";
    public static final String CHANNEL_CURRENT_EVENT_END = "current_end";
    public static final String CHANNEL_CURRENT_EVENT_PRESENT = "current_presence";
    public static final String CHANNEL_NEXT_EVENT_TITLE = "next_title";
    public static final String CHANNEL_NEXT_EVENT_START = "next_start";
    public static final String CHANNEL_NEXT_EVENT_END = "next_end";

    // additional constants
    public static final int HTTP_TIMEOUT_SECS = 60;
    public static final String DATETIME_UNIT_MINUTE = "minute";
    public static final String DATETIME_UNIT_HOUR = "hour";
    public static final String DATETIME_UNIT_DAY = "day";
    public static final String DATETIME_UNIT_WEEK = "week";

    // specials for EventFilter
    public final static String RESULT_GROUP_ID_PREFIX = "result_";
    public final static String RESULT_BEGIN_ID = "begin";
    public final static String RESULT_END_ID = "end";
    public final static String RESULT_TITLE_ID = "title";
    public final static ChannelGroupTypeUID GROUP_TYPE_UID = new ChannelGroupTypeUID(BINDING_ID, "result");
    public final static ChannelTypeUID BEGIN_TYPE_UID = new ChannelTypeUID(BINDING_ID, "result_start");
    public final static ChannelTypeUID END_TYPE_UID = new ChannelTypeUID(BINDING_ID, "result_end");
    public final static ChannelTypeUID TITLE_TYPE_UID = new ChannelTypeUID(BINDING_ID, "result_title");
}
