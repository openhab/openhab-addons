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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.SERVLET_WEBHOOK_PATH;

import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookException;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;

/**
 * Tests the object-based openHAB Cloud webhook registry adapter.
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioCloudWebhookRegistryTest {
    private static final String CLOUD_WEBHOOK_URL = "https://cloud.example.org/rachio/webhook";
    private static final Instant CLOUD_WEBHOOK_EXPIRATION = Instant.parse("2026-06-28T12:00:00Z");

    @Test
    void unavailableServiceReportsDiagnosticCause() {
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> null);

        CloudWebhookException error = assertThrows(CloudWebhookException.class, () -> registry.acquire("bridge"));

        assertThat(error.diagnosticCause(), is("WebhookServiceUnavailable"));
    }

    @Test
    void incompatibleServiceObjectReportsFailureWithoutLinkageError() {
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(Object::new);

        CloudWebhookException error = assertThrows(CloudWebhookException.class, () -> registry.acquire("bridge"));

        assertThat(error.diagnosticCause(), is("NoSuchMethodException"));
    }

    @Test
    void objectBasedWebhookProviderAcquiresCachesAndReleasesLease() throws Exception {
        FakeWebhookProvider webhookProvider = new FakeWebhookProvider();
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> webhookProvider);

        CloudWebhookLease firstLease = registry.acquire("first");
        CloudWebhookLease secondLease = registry.acquire("second");

        assertThat(firstLease.url(), is(CLOUD_WEBHOOK_URL));
        assertThat(firstLease.expiresAt(), is(CLOUD_WEBHOOK_EXPIRATION));
        assertThat(secondLease.url(), is(CLOUD_WEBHOOK_URL));
        assertThat(secondLease.generation(), is(firstLease.generation()));
        assertThat(webhookProvider.requestCount, is(1));
        assertThat(webhookProvider.lastRequestedPath, is(SERVLET_WEBHOOK_PATH));

        registry.release("first");

        assertThat(webhookProvider.removeCount, is(0));

        registry.release("second");

        assertThat(webhookProvider.removeCount, is(1));
        assertThat(webhookProvider.lastRemovedPath, is(SERVLET_WEBHOOK_PATH));
    }

    private static final class FakeWebhookProvider {
        private int requestCount;
        private int removeCount;
        private String lastRequestedPath = "";
        private String lastRemovedPath = "";

        public CompletableFuture<FakeWebhook> requestWebhook(String path) throws Exception {
            requestCount++;
            lastRequestedPath = path;
            return CompletableFuture.completedFuture(new FakeWebhook(URI.create(CLOUD_WEBHOOK_URL).toURL()));
        }

        public CompletableFuture<Void> removeWebhook(String path) {
            removeCount++;
            lastRemovedPath = path;
            return CompletableFuture.completedFuture(null);
        }
    }

    private record FakeWebhook(URL url) {
        public Instant expiresAt() {
            return CLOUD_WEBHOOK_EXPIRATION;
        }
    }
}
