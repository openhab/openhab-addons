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
 *
 * @author Sarvesh Patil - Initial contribution
 */
@Component(service = PlivoCloudWebhookService.class)
@NonNullByDefault
public class PlivoCloudWebhookService {

    private final Logger logger = LoggerFactory.getLogger(PlivoCloudWebhookService.class);

    private static final long REFRESH_INTERVAL_HOURS = 24;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(BINDING_ID + "-cloud-webhook");
    private final Object lock = new Object();
    private final Set<String> requestors = new HashSet<>();
    private final Set<Runnable> availabilityListeners = ConcurrentHashMap.newKeySet();

    private volatile @Nullable WebhookService webhookService;
    private @Nullable String baseUrl;
    private @Nullable ScheduledFuture<?> refreshTask;

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)
    void setWebhookService(WebhookService service) {
        boolean retry;
        synchronized (lock) {
            this.webhookService = service;
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
                ScheduledFuture<?> task = refreshTask;
                if (task != null) {
                    task.cancel(false);
                    refreshTask = null;
                }
            }
        }
    }

    @Deactivate
    public void deactivate() {
        synchronized (lock) {
            ScheduledFuture<?> task = refreshTask;
            if (task != null) {
                task.cancel(true);
                refreshTask = null;
            }
            requestors.clear();
            baseUrl = null;
        }
    }

    /**
     * Registers the cloud webhook on behalf of the given requestor (typically a Thing UID). If the
     * {@link WebhookService} is available, this contacts it synchronously and returns the base URL.
     * If the service is not yet available, the request is remembered and registration will be
     * retried automatically once the service binds.
     *
     * @param requestorId an identifier (typically a Thing UID) so the service can ref-count
     *            registrations across multiple Things
     * @return the cloud webhook base URL, or {@code null} if the {@link WebhookService} is not
     *         (yet) available
     */
    public @Nullable String register(String requestorId) {
        synchronized (lock) {
            requestors.add(requestorId);
        }
        return doRegister();
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
            ScheduledFuture<?> task = refreshTask;
            if (task != null) {
                task.cancel(false);
                refreshTask = null;
            }
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

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private @Nullable String doRegister() {
        WebhookService requestService;
        synchronized (lock) {
            String current = baseUrl;
            if (current != null) {
                return current;
            }
            WebhookService ws = webhookService;
            if (ws == null || requestors.isEmpty()) {
                return null;
            }
            // Capture the service this registration is made through, so a replacement that binds
            // while the request is in flight cannot be mistaken for the one that created the result.
            requestService = ws;
        }
        String url = fetchWebhookUrl(requestService);
        if (url == null) {
            return null;
        }
        boolean newlyRegistered = false;
        synchronized (lock) {
            String current = baseUrl;
            if (current != null) {
                // Another thread registered the same path while this request was in flight. Both
                // asked the service for SERVLET_PATH, so there is nothing orphaned to clean up.
                return current;
            }
            if (webhookService == requestService && !requestors.isEmpty()) {
                baseUrl = url;
                newlyRegistered = true;
                logger.debug("Cloud webhook base URL: {}", url);
                if (refreshTask == null) {
                    refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, REFRESH_INTERVAL_HOURS,
                            REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
                }
            }
        }
        if (newlyRegistered) {
            notifyAvailabilityListeners();
            return url;
        }
        // The last requestor unregistered, or the service was replaced, while the request was in
        // flight. Publishing now would resurrect a registration nobody wants, so drop it through the
        // same service that created it.
        removeWebhook(requestService, "no longer required after registration");
        return null;
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

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    private void refresh() {
        WebhookService requestService;
        synchronized (lock) {
            WebhookService ws = webhookService;
            if (ws == null || requestors.isEmpty() || baseUrl == null) {
                return;
            }
            requestService = ws;
        }
        String url = fetchWebhookUrl(requestService);
        if (url == null) {
            return;
        }
        boolean refreshed = false;
        synchronized (lock) {
            // Only keep the refreshed URL while the registration it belongs to is still live;
            // otherwise an in-flight refresh would restore a URL that unregister() just cleared.
            if (webhookService == requestService && !requestors.isEmpty() && baseUrl != null) {
                baseUrl = url;
                refreshed = true;
                logger.trace("Cloud webhook refreshed: {}", url);
            }
        }
        if (!refreshed) {
            removeWebhook(requestService, "registration torn down during refresh");
        }
    }

    private @Nullable String fetchWebhookUrl(WebhookService service) {
        try {
            Webhook hook = service.requestWebhook(SERVLET_PATH).get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return hook.url().toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.warn("Failed to request an openHAB Cloud webhook for {}: {}", SERVLET_PATH, e.getMessage());
            return null;
        }
    }

    private void removeWebhook(WebhookService service, String reason) {
        try {
            service.removeWebhook(SERVLET_PATH).get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            logger.debug("Cloud webhook removed for {} ({})", SERVLET_PATH, reason);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.debug("Failed to remove cloud webhook for {} ({}): {}", SERVLET_PATH, reason, e.getMessage());
        }
    }
}
