/**
 * Copyright (c) 2024-2024 Contributors to the openHAB project
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
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.MimeTypes;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequest;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final String RESOURCE = "device";
    private static final String REGISTRATIONS = "registrations";

    private static final ObjectMapper ObjectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(HueSyncConnection.class);

    private @NonNull HttpClient httpClient;

    private HueSyncDeviceInfo deviceInfo;
    private HueSyncConfiguration config;

    public HueSyncConnection(@NonNull HttpClient httpClient, @NonNull HueSyncConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    @SuppressWarnings("null")
    public HueSyncDeviceInfo getDeviceInfo() throws InterruptedException, ExecutionException, TimeoutException,
            JsonMappingException, JsonProcessingException {

        String uri = String.format(REQUEST_FORMAT, this.config.host, this.config.port, API, RESOURCE);

        ContentResponse response = this.httpClient.GET(uri);
        String json = response.getContentAsString();

        logger.trace("getDeviceInfo: {}", json);

        this.deviceInfo = ObjectMapper.readValue(json, HueSyncDeviceInfo.class);

        return this.deviceInfo;
    }

    public void registerDevice()
            throws JsonProcessingException, InterruptedException, TimeoutException, ExecutionException {

        HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();

        dto.appName = HueSyncConstants.APPLICATION_NAME;
        dto.instanceName = this.deviceInfo.uniqueId;

        String uri = String.format(REQUEST_FORMAT, this.config.host, this.config.port, API, REGISTRATIONS);
        String json = ObjectMapper.writeValueAsString(dto);

        ContentResponse response = this.httpClient.newRequest(uri).method(HttpMethod.POST)
                .header(HttpHeader.CONTENT_TYPE, MimeTypes.Type.APPLICATION_JSON.asString())
                .content(new StringContentProvider(json)).timeout(500, TimeUnit.MILLISECONDS).send();

        logger.trace("registerDevice", response);
    }
}
