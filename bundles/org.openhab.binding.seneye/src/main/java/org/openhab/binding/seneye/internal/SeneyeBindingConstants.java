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
package org.openhab.binding.seneye.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SeneyeBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Niko Tanghe - Initial contribution
 */
@NonNullByDefault
public class SeneyeBindingConstants {

    public static final String BINDING_ID = "seneye";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SENEYE = new ThingTypeUID(BINDING_ID, "monitor");

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_PH = "ph";
    public static final String CHANNEL_NH3 = "nh3";
    public static final String CHANNEL_NH4 = "nh4";
    public static final String CHANNEL_O2 = "O2";
    public static final String CHANNEL_LUX = "lux";
    public static final String CHANNEL_PAR = "par";
    public static final String CHANNEL_KELVIN = "kelvin";
    public static final String CHANNEL_LASTREADING = "lastreading";
    public static final String CHANNEL_SLIDEEXPIRES = "slideexpires";
    public static final String CHANNEL_WRONGSLIDE = "wrongslide";
    public static final String CHANNEL_SLIDESERIAL = "slideserial";
    public static final String CHANNEL_OUTOFWATER = "outofwater";
    public static final String CHANNEL_DISCONNECTED = "disconnected";
    // List of all Parameters
    public static final String PARAMETER_AQUARIUMNAME = "aquariumname";
    public static final String PARAMETER_USERNAME = "username";
    public static final String PARAMETER_PASSWORD = "password";
    public static final String PARAMETER_POLLTIME = "polltime";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SENEYE);
}
