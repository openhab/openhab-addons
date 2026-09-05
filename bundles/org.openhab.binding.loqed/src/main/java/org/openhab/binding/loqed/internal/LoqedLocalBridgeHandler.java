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
package org.openhab.binding.loqed.internal;

import static org.openhab.binding.loqed.internal.LoqedBindingConstants.PROPERTY_LOCK_ID;
import static org.openhab.binding.loqed.internal.LoqedBindingConstants.WEBHOOK_PATH;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.openhab.binding.loqed.internal.api.BoltState;
import org.openhab.binding.loqed.internal.api.LoqedApiException;
import org.openhab.binding.loqed.internal.api.LoqedAuthenticationException;
import org.openhab.binding.loqed.internal.api.LoqedCommunicationException;
import org.openhab.binding.loqed.internal.api.LoqedConfigurationException;
import org.openhab.binding.loqed.internal.api.LoqedLocalApiClient;
import org.openhab.binding.loqed.internal.api.LoqedLockData;
import org.openhab.binding.loqed.internal.api.LoqedResponseException;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/**
 * Handles one bridge through the LOQED Local Bridge API and outgoing webhooks.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class LoqedLocalBridgeHandler extends BaseBridgeHandler implements LoqedBridge {
    private static final int MAX_INITIALIZATION_RETRIES = 3;
    private static final int INITIALIZATION_RETRY_DELAY_SECONDS = 30;
    private static final int RECOVERY_REFRESH_INTERVAL_SECONDS = 60;
    private static final int WEBHOOK_REFRESH_INTERVAL_SECONDS = 86400;
    private static final Gson GSON = new Gson();

    private final Logger logger = LoggerFactory.getLogger(LoqedLocalBridgeHandler.class);
    private final HttpClient httpClient;
    private final String routeId;
    private final @Nullable String detectedCallbackBaseUrl;
    private final Object connectionLock = new Object();
    private final Object lifecycleLock = new Object();
    private final EnumMap<BoltState, Long> stateUpdateSequences = new EnumMap<>(BoltState.class);

    private volatile List<LoqedLockData> locks = List.of();
    private @Nullable LoqedLockData status;
    private volatile @Nullable LoqedLocalApiClient apiClient;
    private @Nullable ScheduledFuture<?> initializationJob;
    private @Nullable ScheduledFuture<?> pollingJob;
    private @Nullable ScheduledFuture<?> recoveryJob;
    private long lifecycleGeneration;
    private long webhookId = -1;
    private boolean webhookActive;

    public LoqedLocalBridgeHandler(Bridge bridge, HttpClient httpClient, String routeId,
            @Nullable String detectedCallbackBaseUrl) {
        super(bridge);
        this.httpClient = httpClient;
        this.routeId = routeId;
        this.detectedCallbackBaseUrl = detectedCallbackBaseUrl;
    }

    @Override
    public void initialize() {
        resetConnection();
        locks = List.of();
        status = null;

        LoqedLocalConfiguration config = getConfigAs(LoqedLocalConfiguration.class);
        String error = config.validate();
        if (!error.isEmpty()) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status.loqed.local-bridge.configuration-invalid");
            return;
        }

        try {
            LoqedLocalApiClient client = new LoqedLocalApiClient(httpClient, config);
            String callbackBaseUrl = config.callbackBaseUrl.isBlank() ? detectedCallbackBaseUrl
                    : config.callbackBaseUrl;
            if (callbackBaseUrl == null) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.callback-unavailable");
                return;
            }
            String callbackUrl = stripTrailingSlash(callbackBaseUrl) + WEBHOOK_PATH + "/" + routeId;
            int refreshInterval = Math.max(60, config.refreshInterval);
            long generation;
            synchronized (lifecycleLock) {
                generation = ++lifecycleGeneration;
                apiClient = client;
                webhookActive = false;
                stateUpdateSequences.clear();
            }
            updateStatus(ThingStatus.UNKNOWN);
            scheduleInitialization(client, callbackUrl, refreshInterval, generation, 0, 0);
        } catch (LoqedConfigurationException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                    "@text/thing-status.loqed.local-bridge.configuration-invalid");
            logger.debug("Invalid LOQED local bridge configuration", e);
        }
    }

    private void scheduleInitialization(LoqedLocalApiClient client, String callbackUrl, int refreshInterval,
            long generation, int retry, long delaySeconds) {
        synchronized (lifecycleLock) {
            if (isCurrentConnection(client, generation)) {
                initializationJob = scheduler.schedule(
                        () -> initializeConnection(client, callbackUrl, refreshInterval, generation, retry),
                        delaySeconds, TimeUnit.SECONDS);
            }
        }
    }

    private void initializeConnection(LoqedLocalApiClient client, String callbackUrl, int refreshInterval,
            long generation, int retry) {
        boolean retryInitialization = false;
        try {
            long registeredWebhookId;
            boolean active;
            synchronized (connectionLock) {
                registeredWebhookId = client.ensureWebhook(callbackUrl);
                synchronized (lifecycleLock) {
                    active = isCurrentConnection(client, generation);
                    if (active) {
                        webhookId = registeredWebhookId;
                        webhookActive = true;
                        pollingJob = scheduler.scheduleWithFixedDelay(this::refresh, 0,
                                WEBHOOK_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
                    }
                }
            }
            if (!active) {
                scheduleWebhookRemoval(client, registeredWebhookId);
            }
        } catch (LoqedAuthenticationException e) {
            if (isCurrentConnection(client, generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.authentication-error");
                logger.debug("Could not authenticate with the LOQED local bridge", e);
            }
        } catch (LoqedConfigurationException e) {
            if (isCurrentConnection(client, generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.configuration-invalid");
                logger.debug("Invalid LOQED local bridge configuration", e);
            }
        } catch (LoqedCommunicationException | LoqedResponseException e) {
            if (isCurrentConnection(client, generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.communication-error");
                if (retry < MAX_INITIALIZATION_RETRIES) {
                    retryInitialization = true;
                    logger.debug("Could not initialize LOQED local bridge, retrying ({}/{})", retry + 1,
                            MAX_INITIALIZATION_RETRIES, e);
                } else {
                    startPolling(client, refreshInterval, generation);
                    logger.debug("Could not initialize LOQED webhook after {} retries, using fallback polling",
                            MAX_INITIALIZATION_RETRIES, e);
                }
            }
        } catch (LoqedApiException e) {
            if (isCurrentConnection(client, generation)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.communication-error");
                logger.debug("Could not initialize LOQED local bridge", e);
            }
        } finally {
            synchronized (lifecycleLock) {
                if (isCurrentConnection(client, generation)) {
                    initializationJob = null;
                }
            }
        }
        if (retryInitialization) {
            scheduleInitialization(client, callbackUrl, refreshInterval, generation, retry + 1,
                    INITIALIZATION_RETRY_DELAY_SECONDS);
        }
    }

    private void startPolling(LoqedLocalApiClient client, int refreshInterval, long generation) {
        synchronized (lifecycleLock) {
            if (isCurrentConnection(client, generation) && pollingJob == null) {
                pollingJob = scheduler.scheduleWithFixedDelay(this::refresh, 0, refreshInterval, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void dispose() {
        resetConnection();
        locks = List.of();
        status = null;
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        // The local bridge has no channels.
    }

    @Override
    public synchronized List<LoqedLockData> refreshAndGetLocks() {
        refresh();
        return locks;
    }

    @Override
    public synchronized void refresh() {
        LoqedLocalApiClient client = apiClient;
        if (client == null) {
            return;
        }
        try {
            LoqedLockData currentStatus = client.getStatus();
            List<LoqedLockData> refreshedLocks = createLocks(currentStatus);
            synchronized (lifecycleLock) {
                if (!client.equals(apiClient)) {
                    return;
                }
                status = currentStatus;
                locks = refreshedLocks;
            }
            if (!client.equals(apiClient)) {
                return;
            }
            updateStatus(ThingStatus.ONLINE);
            updateRecoveryPolling(client, currentStatus.online);
            if (!client.equals(apiClient)) {
                return;
            }
            updateChildren(refreshedLocks);
        } catch (LoqedAuthenticationException e) {
            if (client.equals(apiClient)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.authentication-error");
                logger.debug("Could not authenticate with the LOQED local bridge", e);
            }
        } catch (LoqedConfigurationException e) {
            if (client.equals(apiClient)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.configuration-invalid");
                logger.debug("Invalid LOQED local bridge configuration", e);
            }
        } catch (LoqedApiException e) {
            if (client.equals(apiClient)) {
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                        "@text/thing-status.loqed.local-bridge.communication-error");
                logger.debug("Could not refresh LOQED local bridge", e);
            }
        }
    }

    @Override
    public void setBoltState(String lockId, String keySecret, int localKeyId, BoltState boltState)
            throws LoqedApiException {
        LoqedLocalApiClient client = apiClient;
        if (client == null) {
            throw new LoqedCommunicationException("The LOQED local bridge is not initialized",
                    new IllegalStateException());
        }
        boolean configured = getThing().getThings().stream()
                .map(thing -> thing.getConfiguration().get(PROPERTY_LOCK_ID)).anyMatch(lockId::equals);
        if (!configured) {
            throw new LoqedConfigurationException("The lock does not belong to this LOQED local bridge");
        }
        client.setBoltState(keySecret, localKeyId, boltState);
    }

    @Override
    public boolean usesPushUpdates() {
        synchronized (lifecycleLock) {
            return webhookActive;
        }
    }

    @Override
    public long getStateUpdateSequence(BoltState boltState) {
        synchronized (lifecycleLock) {
            return stateUpdateSequences.getOrDefault(boltState, 0L);
        }
    }

    @Override
    public boolean requiresLocalCredentials() {
        return true;
    }

    public boolean handleWebhook(byte[] body, String timestamp, String hash) {
        @Nullable
        LoqedLocalApiClient client;
        long generation;
        synchronized (lifecycleLock) {
            client = apiClient;
            generation = lifecycleGeneration;
        }
        if (client == null || !client.verifyWebhook(body, timestamp, hash)) {
            return false;
        }

        @Nullable
        JsonElement parsed;
        try {
            parsed = GSON.fromJson(new String(body, java.nio.charset.StandardCharsets.UTF_8), JsonElement.class);
        } catch (JsonParseException e) {
            return false;
        }
        if (parsed == null || !parsed.isJsonObject()) {
            return false;
        }
        return applyWebhook(client, generation, parsed.getAsJsonObject());
    }

    private synchronized boolean applyWebhook(LoqedLocalApiClient client, long generation, JsonObject event) {
        @Nullable
        LoqedLockData currentStatus;
        boolean refreshRequired = false;
        synchronized (lifecycleLock) {
            if (!isCurrentConnection(client, generation)) {
                return false;
            }
            JsonElement requestedState = event.get("requested_state");
            if (requestedState != null && requestedState.isJsonPrimitive()) {
                BoltState.fromApiValue(requestedState.getAsString())
                        .ifPresent(state -> stateUpdateSequences.merge(state, 1L, Long::sum));
            }
            currentStatus = status;
            if (currentStatus != null) {
                refreshRequired = LoqedLocalApiClient.applyWebhook(currentStatus, event);
            }
        }
        if (currentStatus == null || refreshRequired) {
            scheduler.execute(this::refresh);
            return true;
        }

        List<LoqedLockData> refreshedLocks = createLocks(currentStatus);
        synchronized (lifecycleLock) {
            if (!isCurrentConnection(client, generation)) {
                return false;
            }
            locks = refreshedLocks;
        }
        if (!isCurrentConnection(client, generation)) {
            return false;
        }
        updateStatus(ThingStatus.ONLINE);
        if (!isCurrentConnection(client, generation)) {
            return false;
        }
        updateChildren(refreshedLocks);
        updateRecoveryPolling(client, currentStatus.online);
        return true;
    }

    private void updateRecoveryPolling(LoqedLocalApiClient client, boolean lockOnline) {
        synchronized (lifecycleLock) {
            if (!client.equals(apiClient)) {
                return;
            }
            ScheduledFuture<?> job = recoveryJob;
            if (lockOnline) {
                if (job != null) {
                    job.cancel(false);
                    recoveryJob = null;
                    logger.debug("LOQED lock is online, stopping recovery polling");
                }
            } else if (job == null) {
                recoveryJob = scheduler.scheduleWithFixedDelay(this::refresh, RECOVERY_REFRESH_INTERVAL_SECONDS,
                        RECOVERY_REFRESH_INTERVAL_SECONDS, TimeUnit.SECONDS);
                logger.debug("LOQED lock is offline, starting recovery polling every {} seconds",
                        RECOVERY_REFRESH_INTERVAL_SECONDS);
            }
        }
    }

    private void updateChildren(List<LoqedLockData> lockSnapshot) {
        getThing().getThings().stream().map(thing -> thing.getHandler()).filter(LoqedLockHandler.class::isInstance)
                .map(LoqedLockHandler.class::cast).forEach(handler -> handler.updateFromBridge(lockSnapshot));
    }

    private List<LoqedLockData> createLocks(LoqedLockData currentStatus) {
        List<LoqedLockData> result = new ArrayList<>();
        getThing().getThings().forEach(thing -> {
            LoqedConfiguration config = thing.getConfiguration().as(LoqedConfiguration.class);
            if (!config.lockId.isBlank()) {
                LoqedLockData lock = new LoqedLockData();
                lock.id = config.lockId;
                lock.name = Objects.requireNonNullElse(thing.getLabel(), "LOQED Smart Lock");
                lock.modelName = currentStatus.modelName;
                lock.online = currentStatus.online;
                lock.batteryPercentage = currentStatus.batteryPercentage;
                lock.batteryType = currentStatus.batteryType;
                lock.boltState = currentStatus.boltState;
                lock.partyMode = currentStatus.partyMode;
                lock.guestAccessMode = currentStatus.guestAccessMode;
                lock.twistAssist = currentStatus.twistAssist;
                lock.touchToConnect = currentStatus.touchToConnect;
                result.add(lock);
            }
        });
        return List.copyOf(result);
    }

    private static String stripTrailingSlash(String value) {
        String result = value.strip();
        while (result.endsWith("/")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    private void resetConnection() {
        @Nullable
        LoqedLocalApiClient client;
        long registeredWebhookId;
        synchronized (lifecycleLock) {
            lifecycleGeneration++;
            ScheduledFuture<?> job = initializationJob;
            if (job != null) {
                job.cancel(true);
                initializationJob = null;
            }
            job = pollingJob;
            if (job != null) {
                job.cancel(true);
                pollingJob = null;
            }
            job = recoveryJob;
            if (job != null) {
                job.cancel(true);
                recoveryJob = null;
            }
            client = apiClient;
            registeredWebhookId = webhookId;
            apiClient = null;
            webhookId = -1;
            webhookActive = false;
            stateUpdateSequences.clear();
        }
        if (client != null && registeredWebhookId >= 0) {
            scheduleWebhookRemoval(client, registeredWebhookId);
        }
    }

    private void scheduleWebhookRemoval(LoqedLocalApiClient client, long registeredWebhookId) {
        try {
            scheduler.execute(() -> removeWebhookUnlessInUse(client, registeredWebhookId));
        } catch (RejectedExecutionException e) {
            logger.debug("Could not schedule removal of LOQED webhook {}", registeredWebhookId, e);
        }
    }

    private void removeWebhookUnlessInUse(LoqedLocalApiClient client, long registeredWebhookId) {
        synchronized (connectionLock) {
            synchronized (lifecycleLock) {
                LoqedLocalApiClient currentClient = apiClient;
                if (currentClient != null && currentClient.connectsToSameBridge(client)
                        && webhookId == registeredWebhookId) {
                    return;
                }
            }
            removeWebhook(client, registeredWebhookId);
        }
    }

    private void removeWebhook(LoqedLocalApiClient client, long registeredWebhookId) {
        try {
            client.removeWebhook(registeredWebhookId);
        } catch (LoqedApiException e) {
            logger.debug("Could not remove LOQED webhook {}", registeredWebhookId, e);
        }
    }

    private boolean isCurrentConnection(LoqedLocalApiClient client, long generation) {
        synchronized (lifecycleLock) {
            return client.equals(apiClient) && lifecycleGeneration == generation;
        }
    }
}
