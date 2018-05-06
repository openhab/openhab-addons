/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.feican;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

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
