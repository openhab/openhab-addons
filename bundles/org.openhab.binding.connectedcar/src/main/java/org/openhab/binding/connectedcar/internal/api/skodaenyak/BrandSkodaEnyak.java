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
package org.openhab.binding.connectedcar.internal.api.skodaenyak;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_ENYAK;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_VW_TOKEN_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiToken.JwtToken;
import org.openhab.binding.connectedcar.internal.api.BrandApiProperties;
import org.openhab.binding.connectedcar.internal.api.TokenManager;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetSkoda;

/**
 * {@link BrandSkodaEnyak} provides the Brand interface for Skoda Enyak
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BrandSkodaEnyak extends SkodaEnyakApi {
    private final static String API_URL = "https://api.connect.skoda-auto.cz/api";

    public BrandSkodaEnyak(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        // Properties for the Enyaq native API
        // required to get the vehicle list
        BrandApiProperties properties = new BrandApiProperties();
        properties.userAgent = "OneConnect/000000023 CFNetwork/978.0.7 Darwin/18.7.0";
        properties.apiDefaultUrl = API_URL;
        properties.brand = API_BRAND_ENYAK;
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
        return properties;
    }

    @Override
    public @Nullable BrandApiProperties getProperties2() {
        // The vehicle API uses a different endpoint / client id
        BrandApiProperties properties = BrandCarNetSkoda.getSkodaProperties();
        properties.brand = API_BRAND_ENYAK;
        properties.apiDefaultUrl = API_URL;
        properties.authScope = "openid profile phone address cars email birthdate badge dealers driversLicense mbb";
        properties.responseType = "code id_token";
        return properties;
    }

    @Override
    public String[] getImageUrls() throws ApiException {
        if (config.vstatus.imageUrls.length == 0) {
            try {
                // config.vstatus.imageUrls = super.callApi("",
                String idToken = tokenManager.createProfileToken(config);
                JwtToken jwt = decodeJwt(idToken);
                String json = super.callApi("", "https://api.connect.skoda-auto.cz/api/v1/vehicles/{2}",
                        fillAppHeaders(tokenManager.createProfileToken(config)), "getImageUrls", String.class);
            } catch (ApiException e) {

            }
        }
        return config.vstatus.imageUrls;
    }
}
