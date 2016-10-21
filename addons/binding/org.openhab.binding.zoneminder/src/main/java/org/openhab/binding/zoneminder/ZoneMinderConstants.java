/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/*
 * List all Monitors
 * http://srv-zoneminder/zm/api/monitors.json
 *
 *API Version
 *http://srv-zoneminder/zm/api/host/getVersion.json
 *
 *http://srv-zoneminder/zm/api/events/index/StartTime%20%3E:%202015-12-27%2023:00:00.json
 *
 *http://srv-zoneminder/zm/api/events/index/MonitorId =: 1.json
 * http://zoneminder.readthedocs.org/en/latest/api.html#examples-please-read-security-notice-above
 */

/**
 * The {@link ZoneMinderBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderConstants {

    // public static final String ZONEMINDER_API_URI = "zm/api/";
    // public static final String ZONEMINDER_MONITORS_URI = "monitors/";

    public static final String BINDING_ID = "zoneminder";

    // List of all Bridges
    public static final String BRIDGE_ZONEMINDER_SERVER = "zoneminder-server-bridge";

    // List of all Things
    public static final String THING_ZONEMINDER_MONITOR = "zoneminder-monitor-thing";

    // List of all Channel ids
    public final static String CHANNEL_SERVER_ONLINE = "online";
    public final static String CHANNEL_SERVER_ZM_VERSION = "zm-version";
    public final static String CHANNEL_SERVER_ZM_API_VERSION = "zm-api-version";

    public final static String CHANNEL_MONITOR_ID = "id";
    public final static String CHANNEL_MONITOR_NAME = "name";
    public final static String CHANNEL_MONITOR_TYPE = "type";
    public final static String CHANNEL_MONITOR_ENABLED = "enabled";
    public final static String CHANNEL_MONITOR_ONLINE = "online";
    public final static String CHANNEL_MONITOR_EVENT = "event";

    public final static String CHANNEL_MONITOR_IMAGE_WIDTH = "image-width";
    public final static String CHANNEL_MONITOR_HEIGHT = "image-height";
    public final static String CHANNEL_MONITOR_EVENTPREFIX = "eventprefix";
    public final static String CHANNEL_MONITOR_ANALYSIS_FPS = "analysis-fps";
    public final static String CHANNEL_MONITOR_MAX_FPS = "monitor-max-fps";
    public final static String CHANNEL_MONITOR_ALARM_MAX_FPS = "alarm-max-fps";

    public final static String CHANNEL_MONITOR_TRIGGER_EVENT = "trigger-event";
    public final static String CHANNEL_MONITOR_FUNCTION = "function";
    public final static String CHANNEL_MONITOR_DISK_USAGE = "disk-usage";
    public final static String CHANNEL_MONITOR_CAPTURE_DAEMON_STATE = "daemon-capture-state";
    public final static String CHANNEL_MONITOR_CAPTURE_DAEMON_STATUSTEXT = "daemon-capture-statustext";
    public final static String CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE = "daemon-analysis-state";
    public final static String CHANNEL_MONITOR_ANALYSIS_DAEMON_STATUSTEXT = "daemon-analysis-statustext";
    public final static String CHANNEL_MONITOR_FRAME_DAEMON_STATE = "daemon-frame-state";
    public final static String CHANNEL_MONITOR_FRAME_DAEMON_STATUSTEXT = "daemon-frame-statustext";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE_ZONEMINDER_SERVER = new ThingTypeUID(BINDING_ID,
            BRIDGE_ZONEMINDER_SERVER);
    public final static ThingTypeUID THING_TYPE_THING_ZONEMINDER_MONITOR = new ThingTypeUID(BINDING_ID,
            THING_ZONEMINDER_MONITOR);

    public static final Integer DEFAULT_HTTP_PORT = 80;
    public static final Integer DEFAULT_TELNET_PORT = 6802;

    // List of all Parameters
    public final static String PARAMETER_HOSTNAME = "hostname";
    public final static String PARAMETER_PORT = "port";

    public final static String PARAMETER_MONITOR_ID = "monitorId";
    public final static String PARAMETER_MONITOR_TRIGGER_TIMEOUT = "monitorTriggerTimeout";
    public final static String PARAMETER_MONITOR_EVENTTEXT = "monitorEventText";

    public final static Integer PARAMETER_MONITOR_TRIGGER_TIMEOUT_DEFAULTVALUE = 60;
    public final static String PARAMETER_MONITOR_EVENTTEXT_DEFAULTVALUE = "OpenHAB triggered event";
    /*
     * public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
     * .singleton(THING_TYPE_BRIDGE_ZONEMINDER_SERVER);
     *
     * public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Collections
     * .singleton(THING_TYPE_BRIDGE_ZONEMINDER_SERVER);
     */
}
