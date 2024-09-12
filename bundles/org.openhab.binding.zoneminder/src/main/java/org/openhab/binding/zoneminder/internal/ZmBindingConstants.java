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
package org.openhab.binding.zoneminder.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link ZmBindingConstants} class defines common constants that are
 * used across the whole binding.
 *
 * @author Mark Hilbush - Initial contribution
 */
@NonNullByDefault
public class ZmBindingConstants {

    private static final String BINDING_ID = "zoneminder";

    // Bridge thing
    public static final String THING_TYPE_SERVER = "server";
    public static final ThingTypeUID UID_SERVER = new ThingTypeUID(BINDING_ID, THING_TYPE_SERVER);
    public static final Set<ThingTypeUID> SUPPORTED_SERVER_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_SERVER).collect(Collectors.toSet()));

    // Monitor things
    public static final String THING_TYPE_MONITOR = "monitor";
    public static final ThingTypeUID UID_MONITOR = new ThingTypeUID(BINDING_ID, THING_TYPE_MONITOR);

    // Collection of monitor thing types
    public static final Set<ThingTypeUID> SUPPORTED_MONITOR_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(UID_MONITOR).collect(Collectors.toSet()));

    // Collection of all supported thing types
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.concat(SUPPORTED_MONITOR_THING_TYPES_UIDS.stream(), SUPPORTED_SERVER_THING_TYPES_UIDS.stream())
                    .collect(Collectors.toSet()));

    // Config parameters
    // Server
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_PORT_NUMBER = "portNumber";
    public static final String CONFIG_URL_PATH = "urlPath";
    public static final String CONFIG_DEFAULT_ALARM_DURATION = "defaultAlarmDuration";
    public static final String CONFIG_DEFAULT_IMAGE_REFRESH_INTERVAL = "defaultImageRefreshInterval";

    // Monitor
    public static final String CONFIG_MONITOR_ID = "monitorId";
    public static final String CONFIG_IMAGE_REFRESH_INTERVAL = "imageRefreshInterval";
    public static final String CONFIG_ALARM_DURATION = "alarmDuration";

    public static final int DEFAULT_ALARM_DURATION_SECONDS = 60;
    public static final String DEFAULT_URL_PATH = "/zm";

    // List of all channel ids
    public static final String CHANNEL_RUN_STATE = "runState";
    public static final String CHANNEL_IMAGE_MONITOR_ID = "imageMonitorId";
    public static final String CHANNEL_VIDEO_MONITOR_ID = "videoMonitorId";
    public static final String CHANNEL_ID = "id";
    public static final String CHANNEL_NAME = "name";
    public static final String CHANNEL_IMAGE = "image";
    public static final String CHANNEL_FUNCTION = "function";
    public static final String CHANNEL_ENABLE = "enable";
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_TRIGGER_ALARM = "triggerAlarm";
    public static final String CHANNEL_HOUR_EVENTS = "hourEvents";
    public static final String CHANNEL_DAY_EVENTS = "dayEvents";
    public static final String CHANNEL_WEEK_EVENTS = "weekEvents";
    public static final String CHANNEL_MONTH_EVENTS = "monthEvents";
    public static final String CHANNEL_TOTAL_EVENTS = "totalEvents";
    public static final String CHANNEL_IMAGE_URL = "imageUrl";
    public static final String CHANNEL_VIDEO_URL = "videoUrl";
    public static final String CHANNEL_EVENT_ID = "eventId";
    public static final String CHANNEL_EVENT_NAME = "eventName";
    public static final String CHANNEL_EVENT_CAUSE = "eventCause";
    public static final String CHANNEL_EVENT_NOTES = "eventNotes";
    public static final String CHANNEL_EVENT_START = "eventStart";
    public static final String CHANNEL_EVENT_END = "eventEnd";
    public static final String CHANNEL_EVENT_FRAMES = "eventFrames";
    public static final String CHANNEL_EVENT_ALARM_FRAMES = "eventAlarmFrames";
    public static final String CHANNEL_EVENT_LENGTH = "eventLength";
}
