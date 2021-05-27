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
package org.openhab.binding.carnet.internal.api.brand;

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.CNAPI_BRAND_AUDI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetBrandAuthenticator;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;

/**
 * {@link CarNetBrandApiAudi} provides the Audi specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandApiAudi extends CarNetApiBase implements CarNetBrandAuthenticator {
    public static final String CNAPI_AUDI_TOKEN_URL = "https://app-api.my.audi.com/myaudiappidk/v1/token";
    public static final String CNAPI_URL_AUDI_GET_TOKEN = "https://id.audi.com/v1/token";
    public static final String CNAPI_AUDIURL_OPERATIONS = "https://msg.audi.de/myaudi/vehicle-management/v2/vehicles";

    private static CarNetApiProperties properties = new CarNetApiProperties();
    static {
        properties.brand = CNAPI_BRAND_AUDI;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://msg.audi.de/fs-car";
        properties.oidcConfigUrl = "https://app-api.live-my.audi.com/myaudiappidk/v1/openid-configuration";
        properties.clientId = "09b6cbec-cd19-4589-82fd-363dfa8c24da@apps_vw-dilab_com";
        properties.xClientId = "77869e21-e30a-4a92-b016-48ab7d3db1d8";
        properties.authScope = "address profile badge birthdate birthplace nationalIdentifier nationality profession email vin phone nickname name picture mbb gallery openid";
        properties.redirect_uri = "myaudi:///";
        properties.responseType = "token id_token";
        properties.xappVersion = "3.22.0";
        properties.xappName = "myAudi";
        properties.xrequest = "de.myaudi.mobile.assistant";
    }

    public CarNetBrandApiAudi(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        super(httpClient, tokenManager);
    }

    @Override
    public CarNetApiProperties getProperties() {

        return properties;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws CarNetException {
        return url + "&prompt=login&ui_locales=de-DE%20de";
    }

    /*
     * App token generation - incomplete, untested
     *
     * // Otherwise we just got an auhorization code and need to request the token
     * if (authCode.isEmpty()) {
     * logger.debug("{}: Unable to obtain authCode, last url={}, last response: {}", config.vehicle.vin,
     * url, html);
     * throw new CarNetSecurityException("Unable to complete OAuth, check credentials");
     * }
     *
     * logger.trace("{}: OAuth successful, obtain ID token (auth code={})", config.vehicle.vin, authCode);
     * headers.clear();
     * headers.put(HttpHeader.ACCEPT.toString(), "application/json, text/plain, *"); <- change
     * headers.put(HttpHeader.CONTENT_TYPE.toString(), "application/json");
     * headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0");
     *
     * long tsC = parseDate(config.api.oidcDate);
     * // long n = System.currentTimeMillis();
     * long ts1 = System.currentTimeMillis() - tsC;
     * long ts2 = System.currentTimeMillis();
     * long ts = ts1 + ts2;
     * String s = ((Long) (ts / 100000)).toString();
     * headers.put("X-QMAuth", "v1:934928ef:" + s);
     *
     * data.clear();
     * data.put("client_id", config.api.clientId);
     * data.put("grant_type", "authorization_code");
     * data.put("code", authCode);
     * data.put("redirect_uri", config.api.redirect_uri);
     * data.put("response_type", "token id_token");
     * json = http.post(CNAPI_AUDI_TOKEN_URL, headers, data, true);
     *
     * // process token
     * token = fromJson(gson, json, CNApiToken.class);
     * if ((token.accessToken == null) || token.accessToken.isEmpty()) {
     * throw new CarNetSecurityException("Authentication failed: Unable to get id token!");
     * }
     *
     * tokens.idToken = new CarNetToken(token);
     * }
     */
}
