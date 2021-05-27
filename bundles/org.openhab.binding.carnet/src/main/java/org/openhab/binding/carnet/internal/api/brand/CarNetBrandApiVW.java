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

import static org.openhab.binding.carnet.internal.CarNetBindingConstants.CNAPI_BRAND_VW;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.carnet.internal.CarNetException;
import org.openhab.binding.carnet.internal.api.CarNetApiBase;
import org.openhab.binding.carnet.internal.api.CarNetApiProperties;
import org.openhab.binding.carnet.internal.api.CarNetBrandAuthenticator;
import org.openhab.binding.carnet.internal.api.CarNetHttpClient;
import org.openhab.binding.carnet.internal.api.CarNetTokenManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link CarNetBrandApiVW} provides the VW specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class CarNetBrandApiVW extends CarNetApiBase implements CarNetBrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(CarNetBrandApiVW.class);

    public CarNetBrandApiVW(CarNetHttpClient httpClient, CarNetTokenManager tokenManager) {
        super(httpClient, tokenManager);
    }

    @Override
    public CarNetApiProperties getProperties() {
        CarNetApiProperties properties = new CarNetApiProperties();
        properties.brand = CNAPI_BRAND_VW;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://msg.volkswagen.de/fs-car";
        properties.clientId = "9496332b-ea03-4091-a224-8c746b885068@apps_vw-dilab_com";
        properties.xClientId = "38761134-34d0-41f3-9a73-c4be88d7d337";
        properties.authScope = "openid profile mbb email cars birthdate address vin";
        properties.redirect_uri = "carnet://identity-kit/login";
        properties.xrequest = "de.volkswagen.carnet.eu.eremote";
        properties.responseType = "id_token token";
        properties.xappName = "eRemote";
        properties.xappVersion = "5.1.2";
        properties.xappId = "de.volkswagen.car-net.eu.e-remote";
        return properties;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws CarNetException {
        return url + "&prompt=login"; // + "&code_challenge=" + codeChallenge + "&code_challenge_method=S256";
    }
}
