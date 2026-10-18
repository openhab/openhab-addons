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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;

/**
 * Unit tests for {@link PlivoCloudWebhookService}.
 * <p>
 * These focus on the asynchronous registration lifecycle. {@code requestWebhook()} can stay pending
 * for well over a minute while openHAB Cloud reconnects, so the service must never stop listening
 * to a request it started, and must retry a failed request for as long as a requestor still wants
 * the webhook.
 *
 * @author Sarvesh Patil - Initial contribution
 */
@NonNullByDefault
public class PlivoCloudWebhookServiceTest {

    private static final String REQUESTOR = "plivo:account:test";
    private static final String CLOUD_URL = "https://cloud.example.org/plivo/callback";
    private static final String OTHER_CLOUD_URL = "https://cloud.example.org/plivo/callback-2";

    private @NonNullByDefault({}) ImmediateScheduler scheduler;
    private @NonNullByDefault({}) FakeWebhookService webhookService;
    private @NonNullByDefault({}) PlivoCloudWebhookService service;
    private @NonNullByDefault({}) AtomicInteger availabilityNotifications;

    @BeforeEach
    public void setUp() {
        scheduler = new ImmediateScheduler();
        webhookService = new FakeWebhookService();
        service = new PlivoCloudWebhookService(scheduler);
        availabilityNotifications = new AtomicInteger();
        service.addAvailabilityListener(availabilityNotifications::incrementAndGet);
    }

    @AfterEach
    public void tearDown() {
        service.deactivate();
        scheduler.shutdownNow();
    }

    /**
     * The registration request outlives the 30 second window the service used to wait for. A result
     * arriving later must still be published, rather than dropped because nobody is listening.
     */
    @Test
    public void lateSuccessIsStillPublished() {
        CompletableFuture<Webhook> pending = new CompletableFuture<>();
        webhookService.enqueue(pending);
        service.setWebhookService(webhookService);

        service.register(REQUESTOR);

        assertNull(service.getBaseUrl(), "no URL should be published while the request is pending");
        assertEquals(0, availabilityNotifications.get(), "listeners must not fire before a URL exists");

        pending.complete(webhook(CLOUD_URL));

        assertEquals(CLOUD_URL, service.getBaseUrl(), "a late result must still be published");
        assertEquals(1, availabilityNotifications.get(), "listeners must be notified exactly once");
    }

    /**
     * The scenario from the review: the first attempt times out, and a later attempt succeeds. The
     * timeout must not be terminal, because the refresh task only starts once a registration
     * succeeds and would therefore never retry on its own.
     */
    @Test
    public void initialTimeoutIsRetriedUntilRegistrationSucceeds() {
        webhookService.enqueue(failedFuture(new TimeoutException("Webhook registration timed out")));
        webhookService.enqueue(CompletableFuture.completedFuture(webhook(CLOUD_URL)));
        service.setWebhookService(webhookService);

        service.register(REQUESTOR);

        // The listener fires after the URL is published, so waiting on it covers both.
        waitUntil(() -> availabilityNotifications.get() > 0, "the retry after the timeout should register the webhook");
        assertEquals(CLOUD_URL, service.getBaseUrl());
        assertEquals(2, webhookService.requestCount(), "the failed attempt should have been retried exactly once");
        assertEquals(1, availabilityNotifications.get(), "listeners must be notified once the retry succeeds");
    }

    /**
     * A failure is only worth retrying while somebody still wants the webhook.
     */
    @Test
    public void failureIsNotRetriedWhenNoRequestorRemains() {
        CompletableFuture<Webhook> pending = new CompletableFuture<>();
        webhookService.enqueue(pending);
        service.setWebhookService(webhookService);
        service.register(REQUESTOR);

        service.unregister(REQUESTOR);
        pending.completeExceptionally(new IOException("cloud connector is not initialized"));

        assertNull(service.getBaseUrl());
        assertEquals(1, webhookService.requestCount(), "an abandoned registration must not be retried");
    }

    /**
     * If the last requestor leaves while a request is in flight, a URL that arrives afterwards would
     * resurrect a registration nobody wants, so it has to be handed back.
     */
    @Test
    public void urlArrivingAfterUnregisterIsRemovedAgain() {
        CompletableFuture<Webhook> pending = new CompletableFuture<>();
        webhookService.enqueue(pending);
        service.setWebhookService(webhookService);
        service.register(REQUESTOR);

        service.unregister(REQUESTOR);
        pending.complete(webhook(CLOUD_URL));

        assertNull(service.getBaseUrl(), "an unwanted registration must not be published");
        assertEquals(1, webhookService.removeCount(), "the orphaned registration must be removed");
        assertEquals(0, availabilityNotifications.get());
    }

    /**
     * Several accounts share one registration, so concurrent requestors must not each fire their own
     * request at the cloud.
     */
    @Test
    public void concurrentRequestorsShareASingleRequest() {
        CompletableFuture<Webhook> pending = new CompletableFuture<>();
        webhookService.enqueue(pending);
        webhookService.enqueue(CompletableFuture.completedFuture(webhook(OTHER_CLOUD_URL)));
        service.setWebhookService(webhookService);

        service.register(REQUESTOR);
        service.register("plivo:account:second");

        assertEquals(1, webhookService.requestCount(), "a second requestor must join the in-flight request");

        pending.complete(webhook(CLOUD_URL));

        assertEquals(CLOUD_URL, service.getBaseUrl());
        assertEquals(1, availabilityNotifications.get());
    }

    /**
     * Once a URL is known, a further registration must be served from it rather than asking again.
     */
    @Test
    public void registrationIsNotRepeatedOnceAUrlIsKnown() {
        webhookService.enqueue(CompletableFuture.completedFuture(webhook(CLOUD_URL)));
        service.setWebhookService(webhookService);
        service.register(REQUESTOR);
        assertEquals(CLOUD_URL, service.getBaseUrl());

        service.register("plivo:account:second");

        assertEquals(1, webhookService.requestCount(), "an existing registration should be reused");
        assertEquals(1, availabilityNotifications.get());
    }

    /**
     * A {@link WebhookService} that throws instead of returning a failed future must not latch the
     * in-flight guard, or no further registration would ever be attempted.
     */
    @Test
    public void aThrowingServiceDoesNotBlockLaterAttempts() {
        webhookService.enqueueThrow(new IllegalStateException("cloud connector is not initialized"));
        webhookService.enqueue(CompletableFuture.completedFuture(webhook(CLOUD_URL)));
        service.setWebhookService(webhookService);

        service.register(REQUESTOR);

        waitUntil(() -> availabilityNotifications.get() > 0, "a throwing request should still be retried");
        assertEquals(CLOUD_URL, service.getBaseUrl());
        assertEquals(2, webhookService.requestCount());
    }

    /**
     * Stopping the bundle deliberately leaves the cloud registration in place so that restarts do
     * not churn it, so a URL arriving during shutdown must not be handed back.
     */
    @Test
    public void deactivationLeavesTheCloudRegistrationInPlace() {
        CompletableFuture<Webhook> pending = new CompletableFuture<>();
        webhookService.enqueue(pending);
        service.setWebhookService(webhookService);
        service.register(REQUESTOR);

        service.deactivate();
        pending.complete(webhook(CLOUD_URL));

        assertNull(service.getBaseUrl());
        assertEquals(0, webhookService.removeCount(), "stopping the bundle must not remove the cloud webhook");
    }

    private static Webhook webhook(String url) {
        try {
            URL parsed = URI.create(url).toURL();
            return new Webhook(parsed, Instant.now().plus(Duration.ofDays(30)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static CompletableFuture<Webhook> failedFuture(Throwable error) {
        CompletableFuture<Webhook> future = new CompletableFuture<>();
        future.completeExceptionally(error);
        return future;
    }

    private static void waitUntil(BooleanSupplier condition, String message) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            if (condition.getAsBoolean()) {
                return;
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                fail("interrupted while waiting: " + message);
            }
        }
        fail(message);
    }

    /**
     * Runs delayed tasks immediately so the retry backoff does not have to elapse in real time.
     * Fixed-delay tasks (the daily refresh) keep their original schedule so they do not spin.
     */
    @NonNullByDefault({})
    private static class ImmediateScheduler extends ScheduledThreadPoolExecutor {

        ImmediateScheduler() {
            super(1);
        }

        @Override
        public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
            return super.schedule(command, 0, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * A {@link WebhookService} whose responses the test supplies one request at a time.
     */
    private static class FakeWebhookService implements WebhookService {

        private final Queue<Supplier<CompletableFuture<Webhook>>> responses = new ConcurrentLinkedQueue<>();
        private final AtomicInteger requests = new AtomicInteger();
        private final AtomicInteger removals = new AtomicInteger();

        void enqueue(CompletableFuture<Webhook> response) {
            responses.add(() -> response);
        }

        /**
         * Queues a response that throws instead of returning a failed future.
         */
        void enqueueThrow(RuntimeException error) {
            responses.add(() -> {
                throw error;
            });
        }

        @Override
        public CompletableFuture<Webhook> requestWebhook(String localPath) {
            requests.incrementAndGet();
            Supplier<CompletableFuture<Webhook>> next = responses.poll();
            return next != null ? next.get() : new CompletableFuture<>();
        }

        @Override
        public CompletableFuture<Void> removeWebhook(String localPath) {
            removals.incrementAndGet();
            return CompletableFuture.completedFuture(null);
        }

        int requestCount() {
            return requests.get();
        }

        int removeCount() {
            return removals.get();
        }
    }
}
