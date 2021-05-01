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
package org.openhab.binding.bmwconnecteddrive.internal.utils;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BimmerConstants} This class holds the important constants for the BMW Connected Drive Authorization. They
 * are taken from the Bimmercode from github {@link https://github.com/bimmerconnected/bimmer_connected}
 * File defining these constants
 * {@link https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py}
 * https://customer.bmwgroup.com/one/app/oauth.js
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BimmerConstants {

    // https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/country_selector.py
    public static final String REGION_NORTH_AMERICA = "NORTH_AMERICA";
    public static final String REGION_CHINA = "CHINA";
    public static final String REGION_ROW = "ROW";

    // https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/country_selector.py
    public static final String AUTH_SERVER_NORTH_AMERICA = "b2vapi.bmwgroup.us/gcdm";
    public static final String AUTH_SERVER_CHINA = "b2vapi.bmwgroup.cn/gcdm";
    public static final String AUTH_SERVER_ROW = "b2vapi.bmwgroup.com/gcdm";
    public static final Map<String, String> AUTH_SERVER_MAP = Map.of(REGION_NORTH_AMERICA, AUTH_SERVER_NORTH_AMERICA,
            REGION_CHINA, AUTH_SERVER_CHINA, REGION_ROW, AUTH_SERVER_ROW);

    public static final String OAUTH_ENDPOINT = "/oauth/token";

    public static final String SERVER_NORTH_AMERICA = "b2vapi.bmwgroup.us";
    public static final String SERVER_CHINA = "b2vapi.bmwgroup.cn:8592";
    public static final String SERVER_ROW = "b2vapi.bmwgroup.com";
    public static final Map<String, String> SERVER_MAP = Map.of(REGION_NORTH_AMERICA, SERVER_NORTH_AMERICA,
            REGION_CHINA, SERVER_CHINA, REGION_ROW, SERVER_ROW);

    // see https://github.com/bimmerconnected/bimmer_connected/pull/252/files
    public static final Map<String, String> AUTHORIZATION_VALUE_MAP = Map.of(REGION_NORTH_AMERICA,
            "Basic ZDc2NmI1MzctYTY1NC00Y2JkLWEzZGMtMGNhNTY3MmQ3ZjhkOjE1ZjY5N2Y2LWE1ZDUtNGNhZC05OWQ5LTNhMTViYzdmMzk3Mw==",
            REGION_CHINA,
            "Basic blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanliTEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==",
            REGION_ROW,
            "Basic ZDc2NmI1MzctYTY1NC00Y2JkLWEzZGMtMGNhNTY3MmQ3ZjhkOjE1ZjY5N2Y2LWE1ZDUtNGNhZC05OWQ5LTNhMTViYzdmMzk3Mw==");

    public static final String CREDENTIAL_VALUES = "nQv6CqtxJuXWP74xf3CJwUEP:1zDHx6un4cDjybLENN3kyfumX2kEYigWPcQpdvDRpIBk7rOJ";
    public static final String REDIRECT_URI_VALUE = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html";
    public static final String SCOPE_VALUES = "authenticate_user vehicle_data remote_services";

    public static final String LEGACY_CREDENTIAL_VALUES = "nQv6CqtxJuXWP74xf3CJwUEP:1zDHx6un4cDjybLENN3kyfumX2kEYigWPcQpdvDRpIBk7rOJ";
    public static final String REFERER_URL = "https://www.bmw-connecteddrive.de/app/index.html";
}
