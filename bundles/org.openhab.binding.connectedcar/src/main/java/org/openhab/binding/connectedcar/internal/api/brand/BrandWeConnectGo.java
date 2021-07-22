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
package org.openhab.binding.connectedcar.internal.api.brand;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_VWGO;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.TokenManager;
import org.openhab.binding.connectedcar.internal.api.weconnect.WeConnectApi;

/**
 * {@link BrandWeConnectGo} provides brand interface for WeConnect Go Dataplug
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BrandWeConnectGo extends WeConnectApi {
    public BrandWeConnectGo(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        BrandApiProperties properties = new BrandApiProperties();
        properties.brand = API_BRAND_VWGO;
        properties.apiDefaultUrl = "https://mobileapi.apps.emea.vwapps.io";
        properties.clientId = "ac42b0fa-3b11-48a0-a941-43a399e7ef84@apps_vw-dilab_com";
        properties.authScope = "openid profile";
        properties.redirect_uri = "vwconnect://de.volkswagen.vwconnect/oauth2redirec/identitykit";
        properties.tokenUrl = "https://dmp.apps.emea.vwapps.io/mobility-platform/token";
        properties.tokenRefreshUrl = properties.tokenUrl;
        properties.xrequest = "com.volkswagen.weconnect";
        properties.responseType = "code id_token token";
        return properties;
    }
}
