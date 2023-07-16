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
package org.openhab.binding.connectedcar.internal.api.skodae;

import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_FORM_URLENC;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_SKODA_E;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_VW_TOKEN_URL;
import static org.openhab.binding.connectedcar.internal.util.Helpers.fromJson;

import javax.ws.rs.core.HttpHeaders;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.OAuthToken;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.IdentityOAuthFlow;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetSkoda;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;

/**
 * {@link BrandSkodaE} provides the Brand interface for Skoda Enyak
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BrandSkodaE extends SkodaEApi implements BrandAuthenticator {
    private final static String API_URL = "https://api.connect.skoda-auto.cz/api";
    private static ApiBrandProperties properties = new ApiBrandProperties();
    static {
        properties.userAgent = "OneConnect/000000023 CFNetwork/978.0.7 Darwin/18.7.0";
        properties.apiDefaultUrl = API_URL;
        properties.brand = API_BRAND_SKODA_E;
        properties.xcountry = "CZ";
        properties.clientId = "f9a2359a-b776-46d9-bd0c-db1904343117@apps_vw-dilab_com";
        properties.xClientId = "28cd30c6-dee7-4529-a0e6-b1e07ff90b79";
        properties.xrequest = "cz.skodaauto.connect";
        properties.redirect_uri = "skodaconnect://oidc.login/";
        properties.responseType = "code token id_token";
        properties.authScope = "openid profile";
        properties.tokenUrl = CNAPI_VW_TOKEN_URL;
        properties.tokenRefreshUrl = "https://tokenrefreshservice.apps.emea.vwapps.io";
        properties.xappVersion = "3.2.6";
        properties.xappName = "cz.skodaauto.connect";
    }

    public BrandSkodaE(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public ApiBrandProperties getProperties() {
        // Properties for the Skoda-E native API
        // required to get the vehicle list
        return properties;
    }

    @Override
    public @Nullable ApiBrandProperties getProperties2() {
        // The vehicle API uses a different endpoint / client id
        ApiBrandProperties properties = BrandCarNetSkoda.getSkodaProperties();
        properties.brand = API_BRAND_SKODA_E;
        properties.apiDefaultUrl = API_URL;
        properties.authScope = "openid profile address cars email birthdate badge mbb phone driversLicense dealers profession vin mileage";
        properties.responseType = "code id_token";
        properties.clientId = "7f045eee-7003-4379-9968-9355ed2adb06@apps_vw-dilab_com";
        return properties;
    }

    @Override
    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException {
        String json = oauth.clearHeader().header(HttpHeader.HOST, "tokenrefreshservice.apps.emea.vwapps.io")//
                .header(HttpHeaders.CONTENT_TYPE, CONTENT_TYPE_FORM_URLENC).clearData().data("auth_code", oauth.code)
                .data("id_token", oauth.idToken).data("brand", "skoda") //
                .post("https://tokenrefreshservice.apps.emea.vwapps.io/exchangeAuthCode", false).response;
        return new ApiIdentity(fromJson(gson, json, OAuthToken.class));
    }

    @Override
    public String[] getImageUrls() throws ApiException {
        if (config.vstatus.imageUrls.length == 0) {
            try {
                String idToken = tokenManager.createProfileToken(config);
                // JwtToken jwt = decodeJwt(idToken);
                String json = super.callApi("", API_URL + "vehicles/{2}",
                        fillAppHeaders(tokenManager.createProfileToken(config)), "getImageUrls", String.class);
            } catch (ApiException e) {

            }
        }
        return config.vstatus.imageUrls;
    }
}
