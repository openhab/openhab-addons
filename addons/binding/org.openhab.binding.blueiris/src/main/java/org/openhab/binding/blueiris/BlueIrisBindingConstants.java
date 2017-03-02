/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.blueiris;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link blueirisBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
public class BlueIrisBindingConstants {
    public static final String BINDING_ID = "blueiris";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public final static ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");

    // List of all Channel ids
    public final static String CHANNEL_PROFILE = "profile";
    public final static String CHANNEL_ENABLED = "enabled";
    public final static String CHANNEL_AUDIO_SUPPORTED = "audioSupported";
    public final static String CHANNEL_FPS = "fps";
    public final static String CHANNEL_COLOR = "color";
    public final static String CHANNEL_WEBCAST = "webCast";
    public final static String CHANNEL_ALERTING = "alerting";
    public final static String CHANNEL_MOTION = "motion";
    public final static String CHANNEL_NO_SIGNAL = "noSignal";
    public final static String CHANNEL_PAUSED = "paused";
    public final static String CHANNEL_PAUSED_TYPE = "pausedType";
    public final static String CHANNEL_TRIGGERED = "triggered";
    public final static String CHANNEL_RECORDING = "recording";
    public final static String CHANNEL_CAUTION_ICON = "cautionIcon";
    public final static String CHANNEL_PTZ_SUPPORTED = "ptzSupported";
    public final static String CHANNEL_WIDTH = "width";
    public final static String CHANNEL_HEIGHT = "height";
    public final static String CHANNEL_NEW_ALERTS = "newAlerts";
    public final static String CHANNEL_LAST_ALERT = "lastAlert";
    public final static String CHANNEL_NUMBER_OF_ALERTS = "numberOfAlerts";
    public final static String CHANNEL_NUMBER_OF_TRIGGERS = "numberOfTriggers";
    public final static String CHANNEL_NUMBER_OF_NO_SIGNAL = "numberOfNoSignal";
    public final static String CHANNEL_NUMBER_OF_CLIPS = "numberOfClips";
    public final static String CHANNEL_MOTION_ENABLED = "motionEnabled";
    public final static String CHANNEL_SCHEDULE_ENABLED = "scheduleEnabled";
    public final static String CHANNEL_PTZ_CYCLE = "ptzCycle";
    public final static String CHANNEL_PTZ_EVENTS = "ptzEvents";

    // Bridge channels
    public final static String CHANNEL_GLOBAL_SCHEDULE = "globalSchedule";
    public final static String CHANNEL_WEB_ARCHIVE = "webArchive";

    // Properties.
    public final static String PROPERTY_SHORT_NAME = "shortName";
}
