/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RingBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Wim Vissers - Initial contribution
 */
public class RingBindingConstants {

    public static final String BINDING_ID = "ring";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public final static ThingTypeUID THING_TYPE_DOORBELL = new ThingTypeUID(BINDING_ID, "doorbell");
    public final static ThingTypeUID THING_TYPE_CHIME = new ThingTypeUID(BINDING_ID, "chime");
    public final static ThingTypeUID THING_TYPE_STICKUPCAM = new ThingTypeUID(BINDING_ID, "stickupcam");

    // List of all Channel ids
    public final static String CHANNEL_CONTROL_STATUS = "control#status";
    public final static String CHANNEL_CONTROL_ENABLED = "control#enabled";

    public final static String CHANNEL_STATUS_BATTERY = "status#battery";

    public final static String CHANNEL_EVENT_URL = "event#url";
    public final static String CHANNEL_EVENT_CREATED_AT = "event#createdAt";
    public final static String CHANNEL_EVENT_KIND = "event#kind";
    public final static String CHANNEL_EVENT_DOORBOT_ID = "event#doorbotId";
    public final static String CHANNEL_EVENT_DOORBOT_DESCRIPTION = "event#doorbotDescription";

    public static final String SERVLET_VIDEO_PATH = "/ring/video";
}
