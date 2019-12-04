/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.philipsair.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link PhilipsAirBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Michal Boronski - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirBindingConstants {

    private static final String BINDING_ID = "philipsair";
    
    public static final String SUPPORTED_MODEL_UNIVERSAL = "universal";
    public static final String SUPPORTED_MODEL_NUMBER_AC2889_10 = "ac2889_10";
    public static final String SUPPORTED_MODEL_NUMBER_AC2729_10 = "ac2729_10";
    public static final String SUPPORTED_MODEL_NUMBER_AC2729_50 = "ac2729_50";
    public static final String SUPPORTED_MODEL_NUMBER_AC1214_10 = "ac1214_10";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UNIVERSAL = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_UNIVERSAL);
    public static final ThingTypeUID THING_TYPE_AC2889_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC2889_10);
    public static final ThingTypeUID THING_TYPE_AC2729_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC2729_10);
    public static final ThingTypeUID THING_TYPE_AC2729_50 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC2729_50);
    public static final ThingTypeUID THING_TYPE_AC1214_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC1214_10);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(
            Stream.of(THING_TYPE_UNIVERSAL, THING_TYPE_AC2889_10, THING_TYPE_AC2889_10, THING_TYPE_AC2729_50, THING_TYPE_AC1214_10)
                    .collect(Collectors.toSet()));

    public static final String DISCOVERY_UPNP_MODEL = "AirPurifier";

    public static final String DISCOVERY_UDN = "udn";

    // List of all Channel groups
    public static final String FILTERS = "filters";
    
    // List of all Channel id's
    /**
     * PM2.5 particles amount
     */
    public static final String PM25 = "pm25";
    /**
     * Power switch
     */
    public static final String POWER = "pwr";
    /**
     * Fan mode (s - silent, 1, 2, 3, t - turbo)
     */
    public static final String OM = "om";
    /**
     * Auto mode : P - auto, B - bacteria, M - manual, A - allergen, S - sleep, N -
     * night
     */
    public static final String MODE = "mode";
    /**
     * Buttons light
     */
    public static final String UIL = "uil";
    /**
     * Light birightness
     */
    public static final String AQIL = "aqil";
    /**
     * Index used to show air quality
     */
    public static final String DDP = "ddp";
    /**
     * Allergen index
     */
    public static final String IAQL = "iaql";

    public static final String AQIT = "aqit";
    /**
     * Child lock
     */
    public static final String CL = "cl";
    /**
     * Auto time-off
     */
    public static final String DT = "dt";
    /**
     * Error code
     */
    public static final String ERR = "err";
    /**
     * Current minutes left to turn off
     */
    public static final String DTRS = "dtrs";

    public static final String SWVERSION = "swversion";

    /**
     * Current humidity
     */
    public static final String RH = "rh";

    /**
     * Humidity setpoint
     */
    public static final String RHSET = "rhset";

    /**
     * Current temperature
     */
    public static final String TEMP = "temp";

    /**
     * 'P': 'Purification', 'PH': 'Purification & Humidification'
     */
    public static final String FUNC = "func";

    /**
     * water level
     */
    public static final String WL = "wl";

    /**
     * Pre-filter
     */
    public static final String PRE_FILTER = "fltsts0";

    /**
     * Wicks filter estimated lifetime
     */
    public static final String WICKS_FILTER = "wicksts";

    /**
     * Active carbon estimated lifetime
     */
    public static final String CARBON_FILTER = "fltsts2";

    /**
     * HEPA estimated lifetime
     */
    public static final String HEPA_FILTER = "fltsts1";
}
