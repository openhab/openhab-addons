/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.atlona;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link AtlonaBinding} class defines common constants, which are used across the whole binding.
 *
 * @author Tim Roberts
 */
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

}
