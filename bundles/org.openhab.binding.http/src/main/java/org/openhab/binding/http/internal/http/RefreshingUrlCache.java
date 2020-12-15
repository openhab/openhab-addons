/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.http.internal.http;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Authentication;
import org.eclipse.jetty.client.api.AuthenticationStore;
import org.eclipse.jetty.client.api.Request;
import org.openhab.binding.http.internal.Util;
import org.openhab.binding.http.internal.config.HttpThingConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link RefreshingUrlCache} is responsible for requesting from a single URL and passing the content to the
 * channels
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class RefreshingUrlCache {
    private final Logger logger = LoggerFactory.getLogger(RefreshingUrlCache.class);

    private final String url;
    private final HttpClient httpClient;
    private final int timeout;
    private final int bufferSize;
    private final @Nullable String fallbackEncoding;
    private final Set<Consumer<Content>> consumers = ConcurrentHashMap.newKeySet();
    private final List<String> headers;

    private final ScheduledFuture<?> future;
    private @Nullable Content lastContent;

    public RefreshingUrlCache(ScheduledExecutorService executor, HttpClient httpClient, String url,
            HttpThingConfig thingConfig) {
        this.httpClient = httpClient;
        this.url = url;
        this.timeout = thingConfig.timeout;
        this.bufferSize = thingConfig.bufferSize;
        this.headers = thingConfig.headers;
        fallbackEncoding = thingConfig.encoding;

        future = executor.scheduleWithFixedDelay(this::refresh, 0, thingConfig.refresh, TimeUnit.SECONDS);
        logger.trace("Started refresh task for URL '{}' with interval {}s", url, thingConfig.refresh);
    }

    private void refresh() {
        refresh(false);
    }

    private void refresh(boolean isRetry) {
        if (consumers.isEmpty()) {
            // do not refresh if we don't have listeners
            return;
        }

        // format URL
        try {
            URI finalUrl = new URI(String.format(this.url, new Date()));

            logger.trace("Requesting refresh (retry={}) from '{}' with timeout {}ms", isRetry, finalUrl, timeout);
            Request request = httpClient.newRequest(finalUrl).timeout(timeout, TimeUnit.MILLISECONDS);

            headers.forEach(header -> {
                String[] keyValuePair = header.split("=", 2);
                if (keyValuePair.length == 2) {
                    request.header(keyValuePair[0].trim(), keyValuePair[1].trim());
                } else {
                    logger.warn("Splitting header '{}' failed. No '=' was found. Ignoring", header);
                }
            });

            CompletableFuture<@Nullable Content> response = new CompletableFuture<>();
            response.exceptionally(e -> {
                if (e instanceof HttpAuthException) {
                    if (isRetry) {
                        logger.warn("Retry after authentication  failure failed again for '{}', failing here",
                                finalUrl);
                    } else {
                        AuthenticationStore authStore = httpClient.getAuthenticationStore();
                        Authentication.Result authResult = authStore.findAuthenticationResult(finalUrl);
                        if (authResult != null) {
                            authStore.removeAuthenticationResult(authResult);
                            logger.debug("Cleared authentication result for '{}', retrying immediately", finalUrl);
                            refresh(true);
                        } else {
                            logger.warn("Could not find authentication result for '{}', failing here", finalUrl);
                        }
                    }
                }
                return null;
            }).thenAccept(this::processResult);

            if (logger.isTraceEnabled()) {
                logger.trace("Sending to '{}': {}", finalUrl, Util.requestToLogString(request));
            }

            request.send(new HttpResponseListener(response, fallbackEncoding, bufferSize));
        } catch (IllegalArgumentException | URISyntaxException e) {
            logger.warn("Creating request for '{}' failed: {}", url, e.getMessage());
        }
    }

    public void stop() {
        // clearing all listeners to prevent further updates
        consumers.clear();
        future.cancel(false);
        logger.trace("Stopped refresh task for URL '{}'", url);
    }

    public void addConsumer(Consumer<Content> consumer) {
        consumers.add(consumer);
    }

    public Optional<Content> get() {
        final Content content = lastContent;
        if (content == null) {
            return Optional.empty();
        } else {
            return Optional.of(content);
        }
    }

    private void processResult(@Nullable Content content) {
        if (content != null) {
            for (Consumer<Content> consumer : consumers) {
                try {
                    consumer.accept(content);
                } catch (IllegalArgumentException | IllegalStateException e) {
                    logger.warn("Failed processing result for URL {}: {}", url, e.getMessage());
                }
            }
        }
        lastContent = content;
    }
}
