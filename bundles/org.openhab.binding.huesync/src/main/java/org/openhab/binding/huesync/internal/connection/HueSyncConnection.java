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
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.openhab.binding.huesync.internal.api.dto.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Handles the connection to a Hue HDMI Sync Box using the official API.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
public class HueSyncConnection {
    private static final String REQUEST_FORMAT = "https://%s:%s/%s";
    private static final String DEVICE_INFO_ENDPOINT = "api/v1/device";

    private static final ObjectMapper ObjectMapper = new ObjectMapper();

    private @NonNull HttpClient httpClient;
    private HueSyncConfiguration config;

    public HueSyncConnection(@NonNull HttpClient httpClient, @NonNull HueSyncConfiguration config) {
        this.httpClient = httpClient;
        this.config = config;
    }

    public void start() throws Exception {
        this.httpClient.start();
    }

    @SuppressWarnings("null")
    public HueSyncDeviceInfo getDeviceInfo() throws InterruptedException, ExecutionException, TimeoutException,
            JsonMappingException, JsonProcessingException {
        String request = String.format(REQUEST_FORMAT, this.config.host, this.config.port, DEVICE_INFO_ENDPOINT);

        ContentResponse response = this.httpClient.GET(request);
        String json = response.getContentAsString();

        return ObjectMapper.readValue(json, HueSyncDeviceInfo.class);
    }
}
