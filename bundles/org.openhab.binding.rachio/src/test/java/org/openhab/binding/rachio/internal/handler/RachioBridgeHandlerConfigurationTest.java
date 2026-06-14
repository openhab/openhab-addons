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
import static org.mockito.Mockito.verify;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.PARAM_FORECAST_UNITS;
import static org.openhab.binding.rachio.internal.RachioBindingConstants.THING_TYPE_CLOUD;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.rachio.internal.api.RachioDevice;
import org.openhab.binding.rachio.internal.api.json.RachioDeviceGsonDTO.RachioCloudDevice;
import org.openhab.binding.rachio.internal.api.json.RachioEventGsonDTO;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookMode;
import org.openhab.binding.rachio.internal.api.webhook.RachioWebhookResourceType;
import org.openhab.binding.rachio.internal.handler.RachioBridgeHandler.RefreshReason;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.PRIORITY;
import org.openhab.binding.rachio.internal.utils.ClientRateLimitManager.RequestPurpose;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests Cloud Connector configuration update behavior.
 *
 * @author openHAB Contributors - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
class RachioBridgeHandlerConfigurationTest {
    @Test
    void controllerUsesLegacyNotificationServiceWhileSmartHoseResourcesRemainPollingOnly() {
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, true),
                is(RachioWebhookMode.LEGACY_NOTIFICATION_SERVICE));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.VALVE, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.PROGRAM, true),
                is(RachioWebhookMode.DISABLED));
        assertThat(RachioBridgeHandler.selectWebhookMode(RachioWebhookResourceType.IRRIGATION_CONTROLLER, false),
                is(RachioWebhookMode.DISABLED));
    }

    @Test
    void validLegacyEventTriggersEssentialWebhookReconciliationRefresh() {
        Bridge bridge = BridgeBuilder.create(THING_TYPE_CLOUD, "bridge").build();
        RachioBridgeHandler handler = Mockito.spy(new RachioBridgeHandler(bridge));
        RachioCloudDevice cloudDevice = new RachioCloudDevice();
        cloudDevice.id = "controller-id";
        HashMap<String, RachioDevice> devices = new HashMap<>();
        devices.put(cloudDevice.id, new RachioDevice(cloudDevice));
        Mockito.doReturn(devices).when(handler).getDevices();
        Mockito.doNothing().when(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
        RachioEventGsonDTO event = new RachioEventGsonDTO();
        event.deviceId = cloudDevice.id;
        event.type = "ZONE_STATUS";
        event.subType = "ZONE_STARTED";

        assertThat(handler.legacyWebHookEvent(event), is(true));

        verify(handler).refreshDeviceStatus(RefreshReason.WEBHOOK_RECONCILIATION);
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
}
