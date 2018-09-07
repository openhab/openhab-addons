/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.openuv;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link OpenUVBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class OpenUVBindingConstants {
    public static final String BASE_URL = "https://api.openuv.io/api/v1/uv";
    public static final String BINDING_ID = "openuv";
    public static final String LOCAL = "local";

    public static final String LOCATION = "location";
    public static final String APIKEY = "apikey";

    // List of Bridge Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "openuvapi");

    // List of Things Type UIDs
    public static final ThingTypeUID LOCATION_REPORT_THING_TYPE = new ThingTypeUID(BINDING_ID, "uvreport");

    // List of all Channel id's
    public static final String UVINDEX = "UVIndex";
    public static final String UVCOLOR = "UVColor";
    public static final String UVMAX = "UVMax";
    public static final String UVMAXTIME = "UVMaxTime";
    public static final String UVMAXEVENT = "UVMaxEvent";
    public static final String OZONE = "Ozone";
    public static final String OZONETIME = "OzoneTime";
    public static final String UVTIME = "UVTime";
    public static final String SAFEEXPOSURE = "SafeExposure";

    public static final String PROPERTY_INDEX = "Index";

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = Collections.singleton(APIBRIDGE_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(LOCATION_REPORT_THING_TYPE));
}
