/**
 * Copyright (c) 2014,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.riscocloud;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RiscoCloudBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Sebastien Cantineau - Initial contribution
 */
@NonNullByDefault
public class RiscoCloudBindingConstants {

    public static final String BINDING_ID = "riscocloud";

    // List of all Main Bridge properties
    public static final String USERNAME = "username";
    public static final String WEBPASS = "webpass";
    public static final String WEBUIURL = "webUIUrl";

    // List of all Site Bridge properties
    public static final String SITE_ID = "siteId";
    public static final String SITE_NAME = "siteName";
    public static final String PINCODE = "pincode";
    public static final String SITE_URL = "siteUrl";
    public static final String REST_URL = "restUrl";
    public static final String OVERVIEW_URL = "overviewUrl";
    public static final String HISTORY_URL = "historyUrl";
    public static final String DETECTORS_URL = "detectorsUrl";
    public static final String ARM_DISARM_URL = "armDisarmUrl";

    // List of all Site Things properties
    public static final String PART_ID = "partId";

    // List of all Server constants
    public static final String USER_PASS = "USER_PASS";
    public static final String SITEID_PIN = "SITEID_PIN";
    public static final String POLL = "POLL";
    public static final String ARM_FULL = "ARM_FULL";
    public static final String ARM_PART = "ARM_PART";
    public static final String DISARM = "DISARM";
    public static final String OVERVIEW_PROPERTY = "overview";
    public static final String HISTORY_PROPERTY = "eh";
    public static final String DETECTORS_PROPERTY = "detectors";

    // List of Bridge Type UIDs
    public static final ThingTypeUID LOGIN_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "riscoCloudServer");
    public static final ThingTypeUID SITE_BRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "riscoCloudSite");

    // List of Things Type UIDs
    public static final ThingTypeUID OVERVIEW_THING_TYPE = new ThingTypeUID(BINDING_ID, "riscoCloudOverview");
    public static final ThingTypeUID PART_THING_TYPE = new ThingTypeUID(BINDING_ID, "riscoCloudPart");

    // List of all Channel id's
    // Overview
    public static final String CHANNEL_ONLINE_STATUS = "onlineStatus";
    public static final String CHANNEL_ONGOING_ALARM = "ongoingAlarm";
    public static final String CHANNEL_ARMED_PARTS_NB = "armedPartitionsNb";
    public static final String CHANNEL_DISARMED_PARTS_NB = "disarmedPartitionsNb";
    public static final String CHANNEL_PARTIALLYARMED_PARTS_NB = "partiallyArmedPartitionsNb";
    // Part
    public static final String CHANNEL_PART_ARM = "partArm";
    public static final String CHANNEL_PART_PARTIALLYARM = "partPartiallyArm";
    public static final String CHANNEL_PART_DISARM = "partDisarm";

    public static final Set<ThingTypeUID> BRIDGE_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(LOGIN_BRIDGE_THING_TYPE, SITE_BRIDGE_THING_TYPE));
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(OVERVIEW_THING_TYPE, PART_THING_TYPE));

    public static final Set<String> HANDLED_COMMANDS = new HashSet<>(Arrays.asList(ARM_FULL, ARM_PART, DISARM));
    public static final Map<String, String> REST_URLS = initMap();

    private static Map<String, String> initMap() {
        Map<String, String> map = new HashMap<>();
        map.put(OVERVIEW_PROPERTY, OVERVIEW_URL);
        map.put(HISTORY_PROPERTY, HISTORY_URL);
        map.put(DETECTORS_PROPERTY, DETECTORS_URL);
        return Collections.unmodifiableMap(map);
    }
}
