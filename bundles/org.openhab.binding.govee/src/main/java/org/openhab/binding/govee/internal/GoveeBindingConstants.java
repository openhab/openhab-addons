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
package org.openhab.binding.govee.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link GoveeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Höhn - Initial contribution
 */
@NonNullByDefault
public class GoveeBindingConstants {

    // Thing properties
    public static final String MAC_ADDRESS = "macAddress";
    public static final String IP_ADDRESS = "hostname";
    public static final String DEVICE_TYPE = "deviceType";
    public static final String PRODUCT_NAME = "productName";
    private static final String BINDING_ID = "govee";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "govee-light");

    // List of all Channel ids
    public static final String SWITCH = "switch";
    public static final String COLOR = "color";
    public static final String COLOR_TEMPERATURE = "color-temperature";
    public static final int COLOR_TEMPERATURE_MIN_VALUE = 2000;
    public static final int COLOR_TEMPERATURE_MAX_VALUE = 9000;
    public static final String COLOR_TEMPERATURE_ABS = "color-temperature-abs";
    public static final String BRIGHTNESS = "brightness";
}
