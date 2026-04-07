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
package org.openhab.io.openhabcloud;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Service interface for requesting webhook URLs from the openHAB Cloud.
 *
 * Other bindings can consume this service via OSGi {@code @Reference} to obtain
 * publicly-reachable webhook URLs. When an external service calls the webhook URL,
 * the cloud proxies the request to the specified local path on this openHAB instance.
 *
 * <p>
 * Usage example:
 *
 * <pre>
 * {@code @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC)}
 * protected void setWebhookService(WebhookService service) {
 *     this.webhookService = service;
 * }
 *
 * // In initialize():
 * webhookService.requestWebhook("/myBinding/callback")
 *     .thenAccept(url -> externalApi.registerCallback(url));
 * </pre>
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public interface WebhookService {

    /**
     * Request a webhook URL for the given local path. The cloud service will generate
     * a unique URL like {@code https://myopenhab.org/api/hooks/{uuid}} that proxies
     * incoming requests to the specified local path on this openHAB instance.
     *
     * <p>
     * This method is idempotent: calling it with the same {@code localPath} returns
     * the same webhook URL and refreshes the 30-day TTL.
     *
     * @param localPath the local openHAB path to forward webhook requests to
     *            (e.g., {@code "/rest/webhook/netatmo"})
     * @return a {@link CompletableFuture} that completes with the full webhook URL,
     *         or completes exceptionally if the cloud is not connected or registration fails
     */
    CompletableFuture<String> requestWebhook(String localPath);

    /**
     * Remove a previously registered webhook for the given local path.
     *
     * @param localPath the local openHAB path whose webhook should be removed
     * @return a {@link CompletableFuture} that completes when the webhook is removed,
     *         or completes exceptionally if the cloud is not connected or removal fails
     */
    CompletableFuture<Void> removeWebhook(String localPath);
}
