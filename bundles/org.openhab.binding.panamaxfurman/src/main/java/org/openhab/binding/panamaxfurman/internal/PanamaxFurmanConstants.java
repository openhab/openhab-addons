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

    public static final String PROPERTY_BRAND_NAME = "brand-name";
    public static final String PROPERTY_MODEL_NAME = "model-name";
    public static final String PROPERTY_FIRMWARE_VERSION = "firmware-version";

    // List of all Thing Type UIDs
    public static final String POWERCONDITIONER_THING_ID_FROM_XML = "powerconditioner";
    public static final ThingTypeUID POWERCONDITIONER_THING_TYPE = new ThingTypeUID(BINDING_ID,
            POWERCONDITIONER_THING_ID_FROM_XML);

    // List of all Channel names
    // outlet1#power
    public static final String POWER_CHANNEL = "power";

    // Misc
    public static final int MAX_DEVICE_OUTLET_COUNT = 8;

    public static final String GROUP_CHANNEL_PATTERN = "outlet%s#%s";
    public static final Pattern GROUP_CHANNEL_OUTLET_PATTERN = Pattern
            .compile("outlet([1-" + MAX_DEVICE_OUTLET_COUNT + "])#.*");
}
