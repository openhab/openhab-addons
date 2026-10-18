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
package org.openhab.binding.plivo.internal.service;

import static org.openhab.binding.plivo.internal.PlivoBindingConstants.BINDING_ID;
import static org.openhab.binding.plivo.internal.PlivoBindingConstants.SERVLET_PATH;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the binding-wide openHAB Cloud webhook registration used for receiving Plivo callbacks.
 * <p>
 * All Plivo phone Things share one cloud webhook mapping at
 * {@link org.openhab.binding.plivo.internal.PlivoBindingConstants#SERVLET_PATH}. Each account
 * Thing that wants cloud webhooks calls {@link #register(String)}; the first such call contacts
 * the core {@link WebhookService} and starts a daily refresh task to keep the registration's TTL
 * from expiring. The webhook is only removed from the cloud when an account explicitly calls
 * {@link #unregister(String)} (e.g. when the user turns {@code useCloudWebhook} off in the config)
 * and no other accounts still require it. Bundle stop/restart deliberately does not remove the
 * webhook, so restarts don't churn the cloud registration.
 * <p>
 * Registration is asynchronous. {@link WebhookService#requestWebhook(String)} can stay pending for
 * well over a minute while openHAB Cloud reconnects, so the returned future is always handled to
 * completion rather than waited on: a late success still publishes the URL and notifies listeners.
 * A failed attempt is retried with exponential backoff for as long as requestors remain, so a
 * temporary cloud outage cannot leave cloud webhooks permanently unavailable. Callers observe the
 * result through {@link #getBaseUrl()} and {@link #addAvailabilityListener(Runnable)}.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@Component(service = PlivoCloudWebhookService.class)
@NonNullByDefault
public class PlivoCloudWebhookService {

    private final Logger logger = LoggerFactory.getLogger(PlivoCloudWebhookService.class);

    private static final long REFRESH_INTERVAL_HOURS = 24;
    private static final long RETRY_INITIAL_SECONDS = 30;
    private static final long RETRY_MAX_SECONDS = 600;

    private final ScheduledExecutorService scheduler;
    private final Object lock = new Object();
    private final Set<String> requestors = new HashSet<>();
    private final Set<Runnable> availabilityListeners = ConcurrentHashMap.newKeySet();

    private volatile @Nullable WebhookService webhookService;
    private @Nullable String baseUrl;
    private @Nullable ScheduledFuture<?> refreshTask;
    private @Nullable ScheduledFuture<?> retryTask;
    private boolean registrationInFlight;
    private boolean deactivated;
    private long retryDelaySeconds = RETRY_INITIAL_SECONDS;

    public PlivoCloudWebhookService() {
        this(ThreadPoolManager.getScheduledPool(BINDING_ID + "-cloud-webhook"));
    }

    /**
     * Creates a service using the given scheduler. Exists so tests can drive the retry backoff
     * without waiting for real time to pass.
     *
     * @param scheduler the scheduler used for the refresh and retry tasks
     */
    PlivoCloudWebhookService(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    void setWebhookService(WebhookService service) {
        boolean retry;
        synchronized (lock) {
            this.webhookService = service;
            // A fresh service is a fresh chance: drop any backoff accumulated against the old one.
            cancelRetryTask();
            retryDelaySeconds = RETRY_INITIAL_SECONDS;
            retry = !requestors.isEmpty() && baseUrl == null;
        }
        if (retry) {
            logger.debug("WebhookService now available; attempting deferred webhook registration");
            scheduler.execute(this::doRegister);
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    void unsetWebhookService(WebhookService service) {
        synchronized (lock) {
            if (this.webhookService == service) {
                this.webhookService = null;
                baseUrl = null;
                cancelRefreshTask();
                cancelRetryTask();
            }
        }
    }

    @Deactivate
    public void deactivate() {
        synchronized (lock) {
            deactivated = true;
            cancelRefreshTask();
            cancelRetryTask();
            requestors.clear();
            baseUrl = null;
        }
    }

    /**
     * Registers the cloud webhook on behalf of the given requestor (typically a Thing UID).
     * <p>
     * The request is made asynchronously and this method returns immediately. When the
     * {@link WebhookService} is not yet available, or the request fails, the registration is
     * retried automatically for as long as requestors remain. Use {@link #getBaseUrl()} to read the
     * resulting URL and {@link #addAvailabilityListener(Runnable)} to be told when it arrives.
     *
     * @param requestorId an identifier (typically a Thing UID) so the service can ref-count
     *            registrations across multiple Things
     */
    public void register(String requestorId) {
        synchronized (lock) {
            requestors.add(requestorId);
        }
        doRegister();
    }

    /**
     * Releases this requestor's interest in the cloud webhook. When the last requestor unregisters,
     * the webhook is removed from the {@link WebhookService}.
     *
     * @param requestorId the same identifier previously passed to {@link #register(String)}
     */
    public void unregister(String requestorId) {
        boolean removeNeeded;
        WebhookService ws;
        synchronized (lock) {
            if (!requestors.remove(requestorId) || !requestors.isEmpty()) {
                return;
            }
            removeNeeded = baseUrl != null;
            ws = webhookService;
            baseUrl = null;
            cancelRefreshTask();
            cancelRetryTask();
        }
        if (removeNeeded && ws != null) {
            removeWebhook(ws, "last requestor unregistered");
        }
    }

    /**
     * @return the current cloud webhook base URL, or {@code null} if not yet registered.
     */
    public @Nullable String getBaseUrl() {
        synchronized (lock) {
            return baseUrl;
        }
    }

    /**
     * Registers a listener that is notified once the cloud webhook base URL first becomes available.
     * This lets handlers that already initialized (before the {@link WebhookService} bound) refresh
     * their webhook configuration.
     *
     * @param listener the callback to invoke when the base URL becomes available
     */
    public void addAvailabilityListener(Runnable listener) {
        availabilityListeners.add(listener);
    }

    /**
     * Removes a previously registered availability listener.
     *
     * @param listener the callback to remove
     */
    public void removeAvailabilityListener(Runnable listener) {
        availabilityListeners.remove(listener);
    }

    private void doRegister() {
        WebhookService requestService;
        synchronized (lock) {
            if (baseUrl != null || registrationInFlight) {
                return;
            }
            WebhookService ws = webhookService;
            if (ws == null || requestors.isEmpty()) {
                return;
            }
            // Capture the service this registration is made through, so a replacement that binds
            // while the request is in flight cannot be mistaken for the one that created the result.
            requestService = ws;
            registrationInFlight = true;
            cancelRetryTask();
        }
        try {
            requestService.requestWebhook(SERVLET_PATH)
                    .whenComplete((hook, error) -> onRegistrationComplete(requestService, hook, error));
        } catch (RuntimeException e) {
            // A service that throws instead of returning a failed future would otherwise leave
            // registrationInFlight latched, and no further attempt would ever be made.
            onRegistrationComplete(requestService, null, e);
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void onRegistrationComplete(WebhookService requestService, @Nullable Webhook hook,
            @Nullable Throwable error) {
        boolean newlyRegistered = false;
        boolean orphaned = false;
        String url = "";
        synchronized (lock) {
            registrationInFlight = false;
            if (error != null || hook == null) {
                scheduleRetry(error);
                return;
            }
            url = hook.url().toString();
            if (baseUrl != null) {
                // Another thread registered the same path while this request was in flight. Both
                // asked the service for SERVLET_PATH, so there is nothing orphaned to clean up.
                return;
            }
            if (webhookService == requestService && !requestors.isEmpty()) {
                baseUrl = url;
                newlyRegistered = true;
                retryDelaySeconds = RETRY_INITIAL_SECONDS;
                if (refreshTask == null) {
                    refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, REFRESH_INTERVAL_HOURS,
                            REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
                }
            } else {
                // On deactivation the registration is deliberately left in place, so that stopping
                // or restarting the bundle does not churn the cloud registration.
                orphaned = !deactivated;
            }
        }
        if (newlyRegistered) {
            logger.debug("Cloud webhook base URL: {}", url);
            notifyAvailabilityListeners();
            return;
        }
        if (orphaned) {
            // The last requestor unregistered, or the service was replaced, while the request was in
            // flight. Publishing now would resurrect a registration nobody wants, so drop it through
            // the same service that created it.
            removeWebhook(requestService, "no longer required after registration");
        }
    }

    /**
     * Schedules another registration attempt. Must be called while holding {@link #lock}.
     */
    private void scheduleRetry(@Nullable Throwable error) {
        if (requestors.isEmpty() || webhookService == null) {
            logger.debug("Cloud webhook request for {} failed ({}); not retrying because it is no longer required",
                    SERVLET_PATH, describe(error));
            return;
        }
        long delay = retryDelaySeconds;
        if (delay == RETRY_INITIAL_SECONDS) {
            logger.warn("Failed to request an openHAB Cloud webhook for {}: {}. Retrying in {} seconds", SERVLET_PATH,
                    describe(error), delay);
        } else {
            // Already reported once at warn level; keep repeated outage messages out of the log.
            logger.debug("Cloud webhook request for {} still failing: {}. Retrying in {} seconds", SERVLET_PATH,
                    describe(error), delay);
        }
        retryDelaySeconds = Math.min(delay * 2, RETRY_MAX_SECONDS);
        cancelRetryTask();
        retryTask = scheduler.schedule(this::doRegister, delay, TimeUnit.SECONDS);
    }

    private void notifyAvailabilityListeners() {
        for (Runnable listener : availabilityListeners) {
            try {
                listener.run();
            } catch (RuntimeException e) {
                logger.debug("Cloud webhook availability listener failed: {}", e.getMessage());
            }
        }
    }

    private void refresh() {
        WebhookService requestService;
        synchronized (lock) {
            WebhookService ws = webhookService;
            if (ws == null || requestors.isEmpty() || baseUrl == null) {
                return;
            }
            requestService = ws;
        }
        try {
            requestService.requestWebhook(SERVLET_PATH)
                    .whenComplete((hook, error) -> onRefreshComplete(requestService, hook, error));
        } catch (RuntimeException e) {
            // This runs on scheduleWithFixedDelay, where an escaping exception would silently
            // cancel every future refresh.
            logger.debug("Failed to refresh the openHAB Cloud webhook for {}: {}", SERVLET_PATH, describe(e));
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void onRefreshComplete(WebhookService requestService, @Nullable Webhook hook, @Nullable Throwable error) {
        if (error != null || hook == null) {
            // The existing registration keeps working until its TTL expires, and the next scheduled
            // refresh will try again, so a single failure is not worth reporting at warn level.
            logger.debug("Failed to refresh the openHAB Cloud webhook for {}: {}", SERVLET_PATH, describe(error));
            return;
        }
        boolean refreshed = false;
        boolean stopping;
        synchronized (lock) {
            // Only keep the refreshed URL while the registration it belongs to is still live;
            // otherwise an in-flight refresh would restore a URL that unregister() just cleared.
            if (webhookService == requestService && !requestors.isEmpty() && baseUrl != null) {
                baseUrl = hook.url().toString();
                refreshed = true;
                logger.trace("Cloud webhook refreshed: {}", baseUrl);
            }
            stopping = deactivated;
        }
        if (!refreshed && !stopping) {
            removeWebhook(requestService, "registration torn down during refresh");
        }
    }

    private void removeWebhook(WebhookService service, String reason) {
        try {
            service.removeWebhook(SERVLET_PATH).whenComplete((unused, error) -> {
                if (error != null) {
                    logger.debug("Failed to remove cloud webhook for {} ({}): {}", SERVLET_PATH, reason,
                            describe(error));
                } else {
                    logger.debug("Cloud webhook removed for {} ({})", SERVLET_PATH, reason);
                }
            });
        } catch (RuntimeException e) {
            logger.debug("Failed to remove cloud webhook for {} ({}): {}", SERVLET_PATH, reason, describe(e));
        }
    }

    /**
     * Must be called while holding {@link #lock}.
     */
    private void cancelRefreshTask() {
        ScheduledFuture<?> task = refreshTask;
        if (task != null) {
            task.cancel(false);
            refreshTask = null;
        }
    }

    /**
     * Must be called while holding {@link #lock}.
     */
    private void cancelRetryTask() {
        ScheduledFuture<?> task = retryTask;
        if (task != null) {
            task.cancel(false);
            retryTask = null;
        }
    }

    private static String describe(@Nullable Throwable error) {
        if (error == null) {
            return "no webhook returned";
        }
        Throwable cause = error;
        if (error instanceof CompletionException) {
            Throwable nested = error.getCause();
            if (nested != null) {
                cause = nested;
            }
        }
        String message = cause.getMessage();
        return message != null && !message.isBlank() ? message : cause.toString();
    }
}
