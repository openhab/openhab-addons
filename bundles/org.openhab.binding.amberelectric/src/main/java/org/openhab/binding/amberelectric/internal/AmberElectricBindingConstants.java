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
package org.openhab.binding.amberelectric.internal;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link AmberElectricBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Paul Smedley - Initial contribution
 */
@NonNullByDefault
public class AmberElectricBindingConstants {

    private static final String BINDING_ID = "amberelectric";

    // List of all Thing Type UIDs
    public static final ThingTypeUID AMBERELECTRIC_THING = new ThingTypeUID(BINDING_ID, "amberelectric");

    // List of all Channel ids
    public static final String CHANNEL_AMBERELECTRIC_ELECPRICE = "elecprice";
    public static final String CHANNEL_AMBERELECTRIC_NEMTIME = "nemtime";
    public static final String CHANNEL_AMBERELECTRIC_RENEWABLES = "renewables";
    public static final String CHANNEL_AMBERELECTRIC_SPIKE = "spike";
    public static final String CHANNEL_AMBERELECTRIC_SPOTPRICE = "spotprice";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(AMBERELECTRIC_THING);
}
