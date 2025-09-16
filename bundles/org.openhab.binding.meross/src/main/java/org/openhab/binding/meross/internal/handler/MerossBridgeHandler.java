/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.meross.internal.handler;

import java.net.ConnectException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.meross.internal.api.MerossCloudHttpConnector;
import org.openhab.binding.meross.internal.api.MerossHttpConnector;
import org.openhab.binding.meross.internal.api.MerossMqttConnector;
import org.openhab.binding.meross.internal.config.MerossBridgeConfiguration;
import org.openhab.binding.meross.internal.discovery.MerossDiscoveryService;
import org.openhab.binding.meross.internal.dto.CloudCredentials;
import org.openhab.binding.meross.internal.dto.Device;
import org.openhab.binding.meross.internal.dto.MqttMessageBuilder;
import org.openhab.binding.meross.internal.exception.MerossApiException;
import org.openhab.core.io.transport.mqtt.MqttException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MerossBridgeHandler} is responsible for handling http communication with and retrieve data from Meross
 * Host.
 *
 * @author Giovanni Fabiani - Initial contribution
 * @author Mark Herwege - Refactor initialization
 * @author Mark Herwege - Refactor discovery
 * @author Mark Herwege - Use common http client
 */
@NonNullByDefault
public class MerossBridgeHandler extends BaseBridgeHandler {

    private MerossBridgeConfiguration config = new MerossBridgeConfiguration();
    private @NonNullByDefault({}) MerossCloudHttpConnector merossHttpConnector;
    private @NonNullByDefault({}) MerossMqttConnector merossMqttConnector;
    private @Nullable MerossDiscoveryService discoveryService;
    private final HttpClient httpClient;

    private @Nullable CloudCredentials credentials;
    private List<Device> devices = List.of();

    private final Logger logger = LoggerFactory.getLogger(MerossBridgeHandler.class);

    public MerossBridgeHandler(Thing thing, HttpClient httpClient) {
        super((Bridge) thing);
        this.httpClient = httpClient;
    }

    @Override
    public void initialize() {
        config = getConfigAs(MerossBridgeConfiguration.class);

        if (config.hostName.isBlank() || config.userEmail.isBlank() || config.userPassword.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
            return;
        }

        merossHttpConnector = (MerossCloudHttpConnector) new MerossHttpConnector.Builder().httpClient(httpClient)
                .setApiBaseUrl(config.hostName).setUserEmail(config.userEmail).setUserPassword(config.userPassword)
                .build();
        scheduler.submit(() -> fetchAndInitialize());
    }

    @Override
    public void dispose() {
        if (merossMqttConnector != null) {
            merossMqttConnector.stopConnection();
        }
        super.dispose();
    }

    private void fetchAndInitialize() {
        try {
            // To keep to one http conversation, explicitly login and logout at beginning and end. This will avoid each
            // check and get to login and logout.
            merossHttpConnector.login();
            merossHttpConnector.checkApiStatus();
            credentials = merossHttpConnector.getCredentials();

            initializeMerossMqttConnector();

            updateStatus(ThingStatus.ONLINE);

            MerossDiscoveryService discoveryService = this.discoveryService;
            if (discoveryService != null) {
                discoveryService.discoverDevices();
            }
            merossHttpConnector.logout();
        } catch (ConnectException | MerossApiException | MqttException | InterruptedException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(MerossDiscoveryService.class);
    }

    public void setDiscoveryService(MerossDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
        if (ThingStatus.ONLINE.equals(thing.getStatus())) {
            scheduler.submit(() -> discoveryService.discoverDevices());
        }
    }

    public synchronized List<Device> discoverDevices() throws ConnectException {
        if (ThingStatus.ONLINE.equals(thing.getStatus())) {
            devices = merossHttpConnector.getDevices();
            return devices;
        }
        return List.of();
    }

    public List<Device> getDevices() {
        return devices;
    }

    public @Nullable MerossMqttConnector getMerossMqttConnector() {
        return merossMqttConnector;
    }

    /**
     * Initializes the mqtt connector
     *
     * @throws MqttException
     * @throws InterruptedException
     *
     */
    private void initializeMerossMqttConnector() throws MqttException, InterruptedException {
        String clientId = MqttMessageBuilder.buildClientId();
        MqttMessageBuilder.setClientId(clientId);
        CloudCredentials credentials = this.credentials;
        if (credentials == null) {
            logger.debug("No credentials found");
        } else {
            String userId = credentials.userId();
            MqttMessageBuilder.setUserId(userId);
            String key = credentials.key();
            MqttMessageBuilder.setKey(key);
            String brokerAddress = credentials.mqttDomain();
            MqttMessageBuilder.setBrokerAddress(brokerAddress);

            if (merossMqttConnector != null) {
                merossMqttConnector.stopConnection();
            }
            merossMqttConnector = new MerossMqttConnector(this);
            merossMqttConnector.startConnection();
        }
    }

    /**
     * @param devName The device name
     * @return The device UUID
     */
    public String getDevUUIDByDevName(String devName) {
        Optional<String> uuid = devices.stream().filter(device -> devName.equals(device.devName())).map(Device::uuid)
                .findFirst();
        if (uuid.isPresent()) {
            return uuid.get();
        }
        return "";
    }

    public void updateBridgeStatus(ThingStatus thingStatus, ThingStatusDetail thingStatusDetail) {
        updateStatus(thingStatus, thingStatusDetail);
    }
}
