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
package org.openhab.binding.ntp.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link NtpBindingConstants} class defines common constants, which are used across
 * the whole binding.
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */
@NonNullByDefault
public class NtpBindingConstants {

    public static final String BINDING_ID = "ntp";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_NTP = new ThingTypeUID(BINDING_ID, "ntp");

    // List of all Channel ids
    public static final String CHANNEL_DATE_TIME = "dateTime";
    public static final String CHANNEL_STRING = "string";

    // Custom Properties
    public static final String PROPERTY_NTP_SERVER_HOST = "hostname";
    public static final String PROPERTY_REFRESH_INTERVAL = "refreshInterval";
    public static final String PROPERTY_REFRESH_NTP = "refreshNtp";
    public static final String PROPERTY_TIMEZONE = "timeZone";
    public static final String PROPERTY_LOCALE = "locale";
    public static final String PROPERTY_DATE_TIME_FORMAT = "DateTimeFormat";
    public static final String PROPERTY_NTP_SERVER_PORT = "serverPort";

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_NTP);
}
