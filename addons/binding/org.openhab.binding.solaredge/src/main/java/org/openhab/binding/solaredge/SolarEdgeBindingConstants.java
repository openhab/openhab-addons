/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge;

import java.util.Collections;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link SolarEdgeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
@NonNullByDefault
public class SolarEdgeBindingConstants {

    private static final String BINDING_ID = "solaredge";

    // List of main device types
    public static final String DEVICE_GENERIC = "generic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, DEVICE_GENERIC);

    // List of all Channel ids ==> see DataChannels

    // PRIVATE API CONSTANTS
    // URLs
    public static final String PRIVATE_DATA_API_URL = "https://monitoring.solaredge.com/solaredge-apigw/api/site/";
    public static final String PRIVATE_DATA_API_URL_AGGREGATE_DATA_DAY_WEEK_SUFFIX = "/powerDashboardChart";
    public static final String PRIVATE_DATA_API_URL_AGGREGATE_DATA_MONTH_YEAR_SUFFIX = "/energyDashboardChart";
    public static final String PRIVATE_DATA_API_URL_LIVE_DATA_SUFFIX = "/currentPowerFlow.json";

    // field names
    public static final String PRIVATE_API_TOKEN_COOKIE_NAME = "SPRING_SECURITY_REMEMBER_ME_COOKIE";
    public static final String PRIVATE_API_TOKEN_COOKIE_DOMAIN = "monitoring.solaredge.com";
    public static final String PRIVATE_API_TOKEN_COOKIE_PATH = "/";
    public static final String PRIVATE_DATA_API_AGGREGATE_DATA_CHARTFIELD_FIELD = "chartField";

    //
    //
    // PRIVATE API CONSTANTS
    // URLs
    public static final String PUBLIC_DATA_API_URL = "https://monitoringapi.solaredge.com/site/";
    public static final String PUBLIC_DATA_API_URL_AGGREGATE_DATA_SUFFIX = "/energyDetails";
    public static final String PUBLIC_DATA_API_URL_LIVE_DATA_SUFFIX = "/currentPowerFlow";
    public static final String PUBLIC_DATA_API_URL_LIVE_DATA_METERLESS_SUFFIX = "/overview";

    // field names
    public static final String PUBLIC_DATA_API_KEY_FIELD = "api_key";
    public static final String PUBLIC_DATA_API_START_TIME_FIELD = "startTime";
    public static final String PUBLIC_DATA_API_END_TIME_FIELD = "endTime";
    public static final String PUBLIC_DATA_API_TIME_UNIT_FIELD = "timeUnit";

    // constants
    public static final String BEGIN_OF_DAY_TIME = "00:00:00";
    public static final String END_OF_DAY_TIME = "23:59:59";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_GENERIC);

}
