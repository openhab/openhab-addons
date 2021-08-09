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
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.CarUtils.*;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_VWID;
import static org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCAPI_BASE_URL;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.ApiToken;
import org.openhab.binding.connectedcar.internal.api.ApiToken.OAuthToken;
import org.openhab.binding.connectedcar.internal.api.BrandApiProperties;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.TokenManager;
import org.openhab.binding.connectedcar.internal.api.TokenOAuthFlow;
import org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CarNetImageUrlsVW;

/**
 * {@link BrandWeConnect} provides the VW ID.3/ID.4 specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BrandWeConnect extends WeConnectApi implements BrandAuthenticator {
    public BrandWeConnect(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        BrandApiProperties properties = new BrandApiProperties();
        String nonce = generateNonce();
        properties.brand = API_BRAND_VWID;

        properties.userAgent = "WeConnect/5 CFNetwork/1206 Darwin/20.1.0";
        properties.xcountry = "DE";
        properties.apiDefaultUrl = WCAPI_BASE_URL;
        properties.loginUrl = "https://login.apps.emea.vwapps.io/authorize?nonce=" + nonce
                + "&redirect_uri=weconnect://authenticated";
        properties.tokenUrl = properties.apiDefaultUrl + "/login/v1";
        properties.tokenRefreshUrl = "https://login.apps.emea.vwapps.io/refresh/v1";
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

    @Override
    public String getLoginUrl(TokenOAuthFlow oauth) throws ApiException {
        return oauth.clearData().get(config.api.loginUrl).getLocation();
    }

    @Override
    public ApiToken grantAccess(TokenOAuthFlow oauth) throws ApiException {
        /*
         * state: jwtstate,
         * id_token: jwtid_token,
         * redirect_uri: redirerctUri,
         * region: "emea",
         * access_token: jwtaccess_token,
         * authorizationCode: jwtauth_code,
         * });
         */
        String json = oauth.clearHeader().header(HttpHeader.HOST, "login.apps.emea.vwapps.io")//
                .clearData().data("state", oauth.state).data("id_token", oauth.idToken)
                .data("redirect_uri", config.api.redirect_uri).data("region", "emea")
                .data("access_token", oauth.accessToken).data("authorizationCode", oauth.code) //
                .post("https://login.apps.emea.vwapps.io/login/v1", true).response;
        return new ApiToken(fromJson(gson, json, OAuthToken.class).normalize());
    }

    @Override
    public OAuthToken refreshToken(ApiToken token) throws ApiException {
        ApiHttpMap headers = new ApiHttpMap().header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getRefreshToken());
        String json = http.get(config.api.tokenRefreshUrl, headers.getHeaders()).response;
        return fromJson(gson, json, OAuthToken.class);
    }

    @Override
    public String[] getImageUrls() throws ApiException {
        if (config.vstatus.imageUrls.length == 0) {
            config.vstatus.imageUrls = super.callApi("",
                    "https://vehicle-image.apps.emea.vwapps.io/vehicleimages/exterior/{2}",
                    fillAppHeaders(tokenManager.createProfileToken(config)), "getImageUrls",
                    CarNetImageUrlsVW.class).imageUrls;
        }
        return config.vstatus.imageUrls;
    }
}
