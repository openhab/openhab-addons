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
package org.openhab.binding.konnected.internal;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KonnectedBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Zachary Christiansen - Initial contribution
 */
@NonNullByDefault
public class KonnectedBindingConstants {

    public static final String BINDING_ID = "konnected";
    public static final String PRO_MODULE = "pro-module";
    public static final String WIFI_MODULE = "wifi-module";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_WIFIMODULE = new ThingTypeUID(BINDING_ID, WIFI_MODULE);
    public static final ThingTypeUID THING_TYPE_PROMODULE = new ThingTypeUID(BINDING_ID, PRO_MODULE);

    // Thing config properties
    public static final String BASE_URL = "baseUrl";
    public static final String MAC_ADDR = "macAddress";
    public static final String REQUEST_TIMEOUT = "requestTimeout";
    public static final String RETRY_COUNT = "retryCount";
    public static final String CALLBACK_URL = "callbackUrl";

    // ESP8266_ZONE_TO_PIN map, this maps a zone to a pin for ESP8266 based devices
    // Source: https://help.konnected.io/support/solutions/articles/32000026808-zone-to-gpio-pin-mapping
    public static final Map<String, Integer> ESP8266_ZONE_TO_PIN = Map.of("1", 1, "2", 2, "3", 5, "4", 6, "5", 7, "6",
            9, "alarm_out", 8);
    public static final Map<Integer, String> ESP8266_PIN_TO_ZONE = ESP8266_ZONE_TO_PIN.entrySet().stream()
            .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

    // channeltypeids
    public static final String CHANNEL_SWITCH = "konnected:switch";
    public static final String CHANNEL_ACTUATOR = "konnected:actuator";
    public static final String CHANNEL_TEMPERATURE = "konnected:temperature";
    public static final String CHANNEL_HUMIDITY = "konnected:humidity";
}
