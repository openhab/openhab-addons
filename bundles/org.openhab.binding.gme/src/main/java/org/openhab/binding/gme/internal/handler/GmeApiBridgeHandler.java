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
package org.openhab.binding.gme.internal.handler;

import static org.openhab.binding.gme.internal.GmeBindingConstants.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.gme.internal.api.GmeApiClient;
import org.openhab.binding.gme.internal.api.GmeAuthManager;
import org.openhab.binding.gme.internal.config.GmeApiConfiguration;
import org.openhab.binding.gme.internal.model.GmePasswordAge;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;

/**
 * Handler for a GME API account.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public class GmeApiBridgeHandler extends BaseBridgeHandler {

    private static final String STORAGE_CREDENTIAL_FINGERPRINT = "credentialFingerprint";
    private static final String STORAGE_PASSWORD_CHANGED_AT = "passwordChangedAt";
    private static final ZoneId GME_ZONE = ZoneId.of("Europe/Rome");

    private final GmeApiClient apiClient;
    private final Storage<String> storage;
    private final GmePasswordAge passwordAge = new GmePasswordAge(GME_ZONE, Clock.system(GME_ZONE));
    private @Nullable GmeAuthManager authManager;
    private volatile int refreshInterval = 60;
    private @Nullable ScheduledFuture<?> passwordRefreshJob;

    public GmeApiBridgeHandler(Bridge bridge, HttpClient httpClient, Storage<String> storage) {
        super(bridge);
        this.apiClient = new GmeApiClient(httpClient);
        this.storage = storage;
    }

    @Override
    public void initialize() {
        cancelPasswordRefresh();

        GmeApiConfiguration config = getConfigAs(GmeApiConfiguration.class);
        refreshInterval = Math.max(1, config.refreshInterval);

        if (config.username.isBlank() || config.password.isBlank()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "GME username and password must be configured.");
            return;
        }

        updateStatus(ThingStatus.UNKNOWN);

        scheduler.execute(() -> {
            GmeAuthManager manager = new GmeAuthManager(apiClient, config.username, config.password);
            authManager = manager;

            try {
                manager.getToken();
                updateCredentialState(config.username, config.password);
                updatePasswordChannels();
                schedulePasswordRefresh();
                updateStatus(ThingStatus.ONLINE);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "GME authentication was interrupted.");
            } catch (TimeoutException | ExecutionException | IllegalStateException e) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "GME authentication failed: " + e.getMessage());
            }
        });
    }

    private void updateCredentialState(String username, String password) {
        String fingerprint = credentialFingerprint(username, password);
        String storedFingerprint = storage.get(STORAGE_CREDENTIAL_FINGERPRINT);
        String changedAt = storage.get(STORAGE_PASSWORD_CHANGED_AT);

        boolean validChangedAt = false;
        if (changedAt != null) {
            try {
                Instant.parse(changedAt);
                validChangedAt = true;
            } catch (RuntimeException e) {
                // Reinitialize invalid persisted state after successful authentication.
            }
        }

        if (!fingerprint.equals(storedFingerprint) || !validChangedAt) {
            storage.put(STORAGE_CREDENTIAL_FINGERPRINT, fingerprint);
            storage.put(STORAGE_PASSWORD_CHANGED_AT, Instant.now().toString());
        }
    }

    private String credentialFingerprint(String username, String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((username + "\0" + password).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }

    private void updatePasswordChannels() {
        String changedAtValue = storage.get(STORAGE_PASSWORD_CHANGED_AT);
        if (changedAtValue == null) {
            return;
        }

        try {
            Instant changedAt = Instant.parse(changedAtValue);

            updateState(CHANNEL_PASSWORD_EXPIRY, new DateTimeType(passwordAge.getExpiry(changedAt)));
            updateState(CHANNEL_PASSWORD_DAYS_REMAINING,
                    new DecimalType(Long.toString(passwordAge.getDaysRemaining(changedAt))));
            updateState(CHANNEL_PASSWORD_STATUS, new StringType(passwordAge.getStatus(changedAt).name()));
        } catch (RuntimeException e) {
            // Invalid persisted state is repaired after the next successful authentication.
            storage.put(STORAGE_PASSWORD_CHANGED_AT, null);
        }
    }

    private void schedulePasswordRefresh() {
        cancelPasswordRefresh();
        passwordRefreshJob = scheduler.scheduleWithFixedDelay(this::updatePasswordChannels, 1, 1, TimeUnit.HOURS);
    }

    private void cancelPasswordRefresh() {
        ScheduledFuture<?> job = passwordRefreshJob;
        if (job != null) {
            job.cancel(true);
            passwordRefreshJob = null;
        }
    }

    @Override
    public void dispose() {
        cancelPasswordRefresh();
        authManager = null;
        super.dispose();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    public GmeApiClient getApiClient() {
        return apiClient;
    }

    public @Nullable GmeAuthManager getAuthManager() {
        return authManager;
    }

    public int getRefreshInterval() {
        return refreshInterval;
    }
}
