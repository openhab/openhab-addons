/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.tadoac;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link TadoACBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Fleck - Initial contribution
 */
public class TadoACBindingConstants {

    private static final String BINDING_ID = "tadoac";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AIRCONDITIONER = new ThingTypeUID(BINDING_ID, "airconditioner");

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_TEMP = "temperature";
    public static final String CHANNEL_FANSPEED = "fanspeed";

}
