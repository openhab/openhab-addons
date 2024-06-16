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
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.HueSyncConstants.CHANNELS.COMMANDS;
import org.openhab.binding.huesync.internal.HueSyncConstants.ENDPOINTS;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDto;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDtoDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecutionDto;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmiDto;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationDto;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequestDto;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.types.Command;
import org.slf4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Handles the connection to a Hue HDMI Sync Box using the official API.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncDeviceConnection {
    private final Logger logger = HueSyncLogFactory.getLogger(HueSyncDeviceConnection.class);

    private HueSyncConnection connection;

    public Map<String, Consumer<Command>> DeviceCommandsExecutors = new HashMap<>();

    public HueSyncDeviceConnection(HttpClient httpClient, HueSyncConfiguration configuration)
            throws CertificateException, IOException, URISyntaxException {

        this.connection = new HueSyncConnection(httpClient, configuration.host, configuration.port);

        // TODO: POC - create real implementation to handle simple commands ...
        this.DeviceCommandsExecutors.put(COMMANDS.MODE, command -> {
            this.logger.info("Command executor: {}", command);

            if (!this.connection.isRegistered())
                return;

            String json = String.format("{\"mode\": \"%s\"}", command.toFullString());
            this.connection.executeRequest(HttpMethod.PUT, ENDPOINTS.EXECUTION, json, null);
        });
    }

    public @Nullable HueSyncDeviceDto getDeviceInfo() {
        return this.connection.executeGetRequest(ENDPOINTS.DEVICE, HueSyncDeviceDto.class);
    }

    public @Nullable HueSyncDeviceDtoDetailed getDetailedDeviceInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.DEVICE, "", HueSyncDeviceDtoDetailed.class)
                : null;
    }

    public @Nullable HueSyncHdmiDto getHdmiInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.HDMI, "", HueSyncHdmiDto.class)
                : null;
    }

    public @Nullable HueSyncExecutionDto getExecutionInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.EXECUTION, "", HueSyncExecutionDto.class)
                : null;
    }

    public @Nullable HueSyncRegistrationDto registerDevice(String id) {
        if (!id.isBlank()) {
            try {
                HueSyncRegistrationRequestDto dto = new HueSyncRegistrationRequestDto();
                dto.appName = HueSyncConstants.APPLICATION_NAME;
                dto.instanceName = id;

                String payload = HueSyncConnection.ObjectMapper.writeValueAsString(dto);

                HueSyncRegistrationDto registration = this.connection.executeRequest(HttpMethod.POST,
                        ENDPOINTS.REGISTRATIONS, payload, HueSyncRegistrationDto.class);
                if (registration != null) {
                    this.connection.updateAuthentication(id, registration.accessToken);

                    return registration;
                }
            } catch (JsonProcessingException e) {
                this.logger.error("{}", e.getMessage());
            }
        }
        return null;
    }

    public boolean isRegistered() {
        return this.connection.isRegistered();
    }

    public void unregisterDevice() {
        this.connection.unregisterDevice();
    }

    public void dispose() {
        this.connection.dispose();
    }

    public void updateConfiguration(HueSyncConfiguration config) {
        this.logger.debug("🔧 Connection configuration update for device {}:{} - Registration Id [{}]", config.host,
                config.port, config.registrationId);

        this.connection.updateAuthentication(config.registrationId, config.apiAccessToken);
    }
}
