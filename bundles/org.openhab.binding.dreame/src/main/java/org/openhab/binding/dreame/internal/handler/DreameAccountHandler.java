/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.dreame.internal.handler;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.dreame.internal.api.DreameCloudException;
import org.openhab.binding.dreame.internal.api.DreameMowerApi;
import org.openhab.binding.dreame.internal.config.DreameAccountConfiguration;
import org.openhab.binding.dreame.internal.discovery.DreameMowerDiscoveryService;
import org.openhab.binding.dreame.internal.model.DreameDevice;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Owns the Dreamehome session shared by mower things.
 *
 * @author Ronny Grun - Initial contribution
 */
@NonNullByDefault
public class DreameAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(DreameAccountHandler.class);
    private final DreameMowerApi apiClient;
    private final AtomicInteger lifecycleGeneration = new AtomicInteger();
    private volatile List<DreameDevice> devices = List.of();
    private volatile @Nullable DreameMowerDiscoveryService discoveryService;

    public DreameAccountHandler(Bridge bridge, DreameMowerApi apiClient) {
        super(bridge);
        this.apiClient = apiClient;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The account bridge does not expose command channels.
    }

    @Override
    public void initialize() {
        int generation = lifecycleGeneration.incrementAndGet();
        DreameAccountConfiguration config = getConfigAs(DreameAccountConfiguration.class);
        if (config.username.isBlank() || config.password.isBlank() || config.country.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Username, password and country must be configured");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);
        scheduler.execute(() -> connect(config, generation));
    }

    private void connect(DreameAccountConfiguration config, int generation) {
        try {
            apiClient.login(config.username, config.password, config.country);
            List<DreameDevice> discoveredDevices = apiClient.getDevices();
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            devices = discoveredDevices;
            updateStatus(ThingStatus.ONLINE);
            logger.debug("Connected to Dreamehome; account contains {} supported device records", devices.size());
            DreameMowerDiscoveryService discovery = discoveryService;
            if (discovery != null) {
                discovery.discoverDevices();
            }
        } catch (DreameCloudException e) {
            if (generation != lifecycleGeneration.get()) {
                return;
            }
            logger.debug("Dreamehome connection failed: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public @Nullable DreameDevice getDevice(String deviceId) {
        return devices.stream().filter(device -> device.id().equals(deviceId)).findFirst().orElse(null);
    }

    public DreameMowerApi getApiClient() {
        return apiClient;
    }

    public List<DreameDevice> getDevices() {
        return devices;
    }

    public void setDiscoveryService(@Nullable DreameMowerDiscoveryService discoveryService) {
        this.discoveryService = discoveryService;
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(DreameMowerDiscoveryService.class);
    }

    @Override
    public void dispose() {
        lifecycleGeneration.incrementAndGet();
        discoveryService = null;
        devices = List.of();
        apiClient.logout();
        super.dispose();
    }
}
