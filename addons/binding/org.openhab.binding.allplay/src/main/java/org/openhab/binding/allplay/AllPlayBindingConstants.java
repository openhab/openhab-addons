/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
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
    public final static ThingTypeUID SPEAKER_THING_TYPE = new ThingTypeUID(BINDING_ID, "speaker");

    // List of all Channel ids
    public final static String CONTROL = "control";
    public final static String CURRENT_ALBUM = "currentalbum";
    public final static String CURRENT_ARTIST = "currentartist";
    public final static String CURRENT_DURATION = "currentduration";
    public final static String CURRENT_GENRE = "currentgenre";
    public final static String CURRENT_TITLE = "currenttitle";
    public final static String CURRENT_URL = "currenturl";
    public final static String CURRENT_USER_DATA = "currentuserdata";
    public final static String LOOP_MODE = "loopmode";
    public final static String MUTE = "mute";
    public final static String PLAY_STATE = "playstate";
    public final static String SHUFFLE_MODE = "shufflemode";
    public final static String STOP = "stop";
    public final static String STREAM = "stream";
    public final static String COVER_ART = "coverart";
    public final static String COVER_ART_URL = "coverarturl";
    public final static String VOLUME = "volume";
    public final static String VOLUME_CONTROL = "volumecontrol";
    public final static String ZONE_ID = "zoneid";

    // Config properties
    public static final String DEVICE_ID = "deviceId";
    public static final String DEVICE_NAME = "deviceName";
    public static final String VOLUME_STEP_SIZE = "volumeStepSize";
}
