/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.bosesoundtouch10;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BoseSoundTouch10Binding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author syracom - Initial contribution
 */
public class BoseSoundTouch10BindingConstants {

    public static final String BINDING_ID = "bosesoundtouch10";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SOUNDTOUCH10 = new ThingTypeUID(BINDING_ID, "bosesoundtouch10");

    // List of all Channel ids
    public final static String CHANNEL_POWER = "power";
    public final static String CHANNEL_VOLUME = "volume";
    public final static String CHANNEL_CONTROL = "control";
    public final static String CHANNEL_SOURCE = "source";
    public final static String CHANNEL_NOW_PLAYING = "now_playing";
    public final static String CHANNEL_BASS = "bass";
    public static final String DEVICEURL = "DEVICEURL";

}
