/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.intesis.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link IntesisBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
@NonNullByDefault
public class IntesisBindingConstants {

    private static final String BINDING_ID = "intesis";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INTESISHOME = new ThingTypeUID(BINDING_ID, "intesisHome");

    // List of all Channel ids
    public static final String POWER_CHANNEL = "power";
    public static final String MODE_CHANNEL = "mode";
    public static final String WINDSPEED_CHANNEL = "windspeed";
    public static final String SWINGUD_CHANNEL = "swingUpDown";
    public static final String SWINGLR_CHANNEL = "swingLeftRight";
    public static final String TEMP_CHANNEL = "temperature";
    public static final String RETURNTEMP_CHANNEL = "returnTemp";
    public static final String OUTDOORTEMP_CHANNEL = "outdoorTemp";

    public static final int INTESIS_HTTP_API_TIMEOUT_MS = 5000;
}
