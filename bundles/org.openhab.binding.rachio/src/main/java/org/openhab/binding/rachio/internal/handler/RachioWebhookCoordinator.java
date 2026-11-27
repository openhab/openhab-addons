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
import static org.openhab.binding.rachio.internal.RachioUtils.isSameInstance;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
 * @author Kovacs Istvan - Cloud webhook recovery
 * @author Kovacs Istvan - Cloud operation ownership and registration convergence
 */
@NonNullByDefault
final class RachioWebhookCoordinator {
    private static final long MIN_CLOUD_WEBHOOK_REFRESH_DELAY_SECONDS = 60;
    private static final long CLOUD_WEBHOOK_RETRY_DELAY_SECONDS = 30;
    private static final long MAX_CLOUD_WEBHOOK_RETRY_DELAY_SECONDS = 300;
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
    private final Map<String, RachioWebhookTarget> pendingCloudWebhookTargets = new ConcurrentHashMap<>();
    private final Set<CloudWebhookAttempt> activeCloudWebhookAttempts = ConcurrentHashMap.newKeySet();
    private final AtomicBoolean cloudWebhookReconciliationPending = new AtomicBoolean();
    private final AtomicBoolean cloudWebhookReconciliationRequested = new AtomicBoolean();
    private final AtomicReference<@Nullable ResolvedWebhookState> cloudWebhookInvalidationTarget = new AtomicReference<>();
    private final AtomicReference<ResolvedWebhookState> resolvedWebhook = new AtomicReference<>(
            ResolvedWebhookState.NONE);
    private @Nullable ScheduledFuture<?> cloudWebhookRefreshJob;
    private @Nullable ScheduledFuture<?> cloudWebhookRetryJob;
    private long cloudWebhookRetryGeneration;
    private long cloudWebhookRetryJobGeneration;
    private long cloudWebhookRetryDelaySeconds = CLOUD_WEBHOOK_RETRY_DELAY_SECONDS;
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
        WebhookRegistrationContext context = currentRegistrationContext(bridgeHandler);
        if (context != null) {
            registerWebHook(bridgeHandler, deviceId, requestPurpose, context);
        }
    }

    private void registerWebHook(RachioBridgeHandler bridgeHandler, String deviceId, RequestPurpose requestPurpose,
            WebhookRegistrationContext context) throws RachioApiException {
        if (!isRegistrationCurrent(bridgeHandler, context)) {
            return;
        }
        RachioConfiguration configuration = context.lifecycle().configuration();
        RachioWebhookMode mode = getWebhookMode(configuration, RachioWebhookResourceType.IRRIGATION_CONTROLLER);
        switch (mode) {
            case LEGACY_NOTIFICATION_SERVICE:
                updateWebhookMode(bridgeHandler, context, "legacy");
                updateWebhookRegistrationAttempt(bridgeHandler, context);
                if (!isRegistrationCurrent(bridgeHandler, context)) {
                    return;
                }
                try {
                    context.lifecycle().api().registerLegacyNotificationWebHook(deviceId, configuration.callbackUrl,
                            configuration.callbackUsername, configuration.callbackPassword,
                            context.lifecycle().api().getExternalId(), configuration.clearAllCallbacks, requestPurpose);
                    publishRegistrationState(bridgeHandler, context, () -> webhookRegistrationState = "registered");
                } finally {
                    reconcileAfterStaleRegistration(bridgeHandler, context);
                }
                break;
            case WEBHOOK_SERVICE:
                RachioWebhookTarget pendingTarget = beginCloudWebhookTarget(bridgeHandler, context,
                        RachioWebhookTarget.irrigationController(deviceId, List.of()));
                CloudWebhookAttempt attempt = new CloudWebhookAttempt(
                        webhookTargetKey(RachioWebhookResourceType.IRRIGATION_CONTROLLER, deviceId), context);
                if (pendingTarget != null && !activeCloudWebhookAttempts.add(attempt)) {
                    scheduleCloudWebhookRetry(bridgeHandler, context.lifecycle(), context.retryGeneration());
                    return;
                }
                boolean registrationAttempted = false;
                String attemptedWebhookUrl = "";
                try {
                    String webhookUrl = getModernWebhookUrlForRegistration(bridgeHandler, context);
                    if (!isRegistrationCurrent(bridgeHandler, context)) {
                        return;
                    }
                    if (webhookUrl.isBlank()) {
                        if (!configuration.callbackUrl.isBlank()) {
                            logger.debug(
                                    "RachioCloud: Modern controller webhook URL is unavailable; using configured legacy NotificationService callback.");
                            updateWebhookMode(bridgeHandler, context, "legacy");
                            updateWebhookRegistrationAttempt(bridgeHandler, context);
                            if (!isRegistrationCurrent(bridgeHandler, context)) {
                                return;
                            }
                            registrationAttempted = true;
                            context.lifecycle().api().registerLegacyNotificationWebHook(deviceId,
                                    configuration.callbackUrl, configuration.callbackUsername,
                                    configuration.callbackPassword, context.lifecycle().api().getExternalId(),
                                    configuration.clearAllCallbacks, requestPurpose);
                            publishRegistrationState(bridgeHandler, context,
                                    () -> webhookRegistrationState = "registered");
                            return;
                        }
                        if (deferKnownModernWebhookVerificationBecauseUrlIsUnavailable(bridgeHandler, context,
                                deviceId)) {
                            return;
                        }
                        logger.debug(
                                "RachioCloud: Modern controller webhook registration is enabled but no public webhook URL is available; polling remains active.");
                        return;
                    }
                    String externalId = context.lifecycle().api().getExternalId();
                    updateWebhookRegistrationAttempt(bridgeHandler, context);
                    if (!isRegistrationCurrent(bridgeHandler, context)) {
                        return;
                    }
                    try {
                        registrationAttempted = true;
                        attemptedWebhookUrl = webhookUrl;
                        context.lifecycle().api().registerWebHook(deviceId, webhookUrl, "", "", externalId,
                                configuration.clearAllCallbacks, requestPurpose);
                        publishRegistrationState(bridgeHandler, context, () -> {
                            if (webhookUrl.equals(getResolvedWebhookState().url())) {
                                recordSuccessfulModernWebhookRegistration(deviceId, webhookUrl, externalId);
                                completeCloudWebhookTarget(pendingTarget);
                                webhookRegistrationState = "registered";
                            }
                        });
                    } catch (RachioApiThrottledException e) {
                        throw e;
                    } catch (RachioApiException | RuntimeException e) {
                        if (!isRegistrationCurrent(bridgeHandler, context)
                                || !deferKnownModernWebhookVerificationFailure(bridgeHandler, context, deviceId,
                                        webhookUrl, externalId, e)) {
                            throw e;
                        }
                    }
                } finally {
                    if (pendingTarget != null) {
                        activeCloudWebhookAttempts.remove(attempt);
                    }
                    if (registrationAttempted) {
                        reconcileAfterStaleRegistration(bridgeHandler, context, attemptedWebhookUrl);
                    }
                    if (pendingTarget != null
                            && pendingCloudWebhookTargets.containsKey(webhookTargetKey(pendingTarget))) {
                        scheduleCloudWebhookRetry(bridgeHandler, context.lifecycle(), context.retryGeneration());
                    }
                }
                break;
            case DISABLED:
                logger.debug("RachioCloud: Controller webhook registration disabled; polling remains active.");
                updateWebhookRegistrationState(bridgeHandler, context, "disabled");
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
        WebhookRegistrationContext context = currentRegistrationContext(bridgeHandler);
        if (context == null || !isRegistrationCurrent(bridgeHandler, context)) {
            return;
        }
        registerSmartHoseWebhook(bridgeHandler, target, resourceLabel, requestPurpose, context);
    }

    private void registerSmartHoseWebhook(RachioBridgeHandler bridgeHandler, RachioWebhookTarget target,
            String resourceLabel, RequestPurpose requestPurpose, WebhookRegistrationContext context)
            throws RachioApiException {
        if (!isRegistrationCurrent(bridgeHandler, context)) {
            return;
        }
        if (getWebhookMode(context.lifecycle().configuration(),
                target.getResourceType()) != RachioWebhookMode.WEBHOOK_SERVICE) {
            logger.debug("RachioCloud: Smart Hose Timer {} webhook registration disabled; polling remains active.",
                    resourceLabel);
            return;
        }
        RachioWebhookTarget pendingTarget = beginCloudWebhookTarget(bridgeHandler, context, target);
        CloudWebhookAttempt attempt = new CloudWebhookAttempt(webhookTargetKey(target), context);
        if (pendingTarget != null && !activeCloudWebhookAttempts.add(attempt)) {
            scheduleCloudWebhookRetry(bridgeHandler, context.lifecycle(), context.retryGeneration());
            return;
        }
        boolean registrationAttempted = false;
        String attemptedWebhookUrl = "";
        try {
            String webhookUrl = getModernWebhookUrlForRegistration(bridgeHandler, context);
            if (!isRegistrationCurrent(bridgeHandler, context)) {
                return;
            }
            if (webhookUrl.isBlank()) {
                logger.debug(
                        "RachioCloud: Smart Hose Timer {} webhook registration is enabled but no public webhook URL is available; polling remains active.",
                        resourceLabel);
                return;
            }
            logger.debug("RachioCloud: Smart Hose Timer {} webhook registration enabled for target '{}'", resourceLabel,
                    target.describe());
            registrationAttempted = true;
            attemptedWebhookUrl = webhookUrl;
            context.lifecycle().api().registerWebHookTarget(target, webhookUrl, "", "",
                    context.lifecycle().api().getExternalId(), context.lifecycle().configuration().clearAllCallbacks,
                    requestPurpose);
            publishRegistrationState(bridgeHandler, context, () -> {
                if (webhookUrl.equals(getResolvedWebhookState().url())) {
                    completeCloudWebhookTarget(pendingTarget);
                }
            });
        } finally {
            if (pendingTarget != null) {
                activeCloudWebhookAttempts.remove(attempt);
            }
            if (registrationAttempted) {
                reconcileAfterStaleRegistration(bridgeHandler, context, attemptedWebhookUrl);
            }
            if (pendingTarget != null && pendingCloudWebhookTargets.containsKey(webhookTargetKey(pendingTarget))) {
                scheduleCloudWebhookRetry(bridgeHandler, context.lifecycle(), context.retryGeneration());
            }
        }
    }

    /** Returns the effective webhook mode for a resource type. */
    RachioWebhookMode getWebhookMode(RachioBridgeHandler bridgeHandler, RachioWebhookResourceType resourceType) {
        RachioConfiguration configuration = bridgeHandler.getWebhookConfiguration();
        return getWebhookMode(configuration, resourceType);
    }

    private RachioWebhookMode getWebhookMode(RachioConfiguration configuration,
            RachioWebhookResourceType resourceType) {
        return selectWebhookMode(resourceType, !configuration.callbackUrl.isBlank(),
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
        cancelCloudWebhookRetry("webhook provider changed");
        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        if (lifecycle == null) {
            return;
        }
        RachioConfiguration configuration = lifecycle.configuration();
        if (!usesCloudWebhook(configuration)) {
            return;
        }
        ResolvedWebhookState previous = getResolvedWebhookState();
        if (previous.url() != null && !cloudWebhookRegistry.isLeaseActive(getCloudWebhookConsumerId(bridgeHandler),
                previous.consumerLease())) {
            cloudWebhookInvalidationTarget.set(previous);
        }
        cloudWebhookReconciliationRequested.set(true);
        scheduleCloudWebhookReconciliation(bridgeHandler);
    }

    private void refreshCloudWebhook(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context,
            ResolvedWebhookState expectedState) {
        AtomicBoolean requested = new AtomicBoolean();
        bridgeHandler.publishWebhookStateIfCurrent(context.lifecycle(), () -> {
            synchronized (this) {
                if (isSameInstance(getResolvedWebhookState(), expectedState)) {
                    cancelCloudWebhookRetry("cloud webhook URL refresh");
                    cloudWebhookInvalidationTarget.set(expectedState);
                    requested.set(true);
                }
            }
        });
        if (!requested.get()) {
            return;
        }
        cloudWebhookRegistry.invalidateCachedWebhook(expectedState.providerGeneration());
        cloudWebhookReconciliationRequested.set(true);
        scheduleCloudWebhookReconciliation(bridgeHandler);
    }

    private void reconcileAfterStaleRegistration(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context) {
        reconcileAfterStaleRegistration(bridgeHandler, context, "");
    }

    private void reconcileAfterStaleRegistration(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context,
            String attemptedWebhookUrl) {
        if (isRegistrationCurrent(bridgeHandler, context)
                && (attemptedWebhookUrl.isBlank() || attemptedWebhookUrl.equals(getResolvedWebhookState().url()))) {
            return;
        }
        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        if (lifecycle == null || getWebhookMode(lifecycle.configuration(),
                RachioWebhookResourceType.IRRIGATION_CONTROLLER) != RachioWebhookMode.WEBHOOK_SERVICE) {
            return;
        }
        // A completed old API call may have replaced the current callback remotely. Reuse the latest URL to repair it.
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
                if (getWebhookMode(configuration,
                        RachioWebhookResourceType.IRRIGATION_CONTROLLER) != RachioWebhookMode.WEBHOOK_SERVICE) {
                    return;
                }
                WebhookRegistrationContext context = new WebhookRegistrationContext(lifecycle,
                        currentCloudWebhookRetryGeneration());
                logger.debug(
                        "RachioCloud: openHAB core WebhookService changed or its URL needs refresh; retrying webhook reconciliation.");
                ResolvedWebhookState webhookState = cloudWebhookInvalidationTarget.getAndSet(null);
                if (webhookState != null && usesCloudWebhook(configuration)) {
                    releaseResolvedCloudWebhookIfCurrent(bridgeHandler, webhookState);
                }
                try {
                    reconcileConfiguredWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH, null, context);
                    reconcileSmartHoseTimerWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH, null, context);
                } finally {
                    schedulePendingCloudWebhookRetry(bridgeHandler, context);
                }
            } while (cloudWebhookReconciliationRequested.get());
        } finally {
            cloudWebhookReconciliationPending.set(false);
            if (cloudWebhookReconciliationRequested.get()) {
                scheduleCloudWebhookReconciliation(bridgeHandler);
            }
        }
    }

    private void reconcileSmartHoseTimerWebhooks(RachioBridgeHandler bridgeHandler, RequestPurpose requestPurpose,
            @Nullable Set<String> pendingTargets, WebhookRegistrationContext context) {
        if (!isRegistrationCurrent(bridgeHandler, context) || (getWebhookMode(context.lifecycle().configuration(),
                RachioWebhookResourceType.VALVE) != RachioWebhookMode.WEBHOOK_SERVICE
                && getWebhookMode(context.lifecycle().configuration(),
                        RachioWebhookResourceType.PROGRAM) != RachioWebhookMode.WEBHOOK_SERVICE)) {
            return;
        }

        for (RachioSmartHoseStatusListener listener : bridgeHandler.smartHoseStatusListeners) {
            if (!isRegistrationCurrent(bridgeHandler, context)) {
                return;
            }
            if (listener instanceof RachioValveHandler valveHandler) {
                String valveId = valveHandler.getThingConfigurationOrPropertyString(PROPERTY_VALVE_ID);
                if (pendingTargets == null
                        || pendingTargets.contains(webhookTargetKey(RachioWebhookResourceType.VALVE, valveId))) {
                    String targetId = valveHandler.getWebhookRegistrationTargetId(bridgeHandler);
                    if (targetId != null) {
                        renewSmartHoseWebhook(bridgeHandler,
                                new RachioWebhookTarget(targetId, RachioWebhookResourceType.VALVE,
                                        List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END)),
                                "valve", requestPurpose, context);
                    }
                }
            } else if (listener instanceof RachioValveProgramHandler programHandler) {
                String programId = programHandler.getThingConfigurationOrPropertyString(PROPERTY_VALVE_PROGRAM_ID);
                if (pendingTargets == null
                        || pendingTargets.contains(webhookTargetKey(RachioWebhookResourceType.PROGRAM, programId))) {
                    String targetId = programHandler.getWebhookRegistrationTargetId(bridgeHandler);
                    if (targetId != null) {
                        renewSmartHoseWebhook(bridgeHandler,
                                new RachioWebhookTarget(targetId, RachioWebhookResourceType.PROGRAM,
                                        List.of(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED)),
                                "program", requestPurpose, context);
                    }
                }
            }
        }
    }

    private void renewSmartHoseWebhook(RachioBridgeHandler bridgeHandler, RachioWebhookTarget target,
            String resourceLabel, RequestPurpose requestPurpose, WebhookRegistrationContext context) {
        try {
            registerSmartHoseWebhook(bridgeHandler, target, resourceLabel, requestPurpose, context);
        } catch (RachioApiException e) {
            logger.debug("RachioCloud: Unable to renew {} webhook registration, cause={}", resourceLabel,
                    e.getClass().getSimpleName());
        }
    }

    /** Reconciles all configured webhook registrations against the current cloud state. */
    void reconcileConfiguredWebhooks(RachioBridgeHandler bridgeHandler, RequestPurpose requestPurpose) {
        WebhookRegistrationContext context = currentRegistrationContext(bridgeHandler);
        if (context != null) {
            try {
                reconcileConfiguredWebhooks(bridgeHandler, requestPurpose, null, context);
                reconcileSmartHoseTimerWebhooks(bridgeHandler, requestPurpose, null, context);
            } finally {
                schedulePendingCloudWebhookRetry(bridgeHandler, context);
            }
        }
    }

    private void reconcileConfiguredWebhooks(RachioBridgeHandler bridgeHandler, RequestPurpose requestPurpose,
            @Nullable Set<String> pendingTargets, WebhookRegistrationContext context) {
        if (!isRegistrationCurrent(bridgeHandler, context)) {
            return;
        }
        RachioConfiguration configuration = context.lifecycle().configuration();
        if (!configuration.autoConfigureWebhooks) {
            releaseCloudWebhookUrl(bridgeHandler, context, "modern webhook registration disabled");
            updateWebhookMode(bridgeHandler, context, "disabled");
            updateWebhookRegistrationState(bridgeHandler, context, "disabled");
            return;
        }
        if (getWebhookMode(configuration,
                RachioWebhookResourceType.IRRIGATION_CONTROLLER) != RachioWebhookMode.WEBHOOK_SERVICE) {
            releaseCloudWebhookUrl(bridgeHandler, context, "modern webhook mode inactive");
            updateWebhookRegistrationState(bridgeHandler, context, "disabled");
            return;
        }

        if (pendingTargets == null && usesCloudWebhook(configuration)) {
            getConfiguredWebhookTargets(bridgeHandler, configuration).values()
                    .forEach(target -> beginCloudWebhookTarget(bridgeHandler, context, target));
        }

        Set<String> configuredControllerIds = getConfiguredControllerThingIds(bridgeHandler);
        if (configuredControllerIds.isEmpty()) {
            updateWebhookRegistrationState(bridgeHandler, context, "waiting for configured controller things");
            return;
        }

        for (String deviceId : configuredControllerIds) {
            if (!isRegistrationCurrent(bridgeHandler, context)) {
                return;
            }
            if (pendingTargets != null && !pendingTargets
                    .contains(webhookTargetKey(RachioWebhookResourceType.IRRIGATION_CONTROLLER, deviceId))) {
                continue;
            }
            try {
                registerWebHook(bridgeHandler, deviceId, requestPurpose, context);
            } catch (RachioApiThrottledException e) {
                if (isRegistrationCurrent(bridgeHandler, context)) {
                    updateWebhookRegistrationState(bridgeHandler, context, "deferred by local API budget guard");
                }
                logger.debug(
                        "Modern webhook registration for controller '{}' deferred because the local Rachio API budget guard is active; polling remains active.",
                        deviceId);
                return;
            } catch (RachioApiException e) {
                if (isRegistrationCurrent(bridgeHandler, context)) {
                    updateWebhookRegistrationState(bridgeHandler, context,
                            "registration failed: " + e.getClass().getSimpleName());
                }
                logger.warn("Modern webhook registration for controller '{}' failed; polling remains active, cause={}",
                        deviceId, e.getClass().getSimpleName());
            } catch (RuntimeException e) {
                if (isRegistrationCurrent(bridgeHandler, context)) {
                    updateWebhookRegistrationState(bridgeHandler, context,
                            "registration failed: " + e.getClass().getSimpleName());
                }
                logger.warn("Modern webhook registration for controller '{}' failed; polling remains active, cause={}",
                        deviceId, e.getClass().getSimpleName());
            }
        }
    }

    private Set<String> getConfiguredControllerThingIds(RachioBridgeHandler bridgeHandler) {
        Set<String> controllerIds = new LinkedHashSet<>();
        for (RachioStatusListener listener : bridgeHandler.rachioStatusListeners) {
            if (listener instanceof RachioDeviceHandler deviceHandler) {
                String controllerId = deviceHandler.getConfiguredControllerId();
                if (controllerId != null && !controllerId.isBlank()) {
                    controllerIds.add(controllerId);
                }
            }
        }
        return controllerIds;
    }

    /** Returns the validated URL used for modern webhook registration, or an empty string. */
    String getModernWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler) {
        WebhookRegistrationContext context = currentRegistrationContext(bridgeHandler);
        return context != null ? getModernWebhookUrlForRegistration(bridgeHandler, context) : "";
    }

    private String getModernWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context) {
        if (!isRegistrationCurrent(bridgeHandler, context)) {
            return "";
        }
        RachioConfiguration configuration = context.lifecycle().configuration();
        if (!configuration.autoConfigureWebhooks) {
            releaseCloudWebhookUrl(bridgeHandler, context, "modern webhook registration disabled");
            updateWebhookMode(bridgeHandler, context, "disabled");
            updateWebhookRegistrationState(bridgeHandler, context, "disabled");
            return "";
        }
        if (!configuration.publicWebhookUrl.isBlank()) {
            releaseCloudWebhookUrl(bridgeHandler, context, "manual webhook URL configured");
            updateWebhookMode(bridgeHandler, context, "manual");
            return getManualWebhookUrlForRegistration(bridgeHandler, context);
        }
        if (configuration.useCloudWebhook) {
            updateWebhookMode(bridgeHandler, context, "cloud");
            return getCloudWebhookUrlForRegistration(bridgeHandler, context);
        }

        releaseCloudWebhookUrl(bridgeHandler, context, "no public webhook URL configured");
        updateWebhookMode(bridgeHandler, context, "disabled");
        updateWebhookRegistrationState(bridgeHandler, context, "no public webhook URL configured");
        return "";
    }

    private String getManualWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context) {
        if (!isRegistrationCurrent(bridgeHandler, context)) {
            return "";
        }
        String cachedUrl = getResolvedWebhookState().url();
        if (cachedUrl != null && !cachedUrl.isBlank()) {
            return cachedUrl;
        }

        LifecycleSnapshot lifecycle = context.lifecycle();
        try {
            String webhookUrl = normalizeModernWebhookUrl(lifecycle.configuration().publicWebhookUrl);
            ResolvedWebhookState webhookState = new ResolvedWebhookState(webhookUrl, NO_CLOUD_WEBHOOK_GENERATION,
                    NO_CLOUD_WEBHOOK_CONSUMER_LEASE);
            if (!publishRegistrationState(bridgeHandler, context, () -> {
                resolvedWebhook.set(webhookState);
                webhookRegistrationState = "manual URL ready";
            })) {
                return "";
            }
            if (!bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                return "";
            }
            return webhookUrl;
        } catch (RachioApiException e) {
            if (bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())) {
                updateWebhookRegistrationState(bridgeHandler, context, "invalid publicWebhookUrl");
                logger.warn("Modern Rachio webhook registration disabled because publicWebhookUrl is invalid, cause={}",
                        e.getClass().getSimpleName());
            }
            return "";
        }
    }

    private String getCloudWebhookUrlForRegistration(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context) {
        if (!isRegistrationCurrent(bridgeHandler, context)) {
            return "";
        }
        ResolvedWebhookState cachedState = getResolvedWebhookState();
        String cachedUrl = cachedState.url();
        if (cachedUrl != null && !cachedUrl.isBlank() && cloudWebhookRegistry
                .isLeaseActive(getCloudWebhookConsumerId(bridgeHandler), cachedState.consumerLease())) {
            return cachedUrl;
        }

        LifecycleSnapshot lifecycle = context.lifecycle();
        long retryGeneration = context.retryGeneration();
        updateWebhookRegistrationState(bridgeHandler, context, "requesting cloud webhook URL");
        CloudWebhookLease acquiredWebhook = null;
        boolean acquisitionSucceeded = false;
        try {
            if (!isRegistrationCurrent(bridgeHandler, context)) {
                return "";
            }
            CloudWebhookLease webhook = cloudWebhookRegistry.acquire(getCloudWebhookConsumerId(bridgeHandler));
            acquiredWebhook = webhook;
            String webhookUrl = normalizeModernWebhookUrl(webhook.url());
            ResolvedWebhookState webhookState = new ResolvedWebhookState(webhookUrl, webhook.generation(),
                    webhook.consumerLease());
            AtomicReference<ResolvedWebhookState> selectedState = new AtomicReference<>(webhookState);
            AtomicReference<@Nullable ResolvedWebhookState> previousState = new AtomicReference<>();
            if (!publishRegistrationState(bridgeHandler, context, () -> {
                ResolvedWebhookState current = getResolvedWebhookState();
                if (current.url() != null && current.providerGeneration() >= webhook.generation()
                        && cloudWebhookRegistry.isLeaseActive(getCloudWebhookConsumerId(bridgeHandler),
                                current.consumerLease())) {
                    selectedState.set(current);
                    return;
                }
                previousState.set(resolvedWebhook.getAndSet(webhookState));
                webhookRegistrationState = "cloud URL ready";
                if (pendingCloudWebhookTargets.isEmpty()) {
                    cancelCloudWebhookRetryJob("cloud webhook URL acquired");
                }
            })) {
                return "";
            }
            ResolvedWebhookState previous = previousState.get();
            if (previous != null) {
                releaseCloudWebhookLease(bridgeHandler, previous);
            }
            if (!isRegistrationCurrent(bridgeHandler, context)) {
                return "";
            }
            if (isSameInstance(selectedState.get(), webhookState)) {
                scheduleCloudWebhookRefresh(bridgeHandler, context, webhookState, webhook.expiresAt());
            }
            if (!isRegistrationCurrent(bridgeHandler, context)) {
                return "";
            }
            bridgeHandler.updateProperties();
            acquisitionSucceeded = true;
            return getString(Objects.requireNonNull(selectedState.get()).url());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            if (updateCloudWebhookFailureState(bridgeHandler, lifecycle, retryGeneration,
                    "cloud webhook URL request interrupted")) {
                logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                        e.getClass().getSimpleName());
            }
        } catch (CloudWebhookException e) {
            String message = getString(e.getMessage());
            if (CLOUD_WEBHOOK_SERVICE_UNAVAILABLE.equals(message)) {
                if (updateCloudWebhookFailureState(bridgeHandler, lifecycle, retryGeneration,
                        CLOUD_WEBHOOK_SERVICE_UNAVAILABLE_STATE)) {
                    logger.warn(CLOUD_WEBHOOK_SERVICE_UNAVAILABLE_MESSAGE);
                }
                return "";
            }
            if (updateCloudWebhookFailureState(bridgeHandler, lifecycle, retryGeneration,
                    "cloud webhook URL unavailable: " + message)) {
                logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                        message);
            }
        } catch (RachioApiException e) {
            if (updateCloudWebhookFailureState(bridgeHandler, lifecycle, retryGeneration,
                    "cloud webhook URL unavailable: " + e.getClass().getSimpleName())) {
                logger.warn("Unable to request openHAB Cloud webhook URL for Rachio; polling remains active, cause={}",
                        e.getClass().getSimpleName());
            }
        } finally {
            if (!acquisitionSucceeded) {
                scheduleCloudWebhookRetry(bridgeHandler, lifecycle, retryGeneration);
            }
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

    private void scheduleCloudWebhookRefresh(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context,
            ResolvedWebhookState webhookState, Instant expiresAt) {
        long delaySeconds = Math.max(MIN_CLOUD_WEBHOOK_REFRESH_DELAY_SECONDS,
                Duration.between(Instant.now(), expiresAt.minus(CLOUD_WEBHOOK_REFRESH_SAFETY_WINDOW)).getSeconds());
        publishRegistrationState(bridgeHandler, context, () -> {
            if (!isSameInstance(getResolvedWebhookState(), webhookState)) {
                return;
            }
            cancelCloudWebhookRefresh("new cloud webhook URL");
            try {
                cloudWebhookRefreshJob = scheduler.schedule(() -> {
                    refreshCloudWebhook(bridgeHandler, context, webhookState);
                }, delaySeconds, TimeUnit.SECONDS);
                logger.debug("RachioCloud: Scheduled openHAB Cloud webhook URL refresh in {} seconds", delaySeconds);
            } catch (RuntimeException e) {
                logger.debug("RachioCloud: Unable to schedule openHAB Cloud webhook URL refresh", e);
            }
        });
    }

    private synchronized void cancelCloudWebhookRefresh(String reason) {
        ScheduledFuture<?> refreshJob = cloudWebhookRefreshJob;
        if (refreshJob != null) {
            logger.debug("RachioCloud: Cancelling openHAB Cloud webhook URL refresh ({})", reason);
            refreshJob.cancel(true);
            cloudWebhookRefreshJob = null;
        }
    }

    private synchronized long currentCloudWebhookRetryGeneration() {
        return cloudWebhookRetryGeneration;
    }

    private @Nullable WebhookRegistrationContext currentRegistrationContext(RachioBridgeHandler bridgeHandler) {
        LifecycleSnapshot lifecycle = bridgeHandler.currentLifecycleSnapshot();
        return lifecycle != null ? new WebhookRegistrationContext(lifecycle, currentCloudWebhookRetryGeneration())
                : null;
    }

    private boolean usesCloudWebhook(RachioConfiguration configuration) {
        return configuration.autoConfigureWebhooks && configuration.useCloudWebhook
                && configuration.publicWebhookUrl.isBlank();
    }

    private boolean isRegistrationCurrent(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context) {
        LifecycleSnapshot lifecycle = context.lifecycle();
        return bridgeHandler.isLifecycleCurrent(lifecycle.generation(), lifecycle.api())
                && (!usesCloudWebhook(lifecycle.configuration())
                        || context.retryGeneration() == currentCloudWebhookRetryGeneration());
    }

    private boolean publishRegistrationState(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context,
            Runnable publisher) {
        AtomicBoolean published = new AtomicBoolean();
        bridgeHandler.publishWebhookStateIfCurrent(context.lifecycle(), () -> {
            synchronized (this) {
                if (!usesCloudWebhook(context.lifecycle().configuration())
                        || context.retryGeneration() == cloudWebhookRetryGeneration) {
                    publisher.run();
                    published.set(true);
                }
            }
        });
        if (published.get()) {
            bridgeHandler.updateProperties();
        }
        return published.get();
    }

    private record WebhookRegistrationContext(LifecycleSnapshot lifecycle, long retryGeneration) {
    }

    private record CloudWebhookAttempt(String target, WebhookRegistrationContext context) {
    }

    private @Nullable RachioWebhookTarget beginCloudWebhookTarget(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context, RachioWebhookTarget target) {
        LifecycleSnapshot lifecycle = context.lifecycle();
        if (!usesCloudWebhook(lifecycle.configuration())) {
            return null;
        }
        AtomicReference<@Nullable RachioWebhookTarget> pendingTarget = new AtomicReference<>();
        bridgeHandler.publishWebhookStateIfCurrent(lifecycle, () -> {
            synchronized (this) {
                if (context.retryGeneration() == cloudWebhookRetryGeneration) {
                    RachioWebhookTarget previous = pendingCloudWebhookTargets.putIfAbsent(webhookTargetKey(target),
                            target);
                    pendingTarget.set(previous != null ? previous : target);
                }
            }
        });
        return pendingTarget.get();
    }

    private synchronized void completeCloudWebhookTarget(@Nullable RachioWebhookTarget target) {
        if (target == null) {
            return;
        }
        pendingCloudWebhookTargets.remove(webhookTargetKey(target), target);
        if (pendingCloudWebhookTargets.isEmpty() && getResolvedWebhookState().url() != null) {
            cancelCloudWebhookRetryJob("configured webhook targets reconciled");
        }
    }

    private String webhookTargetKey(RachioWebhookResourceType resourceType, String resourceId) {
        String keyId = resourceType == RachioWebhookResourceType.IRRIGATION_CONTROLLER
                ? resourceId.toLowerCase(Locale.ROOT)
                : resourceId;
        return resourceType.getApiValue() + ":" + keyId;
    }

    private String webhookTargetKey(RachioWebhookTarget target) {
        return webhookTargetKey(target.getResourceType(), target.getResourceId());
    }

    private Map<String, RachioWebhookTarget> getConfiguredWebhookTargets(RachioBridgeHandler bridgeHandler,
            RachioConfiguration configuration) {
        Map<String, RachioWebhookTarget> configuredTargets = new LinkedHashMap<>();
        for (String controllerId : getConfiguredControllerThingIds(bridgeHandler)) {
            RachioWebhookTarget target = RachioWebhookTarget.irrigationController(controllerId, List.of());
            configuredTargets.put(webhookTargetKey(target), target);
        }
        if (configuration.autoConfigureHoseTimerWebhooks) {
            for (RachioSmartHoseStatusListener listener : bridgeHandler.smartHoseStatusListeners) {
                if (listener instanceof RachioValveHandler valveHandler) {
                    String valveId = valveHandler.getThingConfigurationOrPropertyString(PROPERTY_VALVE_ID);
                    if (!valveId.isBlank()) {
                        RachioWebhookTarget target = new RachioWebhookTarget(valveId, RachioWebhookResourceType.VALVE,
                                List.of(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END));
                        configuredTargets.put(webhookTargetKey(target), target);
                    }
                } else if (listener instanceof RachioValveProgramHandler programHandler) {
                    String programId = programHandler.getThingConfigurationOrPropertyString(PROPERTY_VALVE_PROGRAM_ID);
                    if (!programId.isBlank()) {
                        RachioWebhookTarget target = new RachioWebhookTarget(programId,
                                RachioWebhookResourceType.PROGRAM,
                                List.of(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED));
                        configuredTargets.put(webhookTargetKey(target), target);
                    }
                }
            }
        }
        return configuredTargets;
    }

    private void scheduleCloudWebhookRetry(RachioBridgeHandler bridgeHandler, LifecycleSnapshot lifecycle,
            long retryGeneration) {
        if (!usesCloudWebhook(lifecycle.configuration())) {
            return;
        }
        bridgeHandler.publishWebhookStateIfCurrent(lifecycle, () -> {
            synchronized (this) {
                if (retryGeneration != cloudWebhookRetryGeneration || cloudWebhookRetryJob != null
                        || (getResolvedWebhookState().url() != null && pendingCloudWebhookTargets.isEmpty())) {
                    return;
                }
                long jobGeneration = ++cloudWebhookRetryJobGeneration;
                long delaySeconds = cloudWebhookRetryDelaySeconds;
                try {
                    cloudWebhookRetryJob = scheduler.schedule(() -> retryCloudWebhookRegistration(bridgeHandler,
                            lifecycle, retryGeneration, jobGeneration), delaySeconds, TimeUnit.SECONDS);
                    logger.debug("RachioCloud: Scheduled cloud webhook registration retry in {} seconds", delaySeconds);
                } catch (RuntimeException e) {
                    logger.debug("RachioCloud: Unable to schedule cloud webhook registration retry", e);
                }
            }
        });
    }

    private void schedulePendingCloudWebhookRetry(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context) {
        if (!usesCloudWebhook(context.lifecycle().configuration()) || !isRegistrationCurrent(bridgeHandler, context)) {
            return;
        }
        Map<String, RachioWebhookTarget> pendingTargets = Map.copyOf(pendingCloudWebhookTargets);
        Set<String> configuredTargets = getConfiguredWebhookTargets(bridgeHandler, context.lifecycle().configuration())
                .keySet();
        bridgeHandler.publishWebhookStateIfCurrent(context.lifecycle(), () -> {
            synchronized (this) {
                if (context.retryGeneration() == cloudWebhookRetryGeneration) {
                    pendingTargets.forEach((key, target) -> {
                        if (!configuredTargets.contains(key)) {
                            completeCloudWebhookTarget(target);
                        }
                    });
                }
            }
        });
        if (!pendingCloudWebhookTargets.isEmpty()) {
            scheduleCloudWebhookRetry(bridgeHandler, context.lifecycle(), context.retryGeneration());
        }
    }

    private void retryCloudWebhookRegistration(RachioBridgeHandler bridgeHandler, LifecycleSnapshot lifecycle,
            long retryGeneration, long jobGeneration) {
        synchronized (this) {
            if (retryGeneration != cloudWebhookRetryGeneration || jobGeneration != cloudWebhookRetryJobGeneration) {
                return;
            }
            cloudWebhookRetryJob = null;
            cloudWebhookRetryDelaySeconds = Math.min(MAX_CLOUD_WEBHOOK_RETRY_DELAY_SECONDS,
                    cloudWebhookRetryDelaySeconds * 2);
        }
        WebhookRegistrationContext context = new WebhookRegistrationContext(lifecycle, retryGeneration);
        if (!isRegistrationCurrent(bridgeHandler, context) || !usesCloudWebhook(lifecycle.configuration())) {
            return;
        }
        // A retry must preserve a URL acquired by another operation since the failure.
        Map<String, RachioWebhookTarget> pendingTargets;
        synchronized (this) {
            pendingTargets = Map.copyOf(pendingCloudWebhookTargets);
        }
        try {
            if (pendingTargets.isEmpty()) {
                reconcileConfiguredWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH, null, context);
                reconcileSmartHoseTimerWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH, null, context);
                return;
            }
            Set<String> configuredTargets = getConfiguredWebhookTargets(bridgeHandler, lifecycle.configuration())
                    .keySet();
            Set<String> targetsToRetry = new LinkedHashSet<>();
            pendingTargets.forEach((key, target) -> {
                if (configuredTargets.contains(key)) {
                    targetsToRetry.add(key);
                } else {
                    completeCloudWebhookTarget(target);
                }
            });
            if (!targetsToRetry.isEmpty()) {
                reconcileConfiguredWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH, targetsToRetry, context);
                reconcileSmartHoseTimerWebhooks(bridgeHandler, RequestPurpose.BACKGROUND_REFRESH, targetsToRetry,
                        context);
            }
        } finally {
            schedulePendingCloudWebhookRetry(bridgeHandler, context);
        }
    }

    private synchronized void cancelCloudWebhookRetry(String reason) {
        cloudWebhookRetryGeneration++;
        pendingCloudWebhookTargets.clear();
        cancelCloudWebhookRetryJob(reason);
    }

    private synchronized void cancelCloudWebhookRetryJob(String reason) {
        cloudWebhookRetryJobGeneration++;
        cloudWebhookRetryDelaySeconds = CLOUD_WEBHOOK_RETRY_DELAY_SECONDS;
        ScheduledFuture<?> retryJob = cloudWebhookRetryJob;
        if (retryJob != null) {
            logger.debug("RachioCloud: Cancelling cloud webhook registration retry ({})", reason);
            retryJob.cancel(false);
            cloudWebhookRetryJob = null;
        }
    }

    private void releaseResolvedCloudWebhook(RachioBridgeHandler bridgeHandler) {
        ResolvedWebhookState previous = clearResolvedWebhookState();
        releaseCloudWebhookLease(bridgeHandler, previous);
    }

    private void releaseResolvedCloudWebhookIfCurrent(RachioBridgeHandler bridgeHandler,
            ResolvedWebhookState expectedState) {
        if (resolvedWebhook.compareAndSet(expectedState, ResolvedWebhookState.NONE)) {
            releaseCloudWebhookLease(bridgeHandler, expectedState);
        }
    }

    private void releaseCloudWebhookLease(RachioBridgeHandler bridgeHandler, ResolvedWebhookState previous) {
        cloudWebhookRegistry.release(getCloudWebhookConsumerId(bridgeHandler), previous.consumerLease());
        if (previous.url() != null) {
            bridgeHandler.updateProperties();
        }
    }

    /** Releases the bridge's acquired openHAB Cloud webhook URL and refresh job. */
    void releaseCloudWebhookUrl(RachioBridgeHandler bridgeHandler, String reason) {
        cancelCloudWebhookRetry(reason);
        cancelCloudWebhookRefresh(reason);
        releaseResolvedCloudWebhook(bridgeHandler);
    }

    private void releaseCloudWebhookUrl(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context,
            String reason) {
        AtomicReference<@Nullable ResolvedWebhookState> previousState = new AtomicReference<>();
        publishRegistrationState(bridgeHandler, context, () -> {
            cancelCloudWebhookRetry(reason);
            cancelCloudWebhookRefresh(reason);
            previousState.set(clearResolvedWebhookState());
        });
        ResolvedWebhookState previous = previousState.get();
        if (previous != null) {
            releaseCloudWebhookLease(bridgeHandler, previous);
        }
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

    private void updateWebhookMode(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context, String mode) {
        if (webhookMode.equals(mode)) {
            return;
        }
        publishRegistrationState(bridgeHandler, context, () -> webhookMode = mode);
    }

    private void updateWebhookRegistrationAttempt(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context) {
        publishRegistrationState(bridgeHandler, context,
                () -> lastWebhookRegistrationAttempt = Instant.now().toString());
    }

    private void updateWebhookRegistrationState(RachioBridgeHandler bridgeHandler, WebhookRegistrationContext context,
            String state) {
        if (webhookRegistrationState.equals(state)) {
            return;
        }
        publishRegistrationState(bridgeHandler, context, () -> webhookRegistrationState = state);
    }

    private boolean updateCloudWebhookFailureState(RachioBridgeHandler bridgeHandler, LifecycleSnapshot lifecycle,
            long retryGeneration, String state) {
        AtomicBoolean changed = new AtomicBoolean();
        publishRegistrationState(bridgeHandler, new WebhookRegistrationContext(lifecycle, retryGeneration), () -> {
            if (getResolvedWebhookState().url() == null && !webhookRegistrationState.equals(state)) {
                webhookRegistrationState = state;
                changed.set(true);
            }
        });
        return changed.get();
    }

    private void recordSuccessfulModernWebhookRegistration(String deviceId, String webhookUrl,
            @Nullable String externalId) {
        knownModernIrrigationWebhookRegistrations.put(deviceId, modernWebhookRegistrationKey(webhookUrl, externalId));
    }

    private boolean deferKnownModernWebhookVerificationBecauseUrlIsUnavailable(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context, String deviceId) {
        if (!knownModernIrrigationWebhookRegistrations.containsKey(deviceId)) {
            return false;
        }
        publishRegistrationState(bridgeHandler, context,
                () -> webhookRegistrationState = MODERN_WEBHOOK_VERIFICATION_DEFERRED_STATE);
        logger.debug(
                "Modern webhook verification for controller '{}' deferred because no public webhook URL is currently available; an existing registration may still be active and polling remains fallback.",
                deviceId);
        return true;
    }

    private boolean deferKnownModernWebhookVerificationFailure(RachioBridgeHandler bridgeHandler,
            WebhookRegistrationContext context, String deviceId, String webhookUrl, @Nullable String externalId,
            Throwable e) {
        String knownRegistrationKey = knownModernIrrigationWebhookRegistrations.get(deviceId);
        if (!modernWebhookRegistrationKey(webhookUrl, externalId).equals(knownRegistrationKey)) {
            return false;
        }
        publishRegistrationState(bridgeHandler, context,
                () -> webhookRegistrationState = MODERN_WEBHOOK_VERIFICATION_DEFERRED_STATE);
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
