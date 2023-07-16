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
package org.openhab.binding.connectedcar.internal.api.carnet;

import static org.openhab.binding.connectedcar.internal.api.ApiDataTypesDTO.API_BRAND_AUDI;
import static org.openhab.binding.connectedcar.internal.api.carnet.CarNetApiGSonDTO.CNAPI_VW_TOKEN_URL;
import static org.openhab.binding.connectedcar.internal.util.Helpers.getString;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.connectedcar.internal.api.ApiBrandProperties;
import org.openhab.binding.connectedcar.internal.api.ApiEventListener;
import org.openhab.binding.connectedcar.internal.api.ApiException;
import org.openhab.binding.connectedcar.internal.api.ApiHttpClient;
import org.openhab.binding.connectedcar.internal.api.BrandAuthenticator;
import org.openhab.binding.connectedcar.internal.api.IdentityManager;
import org.openhab.binding.connectedcar.internal.api.carnet.BrandCarNetAudi.AudiVehicles.AudiVehicle;
import org.openhab.binding.connectedcar.internal.config.CombinedConfig;
import org.openhab.binding.connectedcar.internal.handler.ThingHandlerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link BrandCarNetAudi} provides the Audi specific functions of the API
 *
 * @author Markus Michels - Initial contribution
 * @author Thomas Knaller - Maintainer
 * @author Dr. Yves Kreis - Maintainer
 */
@NonNullByDefault
public class BrandCarNetAudi extends CarNetApi implements BrandAuthenticator {
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

    private static ApiBrandProperties properties = new ApiBrandProperties();
    static {
        properties.brand = API_BRAND_AUDI;
        properties.xcountry = "DE";
        properties.apiDefaultUrl = "https://msg.audi.de/fs-car";
        // properties.oidcConfigUrl = "https://app-api.live-my.audi.com/myaudiappidk/v1/openid-configuration";
        properties.tokenUrl = CNAPI_VW_TOKEN_URL;
        properties.tokenRefreshUrl = properties.tokenUrl;
        properties.clientId = "09b6cbec-cd19-4589-82fd-363dfa8c24da@apps_vw-dilab_com";
        properties.xClientId = "77869e21-e30a-4a92-b016-48ab7d3db1d8";
        properties.authScope = "openid mbb vin profile gallery";
        properties.redirect_uri = "myaudi:///";
        properties.responseType = "token id_token";
        properties.xappVersion = "3.9.1";
        properties.xappName = "myAudi";
        properties.xrequest = "de.myaudi.mobile.assistant";
    }

    public BrandCarNetAudi(ThingHandlerInterface handler, ApiHttpClient httpClient, IdentityManager tokenManager,
            @Nullable ApiEventListener eventListener) {
        super(handler, httpClient, tokenManager, eventListener);
    }

    @Override
    public ApiBrandProperties getProperties() {
        return properties;
    }

    @Override
    public String updateAuthorizationUrl(String url) throws ApiException {
        return url + "&prompt=login&ui_locales=de-DE%20de";
    }

    @Override
    public CombinedConfig initialize(String vin, CombinedConfig configIn) throws ApiException {
        CombinedConfig config = super.initialize(vin, configIn);
        if (!config.vstatus.pairingInfo.isPairingCompleted()) {
            logger.warn("{}: Unable to verify pairing or pairing not completed (status {}, userId {}, code {})",
                    thingId, getString(config.vstatus.pairingInfo.pairingStatus), getString(config.user.id),
                    getString(config.vstatus.pairingInfo.pairingCode));
        }
        return config;
    }

    @Override
    public ArrayList<String> getVehicles() throws ApiException {
        return super.getVehicles();
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
                for (AudiVehicle vehicle : data.vehicles) {
                    if (config.vehicle.vin.equalsIgnoreCase(getString(vehicle.vin)) && vehicle.imageUrl != null) {
                        // for whatever reason the imageUrl is missing http: at the beginning
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
}
