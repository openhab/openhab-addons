/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.lgthinq.lgservices;

import static org.openhab.binding.lgthinq.lgservices.LGServicesConstants.LG_API_PLATFORM_TYPE_V1;

import java.util.Map;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.lgservices.api.RestResult;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.core.io.net.http.HttpClientFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Creates specialized API clients.
 *
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
public class LGThinQApiClientServiceFactory {

    private static <T> T versioned(String platformType, HttpClientFactory httpClientFactory,
            Function<HttpClient, T> v1Factory, Function<HttpClient, T> v2Factory) {
        HttpClient http = httpClientFactory.getCommonHttpClient();
        return LG_API_PLATFORM_TYPE_V1.equals(platformType) ? v1Factory.apply(http) : v2Factory.apply(http);
    }

    /**
     * Creates a general-purpose API client (used for gateway/bridge operations).
     *
     * @param httpClientFactory the HTTP client factory provided by openHAB
     * @return a new {@link LGThinQGeneralApiClientService}
     */
    public static LGThinQGeneralApiClientService newGeneralApiClientService(HttpClientFactory httpClientFactory) {
        return new LGThinQGeneralApiClientService(httpClientFactory.getCommonHttpClient());
    }

    /**
     * Creates an air-conditioner API client for the given platform version.
     *
     * @param lgPlatformType {@code "thinq1"} for V1 or {@code "thinq2"} for V2
     * @param httpClientFactory the HTTP client factory provided by openHAB
     * @return a V1 or V2 implementation of {@link LGThinQACApiClientService}
     */
    public static LGThinQACApiClientService newACApiClientService(String lgPlatformType,
            HttpClientFactory httpClientFactory) {
        return versioned(lgPlatformType, httpClientFactory, LGThinQACApiV1ClientServiceImpl::new,
                LGThinQACApiV2ClientServiceImpl::new);
    }

    /**
     * Creates a fridge API client for the given platform version.
     *
     * @param lgPlatformType {@code "thinq1"} for V1 or {@code "thinq2"} for V2
     * @param httpClientFactory the HTTP client factory provided by openHAB
     * @return a V1 or V2 implementation of {@link LGThinQFridgeApiClientService}
     */
    public static LGThinQFridgeApiClientService newFridgeApiClientService(String lgPlatformType,
            HttpClientFactory httpClientFactory) {
        return versioned(lgPlatformType, httpClientFactory, LGThinQFridgeApiV1ClientServiceImpl::new,
                LGThinQFridgeApiV2ClientServiceImpl::new);
    }

    /**
     * Creates a washer/dryer API client for the given platform version.
     *
     * @param lgPlatformType {@code "thinq1"} for V1 or {@code "thinq2"} for V2
     * @param httpClientFactory the HTTP client factory provided by openHAB
     * @return a V1 or V2 implementation of {@link LGThinQWMApiClientService}
     */
    public static LGThinQWMApiClientService newWMApiClientService(String lgPlatformType,
            HttpClientFactory httpClientFactory) {
        return versioned(lgPlatformType, httpClientFactory, LGThinQWMApiV1ClientServiceImpl::new,
                LGThinQWMApiV2ClientServiceImpl::new);
    }

    /**
     * Creates a dishwasher API client for the given platform version.
     *
     * @param lgPlatformType {@code "thinq1"} for V1 or {@code "thinq2"} for V2
     * @param httpClientFactory the HTTP client factory provided by openHAB
     * @return a V1 or V2 implementation of {@link LGThinQDishWasherApiClientService}
     */
    public static LGThinQDishWasherApiClientService newDishWasherApiClientService(String lgPlatformType,
            HttpClientFactory httpClientFactory) {
        return versioned(lgPlatformType, httpClientFactory, LGThinQDishWasherApiV1ClientServiceImpl::new,
                LGThinQDishWasherApiV2ClientServiceImpl::new);
    }

    /**
     * General-purpose API client used for gateway/bridge operations (device listing, authentication).
     * Does not support device-specific commands.
     */
    public static final class LGThinQGeneralApiClientService
            extends LGThinQAbstractApiClientService<GenericCapability, AbstractSnapshotDefinition> {

        private LGThinQGeneralApiClientService(HttpClient httpClient) {
            super(GenericCapability.class, AbstractSnapshotDefinition.class, httpClient);
        }

        @Override
        public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected boolean beforeGetDataDevice(String bridgeName, String deviceId) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
                String command, String keyName, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey,
                String command, @Nullable String keyName, @Nullable String value, @Nullable ObjectNode extraNode) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Map<String, Object> handleGenericErrorResult(@Nullable RestResult resp) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class GenericCapability extends AbstractCapability<GenericCapability> {

    }
}
