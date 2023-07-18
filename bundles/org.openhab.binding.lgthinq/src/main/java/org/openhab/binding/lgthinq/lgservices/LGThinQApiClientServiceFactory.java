/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import static org.openhab.binding.lgthinq.internal.LGThinQBindingConstants.PLATFORM_TYPE_V1;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.lgthinq.internal.api.RestResult;
import org.openhab.binding.lgthinq.internal.errors.LGThinqApiException;
import org.openhab.binding.lgthinq.lgservices.model.AbstractCapability;
import org.openhab.binding.lgthinq.lgservices.model.AbstractSnapshotDefinition;
import org.openhab.binding.lgthinq.lgservices.model.DevicePowerState;
import org.openhab.core.io.net.http.HttpClientFactory;

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Creates expecialized API clients.
 * 
 * @author Julio Gesser
 */
public class LGThinQApiClientServiceFactory {

    public static LGThinQGeneralApiClientService newGeneralApiClientService(HttpClientFactory httpClientFactory) {
        return new LGThinQGeneralApiClientService(httpClientFactory.getCommonHttpClient());
    }

    public static LGThinQACApiClientService newACApiClientService(String lgPlatformType, HttpClientFactory httpClientFactory) {
        return lgPlatformType.equals(PLATFORM_TYPE_V1)
                ? new LGThinQACApiV1ClientServiceImpl(httpClientFactory.getCommonHttpClient())
                : new LGThinQACApiV2ClientServiceImpl(httpClientFactory.getCommonHttpClient());
    }

    public static LGThinQFridgeApiClientService newFridgeApiClientService(String lgPlatformType, HttpClientFactory httpClientFactory) {
        return lgPlatformType.equals(PLATFORM_TYPE_V1)
                ? new LGThinQFridgeApiV1ClientServiceImpl(httpClientFactory.getCommonHttpClient())
                : new LGThinQFridgeApiV2ClientServiceImpl(httpClientFactory.getCommonHttpClient());
    }

    public static LGThinQWMApiClientService newWMApiClientService(String lgPlatformType, HttpClientFactory httpClientFactory) {
        return lgPlatformType.equals(PLATFORM_TYPE_V1)
                ? new LGThinQWMApiV1ClientServiceImpl(httpClientFactory.getCommonHttpClient())
                : new LGThinQWMApiV2ClientServiceImpl(httpClientFactory.getCommonHttpClient());
    }

    @NonNullByDefault
    public static final class LGThinQGeneralApiClientService extends LGThinQAbstractApiClientService<GenericCapability, AbstractSnapshotDefinition> {

        private LGThinQGeneralApiClientService(HttpClient httpClient) {
            super(GenericCapability.class, AbstractSnapshotDefinition.class, httpClient);
        }

        @Override
        public void turnDevicePower(String bridgeName, String deviceId, DevicePowerState newPowerState) throws LGThinqApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void beforeGetDataDevice(@NonNull String bridgeName, @NonNull String deviceId) throws LGThinqApiException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey, String command, String keyName, String value) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        protected RestResult sendCommand(String bridgeName, String deviceId, String controlPath, String controlKey, String command, @Nullable String keyName, @Nullable String value, @Nullable ObjectNode extraNode) throws Exception {
            throw new UnsupportedOperationException();
        }

        @Override
        protected Map<String, Object> handleGenericErrorResult(@Nullable RestResult resp) throws LGThinqApiException {
            throw new UnsupportedOperationException();
        }
    }

    @NonNullByDefault
    private static final class GenericCapability extends AbstractCapability<GenericCapability> {

    }
}
