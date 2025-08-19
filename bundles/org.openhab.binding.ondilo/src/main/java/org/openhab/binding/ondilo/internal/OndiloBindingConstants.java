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
package org.openhab.binding.ondilo.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link OndiloBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author MikeTheTux - Initial contribution
 */
@NonNullByDefault
public class OndiloBindingConstants {

    private static final String BINDING_ID = "ondilo";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_ONDILO = new ThingTypeUID(BINDING_ID, "ondilo");

    // Bridge Properties
    public static final String PROPERTY_BRIDGE_USER_INFO = "userInfo";

    // Bridge Channel ids
    public static final String CHANNEL_POLL_UPDATE = "poll-update";

    // Ondilo Thing Properties
    public static final String PROPERTY_ONDILO_ID = "id";
    public static final String PROPERTY_ONDILO_NAME = "name";
    public static final String PROPERTY_ONDILO_TYPE = "type";
    public static final String PROPERTY_ONDILO_VOLUME = "volume";
    public static final String PROPERTY_ONDILO_DISINFECTION = "disinfection";
    public static final String PROPERTY_ONDILO_ADDRESS = "address";
    public static final String PROPERTY_ONDILO_LOCATION = "location";
    public static final String PROPERTY_ONDILO_INFO_UUID = "uuid";
    public static final String PROPERTY_ONDILO_INFO_POOL_GUY_NUMBER = "poolGuyNumber";
    public static final String PROPERTY_ONDILO_INFO_MAINTENANCE_DAY = "maintenanceDay";

    // Ondilo Thing Measures Channel ids
    public static final String GROUP_MEASURES = "measure#";

    public static final String CHANNEL_TEMPERATURE = GROUP_MEASURES + "temperature";
    public static final String CHANNEL_TEMPERATURE_TREND = GROUP_MEASURES + "temperature-trend";
    public static final String CHANNEL_PH = GROUP_MEASURES + "ph";
    public static final String CHANNEL_PH_TREND = GROUP_MEASURES + "ph-trend";
    public static final String CHANNEL_ORP = GROUP_MEASURES + "orp";
    public static final String CHANNEL_ORP_TREND = GROUP_MEASURES + "orp-trend";
    public static final String CHANNEL_SALT = GROUP_MEASURES + "salt";
    public static final String CHANNEL_SALT_TREND = GROUP_MEASURES + "salt-trend";
    public static final String CHANNEL_TDS = GROUP_MEASURES + "tds";
    public static final String CHANNEL_TDS_TREND = GROUP_MEASURES + "tds-trend";
    public static final String CHANNEL_BATTERY = GROUP_MEASURES + "battery";
    public static final String CHANNEL_RSSI = GROUP_MEASURES + "rssi";
    public static final String CHANNEL_VALUE_TIME = GROUP_MEASURES + "value-time";

    // Ondilo Thing Recommendations Channel ids
    public static final String GROUP_RECOMMENDATIONS = "recommendation#";

    public static final String CHANNEL_RECOMMENDATION_ID = GROUP_RECOMMENDATIONS + "id";
    public static final String CHANNEL_RECOMMENDATION_TITLE = GROUP_RECOMMENDATIONS + "title";
    public static final String CHANNEL_RECOMMENDATION_MESSAGE = GROUP_RECOMMENDATIONS + "message";
    public static final String CHANNEL_RECOMMENDATION_CREATED_AT = GROUP_RECOMMENDATIONS + "created-at";
    public static final String CHANNEL_RECOMMENDATION_UPDATED_AT = GROUP_RECOMMENDATIONS + "updated-at";
    public static final String CHANNEL_RECOMMENDATION_STATUS = GROUP_RECOMMENDATIONS + "status";
    public static final String CHANNEL_RECOMMENDATION_DEADLINE = GROUP_RECOMMENDATIONS + "deadline";

    // Ondilo Thing Configuration Channel ids
    public static final String GROUP_CONFIGURATION = "configuration#";

    public static final String CHANNEL_CONFIGURATION_TEMPERATURE_LOW = GROUP_CONFIGURATION + "temperature-low";
    public static final String CHANNEL_CONFIGURATION_TEMPERATURE_HIGH = GROUP_CONFIGURATION + "temperature-high";
    public static final String CHANNEL_CONFIGURATION_PH_LOW = GROUP_CONFIGURATION + "ph-low";
    public static final String CHANNEL_CONFIGURATION_PH_HIGH = GROUP_CONFIGURATION + "ph-high";
    public static final String CHANNEL_CONFIGURATION_ORP_LOW = GROUP_CONFIGURATION + "orp-low";
    public static final String CHANNEL_CONFIGURATION_ORP_HIGH = GROUP_CONFIGURATION + "orp-high";
    public static final String CHANNEL_CONFIGURATION_SALT_LOW = GROUP_CONFIGURATION + "salt-low";
    public static final String CHANNEL_CONFIGURATION_SALT_HIGH = GROUP_CONFIGURATION + "salt-high";
    public static final String CHANNEL_CONFIGURATION_TDS_LOW = GROUP_CONFIGURATION + "tds-low";
    public static final String CHANNEL_CONFIGURATION_TDS_HIGH = GROUP_CONFIGURATION + "tds-high";

    // I18N keys for state details
    public static final String I18N_URL_INVALID = "@text/thing.ondilo.bridge.config.url.invalid";
    public static final String I18N_OAUTH2_PENDING = "@text/thing.ondilo.bridge.config.oauth2.pending";
    public static final String I18N_OAUTH2_ERROR = "@text/thing.ondilo.bridge.config.oauth2.error";
    public static final String I18N_OAUTH2_INTERRUPTED = "@text/thing.ondilo.bridge.config.oauth2.interrupted";
    public static final String I18N_ID_INVALID = "@text/thing.ondilo.ondilo.config.id.invalid";
}
