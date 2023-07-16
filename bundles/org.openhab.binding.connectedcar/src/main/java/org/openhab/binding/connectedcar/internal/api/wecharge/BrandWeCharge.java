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
package org.openhab.binding.connectedcar.internal.api.wecharge;

import static org.openhab.binding.connectedcar.internal.BindingConstants.CONTENT_TYPE_JSON;
import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_WECHARGE;
import static org.openhab.binding.connectedcar.internal.util.Helpers.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpHeader;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity;
import org.openhab.binding.connectedcar.internal.api.ApiIdentity.OAuthToken;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.IdentityOAuthFlow;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;

/**
 * {@link BrandWeCharge} provides the Brand interface for WeCharge
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BrandWeCharge extends WeChargeApi {
    static ApiBrandProperties properties = new ApiBrandProperties();
    static {
        properties.brand = API_BRAND_WECHARGE;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://wecharge.apps.emea.vwapps.io";
        properties.loginUrl = properties.apiDefaultUrl + "/user-identity/v2/identity/login";
        properties.clientId = "0fa5ae01-ebc0-4901-a2aa-4dd60572ea0e@apps_vw-dilab_com";
        properties.authUserAttr = "identifier";
        properties.authScope = "openid";
        properties.redirect_uri = "wecharge://authenticated";
        properties.xrequest = "com.volkswagen.wecharge";
        properties.responseType = "code";
        properties.userAgent = "WeConnect/5 CFNetwork/1206 Darwin/20.1.0";

        properties.stdHeaders.put(HttpHeader.USER_AGENT.toString(), properties.userAgent);
        properties.stdHeaders.put(HttpHeader.ACCEPT.toString(), "*/*");
        properties.stdHeaders.put(HttpHeader.ACCEPT_LANGUAGE.toString(), "de-de");
        properties.stdHeaders.put(HttpHeader.CONTENT_TYPE.toString(), CONTENT_TYPE_JSON);
        properties.stdHeaders.put("x-newrelic-id", "VgAEWV9QDRAEXFlRAAYPUA==");

        properties.loginHeaders.putAll(properties.stdHeaders);
        properties.loginHeaders.put("x-api-key", "yabajourasW9N8sm+9F/oP==");

        properties.stdHeaders.put(HttpHeader.ACCEPT_LANGUAGE.toString(), "en-US,en;q=0.9,de;q=0.8");
        properties.stdHeaders.put(HttpHeader.CACHE_CONTROL.toString(), "no-cache");
        properties.stdHeaders.put("content-version", "1");
    }

    public BrandWeCharge(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public ApiBrandProperties getProperties() {
        return properties;
    }

    @Override
    public String getLoginUrl(IdentityOAuthFlow oauth) throws ApiException {
        return config.api.issuerRegionMappingUrl + "/oidc/v1/authorize?" //
                + "client_id=" + urlEncode(config.api.clientId).replace("%20", "+") //
                + "&scope=openid&response_type=" + urlEncode(config.api.responseType).replace("%20", "+") //
                + "&redirect_uri=" + urlEncode(config.api.redirect_uri).replace("%20", "+");
    }

    @Override
    public IdentityOAuthFlow updateSigninParameters(IdentityOAuthFlow oauth) {
        return oauth.data("registerFlow", "false");
    }

    @Override
    public ApiIdentity grantAccess(IdentityOAuthFlow oauth) throws ApiException {
        String url = "https://wecharge.apps.emea.vwapps.io/user-identity/v1/identity/login?redirect_uri="
                + config.api.redirect_uri + "&code=" + oauth.code;
        String json = oauth.clearHeader().headers(config.api.loginHeaders).get(url).response;
        return new ApiIdentity(fromJson(gson, json, OAuthToken.class));
    }
}
