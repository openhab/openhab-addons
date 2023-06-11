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
package org.openhab.binding.milight.internal.protocol;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Implement this bulb interface for each new bulb type. It is used by {@see MilightLedHandler} to handle commands.
 *
 * @author David Graeff - Initial contribution
 */
@NonNullByDefault
public abstract class ProtocolConstants {
    // Print out a lot of useful debug data for the session establishing
    // This is for development purposes only, if the protocol changes again.
    public static final boolean DEBUG_SESSION = true;

    /**
     * There can only be one command of a category in the send queue (to avoid
     * having multiple on/off commands in the queue for example). You can assign
     * a category to each command you send and use one of the following constants.
     */
    // Session commands
    public static final int CAT_DISCOVER = 1;

    // Bulb commands
    public static final int CAT_BRIGHTNESS_SET = 10;
    public static final int CAT_SATURATION_SET = 11;
    public static final int CAT_COLOR_SET = 12;
    public static final int CAT_POWER_MODE = 13;
    public static final int CAT_TEMPERATURE_SET = 14;
    public static final int CAT_WHITEMODE = 17;
    public static final int CAT_MODE_SET = 18;
    public static final int CAT_SPEED_CHANGE = 19;
    public static final int CAT_LINK = 20;
}
