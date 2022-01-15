/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
    public static final String LEGACY_AUTH_SERVER_NORTH_AMERICA = "login.bmwusa.com/gcdm";
    public static final String LEGACY_AUTH_SERVER_CHINA = "customer.bmwgroup.cn/gcdm";
    public static final String LEGACY_AUTH_SERVER_ROW = "customer.bmwgroup.com/gcdm";
    public static final Map<String, String> LEGACY_AUTH_SERVER_MAP = Map.of(REGION_NORTH_AMERICA,
            LEGACY_AUTH_SERVER_NORTH_AMERICA, REGION_CHINA, LEGACY_AUTH_SERVER_CHINA, REGION_ROW,
            LEGACY_AUTH_SERVER_ROW);

    public static final String OAUTH_ENDPOINT = "/oauth/authenticate";
    public static final String TOKEN_ENDPOINT = "/oauth/token";

    public static final String API_SERVER_NORTH_AMERICA = "b2vapi.bmwgroup.us";
    public static final String API_SERVER_CHINA = "b2vapi.bmwgroup.cn:8592";
    public static final String API_SERVER_ROW = "b2vapi.bmwgroup.com";

    public static final String EADRAX_SERVER_NORTH_AMERICA = "cocoapi.bmwgroup.us";
    public static final String EADRAX_SERVER_ROW = "cocoapi.bmwgroup.com";
    public static final String EADRAX_SERVER_CHINA = Constants.EMPTY;
    public static final Map<String, String> EADRAX_SERVER_MAP = Map.of(REGION_NORTH_AMERICA,
            EADRAX_SERVER_NORTH_AMERICA, REGION_CHINA, EADRAX_SERVER_CHINA, REGION_ROW, EADRAX_SERVER_ROW);

    public static final Map<String, String> API_SERVER_MAP = Map.of(REGION_NORTH_AMERICA, API_SERVER_NORTH_AMERICA,
            REGION_CHINA, API_SERVER_CHINA, REGION_ROW, API_SERVER_ROW);

    // see https://github.com/bimmerconnected/bimmer_connected/pull/252/files
    public static final Map<String, String> LEGACY_AUTHORIZATION_VALUE_MAP = Map.of(REGION_NORTH_AMERICA,
            "Basic ZDc2NmI1MzctYTY1NC00Y2JkLWEzZGMtMGNhNTY3MmQ3ZjhkOjE1ZjY5N2Y2LWE1ZDUtNGNhZC05OWQ5LTNhMTViYzdmMzk3Mw==",
            REGION_CHINA,
            "Basic blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanliTEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==",
            REGION_ROW,
            "Basic ZDc2NmI1MzctYTY1NC00Y2JkLWEzZGMtMGNhNTY3MmQ3ZjhkOjE1ZjY5N2Y2LWE1ZDUtNGNhZC05OWQ5LTNhMTViYzdmMzk3Mw==");

    public static final String LEGACY_CREDENTIAL_VALUES = "nQv6CqtxJuXWP74xf3CJwUEP:1zDHx6un4cDjybLENN3kyfumX2kEYigWPcQpdvDRpIBk7rOJ";
    public static final String LEGACY_REDIRECT_URI_VALUE = "https://www.bmw-connecteddrive.com/app/static/external-dispatch.html";
    public static final String LEGACY_SCOPE_VALUES = "authenticate_user vehicle_data remote_services";
    public static final String LEGACY_CLIENT_ID = "dbf0a542-ebd1-4ff0-a9a7-55172fbfce35";

    public static final String LEGACY_REFERER_URL = "https://www.bmw-connecteddrive.de/app/index.html";

    public static final String AUTH_SERVER_NORTH_AMERICA = "login.bmwusa.com/gcdm";
    public static final String AUTH_SERVER_CHINA = "customer.bmwgroup.cn/gcdm";
    public static final String AUTH_SERVER_ROW = "customer.bmwgroup.com/gcdm";
    public static final Map<String, String> AUTH_SERVER_MAP = Map.of(REGION_NORTH_AMERICA, AUTH_SERVER_NORTH_AMERICA,
            REGION_CHINA, AUTH_SERVER_CHINA, REGION_ROW, AUTH_SERVER_ROW);

    public static final Map<String, String> AUTHORIZATION_VALUE_MAP = Map.of(REGION_NORTH_AMERICA,
            "Basic NTQzOTRhNGItYjZjMS00NWZlLWI3YjItOGZkM2FhOTI1M2FhOmQ5MmYzMWMwLWY1NzktNDRmNS1hNzdkLTk2NmY4ZjAwZTM1MQ==",
            REGION_CHINA,
            "Basic blF2NkNxdHhKdVhXUDc0eGYzQ0p3VUVQOjF6REh4NnVuNGNEanliTEVOTjNreWZ1bVgya0VZaWdXUGNRcGR2RFJwSUJrN3JPSg==",
            REGION_ROW,
            "Basic MzFjMzU3YTAtN2ExZC00NTkwLWFhOTktMzNiOTcyNDRkMDQ4OmMwZTMzOTNkLTcwYTItNGY2Zi05ZDNjLTg1MzBhZjY0ZDU1Mg==");

    public static final Map<String, String> CODE_VERIFIER = Map.of(REGION_NORTH_AMERICA,
            "BKDarcVUpgymBDCgHDH0PwwMfzycDxu1joeklioOhwXA", REGION_CHINA, Constants.EMPTY, REGION_ROW,
            "7PsmfPS5MpaNt0jEcPpi-B7M7u0gs1Nzw6ex0Y9pa-0");

    public static final Map<String, String> CLIENT_ID = Map.of(REGION_NORTH_AMERICA,
            "54394a4b-b6c1-45fe-b7b2-8fd3aa9253aa", REGION_CHINA, Constants.EMPTY, REGION_ROW,
            "31c357a0-7a1d-4590-aa99-33b97244d048");

    public static final Map<String, String> STATE = Map.of(REGION_NORTH_AMERICA, "rgastJbZsMtup49-Lp0FMQ", REGION_CHINA,
            Constants.EMPTY, REGION_ROW, "cEG9eLAIi6Nv-aaCAniziE_B6FPoobva3qr5gukilYw");

    public static final String REDIRECT_URI_VALUE = "com.bmw.connected://oauth";
    public static final String SCOPE_VALUES = "openid profile email offline_access smacc vehicle_data perseus dlm svds cesim vsapi remote_services fupo authenticate_user";
}
