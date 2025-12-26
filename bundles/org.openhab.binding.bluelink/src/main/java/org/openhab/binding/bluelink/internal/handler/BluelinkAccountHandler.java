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
package org.openhab.binding.bluelink.internal.handler;

import java.time.Duration;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.bluelink.internal.api.AbstractBluelinkApi;
import org.openhab.binding.bluelink.internal.api.BluelinkApiCA;
import org.openhab.binding.bluelink.internal.api.BluelinkApiException;
import org.openhab.binding.bluelink.internal.api.BluelinkApiUS;
import org.openhab.binding.bluelink.internal.api.Region;
import org.openhab.binding.bluelink.internal.api.RetryableRequestException;
import org.openhab.binding.bluelink.internal.api.VehicleStatusCallback;
import org.openhab.binding.bluelink.internal.config.BluelinkAccountConfiguration;
import org.openhab.binding.bluelink.internal.discovery.BluelinkVehicleDiscoveryService;
import org.openhab.binding.bluelink.internal.model.Brand;
import org.openhab.binding.bluelink.internal.model.IVehicle;
import org.openhab.binding.bluelink.internal.util.Backoff;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.QuantityType;
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
    private final LocaleProvider localeProvider;

    private volatile @Nullable AbstractBluelinkApi<?> api;
    private volatile @Nullable ScheduledFuture<?> loginTask;

    public BluelinkAccountHandler(final Bridge bridge, final HttpClient httpClient,
            final TimeZoneProvider timeZoneProvider, final LocaleProvider localeProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
        this.localeProvider = localeProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Bluelink account handler");

        final BluelinkAccountConfiguration config = getConfigAs(BluelinkAccountConfiguration.class);
        final String username = config.username;
        final String password = config.password;
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/account-handler.initialize.missing-credentials");
            return;
        }

        // Determine region
        final String regionStr;
        final String configuredRegion = config.region;
        if (configuredRegion != null && !configuredRegion.isBlank()) {
            regionStr = configuredRegion.toUpperCase(Locale.ROOT);
        } else {
            regionStr = localeProvider.getLocale().getCountry();
            logger.debug("Auto-detected region: {}", regionStr);
        }
        final Region region;
        try {
            region = Region.valueOf(regionStr);
        } catch (final IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/account-handler.initialize.unsupported-region");
            return;
        }

        // Determine brand
        final Brand brand;
        final String configuredBrand = config.brand;
        if (configuredBrand != null) {
            try {
                brand = Brand.valueOf(configuredBrand.toUpperCase(Locale.ROOT));
            } catch (final IllegalArgumentException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/account-handler.initialize.unsupported-brand");
                return;
            }
        } else if (region == Region.CA) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/account-handler.initialize.brand-required");
            return;
        } else {
            brand = Brand.UNKNOWN;
        }

        // baseUrl override for tests
        final Optional<String> optBaseUrl = Optional.ofNullable(config.apiBaseUrl);
        this.api = switch (region) {
            case US -> new BluelinkApiUS(httpClient, optBaseUrl, timeZoneProvider, username, password, config.pin);
            case CA ->
                new BluelinkApiCA(httpClient, brand, optBaseUrl, timeZoneProvider, username, password, config.pin);
        };
        logger.debug("Created API for region {} brand {}", region, brand);
        updateStatus(ThingStatus.UNKNOWN);
        loginTask = scheduler.schedule(this::login, 0, TimeUnit.MILLISECONDS);
    }

    private void login() {
        final AbstractBluelinkApi<?> bluelinkApi = api;
        if (bluelinkApi == null) {
            return;
        }

        try {
            if (bluelinkApi.login()) {
                logger.debug("Bluelink login successful");
                updateStatus(ThingStatus.ONLINE);
            } else {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/account-handler.login.login-failed");
            }
        } catch (final BluelinkApiException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        logger.debug("Disposing Bluelink account handler");
        final ScheduledFuture<?> task = loginTask;
        if (task != null) {
            task.cancel(true);
            loginTask = null;
        }
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

    public List<? extends IVehicle> getVehicles() throws BluelinkApiException {
        final Backoff backoff = new Backoff(Duration.ofSeconds(1), Duration.ofMillis(300), 3);
        final AbstractBluelinkApi<?> bluelinkApi = api;
        if (bluelinkApi == null) {
            return List.of();
        }
        while (true) {
            try {
                return bluelinkApi.getVehicles();
            } catch (final RetryableRequestException e) {
                if (backoff.hasMoreAttempts()) {
                    try {
                        Thread.sleep(backoff.nextDelay());
                    } catch (final InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        throw new BluelinkApiException("interrupted");
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    public boolean lockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.lockVehicle(vehicle);
    }

    public boolean unlockVehicle(final IVehicle vehicle) throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.unlockVehicle(vehicle);
    }

    public boolean startCharging(final IVehicle vehicle) throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.startCharging(vehicle);
    }

    public boolean stopCharging(final IVehicle vehicle) throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.stopCharging(vehicle);
    }

    public boolean climateStart(final IVehicle vehicle, final QuantityType<Temperature> temperature, final boolean heat,
            final boolean defrost, final @Nullable Integer igniOnDuration) throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.climateStart(vehicle, temperature, heat, defrost, igniOnDuration);
    }

    public boolean climateStop(final IVehicle vehicle) throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.climateStop(vehicle);
    }

    public boolean getVehicleStatus(final IVehicle vehicle, final boolean forceRefresh, final VehicleStatusCallback cb)
            throws BluelinkApiException {
        final var api = this.api;
        return api != null && api.getVehicleStatus(vehicle, forceRefresh, cb);
    }
}
