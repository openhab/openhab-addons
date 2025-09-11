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
package org.openhab.binding.sbus;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BindingConstants} class defines common constants used across the Sbus binding.
 *
 * @author Ciprian Pascu - Initial contribution
 */
@NonNullByDefault
public class BindingConstants {

    private BindingConstants() {
        // Prevent instantiation
    }

    public static final String BINDING_ID = "sbus";

    // Bridge Type
    public static final ThingTypeUID THING_TYPE_UDP_BRIDGE = new ThingTypeUID(BINDING_ID, "udp");

    // Thing Types
    public static final ThingTypeUID THING_TYPE_SWITCH = new ThingTypeUID(BINDING_ID, "switch");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public static final ThingTypeUID THING_TYPE_RGBW = new ThingTypeUID(BINDING_ID, "rgbw");
    public static final ThingTypeUID THING_TYPE_CONTACT = new ThingTypeUID(BINDING_ID, "contact");

    // Channel IDs for Switch Device
    public static final String CHANNEL_SWITCH_STATE = "state";

    // Channel IDs for Temperature Device
    public static final String CHANNEL_TEMPERATURE = "temperature";

    // Channel IDs for RGBW Device
    public static final String CHANNEL_RED = "red";
    public static final String CHANNEL_GREEN = "green";
    public static final String CHANNEL_BLUE = "blue";
    public static final String CHANNEL_WHITE = "white";
    public static final String CHANNEL_COLOR = "color";

    // Channel Types
    public static final String CHANNEL_TYPE_COLOR = "color-channel";
    public static final String CHANNEL_TYPE_SWITCH = "switch-channel";
    public static final String CHANNEL_TYPE_DIMMER = "dimmer-channel";
    public static final String CHANNEL_TYPE_PAIRED = "paired-channel";
    public static final String CHANNEL_TYPE_CONTACT = "contact-channel";
}
