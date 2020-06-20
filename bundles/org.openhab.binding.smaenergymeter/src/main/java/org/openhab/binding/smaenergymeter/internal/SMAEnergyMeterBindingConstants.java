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
package org.openhab.binding.smaenergymeter.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SMAEnergyMeterBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Osman Basha - Initial contribution
 */
@NonNullByDefault
public class SMAEnergyMeterBindingConstants {

    public static final String BINDING_ID = "smaenergymeter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ENERGY_METER = new ThingTypeUID(BINDING_ID, "energymeter");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_ENERGY_METER);

    // List of all Channel IDs
    public static final String CHANNEL_POWER_IN = "powerIn";
    public static final String CHANNEL_POWER_OUT = "powerOut";
    public static final String CHANNEL_ENERGY_IN = "energyIn";
    public static final String CHANNEL_ENERGY_OUT = "energyOut";
}
