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
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.lang.reflect.Field;
import java.net.URI;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudScheduleRule;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookTarget;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookException;
import org.openhab.binding.rachio.internal.handler.RachioCloudWebhookRegistry.CloudWebhookLease;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RateLimitThrottleException;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.rest.Webhook;
import org.openhab.core.io.rest.WebhookService;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * Tests webhook mode selection, registration, lifecycle and event dispatch behavior.
 *
 * @author Kovacs Istvan - Initial contribution
 * @author Leo Siepel - Stabilize webhook lifecycle tests
 * @author Kovacs Istvan - Cloud webhook recovery regression tests
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioWebhookCoordinatorTest {
    private static final HttpClient HTTP_CLIENT = Mockito.mock(HttpClient.class);

    @Test
    void webhookModeSelectionKeepsHoseTimerWebhooksOptIn() {
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true),
                is(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true,
                true, true), is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true,
                true, false), is(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.VALVE, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.VALVE, true, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.VALVE, true, true, true, true),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.VALVE, true, false, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.VALVE, true, true, true, false),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(
                RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true, true, true, true),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, false),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.LIGHTING_CONTROLLER, true, true,
                true, true), is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.LIGHTING_ZONE, true, true, true,
                true), is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.LIGHTING_SCENE, true, true,
                true, true), is(RachioWebhookMode.DISABLED));
        assertThat(RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.LIGHTING_PROGRAM, true, true,
                true, true), is(RachioWebhookMode.DISABLED));
        assertThat(
                RachioWebhookCoordinator.selectWebhookMode(RachioWebhookResourceType.UNKNOWN, true, true, true, true),
                is(RachioWebhookMode.DISABLED));
    }

    @Test
    void manualModernWebhookUrlIsUsedOnlyWhenAutoConfigurationIsEnabled() throws Exception {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT, () -> {
            throw new AssertionError("manual publicWebhookUrl must not request WebhookService");
        });
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(handler.getModernWebhookUrlForRegistration(), is("https://example.org/rachio/webhook"));
        assertThat(bridge.getProperties().containsKey("rachioWebhookUrl"), is(false));
        assertThat(bridge.getProperties().values().stream()
                .noneMatch(value -> value.contains("https://example.org/rachio/webhook")), is(true));
    }

    @Test
    void manualModernWebhookUrlRejectsHttp() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "http://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
    }

    @Test
    void manualModernWebhookUrlRejectsNonWebScheme() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "ftp://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
    }

    @Test
    void invalidManualModernWebhookUrlDoesNotRegisterWebhook() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "http://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);

        verify(api, never()).registerWebHook(Mockito.eq("controller-id"), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
    }

    @Test
    void valveWebhookRegistrationUsesModernUrlWhenHoseTimerWebhooksAreEnabled() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        RachioConfiguration config = modernHoseTimerWebhookConfig();
        config.callbackUrl = "https://legacy.example.org/rachio/webhook";
        config.callbackUsername = "legacy-user";
        config.callbackPassword = "legacy-password";
        setField(handler, "thingConfig", config);
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.registerValveWebHook("valve-id", RequestPurpose.INITIALIZATION);

        verify(api).registerWebHookTarget(targetCaptor.capture(), Mockito.eq("https://example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.INITIALIZATION));
        RachioWebhookTarget target = targetCaptor.getValue();
        assertThat(target.getResourceType(), is(RachioWebhookResourceType.VALVE));
        assertThat(target.getResourceId(), is("valve-id"));
        assertThat(target.getEventTypeList(), is(Arrays.asList(EVENT_VALVE_RUN_START, EVENT_VALVE_RUN_END)));
    }

    @Test
    void valveProgramWebhookRegistrationUsesModernUrlWhenHoseTimerWebhooksAreEnabled() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        setField(handler, "thingConfig", modernHoseTimerWebhookConfig());
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.registerValveProgramWebHook("program-id", RequestPurpose.INITIALIZATION);

        verify(api).registerWebHookTarget(targetCaptor.capture(), Mockito.eq("https://example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.INITIALIZATION));
        RachioWebhookTarget target = targetCaptor.getValue();
        assertThat(target.getResourceType(), is(RachioWebhookResourceType.PROGRAM));
        assertThat(target.getResourceId(), is("program-id"));
        assertThat(target.getEventTypeList(),
                is(Arrays.asList(EVENT_PROGRAM_RAIN_SKIP_CREATED, EVENT_PROGRAM_RAIN_SKIP_CANCELED)));
    }

    @Test
    void disabledHoseTimerWebhookConfigDoesNotRegisterValveOrProgramWebhooks() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        handler.registerValveWebHook("valve-id", RequestPurpose.INITIALIZATION);
        handler.registerValveProgramWebHook("program-id", RequestPurpose.INITIALIZATION);

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
    }

    @Test
    void enablingHoseTimerWebhooksDoesNotRegisterValveOnStaleApiBeforeReinitialization() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        addConfiguredValveHandler(handler, "valve-id");

        handler.handleConfigurationUpdate(modernHoseTimerWebhookParameters(true));

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        handler.initialize();
        assertThat(handler.getWebhookMode(RachioWebhookResourceType.VALVE), is(RachioWebhookMode.WEBHOOK_SERVICE));
    }

    @Test
    void enablingHoseTimerWebhooksDoesNotRegisterProgramOnStaleApiBeforeReinitialization() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        addConfiguredValveProgramHandler(handler, "program-id");

        handler.handleConfigurationUpdate(modernHoseTimerWebhookParameters(true));

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        handler.initialize();
        assertThat(handler.getWebhookMode(RachioWebhookResourceType.PROGRAM), is(RachioWebhookMode.WEBHOOK_SERVICE));
    }

    @Test
    void configurationUpdateDoesNotNotifyChildrenBeforeBridgeReinitialization() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        RachioWebhookMode[] observedValveMode = new RachioWebhookMode[] { RachioWebhookMode.DISABLED };
        RachioStatusListener listener = Mockito.mock(RachioStatusListener.class);
        Mockito.doAnswer(invocation -> {
            observedValveMode[0] = handler.getWebhookMode(RachioWebhookResourceType.VALVE);
            return null;
        }).when(listener).onConfigurationUpdated();
        handler.rachioStatusListeners.add(listener);

        handler.handleConfigurationUpdate(modernHoseTimerWebhookParameters(true));

        verify(listener, never()).onConfigurationUpdated();
        assertThat(observedValveMode[0], is(RachioWebhookMode.DISABLED));
        handler.initialize();
        assertThat(handler.getWebhookMode(RachioWebhookResourceType.VALVE), is(RachioWebhookMode.WEBHOOK_SERVICE));
    }

    @Test
    void disablingHoseTimerWebhooksAtRuntimeDoesNotRegisterValveOrProgramWebhooks() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, modernHoseTimerWebhookParameters(true));
        addConfiguredValveHandler(handler, "valve-id");
        addConfiguredValveProgramHandler(handler, "program-id");

        handler.handleConfigurationUpdate(Map.of(PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS, false));

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
    }

    @Test
    void controllerWebhookReconciliationUsesResolvedConfiguration() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.when(api.getDevices()).thenReturn(devices);
        addConfiguredControllerHandler(handler, "controller-id");

        applyConfigurationAndReconcile(handler, modernHoseTimerWebhookParameters(false), RequestPurpose.INITIALIZATION);

        verify(api).registerWebHook(Mockito.eq("controller-id"), Mockito.eq("https://example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.INITIALIZATION));
    }

    @Test
    void bridgeModernWebhookReconciliationSkipsAccountVisibleUnconfiguredControllers() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, modernHoseTimerWebhookParameters(false));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put("configured-controller-id", controllerDevice("configured-controller-id"));
        devices.put("shared-controller-id", controllerDevice("shared-controller-id"));
        Mockito.when(api.getDevices()).thenReturn(devices);
        addConfiguredControllerHandler(handler, "configured-controller-id");
        Mockito.doThrow(new RachioApiException("PERMISSION_DENIED")).when(api).registerWebHook(
                Mockito.eq("shared-controller-id"), Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.nullable(String.class), Mockito.anyBoolean(), Mockito.any(RequestPurpose.class));

        applyConfigurationAndReconcile(handler, modernHoseTimerWebhookParameters(false), RequestPurpose.INITIALIZATION);

        verify(api).registerWebHook(Mockito.eq("configured-controller-id"),
                Mockito.eq("https://example.org/rachio/webhook"), Mockito.eq(""), Mockito.eq(""), Mockito.isNull(),
                Mockito.eq(false), Mockito.eq(RequestPurpose.INITIALIZATION));
        verify(api, never()).registerWebHook(Mockito.eq("shared-controller-id"), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE), is("registered"));
    }

    @Test
    void bridgeModernWebhookReconciliationWaitsForConfiguredControllerThings() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, modernHoseTimerWebhookParameters(false));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put("account-visible-controller-id", controllerDevice("account-visible-controller-id"));
        Mockito.when(api.getDevices()).thenReturn(devices);

        applyConfigurationAndReconcile(handler, modernHoseTimerWebhookParameters(false), RequestPurpose.INITIALIZATION);

        verify(api, never()).registerWebHook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE),
                is("waiting for configured controller things"));
    }

    @Test
    void firstModernControllerRegistrationFailureMarksRegistrationFailed() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, modernHoseTimerWebhookParameters(false));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put("controller-id", deviceWithZone());
        Mockito.when(api.getDevices()).thenReturn(devices);
        addConfiguredControllerHandler(handler, "controller-id");
        Mockito.doThrow(new RachioApiException("registration failed")).when(api).registerWebHook(
                Mockito.eq("controller-id"), Mockito.eq("https://example.org/rachio/webhook"), Mockito.eq(""),
                Mockito.eq(""), Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.INITIALIZATION));

        applyConfigurationAndReconcile(handler, modernHoseTimerWebhookParameters(false), RequestPurpose.INITIALIZATION);

        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE),
                is("registration failed: RachioApiException"));
    }

    @Test
    void laterModernControllerVerificationFailureKeepsPreviousRegistrationNonFatal() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, modernHoseTimerWebhookParameters(false));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put("controller-id", deviceWithZone());
        Mockito.when(api.getDevices()).thenReturn(devices);
        addConfiguredControllerHandler(handler, "controller-id");

        applyConfigurationAndReconcile(handler, modernHoseTimerWebhookParameters(false), RequestPurpose.INITIALIZATION);

        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE), is("registered"));
        Mockito.doThrow(new RachioApiException("verification failed")).when(api).registerWebHook(
                Mockito.eq("controller-id"), Mockito.eq("https://example.org/rachio/webhook"), Mockito.eq(""),
                Mockito.eq(""), Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.INITIALIZATION));

        applyConfigurationAndReconcile(handler, modernHoseTimerWebhookParameters(false), RequestPurpose.INITIALIZATION);

        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE),
                is("registered; verification deferred"));
        verify(api, Mockito.times(2)).registerWebHook(Mockito.eq("controller-id"),
                Mockito.eq("https://example.org/rachio/webhook"), Mockito.eq(""), Mockito.eq(""), Mockito.isNull(),
                Mockito.eq(false), Mockito.eq(RequestPurpose.INITIALIZATION));
    }

    @Test
    void webhookServiceAvailabilityRegistersExistingValveWebhook() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>();
        RachioBridgeHandler handler = cloudBridgeHandlerWithMockApi(api, webhookService::get, true);
        addConfiguredValveHandler(handler, "valve-id");
        webhookService.set(cloudWebhookService("https://cloud.example.org/rachio/webhook"));
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.onCloudWebhookProviderChanged();

        verify(api, Mockito.timeout(1000)).registerWebHookTarget(targetCaptor.capture(),
                Mockito.eq("https://cloud.example.org/rachio/webhook"), Mockito.eq(""), Mockito.eq(""),
                Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
        RachioWebhookTarget target = targetCaptor.getValue();
        assertThat(target.getResourceType(), is(RachioWebhookResourceType.VALVE));
        assertThat(target.getResourceId(), is("valve-id"));
        handler.dispose();
    }

    @Test
    void webhookServiceAvailabilityRegistersExistingValveProgramWebhook() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>();
        RachioBridgeHandler handler = cloudBridgeHandlerWithMockApi(api, webhookService::get, true);
        addConfiguredValveProgramHandler(handler, "program-id");
        webhookService.set(cloudWebhookService("https://cloud.example.org/rachio/webhook"));
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.onCloudWebhookProviderChanged();

        verify(api, Mockito.timeout(1000)).registerWebHookTarget(targetCaptor.capture(),
                Mockito.eq("https://cloud.example.org/rachio/webhook"), Mockito.eq(""), Mockito.eq(""),
                Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
        RachioWebhookTarget target = targetCaptor.getValue();
        assertThat(target.getResourceType(), is(RachioWebhookResourceType.PROGRAM));
        assertThat(target.getResourceId(), is("program-id"));
        handler.dispose();
    }

    @Test
    void disabledHoseTimerWebhooksDoNotRegisterValveOrProgramWhenWebhookServiceBecomesAvailable() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>();
        RachioBridgeHandler handler = cloudBridgeHandlerWithMockApi(api, webhookService::get, false);
        WebhookService availableWebhookService = cloudWebhookService("https://cloud.example.org/rachio/webhook");
        addConfiguredValveHandler(handler, "valve-id");
        addConfiguredValveProgramHandler(handler, "program-id");
        webhookService.set(availableWebhookService);

        handler.onCloudWebhookProviderChanged();
        verify(availableWebhookService, Mockito.timeout(1000).times(0)).requestWebhook("/rachio/webhook");

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        verify(availableWebhookService, never()).requestWebhook("/rachio/webhook");
        handler.dispose();
    }

    @Test
    void webhookServiceAvailabilityStillReconcilesControllerWebhooks() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>();
        RachioBridgeHandler handler = cloudBridgeHandlerWithMockApi(api, webhookService::get, false);
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.when(api.getDevices()).thenReturn(devices);
        addConfiguredControllerHandler(handler, "controller-id");
        webhookService.set(cloudWebhookService("https://cloud.example.org/rachio/webhook"));

        handler.onCloudWebhookProviderChanged();

        verify(api, Mockito.timeout(1000)).registerWebHook(Mockito.eq("controller-id"),
                Mockito.eq("https://cloud.example.org/rachio/webhook"), Mockito.eq(""), Mockito.eq(""),
                Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
        handler.dispose();
    }

    @Test
    void cloudWebhookServiceUnavailableLeavesModernWebhookUrlEmpty() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT, () -> null);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        setField(handler, "thingConfig", config);

        assertThat(handler.getWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE),
                is("cloud WebhookService unavailable"));
    }

    @Test
    void cloudWebhookServiceUnavailableDoesNotFailWebhookReconciliation() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        HashMap<String, RachioDevice> devices = new HashMap<>();
        RachioDevice device = deviceWithZone();
        device.macAddress = "AABBCCDDEEFF";
        devices.put(device.id, device);
        Mockito.when(api.getPersonId()).thenReturn("person-id");
        Mockito.when(api.fillProperties()).thenReturn(new HashMap<>());
        Mockito.when(api.getDevices()).thenReturn(devices);
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").withConfiguration(new Configuration(
                Map.of(PARAM_APIKEY, "api-key", PARAM_AUTO_CONFIGURE_WEBHOOKS, true, PARAM_USE_CLOUD_WEBHOOK, true)))
                .build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT, () -> null);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        setField(handler, "rachioApi", api);

        setField(handler, "thingConfig",
                RachioConfiguration.resolveEffectiveConfig(bridge.getConfiguration().getProperties()).configuration());
        assertDoesNotThrow(() -> invokeConfiguredWebhookReconciliation(handler, RequestPurpose.INITIALIZATION));

        verify(api, never()).registerWebHook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE),
                is("waiting for configured controller things"));
        handler.dispose();
    }

    @Test
    void cloudWebhookServiceUnavailableUsesLegacyCallbackWhenConfigured() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT, () -> null);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        config.callbackUrl = "https://legacy.example.org/rachio/webhook";
        setField(handler, "thingConfig", config);
        setField(handler, "rachioApi", api);

        handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);

        verify(api).registerLegacyNotificationWebHook(Mockito.eq("controller-id"),
                Mockito.eq("https://legacy.example.org/rachio/webhook"), Mockito.eq(""), Mockito.eq(""),
                Mockito.nullable(String.class), Mockito.eq(false), Mockito.eq(RequestPurpose.INITIALIZATION));
        verify(api, never()).registerWebHook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                Mockito.any(RequestPurpose.class));
        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_MODE), is("legacy"));
        assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE), is("registered"));
    }

    @Test
    void cloudWebhookServiceProvidesModernWebhookUrl() throws Exception {
        String cloudWebhookUrl = "https://cloud.example.org/rachio/webhook";
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook")).thenReturn(CompletableFuture.completedFuture(
                new Webhook(URI.create(cloudWebhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT, () -> webhookService);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        config.publicWebhookUrl = "";
        setField(handler, "thingConfig", config);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(cloudWebhookUrl));
        assertThat(bridge.getProperties().containsKey("rachioWebhookUrl"), is(false));
        assertThat(bridge.getProperties().values().stream().noneMatch(value -> value.contains(cloudWebhookUrl)),
                is(true));

        verify(webhookService).requestWebhook("/rachio/webhook");
        handler.dispose();
    }

    @Test
    void sharedCloudWebhookUrlIsRemovedOnlyAfterLastActiveBridgeDisposes() throws Exception {
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture
                        .completedFuture(new Webhook(URI.create("https://cloud.example.org/rachio/webhook").toURL(),
                                Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> webhookService);
        RachioBridgeHandler firstHandler = cloudWebhookHandler("first", registry);
        RachioBridgeHandler secondHandler = cloudWebhookHandler("second", registry);

        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/webhook"));
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/webhook"));
        assertThat(registry.activeConsumerCount(), is(2));
        verify(webhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");

        firstHandler.dispose();

        assertThat(registry.activeConsumerCount(), is(1));
        verify(webhookService, never()).removeWebhook("/rachio/webhook");

        secondHandler.dispose();

        assertThat(registry.activeConsumerCount(), is(0));
        verify(webhookService, Mockito.times(1)).removeWebhook("/rachio/webhook");
    }

    @Test
    void disablingCloudWebhookOnOneBridgeDoesNotRemoveSharedUrlForAnotherActiveBridge() throws Exception {
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture
                        .completedFuture(new Webhook(URI.create("https://cloud.example.org/rachio/webhook").toURL(),
                                Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> webhookService);
        RachioBridgeHandler firstHandler = cloudWebhookHandler("first", registry);
        RachioBridgeHandler secondHandler = cloudWebhookHandler("second", registry);

        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/webhook"));
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/webhook"));

        firstHandler.handleConfigurationUpdate(
                Map.of(PARAM_AUTO_CONFIGURE_WEBHOOKS, false, PARAM_USE_CLOUD_WEBHOOK, false));
        firstHandler.initialize();

        assertThat(registry.activeConsumerCount(), is(1));
        verify(webhookService, never()).removeWebhook("/rachio/webhook");

        secondHandler.dispose();

        assertThat(registry.activeConsumerCount(), is(0));
        verify(webhookService, Mockito.times(1)).removeWebhook("/rachio/webhook");
    }

    @Test
    void sharedCloudWebhookRecoversAfterServiceUnavailabilityAndIdentityChange() throws Exception {
        WebhookService firstWebhookService = cloudWebhookService("https://cloud.example.org/rachio/first");
        WebhookService secondWebhookService = cloudWebhookService("https://cloud.example.org/rachio/second");
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>(firstWebhookService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(webhookService::get);
        RachioBridgeHandler firstHandler = cloudWebhookHandler("first", registry);
        RachioBridgeHandler secondHandler = cloudWebhookHandler("second", registry);

        try {
            assertThat(firstHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/first"));
            assertThat(secondHandler.getModernWebhookUrlForRegistration(),
                    is("https://cloud.example.org/rachio/first"));

            webhookService.set(null);
            registry.onProviderChanged(firstWebhookService, null);
            reconcileCloudWebhookProviderChange(firstHandler, secondHandler);
            assertThat(firstHandler.getModernWebhookUrlForRegistration(), is(""));
            assertThat(secondHandler.getModernWebhookUrlForRegistration(), is(""));

            webhookService.set(secondWebhookService);
            registry.onProviderChanged(null, secondWebhookService);
            reconcileCloudWebhookProviderChange(firstHandler, secondHandler);
            assertThat(firstHandler.getModernWebhookUrlForRegistration(),
                    is("https://cloud.example.org/rachio/second"));
            assertThat(secondHandler.getModernWebhookUrlForRegistration(),
                    is("https://cloud.example.org/rachio/second"));

            verify(firstWebhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
            verify(secondWebhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
        } finally {
            firstHandler.dispose();
            secondHandler.dispose();
        }
    }

    @Test
    void providerReconciliationDoesNotClearWebhookAcquiredAfterItsStateSnapshot() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/second";
        WebhookService webhookService = cloudWebhookService(webhookUrl);
        AtomicReference<@Nullable WebhookService> webhookServiceReference = new AtomicReference<>();
        RachioCloudWebhookRegistry registry = Mockito.spy(new RachioCloudWebhookRegistry(webhookServiceReference::get));
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        Mockito.when(api.getDevices()).thenReturn(new HashMap<>());
        addConfiguredControllerHandler(handler, "controller-id");

        CountDownLatch providerInvalidationStarted = new CountDownLatch(1);
        CountDownLatch allowProviderInvalidation = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            providerInvalidationStarted.countDown();
            assertThat(allowProviderInvalidation.await(10, TimeUnit.SECONDS), is(true));
            invocation.callRealMethod();
            return null;
        }).when(registry).invalidateCachedWebhook(Mockito.anyLong());

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            webhookServiceReference.set(webhookService);
            registry.onProviderChanged(null, webhookService);
            handler.onCloudWebhookProviderChanged();

            assertThat(providerInvalidationStarted.await(5, TimeUnit.SECONDS), is(true));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
            allowProviderInvalidation.countDown();

            verify(api, Mockito.timeout(5000)).registerWebHook(Mockito.eq("controller-id"), Mockito.eq(webhookUrl),
                    Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                    Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
            verify(webhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
            verify(webhookService, never()).removeWebhook("/rachio/webhook");
        } finally {
            allowProviderInvalidation.countDown();
            handler.dispose();
        }
    }

    @Test
    void failedCloudWebhookAcquisitionRetriesAndRegistersController() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Runnable retry = scheduledCloudWebhookRetry(scheduler);

            retry.run();

            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
            verify(api).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
            verify(retryFuture, never()).cancel(Mockito.anyBoolean());
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void failedControllerRegistrationKeepsPendingTargetUntilSuccessfulRetry(boolean throttled) throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        handler.getWebhookConfiguration().callbackUrl = "https://example.org/legacy-fallback";
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");
        RachioApiException failure = throttled ? throttledException(RequestPurpose.BACKGROUND_REFRESH)
                : new RachioApiException("transient registration failure");
        Mockito.doThrow(failure).doNothing().when(api).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                RequestPurpose.BACKGROUND_REFRESH);

        try {
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);
            verify(api).registerLegacyNotificationWebHook("controller-id", "https://example.org/legacy-fallback", "",
                    "", null, false, RequestPurpose.INITIALIZATION);
            assertThat(retries.size(), is(1));
            retries.getFirst().run();

            verify(api).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(2));
            retries.get(1).run();

            verify(api, Mockito.times(2)).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(2));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "valve", "program" })
    void failedSmartHoseRegistrationKeepsPendingTargetUntilSuccessfulRetry(String resource) throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        handler.getWebhookConfiguration().autoConfigureHoseTimerWebhooks = true;
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        if ("valve".equals(resource)) {
            addConfiguredValveHandler(handler, "target-id");
        } else {
            addConfiguredValveProgramHandler(handler, "target-id");
        }
        Mockito.doThrow(new RachioApiException("transient registration failure")).doNothing().when(api)
                .registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.eq(webhookUrl), Mockito.eq(""),
                        Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                        Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));

        try {
            if ("valve".equals(resource)) {
                handler.registerValveWebHook("target-id", RequestPurpose.INITIALIZATION);
            } else {
                handler.registerValveProgramWebHook("target-id", RequestPurpose.INITIALIZATION);
            }
            assertThat(retries.size(), is(1));
            retries.getFirst().run();
            assertThat(retries.size(), is(2));
            retries.get(1).run();

            ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);
            verify(api, Mockito.times(2)).registerWebHookTarget(targetCaptor.capture(), Mockito.eq(webhookUrl),
                    Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                    Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
            assertThat(targetCaptor.getValue().getResourceId(), is("target-id"));
            assertThat(targetCaptor.getValue().getResourceType(),
                    is("valve".equals(resource) ? RachioWebhookResourceType.VALVE : RachioWebhookResourceType.PROGRAM));
            assertThat(retries.size(), is(2));
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void interruptedSharedAcquisitionPreservesInterruptAndRetriesOnlyInterruptedController() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        CompletableFuture<Webhook> webhookResponse = new CompletableFuture<>();
        WebhookService service = cloudWebhookService(webhookUrl);
        CountDownLatch requestStarted = new CountDownLatch(1);
        Mockito.when(service.requestWebhook("/rachio/webhook")).thenAnswer(invocation -> {
            requestStarted.countDown();
            return webhookResponse;
        });
        RachioCloudWebhookRegistry registry = Mockito.spy(new RachioCloudWebhookRegistry(() -> service));
        CountDownLatch acquisitionsStarted = new CountDownLatch(2);
        Mockito.doAnswer(invocation -> {
            acquisitionsStarted.countDown();
            return invocation.callRealMethod();
        }).when(registry).acquire(Mockito.anyString());
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");
        ExecutorService workers = Executors.newFixedThreadPool(2);
        AtomicReference<Thread> interruptedWorker = new AtomicReference<>();
        AtomicReference<Thread> successfulWorker = new AtomicReference<>();

        try {
            Future<Boolean> interruptedRegistration = workers.submit(() -> {
                interruptedWorker.set(Thread.currentThread());
                handler.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
                return Thread.currentThread().isInterrupted();
            });
            assertThat(requestStarted.await(5, TimeUnit.SECONDS), is(true));
            Future<?> successfulRegistration = workers.submit(() -> {
                successfulWorker.set(Thread.currentThread());
                handler.registerWebHook("controller-B", RequestPurpose.INITIALIZATION);
                return null;
            });
            assertThat(acquisitionsStarted.await(5, TimeUnit.SECONDS), is(true));
            assertThat(awaitCloudWebhookAcquisitions(Objects.requireNonNull(interruptedWorker.get()),
                    Objects.requireNonNull(successfulWorker.get())), is(true));
            Objects.requireNonNull(interruptedWorker.get()).interrupt();
            assertThat(interruptedRegistration.get(5, TimeUnit.SECONDS), is(true));
            webhookResponse
                    .complete(new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60)));
            successfulRegistration.get(5, TimeUnit.SECONDS);

            assertThat(retries.size(), is(1));
            retries.getFirst().run();

            verify(api).registerWebHook("controller-A", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(api).registerWebHook("controller-B", webhookUrl, "", "", null, false, RequestPurpose.INITIALIZATION);
            verify(api, Mockito.times(2)).registerWebHook(Mockito.anyString(), Mockito.eq(webhookUrl), Mockito.eq(""),
                    Mockito.eq(""), Mockito.isNull(), Mockito.eq(false), Mockito.any(RequestPurpose.class));
            verify(service).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            webhookResponse.completeExceptionally(new IllegalStateException("test finished"));
            workers.shutdownNow();
            try {
                assertThat(workers.awaitTermination(5, TimeUnit.SECONDS), is(true));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void providerChangeBeforeQueuedReconciliationCannotRestoreOldCallback() throws Exception {
        String oldUrl = "https://old-cloud.example.org/rachio/webhook";
        String currentUrl = "https://current-cloud.example.org/rachio/webhook";
        WebhookService oldService = cloudWebhookService(oldUrl);
        WebhookService currentService = cloudWebhookService(currentUrl);
        AtomicReference<WebhookService> provider = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(provider::get);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        captureCloudWebhookRetries(scheduler);
        Queue<Runnable> queuedReconciliations = new ConcurrentLinkedQueue<>();
        Mockito.doAnswer(invocation -> {
            queuedReconciliations.add(invocation.getArgument(0, Runnable.class));
            return null;
        }).when(scheduler).execute(Mockito.any(Runnable.class));
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        Mockito.when(api.getExternalId()).thenReturn("review-instance");
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");
        CompletableFuture<String> postChangeRegistrationReachedApi = new CompletableFuture<>();
        CountDownLatch allowOldRegistration = new CountDownLatch(1);
        AtomicReference<String> remoteCallback = new AtomicReference<>("");
        Mockito.doAnswer(invocation -> {
            String callback = invocation.getArgument(1);
            RequestPurpose purpose = invocation.getArgument(6);
            if (purpose == RequestPurpose.USER_COMMAND) {
                postChangeRegistrationReachedApi.complete(callback);
                if (oldUrl.equals(callback)) {
                    assertThat(allowOldRegistration.await(5, TimeUnit.SECONDS), is(true));
                }
            }
            remoteCallback.set(callback);
            return null;
        }).when(api).registerWebHook(Mockito.eq("controller-id"), Mockito.anyString(), Mockito.eq(""), Mockito.eq(""),
                Mockito.eq("review-instance"), Mockito.eq(false), Mockito.any(RequestPurpose.class));
        ExecutorService worker = Executors.newSingleThreadExecutor();

        try {
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);
            assertThat(remoteCallback.get(), is(oldUrl));
            provider.set(currentService);
            registry.onProviderChanged(oldService, currentService);
            handler.onCloudWebhookProviderChanged();
            assertThat(queuedReconciliations.isEmpty(), is(false));

            Future<?> postChangeRegistration = worker.submit(() -> {
                try {
                    handler.registerWebHook("controller-id", RequestPurpose.USER_COMMAND);
                } finally {
                    // A fix may defer this registration before it reaches the API instead of using the old URL.
                    postChangeRegistrationReachedApi.complete("deferred before API");
                }
                return null;
            });
            postChangeRegistrationReachedApi.get(5, TimeUnit.SECONDS);
            runQueuedWebhookReconciliations(queuedReconciliations);
            assertThat(remoteCallback.get(), is(currentUrl));

            allowOldRegistration.countDown();
            postChangeRegistration.get(5, TimeUnit.SECONDS);
            runQueuedWebhookReconciliations(queuedReconciliations);

            assertThat(remoteCallback.get(), is(currentUrl));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(currentUrl));
            verify(api, Mockito.atLeastOnce()).registerWebHook(Mockito.eq("controller-id"), Mockito.eq(currentUrl),
                    Mockito.eq(""), Mockito.eq(""), Mockito.eq("review-instance"), Mockito.eq(false),
                    Mockito.any(RequestPurpose.class));
        } finally {
            allowOldRegistration.countDown();
            worker.shutdownNow();
            try {
                assertThat(worker.awaitTermination(5, TimeUnit.SECONDS), is(true));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void staleInFlightRegistrationReconcilesToCurrentProviderCallback() throws Exception {
        String oldUrl = "https://old-cloud.example.org/rachio/webhook";
        String currentUrl = "https://current-cloud.example.org/rachio/webhook";
        WebhookService oldService = cloudWebhookService(oldUrl);
        Mockito.when(oldService.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(oldUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        WebhookService currentService = cloudWebhookService(currentUrl);
        AtomicReference<WebhookService> provider = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(provider::get);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(scheduler).execute(Mockito.any(Runnable.class));
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        Mockito.when(api.getExternalId()).thenReturn("review-instance");
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");
        CountDownLatch oldRegistrationStarted = new CountDownLatch(1);
        CountDownLatch allowOldRegistration = new CountDownLatch(1);
        AtomicReference<String> remoteCallback = new AtomicReference<>("");
        Mockito.doAnswer(invocation -> {
            String callback = invocation.getArgument(1);
            if (oldUrl.equals(callback)) {
                oldRegistrationStarted.countDown();
                assertThat(allowOldRegistration.await(5, TimeUnit.SECONDS), is(true));
            }
            remoteCallback.set(callback);
            return null;
        }).when(api).registerWebHook(Mockito.eq("controller-id"), Mockito.anyString(), Mockito.eq(""), Mockito.eq(""),
                Mockito.eq("review-instance"), Mockito.eq(false), Mockito.any(RequestPurpose.class));
        ExecutorService worker = Executors.newSingleThreadExecutor();

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Future<?> oldRetry = worker.submit(retries.getFirst());
            assertThat(oldRegistrationStarted.await(5, TimeUnit.SECONDS), is(true));

            provider.set(currentService);
            registry.onProviderChanged(oldService, currentService);
            handler.onCloudWebhookProviderChanged();
            assertThat(remoteCallback.get(), is(currentUrl));

            allowOldRegistration.countDown();
            oldRetry.get(5, TimeUnit.SECONDS);

            assertThat(remoteCallback.get(), is(currentUrl));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(currentUrl));
            verify(api, Mockito.atLeastOnce()).registerWebHook("controller-id", currentUrl, "", "", "review-instance",
                    false, RequestPurpose.BACKGROUND_REFRESH);
            verify(currentService).requestWebhook("/rachio/webhook");
            verify(currentService, never()).removeWebhook("/rachio/webhook");
        } finally {
            allowOldRegistration.countDown();
            worker.shutdownNow();
            try {
                assertThat(worker.awaitTermination(5, TimeUnit.SECONDS), is(true));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void staleCloudRegistrationAfterReinitializationReconcilesToCurrentManualCallback() throws Exception {
        String oldUrl = "https://old-cloud.example.org/rachio/webhook";
        String manualUrl = "https://manual.example.org/rachio/webhook";
        WebhookService service = cloudWebhookService(oldUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(oldUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(scheduler).execute(Mockito.any(Runnable.class));
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi oldApi = Mockito.mock(RachioApi.class);
        RachioApi currentApi = Mockito.mock(RachioApi.class);
        Mockito.when(oldApi.getExternalId()).thenReturn("review-instance");
        Mockito.when(currentApi.getExternalId()).thenReturn("review-instance");
        setField(handler, "rachioApi", oldApi);
        addConfiguredControllerHandler(handler, "controller-id");
        CountDownLatch oldRegistrationStarted = new CountDownLatch(1);
        CountDownLatch allowOldRegistration = new CountDownLatch(1);
        AtomicReference<String> remoteCallback = new AtomicReference<>("");
        Mockito.doAnswer(invocation -> {
            oldRegistrationStarted.countDown();
            assertThat(allowOldRegistration.await(5, TimeUnit.SECONDS), is(true));
            remoteCallback.set(invocation.getArgument(1));
            return null;
        }).when(oldApi).registerWebHook("controller-id", oldUrl, "", "", "review-instance", false,
                RequestPurpose.BACKGROUND_REFRESH);
        Mockito.doAnswer(invocation -> {
            remoteCallback.set(invocation.getArgument(1));
            return null;
        }).when(currentApi).registerWebHook(Mockito.eq("controller-id"), Mockito.eq(manualUrl), Mockito.eq(""),
                Mockito.eq(""), Mockito.eq("review-instance"), Mockito.eq(false), Mockito.any(RequestPurpose.class));
        ExecutorService worker = Executors.newSingleThreadExecutor();

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Future<?> oldRetry = worker.submit(retries.getFirst());
            assertThat(oldRegistrationStarted.await(5, TimeUnit.SECONDS), is(true));
            handler.dispose();
            RachioConfiguration manualConfiguration = new RachioConfiguration();
            manualConfiguration.autoConfigureWebhooks = true;
            manualConfiguration.publicWebhookUrl = manualUrl;
            setField(handler, "lifecycleGeneration", 2L);
            setField(handler, "thingConfig", manualConfiguration);
            setField(handler, "rachioApi", currentApi);
            setField(handler, "disposed", false);
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);
            assertThat(remoteCallback.get(), is(manualUrl));

            allowOldRegistration.countDown();
            oldRetry.get(5, TimeUnit.SECONDS);

            assertThat(remoteCallback.get(), is(manualUrl));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(manualUrl));
            verify(currentApi).registerWebHook("controller-id", manualUrl, "", "", "review-instance", false,
                    RequestPurpose.INITIALIZATION);
            verify(currentApi, Mockito.atLeastOnce()).registerWebHook("controller-id", manualUrl, "", "",
                    "review-instance", false, RequestPurpose.BACKGROUND_REFRESH);
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
        } finally {
            allowOldRegistration.countDown();
            worker.shutdownNow();
            try {
                assertThat(worker.awaitTermination(5, TimeUnit.SECONDS), is(true));
            } finally {
                handler.dispose();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void staleAcquisitionAfterDisposeCannotRegisterModernOrLegacyCallbacks(boolean reinitialized) throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        RachioCloudWebhookRegistry registry = Mockito.mock(RachioCloudWebhookRegistry.class);
        CountDownLatch acquisitionStarted = new CountDownLatch(1);
        CountDownLatch allowAcquisition = new CountDownLatch(1);
        Mockito.when(registry.acquire(Mockito.anyString())).thenThrow(new CloudWebhookException("cloud reconnecting"))
                .thenAnswer(invocation -> {
                    acquisitionStarted.countDown();
                    assertThat(allowAcquisition.await(5, TimeUnit.SECONDS), is(true));
                    return new CloudWebhookLease(webhookUrl, Instant.now().plusSeconds(24 * 60 * 60), 1, 123);
                });
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        handler.getWebhookConfiguration().callbackUrl = "https://example.org/legacy-fallback";
        RachioApi oldApi = Mockito.mock(RachioApi.class);
        RachioApi currentApi = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", oldApi);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");
        ExecutorService worker = Executors.newSingleThreadExecutor();

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Future<?> retry = worker.submit(retries.getFirst());
            assertThat(acquisitionStarted.await(5, TimeUnit.SECONDS), is(true));
            handler.dispose();
            if (reinitialized) {
                // Install a replacement live lifecycle without scheduling unrelated bridge initialization work.
                setField(handler, "lifecycleGeneration", 2L);
                setField(handler, "rachioApi", currentApi);
                setField(handler, "disposed", false);
            }
            allowAcquisition.countDown();
            retry.get(5, TimeUnit.SECONDS);

            for (RachioApi api : List.of(oldApi, currentApi)) {
                verify(api, never()).registerWebHook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                        Mockito.any(RequestPurpose.class));
                verify(api, never()).registerLegacyNotificationWebHook(Mockito.anyString(), Mockito.anyString(),
                        Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                        Mockito.any(RequestPurpose.class));
            }
            verify(registry, Mockito.times(2)).acquire(Mockito.anyString());
            verify(registry).release(handler.getThing().getUID().getAsString(), 123);
        } finally {
            allowAcquisition.countDown();
            worker.shutdownNow();
            try {
                assertThat(worker.awaitTermination(5, TimeUnit.SECONDS), is(true));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void staleManualUrlAcquisitionCannotReleaseReinitializedCloudLease() throws Exception {
        String manualUrl = "https://manual.example.org/rachio/webhook";
        String currentUrl = "https://cloud.example.org/rachio/current";
        RachioCloudWebhookRegistry registry = Mockito.mock(RachioCloudWebhookRegistry.class);
        Mockito.when(registry.acquire(Mockito.anyString()))
                .thenReturn(new CloudWebhookLease(currentUrl, Instant.now().plusSeconds(24 * 60 * 60), 1, 123));
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> refreshJob = Mockito.mock(ScheduledFuture.class);
        Mockito.doAnswer(invocation -> {
            long delay = invocation.getArgument(1);
            return delay > 300 ? refreshJob : null;
        }).when(scheduler).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.SECONDS));
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioConfiguration manualConfiguration = new RachioConfiguration();
        manualConfiguration.autoConfigureWebhooks = true;
        manualConfiguration.publicWebhookUrl = manualUrl;
        setField(handler, "thingConfig", manualConfiguration);
        RachioApi oldApi = Mockito.mock(RachioApi.class);
        RachioApi currentApi = Mockito.mock(RachioApi.class);
        Mockito.when(currentApi.getExternalId()).thenReturn("review-instance");
        setField(handler, "rachioApi", oldApi);
        addConfiguredControllerHandler(handler, "controller-id");
        CountDownLatch oldLifecycleCheckCompleted = new CountDownLatch(1);
        CountDownLatch allowOldLifecycleCheck = new CountDownLatch(1);
        AtomicInteger oldLifecycleChecks = new AtomicInteger();
        Mockito.doAnswer(invocation -> {
            boolean current = (boolean) invocation.callRealMethod();
            if (current && oldLifecycleChecks.getAndIncrement() == 0) {
                oldLifecycleCheckCompleted.countDown();
                assertThat(allowOldLifecycleCheck.await(5, TimeUnit.SECONDS), is(true));
            }
            return current;
        }).when(handler).isLifecycleCurrent(Mockito.eq(0L), Mockito.same(oldApi));
        ExecutorService worker = Executors.newSingleThreadExecutor();

        try {
            Future<String> oldAcquisition = worker.submit(() -> {
                return handler.getModernWebhookUrlForRegistration();
            });
            assertThat(oldLifecycleCheckCompleted.await(5, TimeUnit.SECONDS), is(true));
            handler.dispose();
            RachioConfiguration cloudConfiguration = new RachioConfiguration();
            cloudConfiguration.autoConfigureWebhooks = true;
            cloudConfiguration.useCloudWebhook = true;
            setField(handler, "lifecycleGeneration", 2L);
            setField(handler, "thingConfig", cloudConfiguration);
            setField(handler, "rachioApi", currentApi);
            setField(handler, "disposed", false);
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);
            verify(currentApi).registerWebHook("controller-id", currentUrl, "", "", "review-instance", false,
                    RequestPurpose.INITIALIZATION);
            ArgumentCaptor<Long> refreshDelay = ArgumentCaptor.forClass(Long.class);
            verify(scheduler).schedule(Mockito.any(Runnable.class), refreshDelay.capture(),
                    Mockito.eq(TimeUnit.SECONDS));
            assertThat(refreshDelay.getValue() > 300, is(true));

            allowOldLifecycleCheck.countDown();
            assertThat(oldAcquisition.get(5, TimeUnit.SECONDS), is(""));

            assertThat(handler.getModernWebhookUrlForRegistration(), is(currentUrl));
            verify(registry).acquire(handler.getThing().getUID().getAsString());
            verify(registry, never()).release(handler.getThing().getUID().getAsString(), 123);
            verify(refreshJob, never()).cancel(Mockito.anyBoolean());
            verify(oldApi, never()).registerWebHook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                    Mockito.any(RequestPurpose.class));
            verify(oldApi, never()).registerLegacyNotificationWebHook(Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                    Mockito.any(RequestPurpose.class));
        } finally {
            allowOldLifecycleCheck.countDown();
            worker.shutdownNow();
            try {
                assertThat(worker.awaitTermination(5, TimeUnit.SECONDS), is(true));
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void throttledRetryReschedulesFailedAndUnvisitedControllers() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");
        Mockito.doThrow(throttledException(RequestPurpose.BACKGROUND_REFRESH)).doNothing().when(api)
                .registerWebHook("controller-A", webhookUrl, "", "", null, false, RequestPurpose.BACKGROUND_REFRESH);

        try {
            handler.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
            handler.registerWebHook("controller-B", RequestPurpose.INITIALIZATION);
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
            assertThat(retries.size(), is(1));
            retries.getFirst().run();

            verify(api, never()).registerWebHook(Mockito.eq("controller-B"), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.any(RequestPurpose.class));
            assertThat(retries.size(), is(2));
            retries.get(1).run();

            verify(api, Mockito.times(2)).registerWebHook("controller-A", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(api).registerWebHook("controller-B", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(2));
            verify(service, Mockito.times(3)).requestWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void throttledFullPassRetriesControllerThatHasNotBeenVisited() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/webhook";
        WebhookService service = cloudWebhookService(webhookUrl);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");
        Mockito.doThrow(throttledException(RequestPurpose.INITIALIZATION)).doNothing().when(api).registerWebHook(
                Mockito.eq("controller-A"), Mockito.eq(webhookUrl), Mockito.eq(""), Mockito.eq(""), Mockito.isNull(),
                Mockito.eq(false), Mockito.any(RequestPurpose.class));

        try {
            handler.reconcileConfiguredWebhooks(RequestPurpose.INITIALIZATION);

            verify(api).registerWebHook("controller-A", webhookUrl, "", "", null, false, RequestPurpose.INITIALIZATION);
            verify(api, never()).registerWebHook(Mockito.eq("controller-B"), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.any(RequestPurpose.class));
            assertThat(retries.size(), is(1));
            retries.getFirst().run();

            verify(api).registerWebHook("controller-A", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(api).registerWebHook("controller-B", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(1));
            verify(service).requestWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "valve", "program" })
    void missingSmartHoseModelIsRetriedWhenModelReturns(String resource) throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Runnable> retries = captureCloudWebhookRetries(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        handler.getWebhookConfiguration().autoConfigureHoseTimerWebhooks = true;
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        RachioValveHandler valveHandler = new RachioValveHandler(thing(THING_TYPE_VALVE, "valve"));
        RachioValveProgramHandler programHandler = new RachioValveProgramHandler(
                thing(THING_TYPE_VALVE_PROGRAM, "program"));
        if ("valve".equals(resource)) {
            Mockito.when(valveHandler.getThing().getConfiguration())
                    .thenReturn(new Configuration(Map.of(PROPERTY_VALVE_ID, "target-id")));
            setField(valveHandler, "cloudHandler", handler);
            handler.smartHoseStatusListeners.add(valveHandler);
        } else {
            Mockito.when(programHandler.getThing().getConfiguration())
                    .thenReturn(new Configuration(Map.of(PROPERTY_VALVE_PROGRAM_ID, "target-id")));
            setField(programHandler, "cloudHandler", handler);
            handler.smartHoseStatusListeners.add(programHandler);
        }

        try {
            if ("valve".equals(resource)) {
                handler.registerValveWebHook("target-id", RequestPurpose.INITIALIZATION);
            } else {
                handler.registerValveProgramWebHook("target-id", RequestPurpose.INITIALIZATION);
            }
            assertThat(retries.size(), is(1));
            retries.getFirst().run();

            verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                    Mockito.anyString(), Mockito.anyString(), Mockito.nullable(String.class), Mockito.anyBoolean(),
                    Mockito.any(RequestPurpose.class));
            assertThat(retries.size(), is(2));
            if ("valve".equals(resource)) {
                RachioValve valve = new RachioValve();
                valve.id = "target-id";
                setField(valveHandler, "valve", valve);
            } else {
                RachioValveProgram program = new RachioValveProgram();
                program.id = "target-id";
                setField(programHandler, "program", program);
            }
            retries.get(1).run();

            ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);
            verify(api).registerWebHookTarget(targetCaptor.capture(), Mockito.eq(webhookUrl), Mockito.eq(""),
                    Mockito.eq(""), Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
            assertThat(targetCaptor.getValue().getResourceId(), is("target-id"));
            assertThat(targetCaptor.getValue().getResourceType(),
                    is("valve".equals(resource) ? RachioWebhookResourceType.VALVE : RachioWebhookResourceType.PROGRAM));
            assertThat(retries.size(), is(2));
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "controller-A", "controller-B" })
    void anotherControllerSuccessKeepsRetryForSkippedController(String skippedController) throws Exception {
        String successfulController = "controller-A".equals(skippedController) ? "controller-B" : "controller-A";
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");

        try {
            handler.registerWebHook(skippedController, RequestPurpose.INITIALIZATION);
            Runnable retry = scheduledCloudWebhookRetry(scheduler);
            handler.registerWebHook(successfulController, RequestPurpose.INITIALIZATION);

            verify(retryFuture, never()).cancel(Mockito.anyBoolean());
            retry.run();

            verify(api).registerWebHook(skippedController, webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(api).registerWebHook(successfulController, webhookUrl, "", "", null, false,
                    RequestPurpose.INITIALIZATION);
            verify(api, Mockito.times(2)).registerWebHook(Mockito.anyString(), Mockito.eq(webhookUrl), Mockito.eq(""),
                    Mockito.eq(""), Mockito.isNull(), Mockito.eq(false), Mockito.any(RequestPurpose.class));
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void controllerSuccessKeepsRetryForSkippedValveProgram() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioConfiguration configuration = new RachioConfiguration();
        configuration.autoConfigureWebhooks = true;
        configuration.autoConfigureHoseTimerWebhooks = true;
        configuration.useCloudWebhook = true;
        setField(handler, "thingConfig", configuration);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");
        addConfiguredValveProgramHandler(handler, "program-id");

        try {
            handler.registerValveProgramWebHook("program-id", RequestPurpose.INITIALIZATION);
            Runnable retry = scheduledCloudWebhookRetry(scheduler);
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);

            verify(retryFuture, never()).cancel(Mockito.anyBoolean());
            retry.run();

            ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);
            verify(api).registerWebHookTarget(targetCaptor.capture(), Mockito.eq(webhookUrl), Mockito.eq(""),
                    Mockito.eq(""), Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
            assertThat(targetCaptor.getValue().getResourceId(), is("program-id"));
            assertThat(targetCaptor.getValue().getResourceType(), is(RachioWebhookResourceType.PROGRAM));
            verify(api).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                    RequestPurpose.INITIALIZATION);
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void retryDoesNotRegisterRemovedController() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        RachioDeviceHandler removedController = addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");

        try {
            handler.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
            Runnable retry = scheduledCloudWebhookRetry(scheduler);
            handler.registerWebHook("controller-B", RequestPurpose.INITIALIZATION);
            handler.rachioStatusListeners.remove(removedController);

            retry.run();

            verify(api, never()).registerWebHook(Mockito.eq("controller-A"), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.any(RequestPurpose.class));
            verify(api).registerWebHook("controller-B", webhookUrl, "", "", null, false, RequestPurpose.INITIALIZATION);
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void registeringSkippedControllerBeforeRetryCancelsRemainingWork() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");

        try {
            handler.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
            Runnable retry = scheduledCloudWebhookRetry(scheduler);
            handler.registerWebHook("controller-B", RequestPurpose.INITIALIZATION);
            handler.registerWebHook("controller-A", RequestPurpose.USER_COMMAND);

            verify(retryFuture).cancel(false);
            retry.run();

            verify(api).registerWebHook("controller-A", webhookUrl, "", "", null, false, RequestPurpose.USER_COMMAND);
            verify(api).registerWebHook("controller-B", webhookUrl, "", "", null, false, RequestPurpose.INITIALIZATION);
            verify(api, never()).registerWebHook(Mockito.anyString(), Mockito.anyString(), Mockito.anyString(),
                    Mockito.anyString(), Mockito.isNull(), Mockito.anyBoolean(),
                    Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void lateAcquisitionFailureRetriesSkippedControllerUsingNewUrl() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        RachioCloudWebhookRegistry registry = Mockito.mock(RachioCloudWebhookRegistry.class);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");
        CountDownLatch firstAcquisitionStarted = new CountDownLatch(1);
        CountDownLatch allowFirstFailure = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            firstAcquisitionStarted.countDown();
            assertThat(allowFirstFailure.await(10, TimeUnit.SECONDS), is(true));
            throw new CloudWebhookException("first acquisition timed out");
        }).doReturn(new CloudWebhookLease(webhookUrl, Instant.now().plusSeconds(24 * 60 * 60), 1, 1)).when(registry)
                .acquire(Mockito.anyString());
        CompletableFuture<Void> firstRegistration = CompletableFuture.runAsync(() -> {
            try {
                handler.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
            } catch (RachioApiException e) {
                throw new IllegalStateException(e);
            }
        });

        try {
            assertThat(firstAcquisitionStarted.await(5, TimeUnit.SECONDS), is(true));
            handler.registerWebHook("controller-B", RequestPurpose.INITIALIZATION);
            allowFirstFailure.countDown();
            firstRegistration.get(5, TimeUnit.SECONDS);
            Runnable retry = scheduledCloudWebhookRetry(scheduler);

            assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE), is("registered"));
            verify(retryFuture, never()).cancel(Mockito.anyBoolean());
            retry.run();

            verify(api).registerWebHook("controller-A", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(api).registerWebHook("controller-B", webhookUrl, "", "", null, false, RequestPurpose.INITIALIZATION);
            verify(registry, Mockito.times(2)).acquire(Mockito.anyString());
            verify(registry, never()).invalidateCachedWebhook(Mockito.anyLong());
        } finally {
            allowFirstFailure.countDown();
            try {
                firstRegistration.get(5, TimeUnit.SECONDS);
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void disposalCancelsRetryForSkippedControllerAfterAnotherControllerSucceeds() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-A");
        addConfiguredControllerHandler(handler, "controller-B");

        handler.registerWebHook("controller-A", RequestPurpose.INITIALIZATION);
        Runnable retry = scheduledCloudWebhookRetry(scheduler);
        handler.registerWebHook("controller-B", RequestPurpose.INITIALIZATION);
        handler.dispose();
        retry.run();

        verify(retryFuture).cancel(false);
        verify(api, never()).registerWebHook(Mockito.eq("controller-A"), Mockito.anyString(), Mockito.anyString(),
                Mockito.anyString(), Mockito.isNull(), Mockito.anyBoolean(), Mockito.any(RequestPurpose.class));
        verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
    }

    @Test
    void retryBackoffIsCappedAndResetsAfterAllPendingRegistrationsSucceed() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Long> retryDelays = new CopyOnWriteArrayList<>();
        List<Runnable> retries = captureCloudWebhookRetries(scheduler, retryDelays);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");
        AtomicInteger remainingFailures = new AtomicInteger(6);
        Mockito.doAnswer(invocation -> {
            if (remainingFailures.getAndDecrement() > 0) {
                throw new RachioApiException("transient registration failure");
            }
            return null;
        }).when(api).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                RequestPurpose.BACKGROUND_REFRESH);

        try {
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);
            for (int attempt = 0; attempt < 7; attempt++) {
                assertThat(retries.size(), is(attempt + 1));
                retries.get(attempt).run();
            }

            assertThat(retryDelays, is(List.of(30L, 60L, 120L, 240L, 300L, 300L, 300L)));
            verify(api, Mockito.times(7)).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(7));

            addConfiguredControllerHandler(handler, "later-controller");
            Mockito.doThrow(throttledException(RequestPurpose.INITIALIZATION)).when(api).registerWebHook(
                    "later-controller", webhookUrl, "", "", null, false, RequestPurpose.INITIALIZATION);
            assertThrows(RachioApiThrottledException.class,
                    () -> handler.registerWebHook("later-controller", RequestPurpose.INITIALIZATION));

            assertThat(retryDelays.getLast(), is(30L));
            assertThat(retries.size(), is(8));
            retries.getLast().run();

            verify(api).registerWebHook("later-controller", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            verify(api, Mockito.times(7)).registerWebHook("controller-id", webhookUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(8));
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void providerInvalidationResetsRetryBackoff() throws Exception {
        String oldUrl = "https://old-cloud.example.org/rachio/webhook";
        String currentUrl = "https://current-cloud.example.org/rachio/webhook";
        WebhookService oldService = cloudWebhookService(oldUrl);
        Mockito.when(oldService.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("old cloud reconnecting")));
        WebhookService currentService = cloudWebhookService(currentUrl);
        Mockito.when(currentService.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("new cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(currentUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        AtomicReference<WebhookService> provider = new AtomicReference<>(oldService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(provider::get);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        List<Long> retryDelays = new CopyOnWriteArrayList<>();
        List<Runnable> retries = captureCloudWebhookRetries(scheduler, retryDelays);
        Mockito.doAnswer(invocation -> {
            invocation.getArgument(0, Runnable.class).run();
            return null;
        }).when(scheduler).execute(Mockito.any(Runnable.class));
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        addConfiguredControllerHandler(handler, "controller-id");

        try {
            handler.registerWebHook("controller-id", RequestPurpose.INITIALIZATION);
            retries.getFirst().run();
            retries.get(1).run();
            assertThat(retryDelays, is(List.of(30L, 60L, 120L)));

            provider.set(currentService);
            registry.onProviderChanged(oldService, currentService);
            handler.onCloudWebhookProviderChanged();

            assertThat(retryDelays, is(List.of(30L, 60L, 120L, 30L)));
            retries.getLast().run();

            verify(api).registerWebHook("controller-id", currentUrl, "", "", null, false,
                    RequestPurpose.BACKGROUND_REFRESH);
            assertThat(retries.size(), is(4));
            verify(oldService, Mockito.times(3)).requestWebhook("/rachio/webhook");
            verify(currentService, Mockito.times(2)).requestWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void repeatedCloudWebhookFailuresKeepOnlyOneScheduledRetry() throws Exception {
        WebhookService service = cloudWebhookService("https://cloud.example.org/rachio/webhook");
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        setField(handler, "rachioApi", Mockito.mock(RachioApi.class));
        addConfiguredControllerHandler(handler, "controller-id");

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Runnable retry = scheduledCloudWebhookRetry(scheduler);

            retry.run();

            verify(scheduler).schedule(Mockito.any(Runnable.class), Mockito.eq(30L), Mockito.eq(TimeUnit.SECONDS));
            verify(scheduler).schedule(Mockito.any(Runnable.class), Mockito.eq(60L), Mockito.eq(TimeUnit.SECONDS));
            verify(scheduler, Mockito.times(2)).schedule(Mockito.any(Runnable.class), Mockito.anyLong(),
                    Mockito.eq(TimeUnit.SECONDS));
        } finally {
            handler.dispose();
        }
    }

    @Test
    void successfulAcquisitionCancelsRetryAndStaleRetryKeepsNewWebhook() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        WebhookService service = cloudWebhookService(webhookUrl);
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(webhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Runnable retry = scheduledCloudWebhookRetry(scheduler);
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));

            retry.run();

            verify(retryFuture).cancel(false);
            verify(service, Mockito.times(2)).requestWebhook("/rachio/webhook");
            verify(service, never()).removeWebhook("/rachio/webhook");
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
        } finally {
            handler.dispose();
        }
    }

    @Test
    void staleCloudWebhookAcquisitionFailureDoesNotOverwriteNewSuccess() throws Exception {
        String webhookUrl = "https://cloud.example.org/rachio/recovered";
        RachioCloudWebhookRegistry registry = Mockito.mock(RachioCloudWebhookRegistry.class);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);
        CountDownLatch firstAcquisitionStarted = new CountDownLatch(1);
        CountDownLatch allowFirstFailure = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            firstAcquisitionStarted.countDown();
            assertThat(allowFirstFailure.await(10, TimeUnit.SECONDS), is(true));
            throw new CloudWebhookException("first acquisition timed out");
        }).doReturn(new CloudWebhookLease(webhookUrl, Instant.now().plusSeconds(24 * 60 * 60), 1, 1)).when(registry)
                .acquire(Mockito.anyString());
        CompletableFuture<String> firstAcquisition = CompletableFuture
                .supplyAsync(handler::getModernWebhookUrlForRegistration);

        try {
            assertThat(firstAcquisitionStarted.await(5, TimeUnit.SECONDS), is(true));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
            assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE), is("cloud URL ready"));

            allowFirstFailure.countDown();
            assertThat(firstAcquisition.get(5, TimeUnit.SECONDS), is(""));

            assertThat(webhookProperty(handler, PROPERTY_WEBHOOK_REGISTRATION_STATE), is("cloud URL ready"));
            assertThat(handler.getModernWebhookUrlForRegistration(), is(webhookUrl));
            verify(scheduler, never()).schedule(Mockito.any(Runnable.class), Mockito.eq(30L),
                    Mockito.eq(TimeUnit.SECONDS));
        } finally {
            allowFirstFailure.countDown();
            try {
                firstAcquisition.get(5, TimeUnit.SECONDS);
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void disposalCancelsCloudWebhookRetry() throws Exception {
        WebhookService service = cloudWebhookService("https://cloud.example.org/rachio/webhook");
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
        Runnable retry = scheduledCloudWebhookRetry(scheduler);

        handler.dispose();
        retry.run();

        verify(retryFuture).cancel(false);
        verify(service, Mockito.times(1)).requestWebhook("/rachio/webhook");
    }

    @Test
    void disablingAutomaticWebhooksCancelsCloudWebhookRetry() throws Exception {
        WebhookService service = cloudWebhookService("https://cloud.example.org/rachio/webhook");
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Runnable retry = scheduledCloudWebhookRetry(scheduler);
            setField(handler, "thingConfig", new RachioConfiguration());
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));

            retry.run();

            verify(retryFuture).cancel(false);
            verify(service, Mockito.times(1)).requestWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void providerChangeCancelsCloudWebhookRetry() throws Exception {
        WebhookService service = cloudWebhookService("https://cloud.example.org/rachio/webhook");
        Mockito.when(service.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.failedFuture(new IllegalStateException("cloud reconnecting")));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> service);
        ScheduledExecutorService scheduler = Mockito.mock(ScheduledExecutorService.class);
        ScheduledFuture<?> retryFuture = mockCloudWebhookRetry(scheduler);
        RachioBridgeHandler handler = cloudWebhookHandler("bridge", registry, scheduler);

        try {
            assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
            Runnable retry = scheduledCloudWebhookRetry(scheduler);

            handler.onCloudWebhookProviderChanged();
            retry.run();

            verify(retryFuture).cancel(false);
            verify(service, Mockito.times(1)).requestWebhook("/rachio/webhook");
        } finally {
            handler.dispose();
        }
    }

    @Test
    void cloudWebhookProviderChangeDuringReconciliationTriggersAnotherPass() throws Exception {
        String secondWebhookUrl = "https://cloud.example.org/rachio/second";
        String thirdWebhookUrl = "https://cloud.example.org/rachio/third";
        WebhookService firstWebhookService = cloudWebhookService("https://cloud.example.org/rachio/first");
        WebhookService secondWebhookService = cloudWebhookService(secondWebhookUrl);
        WebhookService thirdWebhookService = cloudWebhookService(thirdWebhookUrl);
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>(firstWebhookService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(webhookService::get);
        RachioApi api = Mockito.mock(RachioApi.class);
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT, registry);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        setField(handler, "thingConfig", config);
        setField(handler, "rachioApi", api);
        Mockito.when(api.getDevices()).thenReturn(new HashMap<>());
        addConfiguredControllerHandler(handler, "controller-id");

        CountDownLatch secondRegistrationStarted = new CountDownLatch(1);
        CountDownLatch allowSecondRegistrationToComplete = new CountDownLatch(1);
        Mockito.doAnswer(invocation -> {
            String webhookUrl = invocation.getArgument(1);
            if (secondWebhookUrl.equals(webhookUrl)) {
                secondRegistrationStarted.countDown();
                assertThat(allowSecondRegistrationToComplete.await(1, TimeUnit.SECONDS), is(true));
            }
            return null;
        }).when(api).registerWebHook(Mockito.eq("controller-id"), Mockito.anyString(), Mockito.eq(""), Mockito.eq(""),
                Mockito.isNull(), Mockito.eq(false), Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));

        webhookService.set(secondWebhookService);
        registry.onProviderChanged(firstWebhookService, secondWebhookService);
        handler.onCloudWebhookProviderChanged();
        assertThat(secondRegistrationStarted.await(1, TimeUnit.SECONDS), is(true));

        webhookService.set(thirdWebhookService);
        registry.onProviderChanged(secondWebhookService, thirdWebhookService);
        handler.onCloudWebhookProviderChanged();
        allowSecondRegistrationToComplete.countDown();
        ArgumentCaptor<String> webhookUrlCaptor = ArgumentCaptor.forClass(String.class);
        verify(api, Mockito.timeout(1000).times(2)).registerWebHook(Mockito.eq("controller-id"),
                webhookUrlCaptor.capture(), Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
        assertThat(webhookUrlCaptor.getAllValues(), is(List.of(secondWebhookUrl, thirdWebhookUrl)));
        assertThat(handler.getModernWebhookUrlForRegistration(), is(thirdWebhookUrl));
        verify(secondWebhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
        verify(thirdWebhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
        handler.dispose();
    }

    @Test
    void modernWebhookUrlRejectsUserInfoCredentials() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "https://user:password@example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
    }

    @Test
    void validLegacyZoneStartedDispatchesBeforeReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("ZONE_STATUS", "ZONE_STARTED");
        RachioDeviceHandler deviceHandler = directlyHandledDeviceHandler(handler, event, true);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, deviceHandler, event);
    }

    @Test
    void validLegacyZoneStoppedDispatchesBeforeReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("ZONE_STATUS", "ZONE_STOPPED");
        RachioDeviceHandler deviceHandler = directlyHandledDeviceHandler(handler, event, true);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, deviceHandler, event);
    }

    @Test
    void validLegacyScheduleStartedDispatchesBeforeReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STARTED");
        RachioDeviceHandler deviceHandler = directlyHandledDeviceHandler(handler, event, true);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, deviceHandler, event);
    }

    @Test
    void legacyControllerValidationUsesCaseInsensitiveDeviceIdentity() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STARTED");
        event.deviceId = "CONTROLLER-ID";
        RachioDeviceHandler deviceHandler = directlyHandledDeviceHandler(handler, event, true);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, deviceHandler, event);
    }

    @Test
    void unhandledLegacyEventStillTriggersReconciliation() {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent("ZONE_STATUS", "ZONE_STARTED");
        RachioDeviceHandler deviceHandler = directlyHandledDeviceHandler(handler, event, false);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verifyDispatchBeforeReconciliation(handler, deviceHandler, event);
    }

    @Test
    void legacyZoneStartedDirectlyUpdatesMatchingZoneBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandling("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED, "\"zoneNumber\": 7,",
                OnOffType.ON);
    }

    @Test
    void legacyZoneCompletedDirectlyUpdatesMatchingZoneBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandling("ZONE_COMPLETED", EVENT_DEVICE_ZONE_RUN_COMPLETED, "\"zoneId\": \"zone-id\",",
                OnOffType.OFF);
    }

    @Test
    void legacyZoneStoppedClearsControllerSummaryBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandling("ZONE_STOPPED", EVENT_DEVICE_ZONE_RUN_STOPPED, "\"zoneNumber\": 7,",
                OnOffType.OFF);
    }

    @Test
    void legacyZoneStartedWithShortRunStatusStateDirectlyUpdatesMatchingZoneBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandlingWithRunStatusState("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED,
                "\"zoneNumber\": 7,", "STARTED", OnOffType.ON);
    }

    @Test
    void legacyZoneStoppedWithShortRunStatusStateClearsControllerSummaryBeforeReconciliation() throws Exception {
        verifyLegacyZoneRunDirectHandlingWithRunStatusState("ZONE_STOPPED", EVENT_DEVICE_ZONE_RUN_STOPPED,
                "\"zoneNumber\": 7,", "STOPPED", OnOffType.OFF);
    }

    @Test
    void legacyZoneCompletedWithShortRunStatusStateClearsControllerSummaryAndKeepsLastEventMetadata() throws Exception {
        verifyLegacyZoneRunDirectHandlingWithRunStatusState("ZONE_COMPLETED", EVENT_DEVICE_ZONE_RUN_COMPLETED,
                "\"zoneId\": \"zone-id\",", "COMPLETED", OnOffType.OFF);
    }

    @Test
    void legacyScheduleStartedResolvesRegisteredControllerHandlerBeforeReconciliation() {
        verifyRegisteredControllerHandlerResolution("SCHEDULE_STATUS", "SCHEDULE_STARTED", EVENT_SCHEDULE_STARTED);
    }

    @Test
    void legacyScheduleStoppedResolvesRegisteredControllerHandlerBeforeReconciliation() {
        verifyRegisteredControllerHandlerResolution("SCHEDULE_STATUS", "SCHEDULE_STOPPED", EVENT_SCHEDULE_STOPPED);
    }

    @Test
    void legacyZoneRunWithoutZoneIdentityStillReconciles() {
        RachioBridgeHandler handler = legacyHandler();
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone());
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = realShapedLegacyZoneEvent("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED, "");

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void legacyZoneRunWithAmbiguousZoneIdentityStillReconciles() {
        RachioBridgeHandler handler = legacyHandler();
        ThingHandlerCallback deviceCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone(), deviceCallback);
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = realShapedLegacyZoneEvent("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED,
                "\"zoneId\": \"zone-id\", \"zoneNumber\": 8,");

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        verify(deviceCallback, never()).stateUpdated(
                new ChannelUID(deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                new DecimalType(7));
        verify(deviceCallback, never()).stateUpdated(
                new ChannelUID(deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.ON);
    }

    @Test
    void legacyEventForUnknownControllerIsRejectedWithoutRefresh() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, HTTP_CLIENT));
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        Mockito.doReturn(new HashMap<String, RachioDevice>()).when(handler).getDevices();
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.deviceId = "unknown-controller";
        event.type = "ZONE_STATUS";

        assertThat(handler.legacyWebHookEvent(event), is(false));

        verify(handler, never()).webHookEvent(event);
        verify(handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void modernScheduleStartedUpdatesControllerSummaryBeforeReconciliation() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_SCHEDULE_STARTED, """
                "scheduleId": "schedule-id",
                "scheduleName": "Morning Lawn",
                "runType": "FIXED",
                "startTime": "2026-06-18T10:00:00Z",
                "endTime": "2026-06-18T10:10:00Z",
                "durationSeconds": "600"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.currentScheduleId, is("schedule-id"));
        assertThat(fixture.device.currentScheduleName, is("Morning Lawn"));
        assertThat(fixture.device.currentScheduleType, is("FIXED"));
        assertThat(fixture.device.currentScheduleStartTime, is("2026-06-18T10:00:00Z"));
        assertThat(fixture.device.currentScheduleEndTime, is("2026-06-18T10:10:00Z"));
        assertThat(fixture.device.currentScheduleDuration, is(600));
        assertThat(fixture.device.currentScheduleRunning, is(true));
        assertThat(fixture.device.lastApiEventType, is(EVENT_SCHEDULE_STARTED));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_ID),
                new StringType("schedule-id"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_NAME),
                new StringType("Morning Lawn"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_TYPE),
                new StringType("FIXED"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_START),
                new DateTimeType("2026-06-18T10:00:00Z"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_END),
                new DateTimeType("2026-06-18T10:10:00Z"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_DURATION),
                RachioQuantityTypes.seconds(600));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING),
                OnOffType.ON);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_LAST_API_EVENT_TYPE),
                new StringType(EVENT_SCHEDULE_STARTED));
    }

    @Test
    void modernScheduleStartedResolvesScheduleNameAndTimesFromKnownSchedule() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_SCHEDULE_STARTED, """
                "scheduleId": "schedule-id",
                "durationSeconds": "600"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.currentScheduleId, is("schedule-id"));
        assertThat(fixture.device.currentScheduleName, is("Morning Lawn"));
        assertThat(fixture.device.currentScheduleType, is("FIXED"));
        assertThat(fixture.device.currentScheduleStartTime, is("2026-06-18T10:00:00Z"));
        assertThat(fixture.device.currentScheduleEndTime, is("2026-06-18T10:10:00Z"));
        assertThat(fixture.device.currentScheduleDuration, is(600));
        assertThat(fixture.device.currentScheduleRunning, is(true));
    }

    @Test
    void modernScheduleStoppedClearsControllerSummaryBeforeReconciliation() throws Exception {
        verifyModernScheduleEndClearsControllerSummary(EVENT_SCHEDULE_STOPPED);
    }

    @Test
    void modernScheduleCompletedClearsControllerSummaryAndUpdatesScheduleThingBeforeReconciliation() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        seedRunningSchedule(fixture.device);
        ThingHandlerCallback scheduleCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioScheduleHandler scheduleHandler = new RachioScheduleHandler(thing(THING_TYPE_SCHEDULE, "schedule"));
        scheduleHandler.scheduleRuleId = "schedule-id";
        scheduleHandler.setCallback(scheduleCallback);
        fixture.handler.rachioStatusListeners.add(scheduleHandler);
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_SCHEDULE_COMPLETED, """
                "scheduleId": "schedule-id",
                "scheduleName": "Morning Lawn",
                "runType": "FIXED",
                "endTime": "2026-06-18T10:10:00Z"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.currentScheduleRunning, is(false));
        assertThat(fixture.device.currentScheduleName, is(""));
        assertThat(fixture.device.lastApiEventType, is(EVENT_SCHEDULE_COMPLETED));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING),
                OnOffType.OFF);
        verify(scheduleCallback).stateUpdated(
                new ChannelUID(scheduleHandler.getThing().getUID(), CHANNEL_SCHEDULE_LAST_RUN),
                new DateTimeType("2026-06-18T10:10:00Z"));
    }

    @Test
    void modernZoneStartedUpdatesMatchingZoneAndControllerActiveZoneBeforeReconciliation() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "scheduleName": "Quick Run",
                "runType": "MANUAL",
                "startTime": "2026-06-18T10:00:00Z",
                "endTime": "2026-06-18T10:02:00Z",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.activeZoneId, is("zone-id"));
        assertThat(fixture.device.activeZoneName, is("Front lawn"));
        assertThat(fixture.device.activeZoneNumber, is(7));
        assertThat(fixture.device.currentScheduleName, is("Quick Run"));
        assertThat(fixture.device.currentScheduleRunning, is(true));
        verify(fixture.zoneCallback)
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_ID),
                new StringType("zone-id"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                new DecimalType(7));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME),
                new StringType("Front lawn"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING),
                OnOffType.ON);
    }

    @Test
    void modernZoneStartedUsesResolvedZoneNameWhenPayloadOmitsZoneName() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneNumber": "7",
                "runType": "MANUAL",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(event.zoneName, is("Front lawn"));
        assertThat(fixture.device.activeZoneName, is("Front lawn"));
        verify(fixture.zoneCallback)
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME),
                new StringType("Front lawn"));
    }

    @Test
    void modernZoneStartedResolvesActiveScheduleNameFromKnownSchedule() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "scheduleId": "schedule-id",
                "runType": "FIXED",
                "durationSeconds": "300"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.currentScheduleName, is("Morning Lawn"));
        assertThat(fixture.device.currentScheduleType, is("FIXED"));
        assertThat(fixture.device.currentScheduleStartTime, is("2026-06-18T10:00:00Z"));
        assertThat(fixture.device.currentScheduleEndTime, is("2026-06-18T10:05:00Z"));
        assertThat(fixture.device.currentScheduleDuration, is(300));
    }

    @Test
    void legacyIrrigationEventIsAcknowledgedWithoutDispatchOrReconciliationWhenModernModeActive() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        enableModernWebhookMode(fixture.handler);
        RachioEventGsonDTO legacyEvent = realShapedLegacyZoneEvent("ZONE_STARTED", EVENT_DEVICE_ZONE_RUN_STARTED,
                "\"zoneNumber\": 7,");

        assertThat(fixture.handler.legacyWebHookEvent(legacyEvent), is(true));

        verify(fixture.deviceHandler, never()).webhookEvent(Mockito.any(RachioEventGsonDTO.class));
        verify(fixture.zoneCallback, never())
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
        verify(fixture.handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void weakModernHintLegacyEventIsAcknowledgedWithoutDispatchOrReconciliationWhenModernModeActive() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        enableModernWebhookMode(fixture.handler);
        RachioEventGsonDTO legacyEvent = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STOPPED");
        legacyEvent.externalId = "external-id";
        legacyEvent.resourceId = "controller-id";
        legacyEvent.eventType = EVENT_SCHEDULE_STOPPED;
        legacyEvent.resourceType = "DEVICE";
        legacyEvent.timestamp = "2026-06-23T18:00:00Z";

        assertThat(fixture.handler.legacyWebHookEvent(legacyEvent), is(true));

        verify(fixture.deviceHandler, never()).webhookEvent(Mockito.any(RachioEventGsonDTO.class));
        verify(fixture.handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void legacyIrrigationEventDispatchesAndReconcilesWhenLegacyModeActive() {
        RachioBridgeHandler handler = legacyHandler();
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone());
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STOPPED");
        Mockito.doReturn(true).when(deviceHandler).webhookEvent(event);
        handler.rachioStatusListeners.add(deviceHandler);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void unexpectedWebhookProcessingFailurePropagatesToHttpBoundary() {
        RachioBridgeHandler handler = legacyHandler();
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone());
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STOPPED");
        RuntimeException failure = new RuntimeException("simulated processing failure");
        Mockito.doThrow(failure).when(deviceHandler).webhookEvent(event);
        handler.rachioStatusListeners.add(deviceHandler);

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> handler.webHookEvent(event));

        assertThat(thrown, is(failure));
    }

    @Test
    void legacyIrrigationEventIsAcknowledgedWithoutDispatchWhenWebhookProcessingDisabled() throws Exception {
        RachioBridgeHandler handler = disabledWebhookHandler();
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone());
        RachioEventGsonDTO event = legacyEvent("SCHEDULE_STATUS", "SCHEDULE_STOPPED");
        event.externalId = "external-id";
        event.resourceId = "controller-id";
        event.eventType = EVENT_SCHEDULE_STOPPED;
        event.resourceType = "DEVICE";
        event.timestamp = "2026-06-23T18:00:00Z";
        handler.rachioStatusListeners.add(deviceHandler);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verify(deviceHandler, never()).webhookEvent(Mockito.any(RachioEventGsonDTO.class));
        verify(handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void modernIrrigationEventDispatchesAndReconcilesWhenModernModeActive() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        enableModernWebhookMode(fixture.handler);
        RachioEventGsonDTO modernEvent = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "runType": "MANUAL",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(modernEvent), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, modernEvent);
        verify(fixture.zoneCallback)
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
    }

    @Test
    void acceptedModernWebhookEventUpdatesSafeLastEventProperties() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        enableModernWebhookMode(fixture.handler);
        RachioEventGsonDTO modernEvent = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(modernEvent), is(true));

        Map<String, String> properties = fixture.handler.getThing().getProperties();
        assertThat(properties.get(PROPERTY_LAST_WEBHOOK_EVENT_TYPE), is(EVENT_DEVICE_ZONE_RUN_STARTED));
        assertThat(properties.get(PROPERTY_LAST_WEBHOOK_EVENT_TIMESTAMP).isBlank(), is(false));
        assertThat(
                properties.values().stream().noneMatch(
                        value -> value.contains("external-id") || value.contains("https://example.org/rachio/webhook")),
                is(true));
    }

    @Test
    void modernIrrigationEventIsAcknowledgedWithoutDispatchWhenLegacyModeActive() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        enableLegacyWebhookMode(fixture.handler);
        RachioEventGsonDTO modernEvent = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(modernEvent), is(true));

        verify(fixture.deviceHandler, never()).webhookEvent(Mockito.any(RachioEventGsonDTO.class));
        verify(fixture.handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        assertThat(fixture.handler.getThing().getProperties().getOrDefault(PROPERTY_LAST_WEBHOOK_EVENT_TYPE, ""),
                is(""));
        assertThat(fixture.handler.getThing().getProperties().getOrDefault(PROPERTY_LAST_WEBHOOK_EVENT_TIMESTAMP, ""),
                is(""));
    }

    @Test
    void staleLegacyScheduleStoppedCannotOverwriteModernModeStatus() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        enableModernWebhookMode(fixture.handler);
        seedRunningSchedule(fixture.device);
        RachioEventGsonDTO staleLegacyStopped = new RachioEventGsonDTO();
        staleLegacyStopped.deviceId = "controller-id";
        staleLegacyStopped.type = "SCHEDULE_STATUS";
        staleLegacyStopped.subType = "SCHEDULE_STOPPED";

        assertThat(fixture.handler.legacyWebHookEvent(staleLegacyStopped), is(true));

        assertThat(fixture.device.currentScheduleName, is("Morning Lawn"));
        assertThat(fixture.device.currentScheduleRunning, is(true));
        verify(fixture.deviceHandler, never()).webhookEvent(Mockito.any(RachioEventGsonDTO.class));
        verify(fixture.handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void modernIrrigationEventIsAcknowledgedWithoutDispatchWhenProcessingDisabled() throws Exception {
        RachioBridgeHandler handler = disabledWebhookHandler();
        RachioDeviceHandler deviceHandler = deviceHandler(deviceWithZone());
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_SCHEDULE_STARTED, """
                "scheduleId": "schedule-id",
                "scheduleName": "Morning Lawn"
                """);
        handler.rachioStatusListeners.add(deviceHandler);

        assertThat(handler.webHookEvent(event), is(true));

        verify(deviceHandler, never()).webhookEvent(Mockito.any(RachioEventGsonDTO.class));
        verify(handler, never()).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void modernZoneStartedRebindsRegisteredZoneHandlerAfterBridgeModelRefresh() throws Exception {
        RachioBridgeHandler handler = legacyHandler();
        enableModernWebhookMode(handler);
        RachioDevice currentDevice = deviceWithZone();
        RachioZone currentZone = Objects.requireNonNull(currentDevice.getZoneByNumber(7));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(currentDevice.id, currentDevice);
        Mockito.doReturn(devices).when(handler).getDevices();

        RachioDevice staleDevice = deviceWithZone();
        RachioZone staleZone = Objects.requireNonNull(staleDevice.getZoneByNumber(7));
        Thing zoneThing = thing(THING_TYPE_ZONE, "zone");
        RachioZoneHandler zoneHandler = new RachioZoneHandler(zoneThing);
        setField(zoneHandler, "dev", staleDevice);
        setField(zoneHandler, "zone", staleZone);
        setField(zoneHandler, "cloudHandler", handler);
        ThingHandlerCallback zoneCallback = Mockito.mock(ThingHandlerCallback.class);
        zoneHandler.setCallback(zoneCallback);
        staleZone.setThingHandler(zoneHandler);

        ThingHandlerCallback deviceCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioDeviceHandler deviceHandler = deviceHandler(currentDevice, deviceCallback);
        setField(deviceHandler, "cloudHandler", handler);
        currentDevice.setThingHandler(deviceHandler);
        handler.rachioStatusListeners.add(deviceHandler);
        handler.rachioStatusListeners.add(zoneHandler);
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "runType": "MANUAL",
                "durationSeconds": "120"
                """);

        assertThat(handler.webHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler, zoneCallback);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(zoneCallback).stateUpdated(new ChannelUID(zoneThing.getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        assertThat(currentZone.getThingHandler(), is(zoneHandler));
        assertThat(currentDevice.activeZoneNumber, is(7));
    }

    @Test
    void modernZoneStartedWithoutZoneHandlerStillAcknowledgesAndReconciles() throws Exception {
        RachioBridgeHandler handler = legacyHandler();
        enableModernWebhookMode(handler);
        RachioDevice currentDevice = deviceWithZone();
        RachioZone currentZone = Objects.requireNonNull(currentDevice.getZoneByNumber(7));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(currentDevice.id, currentDevice);
        Mockito.doReturn(devices).when(handler).getDevices();
        RachioDeviceHandler deviceHandler = deviceHandler(currentDevice);
        setField(deviceHandler, "cloudHandler", handler);
        currentDevice.setThingHandler(deviceHandler);
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "runType": "MANUAL",
                "durationSeconds": "120"
                """);

        assertThat(handler.webHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        assertThat(currentZone.getThingHandler() == null, is(true));
        assertThat(currentDevice.activeZoneNumber, is(-1));
    }

    @Test
    void modernZoneStoppedClearsMatchingZoneBeforeReconciliation() throws Exception {
        verifyModernZoneEndClearsControllerSummary(EVENT_DEVICE_ZONE_RUN_STOPPED);
    }

    @Test
    void modernZoneCompletedClearsMatchingZoneAndLastWateredBeforeReconciliation() throws Exception {
        verifyModernZoneEndClearsControllerSummary(EVENT_DEVICE_ZONE_RUN_COMPLETED);
    }

    @Test
    void modernZoneStartedWithOnlyZoneNumberResolvesThroughControllerMapping() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "runType": "MANUAL",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.activeZoneNumber, is(7));
        verify(fixture.zoneCallback)
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
    }

    @Test
    void modernZoneEventForUnknownZoneTriggersReconciliationWithoutUpdatingZone() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_STARTED, """
                "zoneNumber": "8",
                "zoneName": "Back lawn",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        verify(fixture.zoneCallback, never())
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.ON);
        assertThat(fixture.device.activeZoneNumber, is(-1));
    }

    @Test
    void modernZonePausedUpdatesControllerPauseStateBeforeReconciliation() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO event = modernIrrigationEvent(EVENT_DEVICE_ZONE_RUN_PAUSED, """
                "zoneNumber": "7",
                "zoneName": "Front lawn"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.paused, is(true));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_PAUSED), OnOffType.ON);
    }

    @Test
    void modernSkipEventsUpdateSkipSummaryBeforeReconciliation() throws Exception {
        for (String eventType : Arrays.asList(EVENT_RAIN_SKIP, EVENT_CLIMATE_SKIP, EVENT_FREEZE_SKIP, EVENT_WIND_SKIP,
                EVENT_NO_SKIP)) {
            ModernWebhookFixture fixture = modernWebhookFixture();
            RachioEventGsonDTO event = modernIrrigationEvent(eventType, """
                    "scheduleId": "schedule-id",
                    "scheduleName": "Morning Lawn",
                    "plannedRunStartTime": "2026-06-19T10:00:00Z"
                    """);

            assertThat(fixture.handler.webHookEvent(event), is(true));

            verifyModernDispatchBeforeReconciliation(fixture, event);
            assertThat(fixture.device.lastSkipType, is(eventType));
            assertThat(fixture.device.lastSkipScheduleId, is("schedule-id"));
            assertThat(fixture.device.lastSkipStartTime, is("2026-06-19T10:00:00Z"));
            assertThat(fixture.device.lastApiEventType, is(eventType));
            verify(fixture.deviceCallback).stateUpdated(
                    new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_LAST_SKIP_TYPE),
                    new StringType(eventType));
            verify(fixture.deviceCallback).stateUpdated(
                    new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_LAST_SKIP_SCHEDULE_ID),
                    new StringType("schedule-id"));
            verify(fixture.deviceCallback).stateUpdated(
                    new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_LAST_SKIP_START),
                    new DateTimeType("2026-06-19T10:00:00Z"));
        }
    }

    @Test
    void modernRainSensorAndRainDelayEventsUseDeviceStatusPathBeforeReconciliation() throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        RachioEventGsonDTO rainSensorOn = modernIrrigationEvent(EVENT_RAIN_SENSOR_DETECTION_ON, "");
        RachioEventGsonDTO rainSensorOff = modernIrrigationEvent(EVENT_RAIN_SENSOR_DETECTION_OFF, "");
        RachioEventGsonDTO rainDelayOn = modernIrrigationEvent(EVENT_RAIN_DELAY_ON, """
                "durationSeconds": "900"
                """);
        RachioEventGsonDTO rainDelayOff = modernIrrigationEvent(EVENT_RAIN_DELAY_OFF, "");

        assertThat(fixture.handler.webHookEvent(rainSensorOn), is(true));
        assertThat(fixture.device.rainSensorTripped, is(true));
        assertThat(fixture.handler.webHookEvent(rainSensorOff), is(true));
        assertThat(fixture.device.rainSensorTripped, is(false));
        assertThat(fixture.handler.webHookEvent(rainDelayOn), is(true));
        assertThat(fixture.device.rainDelay, is(900));
        assertThat(fixture.handler.webHookEvent(rainDelayOff), is(true));
        assertThat(fixture.device.rainDelay, is(0));
        verify(fixture.handler, Mockito.timeout(1000).times(4))
                .refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private RachioBridgeHandler legacyHandler() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, HTTP_CLIENT));
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.callbackUrl = "https://example.org/rachio/webhook";
        try {
            setField(handler, "thingConfig", config);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.doReturn(devices).when(handler).getDevices();
        Mockito.doNothing().when(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        return handler;
    }

    private RachioBridgeHandler disabledWebhookHandler() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, HTTP_CLIENT));
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.doReturn(devices).when(handler).getDevices();
        Mockito.doNothing().when(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        return handler;
    }

    private RachioBridgeHandler cloudWebhookHandler(String id, RachioCloudWebhookRegistry registry)
            throws ReflectiveOperationException {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, id)
                .withConfiguration(
                        new Configuration(Map.of(PARAM_AUTO_CONFIGURE_WEBHOOKS, true, PARAM_USE_CLOUD_WEBHOOK, true)))
                .build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, HTTP_CLIENT, registry));
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        setField(handler, "thingConfig", config);
        Mockito.doReturn(new HashMap<>()).when(handler).getDevices();
        return handler;
    }

    private RachioBridgeHandler cloudWebhookHandler(String id, RachioCloudWebhookRegistry registry,
            ScheduledExecutorService scheduler) throws ReflectiveOperationException {
        RachioBridgeHandler handler = cloudWebhookHandler(id, registry);
        setField(handler, "webhookCoordinator", new RachioWebhookCoordinator(registry, scheduler));
        return handler;
    }

    private void runQueuedWebhookReconciliations(Queue<Runnable> reconciliations) {
        for (int task = 0; task < 10; task++) {
            Runnable reconciliation = reconciliations.poll();
            if (reconciliation == null) {
                return;
            }
            reconciliation.run();
        }
        assertThat(reconciliations.isEmpty(), is(true));
    }

    private RachioApiThrottledException throttledException(RequestPurpose requestPurpose) {
        return new RachioApiThrottledException(new RateLimitThrottleException(Priority.LOW, requestPurpose, 0.1, 0.2),
                new RachioApiResult());
    }

    private ScheduledFuture<?> mockCloudWebhookRetry(ScheduledExecutorService scheduler) {
        ScheduledFuture<?> future = Mockito.mock(ScheduledFuture.class);
        Mockito.doAnswer(invocation -> {
            long delay = invocation.getArgument(1);
            return delay > 0 && delay <= 300 ? future : null;
        }).when(scheduler).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.SECONDS));
        return future;
    }

    private List<Runnable> captureCloudWebhookRetries(ScheduledExecutorService scheduler) {
        return captureCloudWebhookRetries(scheduler, new CopyOnWriteArrayList<>());
    }

    private List<Runnable> captureCloudWebhookRetries(ScheduledExecutorService scheduler, List<Long> retryDelays) {
        List<Runnable> retries = new CopyOnWriteArrayList<>();
        Mockito.doAnswer(invocation -> {
            long delay = invocation.getArgument(1);
            // These fixtures use a 24-hour lease, so its refresh is outside the bounded retry range.
            if (delay > 0 && delay <= 300) {
                retries.add(invocation.getArgument(0, Runnable.class));
                retryDelays.add(delay);
                return Mockito.mock(ScheduledFuture.class);
            }
            return null;
        }).when(scheduler).schedule(Mockito.any(Runnable.class), Mockito.anyLong(), Mockito.eq(TimeUnit.SECONDS));
        return retries;
    }

    private boolean awaitCloudWebhookAcquisitions(Thread... workers) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (System.nanoTime() < deadline) {
            boolean allWaiting = true;
            for (Thread worker : workers) {
                boolean acquiring = Arrays.stream(worker.getStackTrace())
                        .anyMatch(frame -> RachioCloudWebhookRegistry.class.getName().equals(frame.getClassName())
                                && "acquire".equals(frame.getMethodName()));
                allWaiting &= worker.getState() == Thread.State.TIMED_WAITING && acquiring;
            }
            if (allWaiting) {
                return true;
            }
            Thread.yield();
        }
        return false;
    }

    private Runnable scheduledCloudWebhookRetry(ScheduledExecutorService scheduler) {
        ArgumentCaptor<Runnable> retryCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(retryCaptor.capture(), Mockito.eq(30L), Mockito.eq(TimeUnit.SECONDS));
        return retryCaptor.getValue();
    }

    private void enableModernWebhookMode(RachioBridgeHandler handler) throws ReflectiveOperationException {
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);
    }

    private void enableLegacyWebhookMode(RachioBridgeHandler handler) throws ReflectiveOperationException {
        RachioConfiguration config = new RachioConfiguration();
        config.callbackUrl = "https://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);
    }

    private Map<String, Object> modernHoseTimerWebhookParameters(boolean autoConfigureHoseTimerWebhooks) {
        return Map.of(PARAM_AUTO_CONFIGURE_WEBHOOKS, true, PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS,
                autoConfigureHoseTimerWebhooks, PARAM_PUBLIC_WEBHOOK_URL, "https://example.org/rachio/webhook");
    }

    private RachioConfiguration modernHoseTimerWebhookConfig() {
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.autoConfigureHoseTimerWebhooks = true;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        return config;
    }

    private void applyConfigurationAndReconcile(RachioBridgeHandler handler,
            Map<String, Object> configurationParameters, RequestPurpose requestPurpose) throws Exception {
        handler.handleConfigurationUpdate(configurationParameters);
        setField(handler, "thingConfig", RachioConfiguration
                .resolveEffectiveConfig(handler.getThing().getConfiguration().getProperties()).configuration());
        invokeConfiguredWebhookReconciliation(handler, requestPurpose);
    }

    private void invokeConfiguredWebhookReconciliation(RachioBridgeHandler handler, RequestPurpose requestPurpose)
            throws Exception {
        handler.reconcileConfiguredWebhooks(requestPurpose);
    }

    private void reconcileCloudWebhookProviderChange(RachioBridgeHandler... handlers) throws Exception {
        CompletableFuture<?>[] reconciliations = new CompletableFuture<?>[handlers.length];
        for (int i = 0; i < handlers.length; i++) {
            CompletableFuture<@Nullable Void> reconciled = new CompletableFuture<>();
            reconciliations[i] = reconciled;
            ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
            Mockito.doAnswer(invocation -> {
                Thing updatedThing = invocation.getArgument(0);
                // These bridges have no configured controllers; this state follows stale lease release.
                if ("waiting for configured controller things"
                        .equals(updatedThing.getProperties().get(PROPERTY_WEBHOOK_REGISTRATION_STATE))) {
                    reconciled.complete(null);
                }
                return null;
            }).when(callback).thingUpdated(Mockito.any(Thing.class));
            handlers[i].setCallback(callback);
        }
        for (RachioBridgeHandler handler : handlers) {
            handler.onCloudWebhookProviderChanged();
        }
        CompletableFuture.allOf(reconciliations).get(5, TimeUnit.SECONDS);
    }

    private String webhookProperty(RachioBridgeHandler handler, String propertyName) {
        handler.updateProperties();
        return handler.getThing().getProperties().getOrDefault(propertyName, "");
    }

    private RachioBridgeHandler bridgeHandlerWithMockApi(RachioApi api, Map<String, Object> initialConfiguration)
            throws ReflectiveOperationException {
        Map<String, @Nullable Object> nullableInitialConfiguration = new HashMap<>(initialConfiguration);
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge")
                .withConfiguration(new Configuration(nullableInitialConfiguration)).build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        setField(handler, "rachioApi", api);
        setField(handler, "thingConfig",
                RachioConfiguration.resolveEffectiveConfig(nullableInitialConfiguration).configuration());
        Mockito.when(api.fillProperties()).thenReturn(new HashMap<>());
        Mockito.when(api.getDevices()).thenReturn(new HashMap<>());
        return handler;
    }

    private RachioBridgeHandler cloudBridgeHandlerWithMockApi(RachioApi api,
            Supplier<@Nullable WebhookService> webhookServiceSupplier, boolean autoConfigureHoseTimerWebhooks)
            throws ReflectiveOperationException {
        Map<String, Object> initialConfiguration = Map.of(PARAM_AUTO_CONFIGURE_WEBHOOKS, true,
                PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS, autoConfigureHoseTimerWebhooks, PARAM_USE_CLOUD_WEBHOOK,
                true);
        Map<String, @Nullable Object> nullableInitialConfiguration = new HashMap<>(initialConfiguration);
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge")
                .withConfiguration(new Configuration(nullableInitialConfiguration)).build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT, webhookServiceSupplier);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        setField(handler, "rachioApi", api);
        setField(handler, "thingConfig",
                RachioConfiguration.resolveEffectiveConfig(nullableInitialConfiguration).configuration());
        Mockito.when(api.fillProperties()).thenReturn(new HashMap<>());
        Mockito.when(api.getDevices()).thenReturn(new HashMap<>());
        return handler;
    }

    private WebhookService cloudWebhookService(String url) throws Exception {
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook")).thenReturn(CompletableFuture
                .completedFuture(new Webhook(URI.create(url).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        return webhookService;
    }

    private RachioDeviceHandler addConfiguredControllerHandler(RachioBridgeHandler handler, String controllerId)
            throws ReflectiveOperationException {
        RachioDeviceHandler deviceHandler = Mockito.mock(RachioDeviceHandler.class);
        Mockito.when(deviceHandler.getBoundControllerId()).thenReturn(controllerId);
        handler.rachioStatusListeners.add(deviceHandler);
        return deviceHandler;
    }

    private void addConfiguredValveHandler(RachioBridgeHandler handler, String valveId)
            throws ReflectiveOperationException {
        RachioValveHandler valveHandler = new RachioValveHandler(thing(THING_TYPE_VALVE, "valve"));
        Mockito.when(valveHandler.getThing().getConfiguration())
                .thenReturn(new Configuration(Map.of(PROPERTY_VALVE_ID, valveId)));
        RachioValve valve = new RachioValve();
        valve.id = valveId;
        valve.name = "Valve";
        setField(valveHandler, "cloudHandler", handler);
        setField(valveHandler, "valve", valve);
        handler.smartHoseStatusListeners.add(valveHandler);
    }

    private void addConfiguredValveProgramHandler(RachioBridgeHandler handler, String programId)
            throws ReflectiveOperationException {
        RachioValveProgramHandler programHandler = new RachioValveProgramHandler(
                thing(THING_TYPE_VALVE_PROGRAM, "program"));
        Mockito.when(programHandler.getThing().getConfiguration())
                .thenReturn(new Configuration(Map.of(PROPERTY_VALVE_PROGRAM_ID, programId)));
        RachioValveProgram program = new RachioValveProgram();
        program.id = programId;
        program.name = "Program";
        setField(programHandler, "cloudHandler", handler);
        setField(programHandler, "program", program);
        handler.smartHoseStatusListeners.add(programHandler);
    }

    private ModernWebhookFixture modernWebhookFixture() throws ReflectiveOperationException {
        RachioBridgeHandler handler = legacyHandler();
        enableModernWebhookMode(handler);
        RachioDevice boundDevice = deviceWithZone();
        RachioZone zone = Objects.requireNonNull(boundDevice.getZoneByNumber(7));
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put("CONTROLLER-ID", boundDevice);
        Mockito.doReturn(devices).when(handler).getDevices();

        Thing zoneThing = thing(THING_TYPE_ZONE, "zone");
        RachioZoneHandler zoneHandler = new RachioZoneHandler(zoneThing);
        setField(zoneHandler, "dev", boundDevice);
        setField(zoneHandler, "zone", zone);
        ThingHandlerCallback zoneCallback = Mockito.mock(ThingHandlerCallback.class);
        zoneHandler.setCallback(zoneCallback);
        setField(zoneHandler, "cloudHandler", handler);
        zone.setThingHandler(zoneHandler);

        ThingHandlerCallback deviceCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioDeviceHandler deviceHandler = deviceHandler(boundDevice, deviceCallback);
        setField(deviceHandler, "cloudHandler", handler);
        boundDevice.setThingHandler(deviceHandler);
        handler.rachioStatusListeners.add(deviceHandler);
        return new ModernWebhookFixture(handler, boundDevice, deviceHandler, deviceCallback, zone, zoneHandler,
                zoneCallback);
    }

    private RachioEventGsonDTO legacyEvent(String type, String subType) {
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.deviceId = "controller-id";
        event.type = type;
        event.subType = subType;
        return event;
    }

    private void verifyLegacyZoneRunDirectHandling(String subType, String eventType, String zoneIdentity,
            OnOffType expectedState) throws Exception {
        verifyLegacyZoneRunDirectHandlingWithRunStatusState(subType, eventType, zoneIdentity, "", expectedState);
    }

    private void verifyLegacyZoneRunDirectHandlingWithRunStatusState(String subType, String eventType,
            String zoneIdentity, String runStatusState, OnOffType expectedState) throws Exception {
        RachioBridgeHandler handler = legacyHandler();
        RachioDevice boundDevice = deviceWithZone();
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(boundDevice.id, boundDevice);
        Mockito.doReturn(devices).when(handler).getDevices();
        RachioZone zone = Objects.requireNonNull(boundDevice.getZoneByNumber(7));
        if (expectedState == OnOffType.OFF) {
            boundDevice.applyActiveZoneEvent("ZONE_STARTED", 7, zone);
            boundDevice.currentScheduleName = "Quick Run";
            boundDevice.currentScheduleType = "MANUAL";
            boundDevice.currentScheduleDuration = 120;
            boundDevice.currentScheduleRunning = true;
        }
        Thing zoneThing = thing(THING_TYPE_ZONE, "zone");
        RachioZoneHandler zoneHandler = new RachioZoneHandler(zoneThing);
        setField(zoneHandler, "dev", boundDevice);
        setField(zoneHandler, "zone", zone);
        ThingHandlerCallback zoneCallback = Mockito.mock(ThingHandlerCallback.class);
        zoneHandler.setCallback(zoneCallback);
        setField(zoneHandler, "cloudHandler", handler);
        zone.setThingHandler(zoneHandler);

        ThingHandlerCallback deviceCallback = Mockito.mock(ThingHandlerCallback.class);
        RachioDeviceHandler deviceHandler = deviceHandler(boundDevice, deviceCallback);
        setField(deviceHandler, "cloudHandler", handler);
        handler.rachioStatusListeners.add(deviceHandler);
        RachioEventGsonDTO event = realShapedLegacyZoneEvent(subType, eventType, zoneIdentity, runStatusState);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        Thing deviceThing = deviceHandler.getThing();
        InOrder order = Mockito.inOrder(handler, deviceHandler, zoneCallback, deviceCallback);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(zoneCallback).stateUpdated(new ChannelUID(zoneThing.getUID(), CHANNEL_ZONE_RUN), expectedState);
        order.verify(deviceCallback).stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE),
                OnOffType.ON);
        if (expectedState == OnOffType.ON) {
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER), new DecimalType(7));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME),
                    new StringType("Front lawn"));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_NAME), new StringType("Quick Run"));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_TYPE), new StringType("MANUAL"));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_DURATION),
                    RachioQuantityTypes.seconds(120));
            order.verify(deviceCallback)
                    .stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.ON);
        } else {
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER), new DecimalType(0));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME), UnDefType.NULL);
            order.verify(deviceCallback)
                    .stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_NAME), UnDefType.NULL);
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_DURATION),
                    RachioQuantityTypes.seconds(0));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.OFF);
        }
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        verify(zoneCallback).stateUpdated(new ChannelUID(zoneThing.getUID(), CHANNEL_LAST_EVENT),
                new StringType(subType));
        verify(zoneCallback).stateUpdated(Mockito.eq(new ChannelUID(zoneThing.getUID(), CHANNEL_LAST_EVENTTS)),
                Mockito.any(DateTimeType.class));
    }

    private void verifyRegisteredControllerHandlerResolution(String type, String subType, String eventType) {
        RachioBridgeHandler handler = legacyHandler();
        RachioEventGsonDTO event = legacyEvent(type, subType);
        event.eventType = eventType;
        RachioCloudDevice boundCloudDevice = new RachioCloudDevice();
        boundCloudDevice.id = event.deviceId;
        RachioDeviceHandler deviceHandler = Mockito.spy(new RachioDeviceHandler(Mockito.mock(Thing.class)));
        deviceHandler.dev = new RachioDevice(boundCloudDevice);
        Mockito.doReturn(true).when(deviceHandler).webhookEvent(event);
        handler.rachioStatusListeners.add(deviceHandler);

        assertThat(handler.legacyWebHookEvent(event), is(true));

        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private RachioDeviceHandler deviceHandler(RachioDevice device) {
        return deviceHandler(device, Mockito.mock(ThingHandlerCallback.class));
    }

    private RachioDeviceHandler deviceHandler(RachioDevice device, ThingHandlerCallback callback) {
        RachioDeviceHandler deviceHandler = Mockito
                .spy(new RachioDeviceHandler(thing(THING_TYPE_DEVICE, "controller")));
        deviceHandler.dev = device;
        deviceHandler.thingId = device.name;
        deviceHandler.setCallback(callback);
        return deviceHandler;
    }

    private RachioDevice controllerDevice(String controllerId) {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = controllerId;
        cloudDevice.name = "Controller " + controllerId;
        cloudDevice.status = "ONLINE";
        return new RachioDevice(cloudDevice);
    }

    private RachioDevice deviceWithZone() {
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        cloudDevice.name = "Controller";
        cloudDevice.status = "ONLINE";
        RachioCloudZone cloudZone = new RachioCloudZone();
        cloudZone.id = "zone-id";
        cloudZone.name = "Front lawn";
        cloudZone.zoneNumber = 7;
        cloudDevice.zones.add(cloudZone);
        RachioCloudScheduleRule scheduleRule = new RachioCloudScheduleRule();
        scheduleRule.id = "schedule-id";
        scheduleRule.name = "Morning Lawn";
        scheduleRule.type = "FIXED";
        cloudDevice.scheduleRules.add(scheduleRule);
        return new RachioDevice(cloudDevice);
    }

    private RachioEventGsonDTO realShapedLegacyZoneEvent(String subType, String eventType, String zoneIdentity) {
        return realShapedLegacyZoneEvent(subType, eventType, zoneIdentity, "");
    }

    private RachioEventGsonDTO realShapedLegacyZoneEvent(String subType, String eventType, String zoneIdentity,
            String runStatusState) {
        return Objects.requireNonNull(new Gson().fromJson("""
                {
                  "type": "ZONE_STATUS",
                  "subType": "%s",
                  "category": "ZONE",
                  "eventType": "%s",
                  "deviceId": "controller-id",
                  "externalId": "external-id",
                  "zoneName": "Front lawn",
                  "scheduleName": "Quick Run",
                  "scheduleType": "MANUAL",
                  %s
                  "zoneRunStatus": {
                    "duration": 120,
                    "scheduleType": "MANUAL",
                    "zoneNumber": 0,
                                                                                "state": "%s"
                  }
                }
                """.formatted(subType, eventType, zoneIdentity, runStatusState), RachioEventGsonDTO.class));
    }

    private RachioEventGsonDTO modernIrrigationEvent(String eventType, String payloadFields) {
        RachioEventGsonDTO event = Objects.requireNonNull(new Gson().fromJson("""
                {
                  "eventId": "%s-id",
                  "eventType": "%s",
                  "resourceType": "IRRIGATION_CONTROLLER",
                  "resourceId": "controller-id",
                  "externalId": "external-id",
                  "timestamp": "2026-06-18T10:00:00Z",
                  "payload": {
                    %s
                  }
                }
                """.formatted(eventType, eventType, payloadFields), RachioEventGsonDTO.class));
        event.normalize();
        return event;
    }

    private void verifyModernScheduleEndClearsControllerSummary(String eventType) throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        seedRunningSchedule(fixture.device);
        RachioEventGsonDTO event = modernIrrigationEvent(eventType, """
                "scheduleId": "schedule-id",
                "scheduleName": "Morning Lawn",
                "runType": "FIXED",
                "endTime": "2026-06-18T10:10:00Z"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.currentScheduleRunning, is(false));
        assertThat(fixture.device.currentScheduleName, is(""));
        assertThat(fixture.device.activeZoneNumber, is(-1));
        assertThat(fixture.device.lastApiEventType, is(eventType));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING),
                OnOffType.OFF);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_NAME),
                UnDefType.NULL);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                new DecimalType(0));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_LAST_API_EVENT_TYPE),
                new StringType(eventType));
    }

    private void verifyModernZoneEndClearsControllerSummary(String eventType) throws Exception {
        ModernWebhookFixture fixture = modernWebhookFixture();
        seedRunningSchedule(fixture.device);
        fixture.device.applyActiveZoneEvent("ZONE_STARTED", 7, fixture.zone);
        RachioEventGsonDTO event = modernIrrigationEvent(eventType, """
                "zoneId": "zone-id",
                "zoneNumber": "7",
                "zoneName": "Front lawn",
                "endTime": "2026-06-18T10:02:00Z",
                "durationSeconds": "120"
                """);

        assertThat(fixture.handler.webHookEvent(event), is(true));

        verifyModernDispatchBeforeReconciliation(fixture, event);
        assertThat(fixture.device.currentScheduleRunning, is(false));
        assertThat(fixture.device.activeZoneNumber, is(-1));
        assertThat(fixture.zone.lastWateredDate, is(Instant.parse("2026-06-18T10:02:00Z").toEpochMilli()));
        verify(fixture.zoneCallback)
                .stateUpdated(new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_RUN), OnOffType.OFF);
        verify(fixture.zoneCallback).stateUpdated(
                new ChannelUID(fixture.zoneHandler.getThing().getUID(), CHANNEL_ZONE_LAST_WATERED_DATE),
                new DateTimeType("2026-06-18T10:02:00Z"));
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING),
                OnOffType.OFF);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                new DecimalType(0));
    }

    private void seedRunningSchedule(RachioDevice device) {
        device.currentScheduleId = "schedule-id";
        device.currentScheduleName = "Morning Lawn";
        device.currentScheduleType = "FIXED";
        device.currentScheduleStartTime = "2026-06-18T10:00:00Z";
        device.currentScheduleEndTime = "2026-06-18T10:10:00Z";
        device.currentScheduleDuration = 600;
        device.currentScheduleRunning = true;
        device.activeZoneNumber = 7;
        device.activeZoneName = "Front lawn";
        device.activeZoneId = "zone-id";
    }

    private Thing thing(ThingTypeUID thingType, String id) {
        Thing thing = Mockito.mock(Thing.class);
        Mockito.when(thing.getUID()).thenReturn(new ThingUID(thingType, "bridge", id));
        return thing;
    }

    private void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Class<?> type = target.getClass();
        Field field = null;
        while (type != null && field == null) {
            try {
                field = type.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                type = type.getSuperclass();
            }
        }
        if (field == null) {
            throw new NoSuchFieldException(fieldName);
        }
        field.setAccessible(true);
        field.set(target, value);
    }

    private RachioDeviceHandler directlyHandledDeviceHandler(RachioBridgeHandler handler, RachioEventGsonDTO event,
            boolean handled) {
        RachioCloudDevice boundCloudDevice = new RachioCloudDevice();
        boundCloudDevice.id = "controller-id";
        RachioDeviceHandler deviceHandler = Mockito.spy(new RachioDeviceHandler(Mockito.mock(Thing.class)));
        deviceHandler.dev = new RachioDevice(boundCloudDevice);
        Mockito.doReturn(handled).when(deviceHandler).webhookEvent(event);
        handler.rachioStatusListeners.add(deviceHandler);
        return deviceHandler;
    }

    private void verifyDispatchBeforeReconciliation(RachioBridgeHandler handler, RachioDeviceHandler deviceHandler,
            RachioEventGsonDTO event) {
        InOrder order = Mockito.inOrder(handler, deviceHandler);
        order.verify(deviceHandler).webhookEvent(event);
        order.verify(handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private void verifyModernDispatchBeforeReconciliation(ModernWebhookFixture fixture, RachioEventGsonDTO event) {
        InOrder order = Mockito.inOrder(fixture.handler, fixture.deviceHandler);
        order.verify(fixture.deviceHandler).webhookEvent(event);
        order.verify(fixture.handler, Mockito.timeout(1000)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private record ModernWebhookFixture(RachioBridgeHandler handler, RachioDevice device,
            RachioDeviceHandler deviceHandler, ThingHandlerCallback deviceCallback, RachioZone zone,
            RachioZoneHandler zoneHandler, ThingHandlerCallback zoneCallback) {
    }
}
