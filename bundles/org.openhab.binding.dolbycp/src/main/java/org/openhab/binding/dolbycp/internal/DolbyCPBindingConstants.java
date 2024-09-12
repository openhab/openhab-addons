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
package org.openhab.binding.dolbycp.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DolbyCPBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Roland Tapken - Initial contribution
 */
@NonNullByDefault
public class DolbyCPBindingConstants {

    private static final String BINDING_ID = "dolbycp";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SAMPLE = new ThingTypeUID(BINDING_ID, "cp750");

    // List of all Channel ids
    public static final String CHANNEL_FADER = "fader";
    public static final String CHANNEL_MUTE = "mute";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_ANALOG = "analog";
    public static final String CHANNEL_DIG1 = "dig1";
    public static final String CHANNEL_DIG2 = "dig2";
    public static final String CHANNEL_DIG3 = "dig3";
    public static final String CHANNEL_DIG4 = "dig4";
    public static final String CHANNEL_NONSYNC = "nonsync";
    public static final String CHANNEL_MIC = "mic";

    // List of properties
    public static final String PROPERTY_VERSION = "osVersion";
}
