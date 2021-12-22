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
package org.openhab.binding.mybmw.internal.utils;

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

    public static final String REGION_NORTH_AMERICA = "NORTH_AMERICA";
    public static final String REGION_CHINA = "CHINA";
    public static final String REGION_ROW = "ROW";

    public static final String OAUTH_ENDPOINT = "/gcdm/oauth/authenticate";

    public static final String EADRAX_SERVER_NORTH_AMERICA = "cocoapi.bmwgroup.us";
    public static final String EADRAX_SERVER_ROW = "cocoapi.bmwgroup.com";
    public static final String EADRAX_SERVER_CHINA = "myprofile.bmw.com.cn";
    public static final Map<String, String> EADRAX_SERVER_MAP = Map.of(REGION_NORTH_AMERICA,
            EADRAX_SERVER_NORTH_AMERICA, REGION_CHINA, EADRAX_SERVER_CHINA, REGION_ROW, EADRAX_SERVER_ROW);

    public static final String OCP_APIM_KEY_NORTH_AMERICA = "31e102f5-6f7e-7ef3-9044-ddce63891362";
    public static final String OCP_APIM_KEY_ROW = "4f1c85a3-758f-a37d-bbb6-f8704494acfa";
    public static final String OCP_APIM_KEY_CHINA = Constants.EMPTY;
    public static final Map<String, String> OCP_APIM_KEYS = Map.of(REGION_NORTH_AMERICA, OCP_APIM_KEY_NORTH_AMERICA,
            REGION_ROW, OCP_APIM_KEY_ROW, REGION_CHINA, OCP_APIM_KEY_CHINA);

    // Http variables
    public static final String USER_AGENT_BMW = "android(v1.07_20200330);bmw;1.7.0(11152)";
    public static final String USER_AGENT_MINI = "android(v1.07_20200330);mini;1.7.0(11152)";
    public static final String LOGIN_NONCE = "login_nonce";
    public static final String AUTHORIZATION_CODE = "authorization_code";

    // API endpoints
    public static final String API_OAUTH_CONFIG = "/eadrax-ucs/v1/presentation/oauth/config";
    public static final String API_VEHICLES = "/eadrax-vcs/v1/vehicles";
    public static final String API_REMOTE_SERVICE_BASE_URL = "/eadrax-vrccs/v2/presentation/remote-commands/"; // '/{vin}/{service_type}'
    public static final String vehicleEADRXPoiUrl = "/eadrax-dcs/v1/send-to-car/send-to-car";

}
