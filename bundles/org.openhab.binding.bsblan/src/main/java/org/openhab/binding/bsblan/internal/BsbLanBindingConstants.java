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
package org.openhab.binding.bsblan.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link BsbLanBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Peter Schraffl - Initial contribution
 */
@NonNullByDefault
public class BsbLanBindingConstants {

    private static final String BINDING_ID = "bsblan";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_PARAMETER = new ThingTypeUID(BINDING_ID, "parameter");

    // List of all channel ids
    public static final String PARAMETER_CHANNEL_NAME = "name";
    public static final String PARAMETER_CHANNEL_NUMBER_VALUE = "number-value";
    public static final String PARAMETER_CHANNEL_STRING_VALUE = "string-value";
    public static final String PARAMETER_CHANNEL_SWITCH_VALUE = "switch-value";
    public static final String PARAMETER_CHANNEL_UNIT = "unit";
    public static final String PARAMETER_CHANNEL_DESCRIPTION = "description";
    public static final String PARAMETER_CHANNEL_DATATYPE = "datatype";

    public static final Set<String> WRITEABLE_CHANNELS = Set.of(PARAMETER_CHANNEL_NUMBER_VALUE,
            PARAMETER_CHANNEL_STRING_VALUE, PARAMETER_CHANNEL_SWITCH_VALUE);

    public static final int MIN_REFRESH_INTERVAL = 5;
    public static final int DEFAULT_REFRESH_INTERVAL = 60;
    public static final int API_TIMEOUT = 10000;
    public static final int DEFAULT_API_PORT = 80;
}
