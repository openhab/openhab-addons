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

import static org.openhab.binding.carnet.internal.BindingConstants.CNAPI_BRAND_WECHARGE;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.ApiEventListener;
import org.openhab.binding.carnet.internal.api.ApiHttpClient;
import org.openhab.binding.carnet.internal.api.TokenManager;
import org.openhab.binding.carnet.internal.api.weconnect.WeConnectApi;

/**
 * {@link BrandWeCharge} provides the Brand interface for WeCharge
 *
 * @author Markus Michels - Initial contribution
 */
public class BrandWeCharge extends WeConnectApi {

    public BrandWeCharge(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        BrandApiProperties properties = new BrandApiProperties();
        properties.brand = CNAPI_BRAND_WECHARGE;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://mobileapi.apps.emea.vwapps.io";
        properties.oidcConfigUrl = "https://identity.vwgroup.io/.well-known/openid-configuration";
        properties.clientId = "0fa5ae01-ebc0-4901-a2aa-4dd60572ea0e@apps_vw-dilab_com";
        properties.xClientId = "";
        properties.authScope = "openid profile";
        properties.redirect_uri = "wecharge://authenticated";
        properties.xrequest = "com.volkswagen.weconnect";
        properties.responseType = "code id_token token";
        properties.xappName = "";
        properties.xappVersion = "";
        return properties;
    }
}
