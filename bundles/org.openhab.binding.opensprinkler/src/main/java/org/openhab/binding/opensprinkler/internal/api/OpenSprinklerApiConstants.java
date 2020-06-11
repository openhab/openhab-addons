/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.opensprinkler.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OpenSprinklerApiContents} class defines common constants, which are
 * used across OpenSprinkler API classes.
 *
 * @author Chris Graham - Initial contribution
 */
@NonNullByDefault
public class OpenSprinklerApiConstants {
    public static final String HTTP_REQUEST_URL_PREFIX = "http://";
    public static final String HTTPS_REQUEST_URL_PREFIX = "https://";

    public static final String DEFAULT_ADMIN_PASSWORD = "opendoor";
    public static final int DEFAULT_API_PORT = 80;
    public static final int DEFAULT_STATION_COUNT = 8;

    public static final String CMD_ENABLE_MANUAL_MODE = "mm=1";
    public static final String CMD_DISABLE_MANUAL_MODE = "mm=0";
    public static final String CMD_PASSWORD = "pw=";
    public static final String CMD_STATION = "sid=";
    public static final String CMD_STATION_ENABLE = "en=1";
    public static final String CMD_STATION_DISABLE = "en=0";

    public static final String CMD_STATUS_INFO = "jc";
    public static final String CMD_OPTIONS_INFO = "jo";
    public static final String CMD_STATION_INFO = "js";
    public static final String CMD_STATION_CONTROL = "cm";

    public static final String JSON_OPTION_FIRMWARE_VERSION = "fwv";
    public static final String JSON_OPTION_RAINSENSOR = "rs";
    public static final String JSON_OPTION_STATION = "sn";
    public static final String JSON_OPTION_STATION_COUNT = "nstations";

    public static final String JSON_OPTION_RESULT = "result";
}
