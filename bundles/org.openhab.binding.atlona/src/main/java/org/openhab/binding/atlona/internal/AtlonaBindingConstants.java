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
package org.openhab.binding.atlona.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AtlonaBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Tim Roberts - Initial contribution
 * @author Michael Lobstein - Add support for AT-PRO3HD 44/66 M
 */
@NonNullByDefault
public class AtlonaBindingConstants {

    /**
     * The binding identifier for atlona
     */
    public static final String BINDING_ID = "atlona";

    /**
     * Thing ID for the AT-UHD-PRO3-44m (4x4 hdbaset matrix)
     */
    public static final ThingTypeUID THING_TYPE_PRO3_44M = new ThingTypeUID(BINDING_ID, "pro3-44m");

    /**
     * Thing ID for the AT-UHD-PRO3-66m (6x6 hdbaset matrix)
     */
    public static final ThingTypeUID THING_TYPE_PRO3_66M = new ThingTypeUID(BINDING_ID, "pro3-66m");

    /**
     * Thing ID for the AT-UHD-PRO3-88m (8x8 hdbaset matrix)
     */
    public static final ThingTypeUID THING_TYPE_PRO3_88M = new ThingTypeUID(BINDING_ID, "pro3-88m");

    /**
     * Thing ID for the AT-UHD-PRO3-1616m (16x16 hdbaset matrix)
     */
    public static final ThingTypeUID THING_TYPE_PRO3_1616M = new ThingTypeUID(BINDING_ID, "pro3-1616m");

    /**
     * Thing ID for the AT-PRO3HD44M (HD 4x4 hdbaset matrix)
     */
    public static final ThingTypeUID THING_TYPE_PRO3HD_44M = new ThingTypeUID(BINDING_ID, "pro3-hd44m");

    /**
     * Thing ID for the AT-PRO3HD66M (HD 6x6 hdbaset matrix)
     */
    public static final ThingTypeUID THING_TYPE_PRO3HD_66M = new ThingTypeUID(BINDING_ID, "pro3-hd66m");
}
