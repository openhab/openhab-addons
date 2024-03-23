/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.huesync.internal.connection;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequest;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the connection to a Hue HDMI Sync Box using the official API.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
public class HueSyncConnection {
    /**
     * Request format: The Sync Box API can be accessed locally via HTTPS on root
     * level (port 443, /api/v1), resource level /api/v1/<resource> and in some
     * cases subresource level /api/v1/<resource>/<subresource>.
     */
    private static final String REQUEST_FORMAT = "https://%s:%s/%s/%s";
    private static final String API = "api/v1";
    private static final Integer TIMEOUT_MILLISECONDS = 500;

    private static class ENDPOINTS {
        public static final String DEVICE = "device";
        public static final String REGISTRATIONS = "registrations";
    }

    private static final ObjectMapper ObjectMapper = new ObjectMapper();
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncConnection.class);

    private @NonNull HttpClient httpClient;

    private HueSyncDeviceInfo deviceInfo;
    private HueSyncConfiguration config;

    /**
     * 
     * @param httpClient
     * @param config
     */
    public HueSyncConnection(@NonNull HttpClient httpClient, @NonNull HueSyncConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    /**
     * 
     * @return
     */
    public @Nullable HueSyncDeviceInfo getDeviceInfo() {
        try {
            ContentResponse response = this.executeGetRequest(ENDPOINTS.DEVICE);

            if (response.getStatus() == HttpStatus.OK_200) {
                this.deviceInfo = this.deserialize(response.getContentAsString(), HueSyncDeviceInfo.class);
            }

        } catch (InterruptedException | ExecutionException | TimeoutException | JsonProcessingException e) {
            this.logger.error(e.getMessage());
        }

        return this.deviceInfo;
    }

    /**
     * 
     * @return
     */
    public boolean unregisterDevice() {
        if (!this.config.apiAccessToken.isBlank() && !this.config.registrationId.isBlank()) {
            try {
                String endpoint = ENDPOINTS.REGISTRATIONS + "/" + this.config.registrationId;
                ContentResponse response = this.executeRequest(HttpMethod.DELETE, endpoint);

                return response.getStatus() == HttpStatus.OK_200;
            } catch (InterruptedException | TimeoutException | ExecutionException e) {
                this.logger.error(e.getMessage());
            }
        }
        return false;
    }

    /**
     * Try to register the application with the device.
     * 
     * @return null || HueSyncRegistration
     */
    public @Nullable HueSyncRegistration registerDevice() {
        HueSyncRegistration registration = null;

        try {
            HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();

            dto.appName = HueSyncConstants.APPLICATION_NAME;
            dto.instanceName = this.deviceInfo.uniqueId;

            String json = ObjectMapper.writeValueAsString(dto);

            ContentResponse response = this.executeRequest(HttpMethod.POST, ENDPOINTS.REGISTRATIONS, json);

            if (response.getStatus() == HttpStatus.OK_200) {
                registration = this.deserialize(response.getContentAsString(), HueSyncRegistration.class);
            }
        } catch (InterruptedException | TimeoutException | ExecutionException | JsonProcessingException e) {
            this.logger.error(e.getMessage());
        }

        return registration;
    }

    // #region - private

    private <T> T deserialize(String json, Class<T> type) throws JsonMappingException, JsonProcessingException {
        return ObjectMapper.readValue(json, type);
    }

    private ContentResponse executeGetRequest(String endpoint)
            throws InterruptedException, ExecutionException, TimeoutException {
        String uri = String.format(REQUEST_FORMAT, this.config.host, this.config.port, API, endpoint);

        return httpClient.GET(uri);
    }

    private ContentResponse executeRequest(HttpMethod method, String endpoint)
            throws InterruptedException, TimeoutException, ExecutionException {
        return this.executeRequest(method, endpoint, "");
    }

    /**
     * Executes a REST API call, with an optional json payload.
     * 
     * @param method
     * @param endpoint
     * @param payload
     * 
     * @return
     * 
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    private ContentResponse executeRequest(HttpMethod method, String endpoint, String payload)
            throws InterruptedException, TimeoutException, ExecutionException {

        String uri = String.format(REQUEST_FORMAT, this.config.host, this.config.port, API, endpoint);

        Request request = this.httpClient.newRequest(uri).method(method);

        if (!payload.isBlank()) {
            request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                    .content(new StringContentProvider(payload));
        }

        if (!this.config.apiAccessToken.isBlank()) {
            // TODO: Check if httpClient.setAuthenticationStore makes sense ...
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.config.apiAccessToken);
        }

        return request.timeout(TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS).send();
    }

    // #endregion
}
