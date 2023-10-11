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
package org.openhab.binding.mybmw.internal.utils;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BimmerConstants} This class holds the important constants for the BMW Connected Drive Authorization. They
 * are taken from the Bimmercode from github <a href="https://github.com/bimmerconnected/bimmer_connected">
 * https://github.com/bimmerconnected/bimmer_connected</a>.
 * File defining these constants
 * <a href="https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py">
 * https://github.com/bimmerconnected/bimmer_connected/blob/master/bimmer_connected/account.py</a>
 * <a href="https://customer.bmwgroup.com/one/app/oauth.js">https://customer.bmwgroup.com/one/app/oauth.js</a>
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class BimmerConstants {

    public static final String REGION_NORTH_AMERICA = "NORTH_AMERICA";
    public static final String REGION_CHINA = "CHINA";
    public static final String REGION_ROW = "ROW";

    public static final String BRAND_BMW = "bmw";
    public static final String BRAND_MINI = "mini";
    public static final List<String> ALL_BRANDS = List.of(BRAND_BMW, BRAND_MINI);

    public static final String OAUTH_ENDPOINT = "/gcdm/oauth/authenticate";

    public static final String EADRAX_SERVER_NORTH_AMERICA = "cocoapi.bmwgroup.us";
    public static final String EADRAX_SERVER_ROW = "cocoapi.bmwgroup.com";
    public static final String EADRAX_SERVER_CHINA = "myprofile.bmw.com.cn";
    public static final Map<String, String> EADRAX_SERVER_MAP = Map.of(REGION_NORTH_AMERICA,
            EADRAX_SERVER_NORTH_AMERICA, REGION_CHINA, EADRAX_SERVER_CHINA, REGION_ROW, EADRAX_SERVER_ROW);

    public static final String OCP_APIM_KEY_NORTH_AMERICA = "31e102f5-6f7e-7ef3-9044-ddce63891362";
    public static final String OCP_APIM_KEY_ROW = "4f1c85a3-758f-a37d-bbb6-f8704494acfa";
    public static final Map<String, String> OCP_APIM_KEYS = Map.of(REGION_NORTH_AMERICA, OCP_APIM_KEY_NORTH_AMERICA,
            REGION_ROW, OCP_APIM_KEY_ROW);

    public static final String CHINA_PUBLIC_KEY = "/eadrax-coas/v1/cop/publickey";
    public static final String CHINA_LOGIN = "/eadrax-coas/v1/login/pwd";

    // Http variables
    public static final String USER_AGENT = "Dart/2.14 (dart:io)";
    public static final String X_USER_AGENT = "android(SP1A.210812.016.C1);%s;2.5.2(14945);%s";

    public static final String LOGIN_NONCE = "login_nonce";
    public static final String AUTHORIZATION_CODE = "authorization_code";

    // Parameters for API Requests
    public static final String TIRE_GUARD_MODE = "tireGuardMode";
    public static final String APP_DATE_TIME = "appDateTime";
    public static final String APP_TIMEZONE = "apptimezone";

    // API endpoints
    public static final String API_OAUTH_CONFIG = "/eadrax-ucs/v1/presentation/oauth/config";
    public static final String API_VEHICLES = "/eadrax-vcs/v1/vehicles";
    public static final String API_REMOTE_SERVICE_BASE_URL = "/eadrax-vrccs/v2/presentation/remote-commands/"; // '/{vin}/{service_type}'
    public static final String API_POI = "/eadrax-dcs/v1/send-to-car/send-to-car";
}
