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
package org.openhab.binding.bluelink.internal.handler;

import static org.openhab.binding.bluelink.internal.BluelinkBindingConstants.API_ENDPOINT;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bluelink.internal.api.BluelinkApi;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.api.RetryableRequestException;
import org.openhab.binding.bluelink.internal.config.BluelinkAccountConfiguration;
import org.openhab.binding.bluelink.internal.discovery.BluelinkVehicleDiscoveryService;
import org.openhab.binding.bluelink.internal.dto.VehicleInfo;
import org.openhab.binding.bluelink.internal.util.Backoff;
import org.openhab.core.i18n.TimeZoneProvider;
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
 * The {@link BluelinkAccountHandler} is responsible for handling the Bluelink account bridge.
 * It manages authentication and provides API access to vehicle handlers.
 *
 * @author Marcus Better - Initial contribution
 */
@NonNullByDefault
public class BluelinkAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(BluelinkAccountHandler.class);

    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;
    private final ConcurrentMap<String, VehicleInfo> vehicles = new ConcurrentHashMap<>(1);

    private volatile @Nullable BluelinkApi api;

    public BluelinkAccountHandler(final Bridge bridge, final HttpClient httpClient,
            final TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Bluelink account handler");

        final BluelinkAccountConfiguration config = getConfigAs(BluelinkAccountConfiguration.class);
        final String username = config.username;
        final String password = config.password;
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "Username and password are required");
            return;
        }

        final String configuredBaseUrl = config.apiBaseUrl;
        final String baseUrl = configuredBaseUrl != null ? configuredBaseUrl : API_ENDPOINT;
        this.api = new BluelinkApi(httpClient, baseUrl, timeZoneProvider, username, password, config.pin);
        scheduler.execute(this::login);
    }

    private void login() {
        final BluelinkApi bluelinkApi = api;
        if (bluelinkApi == null) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "API not initialized");
            return;
        }

        try {
            if (bluelinkApi.login()) {
                logger.debug("Bluelink login successful");
                loadEnrolledVehicles();
                logger.debug("Found {} vehicles", vehicles.size());
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, "Login failed");
            }
        } catch (final BluelinkApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Bluelink account handler");
        api = null;
    }

    @Override
    public void handleCommand(final ChannelUID channelUID, final Command command) {
        // Bridge has no channels to handle
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(BluelinkVehicleDiscoveryService.class);
    }

    public @Nullable BluelinkApi getApi() {
        return api;
    }

    public Map<String, VehicleInfo> getVehicles() {
        return Map.copyOf(vehicles);
    }

    private void loadEnrolledVehicles() throws BluelinkApiException {
        final Backoff backoff = new Backoff(Duration.ofSeconds(1), Duration.ofMillis(300), 3);
        final BluelinkApi bluelinkApi = api;
        if (bluelinkApi == null) {
            return;
        }
        do {
            try {
                bluelinkApi.getVehicles().forEach(v -> vehicles.put(v.vin(), v));
                break;
            } catch (final RetryableRequestException e) {
                if (backoff.hasMoreAttempts()) {
                    try {
                        Thread.sleep(backoff.nextDelay());
                    } catch (final InterruptedException ex) {
                        throw new BluelinkApiException("interrupted");
                    }
                } else {
                    throw e;
                }
            }
        } while (backoff.hasMoreAttempts());
    }

    public @Nullable VehicleInfo getVehicleByVin(final String vin) {
        return vehicles.get(vin);
    }
}
