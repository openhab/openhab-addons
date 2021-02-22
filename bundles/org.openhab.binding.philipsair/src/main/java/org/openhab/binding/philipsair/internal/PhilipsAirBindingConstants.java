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
package org.openhab.binding.philipsair.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.measure.Unit;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.library.dimension.Density;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link PhilipsAirBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Michal Boronski - Initial contribution
 */
@NonNullByDefault
public class PhilipsAirBindingConstants {

    private static final String BINDING_ID = "philipsair";
    public static final String VENDOR = "Philips";

    public static final String SUPPORTED_MODEL_UNIVERSAL = "universal";
    public static final String SUPPORTED_MODEL_NUMBER_AC2889_10 = "ac2889_10";
    public static final String SUPPORTED_MODEL_NUMBER_AC2729 = "ac2729";
    public static final String SUPPORTED_MODEL_NUMBER_AC1214_10 = "ac1214_10";
    public static final String SUPPORTED_MODEL_NUMBER_AC3829_10 = "ac3829_10";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UNIVERSAL = new ThingTypeUID(BINDING_ID, SUPPORTED_MODEL_UNIVERSAL);
    public static final ThingTypeUID THING_TYPE_AC2889_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC2889_10);
    public static final ThingTypeUID THING_TYPE_AC2729 = new ThingTypeUID(BINDING_ID, SUPPORTED_MODEL_NUMBER_AC2729);
    public static final ThingTypeUID THING_TYPE_AC1214_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC1214_10);
    public static final ThingTypeUID THING_TYPE_AC3829_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC3829_10);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_UNIVERSAL, THING_TYPE_AC2889_10, THING_TYPE_AC2729,
                    THING_TYPE_AC1214_10, THING_TYPE_AC3829_10).collect(Collectors.toSet()));

    public static final String DISCOVERY_UPNP_MODEL = "AirPurifier";

    public static final String DISCOVERY_UDN = "udn";

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Density> DENSITY_UNIT = Units.MICROGRAM_PER_CUBICMETRE;

    // Thing Configuration Properties
    public static final String PROPERTY_DEV_TYPE = "deviceType";
    public static final String PROPERTY_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_NAME = "name";

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
    public static final String FAN_MODE = "om";
    /**
     * Auto mode : P - auto, B - bacteria, M - manual, A - allergen, S - sleep, N -
     * night
     */
    public static final String MODE = "mode";
    /**
     * Buttons light
     */
    public static final String BUTTONS_LIGHT = "uil";
    /**
     * Light birightness level
     */
    public static final String LED_LIGHT_LEVEL = "aqil";
    /**
     * Index used to show air quality
     */
    public static final String DISPLAYED_INDEX = "ddp";
    /**
     * Allergen index
     */
    public static final String ALLERGEN_INDEX = "iaql";

    public static final String AIR_QUALITY_NOTIFICATION_THRESHOLD = "aqit";
    /**
     * Child lock
     */
    public static final String CHILD_LOCK = "cl";
    /**
     * Auto time-off
     */
    public static final String AUTO_TIMEOFF = "dt";
    /**
     * Error code
     */
    public static final String ERROR_CODE = "err";
    /**
     * Current minutes left to turn off
     */
    public static final String TIMER_COUNTDOWN = "dtrs";

    public static final String SOFTWARE_VERSION = "swversion";

    /**
     * Current humidity
     */
    public static final String HUMIDITY = "rh";

    /**
     * Humidity setpoint
     */
    public static final String HUMIDITY_SETPOINT = "rhset";

    /**
     * Current temperature
     */
    public static final String TEMPERATURE = "temp";

    /**
     * 'P': 'Purification', 'PH': 'Purification & Humidification'
     */
    public static final String FUNCTION = "func";

    /**
     * water level
     */
    public static final String WATER_LEVEL = "wl";

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
