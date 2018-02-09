/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.opensprinkler.internal.api;

import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.RaspiPin;

/**
 * The {@link OpenSprinklerApiContents} class defines common constants, which are
 * used across OpenSprinkler API classes.
 *
 * @author Chris Graham - Initial contribution
 */
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
    public static final String CMD_STATION_ENABLE_TIME = "t=64800";
    public static final String CMD_STATION_ENABLE_TIME_UNLIMITED = "t=0";

    public static final String CMD_STATUS_INFO = "jc";
    public static final String CMD_OPTIONS_INFO = "jo";
    public static final String CMD_STATION_INFO = "js";
    public static final String CMD_STATION_CONTROL = "cm";

    public static final String JSON_OPTION_FIRMWARE_VERSION = "fwv";
    public static final String JSON_OPTION_RAINSENSOR = "rs";
    public static final String JSON_OPTION_STATION = "sn";
    public static final String JSON_OPTION_STATION_COUNT = "nstations";

    public static final String JSON_OPTION_RESULT = "result";

    /* These pin-outs are based on the common scheme used by Pi4J and wiringPi. */
    public static final Pin SR_CLK_PIN = RaspiPin.GPIO_07; // rev1:4, rev2:4
    public static final Pin SR_NOE_PIN = RaspiPin.GPIO_00; // rev1:17, rev2:17
    public static final Pin SR_DAT_PIN = RaspiPin.GPIO_02; // rev1:21, rev2:27
    public static final Pin SR_LAT_PIN = RaspiPin.GPIO_03; // rev1:22, rev2:22
}
