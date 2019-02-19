/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project.
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
package org.openhab.binding.sensibosky.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SensiboSkyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Robert Kaczmarczyk - Initial contribution
 */
@NonNullByDefault
public class SensiboSkyBindingConstants {

    private static final String BINDING_ID = "sensibosky";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_AC = new ThingTypeUID(BINDING_ID, "ac");

    // List of all Channel ids
    public static final String POWER = "power";
    public static final String CURRENT_TEMPERATURE = "current-temperature";
    public static final String HUMIDITY = "humidity";
    public static final String TARGET_TEMPERATURE = "target-temperature";
    public static final String FAN_MODE = "fan-mode";
    public static final String AC_MODE = "ac-mode";
    public static final String SWING_MODE = "swing-mode";
}
