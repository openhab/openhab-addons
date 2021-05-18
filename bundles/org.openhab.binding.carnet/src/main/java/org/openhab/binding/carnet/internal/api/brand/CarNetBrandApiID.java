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

import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_BRAND_VWID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;

/**
 * {@link CarNetBrandApiID} provides the VW ID.3/ID.4 specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandApiID extends CarNetApiBase {
    public CarNetBrandApiID(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        super(httpClient, tokenManager);
    }

    @Override
    public CarNetApiProperties getProperties() {
        CarNetApiProperties properties = new CarNetApiProperties();
        properties.brand = CNAPI_BRAND_VWID;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://mobileapi.apps.emea.vwapps.io";
        properties.oidcConfigUrl = "https://identity.vwgroup.io/.well-known/openid-configuration";
        properties.clientId = "a24fba63-34b3-4d43-b181-942111e6bda8@apps_vw-dilab_com";
        properties.xClientId = "1e63bd93-ce66-4aa3-b373-0ec56247e1d7";
        properties.authScope = "openid profile badge cars dealers vin";
        properties.redirect_uri = "weconnect://authenticated";
        properties.xrequest = "com.volkswagen.weconnect";
        properties.responseType = "code id_token token";
        properties.xappName = "";
        properties.xappVersion = "";
        return properties;
    }
    /*
     * @Override
     * public String updateAuthorizationUrl(String url) throws CarNetException {
     * String codeVerifier = generateCodeVerifier();
     * String codeChallenge = generateCodeChallange(codeVerifier);
     * return url + "&prompt=login&code_challenge_method=s256&code_challenge=" + codeChallenge;
     * }
     *
     * }
     */
}
