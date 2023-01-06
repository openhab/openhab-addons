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
package org.openhab.binding.openuv.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OpenUVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVBindingConstants {
    public static final String BINDING_ID = "openuv";
    public static final String LOCAL = "local";

    // List of Bridge Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "openuvapi");

    // List of Things Type UIDs
    public static final ThingTypeUID LOCATION_REPORT_THING_TYPE = new ThingTypeUID(BINDING_ID, "uvreport");

    // List of all Channel id's
    public static final String UV_INDEX = "UVIndex";
    public static final String ALERT_LEVEL = "Alert";
    public static final String UV_COLOR = "UVColor";
    public static final String UV_MAX = "UVMax";
    public static final String UV_MAX_TIME = "UVMaxTime";
    public static final String UV_MAX_EVENT = "UVMaxEvent";
    public static final String OZONE = "Ozone";
    public static final String OZONE_TIME = "OzoneTime";
    public static final String UV_TIME = "UVTime";
    public static final String SAFE_EXPOSURE = "SafeExposure";
    public static final String ELEVATION = "elevation";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(APIBRIDGE_THING_TYPE,
            LOCATION_REPORT_THING_TYPE);
}
