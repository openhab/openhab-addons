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
package org.openhab.binding.mqtt.awtrixlight.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mqtt.awtrixlight.internal.AwtrixLightBindingConstants.*;
import static org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgBindingConstants.THING_TYPE_NG_APP;
import static org.openhab.binding.mqtt.awtrixlight.internal.ng.AwtrixNgBindingConstants.THING_TYPE_NG_CLOCK;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Test cases for the change of the AWTRIX 3 things to the AWTRIX NG thing types.
 *
 * @author Keith T. Garner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AwtrixLightFirmwareDetectionTest {

    private static final String BASE = "awtrix_abc123";
    private static final ThingUID BROKER_UID = new ThingUID("mqtt", "broker", "b");
    private static final String AWTRIX3_STATS = "{\"version\":\"0.98\",\"uid\":\"awtrix_abc123\",\"bat\":80}";

    private @Mock ThingHandlerCallback callback;
    private @Mock Bridge broker;
    private @Mock AbstractBrokerHandler brokerHandler;
    private @Mock MqttBrokerConnection connection;

    private Bridge clock;
    private AwtrixLightBridgeHandler handler;

    @BeforeEach
    void setUp() {
        clock = BridgeBuilder.create(THING_TYPE_BRIDGE, "clock").withBridge(BROKER_UID)
                .withConfiguration(new Configuration(Map.of("basetopic", BASE, "appLockTimeout", 12,
                        "discoverDefaultApps", true, "lowBatteryThreshold", 30)))
                .build();
        handler = new AwtrixLightBridgeHandler(clock);
        handler.setCallback(callback);

        when(callback.getBridge(any())).thenReturn(broker);
        when(broker.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        when(broker.getHandler()).thenReturn(brokerHandler);
        when(brokerHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));
    }

    @AfterEach
    void tearDown() {
        handler.dispose();
    }

    private void message(String suffix, String payload) {
        handler.processMessage(BASE + suffix, payload.getBytes(StandardCharsets.UTF_8));
    }

    private void verifyNoThingTypeChange() {
        verify(callback, never()).migrateThingType(any(), any(), any());
    }

    private static String deviceState(long uptimeSeconds) {
        return "{\"version\":\"1.0.12\",\"uid\":\"a4cf12ab34cd\",\"uptimeSeconds\":" + uptimeSeconds + "}";
    }

    @Test
    void subscribesToTheDeviceStateOfAwtrixNg() {
        handler.initialize();

        verify(connection).subscribe(BASE + "/state/device", handler);
    }

    @Test
    void disposeUnsubscribesFromTheDeviceState() {
        handler.initialize();

        handler.dispose();

        verify(connection).unsubscribe(BASE + "/state/device", handler);
    }

    @Test
    void firstDeviceStateMayBeLeftOnTheBrokerSoItIsNoReasonToSwitch() {
        handler.initialize();

        message("/state/device", deviceState(4213));

        verifyNoThingTypeChange();
    }

    @Test
    void deviceStateWithTheSameUptimeIsNoReasonToSwitch() {
        handler.initialize();

        message("/state/device", deviceState(4213));
        message("/state/device", deviceState(4213));

        verifyNoThingTypeChange();
    }

    @Test
    void deviceStateWithAnotherUptimeChangesToTheNgClockAndKeepsTheSharedConfiguration() {
        handler.initialize();

        message("/state/device", deviceState(4213));
        message("/state/device", deviceState(4223));

        ArgumentCaptor<Configuration> configuration = ArgumentCaptor.forClass(Configuration.class);
        verify(callback).migrateThingType(eq(clock), eq(THING_TYPE_NG_CLOCK), configuration.capture());
        assertEquals(new Configuration(Map.of("basetopic", BASE, "appLockTimeout", 12, "discoverDefaultApps", true))
                .getProperties(), configuration.getValue().getProperties());
    }

    @Test
    void rebootedClockCountsAsRunningAwtrixNg() {
        handler.initialize();

        message("/state/device", deviceState(4213));
        message("/state/device", deviceState(3));

        verify(callback).migrateThingType(eq(clock), eq(THING_TYPE_NG_CLOCK), any());
    }

    @Test
    void thingTypeIsChangedOnceWhileDeviceStatesKeepComing() {
        handler.initialize();

        message("/state/device", deviceState(4213));
        message("/state/device", deviceState(4223));
        message("/state/device", deviceState(4233));

        verify(callback, times(1)).migrateThingType(any(), any(), any());
    }

    @Test
    void deviceStateWithoutUptimeOrThatIsNoJsonIsIgnored() {
        handler.initialize();

        message("/state/device", deviceState(4213));
        message("/state/device", "{\"version\":\"1.0.12\"}");
        message("/state/device", "not json");
        message("/state/device", "");

        verifyNoThingTypeChange();
    }

    @Test
    void newSubscriptionTakesTheNextDeviceStateAsLeftOnTheBrokerAgain() {
        handler.initialize();
        message("/state/device", deviceState(4213));
        handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));

        // The broker delivers what it holds for the new subscription first
        message("/state/device", deviceState(4223));

        verifyNoThingTypeChange();
    }

    @Test
    void statsOfAwtrix3AreNoReasonToSwitch() {
        handler.initialize();

        message("/stats", AWTRIX3_STATS);
        message("/stats", AWTRIX3_STATS);

        verifyNoThingTypeChange();
    }

    @Test
    void appChangesToTheNgAppWhenItsClockIsNg() {
        Bridge ngClock = BridgeBuilder.create(THING_TYPE_NG_CLOCK, "clock").withBridge(BROKER_UID).build();
        Thing app = ThingBuilder.create(THING_TYPE_APP, "washing").withBridge(ngClock.getUID())
                .withConfiguration(new Configuration(Map.of("appname", "washing", "useButtons", true))).build();
        AwtrixLightAppHandler appHandler = new AwtrixLightAppHandler(app);
        appHandler.setCallback(callback);
        when(callback.getBridge(ngClock.getUID())).thenReturn(ngClock);

        appHandler.initialize();

        ArgumentCaptor<Configuration> configuration = ArgumentCaptor.forClass(Configuration.class);
        verify(callback).migrateThingType(eq(app), eq(THING_TYPE_NG_APP), configuration.capture());
        assertEquals(Map.of("appname", "washing", "useButtons", true), configuration.getValue().getProperties());
    }

    @Test
    void appStaysAnAwtrix3AppWhenItsClockIsAwtrix3() {
        Thing app = ThingBuilder.create(THING_TYPE_APP, "washing").withBridge(clock.getUID())
                .withConfiguration(new Configuration(Map.of("appname", "washing"))).build();
        AwtrixLightAppHandler appHandler = new AwtrixLightAppHandler(app);
        appHandler.setCallback(callback);
        when(callback.getBridge(clock.getUID())).thenReturn(clock);

        appHandler.initialize();

        verifyNoThingTypeChange();
        appHandler.dispose();
    }
}
