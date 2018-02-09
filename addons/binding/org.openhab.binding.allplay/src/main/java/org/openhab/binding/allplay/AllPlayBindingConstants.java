/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.allplay;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AllPlayBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dominic Lerbs - Initial contribution
 */
public class AllPlayBindingConstants {

    public static final String BINDING_ID = "allplay";

    // List of all Thing Type UIDs
    public static final ThingTypeUID SPEAKER_THING_TYPE = new ThingTypeUID(BINDING_ID, "speaker");

    // List of all Channel ids
    public static final String CLEAR_ZONE = "clearzone";
    public static final String CONTROL = "control";
    public static final String CURRENT_ALBUM = "currentalbum";
    public static final String CURRENT_ARTIST = "currentartist";
    public static final String CURRENT_DURATION = "currentduration";
    public static final String CURRENT_GENRE = "currentgenre";
    public static final String CURRENT_TITLE = "currenttitle";
    public static final String CURRENT_URL = "currenturl";
    public static final String CURRENT_USER_DATA = "currentuserdata";
    public static final String INPUT = "input";
    public static final String LOOP_MODE = "loopmode";
    public static final String MUTE = "mute";
    public static final String PLAY_STATE = "playstate";
    public static final String SHUFFLE_MODE = "shufflemode";
    public static final String STOP = "stop";
    public static final String STREAM = "stream";
    public static final String COVER_ART = "coverart";
    public static final String COVER_ART_URL = "coverarturl";
    public static final String VOLUME = "volume";
    public static final String VOLUME_CONTROL = "volumecontrol";
    public static final String ZONE_ID = "zoneid";
    public static final String ZONE_MEMBERS = "zonemembers";

    // Config properties
    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_NAME = "deviceName";
    public static final String VOLUME_STEP_SIZE = "volumeStepSize";
}
