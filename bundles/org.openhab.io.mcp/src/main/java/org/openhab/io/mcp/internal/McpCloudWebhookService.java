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
package org.openhab.io.mcp.internal;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.http.HttpMethod;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.io.mcp.internal.tools.McpToolUtils;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Registers the MCP servlet path with the openHAB Cloud {@code WebhookService},
 * exposing it publicly at {@code https://<cloudhost>/api/hooks/<uuid>/...} so remote
 * MCP clients can reach this openHAB instance without port forwarding or a reverse
 * proxy.
 * <p>
 * The openHAB Cloud {@code WebhookService} is looked up via reflection so this bundle
 * has no compile-time or class-loading dependency on the cloud add-on; if the cloud
 * add-on isn't installed, registration is silently skipped.
 * <p>
 * Registrations have a 30-day TTL on the cloud side; a scheduled daily refresh keeps
 * the mapping alive as long as the MCP bundle is active.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class McpCloudWebhookService {

    private static final long REFRESH_INTERVAL_HOURS = 24;
    private static final String WEBHOOK_SERVICE_CLASS = "org.openhab.io.openhabcloud.WebhookService";
    private static final int REQUEST_TIMEOUT_SECONDS = 30;
    private static final String PROXY_URL_PATH = "/api/v1/proxyurl";

    private final Logger logger = LoggerFactory.getLogger(McpCloudWebhookService.class);
    private final ObjectMapper jackson = McpToolUtils.jackson();

    private final BundleContext bundleContext;
    private final String localPath;
    private final HttpClient httpClient;
    private final ScheduledExecutorService scheduler;
    private final Object lock = new Object();

    private volatile @Nullable String publicUrl;
    private volatile @Nullable String browserBaseUrl;
    private @Nullable ScheduledFuture<?> refreshTask;

    public McpCloudWebhookService(BundleContext bundleContext, String localPath, HttpClient httpClient) {
        this.bundleContext = bundleContext;
        this.localPath = localPath;
        this.httpClient = httpClient;
        this.scheduler = ThreadPoolManager.getScheduledPool("mcp-cloud-webhook");
    }

    /**
     * Registers the webhook with openHAB Cloud. Starts a daily refresh task the first
     * time a URL comes back.
     *
     * @return {@code true} if the path was successfully registered.
     */
    public boolean register() {
        synchronized (lock) {
            String url = fetchWebhookUrl();
            if (url == null) {
                logger.debug("MCP cloud webhook for {} unavailable (openHAB Cloud service not connected?)", localPath);
                return false;
            }
            publicUrl = url;
            browserBaseUrl = fetchBrowserBaseUrl(url);
            logger.debug("MCP cloud webhook registered: {} -> {} (browser: {})", localPath, url, browserBaseUrl);
            if (refreshTask == null) {
                refreshTask = scheduler.scheduleWithFixedDelay(this::refresh, REFRESH_INTERVAL_HOURS,
                        REFRESH_INTERVAL_HOURS, TimeUnit.HOURS);
            }
            return true;
        }
    }

    /**
     * Unregisters the webhook and cancels the refresh task. Only call this when the
     * feature is being explicitly turned off by the user — not on normal bundle /
     * service lifecycle transitions, because removing a hook causes the Cloud to mint
     * a new UUID on the next registration, which breaks any client that saved the
     * previous URL.
     */
    public void unregister() {
        synchronized (lock) {
            stopRefresh();
            withWebhookService(ws -> {
                invokeRemoveWebhook(ws);
                return null;
            });
            publicUrl = null;
            browserBaseUrl = null;
        }
    }

    /**
     * Cancels the daily refresh task without removing the hook. Used on normal
     * service shutdown so the Cloud-side registration persists (same UUID on next
     * startup). The Cloud's 30-day TTL will eventually expire abandoned hooks.
     */
    public void stopRefresh() {
        synchronized (lock) {
            ScheduledFuture<?> task = refreshTask;
            if (task != null) {
                task.cancel(true);
                refreshTask = null;
            }
        }
    }

    /**
     * @return the public hook URL, or {@code null} if no hook is registered.
     */
    public @Nullable String getPublicUrl() {
        return publicUrl;
    }

    /**
     * Browser-facing URL that the user's browser is redirected to for OAuth consent.
     * Sourced from the Cloud service's {@code /api/v1/proxyurl} endpoint
     * ({@code browserUrl} field), so the Cloud operator controls the host. If the
     * value wasn't cached at registration time (Cloud service not up yet, transient
     * failure, …) we retry on demand so the first OAuth metadata request after the
     * Cloud comes back online can still populate it.
     */
    public @Nullable String deriveBrowserBaseUrl() {
        String cached = browserBaseUrl;
        if (cached != null) {
            return cached;
        }
        String hookUrl = publicUrl;
        if (hookUrl == null) {
            return null;
        }
        String fresh = fetchBrowserBaseUrl(hookUrl);
        if (fresh != null) {
            browserBaseUrl = fresh;
        }
        return fresh;
    }

    private void refresh() {
        String url = fetchWebhookUrl();
        if (url != null) {
            publicUrl = url;
            browserBaseUrl = fetchBrowserBaseUrl(url);
            logger.trace("MCP cloud webhook refreshed: {} -> {} (browser: {})", localPath, url, browserBaseUrl);
        }
    }

    /**
     * Queries {@code <cloudbase>/api/v1/proxyurl} for the browser-facing URL. The
     * cloud base is derived from the hook URL's scheme+host+port.
     */
    private @Nullable String fetchBrowserBaseUrl(String hookUrl) {
        try {
            URI uri = URI.create(hookUrl);
            String scheme = uri.getScheme();
            String host = uri.getHost();
            if (scheme == null || host == null) {
                return null;
            }
            int port = uri.getPort();
            String base = scheme + "://" + host + (port > 0 ? ":" + port : "");
            ContentResponse resp = httpClient.newRequest(base + PROXY_URL_PATH).method(HttpMethod.GET)
                    .timeout(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS).send();
            if (resp.getStatus() != 200) {
                logger.debug("Cloud {} returned {} {}", PROXY_URL_PATH, resp.getStatus(), resp.getReason());
                return null;
            }
            JsonNode body = jackson.readTree(resp.getContentAsString());
            JsonNode browserUrl = body.get("browserUrl");
            return browserUrl != null && browserUrl.isTextual() ? browserUrl.asText() : null;
        } catch (Exception e) {
            logger.debug("Failed to fetch browser URL from cloud {}: {}", PROXY_URL_PATH, e.getMessage());
            return null;
        }
    }

    private @Nullable String fetchWebhookUrl() {
        return withWebhookService(this::invokeRequestWebhook);
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

    private @Nullable String invokeRequestWebhook(Object webhookService) {
        try {
            Method method = webhookService.getClass().getMethod("requestWebhook", String.class);
            Object result = method.invoke(webhookService, localPath);
            if (!(result instanceof CompletableFuture<?> future)) {
                logger.debug("requestWebhook returned unexpected type: {}",
                        result != null ? result.getClass() : "null");
                return null;
            }
            Object value = future.get(REQUEST_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            return value instanceof String s ? s : null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            logger.debug("Failed to request cloud webhook for {}: {}", localPath, e.getMessage());
            return null;
        }
    }

    private void invokeRemoveWebhook(Object webhookService) {
        try {
            Method method = webhookService.getClass().getMethod("removeWebhook", String.class);
            method.invoke(webhookService, localPath);
        } catch (Exception e) {
            logger.debug("Failed to remove cloud webhook for {}: {}", localPath, e.getMessage());
        }
    }
}
