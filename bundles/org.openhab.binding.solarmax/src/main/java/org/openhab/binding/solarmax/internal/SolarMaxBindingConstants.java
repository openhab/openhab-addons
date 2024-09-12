/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.solarmax.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SolarMaxBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jamie Townsend - Initial contribution
 */
@NonNullByDefault
public class SolarMaxBindingConstants {

    private static final String BINDING_ID = "solarmax";
    private static final String THING_TYPE_ID = "inverter";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SOLARMAX = new ThingTypeUID(BINDING_ID, THING_TYPE_ID);
}
