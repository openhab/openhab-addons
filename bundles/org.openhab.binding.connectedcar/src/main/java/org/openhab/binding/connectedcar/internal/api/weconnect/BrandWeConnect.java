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
package org.openhab.binding.connectedcar.internal.api.weconnect;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_VWID;
import static org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApiJsonDTO.WCAPI_BASE_URL;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiHttpMap;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.OAuthToken;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.IdentityOAuthFlow;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BrandWeConnect} provides the VW ID.3/ID.4 specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BrandWeConnect extends WeConnectApi implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(BrandWeConnect.class);
    static ApiBrandProperties properties = new ApiBrandProperties();
    static {
        properties.brand = API_BRAND_VWID;
        properties.apiDefaultUrl = "https://emea.bff.cariad.digital/vehicle/v1";
        properties.userAgent = "WeConnect/5 CFNetwork/1206 Darwin/20.1.0";
        properties.xcountry = "DE";
        properties.apiDefaultUrl = WCAPI_BASE_URL;
        properties.loginUrl = "https://emea.bff.cariad.digital/user-login/v1/authorize?nonce=" + generateNonce()
                + "&redirect_uri=weconnect://authenticated";
        properties.tokenUrl = "https://emea.bff.cariad.digital/user-login/login/v1";
        properties.tokenRefreshUrl = "https://emea.bff.cariad.digital/refresh/v1";
        properties.clientId = "a24fba63-34b3-4d43-b181-942111e6bda8@apps_vw-dilab_com";
        properties.xClientId = "1e63bd93-ce66-4aa3-b373-0ec56247e1d7";
        properties.authScope = "openid cars vin profile";
        properties.redirect_uri = "weconnect://authenticated";
        properties.xrequest = "com.volkswagen.weconnect";
        properties.responseType = "code id_token token";
        properties.xappName = "";
        properties.xappVersion = "";

        properties.stdHeaders.put("x-newrelic-id", "VgAEWV9QDRAEXFlRAAYPUA==");
        properties.stdHeaders.put(HttpHeader.USER_AGENT.toString(), properties.userAgent);
        properties.stdHeaders.put(HttpHeaders.ACCEPT.toString(), "*/*");
        properties.stdHeaders.put(HttpHeader.ACCEPT_LANGUAGE.toString(), "de-de");
    }

    public BrandWeConnect(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public ApiBrandProperties getProperties() {
        properties.loginUrl = "https://emea.bff.cariad.digital/user-login/v1/authorize?nonce=" + generateNonce()
                + "&redirect_uri=weconnect://authenticated";
        return properties;
    }

    @Override
    public String getLoginUrl(IdentityOAuthFlow oauth) throws ApiException {
        return oauth.clearData().get(config.api.loginUrl).getLocation();
    }

    @Override
    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException {
        /*
         * state: jwtstate,
         * id_token: jwtid_token,
         * redirect_uri: redirerctUri,
         * region: "emea",
         * access_token: jwtaccess_token,
         * authorizationCode: jwtauth_code,
         * });
         *
         */
        logger.debug("{}: Grant API access", config.getLogId());
        String json = oauth.clearHeader().header(HttpHeader.HOST, "emea.bff.cariad.digital").clearData()//
                .data("state", oauth.state).data("id_token", oauth.idToken)
                .data("redirect_uri", config.api.redirect_uri).data("region", "emea")
                .data("access_token", oauth.accessToken).data("authorizationCode", oauth.code) //
                .post(config.api.tokenUrl, true).response;
        return new ApiIdentity(fromJson(gson, json, OAuthToken.class).normalize());
    }

    @Override
    public OAuthToken refreshToken(ApiIdentity token) throws ApiException {
        ApiHttpMap headers = new ApiHttpMap().header(HttpHeaders.AUTHORIZATION, "Bearer " + token.getRefreshToken());
        String json = http.get(config.api.tokenRefreshUrl, headers.getHeaders()).response;
        return fromJson(gson, json, OAuthToken.class);
    }
}
