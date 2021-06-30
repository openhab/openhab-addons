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

import static org.openhab.binding.carnet.internal.BindingConstants.CNAPI_BRAND_AUDI;
import static org.openhab.binding.carnet.internal.CarUtils.getString;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.carnet.internal.api.ApiEventListener;
import org.openhab.binding.carnet.internal.api.ApiException;
import org.openhab.binding.carnet.internal.api.ApiHttpClient;
import org.openhab.binding.carnet.internal.api.TokenManager;
import org.openhab.binding.carnet.internal.api.carnet.CarNetApiBase;
import org.openhab.binding.carnet.internal.config.CombinedConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BrandCarNetAudi} provides the Audi specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class BrandCarNetAudi extends CarNetApiBase implements BrandAuthenticator {
    private final Logger logger = LoggerFactory.getLogger(BrandCarNetAudi.class);

    public static class AudiServiceUrls {
        public static class BaseUrl {
            public @Nullable String baseUrl;
        }

        public static class Service {
            public @Nullable String baseUrl;
            public @Nullable BaseUrl emea;
            public @Nullable BaseUrl ap;
            public @Nullable BaseUrl na;
        }

        public class IssuerRegionMapping {
            public @Nullable String emea;
            public @Nullable String ap;
            public @Nullable String na;
        }

        public @Nullable Service customerProfileService;
        public @Nullable Service consentService;
        public @Nullable IssuerRegionMapping issuerRegionMapping;
    }

    public static class AudiVehicles {
        public static class AudiVehicle {
            public @Nullable String type;
            public @Nullable Boolean favorite;
            public @Nullable String vin;
            public @Nullable String commissionNumber;
            public @Nullable String shortName;
            public @Nullable String imageUrl;
        }

        public @Nullable ArrayList<AudiVehicle> vehicles;
    }

    private static BrandApiProperties properties = new BrandApiProperties();
    static {
        properties.brand = CNAPI_BRAND_AUDI;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://msg.audi.de/fs-car";
        properties.oidcConfigUrl = "https://app-api.live-my.audi.com/myaudiappidk/v1/openid-configuration";
        properties.clientId = "09b6cbec-cd19-4589-82fd-363dfa8c24da@apps_vw-dilab_com";
        properties.xClientId = "77869e21-e30a-4a92-b016-48ab7d3db1d8";
        properties.authScope = "openid mbb vin profile name nickname address";
        properties.redirect_uri = "myaudi:///";
        properties.responseType = "token id_token";
        properties.xappVersion = "3.22.0";
        properties.xappName = "myAudi";
        properties.xrequest = "de.myaudi.mobile.assistant";
    }

    public BrandCarNetAudi(ApiHttpClient httpClient, TokenManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(httpClient, tokenManager, eventListener);
    }

    @Override
    public BrandApiProperties getProperties() {
        return properties;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws ApiException {
        return url + "&prompt=login&ui_locales=de-DE%20de";
    }

    @Override
    public CombinedConfig initialize(String vin, CombinedConfig configIn) throws ApiException {
        CombinedConfig cfg = super.initialize(vin, configIn);

        if (!cfg.vstatus.pairingInfo.isPairingCompleted()) {
            logger.warn("{}: Unable to verify pairing or pairing not completed (status {}, userId {}, code {})",
                    thingId, getString(config.vstatus.pairingInfo.pairingStatus), getString(config.user.id),
                    getString(config.vstatus.pairingInfo.pairingCode));
        }
        return cfg;
    }

    @Override
    public String[] getImageUrls() throws ApiException {
        if (config.vstatus.imageUrls.length == 0) {
            // config.vstatus.imageUrls =
            Map<String, String> headers = fillAppHeaders(tokenManager.createProfileToken(config));
            headers.put("x-market", "de_DE");
            AudiVehicles data = super.callApi("", "https://api.my.audi.com/smns/v1/navigation/v1/vehicles", headers,
                    "getImageUrls", AudiVehicles.class);
            String[] imageUrls = new String[1];
            if (data.vehicles != null) {
                for (AudiVehicles.AudiVehicle vehicle : data.vehicles) {
                    if (config.vehicle.vin.equalsIgnoreCase(getString(vehicle.vin))) {
                        // for whatever the imageUrl is missing http: at the beginning
                        imageUrls[0] = vehicle.imageUrl.startsWith("//") ? "https:" + vehicle.imageUrl
                                : vehicle.imageUrl;
                        break;
                    }
                }
            }
            if (imageUrls.length > 0) {
                config.vstatus.imageUrls = imageUrls;
            }
        }
        return config.vstatus.imageUrls;
    }
    /*
     * try {
     * AudiServiceUrls urls = super.callApi(
     * "https://featureapps.audi.com/audi-env-config/0/config/myaudi/livem1/idk.json", "getServiceUrls",
     * AudiServiceUrls.class);
     * if (urls.consentService != null && urls.consentService.emea != null
     * && urls.consentService.emea.baseUrl != null) {
     * properties.customerProfileServiceUrl = urls.consentService.emea.baseUrl;
     * }
     * if (urls.issuerRegionMapping != null && urls.issuerRegionMapping.emea != null) {
     * properties.issuerRegionMappingUrl = urls.issuerRegionMapping.emea;
     * }
     * } catch (CarNetException e) {
     * logger.debug("{}: Unable to get Service URLs", properties.brand);
     * }
     *
     */

    /*
     * App token generation - incomplete, untested
     *
     * // Otherwise we just got an auhorization code and need to request the token
     * if (authCode.isEmpty()) {
     * logger.debug("{}: Unable to obtain authCode, last url={}, last response: {}", config.vehicle.vin,
     * url, html);
     * throw new CarNetSecurityException("Unable to complete OAuth, check credentials");
     * }
     *
     * logger.trace("{}: OAuth successful, obtain ID token (auth code={})", config.vehicle.vin, authCode);
     * headers.clear();
     * headers.put(HttpHeader.ACCEPT.toString(), "application/json, text/plain, *"); <- change
     * headers.put(HttpHeader.CONTENT_TYPE.toString(), "application/json");
     * headers.put(HttpHeader.USER_AGENT.toString(), "okhttp/3.7.0");
     *
     * long tsC = parseDate(config.api.oidcDate);
     * // long n = System.currentTimeMillis();
     * long ts1 = System.currentTimeMillis() - tsC;
     * long ts2 = System.currentTimeMillis();
     * long ts = ts1 + ts2;
     * String s = ((Long) (ts / 100000)).toString();
     * headers.put("X-QMAuth", "v1:934928ef:" + s);
     *
     * data.clear();
     * data.put("client_id", config.api.clientId);
     * data.put("grant_type", "authorization_code");
     * data.put("code", authCode);
     * data.put("redirect_uri", config.api.redirect_uri);
     * data.put("response_type", "token id_token");
     * json = http.post(CNAPI_AUDI_TOKEN_URL, headers, data, true);
     *
     * // process token
     * token = fromJson(gson, json, CNApiToken.class);
     * if ((token.accessToken == null) || token.accessToken.isEmpty()) {
     * throw new CarNetSecurityException("Authentication failed: Unable to get id token!");
     * }
     *
     * tokens.idToken = new CarNetToken(token);
     * }
     */
}
