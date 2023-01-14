/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.meteoalerte.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MeteoAlerteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoAlerteBindingConstants {

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_METEO_ALERT = new ThingTypeUID("meteoalerte", "department");

    // List of all Channel id's
    public static final String WAVE = "vague-submersion";
    public static final String AVALANCHE = "avalanches";
    public static final String HEAT = "canicule";
    public static final String FREEZE = "grand-froid";
    public static final String FLOOD = "inondation";
    public static final String SNOW = "neige";
    public static final String STORM = "orage";
    public static final String RAIN = "pluie-inondation";
    public static final String WIND = "vent";
    public static final String OBSERVATION_TIME = "observation-time";
    public static final String END_TIME = "end-time";
    public static final String COMMENT = "comment";
}
