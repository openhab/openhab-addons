/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tailwind.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.type.ChannelGroupTypeUID;

/**
 * The {@link TailwindBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Dave J. Schoepel - Initial contribution
 * @author Christopher Smith - Reworked Thing type, channels and configuration
 */
@NonNullByDefault
public class TailwindBindingConstants {

    public static final String BINDING_ID = "tailwind";

    public static final ThingTypeUID THING_TYPE_IQ3 = new ThingTypeUID(BINDING_ID, "iq3");
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_IQ3);

    /** The product name the iQ3 reports in its status and its mDNS record. */
    public static final String PRODUCT_IQ3 = "iQ3";
    public static final String VENDOR = "TailWind";
    public static final int MAX_DOORS = 3;

    public static final String CONFIG_HOSTNAME = "hostname";

    public static final String GROUP_CONTROLLER = "controller";
    /** Door channel groups are door1 to door3. */
    public static final String GROUP_DOOR_PREFIX = "door";
    public static final ChannelGroupTypeUID GROUP_TYPE_DOOR = new ChannelGroupTypeUID(BINDING_ID, "door");

    public static final String CHANNEL_NIGHT_MODE = "night-mode";
    public static final String CHANNEL_LED_BRIGHTNESS = "led-brightness";
    public static final String CHANNEL_RSSI = "rssi";
    public static final String CHANNEL_SERVER_MONITOR = "server-monitor";
    public static final String CHANNEL_COMMAND = "command";

    public static final String CHANNEL_DOOR_STATUS = "status";
    public static final String CHANNEL_DOOR_CONTROL = "control";
    public static final String CHANNEL_DOOR_LOCKED = "locked";
    public static final String CHANNEL_DOOR_DISABLED = "disabled";

    public static final String COMMAND_IDENTIFY = "identify";
    public static final String COMMAND_REBOOT = "reboot";

    public static final String DOOR_OPEN = "open";
    public static final String DOOR_CLOSE = "close";
    public static final String DOOR_PARTIAL = "partial";

    private TailwindBindingConstants() {
    }
}
