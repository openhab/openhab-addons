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
package org.openhab.binding.innogysmarthome.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Holds necessary constants for the innogy API.
 *
 * @author Oliver Kuhl - Initial contribution
 *
 */
@NonNullByDefault
public final class Constants {

    // API URLs
    public static final String API_HOST = "api.services-smarthome.de";
    public static final String AUTH_HOST = "auth.services-smarthome.de";
    public static final String API_VERSION = "1.1";
    public static final String API_URL_BASE = "https://" + API_HOST + "/API/" + API_VERSION;
    public static final String API_URL_TOKEN = "https://" + AUTH_HOST + "/AUTH/token";

    public static final String API_URL_STATUS = API_URL_BASE + "/status";

    public static final String API_URL_DEVICE = API_URL_BASE + "/device";
    public static final String API_URL_DEVICE_ID = API_URL_DEVICE + "/{id}";
    public static final String API_URL_DEVICE_ID_STATE = API_URL_DEVICE_ID + "/state";
    public static final String API_URL_DEVICE_CAPABILITIES = API_URL_DEVICE + "/{id}/capabilities";
    public static final String API_URL_DEVICE_STATES = API_URL_DEVICE + "/states";

    public static final String API_URL_LOCATION = API_URL_BASE + "/location";

    public static final String API_URL_CAPABILITY = API_URL_BASE + "/capability";
    public static final String API_URL_CAPABILITY_STATES = API_URL_CAPABILITY + "/states";

    public static final String API_URL_MESSAGE = API_URL_BASE + "/message";

    public static final String API_URL_ACTION = API_URL_BASE + "/action";

    private Constants() {
        // Constants class
    }
}
