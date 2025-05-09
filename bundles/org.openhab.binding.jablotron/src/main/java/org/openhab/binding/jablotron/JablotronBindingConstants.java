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
package org.openhab.binding.jablotron;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link JablotronBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class JablotronBindingConstants {

    public static final String BINDING_ID = "jablotron";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_OASIS = new ThingTypeUID(BINDING_ID, "oasis");
    public static final ThingTypeUID THING_TYPE_JA100 = new ThingTypeUID(BINDING_ID, "ja100");
    public static final ThingTypeUID THING_TYPE_JA100F = new ThingTypeUID(BINDING_ID, "ja100f");

    // Common alarm channels
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_LAST_CHECK_TIME = "lastCheckTime";
    public static final String CHANNEL_LAST_EVENT = "lastEvent";
    public static final String CHANNEL_LAST_EVENT_CLASS = "lastEventClass";
    public static final String CHANNEL_LAST_EVENT_TIME = "lastEventTime";
    public static final String CHANNEL_LAST_EVENT_INVOKER = "lastEventInvoker";
    public static final String CHANNEL_LAST_EVENT_SECTION = "lastEventSection";

    // List of all OASIS Channel ids
    public static final String CHANNEL_COMMAND = "command";
    public static final String CHANNEL_STATUS_A = "statusA";
    public static final String CHANNEL_STATUS_B = "statusB";
    public static final String CHANNEL_STATUS_ABC = "statusABC";
    public static final String CHANNEL_STATUS_PGX = "statusPGX";
    public static final String CHANNEL_STATUS_PGY = "statusPGY";

    // Constants
    public static final String JABLOTRON_API_URL = "https://api.jablonet.net/api/2.2/";
    public static final String JABLOTRON_GQL_URL = "https://graph.jablotron.cloud/graphql";
    public static final String APP_VERSION = "8.6.1.3887";
    public static final String AGENT = "net.jablonet/" + APP_VERSION;
    public static final int TIMEOUT_SEC = 15;
    public static final int TIMEOUT_LIMIT = 3;
    public static final String SYSTEM = "openHAB";
    public static final String VENDOR = "JABLOTRON:Jablotron";
    public static final String CLIENT_VERSION = "MYJ-PUB-IOS-" + APP_VERSION;
    public static final String APPLICATION_JSON = "application/json";
    public static final String MULTIPART_MIXED = "multipart/mixed;deferSpec=20220824";
    public static final String WWW_FORM_URLENCODED = "application/x-www-form-urlencoded; charset=UTF-8";
    public static final String AUTHENTICATION_CHALLENGE = "Authentication challenge without WWW-Authenticate header";
    public static final String PROPERTY_SERVICE_ID = "serviceId";
    public static final int DISCOVERY_TIMEOUT_SEC = 10;
    public static final int CACHE_TIMEOUT_MS = 10000;

    // supported thing types for discovery
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>(
            Arrays.asList(THING_TYPE_OASIS, THING_TYPE_JA100, THING_TYPE_JA100F));
}
