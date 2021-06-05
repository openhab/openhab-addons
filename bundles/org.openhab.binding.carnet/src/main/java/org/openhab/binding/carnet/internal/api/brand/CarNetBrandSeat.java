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

import static org.openhab.binding.carnet.internal.api.CarNetApiConstants.CNAPI_BRAND_SEAT;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;

/**
 * {@link CarNetBrandSeat} provides the SEAT specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandSeat extends CarNetApiBase {
    public static final String CNAPI_BASE_URL_VW = "https://msg.volkswagen.de/fs-car/";

    public CarNetBrandSeat(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        super(httpClient, tokenManager);
    }

    @Override
    public CarNetApiProperties getProperties() {
        CarNetApiProperties properties = new CarNetApiProperties();
        properties.brand = CNAPI_BRAND_SEAT;
        properties.xcountry = "ES";
        properties.apiDefaultUrl = ""; // NAPI_BASE_URL_VW;
        properties.clientId = "50f215ac-4444-4230-9fb1-fe15cd1a9bcc@apps_vw-dilab_com";
        properties.xClientId = "9dcc70f0-8e79-423a-a3fa-4065d99088b4";
        properties.authScope = "openid profile mbb cars birthdate nickname address phone";
        properties.redirect_uri = "seatconnect://identity-kit/login";
        properties.xrequest = "cz.skodaauto.connect";
        properties.responseType = "code id_token";
        properties.xappName = "SEATConnect";
        properties.xappVersion = "1.1.29";
        return properties;
    }
}
