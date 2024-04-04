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
package org.openhab.binding.ephemeris.internal;

import java.io.File;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.OpenHAB;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link EphemerisBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class EphemerisBindingConstants {

    private static final String BINDING_ID = "ephemeris";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FILE = new ThingTypeUID(BINDING_ID, "file");

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_EVENT_TITLE = "currentTitle";
    public static final String CHANNEL_NEXT_EVENT_TITLE = "nextTitle";
    public static final String CHANNEL_NEXT_EVENT_START = "nextStart";
    public static final String CHANNEL_NEXT_REMAINING = "remainingDays";

    public static final String BINDING_DATA_PATH = "%s%smisc%s%s".formatted(OpenHAB.getConfigFolder(), File.separator,
            File.separator, BINDING_ID);
}
