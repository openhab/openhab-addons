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
import java.util.Objects;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.binding.huesync.internal.HueSyncConstants;
import org.openhab.binding.huesync.internal.HueSyncConstants.ENDPOINTS;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecution;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmi;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistration;
import org.openhab.binding.huesync.internal.api.dto.registration.HueSyncRegistrationRequest;
import org.openhab.binding.huesync.internal.config.HueSyncConfiguration;
import org.openhab.binding.huesync.internal.exceptions.HueSyncConnectionException;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Handles the connection to a Hue HDMI Sync Box using the official API.
 * 
 * @author Patrik Gfeller - Initial Contribution
 */
@NonNullByDefault
public class HueSyncDeviceConnection {
    private final Logger logger = LoggerFactory.getLogger(HueSyncDeviceConnection.class);

    private final HueSyncConnection connection;

    private final Map<String, Consumer<Command>> deviceCommandExecutors = new HashMap<>();

    public HueSyncDeviceConnection(HttpClient httpClient, HueSyncConfiguration configuration)
            throws CertificateException, IOException, URISyntaxException {
        this.connection = new HueSyncConnection(httpClient, configuration.host, configuration.port);

        registerCommandHandlers();
    }

    // #region private

    private void registerCommandHandlers() {
        this.deviceCommandExecutors.put(HueSyncConstants.CHANNELS.COMMANDS.MODE,
                defaultHandler(HueSyncConstants.ENDPOINTS.COMMANDS.MODE));
        this.deviceCommandExecutors.put(HueSyncConstants.CHANNELS.COMMANDS.SOURCE,
                defaultHandler(HueSyncConstants.ENDPOINTS.COMMANDS.SOURCE));
        this.deviceCommandExecutors.put(HueSyncConstants.CHANNELS.COMMANDS.BRIGHTNESS,
                defaultHandler(HueSyncConstants.ENDPOINTS.COMMANDS.BRIGHTNESS));
        this.deviceCommandExecutors.put(HueSyncConstants.CHANNELS.COMMANDS.SYNC,
                defaultHandler(HueSyncConstants.ENDPOINTS.COMMANDS.SYNC));
        this.deviceCommandExecutors.put(HueSyncConstants.CHANNELS.COMMANDS.HDMI,
                defaultHandler(HueSyncConstants.ENDPOINTS.COMMANDS.HDMI));
    }

    private Consumer<Command> defaultHandler(String endpoint) {
        return command -> {
            execute(endpoint, command);
        };
    }

    private void execute(String key, Command command) {
        this.logger.debug("Command executor: {} - {}", key, command);

        if (!this.connection.isRegistered()) {
            this.logger.warn("Device is not registered - ignoring command: {}", command);
            return;
        }

        String value;

        if (command instanceof QuantityType quantityCommand) {
            value = Integer.toString(quantityCommand.intValue());
        } else if (command instanceof OnOffType) {
            value = command == OnOffType.ON ? "true" : "false";
        } else if (command instanceof StringType) {
            value = '"' + command.toString() + '"';
        } else {
            this.logger.warn("Type [{}] not supported by this connection", command.getClass().getCanonicalName());
            return;
        }

        String json = String.format("{ \"%s\": %s }", key, value);

        this.connection.executeRequest(HttpMethod.PUT, ENDPOINTS.EXECUTION, json, null);
    }

    // #endregion

    public void executeCommand(Channel channel, Command command) {
        String uid = channel.getUID().getAsString();
        String commandId = channel.getUID().getId();

        this.logger.debug("Channel UID: {} - Command: {}", uid, command.toFullString());

        if (RefreshType.REFRESH.equals(command)) {
            return;
        }

        if (this.deviceCommandExecutors.containsKey(commandId)) {
            Objects.requireNonNull(this.deviceCommandExecutors.get(commandId)).accept(command);
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

    public @Nullable HueSyncRegistration registerDevice(String id) throws HueSyncConnectionException {
        if (!id.isBlank()) {
            try {
                HueSyncRegistrationRequest dto = new HueSyncRegistrationRequest();
                dto.appName = HueSyncConstants.APPLICATION_NAME;
                dto.instanceName = id;

                String payload = HueSyncConnection.OBJECT_MAPPER.writeValueAsString(dto);

                HueSyncRegistration registration = this.connection.executeRequest(HttpMethod.POST,
                        ENDPOINTS.REGISTRATIONS, payload, HueSyncRegistration.class);
                if (registration != null) {
                    this.connection.updateAuthentication(id, registration.accessToken);

                    return registration;
                }
            } catch (JsonProcessingException e) {
                this.logger.warn("{}", e.getMessage());
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
        this.logger.debug("Connection configuration update for device {}:{} - Registration Id [{}]", config.host,
                config.port, config.registrationId);

        this.connection.updateAuthentication(config.registrationId, config.apiAccessToken);
    }
}
