/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.ring;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RingBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
@NonNullByDefault
public class RingBindingConstants {

    public static final String BINDING_ID = "ring";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_DOORBELL = new ThingTypeUID(BINDING_ID, "doorbell");
    public static final ThingTypeUID THING_TYPE_CHIME = new ThingTypeUID(BINDING_ID, "chime");
    public static final ThingTypeUID THING_TYPE_STICKUPCAM = new ThingTypeUID(BINDING_ID, "stickupcam");
    public static final ThingTypeUID THING_TYPE_OTHERDEVICE = new ThingTypeUID(BINDING_ID, "otherdevice");

    // List of all Channel ids
    public static final String CHANNEL_CONTROL_STATUS = "control#status";
    public static final String CHANNEL_CONTROL_ENABLED = "control#enabled";

    public static final String CHANNEL_STATUS_BATTERY = "status#battery";

    public static final String CHANNEL_EVENT_URL = "event#url";
    public static final String CHANNEL_EVENT_CREATED_AT = "event#createdAt";
    public static final String CHANNEL_EVENT_KIND = "event#kind";
    public static final String CHANNEL_EVENT_DOORBOT_ID = "event#doorbotId";
    public static final String CHANNEL_EVENT_DOORBOT_DESCRIPTION = "event#doorbotDescription";

    public static final String SERVLET_VIDEO_PATH = "/ring/video";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_DOORBELL,
            THING_TYPE_CHIME, THING_TYPE_STICKUPCAM, THING_TYPE_OTHERDEVICE);

    public static final String THING_CONFIG_ID = "id";
    public static final String THING_PROPERTY_DESCRIPTION = "description";
    public static final String THING_PROPERTY_KIND = "kind";
    public static final String THING_PROPERTY_DEVICE_ID = "deviceId";
    public static final String THING_PROPERTY_OWNER_ID = "ownerId";
}
