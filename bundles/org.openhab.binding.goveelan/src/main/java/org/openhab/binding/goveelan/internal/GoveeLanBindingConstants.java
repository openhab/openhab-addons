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
package org.openhab.binding.goveelan.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GoveeLanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeLanBindingConstants {

    private static final String BINDING_ID = "goveelan";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "govee-light");

    // List of all Channel ids
    public static final String SWITCH = "switch";
    public static final String COLOR = "color";
    public static final String COLOR_TEMPERATURE_ABS = "color-temperature-abs";
    public static final String BRIGHTNESS = "brightness";
}
