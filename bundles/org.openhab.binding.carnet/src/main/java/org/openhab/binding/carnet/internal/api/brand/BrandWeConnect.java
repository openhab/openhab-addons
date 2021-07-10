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

import static org.openhab.binding.carnet.internal.BindingConstants.CNAPI_BRAND_VWID;
import static org.openhab.binding.carnet.internal.CarUtils.generateNonce;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.ApiEventListener;
import org.openhab.binding.carnet.internal.api.ApiException;
import org.openhab.binding.carnet.internal.api.ApiHttpClient;
import org.openhab.binding.carnet.internal.api.TokenManager;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiGSonDTO.CarNetImageUrlsVW;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApi;

/**
 * {@link BrandWeConnect} provides the VW ID.3/ID.4 specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BrandWeConnect extends WeConnectApi {
    public BrandWeConnect(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        BrandApiProperties properties = new BrandApiProperties();
        String nonce = generateNonce();
        properties.brand = CNAPI_BRAND_VWID;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://mobileapi.apps.emea.vwapps.io";
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
