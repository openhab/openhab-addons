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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_DURATION;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_END;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_RUNNING;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_CURRENT_SCHEDULE_TYPE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE_ZONE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE_ZONE_NAME;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_DEVICE_PAUSED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_API_EVENT_TYPE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_EVENT;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_EVENTTS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_SKIP_SCHEDULE_ID;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_SKIP_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_LAST_SKIP_TYPE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_SCHEDULE_LAST_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_LAST_WATERED_DATE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.CHANNEL_ZONE_RUN;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_CLIMATE_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_COMPLETED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_PAUSED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_DEVICE_ZONE_RUN_STOPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_FREEZE_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_NO_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CANCELED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_PROGRAM_RAIN_SKIP_CREATED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_RAIN_DELAY_OFF;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_RAIN_DELAY_ON;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_RAIN_SENSOR_DETECTION_OFF;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_RAIN_SENSOR_DETECTION_ON;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_RAIN_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_SCHEDULE_COMPLETED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_SCHEDULE_STARTED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_SCHEDULE_STOPPED;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_END;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_VALVE_RUN_START;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.EVENT_WIND_SKIP;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_AUTO_CONFIGURE_WEBHOOKS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_PUBLIC_WEBHOOK_URL;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_USE_CLOUD_WEBHOOK;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_DEVICE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_SCHEDULE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_VALVE_PROGRAM;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_ZONE;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.RachioZone;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudScheduleRule;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValve;
import org.openhab.binding.rachio.internal.api.json.RachioSmartHoseTimerGsonDTO.RachioValveProgram;
import org.openhab.binding.rachio.internal.api.json.RachioZoneGsonDTO.RachioCloudZone;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookTarget;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
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
 * Tests Cloud Connector configuration update behavior.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioBridgeHandlerConfigurationTest {
    @Test
    void webhookModeSelectionKeepsHoseTimerWebhooksOptIn() {
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true),
                is(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true, true,
                true), is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true, true,
                false), is(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true, true, true, true),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true, false, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true, true, true, false),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true, true, true, true),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, false),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.LIGHTING_CONTROLLER, true, true,
                true, true), is(RachioWebhookMode.DISABLED));
        assertThat(
                RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.LIGHTING_ZONE, true, true, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(
                RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.LIGHTING_SCENE, true, true, true, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.LIGHTING_PROGRAM, true, true, true,
                true), is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.UNKNOWN, true, true, true, true),
                is(RachioWebhookMode.DISABLED));
    }

    @Test
    void manualModernWebhookUrlIsUsedOnlyWhenAutoConfigurationIsEnabled() throws Exception {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, () -> webhookService);
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(handler.getModernWebhookUrlForRegistration(), is("https://example.org/rachio/webhook"));
        assertThat(bridge.getProperties().containsKey("rachioWebhookUrl"), is(false));
        assertThat(bridge.getProperties().values().stream()
                .noneMatch(value -> value.contains("https://example.org/rachio/webhook")), is(true));
        verify(webhookService, never()).requestWebhook("/rachio/webhook");
    }

    @Test
    void manualModernWebhookUrlRejectsHttp() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "http://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
    }

    @Test
    void manualModernWebhookUrlRejectsNonWebScheme() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "ftp://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
    }

    @Test
    void invalidManualModernWebhookUrlDoesNotRegisterWebhook() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
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
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
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
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
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
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
        RachioApi api = Mockito.mock(RachioApi.class);
        setField(handler, "rachioApi", api);
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.publicWebhookUrl = "https://example.org/rachio/webhook";
        setField(handler, "thingConfig", config);

        handler.registerValveWebHook("valve-id", RequestPurpose.INITIALIZATION);
        handler.registerValveProgramWebHook("program-id", RequestPurpose.INITIALIZATION);

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void enablingHoseTimerWebhooksAtRuntimeRegistersExistingValveWithRefreshedConfig() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        addConfiguredValveHandler(handler, "valve-id");
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.handleConfigurationUpdate(modernHoseTimerWebhookParameters(true));

        verify(api).registerWebHookTarget(targetCaptor.capture(), Mockito.eq("https://example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.USER_COMMAND));
        RachioWebhookTarget target = targetCaptor.getValue();
        assertThat(target.getResourceType(), is(RachioWebhookResourceType.VALVE));
        assertThat(target.getResourceId(), is("valve-id"));
    }

    @Test
    void enablingHoseTimerWebhooksAtRuntimeRegistersExistingValveProgramWithRefreshedConfig() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        addConfiguredValveProgramHandler(handler, "program-id");
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.handleConfigurationUpdate(modernHoseTimerWebhookParameters(true));

        verify(api).registerWebHookTarget(targetCaptor.capture(), Mockito.eq("https://example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.USER_COMMAND));
        RachioWebhookTarget target = targetCaptor.getValue();
        assertThat(target.getResourceType(), is(RachioWebhookResourceType.PROGRAM));
        assertThat(target.getResourceId(), is("program-id"));
    }

    @Test
    void childConfigurationUpdateSeesRefreshedBridgeWebhookConfig() throws Exception {
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

        verify(listener).onConfigurationUpdated();
        assertThat(observedValveMode[0], is(RachioWebhookMode.WEBHOOK_SERVICE));
    }

    @Test
    void disablingHoseTimerWebhooksAtRuntimeDoesNotRegisterValveOrProgramWebhooks() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, modernHoseTimerWebhookParameters(true));
        addConfiguredValveHandler(handler, "valve-id");
        addConfiguredValveProgramHandler(handler, "program-id");

        handler.handleConfigurationUpdate(Map.of(PARAM_AUTO_CONFIGURE_HOSE_TIMER_WEBHOOKS, false));

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
    }

    @Test
    void controllerWebhookReconciliationStillRunsAfterConfigurationOrderingChange() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        RachioBridgeHandler handler = bridgeHandlerWithMockApi(api, Map.of());
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.when(api.getDevices()).thenReturn(devices);

        handler.handleConfigurationUpdate(modernHoseTimerWebhookParameters(false));

        verify(api).registerWebHook(Mockito.eq("controller-id"), Mockito.eq("https://example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.USER_COMMAND));
    }

    @Test
    void webhookServiceAvailabilityRegistersExistingValveWebhook() throws Exception {
        RachioApi api = Mockito.mock(RachioApi.class);
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>();
        RachioBridgeHandler handler = cloudBridgeHandlerWithMockApi(api, webhookService::get, true);
        addConfiguredValveHandler(handler, "valve-id");
        webhookService.set(cloudWebhookService("https://cloud.example.org/rachio/webhook"));
        ArgumentCaptor<RachioWebhookTarget> targetCaptor = ArgumentCaptor.forClass(RachioWebhookTarget.class);

        handler.onWebhookServiceChanged();

        verify(api).registerWebHookTarget(targetCaptor.capture(),
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

        handler.onWebhookServiceChanged();

        verify(api).registerWebHookTarget(targetCaptor.capture(),
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

        handler.onWebhookServiceChanged();

        verify(api, never()).registerWebHookTarget(Mockito.any(RachioWebhookTarget.class), Mockito.anyString(),
                Mockito.anyString(), Mockito.anyString(), Mockito.any(), Mockito.any(), Mockito.any());
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
        webhookService.set(cloudWebhookService("https://cloud.example.org/rachio/webhook"));

        handler.onWebhookServiceChanged();

        verify(api).registerWebHook(Mockito.eq("controller-id"), Mockito.eq("https://cloud.example.org/rachio/webhook"),
                Mockito.eq(""), Mockito.eq(""), Mockito.isNull(), Mockito.eq(false),
                Mockito.eq(RequestPurpose.BACKGROUND_REFRESH));
        handler.dispose();
    }

    @Test
    void cloudWebhookServiceUnavailableLeavesModernWebhookUrlEmpty() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                () -> null);
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        setField(handler, "thingConfig", config);

        assertThat(handler.getWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER),
                is(RachioWebhookMode.WEBHOOK_SERVICE));
        assertThat(handler.getModernWebhookUrlForRegistration(), is(""));
    }

    @Test
    void cloudWebhookServiceProvidesModernWebhookUrlAndSchedulesRefresh() throws Exception {
        String cloudWebhookUrl = "https://cloud.example.org/rachio/webhook";
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook")).thenReturn(CompletableFuture
                .completedFuture(new Webhook(new URL(cloudWebhookUrl), Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, () -> webhookService);
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

        ScheduledFuture<?> refreshJob = getField(handler, "cloudWebhookRefreshJob");
        assertThat(refreshJob != null && !refreshJob.isDone(), is(true));
        handler.dispose();
    }

    @Test
    void sharedCloudWebhookUrlIsRemovedOnlyAfterLastActiveBridgeDisposes() throws Exception {
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook")).thenReturn(
                CompletableFuture.completedFuture(new Webhook(new URL("https://cloud.example.org/rachio/webhook"),
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
        Mockito.when(webhookService.requestWebhook("/rachio/webhook")).thenReturn(
                CompletableFuture.completedFuture(new Webhook(new URL("https://cloud.example.org/rachio/webhook"),
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

        assertThat(registry.activeConsumerCount(), is(1));
        verify(webhookService, never()).removeWebhook("/rachio/webhook");

        secondHandler.dispose();

        assertThat(registry.activeConsumerCount(), is(0));
        verify(webhookService, Mockito.times(1)).removeWebhook("/rachio/webhook");
    }

    @Test
    void cloudWebhookExpiryRefreshIsSharedAcrossBridgeHandlers() throws Exception {
        String cloudWebhookUrl = "https://cloud.example.org/rachio/webhook";
        WebhookService webhookService = Mockito.mock(WebhookService.class);
        Mockito.when(webhookService.requestWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(cloudWebhookUrl).toURL(), Instant.now().plusSeconds(2 * 60 * 60))))
                .thenReturn(CompletableFuture.completedFuture(
                        new Webhook(URI.create(cloudWebhookUrl).toURL(), Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(() -> webhookService);
        RachioBridgeHandler firstHandler = cloudWebhookHandler("first", registry);
        RachioBridgeHandler secondHandler = cloudWebhookHandler("second", registry);

        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is(cloudWebhookUrl));
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is(cloudWebhookUrl));
        verify(webhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");

        firstHandler.onWebhookServiceChanged();
        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is(cloudWebhookUrl));
        secondHandler.onWebhookServiceChanged();
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is(cloudWebhookUrl));

        verify(webhookService, Mockito.times(2)).requestWebhook("/rachio/webhook");
        firstHandler.dispose();
        secondHandler.dispose();
    }

    @Test
    void sharedCloudWebhookRecoversAfterServiceUnavailabilityAndIdentityChange() throws Exception {
        WebhookService firstWebhookService = cloudWebhookService("https://cloud.example.org/rachio/first");
        WebhookService secondWebhookService = cloudWebhookService("https://cloud.example.org/rachio/second");
        AtomicReference<@Nullable WebhookService> webhookService = new AtomicReference<>(firstWebhookService);
        RachioCloudWebhookRegistry registry = new RachioCloudWebhookRegistry(webhookService::get);
        RachioBridgeHandler firstHandler = cloudWebhookHandler("first", registry);
        RachioBridgeHandler secondHandler = cloudWebhookHandler("second", registry);

        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/first"));
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/first"));

        webhookService.set(null);
        registry.clearCachedWebhook();
        firstHandler.onWebhookServiceChanged();
        secondHandler.onWebhookServiceChanged();
        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is(""));
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is(""));

        webhookService.set(secondWebhookService);
        registry.clearCachedWebhook();
        firstHandler.onWebhookServiceChanged();
        secondHandler.onWebhookServiceChanged();
        assertThat(firstHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/second"));
        assertThat(secondHandler.getModernWebhookUrlForRegistration(), is("https://cloud.example.org/rachio/second"));

        verify(firstWebhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
        verify(secondWebhookService, Mockito.times(1)).requestWebhook("/rachio/webhook");
        firstHandler.dispose();
        secondHandler.dispose();
    }

    @Test
    void modernWebhookUrlRejectsUserInfoCredentials() throws Exception {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build());
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        verify(deviceCallback, never()).stateUpdated(
                new ChannelUID(deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                new DecimalType(7));
        verify(deviceCallback, never()).stateUpdated(
                new ChannelUID(deviceHandler.getThing().getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.ON);
    }

    @Test
    void legacyEventForUnknownControllerIsRejectedWithoutRefresh() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge));
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
    void zoneLogSubjectDoesNotDuplicateZonePrefix() throws Exception {
        RachioZoneHandler zoneHandler = new RachioZoneHandler(thing(THING_TYPE_ZONE, "zone"));
        Method method = RachioZoneHandler.class.getDeclaredMethod("getZoneLogSubject", String.class);
        method.setAccessible(true);

        assertThat((String) method.invoke(zoneHandler, "Zone 7"), is("Zone 7"));
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
        verify(fixture.handler, Mockito.times(4)).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    @Test
    void scheduledCorePollUsesMediumPriority() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge);

        assertThat(handler.getRefreshPriority(RefreshReason.SCHEDULED_POLL), is(PRIORITY.MED));
        assertThat(handler.getRequestPurpose(RefreshReason.SCHEDULED_POLL), is(RequestPurpose.CORE_STATUS_POLL));
    }

    @Test
    void handleConfigurationUpdatePersistsCloudThingConfiguration() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge")
                .withConfiguration(new Configuration(Map.of(PARAM_FORECAST_UNITS, "METRIC"))).build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleConfigurationUpdate(Map.of(PARAM_FORECAST_UNITS, "US"));

        assertThat(bridge.getConfiguration().get(PARAM_FORECAST_UNITS), is("US"));
        verify(callback).thingUpdated(bridge);
    }

    private RachioBridgeHandler legacyHandler() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge));
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
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge));
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
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, registry));
        handler.setCallback(Mockito.mock(ThingHandlerCallback.class));
        RachioConfiguration config = new RachioConfiguration();
        config.autoConfigureWebhooks = true;
        config.useCloudWebhook = true;
        setField(handler, "thingConfig", config);
        Mockito.doReturn(new HashMap<>()).when(handler).getDevices();
        return handler;
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

    private RachioBridgeHandler bridgeHandlerWithMockApi(RachioApi api, Map<String, Object> initialConfiguration)
            throws ReflectiveOperationException {
        Map<String, @Nullable Object> nullableInitialConfiguration = new HashMap<>(initialConfiguration);
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge")
                .withConfiguration(new Configuration(nullableInitialConfiguration)).build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge);
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
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, webhookServiceSupplier);
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
        Mockito.when(webhookService.requestWebhook("/rachio/webhook")).thenReturn(
                CompletableFuture.completedFuture(new Webhook(new URL(url), Instant.now().plusSeconds(24 * 60 * 60))));
        Mockito.when(webhookService.removeWebhook("/rachio/webhook"))
                .thenReturn(CompletableFuture.completedFuture(null));
        return webhookService;
    }

    private void addConfiguredValveHandler(RachioBridgeHandler handler, String valveId)
            throws ReflectiveOperationException {
        RachioValveHandler valveHandler = new RachioValveHandler(thing(THING_TYPE_VALVE, "valve"));
        RachioValve valve = new RachioValve();
        valve.id = valveId;
        valve.name = "Valve";
        setField(valveHandler, "cloudHandler", handler);
        setField(valveHandler, "valve", valve);
        handler.rachioStatusListeners.add(valveHandler);
    }

    private void addConfiguredValveProgramHandler(RachioBridgeHandler handler, String programId)
            throws ReflectiveOperationException {
        RachioValveProgramHandler programHandler = new RachioValveProgramHandler(
                thing(THING_TYPE_VALVE_PROGRAM, "program"));
        RachioValveProgram program = new RachioValveProgram();
        program.id = programId;
        program.name = "Program";
        setField(programHandler, "cloudHandler", handler);
        setField(programHandler, "program", program);
        handler.rachioStatusListeners.add(programHandler);
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
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER), UnDefType.NULL);
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NAME), UnDefType.NULL);
            order.verify(deviceCallback)
                    .stateUpdated(new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_NAME), UnDefType.UNDEF);
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_DURATION),
                    RachioQuantityTypes.seconds(0));
            order.verify(deviceCallback).stateUpdated(
                    new ChannelUID(deviceThing.getUID(), CHANNEL_CURRENT_SCHEDULE_RUNNING), OnOffType.OFF);
        }
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
                UnDefType.UNDEF);
        verify(fixture.deviceCallback).stateUpdated(
                new ChannelUID(fixture.deviceHandler.getThing().getUID(), CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER),
                UnDefType.NULL);
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
                UnDefType.NULL);
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

    @SuppressWarnings("unchecked")
    private <T> T getField(Object target, String fieldName) throws ReflectiveOperationException {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        return (T) field.get(target);
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
        order.verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private void verifyModernDispatchBeforeReconciliation(ModernWebhookFixture fixture, RachioEventGsonDTO event) {
        InOrder order = Mockito.inOrder(fixture.handler, fixture.deviceHandler);
        order.verify(fixture.deviceHandler).webhookEvent(event);
        order.verify(fixture.handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
    }

    private record ModernWebhookFixture(RachioBridgeHandler handler, RachioDevice device,
            RachioDeviceHandler deviceHandler, ThingHandlerCallback deviceCallback, RachioZone zone,
            RachioZoneHandler zoneHandler, ThingHandlerCallback zoneCallback) {
    }
}
