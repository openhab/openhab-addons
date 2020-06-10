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
package org.openhab.binding.meteoalerte.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link MeteoAlerteBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class MeteoAlerteBindingConstants {

    public static final String BINDING_ID = "meteoalerte";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_METEO_ALERT = new ThingTypeUID(BINDING_ID, "department");

    // List of all Channel id's
    public static final String AVALANCHE = "avalanches";
    public static final String HEAT = "canicule";
    public static final String FREEZE = "grand-froid";
    public static final String FLOOD = "inondation";
    public static final String SNOW = "neige";
    public static final String STORM = "orage";
    public static final String RAIN = "pluie-inondation";
    public static final String WIND = "vent";
    public static final String WIND_ICON = "vent-icon";
    public static final String RAIN_ICON = "pluie-inondation-icon";
    public static final String STORM_ICON = "orage-icon";
    public static final String FLOOD_ICON = "inondation-icon";
    public static final String SNOW_ICON = "neige-icon";
    public static final String HEAT_ICON = "canicule-icon";
    public static final String FREEZE_ICON = "grand-froid-icon";
    public static final String AVALANCHE_ICON = "avalanches-icon";
    public static final String OBSERVATION_TIME = "observation-time";
    public static final String COMMENT = "comment";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_METEO_ALERT);
}
