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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiConstants.CNAPI_VW_TOKEN_URL;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.BrandApiProperties;
import org.openhab.binding.connectedcar.internal.api.TokenManager;

/**
 * {@link BrandCarNetSeat} provides the SEAT specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BrandCarNetSeat extends CarNetApi {
    public BrandCarNetSeat(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        BrandApiProperties properties = new BrandApiProperties();
        properties.brand = "VW"; // CNAPI_BRAND_SEAT;
        properties.xcountry = "ES";
        properties.apiDefaultUrl = "";
        properties.clientId = "50f215ac-4444-4230-9fb1-fe15cd1a9bcc@apps_vw-dilab_com";
        properties.xClientId = "9dcc70f0-8e79-423a-a3fa-4065d99088b4";
        properties.authScope = "openid profile mbb cars birthdate nickname address phone";
        properties.tokenUrl = CNAPI_VW_TOKEN_URL;
        properties.tokenRefreshUrl = properties.tokenUrl;
        properties.redirect_uri = "seatconnect://identity-kit/login";
        properties.xrequest = "cz.skodaauto.connect";
        properties.responseType = "code id_token";
        properties.xappName = "SEATConnect";
        properties.xappVersion = "1.1.29";
        return properties;
    }
}
