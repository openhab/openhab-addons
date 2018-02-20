/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
     * ZoneMinder Server Constants
     */

    // Thing Type UID for Server
    public static final ThingTypeUID THING_TYPE_BRIDGE_ZONEMINDER_SERVER = new ThingTypeUID(BINDING_ID,
            BRIDGE_ZONEMINDER_SERVER);

    // Shared channel for all bridges / things
    public static final String CHANNEL_ONLINE = "online";

    // Channel Id's for the ZoneMinder Server
    public static final String CHANNEL_SERVER_DISKUSAGE = "disk-usage";
    public static final String CHANNEL_SERVER_CPULOAD = "cpu-load";

    // Parameters for the ZoneMinder Server
    public static final String PARAM_HOSTNAME = "hostname";
    public static final String PARAM_PORT = "port";
    public static final String PARAM_REFRESH_INTERVAL_ = "refresh_interval";
    public static final String PARAM_REFRESH_INTERVAL_DISKUSAGE = "refresh_interval_disk_usage";

    // Default values for Monitor parameters
    public static final Integer DEFAULT_HTTP_PORT = 80;
    public static final Integer DEFAULT_TELNET_PORT = 6802;

    /*
     * ZoneMinder Monitor Constants
     */

    // Thing Type UID for Monitor
    public static final ThingTypeUID THING_TYPE_THING_ZONEMINDER_MONITOR = new ThingTypeUID(BINDING_ID,
            THING_ZONEMINDER_MONITOR);
    /*
     * Channel Id's for the ZoneMinder Monitor
     */
    public static final String CHANNEL_MONITOR_ENABLED = "enabled";
    public static final String CHANNEL_MONITOR_FORCE_ALARM = "force-alarm";
    public static final String CHANNEL_MONITOR_EVENT_STATE = "alarm";
    public static final String CHANNEL_MONITOR_EVENT_CAUSE = "event-cause";
    public static final String CHANNEL_MONITOR_RECORD_STATE = "recording";
    public static final String CHANNEL_MONITOR_DETAILED_STATUS = "detailed-status";
    public static final String CHANNEL_MONITOR_FUNCTION = "function";

    public static final String CHANNEL_MONITOR_CAPTURE_DAEMON_STATE = "capture-daemon";
    public static final String CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE = "analysis-daemon";
    public static final String CHANNEL_MONITOR_FRAME_DAEMON_STATE = "frame-daemon";

    // Parameters for the ZoneMinder Monitor
    public static final String PARAMETER_MONITOR_ID = "monitorId";
    public static final String PARAMETER_MONITOR_TRIGGER_TIMEOUT = "monitorTriggerTimeout";
    public static final String PARAMETER_MONITOR_EVENTTEXT = "monitorEventText";

    // Default values for Monitor parameters
    public static final Integer PARAMETER_MONITOR_TRIGGER_TIMEOUT_DEFAULTVALUE = 60;

    public static final String PARAMETER_MONITOR_EVENTNOTE_DEFAULTVALUE = "openHAB triggered event";

    // ZoneMinder Event types
    public static final String MONITOR_EVENT_NONE = "";
    public static final String MONITOR_EVENT_SIGNAL = "Signal";
    public static final String MONITOR_EVENT_MOTION = "Motion";
    public static final String MONITOR_EVENT_FORCED_WEB = "Forced Web";
    public static final String MONITOR_EVENT_OPENHAB = "openHAB";

}
