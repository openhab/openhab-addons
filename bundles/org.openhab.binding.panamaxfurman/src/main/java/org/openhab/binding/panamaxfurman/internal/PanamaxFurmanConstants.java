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
package org.openhab.binding.panamaxfurman.internal;

import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Defines common constants, which are used across the whole binding.
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanamaxFurmanConstants {

    private static final String BINDING_ID = "panamaxfurman";

    // List of all Thing Type UIDs
    public static final ThingTypeUID TELNET_PM_THING_TYPE = new ThingTypeUID(BINDING_ID, "telnet");

    public static final String PARAMETER_HOST = "address";
    public static final String PARAMETER_TELNET_PORT = "telnetPort";

    // List of all Channel names
    // outlet1#power
    public static final String POWER_CHANNEL = "power";

    // Misc
    public static final int MAX_DEVICE_OUTLET_COUNT = 8;

    public static final String GROUP_CHANNEL_PATTERN = "outlet%s#%s";
    public static final Pattern GROUP_CHANNEL_OUTLET_PATTERN = Pattern
            .compile("outlet([1-" + MAX_DEVICE_OUTLET_COUNT + "])#.*");
}
