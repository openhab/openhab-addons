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
package org.openhab.binding.feican.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FeicanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
public final class FeicanBindingConstants {

    private static final String BINDING_ID = "feican";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BULB = new ThingTypeUID(BINDING_ID, "bulb");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_BULB);

    // List of all Channel ids
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "color_temperature";
    public static final String CHANNEL_PROGRAM = "program";
    public static final String CHANNEL_PROGRAM_SPEED = "program_speed";

    // List of al property ids
    public static final String CONFIG_IP = "ipAddress";
    public static final String PROPERTY_MAC = "mac";

    private FeicanBindingConstants() {
        // Constants class.
    }
}
