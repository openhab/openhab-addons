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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.*;

import java.lang.reflect.Field;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.RachioConfiguration;
import org.openhab.binding.rachio.internal.api.RachioApi;
import org.openhab.binding.rachio.internal.api.RachioApiException;
import org.openhab.binding.rachio.internal.api.RachioApiResult;
import org.openhab.binding.rachio.internal.api.RachioApiThrottledException;
import org.openhab.binding.rachio.internal.api.json.RachioPropertyGsonDTO.RachioProperty;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.Priority;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests Cloud Connector configuration and communication status behavior.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class RachioBridgeHandlerConfigurationTest {
    private static final HttpClient HTTP_CLIENT = Mockito.mock(HttpClient.class);

    @Test
    void smartHoseTimeZoneUsesAndCachesPropertyTimeZone() throws RachioApiException {
        RachioBridgeHandler handler = Mockito
                .spy(new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(), HTTP_CLIENT,
                        () -> null, () -> ZoneId.of("Europe/Budapest")));
        RachioProperty property = new RachioProperty();
        property.timeZone = "America/Denver";
        Mockito.doReturn(property).when(handler).findPropertyForBaseStation("base-station-id");

        assertThat(handler.getSmartHoseTimeZone("base-station-id"), is(ZoneId.of("America/Denver")));
        assertThat(handler.getSmartHoseTimeZone("base-station-id"), is(ZoneId.of("America/Denver")));
        verify(handler).findPropertyForBaseStation("base-station-id");
    }

    @Test
    void scheduledCorePollUsesMediumPriority() {
        RachioBridgeHandler handler = new RachioBridgeHandler(BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build(),
                HTTP_CLIENT);

        assertThat(handler.getRefreshPriority(RefreshReason.SCHEDULED_POLL), is(Priority.MEDIUM));
        assertThat(handler.getRequestPurpose(RefreshReason.SCHEDULED_POLL), is(RequestPurpose.CORE_STATUS_POLL));
    }

    @Test
    void handleConfigurationUpdatePersistsCloudThingConfiguration() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge")
                .withConfiguration(new Configuration(Map.of(PARAM_FORECAST_UNITS, "METRIC"))).build();
        RachioBridgeHandler handler = new RachioBridgeHandler(bridge, HTTP_CLIENT);
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.handleConfigurationUpdate(Map.of(PARAM_FORECAST_UNITS, "US"));

        assertThat(bridge.getConfiguration().get(PARAM_FORECAST_UNITS), is("US"));
        verify(callback).thingUpdated(bridge);
    }

    @Test
    void pollingFailureMarksBridgeOfflineAndNextSuccessRestoresOnline() throws Exception {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, HTTP_CLIENT));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        RachioApi activeApi = activeApi();
        RachioApi failingApi = Mockito.mock(RachioApi.class);
        RachioApi successfulApi = successfulRefreshApi();
        RachioConfiguration configuration = configurationWithApiKey();
        setField(handler, "rachioApi", activeApi);
        setField(handler, "thingConfig", configuration);
        Mockito.doReturn(failingApi, successfulApi).when(handler).createRachioApi("person-id");
        Mockito.doThrow(new RachioApiException("refresh failed")).when(failingApi).initialize("api-key",
                bridge.getUID(), Priority.MEDIUM, RequestPurpose.CORE_STATUS_POLL);

        handler.refreshDeviceStatus(RefreshReason.SCHEDULED_POLL);
        handler.refreshDeviceStatus(RefreshReason.SCHEDULED_POLL);

        verify(callback).statusUpdated(eq(bridge), argThat(status -> status.getStatus() == ThingStatus.OFFLINE
                && status.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        verify(callback).statusUpdated(eq(bridge), argThat(status -> status.getStatus() == ThingStatus.ONLINE));
    }

    @Test
    void localRateLimitDeferralDoesNotMarkBridgeOffline() throws Exception {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge, HTTP_CLIENT));
        ThingHandlerCallback callback = Mockito.mock(ThingHandlerCallback.class);
        handler.setCallback(callback);
        RachioApi activeApi = activeApi();
        RachioApi deferredApi = Mockito.mock(RachioApi.class);
        RachioApiThrottledException throttled = Mockito.mock(RachioApiThrottledException.class);
        setField(handler, "rachioApi", activeApi);
        setField(handler, "thingConfig", configurationWithApiKey());
        Mockito.doReturn(deferredApi).when(handler).createRachioApi("person-id");
        Mockito.doThrow(throttled).when(deferredApi).initialize("api-key", bridge.getUID(), Priority.MEDIUM,
                RequestPurpose.CORE_STATUS_POLL);

        handler.refreshDeviceStatus(RefreshReason.SCHEDULED_POLL);

        verify(callback, never()).statusUpdated(eq(bridge),
                argThat(status -> status.getStatus() == ThingStatus.OFFLINE));
    }

    private RachioApi activeApi() {
        RachioApi api = Mockito.mock(RachioApi.class);
        Mockito.when(api.getPersonId()).thenReturn("person-id");
        Mockito.when(api.getDevices()).thenReturn(new HashMap<>());
        return api;
    }

    private RachioApi successfulRefreshApi() throws RachioApiException {
        RachioApi api = Mockito.mock(RachioApi.class);
        Mockito.when(api.getLastApiResult()).thenReturn(new RachioApiResult());
        Mockito.when(api.getDevices()).thenReturn(new HashMap<>());
        return api;
    }

    private RachioConfiguration configurationWithApiKey() {
        RachioConfiguration configuration = new RachioConfiguration();
        configuration.apikey = "api-key";
        return configuration;
    }

    private void setField(Object target, String fieldName, Object value) throws ReflectiveOperationException {
        Field field = RachioBridgeHandler.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
