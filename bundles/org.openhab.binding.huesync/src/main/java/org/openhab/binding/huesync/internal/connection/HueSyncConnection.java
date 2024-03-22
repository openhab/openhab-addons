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

    private static class ENDPOINTS {
        public static final String DEVICE = "device";
        public static final String REGISTRATIONS = "registrations";
    }

    private static final ObjectMapper ObjectMapper = new ObjectMapper();
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncConnection.class);

    private @NonNull HttpClient httpClient;

    private HueSyncDeviceInfo deviceInfo;
    private HueSyncConfiguration config;

    public HueSyncConnection(@NonNull HttpClient httpClient, @NonNull HueSyncConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    /**
     * TODO: Add log, debug and trace information ...
     * 
     * @return
     * 
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     */
    @SuppressWarnings("null")
    public @Nullable HueSyncDeviceInfo getDeviceInfo()
            throws InterruptedException, ExecutionException, TimeoutException {

        ContentResponse response = this.executeGetRequest(ENDPOINTS.DEVICE);

        if (response.getStatus() == HttpStatus.OK_200) {
            String payload = response.getContentAsString();

            logger.trace("getDeviceInfo: {}", payload);

            try {
                this.deviceInfo = ObjectMapper.readValue(payload, HueSyncDeviceInfo.class);
            } catch (JsonProcessingException e) {

            }
        }

        return this.deviceInfo;
    }

    /**
     * Try to register the application with the device.
     * 
     * @return null || HueSyncRegistration
     * 
     * @throws JsonProcessingException
     * @throws InterruptedException
     * @throws TimeoutException
     * @throws ExecutionException
     */
    public @Nullable HueSyncRegistration registerDevice()
            throws JsonProcessingException, InterruptedException, TimeoutException, ExecutionException {

        HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();

        dto.appName = HueSyncConstants.APPLICATION_NAME;
        dto.instanceName = this.deviceInfo.uniqueId;

        String payload = ObjectMapper.writeValueAsString(dto);
        ContentResponse response = this.executeJsonRequest(HttpMethod.POST, ENDPOINTS.REGISTRATIONS, payload, 500);

        // TODO: Check if "this.httpClient.setAuthenticationStore(null)" makes sense to
        // be used in this context ...

        return (response.getStatus() == HttpStatus.OK_200)
                ? ObjectMapper.readValue(response.getContentAsString(), HueSyncRegistration.class)
                : null;
    }

    private ContentResponse executeGetRequest(String endpoint)
            throws InterruptedException, ExecutionException, TimeoutException {
        String uri = String.format(REQUEST_FORMAT, this.config.host, this.config.port, API, endpoint);

        return httpClient.GET(uri);
    }

    private ContentResponse executeJsonRequest(HttpMethod method, String endpoint, String payload,
            Integer timeoutInMillisecons) throws InterruptedException, TimeoutException, ExecutionException {

        String uri = String.format(REQUEST_FORMAT, this.config.host, this.config.port, API, endpoint);

        Request request = this.httpClient.newRequest(uri).method(method);

        if (!payload.isBlank()) {
            request.header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                    .content(new StringContentProvider(payload));
        }

        if (!this.config.apiAccessToken.isBlank()) {
            request.header(HttpHeader.AUTHORIZATION, "Bearer " + this.config.apiAccessToken);
        }

        return request.timeout(timeoutInMillisecons, TimeUnit.MILLISECONDS).send();
    }
}
