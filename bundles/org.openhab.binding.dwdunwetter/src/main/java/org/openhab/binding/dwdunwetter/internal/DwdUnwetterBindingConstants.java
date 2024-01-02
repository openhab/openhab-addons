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
package org.openhab.binding.dwdunwetter.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link DwdUnwetterBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Martin Koehler - Initial contribution
 */
@NonNullByDefault
public class DwdUnwetterBindingConstants {

    public static final String BINDING_ID = "dwdunwetter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WARNINGS = new ThingTypeUID(BINDING_ID, "dwdwarnings");

    // Channels
    public static final String CHANNEL_LAST_UPDATED = "lastUpdated";

    // Channels per Warning
    public static final String CHANNEL_WARNING = "warning";
    public static final String CHANNEL_UPDATED = "updated";
    public static final String CHANNEL_SEVERITY = "severity";
    public static final String CHANNEL_DESCRIPTION = "description";
    public static final String CHANNEL_EFFECTIVE = "effective";
    public static final String CHANNEL_ONSET = "onset";
    public static final String CHANNEL_EXPIRES = "expires";
    public static final String CHANNEL_EVENT = "event";
    public static final String CHANNEL_HEADLINE = "headline";
    public static final String CHANNEL_ALTITUDE = "altitude";
    public static final String CHANNEL_CEILING = "ceiling";
    public static final String CHANNEL_INSTRUCTION = "instruction";
    public static final String CHANNEL_URGENCY = "urgency";
}
