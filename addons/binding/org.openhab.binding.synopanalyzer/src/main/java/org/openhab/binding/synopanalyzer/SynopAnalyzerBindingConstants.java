/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.synopanalyzer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SynopAnalyzerBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class SynopAnalyzerBindingConstants {

    @NonNull
    public static final String BINDING_ID = "synopanalyzer";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_SYNOP = new ThingTypeUID(BINDING_ID, "synopanalyzer");

    // List of all Channel ids
    public static final String HORIZONTAL_VISIBILITY = "horizontal-visibility";
    public static final String OCTA = "octa";
    public static final String ATTENUATION_FACTOR = "attenuation-factor";
    public static final String OVERCAST = "overcast";
    public static final String PRESSURE = "pressure";
    public static final String TEMPERATURE = "temperature";
    public static final String WIND_ANGLE = "wind-angle";
    public static final String WIND_DIRECTION = "wind-direction";
    public static final String WIND_SPEED_MS = "wind-speed-ms";
    public static final String WIND_SPEED_KNOTS = "wind-speed-knots";
    public static final String WIND_SPEED_BEAUFORT = "wind-speed-beaufort";
    public static final String TIME_UTC = "time-utc";

}
