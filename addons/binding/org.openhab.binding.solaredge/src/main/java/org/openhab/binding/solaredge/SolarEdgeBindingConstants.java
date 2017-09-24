/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.solaredge;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link SolarEdgeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - Initial contribution
 */
public class SolarEdgeBindingConstants {

    private static final String BINDING_ID = "solaredge";

    // List of main device types
    public static final String DEVICE_GENERIC = "generic";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERIC = new ThingTypeUID(BINDING_ID, DEVICE_GENERIC);

    // List of all Channel ids ==> see DataChannels

    // // URLs
    public static final String DATA_API_URL = "https://monitoring.solaredge.com/solaredge-apigw/api/site/";
    public static final String DATA_API_URL_AGGREGATE_DATA_SUFFIX = "/powerDashboardChart?chartField=DAY";
    public static final String DATA_API_URL_LIVE_DATA_SUFFIX = "/currentPowerFlow.json";

    // login field names
    public static final String TOKEN_COOKIE_NAME = "SPRING_SECURITY_REMEMBER_ME_COOKIE";
    public static final String TOKEN_COOKIE_DOMAIN = "monitoring.solaredge.com";
    public static final String TOKEN_COOKIE_PATH = "/";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_GENERIC);

}
