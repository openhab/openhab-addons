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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;

/**
 * {@link CarNetBrandApiSkoda} provides the Skoda specific functions of the API, portal URL us
 * https://www.skoda-connect.com
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandApiSkoda extends CarNetApiBase {
    public CarNetBrandApiSkoda(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        super(httpClient, tokenManager);
    }

    @Override
    public CarNetApiProperties getProperties() {
        CarNetApiProperties properties = new CarNetApiProperties();
        properties.brand = "VW"; // CNAPI_BRAND_SKODA;
        properties.xcountry = "CZ";
        properties.apiDefaultUrl = "";
        properties.clientId = "7f045eee-7003-4379-9968-9355ed2adb06@apps_vw-dilab_com";
        properties.xClientId = "28cd30c6-dee7-4529-a0e6-b1e07ff90b79";
        properties.authScope = "openid profile phone address cars email birthdate badge dealers driversLicense mbb";
        properties.redirect_uri = "skodaconnect://oidc.login/";
        properties.xrequest = "cz.skodaauto.connect";
        properties.responseType = "token id_token";
        properties.xappVersion = "3.2.6";
        properties.xappName = "cz.skodaauto.connect";
        return properties;
    }
}
