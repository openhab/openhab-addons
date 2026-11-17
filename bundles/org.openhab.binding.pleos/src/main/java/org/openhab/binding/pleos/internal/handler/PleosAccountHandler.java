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
package org.openhab.binding.pleos.internal.handler;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.pleos.internal.api.PleosApiClient;
import org.openhab.binding.pleos.internal.api.PleosApiException;
import org.openhab.binding.pleos.internal.config.PleosAccountConfiguration;
import org.openhab.binding.pleos.internal.discovery.PleosVehicleDiscoveryService;
import org.openhab.binding.pleos.internal.model.Brand;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.types.Command;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PleosAccountHandler} manages the {@link PleosApiClient}
 * as well as the cyclical updating of the linked vehicles.
 *
 * @author Stephan Sandmann - Initial contribution
 */
@NonNullByDefault
public class PleosAccountHandler extends BaseBridgeHandler {

    private final Logger logger = LoggerFactory.getLogger(PleosAccountHandler.class);
    private final HttpClient httpClient;
    private final TimeZoneProvider timeZoneProvider;

    private @Nullable ZonedDateTime secretExpiryTime;
    private int lastAlertStage = 0; // 0 = OK, 1 = Warned <7 days, 2 = Warned <2 days, 3 = Expired
    private Instant lastSixHourAlert = Instant.EPOCH;

    private volatile @Nullable PleosApiClient apiClient;
    private @Nullable ScheduledFuture<?> refreshJob;

    public PleosAccountHandler(Bridge bridge, HttpClient httpClient, TimeZoneProvider timeZoneProvider) {
        super(bridge);
        this.httpClient = httpClient;
        this.timeZoneProvider = timeZoneProvider;
    }

    @Override
    public void initialize() {
        logger.debug("Initialize Pleos Account Handler...");

        PleosAccountConfiguration config = getConfigAs(PleosAccountConfiguration.class);

        String secretCreationDate = config.secretCreationDate;
        this.secretExpiryTime = calculateSecretExpiry(secretCreationDate, timeZoneProvider.getTimeZone());
        if (this.secretExpiryTime == null && secretCreationDate != null && !secretCreationDate.isBlank()) {
            logger.warn("Invalid format for keyCreationDate ('{}')", secretCreationDate);
        }
        this.lastAlertStage = 0;
        this.lastSixHourAlert = Instant.EPOCH;
        checkSecretExpiration(); // Immediate initial check upon startup

        String clientId = config.clientId;
        String clientSecret = config.clientSecret;
        if (clientId == null || clientId.isBlank() || clientSecret == null || clientSecret.isBlank()) {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/account-handler.initialize.missing-credentials");
            return;
        }

        Brand brand;
        String configuredBrand = config.brand;
        if (configuredBrand != null && !configuredBrand.isBlank()) {
            try {
                brand = Brand.valueOf(configuredBrand.toUpperCase(Locale.ROOT));
                if (brand == Brand.UNKNOWN) {
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                            "@text/account-handler.initialize.unsupported-brand");
                    return;
                }
            } catch (IllegalArgumentException e) {
                updateStatus(ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/account-handler.initialize.unsupported-brand");
                return;
            }
        } else {
            updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, "@text/account-handler.initialize.brand-required");
            return;
        }

        PleosApiClient client = new PleosApiClient(httpClient, clientId, clientSecret, brand);
        this.apiClient = client;

        // Asynchronous initial fetch, so that initialize() does not block
        scheduler.execute(() -> {
            try {
                client.authenticate();
                updateStatus(ThingStatus.ONLINE);
                startAutomaticRefresh(config.refreshInterval);
            } catch (PleosApiException e) {
                logger.debug("Initial authentication failed: {}", e.getMessage());
                if (e.isAuthError()) {
                    updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
                } else {
                    updateStatus(ThingStatusDetail.COMMUNICATION_ERROR, e.getMessage());
                }
            }
        });
    }

    @Override
    public void dispose() {
        logger.debug("Terminate Pleo's account handler (dispose).");
        ScheduledFuture<?> job = this.refreshJob;
        if (job != null) {
            job.cancel(true);
            this.refreshJob = null;
        }
        this.apiClient = null;
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // Read-Only-API for Private Users
    }

    @Override
    public Collection<Class<? extends ThingHandlerService>> getServices() {
        return List.of(PleosVehicleDiscoveryService.class);
    }

    /**
     * Provides the central API client for discovery and the vehicle handlers.
     */
    public @Nullable PleosApiClient getApiClient() {
        return apiClient;
    }

    /**
     * Called by the Vehicle Handler if a permanent authentication error occurs.
     */
    public void handleFatalAuthError(String reason) {
        updateStatus(ThingStatusDetail.CONFIGURATION_ERROR, reason);
    }

    private void startAutomaticRefresh(int intervalMinutes) {
        int safeInterval = Math.max(1, intervalMinutes); // Protection against 0 or negative values
        logger.debug("Start cyclic refresh every {} minutes.", safeInterval);

        refreshJob = scheduler.scheduleWithFixedDelay(() -> {
            if (getThing().getStatus() != ThingStatus.ONLINE) {
                return;
            }
            logger.debug("Trigger automatic data updates for all Pleos vehicles.");
            for (Thing childThing : getThing().getThings()) {
                if (childThing.getHandler() instanceof PleosVehicleHandler vehicleHandler) {
                    vehicleHandler.refreshVehicleData();
                }
            }
        }, safeInterval, safeInterval, TimeUnit.MINUTES);
    }

    static @Nullable ZonedDateTime calculateSecretExpiry(@Nullable String dateStr, ZoneId zoneId) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr.trim()).atStartOfDay(zoneId).plusDays(90);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private void checkSecretExpiration() {
        ZonedDateTime expiry = this.secretExpiryTime;
        if (expiry == null) {
            updateState("secret-expires", UnDefType.UNDEF);
            updateState("secret-remaining-days", UnDefType.UNDEF);
            updateState("secret-expiry-warning", UnDefType.UNDEF);
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());
        long remainingHours = ChronoUnit.HOURS.between(now, expiry);
        long remainingDays = ChronoUnit.DAYS.between(now, expiry);

        updateState("secret-expires", new DateTimeType(expiry));
        updateState("secret-remaining-days", new QuantityType<>(Math.max(0, remainingDays), Units.DAY));
        updateState("secret-expiry-warning", OnOffType.from(remainingHours <= 168)); // 168 Std. = 7 Tage

        // 1. The clientSecret has already expired
        if (remainingHours <= 0) {
            if (lastAlertStage < 3) {
                lastAlertStage = 3;
                logger.warn("Pleos Client Secret has expired! Please generate a new key in the Pleos Playground.");
                triggerChannel("secret-expiry-event", "EXPIRED");
            }
            return;
        }

        // 2. Less than 2 days (48 hours): Issue a warning immediately, then repeat every 6 hours
        if (remainingHours <= 48) {
            Instant currentInstant = Instant.now();
            if (lastAlertStage < 2 || ChronoUnit.HOURS.between(lastSixHourAlert, currentInstant) >= 6) {
                lastAlertStage = 2;
                lastSixHourAlert = currentInstant;
                logger.warn("Pleos Client Secret expires in {} hours! Please renew it soon.", remainingHours);
                triggerChannel("secret-expiry-event", "EXPIRING_2_DAYS");
            }
            return;
        }

        // 3. Less than 7 days (168 hours): One-time advance warning
        if (remainingHours <= 168 && lastAlertStage < 1) {
            lastAlertStage = 1;
            logger.warn("Pleos Client Secret expires in {} days ({}).", remainingDays, expiry.toLocalDate());
            triggerChannel("secret-expiry-event", "EXPIRING_7_DAYS");
        }
    }
}
