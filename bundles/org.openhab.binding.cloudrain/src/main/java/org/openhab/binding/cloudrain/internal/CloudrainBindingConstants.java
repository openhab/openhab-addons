/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.cloudrain.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link CloudrainBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Till Koellmann - Initial contribution
 */
@NonNullByDefault
public class CloudrainBindingConstants {

    public static final String BINDING_ID = "cloudrain";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");

    // List of Thing labels
    public static final String LABEL_ZONE = "Cloudrain Zone: ";

    // List of Thing properties
    public static final String PROPERTY_UID = "uid";
    public static final String PROPERTY_ZONE_ID = "zoneId";
    public static final String PROPERTY_ZONE_NAME = "zoneName";
    public static final String PROPERTY_CTRL_ID = "controllerId";
    public static final String PROPERTY_CTRL_NAME = "controllerName";

    // List of all Channel group ids
    public static final String CHANNEL_GROUP_ID_IRRIGATION = "irrigation";
    public static final String CHANNEL_GROUP_ID_COMMAND = "command";

    // List of all Channel ids
    public static final String CHANNEL_ID_STATE = "state";
    public static final String CHANNEL_ID_START_TIME = "startTime";
    public static final String CHANNEL_ID_END_TIME = "plannedEndTime";
    public static final String CHANNEL_ID_REMAINING_SECS = "remainingSeconds";
    public static final String CHANNEL_ID_DURATION = "duration";

    public static final String CHANNEL_ID_DURATION_CMD = "commandDuration";
    public static final String CHANNEL_ID_START_CMD = "startIrrigation";
    public static final String CHANNEL_ID_CHANGE_CMD = "changeIrrigation";
    public static final String CHANNEL_ID_STOP_CMD = "stopIrrigation";

    public static final String ERROR_MSG_CONFIG_PARAMS = "Mandatory configuration parameters missing for authenticating. Please check the account configuration page.";
    public static final String ERROR_MSG_API_AUTH = "Failed to authenticate at the Cloudrain API. Please check the configuration. API response: %s";
    public static final String ERROR_MSG_GET_ZONES = "Failed to discover zones from the Cloudrain API. Details: {}";
    public static final String ERROR_MSG_GET_IRRIGATION_ZONE_ID = "Retrieved invalid irrigation from the Cloudrain API. The zone ID is missing.";
    public static final String ERROR_MSG_STATUS_UPDATE = "Failed to retrieve the status for zone %s. Details: %s";
    public static final String ERROR_MSG_ZONE_STATUS_UPDATE = "Failed to retrieve zones. API response: %s";
    public static final String ERROR_MSG_STATUS_UPDATE_IRRIGATION = "Failed to retrieve irrigation information. Details: %s";
    public static final String ERROR_MSG_ZONE_NOT_FOUND = "Zone with id %s is unknown by the API. Maybe the zone got deleted in the app or this is due to test mode. Test Mode is: %s";
    public static final String ERROR_MSG_IRRIGATION_COMMAND = "Failed to execute irrigation command for zone %s. Details: %s";
    public static final String ERROR_MSG_ZONE_REGISTRATION = "Could not register zone %s for updates.";
    public static final String ERROR_MSG_ACCOUNTHANDLER_NOT_FOUND = "Bridge not yet initialized. Please try again later.";
    public static final String ERROR_MSG_DISCOVERY_SCAN = "Error waiting for discovery scan: {}";
    public static final String ERROR_MSG_DISCOVERY_FAILED = "Discovery scan failed. Bridge not yet initialized. Please re-run scan later.";
    public static final String ERROR_MSG_DISCOVERY_FAILED_ZONE = "Discovery of zone {} failed. Bridge not yet initialized. Please re-run scan later.";
    public static final String ERROR_MSG_DISCOVERY_FAILED_ZONE_ID = "Discovery found an invalid zone. The zone ID is missing. Please re-run scan later.";
    public static final String ERROR_MSG_DISCOVERY_FAILED_CREATE_RESULT = "Discovery found zone {}, but could not create the discovery result. Please re-run scan later.";
    public static final String ERROR_MSG_ZONE_ID_PROPERTY = "Failed to retrieve the mandatory zone property 'zoneId'. Please check the configuration.";
    public static final String ERROR_MSG_ZONE_UPDATE_PROPERTIES = "Failed to update properties of zone {}";
    public static final String ERROR_MSG_ZONE_UPDATE_IRRIGATION = "Failed to update the zone {} with irrigation status";
}
