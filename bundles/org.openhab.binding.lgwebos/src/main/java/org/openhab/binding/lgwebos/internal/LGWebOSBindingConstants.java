/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.jupnp.model.types.ServiceType;
import org.openhab.core.thing.ThingTypeUID;

/**
 * This class defines common constants, which are used across the whole binding.
 *
 * @author Sebastian Prehn - Initial contribution
 */
@NonNullByDefault
public class LGWebOSBindingConstants {

    public static final String BINDING_ID = "lgwebos";

    public static final ThingTypeUID THING_TYPE_WEBOSTV = new ThingTypeUID(BINDING_ID, "WebOSTV");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_WEBOSTV);

    public static final ServiceType UPNP_SERVICE_TYPE = new ServiceType("lge-com", "webos-second-screen", 1);

    /*
     * Config names must match property names in
     * - WebOSConfiguration
     * - parameter names in OH-INF/config/config.xml
     * - property names in OH-INF/thing/thing-types.xml
     */
    public static final String CONFIG_HOST = "host";
    public static final String CONFIG_KEY = "key";
    public static final String CONFIG_MAC_ADDRESS = "macAddress";

    /*
     * Property names must match property names in
     * - property names in OH-INF/thing/thing-types.xml
     */
    public static final String PROPERTY_DEVICE_ID = "deviceId";
    public static final String PROPERTY_DEVICE_OS = "deviceOS";
    public static final String PROPERTY_DEVICE_OS_VERSION = "deviceOSVersion";
    public static final String PROPERTY_DEVICE_OS_RELEASE_VERSION = "deviceOSReleaseVersion";
    public static final String PROPERTY_LAST_CONNECTED = "lastConnected";

    /*
     * List of all Channel ids.
     * Values have to match ids in thing-types.xml
     */
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_CHANNEL = "channel";
    public static final String CHANNEL_TOAST = "toast";
    public static final String CHANNEL_MEDIA_PLAYER = "mediaPlayer";
    public static final String CHANNEL_MEDIA_STOP = "mediaStop";
    public static final String CHANNEL_APP_LAUNCHER = "appLauncher";
    public static final String CHANNEL_RCBUTTON = "rcButton";
}
