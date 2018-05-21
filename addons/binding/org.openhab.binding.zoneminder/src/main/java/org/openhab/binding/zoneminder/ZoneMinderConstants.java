/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.zoneminder;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link ZoneMinderConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin S. Eskildsen - Initial contribution
 */
public class ZoneMinderConstants {

    @NonNull
    public static final String BINDING_ID = "zoneminder";

    // ZoneMinder Server Bridge
    @NonNull
    public static final String BRIDGE_ZONEMINDER_SERVER = "server";

    // ZoneMinder Monitor thing
    @NonNull
    public static final String THING_ZONEMINDER_MONITOR = "monitor";
    // ZoneMinder PTZControl thing
    @NonNull
    public static final String THING_ZONEMINDER_PTZCONTROL = "ptzcontrol";

    // ZoneMinder Server displayable name
    @NonNull
    public static final String ZONEMINDER_SERVER_NAME = "ZoneMinder Server";

    // ZoneMinder Monitor displayable name
    @NonNull
    public static final String ZONEMINDER_MONITOR_NAME = "ZoneMinder Monitor";

    /*
     * ZoneMinder Server Constants
     */

    // Thing Type UID for Server
    @NonNull
    public static final ThingTypeUID THING_TYPE_BRIDGE_ZONEMINDER_SERVER = new ThingTypeUID(BINDING_ID,
            BRIDGE_ZONEMINDER_SERVER);

    // Shared channel for all bridges / things
    @NonNull
    public static final String CHANNEL_ONLINE = "online";

    // Channel Id's for the ZoneMinder Server
    public static final String CHANNEL_SERVER_DISKUSAGE = "disk-usage";
    public static final String CHANNEL_SERVER_CPULOAD = "cpu-load";

    // Parameters for the ZoneMinder Server
    @NonNull
    public static final String PARAM_PROTOCOL = "protocol";
    @NonNull
    public static final String PARAM_HOST = "host";
    @NonNull
    public static final String PARAM_HTTP_PORT = "portHttp";
    @NonNull
    public static final String PARAM_TELNET_PORT = "portTelnet";

    @NonNull
    public static final String PARAM_USER = "user";
    @NonNull
    public static final String PARAM_URL_PASSWORD = "password";

    @NonNull
    public static final String PARAM_URL_SITE = "urlSite";
    @NonNull
    public static final String PARAM_URL_API = "urlApi";

    @NonNull
    public static final String PARAM_REFRESH_DISKUSAGE = "diskUsageRefresh";

    @NonNull
    public static final String PARAM_REFRESH_NORMAL = "refreshNormal";
    @NonNull
    public static final String PARAM_REFRESH_LOW = "refreshLow";
    @NonNull
    public static final String PARAM_AUTODICOVER = "autodiscover";

    @NonNull
    public static final String CONFIG_VALUE_REFRESH_BATCH = "batch";
    @NonNull
    public static final String CONFIG_VALUE_REFRESH_LOW = "low";
    @NonNull
    public static final String CONFIG_VALUE_REFRESH_NORMAL = "normal";
    @NonNull
    public static final String CONFIG_VALUE_REFRESH_HIGH = "high";
    @NonNull
    public static final String CONFIG_VALUE_REFRESH_ALARM = "alarm";
    @NonNull
    public static final String CONFIG_VALUE_REFRESH_DISABLED = "disabled";

    // Default values for Monitor parameters
    @NonNull
    public static final Integer DEFAULT_HTTP_PORT = 80;
    @NonNull
    public static final Integer DEFAULT_TELNET_PORT = 6802;

    /*
     * ZoneMinder Monitor Constants
     */

    // Thing Type UID for Monitor
    @NonNull
    public static final ThingTypeUID THING_TYPE_THING_ZONEMINDER_MONITOR = new ThingTypeUID(BINDING_ID,
            THING_ZONEMINDER_MONITOR);
    /*
     * Channel Id's for the ZoneMinder Monitor
     */
    @NonNull
    public static final String CHANNEL_MONITOR_ENABLED = "enabled";
    @NonNull
    public static final String CHANNEL_MONITOR_FORCE_ALARM = "force-alarm";
    @NonNull
    public static final String CHANNEL_MONITOR_EVENT_STATE = "alarm";
    @NonNull
    public static final String CHANNEL_MONITOR_EVENT_CAUSE = "event-cause";
    @NonNull
    public static final String CHANNEL_MONITOR_RECORD_STATE = "recording";
    @NonNull
    public static final String CHANNEL_MONITOR_MOTION_EVENT = "motion-event";
    @NonNull
    public static final String CHANNEL_MONITOR_DETAILED_STATUS = "detailed-status";
    @NonNull
    public static final String CHANNEL_MONITOR_FUNCTION = "function";

    @NonNull
    public static final String CHANNEL_MONITOR_CAPTURE_DAEMON_STATE = "capture-daemon";
    @NonNull
    public static final String CHANNEL_MONITOR_ANALYSIS_DAEMON_STATE = "analysis-daemon";
    @NonNull
    public static final String CHANNEL_MONITOR_FRAME_DAEMON_STATE = "frame-daemon";
    @NonNull
    public static final String CHANNEL_MONITOR_STILL_IMAGE = "image";
    @NonNull
    public static final String CHANNEL_MONITOR_VIDEOURL = "videourl";

    // Parameters for the ZoneMinder Monitor
    @NonNull
    public static final String PARAMETER_MONITOR_ID = "id";
    @NonNull
    public static final String PARAMETER_MONITOR_TRIGGER_TIMEOUT = "triggerTimeout";
    @NonNull
    public static final String PARAMETER_MONITOR_EVENTTEXT = "eventText";
    @NonNull
    public static final String PARAMETER_MONITOR_IMAGE_REFRESH = "imageRefresh";

    // Default values for Monitor parameters
    @NonNull
    public static final Integer PARAMETER_MONITOR_TRIGGER_TIMEOUT_DEFAULTVALUE = 60;

    public static final String PARAMETER_MONITOR_EVENTNOTE_DEFAULTVALUE = "openHAB triggered event";

    // ZoneMinder Event types
    @NonNull
    public static final String MONITOR_EVENT_NONE = "";
    @NonNull
    public static final String MONITOR_EVENT_SIGNAL = "Signal";
    @NonNull
    public static final String MONITOR_EVENT_MOTION = "Motion";
    @NonNull
    public static final String MONITOR_EVENT_FORCED_WEB = "Forced Web";
    @NonNull
    public static final String MONITOR_EVENT_OPENHAB = "openHAB";

    // Thing Type UID for PTZ Control
    @NonNull
    public static final ThingTypeUID THING_TYPE_THING_ZONEMINDER_PTZCONTROL = new ThingTypeUID(BINDING_ID,
            THING_ZONEMINDER_PTZCONTROL);
    /*
     * Dyncamic Channel Id's for the ZoneMinder PTZ Control
     */
    @NonNull
    public static final String CHANNEL_PTZCONTROL_PRESET = "Presets";

}
