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
package org.openhab.binding.novafinedust.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NovaFineDustBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Stefan Triller - Initial contribution
 */
@NonNullByDefault
public class NovaFineDustBindingConstants {

    private static final String BINDING_ID = "novafinedust";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_SDS011 = new ThingTypeUID(BINDING_ID, "SDS011");

    // List of all Channel ids
    public static final String CHANNEL_PM25 = "pm25";
    public static final String CHANNEL_PM10 = "pm10";
}
