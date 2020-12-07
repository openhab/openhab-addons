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
package org.openhab.binding.omatic.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OMaticBindingConstants} class defines common constants, which are
 * used across the State O-Matic binding.
 *
 * @author Joseph (Seaside) Hagberg - Initial contribution
 */
@NonNullByDefault
public class OMaticBindingConstants {

    public static final String BINDING_ID = "omatic";
    public static final ThingTypeUID THING_TYPE_MACHINE = new ThingTypeUID(BINDING_ID, "machine");
    public static final String PROPERTY_POWER_INPUT = "POWER_INPUT";
    public static final String PROPERTY_MAX_POWER = "MAX_POWER";
    public static final String PROPERTY_TOTAL_TIME = "TOTAL_TIME";
    public static final String PROPERTY_COMPLETED = "COMPLETED";
    public static final String PROPERTY_TOTAL_ENERGY = "TOTAL_MEASURED_ENERGY";
    public static final String PROPERTY_TOTAL_ESTIMATED_ENERGY = "TOTAL_ESTIMATED_ENERGY";
    public static final String PROPERTY_LAST_KNOWN_ENERGY_VALUE = "LAST_KNOWN_ENERGY_VALUE";
}
