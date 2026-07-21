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
package org.openhab.binding.smartthings.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.ClientBuilder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.smartthings.internal.SmartThingsAuthService;
import org.openhab.binding.smartthings.internal.SmartThingsBindingConstants;
import org.openhab.binding.smartthings.internal.SmartThingsHandlerFactory;
import org.openhab.binding.smartthings.internal.discovery.SmartThingsDiscoveryService;
import org.openhab.binding.smartthings.internal.type.SmartThingsException;
import org.openhab.binding.smartthings.internal.type.SmartThingsTypeRegistry;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.osgi.framework.BundleContext;
import org.osgi.service.http.HttpService;
import org.osgi.service.jaxrs.client.SseEventSourceFactory;

/**
 * Tests for {@link SmartThingsAccountHandler}.
 */
@NonNullByDefault
class SmartThingsAccountHandlerTest {

    @Test
    void initLocationMarksBridgeOnlineAfterSuccessfulAuthorization() throws SmartThingsException {
        SmartThingsDiscoveryService discoveryService = mock(SmartThingsDiscoveryService.class);
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(), discoveryService);

        handler.initLocation("location-1");

        verify(discoveryService).doScan(true);
        assertEquals(ThingStatus.ONLINE, handler.lastStatus);
    }

    @Test
    void initializeSetsUnknownAndWaitsForStartupDiscoveryBeforeMarkingBridgeOnline() throws Exception {
        OAuthClientService oAuthService = mock(OAuthClientService.class);
        AccessTokenResponse accessTokenResponse = mock(AccessTokenResponse.class);
        when(accessTokenResponse.getAccessToken()).thenReturn("access-token");
        when(oAuthService.getAccessTokenResponse()).thenReturn(accessTokenResponse);

        OAuthFactory oAuthFactory = mock(OAuthFactory.class);
        when(oAuthFactory.createOAuthClientService(anyString(), anyString(), nullable(String.class), anyString(),
                nullable(String.class), nullable(String.class), nullable(Boolean.class))).thenReturn(oAuthService);

        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(), oAuthFactory);

        handler.initialize();

        assertEquals(1, handler.setupClientCalls);
        assertEquals(ThingStatus.UNKNOWN, handler.lastStatus);
    }

    @Test
    void setupClientRunsStartupDiscoveryBeforeMarkingBridgeOnlineWithoutStoredLocations() throws Exception {
        SmartThingsDiscoveryService discoveryService = mock(SmartThingsDiscoveryService.class);
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(), discoveryService);
        handler.runRealSetupClient();
        handler.setHasLocations(false);
        doAnswer(invocation -> {
            handler.markStartupDiscoveryCompleted();
            return null;
        }).when(discoveryService).doScan(false);

        handler.setupClient(null);

        verify(discoveryService, timeout(1000)).doScan(false);
        assertEquals(ThingStatus.ONLINE, handler.awaitStatusUpdate());
        assertFalse(handler.onlineBeforeStartupDiscovery);
        assertEquals(0, handler.registerSubscriptionsCalls);
    }

    @Test
    void emptyCallbackUrlIsFilledFromCloudWebhook() throws Exception {
        WebhookService webhookService = mock(WebhookService.class);
        when(webhookService.requestWebhook("/smartthings/account/cb")).thenReturn(
                CompletableFuture.completedFuture(webhook("https://cloud.example.org/smartthings/account/cb")));
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(), webhookService);

        handler.registerCloudWebhook();

        assertEquals("https://cloud.example.org/smartthings/account/cb",
                handler.updatedConfiguration.get(SmartThingsBindingConstants.CALLBACK_URL));
    }

    @Test
    void customCallbackUrlIsNotReplacedByCloudWebhook() throws Exception {
        WebhookService webhookService = mock(WebhookService.class);
        when(webhookService.requestWebhook("/smartthings/account/cb")).thenReturn(
                CompletableFuture.completedFuture(webhook("https://cloud.example.org/smartthings/account/cb")));
        when(webhookService.removeWebhook("/smartthings/account/cb"))
                .thenReturn(CompletableFuture.<Void> completedFuture(null));
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(
                Map.of(SmartThingsBindingConstants.CALLBACK_URL, "https://openhab.example.org/smartthings/account/cb")),
                webhookService);

        handler.registerCloudWebhook();

        verify(webhookService).removeWebhook("/smartthings/account/cb");
        assertEquals("https://openhab.example.org/smartthings/account/cb",
                handler.getThing().getConfiguration().get(SmartThingsBindingConstants.CALLBACK_URL));
    }

    @Test
    void sameHostCustomCallbackUrlIsNotReplacedByCloudWebhook() throws Exception {
        WebhookService webhookService = mock(WebhookService.class);
        when(webhookService.requestWebhook("/smartthings/account/cb"))
                .thenReturn(CompletableFuture.completedFuture(webhook("https://cloud.example.org/new-callback")));
        when(webhookService.removeWebhook("/smartthings/account/cb"))
                .thenReturn(CompletableFuture.<Void> completedFuture(null));
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(
                accountBridge(
                        Map.of(SmartThingsBindingConstants.CALLBACK_URL, "https://cloud.example.org/old-callback")),
                webhookService);

        handler.registerCloudWebhook();

        verify(webhookService).removeWebhook("/smartthings/account/cb");
        assertEquals("https://cloud.example.org/old-callback",
                handler.getThing().getConfiguration().get(SmartThingsBindingConstants.CALLBACK_URL));
    }

    @Test
    void matchingCloudCallbackUrlKeepsCloudWebhook() throws Exception {
        WebhookService webhookService = mock(WebhookService.class);
        when(webhookService.requestWebhook("/smartthings/account/cb")).thenReturn(
                CompletableFuture.completedFuture(webhook("https://cloud.example.org/smartthings/account/cb")));
        TestSmartThingsAccountHandler handler = new TestSmartThingsAccountHandler(accountBridge(
                Map.of(SmartThingsBindingConstants.CALLBACK_URL, "https://cloud.example.org/smartthings/account/cb")),
                webhookService);

        handler.registerCloudWebhook();

        verify(webhookService, never()).removeWebhook("/smartthings/account/cb");
        assertEquals("https://cloud.example.org/smartthings/account/cb",
                handler.getThing().getConfiguration().get(SmartThingsBindingConstants.CALLBACK_URL));
    }

    private Bridge accountBridge() {
        return accountBridge(Map.of());
    }

    private Bridge accountBridge(Map<String, @Nullable Object> configuration) {
        return BridgeBuilder.create(SmartThingsBindingConstants.THING_TYPE_ACCOUNT, "account")
                .withConfiguration(new Configuration(configuration)).build();
    }

    private static Webhook webhook(String url) throws Exception {
        return new Webhook(URI.create(url).toURL(), Instant.now());
    }

    private static class TestSmartThingsAccountHandler extends SmartThingsAccountHandler {
        private @Nullable ThingStatus lastStatus;
        private Configuration updatedConfiguration = new Configuration();
        private int setupClientCalls;
        private int registerSubscriptionsCalls;
        private final CountDownLatch statusUpdateLatch = new CountDownLatch(1);
        private boolean runRealSetupClient;
        private boolean hasLocations;
        private volatile boolean startupDiscoveryCompleted;
        private volatile boolean onlineBeforeStartupDiscovery;

        TestSmartThingsAccountHandler(Bridge bridge, SmartThingsDiscoveryService discoveryService) {
            this(bridge, discoveryService, null);
        }

        TestSmartThingsAccountHandler(Bridge bridge, @Nullable WebhookService webhookService) {
            this(bridge, mock(SmartThingsDiscoveryService.class), webhookService);
        }

        TestSmartThingsAccountHandler(Bridge bridge, OAuthFactory oAuthFactory) {
            this(bridge, mock(SmartThingsDiscoveryService.class), null, oAuthFactory);
        }

        TestSmartThingsAccountHandler(Bridge bridge, SmartThingsDiscoveryService discoveryService,
                @Nullable WebhookService webhookService) {
            this(bridge, discoveryService, webhookService, mock(OAuthFactory.class));
        }

        TestSmartThingsAccountHandler(Bridge bridge, SmartThingsDiscoveryService discoveryService,
                @Nullable WebhookService webhookService, OAuthFactory oAuthFactory) {
            super(bridge, mock(SmartThingsHandlerFactory.class), mock(SmartThingsAuthService.class),
                    mock(TranslationProvider.class), mock(BundleContext.class), mock(HttpService.class), oAuthFactory,
                    mock(HttpClientFactory.class), mock(SmartThingsTypeRegistry.class), mock(ClientBuilder.class),
                    mock(SseEventSourceFactory.class), webhookService);
            this.discoService = discoveryService;
        }

        @Override
        public void registerSubscriptions() {
            registerSubscriptionsCalls++;
        }

        @Override
        public void registerServlet() throws SmartThingsException {
        }

        @Override
        protected void setupClient(@Nullable String eventCallbackUri) throws SmartThingsException {
            setupClientCalls++;
            if (runRealSetupClient) {
                super.setupClient(eventCallbackUri);
            }
        }

        @Override
        protected void updateLocationProperties(@Nullable String location) {
        }

        @Override
        protected void updateStatus(ThingStatus status) {
            lastStatus = status;
            if (ThingStatus.ONLINE.equals(status)) {
                onlineBeforeStartupDiscovery = !startupDiscoveryCompleted;
            }
            statusUpdateLatch.countDown();
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail statusDetail, @Nullable String description) {
            lastStatus = status;
            statusUpdateLatch.countDown();
        }

        @Override
        protected void updateConfiguration(Configuration configuration) {
            updatedConfiguration = configuration;
            getThing().getConfiguration().setProperties(configuration.getProperties());
        }

        @Override
        public boolean hasLocations() {
            return hasLocations;
        }

        @Override
        public void initRegistry() {
        }

        private void runRealSetupClient() {
            runRealSetupClient = true;
        }

        private void setHasLocations(boolean hasLocations) {
            this.hasLocations = hasLocations;
        }

        private void markStartupDiscoveryCompleted() {
            startupDiscoveryCompleted = true;
        }

        private @Nullable ThingStatus awaitStatusUpdate() throws InterruptedException {
            statusUpdateLatch.await(1, TimeUnit.SECONDS);
            return lastStatus;
        }
    }
}
