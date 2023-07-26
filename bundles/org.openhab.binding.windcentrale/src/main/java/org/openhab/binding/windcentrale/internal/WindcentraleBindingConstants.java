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
package org.openhab.binding.windcentrale.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WindcentraleBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Wouter Born - Add support for new API with authentication
 */
@NonNullByDefault
public final class WindcentraleBindingConstants {

    public static final String BINDING_ID = "windcentrale";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_WINDMILL = new ThingTypeUID(BINDING_ID, "windmill");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_WINDMILL);

    // List of all Channel IDs
    public static final String CHANNEL_ENERGY_TOTAL = "energy-total";
    public static final String CHANNEL_POWER_RELATIVE = "power-relative";
    public static final String CHANNEL_POWER_SHARES = "power-shares";
    public static final String CHANNEL_POWER_TOTAL = "power-total";
    public static final String CHANNEL_RUN_PERCENTAGE = "run-percentage";
    public static final String CHANNEL_RUN_TIME = "run-time";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_WIND_DIRECTION = "wind-direction";
    public static final String CHANNEL_WIND_SPEED = "wind-speed";

    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_SHARES = "shares";
    public static final String PROPERTY_REFRESH_INTERVAL = "refreshInterval";

    public static final String PROPERTY_BUILD_YEAR = "buildYear";
    public static final String PROPERTY_COORDINATES = "coordinates";
    public static final String PROPERTY_DETAILS_URL = "detailsUrl";
    public static final String PROPERTY_MUNICIPALITY = "municipality";
    public static final String PROPERTY_PROJECT_CODE = "projectCode";
    public static final String PROPERTY_PROVINCE = "province";
    public static final String PROPERTY_TOTAL_SHARES = "totalShares";
}
