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

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.ThreadPoolManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the binding-wide openHAB Cloud webhook registration used for receiving Twilio callbacks.
 * <p>
 * All Twilio phone Things share one cloud webhook mapping at
 * {@link org.openhab.binding.twilio.internal.TwilioBindingConstants#SERVLET_PATH}. The first call
 * to {@link #register()} registers the mapping with the openHAB Cloud {@code WebhookService}
 * and starts a daily refresh task to keep the 30-day TTL from expiring. The mapping is removed
 * when the binding is deactivated.
 * <p>
 * The openHAB Cloud {@code WebhookService} is looked up via reflection so this binding has no
 * compile-time or class-loading dependency on the cloud add-on.
 *
 * @author Dan Cunningham - Initial contribution
 */
@Component(service = TwilioCloudWebhookService.class)
@NonNullByDefault
public class TwilioCloudWebhookService {

    private final Logger logger = LoggerFactory.getLogger(TwilioCloudWebhookService.class);

    private static final long REFRESH_INTERVAL_HOURS = 24;
    private static final String WEBHOOK_SERVICE_CLASS = "org.openhab.io.openhabcloud.WebhookService";
    private static final int REQUEST_TIMEOUT_SECONDS = 30;

    private final BundleContext bundleContext;
    private final ScheduledExecutorService scheduler;
    private final Object lock = new Object();

    private @Nullable String baseUrl;
    private @Nullable ScheduledFuture<?> refreshTask;

    @Activate
    public TwilioCloudWebhookService(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
        this.scheduler = ThreadPoolManager.getScheduledPool(BINDING_ID + "-cloud-webhook");
    }

    @Deactivate
    public void deactivate() {
        synchronized (lock) {
            ScheduledFuture<?> task = refreshTask;
            if (task != null) {
                task.cancel(true);
                refreshTask = null;
            }
            if (baseUrl != null) {
                withWebhookService(ws -> {
                    invokeRemoveWebhook(ws, SERVLET_PATH);
                    return null;
                });
                baseUrl = null;
            }
        }
    }

    /**
     * Ensures the cloud webhook is registered, returning the base URL. On the first call this
     * contacts the openHAB Cloud service and starts a daily refresh. Subsequent calls return the
     * cached URL immediately.
     *
     * @return the cloud webhook base URL, or {@code null} if the openHAB Cloud service is not
     *         available
     */
    public @Nullable String register() {
        synchronized (lock) {
            String baseUrl = fetchWebhookUrl();
            if (baseUrl != null) {
                this.baseUrl = baseUrl;
                logger.debug("Cloud webhook base URL: {}", baseUrl);
            } else {
                logger.debug("Cloud webhook requested but openHAB Cloud WebhookService is not available");
            }
            if (baseUrl != null && refreshTask == null) {
                refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, REFRESH_INTERVAL_HOURS,
                        REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
            }
            return baseUrl;
        }
    }

    /**
     * @return the current cloud webhook base URL, or {@code null} if not yet registered.
     */
    public @Nullable String getBaseUrl() {
        return baseUrl;
    }

    private void refresh() {
        String url = fetchWebhookUrl();
        if (url != null) {
            baseUrl = url;
            logger.trace("Cloud webhook refreshed: {}", url);
        }
    }

    // the following methods are bit complicated to get around not having a direct java dependency on the cloud binding

    private @Nullable String fetchWebhookUrl() {
        return withWebhookService(ws -> invokeRequestWebhook(ws, SERVLET_PATH));
    }

    private <T> @Nullable T withWebhookService(Function<Object, @Nullable T> action) {
        try {
            ServiceReference<?>[] refs = bundleContext.getAllServiceReferences(WEBHOOK_SERVICE_CLASS, null);
            if (refs == null || refs.length == 0) {
                return null;
            }
            ServiceReference<?> ref = refs[0];
            Object service = bundleContext.getService(ref);
            if (service == null) {
                return null;
            }
            try {
                return action.apply(service);
            } finally {
                bundleContext.ungetService(ref);
            }
        } catch (Exception e) {
            logger.debug("Could not look up openHAB Cloud WebhookService: {}", e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private @Nullable String invokeRequestWebhook(Object webhookService, String localPath) {
        try {
            Method method = webhookService.getClass().getMethod("requestWebhook", String.class);
            CompletableFuture<String> future = (CompletableFuture<String>) method.invoke(webhookService, localPath);
            return future != null ? future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS) : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.debug("Failed to request cloud webhook for {}: {}", localPath, e.getMessage());
            return null;
        }
    }

    private void invokeRemoveWebhook(Object webhookService, String localPath) {
        try {
            Method method = webhookService.getClass().getMethod("removeWebhook", String.class);
            method.invoke(webhookService, localPath);
        } catch (Exception e) {
            logger.debug("Failed to remove cloud webhook for {}: {}", localPath, e.getMessage());
        }
    }
}
