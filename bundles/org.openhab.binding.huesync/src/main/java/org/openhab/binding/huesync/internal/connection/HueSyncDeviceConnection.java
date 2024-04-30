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

import java.io.IOException;
import java.net.URISyntaxException;
import java.security.cert.CertificateException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.HueSyncConstants.ENDPOINTS;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDetailedDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceInfo;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequest;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Handles the connection to a Hue HDMI Sync Box using the official API.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncDeviceConnection {
    private HueSyncConnection connection;
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncDeviceConnection.class);

    public HueSyncDeviceConnection(HttpClient httpClient, String host, Integer port)
            throws CertificateException, IOException, URISyntaxException {

        this.connection = new HueSyncConnection(httpClient, host, port);
    }

    public @Nullable HueSyncDeviceInfo getDeviceInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.DEVICE, "", HueSyncDetailedDeviceInfo.class)
                : this.connection.executeGetRequest(ENDPOINTS.DEVICE, HueSyncDeviceInfo.class);
    }

    public @Nullable HueSyncDetailedDeviceInfo getDetailedDeviceInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.DEVICE, "", HueSyncDetailedDeviceInfo.class)
                : null;
    }

    public @Nullable HueSyncRegistration registerDevice(@Nullable String id) {
        if (id == null || id.isBlank()) {
            return null;
        }

        HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();

        dto.appName = HueSyncConstants.APPLICATION_NAME;
        dto.instanceName = id;

        try {
            String json = HueSyncConnection.ObjectMapper.writeValueAsString(dto);
            HueSyncRegistration registration = this.connection.executeRequest(HttpMethod.POST, ENDPOINTS.REGISTRATIONS,
                    json, HueSyncRegistration.class);

            Optional.ofNullable(registration).ifPresent((obj) -> {
                Optional.ofNullable(obj.accessToken).ifPresent((token) -> {
                    this.connection.setAuthentication(token);
                });
            });
            return registration;
        } catch (JsonProcessingException e) {
            this.logger.error("{}", e.getMessage());
        }

        return null;
    }

    public boolean isRegistered() {
        return this.connection.isRegistered();
    }

    public boolean unregisterDevice() {
        return this.connection.unregisterDevice();
    }

    public void dispose() {
        this.connection.dispose();
    }

    public void updateConfig(HueSyncConfiguration config) {
        this.connection.updateConfig(config);
    }
}
