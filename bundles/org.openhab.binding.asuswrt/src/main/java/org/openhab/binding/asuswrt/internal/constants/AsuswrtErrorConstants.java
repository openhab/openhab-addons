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
package org.openhab.binding.asuswrt.internal.constants;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link AsuswrtErrorConstants} class defines error constants, which are used across the whole binding.
 *
 * @author Christian Wild - Initial contribution
 */
@NonNullByDefault
public class AsuswrtErrorConstants {

    public static final String ERR_HTTP_CLIENT_FAILED = "Starting 'httpClient' failed";
    public static final String ERR_CONN_TIMEOUT = "Connection timeout";
    public static final String ERR_RESPONSE = "Response not okay";
    public static final String ERR_JSON_FORMAT = "Unexpected or malfomrated JSON response";
    public static final String ERR_JSON_UNKNOWN_MEMBER = "JSON member not found";
    public static final String ERR_SSL_EXCEPTION = "SSL Exception";
    public static final String ERR_INVALID_MAC_ADDRESS = "Invalid MAC address";
    public static final String ERR_BRIDGE_OFFLINE = "Bridge is offline";
    public static final String ERR_BRIDGE_NOT_DECLARED = "Bridge not found or not declared";
}
