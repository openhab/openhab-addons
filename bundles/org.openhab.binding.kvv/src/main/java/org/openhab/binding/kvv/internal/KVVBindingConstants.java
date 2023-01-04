/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
import org.openhab.core.thing.ThingTypeUID;

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
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");

    /** the thing type of the stop */
    public static final ThingTypeUID THING_TYPE_STOP = new ThingTypeUID(BINDING_ID, "stop");

    /** all of the supported types */
    public static final List<ThingTypeUID> SUPPORTED_THING_TYPES = Arrays.asList(THING_TYPE_BRIDGE, THING_TYPE_STOP);

    /** URL of the KVV API */
    public static final String API_FORMAT = "https://projekte.kvv-efa.de/sl3-alone/XSLT_DM_REQUEST?outputFormat=JSON&coordOutputFormat=WGS84%%5Bdd.ddddd%%5D&depType=stopEvents&locationServerActive=1&mode=direct&name_dm=%s&type_dm=stop&useOnlyStops=1&useRealtime=1&limit=%d";

    /** timeout for API calls in seconds */
    public static final int TIMEOUT_IN_SECONDS = 10;

    /** default value to initialize the API cache */
    public static final int CACHE_DEFAULT_UPDATEINTERVAL = 10;
}
