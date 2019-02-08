/**
 * Copyright (c) 2014,2019 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.facadelightcalculator.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link FacadeLightCalculatorBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class FacadeLightCalculatorBindingConstants {

    private static final String BINDING_ID = "facadelightcalculator";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_FACADE = new ThingTypeUID(BINDING_ID, "facade");

    // List of all Channel ids
    public static final String SUN_AZIMUTH = "sunAzimuth";
    public static final String FACADE_BEARING = "bearing";
    public static final String FACADE_FACING = "facingSun";
    public static final String FACADE_SIDE = "side";

    // event channelIds
    public static final String EVENT_FACADE = "facadeEvent";
    public static final String EVENT_ENTER_FACADE = "SUN_ENTER";
    public static final String EVENT_LEAVE_FACADE = "SUN_LEAVE";
    public static final String EVENT_FRONT_FACADE = "SUN_FRONT";

}
