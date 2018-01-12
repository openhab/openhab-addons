/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.lgwebos;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Sebastian Prehn - initial contribution
 */
public class LGWebOSBindingConstants {

    public static final @NonNull String BINDING_ID = "lgwebos";

    public static final ThingTypeUID THING_TYPE_WEBOSTV = new ThingTypeUID(BINDING_ID, "WebOSTV");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_WEBOSTV);

    // List of all Channel ids. Values have to match ids in thing-types.xml
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_TOAST = "toast";
    public static final String CHANNEL_CHANNEL_UP = "channelUp";
    public static final String CHANNEL_CHANNEL_DOWN = "channelDown";
    public static final String CHANNEL_CHANNEL_NAME = "channelName";
    public static final String CHANNEL_PROGRAM = "program";
    public static final String CHANNEL_MEDIA_STOP = "mediaStop";
    public static final String CHANNEL_APP_LAUNCHER = "appLauncher";
    public static final String CHANNEL_MEDIA_PLAYER = "mediaPlayer";
}
