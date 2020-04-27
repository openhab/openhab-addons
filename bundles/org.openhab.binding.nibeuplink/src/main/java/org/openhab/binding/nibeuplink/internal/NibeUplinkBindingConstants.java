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
package org.openhab.binding.nibeuplink.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NibeUplinkBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public final class NibeUplinkBindingConstants {

    private static final String BINDING_ID = "nibeuplink";

    // List of main device types
    public static final String DEVICE_VVM320 = "vvm320";
    public static final String DEVICE_VVM310 = "vvm310";
    public static final String DEVICE_F730 = "f730";
    public static final String DEVICE_F750 = "f750";
    public static final String DEVICE_F1145 = "f1145";
    public static final String DEVICE_F1155 = "f1155";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VVM320 = new ThingTypeUID(BINDING_ID, DEVICE_VVM320);
    public static final ThingTypeUID THING_TYPE_VVM310 = new ThingTypeUID(BINDING_ID, DEVICE_VVM310);
    public static final ThingTypeUID THING_TYPE_F730 = new ThingTypeUID(BINDING_ID, DEVICE_F730);
    public static final ThingTypeUID THING_TYPE_F750 = new ThingTypeUID(BINDING_ID, DEVICE_F750);
    public static final ThingTypeUID THING_TYPE_F1145 = new ThingTypeUID(BINDING_ID, DEVICE_F1145);
    public static final ThingTypeUID THING_TYPE_F1155 = new ThingTypeUID(BINDING_ID, DEVICE_F1155);

    // List of all Channel ids ==> see UplinkDataChannels

    // URLs
    public static final String LOGIN_URL = "https://www.nibeuplink.com/LogIn";
    public static final String DATA_API_URL = "https://www.nibeuplink.com/PrivateAPI/QueueValues";
    public static final String MANAGE_API_BASE_URL = "https://www.nibeuplink.com/System/";

    // login field names
    public static final String LOGIN_FIELD_PASSWORD = "Password";
    public static final String LOGIN_FIELD_EMAIL = "Email";
    public static final String LOGIN_FIELD_RETURN_URL = "returnUrl";

    // other field names
    public static final String DATA_API_FIELD_LAST_DATE = "currentWebDate";
    public static final String DATA_API_FIELD_LAST_DATE_DEFAULT_VALUE = "01.01.2017 13:37:42";
    public static final String DATA_API_FIELD_ID = "hpid";
    public static final String DATA_API_FIELD_DATA = "variables";
    public static final String DATA_API_FIELD_DATA_DEFAULT_VALUE = "0";

    // web request constants
    public static final long WEB_REQUEST_INITIAL_DELAY = TimeUnit.SECONDS.toMillis(30);
    public static final long WEB_REQUEST_INTERVAL = TimeUnit.SECONDS.toMillis(5);
    public static final int WEB_REQUEST_QUEUE_MAX_SIZE = 20;

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections
            .unmodifiableSet(Stream.of(THING_TYPE_VVM320, THING_TYPE_VVM310, THING_TYPE_F730, THING_TYPE_F750,
                    THING_TYPE_F1145, THING_TYPE_F1155).collect(Collectors.toSet()));
}
