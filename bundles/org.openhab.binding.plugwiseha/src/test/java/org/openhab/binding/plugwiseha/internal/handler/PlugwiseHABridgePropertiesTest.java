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
package org.openhab.binding.plugwiseha.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.plugwiseha.internal.PlugwiseHABindingConstants.THING_TYPE_GATEWAY;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openhab.binding.plugwiseha.internal.api.exception.PlugwiseHAException;
import org.openhab.binding.plugwiseha.internal.api.model.PlugwiseHAController;
import org.openhab.binding.plugwiseha.internal.api.model.dto.GatewayInfo;
import org.openhab.binding.plugwiseha.internal.api.xml.PlugwiseHAXStream;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Tests gateway properties during initial polling and communication recovery.
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
class PlugwiseHABridgePropertiesTest {

    @Test
    void firstSuccessfulPollPublishesGatewayProperties() throws Exception {
        try (Fixture fixture = fixture()) {
            GatewayInfo info = gatewayInfo("4.0");
            when(fixture.controller().getGatewayInfo()).thenReturn(info);

            fixture.poll().run();

            assertEquals(ThingStatus.ONLINE, fixture.bridge().getStatus());
            assertGatewayProperties(fixture.bridge(), "4.0");
        }
    }

    @Test
    void successfulRetryPublishesPropertiesAfterInitialConnectionFailure() throws Exception {
        try (Fixture fixture = fixture()) {
            GatewayInfo info = gatewayInfo("4.0");
            when(fixture.controller().getGatewayInfo()).thenReturn(info);
            doThrow(new PlugwiseHAException("Gateway unavailable")).doNothing().when(fixture.controller()).refresh();

            fixture.poll().run();
            assertEquals(ThingStatus.OFFLINE, fixture.bridge().getStatus());
            assertNull(fixture.bridge().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));

            fixture.poll().run();

            assertEquals(ThingStatus.ONLINE, fixture.bridge().getStatus());
            assertGatewayProperties(fixture.bridge(), "4.0");
        }
    }

    @Test
    void optionalMetadataFailureDoesNotTakeBridgeOfflineAndIsRetried() throws Exception {
        try (Fixture fixture = fixture()) {
            GatewayInfo info = gatewayInfo("4.0");
            when(fixture.controller().getGatewayInfo()).thenThrow(new PlugwiseHAException("Metadata unavailable"))
                    .thenReturn(info);

            fixture.poll().run();
            assertEquals(ThingStatus.ONLINE, fixture.bridge().getStatus());
            assertNull(fixture.bridge().getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION));

            fixture.poll().run();

            assertEquals(ThingStatus.ONLINE, fixture.bridge().getStatus());
            assertGatewayProperties(fixture.bridge(), "4.0");
        }
    }

    @Test
    void laterPollUpdatesChangedGatewayProperties() throws Exception {
        try (Fixture fixture = fixture()) {
            GatewayInfo original = gatewayInfo("4.0");
            GatewayInfo updated = gatewayInfo("4.1");
            when(fixture.controller().getGatewayInfo()).thenReturn(original, updated);

            fixture.poll().run();
            assertGatewayProperties(fixture.bridge(), "4.0");
            fixture.poll().run();

            assertGatewayProperties(fixture.bridge(), "4.1");
        }
    }

    @Test
    void disposedHandlerDoesNotFetchGatewayProperties() throws Exception {
        try (Fixture fixture = fixture()) {
            fixture.handler().dispose();

            fixture.poll().run();

            verifyNoInteractions(fixture.controller());
        }
    }

    private Fixture fixture() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        ScheduledFuture<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).scheduleWithFixedDelay(any(Runnable.class), eq(0L), eq(15L),
                eq(TimeUnit.SECONDS));
        Bridge bridge = BridgeBuilder.create(THING_TYPE_GATEWAY, "test")
                .withConfiguration(new Configuration(Map.of("smileId", "abcdefgh")))
                .withProperties(Map.of("custom", "retained")).build();
        bridge.setStatusInfo(ThingStatusInfoBuilder.create(ThingStatus.INITIALIZING).build());
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(Thing.class), any());

        try (MockedStatic<ThreadPoolManager> pools = mockStatic(ThreadPoolManager.class);
                MockedConstruction<PlugwiseHAController> controllers = mockConstruction(PlugwiseHAController.class)) {
            pools.when(() -> ThreadPoolManager.getScheduledPool("thingHandler")).thenReturn(scheduler);
            PlugwiseHABridgeHandler handler = new PlugwiseHABridgeHandler(bridge, mock(HttpClient.class));
            handler.setCallback(callback);
            handler.initialize();
            ArgumentCaptor<Runnable> poll = ArgumentCaptor.forClass(Runnable.class);
            verify(scheduler).scheduleWithFixedDelay(poll.capture(), eq(0L), eq(15L), eq(TimeUnit.SECONDS));
            return new Fixture(handler, bridge, controllers.constructed().getFirst(), poll.getValue());
        }
    }

    private GatewayInfo gatewayInfo(String firmware) {
        return (GatewayInfo) new PlugwiseHAXStream().fromXML("""
                <gateway>
                    <firmware_version>%s</firmware_version><hardware_version>hardware-1</hardware_version>
                    <mac_address>00:11:22:33:44:55</mac_address><vendor_name>Plugwise</vendor_name>
                    <vendor_model>Adam HA</vendor_model>
                </gateway>
                """.formatted(firmware));
    }

    private void assertGatewayProperties(Bridge bridge, String firmware) {
        assertAll(() -> assertEquals(firmware, bridge.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION)),
                () -> assertEquals("hardware-1", bridge.getProperties().get(Thing.PROPERTY_HARDWARE_VERSION)),
                () -> assertEquals("00:11:22:33:44:55", bridge.getProperties().get(Thing.PROPERTY_MAC_ADDRESS)),
                () -> assertEquals("Plugwise", bridge.getProperties().get(Thing.PROPERTY_VENDOR)),
                () -> assertEquals("Adam HA", bridge.getProperties().get(Thing.PROPERTY_MODEL_ID)),
                () -> assertEquals("retained", bridge.getProperties().get("custom")));
    }

    private record Fixture(PlugwiseHABridgeHandler handler, Bridge bridge, PlugwiseHAController controller,
            Runnable poll) implements AutoCloseable {
        @Override
        public void close() {
            handler.dispose();
        }
    }
}
