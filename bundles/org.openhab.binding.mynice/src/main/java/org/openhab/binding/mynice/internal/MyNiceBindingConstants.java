/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MyNiceBindingConstants} class defines common constants, which are used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MyNiceBindingConstants {
    public static final String BINDING_ID = "mynice";

    // List of all Channel ids
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_OBSTRUCTED = "obstruct";
    public static final String CHANNEL_MOVING = "moving";
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_T4_COMMAND = "t4command";
    public static final String CHANNEL_COURTESY = "courtesy";

    // List of all Thing Type UIDs
    public static final ThingTypeUID BRIDGE_TYPE_IT4WIFI = new ThingTypeUID(BINDING_ID, "it4wifi");
    public static final ThingTypeUID THING_TYPE_SWING = new ThingTypeUID(BINDING_ID, "swing");
    public static final ThingTypeUID THING_TYPE_SLIDING = new ThingTypeUID(BINDING_ID, "sliding");

    // Configuration element of a portal
    public static final String DEVICE_ID = "id";

    public static final String ALLOWED_T4 = "allowedT4";
}
