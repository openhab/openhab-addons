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
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecution;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmi;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequest;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.log.HueSyncLogFactory;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
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

    private Map<String, Consumer<ExecutionPayload>> DeviceCommandExecutors = new HashMap<>();

    public HueSyncDeviceConnection(HttpClient httpClient, HueSyncConfiguration configuration)
            throws CertificateException, IOException, URISyntaxException {

        this.connection = new HueSyncConnection(httpClient, configuration.host, configuration.port);

        registerCommandHandlers();
    }

    // #region private

    private void registerCommandHandlers() {
        this.DeviceCommandExecutors.put(COMMANDS.MODE, defaultHandler());
        this.DeviceCommandExecutors.put(COMMANDS.SOURCE, defaultHandler());
        this.DeviceCommandExecutors.put(COMMANDS.BRIGHTNESS, defaultHandler());
        this.DeviceCommandExecutors.put(COMMANDS.SYNC, defaultHandler());
        this.DeviceCommandExecutors.put(COMMANDS.HDMI, defaultHandler());
    }

    private Consumer<ExecutionPayload> defaultHandler() {
        return payload -> {
            execute(payload.API, payload.Command);
        };
    }

    private void execute(String key, Command command) {
        this.logger.info("Command executor: {} - {}", key, command);

        String value = "";

        if (command instanceof QuantityType) {
            value = Integer.toString(((QuantityType<?>) command).intValue());
        } else if (command instanceof OnOffType) {
            value = ((OnOffType) command).name().equals("ON") ? "true" : "false";
        } else if (command instanceof StringType) {
            value = "\"" + ((StringType) command).toString() + "\"";
        } else {
            this.logger.error("Type {} not supported by this connection", command.getClass().getCanonicalName());
            return;
        }

        if (!this.connection.isRegistered()) {
            this.logger.warn("Device is not registered - ignoring command: {}", command);
            return;
        }

        String json = String.format("{ \"%s\": %s }", key, value);
        this.connection.executeRequest(HttpMethod.PUT, ENDPOINTS.EXECUTION, json, null);
    }

    // #endregion

    @SuppressWarnings("null")
    public void executeCommand(Channel channel, Command command) {
        String uid = channel.getUID().getAsString();
        String commandId = channel.getUID().getId();

        this.logger.trace("Channel UID: {} - Command: {}", uid, command.toFullString());

        if (RefreshType.REFRESH.equals(command)) {
            return;
        }

        if (this.DeviceCommandExecutors.containsKey(commandId)) {
            this.DeviceCommandExecutors.get(commandId).accept(new ExecutionPayload(commandId, command));
        } else {
            this.logger.error("No executor registered for command {} - please report this as an issue", commandId);
        }
    }

    public @Nullable HueSyncDevice getDeviceInfo() {
        return this.connection.executeGetRequest(ENDPOINTS.DEVICE, HueSyncDevice.class);
    }

    public @Nullable HueSyncDeviceDetailed getDetailedDeviceInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.DEVICE, "", HueSyncDeviceDetailed.class)
                : null;
    }

    public @Nullable HueSyncHdmi getHdmiInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.HDMI, "", HueSyncHdmi.class)
                : null;
    }

    public @Nullable HueSyncExecution getExecutionInfo() {
        return this.connection.isRegistered()
                ? this.connection.executeRequest(HttpMethod.GET, ENDPOINTS.EXECUTION, "", HueSyncExecution.class)
                : null;
    }

    public @Nullable HueSyncRegistration registerDevice(String id) {
        if (!id.isBlank()) {
            try {
                HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();
                dto.appName = HueSyncConstants.APPLICATION_NAME;
                dto.instanceName = id;

                String payload = HueSyncConnection.ObjectMapper.writeValueAsString(dto);

                HueSyncRegistration registration = this.connection.executeRequest(HttpMethod.POST,
                        ENDPOINTS.REGISTRATIONS, payload, HueSyncRegistration.class);
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
