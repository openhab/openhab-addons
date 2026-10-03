/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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

import java.util.Set;

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
 * @author Marcel Verpaalen - Add coap protocol support and update channel definitions
 */
@NonNullByDefault
public class PhilipsAirBindingConstants {

    public static final String BINDING_ID = "philipsair";
    public static final String VENDOR = "Philips";

    public static final String SUPPORTED_MODEL_UNIVERSAL = "universal";
    public static final String SUPPORTED_MODEL_NUMBER_AC2889_10 = "ac2889-10";
    public static final String SUPPORTED_MODEL_NUMBER_AC2729 = "ac2729";
    public static final String SUPPORTED_MODEL_NUMBER_AC1214_10 = "ac1214-10";
    public static final String SUPPORTED_MODEL_NUMBER_AC3829_10 = "ac3829-10";

    public static final String SUPPORTED_MODEL_COAP = "coap";

    /**
     * The product range of devices such as the AC3210, as reported by CoAP devices
     */
    public static final String RANGE_UNICORN = "Unicorn";

    /**
     * The device profile setting that detects the profile from the device
     */
    public static final String PROFILE_AUTO = "auto";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_UNIVERSAL = new ThingTypeUID(BINDING_ID, SUPPORTED_MODEL_UNIVERSAL);
    public static final ThingTypeUID THING_TYPE_COAP = new ThingTypeUID(BINDING_ID, SUPPORTED_MODEL_COAP);
    public static final ThingTypeUID THING_TYPE_AC2889_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC2889_10);
    public static final ThingTypeUID THING_TYPE_AC2729 = new ThingTypeUID(BINDING_ID, SUPPORTED_MODEL_NUMBER_AC2729);
    public static final ThingTypeUID THING_TYPE_AC1214_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC1214_10);
    public static final ThingTypeUID THING_TYPE_AC3829_10 = new ThingTypeUID(BINDING_ID,
            SUPPORTED_MODEL_NUMBER_AC3829_10);

    public static final Set<ThingTypeUID> SUPPORTED_UPNP_THING_TYPES_UIDS = Set.of(THING_TYPE_UNIVERSAL,
            THING_TYPE_AC2889_10, THING_TYPE_AC2729, THING_TYPE_AC1214_10, THING_TYPE_AC3829_10);

    public static final Set<ThingTypeUID> SUPPORTED_COAP_THING_TYPES_UIDS = Set.of(THING_TYPE_COAP);

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_UNIVERSAL, THING_TYPE_COAP,
            THING_TYPE_AC2889_10, THING_TYPE_AC2729, THING_TYPE_AC1214_10, THING_TYPE_AC3829_10);

    public static final String DISCOVERY_UPNP_MODEL = "AirPurifier";

    // Units of measurement of the data delivered by the API
    public static final Unit<Temperature> TEMPERATURE_UNIT = SIUnits.CELSIUS;
    public static final Unit<Dimensionless> HUMIDITY_UNIT = Units.PERCENT;
    public static final Unit<Density> DENSITY_UNIT = Units.MICROGRAM_PER_CUBICMETRE;

    // Thing Configuration Properties
    public static final String PROPERTY_DEV_TYPE = "deviceType";
    public static final String PROPERTY_MANUFACTURER = "manufacturer";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_PRE_FILTER_TYPE = "preFilterType";
    public static final String PROPERTY_HEPA_FILTER_TYPE = "hepaFilterType";
    public static final String PROPERTY_CARBON_FILTER_TYPE = "carbonFilterType";
    public static final String PROPERTY_DEVICE_PROFILE = "deviceProfile";

    // List of all Channel groups
    public static final String CONTROLS = "controls";
    public static final String CONTROLS_UI = "controls-ui";
    public static final String SENSORS = "sensors";
    public static final String FILTERS = "filters";

    // List of all Channel id's
    /**
     * PM2.5 particles amount
     */
    public static final String PM25 = "pm25";
    /**
     * Power switch
     */
    public static final String POWER = "power";
    /**
     * Fan speed (s - silent, 1, 2, 3, t - turbo)
     */
    public static final String FAN_MODE = "fan-speed";
    /**
     * Auto mode : P - auto, B - bacteria, M - manual, A - allergen, S - sleep, N -
     * night
     */
    public static final String MODE = "mode";
    /**
     * Buttons light
     */
    public static final String BUTTONS_LIGHT = "button-light";
    /**
     * Light brightness level
     */
    public static final String LED_LIGHT_LEVEL = "light-level";
    /**
     * Index used to show air quality
     */
    public static final String DISPLAYED_INDEX = "displayed-index";
    /**
     * Allergen index
     */
    public static final String ALLERGEN_INDEX = "allergen-index";

    public static final String AIR_QUALITY_NOTIFICATION_THRESHOLD = "air-quality-threshold";
    /**
     * Child lock
     */
    public static final String CHILD_LOCK = "child-lock";
    /**
     * Auto time-off
     */
    public static final String AUTO_TIMEOFF = "timer";
    /**
     * Error code
     */
    public static final String ERROR_CODE = "error-code";
    /**
     * Current minutes left to turn off
     */
    public static final String TIMER_COUNTDOWN = "timer-remaining";

    /**
     * Total volatile organic compounds level
     */
    public static final String TVOC = "tvoc";

    /**
     * Wi-Fi signal strength
     */
    public static final String RSSI = "rssi";

    /**
     * Current humidity
     */
    public static final String HUMIDITY = "humidity";

    /**
     * Humidity setpoint
     */
    public static final String HUMIDITY_SETPOINT = "target-humidity";

    /**
     * Current temperature
     */
    public static final String TEMPERATURE = "temperature";

    /**
     * 'P': 'Purification', 'PH': 'Purification & Humidification'
     */
    public static final String FUNCTION = "function";

    /**
     * water level
     */
    public static final String WATER_LEVEL = "water-level";

    /**
     * Pre-filter
     */
    public static final String PRE_FILTER = "pre-filter-life";

    /**
     * Wicks filter estimated lifetime
     */
    public static final String WICKS_FILTER = "wick-filter-life";

    /**
     * Active carbon estimated lifetime
     */
    public static final String CARBON_FILTER = "carbon-filter-life";

    /**
     * HEPA estimated lifetime
     */
    public static final String HEPA_FILTER = "hepa-filter-life";
}
