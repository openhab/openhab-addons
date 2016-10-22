/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/*
 *http://<SERVER>/zm/api/events/index/StartTime%20%3E:%202015-12-27%2023:00:00.json
 *
 *http://<SERVER>/zm/api/events/index/MonitorId =: 1.json
 * http://zoneminder.readthedocs.org/en/latest/api.html#examples-please-read-security-notice-above
 */

/**
 * The {@link ZoneMinderConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderConstants {

    public static final String BINDING_ID = "zoneminder";

    // ZoneMinder Server Bridge
    public static final String BRIDGE_ZONEMINDER_SERVER = "server";

    // ZoneMinder Monitor thing
    public static final String THING_ZONEMINDER_MONITOR = "monitor";

    // ZoneMinder Server displayable name
    public static final String ZONEMINDER_SERVER_NAME = "ZoneMinder Server";

    // ZoneMinder Monitor displayable name
    public static final String ZONEMINDER_MONITOR_NAME = "ZoneMinder Monitor";

    /*
     * ZoneMinder Server
     */

    // Thing Type UID for Server
    public final static ThingTypeUID THING_TYPE_BRIDGE_ZONEMINDER_SERVER = new ThingTypeUID(BINDING_ID,
            BRIDGE_ZONEMINDER_SERVER);

    // Channel Id's for the ZoneMinder Server
    public final static String CHANNEL_SERVER_ONLINE = "online";
    public final static String CHANNEL_SERVER_ZM_VERSION = "zm-version";
    public final static String CHANNEL_SERVER_ZM_API_VERSION = "zm-api-version";

    // Parameters for the ZoneMinder Server
    public final static String PARAMETER_HOSTNAME = "hostname";
    public final static String PARAMETER_PORT = "port";

    // Default values for Monitor parameters
    public static final Integer DEFAULT_HTTP_PORT = 80;
    public static final Integer DEFAULT_TELNET_PORT = 6802;

    /*
     * ZoneMinder Monitor
     */

    // Thing Type UID for Monitor
    public final static ThingTypeUID THING_TYPE_THING_ZONEMINDER_MONITOR = new ThingTypeUID(BINDING_ID,
            THING_ZONEMINDER_MONITOR);
    /*
     * Channel Id's for the ZoneMinder Monitor
     */
    public final static String CHANNEL_MONITOR_ID = "id";
    public final static String CHANNEL_MONITOR_NAME = "name";
    public final static String CHANNEL_MONITOR_SOURCETYPE = "sourcetype";
    public final static String CHANNEL_MONITOR_ENABLED = "enabled";
    public final static String CHANNEL_MONITOR_ONLINE = "online";
    // public final static String CHANNEL_MONITOR_EVENT = "event";

    // public final static String CHANNEL_MONITOR_IMAGE_WIDTH = "image-width";
    // public final static String CHANNEL_MONITOR_HEIGHT = "image-height";
    // public final static String CHANNEL_MONITOR_EVENTPREFIX = "eventprefix";
    // public final static String CHANNEL_MONITOR_ANALYSIS_FPS = "analysis-fps";
    // public final static String CHANNEL_MONITOR_MAX_FPS = "monitor-max-fps";
    // public final static String CHANNEL_MONITOR_ALARM_MAX_FPS = "alarm-max-fps";

    public final static String CHANNEL_MONITOR_TRIGGER_EVENT = "trigger-event";
    public final static String CHANNEL_MONITOR_FUNCTION = "function";
    // public final static String CHANNEL_MONITOR_DISK_USAGE = "disk-usage";
    public final static String CHANNEL_MONITOR_CAPTURE_DAEMON_STATE = "daemon-capture-state";
    public final static String CHANNEL_MONITOR_CAPTURE_DAEMON_STATUSTEXT = "daemon-capture-statustext";
    public final static String CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE = "daemon-analysis-state";
    public final static String CHANNEL_MONITOR_ANALYSIS_DAEMON_STATUSTEXT = "daemon-analysis-statustext";
    public final static String CHANNEL_MONITOR_FRAME_DAEMON_STATE = "daemon-frame-state";
    public final static String CHANNEL_MONITOR_FRAME_DAEMON_STATUSTEXT = "daemon-frame-statustext";

    // Parameters for the ZoneMinder Monitor
    public final static String PARAMETER_MONITOR_ID = "monitorId";
    public final static String PARAMETER_MONITOR_TRIGGER_TIMEOUT = "monitorTriggerTimeout";
    public final static String PARAMETER_MONITOR_EVENTTEXT = "monitorEventText";

    // Default values for Monitor parameters
    public final static Integer PARAMETER_MONITOR_TRIGGER_TIMEOUT_DEFAULTVALUE = 60;
    public final static String PARAMETER_MONITOR_EVENTTEXT_DEFAULTVALUE = "OpenHAB triggered event";

}
