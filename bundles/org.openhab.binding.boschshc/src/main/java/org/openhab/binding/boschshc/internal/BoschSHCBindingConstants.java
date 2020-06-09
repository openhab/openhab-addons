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
package org.openhab.binding.boschshc.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link BoschSHCBindingConstants} class defines common constants, which
 * are used across the whole binding.
 *
 * @author Stefan Kästle - Initial contribution
 */
@NonNullByDefault
public class BoschSHCBindingConstants {

    private static final String BINDING_ID = "boschshc";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SHC = new ThingTypeUID(BINDING_ID, "shc");

    public static final ThingTypeUID THING_TYPE_INWALL_SWITCH = new ThingTypeUID(BINDING_ID, "in-wall-switch");
    public static final ThingTypeUID THING_TYPE_TWINGUARD = new ThingTypeUID(BINDING_ID, "twinguard");
    public static final ThingTypeUID THING_TYPE_WINDOW_CONTACT = new ThingTypeUID(BINDING_ID, "window-contact");
    public static final ThingTypeUID THING_TYPE_MOTION_DETECTOR = new ThingTypeUID(BINDING_ID, "motion-detector");
    public static final ThingTypeUID THING_TYPE_SHUTTER_CONTROL = new ThingTypeUID(BINDING_ID, "shutter-control");

    // List of all Channel IDs
    // Auto-generated from thing-types.xml via script, don't modify
    public static final String CHANNEL_POWER_SWITCH = "power-switch";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_TEMPERATURE_RATING = "temperature-rating";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_HUMIDITY_RATING = "humidity-rating";
    public static final String CHANNEL_ENERGY_CONSUMPTION = "energy-consumption";
    public static final String CHANNEL_POWER_CONSUMPTION = "power-consumption";
    public static final String CHANNEL_PURITY = "purity";
    public static final String CHANNEL_AIR_DESCRIPTION = "air-description";
    public static final String CHANNEL_PURITY_RATING = "purity-rating";
    public static final String CHANNEL_COMBINED_RATING = "combined-rating";
    public static final String CHANNEL_CONTACT = "contact";
    public static final String CHANNEL_LATEST_MOTION = "latest-motion";
    public static final String CHANNEL_LEVEL = "level";
}
