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
package org.openhab.binding.twilio.internal.service;

import static org.openhab.binding.twilio.internal.TwilioBindingConstants.BINDING_ID;
import static org.openhab.binding.twilio.internal.TwilioBindingConstants.SERVLET_PATH;

import java.util.HashSet;
import java.util.Set;
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
 * Manages the binding-wide openHAB Cloud webhook registration used for receiving Twilio callbacks.
 * <p>
 * All Twilio phone Things share one cloud webhook mapping at
 * {@link org.openhab.binding.twilio.internal.TwilioBindingConstants#SERVLET_PATH}. Each account
 * Thing that wants cloud webhooks calls {@link #register(String)}; the first such call contacts
 * the core {@link WebhookService} and starts a daily refresh task to keep the registration's TTL
 * from expiring. The webhook is only removed from the cloud when an account explicitly calls
 * {@link #unregister(String)} (e.g. when the user turns {@code useCloudWebhook} off in the config)
 * and no other accounts still require it. Bundle stop/restart deliberately does not remove the
 * webhook, so restarts don't churn the cloud registration.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = TwilioCloudWebhookService.class)
@NonNullByDefault
public class TwilioCloudWebhookService {

    private final Logger logger = LoggerFactory.getLogger(TwilioCloudWebhookService.class);

    private static final long REFRESH_INTERVAL_HOURS = 24;
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final ScheduledExecutorService scheduler = ThreadPoolManager
            .getScheduledPool(BINDING_ID + "-cloud-webhook");
    private final Object lock = new Object();
    private final Set<String> requestors = new HashSet<>();

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
            try {
                ws.removeWebhook(SERVLET_PATH).get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                logger.debug("Cloud webhook removed for {}", SERVLET_PATH);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.debug("Failed to remove cloud webhook for {}: {}", SERVLET_PATH, e.getMessage());
            }
        }
    }

    /**
     * @return the current cloud webhook base URL, or {@code null} if not yet registered.
     */
    public @Nullable String getBaseUrl() {
        return baseUrl;
    }

    private @Nullable String doRegister() {
        synchronized (lock) {
            if (baseUrl != null) {
                return baseUrl;
            }
            if (webhookService == null || requestors.isEmpty()) {
                // will (re-)register when the webhookService is set or a new requestor calls in
                return null;
            }
        }
        String url = fetchWebhookUrl();
        if (url == null) {
            return null;
        }
        WebhookService orphanedService = null;
        synchronized (lock) {
            if (baseUrl != null) {
                return baseUrl;
            }
            if (requestors.isEmpty()) {
                // the last requestor unregistered while requestWebhook() was in flight; since
                // unregister() saw baseUrl == null it didn't remove the just-created hook
                orphanedService = webhookService;
            } else {
                baseUrl = url;
                logger.debug("Cloud webhook base URL: {}", url);
                if (refreshTask == null) {
                    refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, REFRESH_INTERVAL_HOURS,
                            REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
                }
                return url;
            }
        }
        if (orphanedService != null) {
            try {
                orphanedService.removeWebhook(SERVLET_PATH).get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                logger.debug("Removed orphaned cloud webhook for {} (no requestors after registration)", SERVLET_PATH);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                logger.debug("Failed to remove orphaned cloud webhook for {}: {}", SERVLET_PATH, e.getMessage());
            }
        }
        return null;
    }

    private void refresh() {
        String url = fetchWebhookUrl();
        if (url != null) {
            baseUrl = url;
            logger.trace("Cloud webhook refreshed: {}", url);
        }
    }

    private @Nullable String fetchWebhookUrl() {
        WebhookService ws = webhookService;
        if (ws == null) {
            return null;
        }
        try {
            Webhook hook = ws.requestWebhook(SERVLET_PATH).get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return hook.url().toString();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.debug("Failed to request cloud webhook for {}: {}", SERVLET_PATH, e.getMessage());
            return null;
        }
    }
}
