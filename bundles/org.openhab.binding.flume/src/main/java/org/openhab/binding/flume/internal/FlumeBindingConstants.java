/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.flume.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link FlumeBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jeff James - Initial contribution
 */
@NonNullByDefault
public class FlumeBindingConstants {

    private static final String BINDING_ID = "flume";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CLOUD = new ThingTypeUID(BINDING_ID, "cloud");
    public static final ThingTypeUID THING_TYPE_METER = new ThingTypeUID(BINDING_ID, "meter-device");

    // Config options
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_CLIENTID = "clientId";
    public static final String PARAM_CLIENTSECRET = "clientSecret";
    public static final String PARAM_REFRESH_INTERVAL_INSTANTANEOUS = "refreshIntervalInstanteous";
    public static final String PARAM_REFRESH_INTERVAL_CUMULATIVE = "refreshIntervalCumulative";

    // List of all Device Channel ids
    public static final String CHANNEL_DEVICE_CUMULATIVEUSAGE = "cumulative-usage";
    public static final String CHANNEL_DEVICE_INSTANTUSAGE = "instant-usage";
    public static final String CHANNEL_DEVICE_BATTERYLEVEL = "battery-level";
    public static final String CHANNEL_DEVICE_LOWBATTERY = "low-battery";
    public static final String CHANNEL_DEVICE_LASTSEEN = "last-seen";
    public static final String CHANNEL_DEVICE_USAGEALERT = "usage-alert";

    // Properties
    public static final String PROPERTY_ID = "id";

    public static final int DEFAULT_POLLING_INTERVAL_INSTANTANEOUS_MIN = 1;
    public static final int DEFAULT_POLLING_INTERVAL_CUMULATIVE_MIN = 5;
}
