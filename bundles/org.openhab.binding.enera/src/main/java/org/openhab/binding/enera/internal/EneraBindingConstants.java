/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.enera.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link EneraBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Oliver Rahner - Initial contribution
 */
@NonNullByDefault
public class EneraBindingConstants {

    private static final String BINDING_ID = "enera";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    // List of all Properties
    public static final String PROPERTY_ID = "id";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_BRAND = "brand";
    public static final String PROPERTY_SERIAL = "serial";
    public static final String PROPERTY_EXTERNAL_ID = "external-id";
    public static final String PROPERTY_METER_ID = "meter-id";
    public static final String PROPERTY_REGISTERED_AT = "registered-at";

    // AWS Cognito parameters of Enera
    public static final String COGNITO_CLIENT_ID = "2iv53r55ahff70bcjo7f32a7lb";
    public static final String COGNITO_USER_POOL_ID = "eu-central-1_m9ZZbQCTb";
    public static final String COGNITO_REGION = "eu-central-1";

    // Enera Live Consumption API data
    public static final String LIVE_CONSUMPTION_URL = "ws://broker.enera.energie-vernetzen.de/rabbitmq-ws";
    public static final String LIVE_CONSUMPTION_USERNAME = "energiemonitor:em-app";
    public static final String LIVE_CONSUMPTION_PASSWORD = "em-app";

    // Enera master data API
    public static final String ENERA_BASE_URL = "https://api.enera.energie-vernetzen.de/Prod/v1";
    public static final String ENERA_DEVICES_URL = ENERA_BASE_URL + "/Devices";

    // OBIS keys
    public static final String OBIS_METER_READING = "1-0:1.8.0*255";
    public static final String OBIS_METER_READING_OUTBOUND = "1-0:2.8.0*255";
    public static final String OBIS_LIVE_CONSUMPTION_TOTAL = "1-0:1.7.0*255";

    // List of all Channel ids
    public static final String CHANNEL_METER_READING = "meter-reading";
    public static final String CHANNEL_METER_READING_OUTBOUND = "meter-reading-outbound";
    public static final String CHANNEL_CURRENT_CONSUMPTION = "current-consumption";

    // List all Configuration Keys
    public static final String CONFIG_USERNAME = "username";
    public static final String CONFIG_PASSWORD = "password";
    public static final String CONFIG_RATE = "rate";
}
