/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.seneye;

import java.util.Collections;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SeneyeBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Niko Tanghe - Initial contribution
 */
public class SeneyeBindingConstants {

    public static final String BINDING_ID = "seneye";

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_SENEYE = new ThingTypeUID(BINDING_ID, "seneye-thing");

    // List of all Channel ids
    public final static String CHANNEL_TEMPERATURE = "channeltemperature";
    public final static String CHANNEL_PH = "channelph";
    public final static String CHANNEL_NH3 = "channelnh3";
    public final static String CHANNEL_NH4 = "channelnh4";
    public final static String CHANNEL_O2 = "channelO2";
    public final static String CHANNEL_LUX = "channellux";
    public final static String CHANNEL_PAR = "channelpar";
    public final static String CHANNEL_KELVIN = "channelkelvin";
    public final static String CHANNEL_LASTREADING = "channellastreading";
    public final static String CHANNEL_SLIDEEXPIRES = "channelslideexpires";

    // List of all Parameters
    public final static String PARAMETER_AQUARIUMNAME = "aquariumname";
    public final static String PARAMETER_USERNAME = "username";
    public final static String PARAMETER_PASSWORD = "password";
    public final static String PARAMETER_POLLTIME = "polltime";

    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_SENEYE);

}
