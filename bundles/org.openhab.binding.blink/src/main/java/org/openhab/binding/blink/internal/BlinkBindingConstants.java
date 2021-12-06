/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * <p>
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 * <p>
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 * <p>
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.blink.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BlinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@NonNullByDefault
public class BlinkBindingConstants {
    private static final String BINDING_ID = "blink";
    // List of all Bridge Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");
    public static final ThingTypeUID THING_TYPE_NETWORK = new ThingTypeUID(BINDING_ID, "network");
    // List of all Channel ids
    public static final String CHANNEL_CAMERA_MOTIONDETECTION = "motiondetection";
    public static final String CHANNEL_CAMERA_BATTERY = "battery";
    public static final String CHANNEL_CAMERA_TEMPERATURE = "temperature";
    public static final String CHANNEL_CAMERA_SETTHUMBNAIL = "setThumbnail";
    public static final String CHANNEL_CAMERA_GETTHUMBNAIL = "getThumbnail";
    public static final String CHANNEL_NETWORK_ARMED = "armed";
    // List of property names
    public static final String PROPERTY_CAMERA_ID = "cameraId";

    public static final String PROPERTY_ACCOUNT_ID = "accountId";
    public static final String PROPERTY_CLIENT_ID = "clientId";
    public static final String PROPERTY_TIER = "tier";
    public static final String PROPERTY_TOKEN = "token";
    public static final String PROPERTY_NETWORK_ID = "networkId";
}
