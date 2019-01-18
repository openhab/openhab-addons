/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.lgwebos.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Sebastian Prehn - initial contribution
 */
@NonNullByDefault
public class LGWebOSBindingConstants {

    public static final String BINDING_ID = "lgwebos";

    public static final ThingTypeUID THING_TYPE_WEBOSTV = new ThingTypeUID(BINDING_ID, "WebOSTV");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_WEBOSTV);

    public static final String BINDING_CONFIGURATION_LOCALIP = "localIP";

    public static final String PROPERTY_DEVICE_ID = "deviceId";

    // List of all Channel ids. Values have to match ids in thing-types.xml
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_CHANNEL_NAME = "channelName";
    public static final String CHANNEL_TOAST = "toast";
    public static final String CHANNEL_MEDIA_PLAYER = "mediaPlayer";
    public static final String CHANNEL_MEDIA_STOP = "mediaStop";
    public static final String CHANNEL_APP_LAUNCHER = "appLauncher";
}
