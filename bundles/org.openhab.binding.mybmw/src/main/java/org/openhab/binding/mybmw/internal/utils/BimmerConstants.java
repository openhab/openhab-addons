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
 * @author Martin Grassl - update to v2 API
 */
@NonNullByDefault
public interface BimmerConstants {

    static final String REGION_NORTH_AMERICA = "NORTH_AMERICA";
    static final String REGION_CHINA = "CHINA";
    static final String REGION_ROW = "ROW";

    static final String BRAND_BMW = "bmw";
    static final String BRAND_BMWI = "bmw_i";
    static final String BRAND_MINI = "mini";
    static final List<String> REQUESTED_BRANDS = List.of(BRAND_BMW, BRAND_MINI);

    static final String OAUTH_ENDPOINT = "/gcdm/oauth/authenticate";
    static final String AUTH_PROVIDER = "gcdm";

    static final String EADRAX_SERVER_NORTH_AMERICA = "cocoapi.bmwgroup.us";
    static final String EADRAX_SERVER_ROW = "cocoapi.bmwgroup.com";
    static final String EADRAX_SERVER_CHINA = "myprofile.bmw.com.cn";
    static final Map<String, String> EADRAX_SERVER_MAP = Map.of(REGION_NORTH_AMERICA, EADRAX_SERVER_NORTH_AMERICA,
            REGION_CHINA, EADRAX_SERVER_CHINA, REGION_ROW, EADRAX_SERVER_ROW);

    static final String OCP_APIM_KEY_NORTH_AMERICA = "31e102f5-6f7e-7ef3-9044-ddce63891362";
    static final String OCP_APIM_KEY_ROW = "4f1c85a3-758f-a37d-bbb6-f8704494acfa";
    static final Map<String, String> OCP_APIM_KEYS = Map.of(REGION_NORTH_AMERICA, OCP_APIM_KEY_NORTH_AMERICA,
            REGION_ROW, OCP_APIM_KEY_ROW);

    static final String CHINA_PUBLIC_KEY = "/eadrax-coas/v1/cop/publickey";
    static final String CHINA_LOGIN = "/eadrax-coas/v2/login/pwd";

    // Http variables
    static final String APP_VERSION_NORTH_AMERICA = "2.12.0(19883)";
    static final String APP_VERSION_ROW = "2.12.0(19883)";
    static final String APP_VERSION_CHINA = "2.3.0(13603)";
    static final Map<String, String> APP_VERSIONS = Map.of(REGION_NORTH_AMERICA, APP_VERSION_NORTH_AMERICA, REGION_ROW,
            APP_VERSION_ROW, REGION_CHINA, APP_VERSION_CHINA);
    static final String USER_AGENT = "Dart/2.16 (dart:io)";
    // see const.py of bimmer_constants: user-agent; brand; app_version; region
    static final String X_USER_AGENT = "android(SP1A.210812.016.C1);%s;%s;%s";

    static final String LOGIN_NONCE = "login_nonce";
    static final String AUTHORIZATION_CODE = "authorization_code";

    // Parameters for API Requests
    static final String TIRE_GUARD_MODE = "tireGuardMode";
    static final String APP_DATE_TIME = "appDateTime";
    static final String APP_TIMEZONE = "apptimezone";

    // API endpoints
    static final String API_OAUTH_CONFIG = "/eadrax-ucs/v1/presentation/oauth/config";
    static final String API_VEHICLES = "/eadrax-vcs/v4/vehicles";
    static final String API_REMOTE_SERVICE_BASE_URL = "/eadrax-vrccs/v3/presentation/remote-commands/"; // '/{vin}/{service_type}'
    static final String API_POI = "/eadrax-dcs/v1/send-to-car/send-to-car";
}
