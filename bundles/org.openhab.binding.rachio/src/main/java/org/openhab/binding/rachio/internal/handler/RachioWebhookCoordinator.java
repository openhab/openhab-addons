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
package org.openhab.binding.rachio.internal.handler;

import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;
import static org.openhab.binding.rachio.internal.RachioUtils.getString;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookTarget;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.LifecycleSnapshot;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookException;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Coordinates webhook mode selection, registration, cloud URL leases, event dispatch, and reconciliation.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
final class RachioWebhookCoordinator {
    private static final long MIN_CLOUD_WEBHOOK_REFRESH_DELAY_SECONDS = 60;
    private static final long NO_CLOUD_WEBHOOK_GENERATION = -1;
    private static final long NO_CLOUD_WEBHOOK_CONSUMER_LEASE = -1;
    private static final Duration CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW = Duration.ofHours(1);
    private static final String CLOUD_WEBHOOK_SERVICE_UNAVAILABLE = RachioCloudWebhookRegistry.WEBHOOK_SERVICE_UNAVAILABLE;
    private static final String CLOUD_WEBHOOK_SERVICE_UNAVAILABLE_STATE = "cloud WebhookService unavailable";
    private static final String CLOUD_WEBHOOK_SERVICE_UNAVAILABLE_MESSAGE = "RachioCloud: openHAB core WebhookService is not available; automatic openHAB Cloud webhook URL acquisition is disabled on this runtime";
    private static final String MODERN_WEBHOOK_VERIFICATION_DEFERRED_STATE = "registered; verification deferred";

    private final Logger logger = LoggerFactory.getLogger(RachioWebhookCoordinator.class);
    private final RachioCloudWebhookRegistry cloudWebhookRegistry;
    private final ScheduledExecutorService scheduler;
    private final Map<String, String> knownModernIrrigationWebhookRegistrations = new ConcurrentHashMap<>();
    private final AtomicBoolean cloudWebhookReconciliationPending = new AtomicBoolean();
    private final AtomicBoolean cloudWebhookReconciliationRequested = new AtomicBoolean();
    private final AtomicReference<ResolvedWebhookState> resolvedWebhook = new AtomicReference<>(
            ResolvedWebhookState.NONE);
    private @Nullable ScheduledFuture<?> cloudWebhookRefreshJob;
    private volatile String webhookMode = "disabled";
    private volatile String webhookRegistrationState = "disabled";
    private volatile String lastWebhookRegistrationAttempt = "";
    private volatile String lastWebhookEventTimestamp = "";
    private volatile String lastWebhookEventType = "";

    /** Creates a coordinator backed by the shared webhook registry and bridge scheduler. */
    RachioWebhookCoordinator(RachioCloudWebhookRegistry cloudWebhookRegistry, ScheduledExecutorService scheduler) {
        this.cloudWebhookRegistry = cloudWebhookRegistry;
        this.scheduler = scheduler;
    }

    /** Registers the configured controller webhook for a discovered device. */
    void registerWebHook(RachioBridgeHandler bridgeHandler, String deviceId, RequestPurpose requestPurpose)
            throws RachioApiException {
        RachioWebhookMode mode = getWebhookMode(bridgeHandler, RachioWebhookResourceType.IRRIGATION_CONTROLLER);
        switch (mode) {
            case LEGACY_NOTIFICATION_SERVICE:
                updateWebhookMode(bridgeHandler, "legacy");
                updateWebhookRegistrationAttempt(bridgeHandler);
                bridgeHandler.getWebhookApi().registerLegacyNotificationWebHook(deviceId,
                        bridgeHandler.getCallbackUrl(), bridgeHandler.getCallbackUsername(),
                        bridgeHandler.getCallbackPassword(), bridgeHandler.getExternalId(),
                        bridgeHandler.getClearAllCallbacks(), requestPurpose);
                updateWebhookRegistrationState(bridgeHandler, "registered");
                break;
            case WEBHOOK_SERVICE:
                String webhookUrl = getModernWebhookUrlForRegistration(bridgeHandler);
                if (webhookUrl.isBlank()) {
                    if (!bridgeHandler.getCallbackUrl().isBlank()) {
                        logger.debug(
                                "RachioCloud: Modern controller webhook URL is unavailable; using configured legacy NotificationService callback.");
                        updateWebhookMode(bridgeHandler, "legacy");
                        updateWebhookRegistrationAttempt(bridgeHandler);
                        bridgeHandler.getWebhookApi().registerLegacyNotificationWebHook(deviceId,
                                bridgeHandler.getCallbackUrl(), bridgeHandler.getCallbackUsername(),
                                bridgeHandler.getCallbackPassword(), bridgeHandler.getExternalId(),
                                bridgeHandler.getClearAllCallbacks(), requestPurpose);
                        updateWebhookRegistrationState(bridgeHandler, "registered");
                        return;
                    }
                    if (deferKnownModernWebhookVerificationBecauseUrlIsUnavailable(bridgeHandler, deviceId)) {
                        return;
                    }
                    logger.debug(
                            "RachioCloud: Modern controller webhook registration is enabled but no public webhook URL is available; polling remains active.");
                    return;
                }
                String externalId = bridgeHandler.getExternalId();
                updateWebhookRegistrationAttempt(bridgeHandler);
                try {
                    bridgeHandler.getWebhookApi().registerWebHook(deviceId, webhookUrl, "", "", externalId,
                            bridgeHandler.getClearAllCallbacks(), requestPurpose);
                    recordSuccessfulModernWebhookRegistration(deviceId, webhookUrl, externalId);
                    updateWebhookRegistrationState(bridgeHandler, "registered");
                } catch (RachioApiThrottledException e) {
                    throw e;
                } catch (RachioApiException e) {
                    if (!deferKnownModernWebhookVerificationFailure(bridgeHandler, deviceId, webhookUrl, externalId,
                            e)) {
                        throw e;
                    }
                } catch (RuntimeException e) {
                    if (!deferKnownModernWebhookVerificationFailure(bridgeHandler, deviceId, webhookUrl, externalId,
                            e)) {
                        throw e;
                    }
                }
                break;
            case DISABLED:
                logger.debug("RachioCloud: Controller webhook registration disabled; polling remains active.");
                updateWebhookRegistrationState(bridgeHandler, "disabled");
                break;
        }
    }

    /** Registers a modern webhook for a Smart Hose Timer valve when enabled. */
    void registerValveWebHook(RachioBridgeHandler bridgeHandler, String valveId, RequestPurpose requestPurpose)
            throws RachioApiException {
        registerSmartHoseWebhook(bridgeHandler, new RachioWebhookTarget(valveId, RachioWebhookResourceType.VALVE,
                List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END)), "valve", requestPurpose);
    }

    /** Registers a modern webhook for a Smart Hose Timer program when enabled. */
    void registerValveProgramWebHook(RachioBridgeHandler bridgeHandler, String programId, RequestPurpose requestPurpose)
            throws RachioApiException {
        registerSmartHoseWebhook(bridgeHandler,
                new RachioWebhookTarget(programId, RachioWebhookResourceType.PROGRAM,
                        List.of(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED)),
                "program", requestPurpose);
    }

    private void registerSmartHoseWebhook(RachioBridgeHandler bridgeHandler, RachioWebhookTarget target,
            String resourceLabel, RequestPurpose requestPurpose) throws RachioApiException {
        if (getWebhookMode(bridgeHandler, target.getResourceType()) != RachioWebhookMode.WEBHOOK_SERVICE) {
            logger.debug("RachioCloud: Smart Hose Timer {} webhook registration disabled; polling remains active.",
                    resourceLabel);
            return;
        }
        String webhookUrl = getModernWebhookUrlForRegistration(bridgeHandler);
        if (webhookUrl.isBlank()) {
            logger.debug(
                    "RachioCloud: Smart Hose Timer {} webhook registration is enabled but no public webhook URL is available; polling remains active.",
                    resourceLabel);
            return;
        }
        logger.debug("RachioCloud: Smart Hose Timer {} webhook registration enabled for target '{}'", resourceLabel,
                target.describe());
        bridgeHandler.getWebhookApi().registerWebHookTarget(target, webhookUrl, "", "", bridgeHandler.getExternalId(),
                bridgeHandler.getClearAllCallbacks(), requestPurpose);
    }

    /** Returns the effective webhook mode for a resource type. */
    RachioWebhookMode getWebhookMode(RachioBridgeHandler bridgeHandler, RachioWebhookResourceType resourceType) {
        RachioConfiguration configuration = bridgeHandler.getWebhookConfiguration();
        return selectWebhookMode(resourceType, !bridgeHandler.getCallbackUrl().isBlank(),
                configuration.autoConfigureWebhooks, configuration.autoConfigureHoseTimerWebhooks,
                configuration.useCloudWebhook || !configuration.publicWebhookUrl.isBlank());
    }

    /** Returns the controller webhook mode that may currently accept inbound events. */
    RachioWebhookMode getActiveIrrigationWebhookProcessingMode(RachioBridgeHandler bridgeHandler) {
        RachioConfiguration configuration = bridgeHandler.getWebhookConfiguration();
        return selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER,
                !bridgeHandler.getCallbackUrl().isBlank(), configuration.autoConfigureWebhooks,
                isModernIrrigationWebhookUrlAvailableForProcessing(bridgeHandler));
    }

    private boolean isModernIrrigationWebhookUrlAvailableForProcessing(RachioBridgeHandler bridgeHandler) {
        RachioConfiguration configuration = bridgeHandler.getWebhookConfiguration();
        if (!configuration.autoConfigureWebhooks) {
            return false;
        }
        if (!configuration.publicWebhookUrl.isBlank()) {
            try {
                normalizeModernWebhookUrl(configuration.publicWebhookUrl);
                return true;
            } catch (RachioApiException e) {
                return false;
            }
        }
        String cachedUrl = getResolvedWebhookState().url();
        return configuration.useCloudWebhook && cachedUrl != null && !cachedUrl.isBlank();
    }

    /** Selects a webhook mode when only legacy callback availability is known. */
    static RachioWebhookMode selectWebhookMode(RachioWebhookResourceType resourceType, boolean callbackConfigured) {
        return selectWebhookMode(resourceType, callbackConfigured, false, false, false);
    }

    /** Selects a controller webhook mode from the configured webhook capabilities. */
    static RachioWebhookMode selectWebhookMode(RachioWebhookResourceType resourceType, boolean callbackConfigured,
            boolean autoConfigureWebhooks, boolean modernWebhookUrlConfigured) {
        return selectWebhookMode(resourceType, callbackConfigured, autoConfigureWebhooks, false,
                modernWebhookUrlConfigured);
    }

    /** Selects a webhook mode, including the separate Smart Hose Timer opt-in. */
    static RachioWebhookMode selectWebhookMode(RachioWebhookResourceType resourceType, boolean callbackConfigured,
            boolean autoConfigureWebhooks, boolean autoConfigureHoseTimerWebhooks, boolean modernWebhookUrlConfigured) {
        switch (resourceType) {
            case IRRIGATION_CONTROLLER:
                if (autoConfigureWebhooks && modernWebhookUrlConfigured) {
                    return RachioWebhookMode.WEBHOOK_SERVICE;
                }
                return callbackConfigured ? RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE : RachioWebhookMode.DISABLED;
            case VALVE:
            case PROGRAM:
                return autoConfigureWebhooks && autoConfigureHoseTimerWebhooks && modernWebhookUrlConfigured
                        ? RachioWebhookMode.WEBHOOK_SERVICE
                        : RachioWebhookMode.DISABLED;
            case LIGHTING_CONTROLLER:
            case LIGHTING_ZONE:
            case LIGHTING_SCENE:
            case LIGHTING_PROGRAM:
            case UNKNOWN:
            default:
                return RachioWebhookMode.DISABLED;
        }
    }

    /** Schedules reconciliation after the openHAB Cloud webhook provider changes. */
    void onCloudWebhookProviderChanged(RachioBridgeHandler bridgeHandler) {
        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        if (lifecycle == null) {
            return;
        }
        RachioConfiguration configuration = lifecycle.configuration();
        if (!configuration.autoConfigureWebhooks || !configuration.useCloudWebhook) {
            return;
        }
        cloudWebhookReconciliationRequested.set(true);
        scheduleCloudWebhookReconciliation(bridgeHandler);
    }

    private void scheduleCloudWebhookReconciliation(RachioBridgeHandler bridgeHandler) {
        if (!cloudWebhookReconciliationPending.compareAndSet(false, true)) {
            return;
        }
        try {
            scheduler.execute(() -> drainCloudWebhookReconciliationRequests(bridgeHandler));
        } catch (RuntimeException e) {
            cloudWebhookReconciliationPending.set(false);
            logger.debug("RachioCloud: Unable to schedule cloud webhook reconciliation", e);
        }
    }

    private void drainCloudWebhookReconciliationRequests(RachioBridgeHandler bridgeHandler) {
        try {
            do {
                cloudWebhookReconciliationRequested.set(false);
                LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
                if (lifecycle == null) {
                    return;
                }
                RachioConfiguration configuration = lifecycle.configuration();
                if (!configuration.autoConfigureWebhooks || !configuration.useCloudWebhook) {
                    return;
                }
                logger.debug(
                        "RachioCloud: openHAB core WebhookService changed or its URL needs refresh; retrying webhook reconciliation.");
                ResolvedWebhookState webhookState = getResolvedWebhookState();
                cloudWebhookRegistry.invalidateCachedWebhook(webhookState.providerGeneration());
                releaseResolvedCloudWebhook(bridgeHandler);
                reconcileConfiguredWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH);
                reconcileSmartHoseTimerWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH);
            } while (cloudWebhookReconciliationRequested.get());
        } finally {
            cloudWebhookReconciliationPending.set(false);
            if (cloudWebhookReconciliationRequested.get()) {
                scheduleCloudWebhookReconciliation(bridgeHandler);
            }
        }
    }

    private void reconcileSmartHoseTimerWebhooks(RachioBridgeHandler bridgeHandler, RequestPurpose requestPurpose) {
        if (getWebhookMode(bridgeHandler, RachioWebhookResourceType.VALVE) != RachioWebhookMode.WEBHOOK_SERVICE
                && getWebhookMode(bridgeHandler,
                        RachioWebhookResourceType.PROGRAM) != RachioWebhookMode.WEBHOOK_SERVICE) {
            return;
        }

        for (RachioSmartHoseStatusListener listener : bridgeHandler.smartHoseStatusListeners) {
            if (listener instanceof RachioValveHandler valveHandler) {
                valveHandler.renewWebhookRegistration(requestPurpose);
            } else if (listener instanceof RachioValveProgramHandler programHandler) {
                programHandler.renewWebhookRegistration(requestPurpose);
            }
        }
    }

    /** Reconciles all configured webhook registrations against the current cloud state. */
    void reconcileConfiguredWebhooks(RachioBridgeHandler bridgeHandler, RequestPurpose requestPurpose) {
        if (bridgeHandler.isDisposed()) {
            return;
        }
        RachioConfiguration configuration = bridgeHandler.getWebhookConfiguration();
        if (!configuration.autoConfigureWebhooks) {
            releaseCloudWebhookUrl(bridgeHandler, "modern webhook registration disabled");
            updateWebhookMode(bridgeHandler, "disabled");
            updateWebhookRegistrationState(bridgeHandler, "disabled");
            return;
        }
        if (getWebhookMode(bridgeHandler,
                RachioWebhookResourceType.IRRIGATION_CONTROLLER) != RachioWebhookMode.WEBHOOK_SERVICE) {
            releaseCloudWebhookUrl(bridgeHandler, "modern webhook mode inactive");
            updateWebhookRegistrationState(bridgeHandler, "disabled");
            return;
        }

        Set<String> configuredControllerIds = getConfiguredControllerThingIds(bridgeHandler);
        if (configuredControllerIds.isEmpty()) {
            updateWebhookRegistrationState(bridgeHandler, "waiting for configured controller things");
            return;
        }

        for (String deviceId : configuredControllerIds) {
            try {
                registerWebHook(bridgeHandler, deviceId, requestPurpose);
            } catch (RachioApiThrottledException e) {
                updateWebhookRegistrationState(bridgeHandler, "deferred by local API budget guard");
                logger.debug(
                        "Modern webhook registration for controller '{}' deferred because the local Rachio API budget guard is active; polling remains active.",
                        deviceId);
                return;
            } catch (RachioApiException e) {
                updateWebhookRegistrationState(bridgeHandler, "registration failed: " + e.getClass().getSimpleName());
                logger.warn("Modern webhook registration for controller '{}' failed; polling remains active, cause={}",
                        deviceId, e.getClass().getSimpleName());
            } catch (RuntimeException e) {
                updateWebhookRegistrationState(bridgeHandler, "registration failed: " + e.getClass().getSimpleName());
                logger.warn("Modern webhook registration for controller '{}' failed; polling remains active, cause={}",
                        deviceId, e.getClass().getSimpleName());
            }
        }
    }

    private Set<String> getConfiguredControllerThingIds(RachioBridgeHandler bridgeHandler) {
        Set<String> controllerIds = new LinkedHashSet<>();
        for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
            if (listener instanceof RachioDeviceHandler deviceHandler) {
                String controllerId = deviceHandler.getBoundControllerId();
                if (controllerId != null && !controllerId.isBlank()) {
                    controllerIds.add(controllerId);
                }
            }
        }
        return controllerIds;
    }

    /** Returns the validated URL used for modern webhook registration, or an empty string. */
    String getModernWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler) {
        RachioConfiguration configuration = bridgeHandler.getWebhookConfiguration();
        if (!configuration.autoConfigureWebhooks) {
            releaseCloudWebhookUrl(bridgeHandler, "modern webhook registration disabled");
            updateWebhookMode(bridgeHandler, "disabled");
            updateWebhookRegistrationState(bridgeHandler, "disabled");
            return "";
        }
        if (!configuration.publicWebhookUrl.isBlank()) {
            releaseCloudWebhookUrl(bridgeHandler, "manual webhook URL configured");
            updateWebhookMode(bridgeHandler, "manual");
            return getManualWebhookUrlForRegistration(bridgeHandler);
        }
        if (configuration.useCloudWebhook) {
            updateWebhookMode(bridgeHandler, "cloud");
            return getCloudWebhookUrlForRegistration(bridgeHandler);
        }

        releaseCloudWebhookUrl(bridgeHandler, "no public webhook URL configured");
        updateWebhookMode(bridgeHandler, "disabled");
        updateWebhookRegistrationState(bridgeHandler, "no public webhook URL configured");
        return "";
    }

    private String getManualWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler) {
        String cachedUrl = getResolvedWebhookState().url();
        if (cachedUrl != null && !cachedUrl.isBlank()) {
            return cachedUrl;
        }

        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        if (lifecycle == null) {
            return "";
        }
        try {
            String webhookUrl = normalizeModernWebhookUrl(bridgeHandler.getWebhookConfiguration().publicWebhookUrl);
            ResolvedWebhookState webhookState = new ResolvedWebhookState(webhookUrl, NO_CLOUD_WEBHOOK_GENERATION,
                    NO_CLOUD_WEBHOOK_CONSUMER_LEASE);
            if (!bridgeHandler.publishWebhookStateIfCurrent(lifecycle, () -> resolvedWebhook.set(webhookState))) {
                return "";
            }
            if (!bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                return "";
            }
            updateWebhookRegistrationState(bridgeHandler, "manual URL ready");
            bridgeHandler.updateProperties();
            return webhookUrl;
        } catch (RachioApiException e) {
            if (bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                updateWebhookRegistrationState(bridgeHandler, "invalid publicWebhookUrl");
                logger.warn("Modern Rachio webhook registration disabled because publicWebhookUrl is invalid, cause={}",
                        e.getClass().getSimpleName());
            }
            return "";
        }
    }

    private String getCloudWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler) {
        String cachedUrl = getResolvedWebhookState().url();
        if (cachedUrl != null && !cachedUrl.isBlank()) {
            return cachedUrl;
        }

        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        if (lifecycle == null) {
            return "";
        }
        updateWebhookRegistrationState(bridgeHandler, "requesting cloud webhook URL");
        CloudWebhookLease acquiredWebhook = null;
        try {
            CloudWebhookLease webhook = cloudWebhookRegistry.acquire(getCloudWebhookConsumerId(bridgeHandler));
            acquiredWebhook = webhook;
            String webhookUrl = normalizeModernWebhookUrl(webhook.url());
            ResolvedWebhookState webhookState = new ResolvedWebhookState(webhookUrl, webhook.generation(),
                    webhook.consumerLease());
            if (!bridgeHandler.publishWebhookStateIfCurrent(lifecycle, () -> resolvedWebhook.set(webhookState))) {
                return "";
            }
            if (!bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                return "";
            }
            updateWebhookRegistrationState(bridgeHandler, "cloud URL ready");
            scheduleCloudWebhookRefresh(bridgeHandler, webhook.expiresAt());
            if (!bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                cancelCloudWebhookRefresh("stale cloud webhook acquisition");
                return "";
            }
            bridgeHandler.updateProperties();
            return webhookUrl;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                updateWebhookRegistrationState(bridgeHandler, "cloud webhook URL request interrupted");
                logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                        e.getClass().getSimpleName());
            }
        } catch (CloudWebhookException e) {
            if (!bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                return "";
            }
            String message = getString(e.getMessage());
            if (CLOUD_WEBHOOK_SERVICE_UNAVAILABLE.equals(message)) {
                if (updateWebhookRegistrationState(bridgeHandler, CLOUD_WEBHOOK_SERVICE_UNAVAILABLE_STATE)) {
                    logger.warn(CLOUD_WEBHOOK_SERVICE_UNAVAILABLE_MESSAGE);
                }
                return "";
            }
            updateWebhookRegistrationState(bridgeHandler, "cloud webhook URL unavailable: " + message);
            logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                    message);
        } catch (RachioApiException e) {
            if (bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                updateWebhookRegistrationState(bridgeHandler,
                        "cloud webhook URL unavailable: " + e.getClass().getSimpleName());
                logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                        e.getClass().getSimpleName());
            }
        } finally {
            if (acquiredWebhook != null
                    && getResolvedWebhookState().consumerLease() != acquiredWebhook.consumerLease()) {
                cloudWebhookRegistry.release(getCloudWebhookConsumerId(bridgeHandler), acquiredWebhook.consumerLease());
            }
        }
        return "";
    }

    private String normalizeModernWebhookUrl(String webhookUrl) throws RachioApiException {
        try {
            URI uri = new URI(webhookUrl.trim());
            if (!uri.isAbsolute() || uri.getRawAuthority() == null || uri.getHost() == null) {
                throw new RachioApiException("Invalid webhook URL format: expected an absolute URL with a host.");
            }
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new RachioApiException("Modern webhook URL must use HTTPS.");
            }
            if (uri.getRawUserInfo() != null) {
                throw new RachioApiException("Modern webhook URL must not contain URL userinfo credentials.");
            }
            return uri.toASCIIString();
        } catch (URISyntaxException e) {
            throw new RachioApiException("Invalid webhook URL format: " + e.getReason());
        }
    }

    private synchronized void scheduleCloudWebhookRefresh(RachioBridgeHandler bridgeHandler, Instant expiresAt) {
        cancelCloudWebhookRefresh("new cloud webhook URL");
        long delaySeconds = Duration.between(Instant.now(), expiresAt.minus(CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW))
                .getSeconds();
        delaySeconds = Math.max(MIN_CLOUD_WEBHOOK_REFRESH_DELAY_SECONDS, delaySeconds);
        try {
            cloudWebhookRefreshJob = scheduler.schedule(() -> onCloudWebhookProviderChanged(bridgeHandler),
                    delaySeconds, TimeUnit.SECONDS);
            logger.debug("RachioCloud: Scheduled openHAB Cloud webhook URL refresh in {} seconds", delaySeconds);
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Unable to schedule openHAB Cloud webhook URL refresh", e);
        }
    }

    private synchronized void cancelCloudWebhookRefresh(String reason) {
        ScheduledFuture<?> refreshJob = cloudWebhookRefreshJob;
        if (refreshJob != null) {
            logger.debug("RachioCloud: Cancelling openHAB Cloud webhook URL refresh ({})", reason);
            refreshJob.cancel(true);
            cloudWebhookRefreshJob = null;
        }
    }

    private void releaseResolvedCloudWebhook(RachioBridgeHandler bridgeHandler) {
        ResolvedWebhookState previous = clearResolvedWebhookState();
        cloudWebhookRegistry.release(getCloudWebhookConsumerId(bridgeHandler), previous.consumerLease());
        if (previous.url() != null) {
            bridgeHandler.updateProperties();
        }
    }

    /** Releases the bridge's acquired openHAB Cloud webhook URL and refresh job. */
    void releaseCloudWebhookUrl(RachioBridgeHandler bridgeHandler, String reason) {
        cancelCloudWebhookRefresh(reason);
        releaseResolvedCloudWebhook(bridgeHandler);
    }

    private ResolvedWebhookState getResolvedWebhookState() {
        return Objects.requireNonNull(resolvedWebhook.get());
    }

    private ResolvedWebhookState clearResolvedWebhookState() {
        return Objects.requireNonNull(resolvedWebhook.getAndSet(ResolvedWebhookState.NONE));
    }

    private String getCloudWebhookConsumerId(RachioBridgeHandler bridgeHandler) {
        return bridgeHandler.getThing().getUID().getAsString();
    }

    private void updateWebhookMode(RachioBridgeHandler bridgeHandler, String mode) {
        if (webhookMode.equals(mode)) {
            return;
        }
        webhookMode = mode;
        bridgeHandler.updateProperties();
    }

    private void updateWebhookRegistrationAttempt(RachioBridgeHandler bridgeHandler) {
        lastWebhookRegistrationAttempt = Instant.now().toString();
        bridgeHandler.updateProperties();
    }

    private boolean updateWebhookRegistrationState(RachioBridgeHandler bridgeHandler, String state) {
        if (webhookRegistrationState.equals(state)) {
            return false;
        }
        webhookRegistrationState = state;
        bridgeHandler.updateProperties();
        return true;
    }

    private void recordSuccessfulModernWebhookRegistration(String deviceId, String webhookUrl,
            @Nullable String externalId) {
        knownModernIrrigationWebhookRegistrations.put(deviceId, modernWebhookRegistrationKey(webhookUrl, externalId));
    }

    private boolean deferKnownModernWebhookVerificationBecauseUrlIsUnavailable(RachioBridgeHandler bridgeHandler,
            String deviceId) {
        if (!knownModernIrrigationWebhookRegistrations.containsKey(deviceId)) {
            return false;
        }
        updateWebhookRegistrationState(bridgeHandler, MODERN_WEBHOOK_VERIFICATION_DEFERRED_STATE);
        logger.debug(
                "Modern webhook verification for controller '{}' deferred because no public webhook URL is currently available; an existing registration may still be active and polling remains fallback.",
                deviceId);
        return true;
    }

    private boolean deferKnownModernWebhookVerificationFailure(RachioBridgeHandler bridgeHandler, String deviceId,
            String webhookUrl, @Nullable String externalId, Throwable e) {
        String knownRegistrationKey = knownModernIrrigationWebhookRegistrations.get(deviceId);
        if (!modernWebhookRegistrationKey(webhookUrl, externalId).equals(knownRegistrationKey)) {
            return false;
        }
        updateWebhookRegistrationState(bridgeHandler, MODERN_WEBHOOK_VERIFICATION_DEFERRED_STATE);
        logger.debug(
                "Modern webhook verification for controller '{}' failed after a previous successful registration; existing registration may still be active and polling remains fallback, cause={}",
                deviceId, e.getClass().getSimpleName());
        return true;
    }

    private String modernWebhookRegistrationKey(String webhookUrl, @Nullable String externalId) {
        return webhookUrl + "\n" + (externalId != null ? externalId : "");
    }

    private void recordWebhookEvent(RachioBridgeHandler bridgeHandler, RachioEventGsonDTO event) {
        lastWebhookEventTimestamp = Instant.now().toString();
        lastWebhookEventType = getEventTypeForProperties(event);
        bridgeHandler.updateProperties();
    }

    private String getEventTypeForProperties(RachioEventGsonDTO event) {
        if (!event.eventType.isBlank()) {
            return event.eventType;
        }
        if (!event.type.isBlank() && !event.subType.isBlank()) {
            return event.type + "." + event.subType;
        }
        if (!event.type.isBlank()) {
            return event.type;
        }
        return "unknown";
    }

    /** Validates and dispatches a modern webhook event according to the active mode. */
    boolean webHookEvent(RachioBridgeHandler bridgeHandler, RachioEventGsonDTO event) {
        if (isModernWebhookEvent(event)) {
            RachioWebhookResourceType resourceType = RachioWebhookResourceType.fromApiValue(event.resourceType);
            if (resourceType == RachioWebhookResourceType.IRRIGATION_CONTROLLER) {
                RachioWebhookMode mode = getActiveIrrigationWebhookProcessingMode(bridgeHandler);
                if (mode != RachioWebhookMode.WEBHOOK_SERVICE) {
                    logger.debug(
                            "RachioCloud: Ignoring modern WebhookService irrigation event eventType='{}', resourceType='{}' because active irrigation webhook processing mode is {}; polling remains active",
                            event.eventType, event.resourceType, mode);
                    return true;
                }
                logger.debug(
                        "RachioCloud: Processing modern WebhookService irrigation event eventType='{}', resourceType='{}' because active irrigation webhook processing mode is {}",
                        event.eventType, event.resourceType, mode);
            } else if (resourceType == RachioWebhookResourceType.VALVE
                    || resourceType == RachioWebhookResourceType.PROGRAM) {
                RachioWebhookMode mode = getWebhookMode(bridgeHandler, resourceType);
                if (mode != RachioWebhookMode.WEBHOOK_SERVICE) {
                    logger.debug(
                            "RachioCloud: Ignoring Smart Hose Timer WebhookService event eventType='{}', resourceType='{}' because active webhook processing mode is {}; polling remains active",
                            event.eventType, event.resourceType, mode);
                    return true;
                }
                logger.debug(
                        "RachioCloud: Processing Smart Hose Timer WebhookService event eventType='{}', resourceType='{}' because active webhook processing mode is {}",
                        event.eventType, event.resourceType, mode);
            }
            boolean dispatched = dispatchWebHookEvent(bridgeHandler, event);
            if (!dispatched) {
                logger.debug(
                        "RachioCloud: Modern webhook event eventType='{}', resourceType='{}', resourceIdPresent={} was not directly handled; reconciliation refresh remains active",
                        event.eventType, event.resourceType, !event.resourceId.isBlank());
            }
            reconcileAfterModernWebhookEvent(bridgeHandler, event);
            return true;
        }
        return dispatchWebHookEvent(bridgeHandler, event);
    }

    private void reconcileAfterModernWebhookEvent(RachioBridgeHandler bridgeHandler, RachioEventGsonDTO event) {
        RachioWebhookResourceType resourceType = RachioWebhookResourceType.fromApiValue(event.resourceType);
        if (resourceType == RachioWebhookResourceType.IRRIGATION_CONTROLLER) {
            logger.debug(
                    "RachioCloud: Scheduling essential controller refresh after modern webhook event eventType='{}', resourceType='{}'",
                    event.eventType, event.resourceType);
            scheduleWebhookReconciliation(bridgeHandler, event.eventType, event.resourceType);
            return;
        }
        if (resourceType == RachioWebhookResourceType.VALVE || resourceType == RachioWebhookResourceType.PROGRAM) {
            logger.debug(
                    "RachioCloud: Smart Hose Timer webhook event eventType='{}', resourceType='{}' handled or acknowledged; Thing polling remains available as reconciliation fallback",
                    event.eventType, event.resourceType);
            return;
        }
        logger.debug(
                "RachioCloud: Modern webhook event eventType='{}', resourceType='{}' acknowledged; polling remains available as fallback",
                event.eventType, event.resourceType);
    }

    private boolean dispatchWebHookEvent(RachioBridgeHandler bridgeHandler, RachioEventGsonDTO event) {
        recordWebhookEvent(bridgeHandler, event);
        return RachioWebhookDispatcher.createDefault(bridgeHandler).dispatch(event);
    }

    private boolean isModernWebhookEvent(RachioEventGsonDTO event) {
        return !event.eventType.isBlank() && !event.resourceType.isBlank();
    }

    /** Validates and dispatches a legacy NotificationService event according to the active mode. */
    boolean legacyWebHookEvent(RachioBridgeHandler bridgeHandler, RachioEventGsonDTO event) {
        if (bridgeHandler.findIrrigationController(event.deviceId) == null) {
            logger.debug("RachioCloud: Rejecting legacy NotificationService event for unknown controller '{}'",
                    event.deviceId);
            return false;
        }
        RachioWebhookMode mode = getActiveIrrigationWebhookProcessingMode(bridgeHandler);
        if (mode == RachioWebhookMode.WEBHOOK_SERVICE) {
            logger.debug(
                    "RachioCloud: Ignoring legacy NotificationService event {}.{} because modern WebhookService mode is active for irrigation events",
                    event.type, event.subType);
            return true;
        }
        if (mode == RachioWebhookMode.DISABLED) {
            logger.debug(
                    "RachioCloud: Ignoring legacy NotificationService event {}.{} because irrigation webhook processing is disabled; polling remains active",
                    event.type, event.subType);
            return true;
        }
        logger.debug(
                "RachioCloud: Dispatching validated legacy NotificationService event {}.{} before reconciliation because active irrigation webhook processing mode is {}",
                event.type, event.subType, mode);
        boolean dispatched = dispatchWebHookEvent(bridgeHandler, event);
        if (!dispatched) {
            logger.debug(
                    "RachioCloud: Legacy NotificationService event {}.{} was not directly handled; reconciliation refresh remains active",
                    event.type, event.subType);
        }
        logger.debug(
                "RachioCloud: Scheduling essential controller refresh after legacy NotificationService event {}.{}",
                event.type, event.subType);
        scheduleWebhookReconciliation(bridgeHandler, event.type, event.subType);
        return true;
    }

    private void scheduleWebhookReconciliation(RachioBridgeHandler bridgeHandler, String eventType,
            String eventDetail) {
        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        if (lifecycle == null) {
            return;
        }
        try {
            scheduler.execute(() -> {
                if (!bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                    return;
                }
                logger.debug("RachioCloud: Running controller reconciliation after webhook event {}.{}", eventType,
                        eventDetail);
                bridgeHandler.refreshDeviceStatus(RachioBridgeHandler.RefreshReason.WEBHOOK_RECONCILIATION);
            });
        } catch (RuntimeException e) {
            logger.debug("RachioCloud: Unable to schedule controller reconciliation after webhook event {}.{}",
                    eventType, eventDetail, e);
        }
    }

    /** Adds coordinator diagnostics to the bridge Thing properties. */
    void addProperties(Map<String, String> properties) {
        properties.put(PROPERTY_WEBHOOK_MODE, webhookMode);
        properties.put(PROPERTY_WEBHOOK_REGISTRATION_STATE, webhookRegistrationState);
        properties.put(PROPERTY_LAST_WEBHOOK_REGISTRATION_ATTEMPT, lastWebhookRegistrationAttempt);
        properties.put(PROPERTY_LAST_WEBHOOK_EVENT_TIMESTAMP, lastWebhookEventTimestamp);
        properties.put(PROPERTY_LAST_WEBHOOK_EVENT_TYPE, lastWebhookEventType);
    }

    private record ResolvedWebhookState(@Nullable String url, long providerGeneration, long consumerLease) {
        private static final ResolvedWebhookState NONE = new ResolvedWebhookState(null, NO_CLOUD_WEBHOOK_GENERATION,
                NO_CLOUD_WEBHOOK_CONSUMER_LEASE);
    }
}
