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
package org.openhab.binding.semsportal.internal;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SEMSPortalBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Iwan Bron - Initial contribution
 */
@NonNullByDefault
public class SEMSPortalBindingConstants {

    private static final String BINDING_ID = "semsportal";
    public static final String TIME_FORMAT = "HH:mm:ss";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_PORTAL = new ThingTypeUID(BINDING_ID, "portal");
    public static final ThingTypeUID THING_TYPE_STATION = new ThingTypeUID(BINDING_ID, "station");

    // the default update interval for statusses at the portal
    public static final int DEFAULT_UPDATE_INTERVAL_MINUTES = 5;

    // station properties
    public static final String STATION_UUID = "stationUUID";
    public static final String STATION_NAME = "stationName";
    public static final String STATION_CAPACITY = "stationCapacity";
    public static final String STATION_REPRESENTATION_PROPERTY = STATION_UUID;
    public static final String STATION_LABEL_FORMAT = "Power Station %s";

    // List of all Channel ids
    public static final String CHANNEL_CURRENT_OUTPUT = "currentOutput";
    public static final String CHANNEL_LASTUPDATE = "lastUpdate";
    public static final String CHANNEL_TODAY_TOTAL = "todayTotal";
    public static final String CHANNEL_MONTH_TOTAL = "monthTotal";
    public static final String CHANNEL_OVERALL_TOTAL = "overallTotal";
    public static final String CHANNEL_TODAY_INCOME = "todayIncome";
    public static final String CHANNEL_TOTAL_INCOME = "totalIncome";

    protected static final List<String> ALL_CHANNELS = Arrays.asList(CHANNEL_LASTUPDATE, CHANNEL_CURRENT_OUTPUT,
            CHANNEL_TODAY_TOTAL, CHANNEL_MONTH_TOTAL, CHANNEL_OVERALL_TOTAL, CHANNEL_TODAY_INCOME,
            CHANNEL_TOTAL_INCOME);
}
