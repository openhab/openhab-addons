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
package org.openhab.binding.kvv.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link KVVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Maximilian Hess - Initial contribution
 */
@NonNullByDefault
public class KVVBindingConstants {

    private static final String BINDING_ID = "kvv";

    /** the thing type of the bridges */
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "kvvbridge");

    /** the thing type of the stations */
    public static final ThingTypeUID THING_TYPE_STATION = new ThingTypeUID(BINDING_ID, "kvvstation");

    /** all of the supported types */
    public static final List<ThingTypeUID> SUPPORTED_THING_TYPES = Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_STATION);

    /** URL of the KVV API */
    public static final String API_URL = "https://live.kvv.de/webapp";
}
