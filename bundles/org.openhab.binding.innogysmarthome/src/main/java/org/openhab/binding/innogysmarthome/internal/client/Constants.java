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
package org.openhab.binding.innogysmarthome.internal.client;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds necessary constants for the innogy API.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public class Constants {

    // devices
    public static final Set<String> BATTERY_POWERED_DEVICES = Collections.unmodifiableSet(
            Stream.of("RST", "WRT", "WDS", "WSD", "WSD2", "WMD", "WMDO", "WSC2", "BRC8").collect(Collectors.toSet()));

    // API URLs
    public static final String API_HOST = "api.services-smarthome.de";
    public static final String API_VERSION = "1.0";
    public static final String API_URL_BASE = "https://" + API_HOST + "/API/" + API_VERSION;
    public static final String API_URL_TOKEN = "https://" + API_HOST + "/AUTH/token";

    public static final String API_URL_CHECK_CONNECTION = API_URL_BASE + "/desc/device/SHC.RWE/1.0/event/StateChanged";
    public static final String API_URL_INITIALIZE = API_URL_BASE + "/initialize";
    public static final String API_URL_UNINITIALIZE = API_URL_BASE + "/uninitialize";

    public static final String API_URL_DEVICE = API_URL_BASE + "/device";
    public static final String API_URL_DEVICE_ID = API_URL_DEVICE + "/{id}";
    public static final String API_URL_DEVICE_ID_STATE = API_URL_DEVICE_ID + "/state";
    public static final String API_URL_DEVICE_CAPABILITIES = API_URL_DEVICE + "/{id}/capabilities";
    public static final String API_URL_DEVICE_STATES = API_URL_DEVICE + "/states";

    public static final String API_URL_LOCATION = API_URL_BASE + "/location";

    public static final String API_URL_CAPABILITY = API_URL_BASE + "/capability";
    public static final String API_URL_CAPABILITY_STATES = API_URL_CAPABILITY + "/states";

    public static final String API_URL_MESSAGE = API_URL_BASE + "/message";

    public static final String API_URL_EVENTS = "wss://" + API_HOST + "/API/" + API_VERSION + "/events?token={token}";
    public static final String API_URL_ACTION = API_URL_BASE + "/action";

    // others
    public static final String FORMAT_DATETIME = "dd.MM.yyyy HH:mm:ss";
}
