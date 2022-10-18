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
package org.openhab.binding.saicismart.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SAICiSMARTBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Heberling - Initial contribution
 */
@NonNullByDefault
public class SAICiSMARTBindingConstants {

    private static final String BINDING_ID = "saicismart";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_VEHICLE = new ThingTypeUID(BINDING_ID, "vehicle");

    // List of all Channel ids
    public static final String CHANNEL_MILAGE = "milage";
    public static final String CHANNEL_RANGE_ELECTRIC = "range-electric";
    public static final String CHANNEL_SOC = "soc";
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_CHARGING = "charging";
}
