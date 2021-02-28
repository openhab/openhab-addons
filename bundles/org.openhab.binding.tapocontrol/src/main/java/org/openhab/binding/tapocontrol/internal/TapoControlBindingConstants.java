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
package org.openhab.binding.tapocontrol.internal;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link TapoControlBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class TapoControlBindingConstants {

    private static final String BINDING_ID = "tapocontrol";

    // Lisst of all constant configurations
    public static final String HTTP_HEADER_AUTH = "Authorization";
    public static final String HTTP_AUTH_TYPE_BASIC = "Basic";
    public static final String HTTP_AUTH_TYPE_COOKIE = "cookie";
    public static final String CONTENT_CHARSET = "utf-8";
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String TAPO_CLOUD_URL = "https://eu-wap.tplinkcloud.com";
    public static final String TAPO_TERMINAL_UUID = "0A950402-7224-46EB-A450-7362CDB902A2";
    public static final String TAPO_DEVICE_URL = "http://%s/app";
    public static final Integer TAPO_HTTP_TIMEOUT_MS = 5000;
    public static final Integer TAPO_PING_TIMEOUT_MS = 500;

    // LIST OF SUPPORTED DEVICE NAMES
    public static final String DEVICE_P100 = "tapoP100";
    public static final String DEVICE_L530E = "tapoL530E";

    // LIST OF SUPPORTED THING UIDS
    public static final ThingTypeUID P100_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_P100);
    public static final ThingTypeUID L530E_THING_TYPE = new ThingTypeUID(BINDING_ID, DEVICE_L530E);

    // SET OF SUPPORTED UIDS
    public static final Set<ThingTypeUID> SUPPORTED_SMART_PLUG_UIDS = Set.of(P100_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_SMART_BULB_UIDS = Set.of(L530E_THING_TYPE);
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.unmodifiableSet(Stream
            .of(SUPPORTED_SMART_PLUG_UIDS, SUPPORTED_SMART_BULB_UIDS).flatMap(Set::stream).collect(Collectors.toSet()));

    // CHANNEL LIST
    public static final String CHANNEL_SWITCH = "switch";
    public static final String CHANNEL_OUTPUT = "output";

    // THING CONFIGUTATION PROPERTYS
    public static final String CONFIG_EMAIL = "eMail";
    public static final String CONFIG_PASS = "password";
    public static final String CONFIG_DEVICE_IP = "ipAddress";
    public static final String CONFIG_UPDATE_INTERVAL = "pollingInterval";

    // LIST OF PROPERTY NAMES
    public static final String PROPERTY_WIFI_LEVEL = "signal-strength";
    public static final String PROPERTY_LOCATION = "location";
}
