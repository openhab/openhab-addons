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
package org.openhab.binding.roborock.internal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openhab.core.common.ThreadPoolManager;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * Tests routing of inbound MQTT messages to child vacuum handlers.
 *
 * @author Arne Seime - Initial contribution
 */
@NonNullByDefault
class RoborockAccountHandlerMessageDispatchTest {
    private static final String TOPIC_PREFIX = "rr/m/o/test-user/abcdef12/";

    private final MockedStatic<ThreadPoolManager> threadPools;
    private final MockedConstruction<RoborockWebTargets> webTargets;
    private final RoborockAccountHandler handler;

    RoborockAccountHandlerMessageDispatchTest() {
        threadPools = mockStatic(ThreadPoolManager.class);
        threadPools.when(() -> ThreadPoolManager.getScheduledPool("thingHandler"))
                .thenReturn(mock(ScheduledExecutorService.class));
        webTargets = mockConstruction(RoborockWebTargets.class);
        Bridge bridge = mock(Bridge.class);
        when(bridge.getUID()).thenReturn(new ThingUID("roborock:account:test"));
        when(bridge.getConfiguration()).thenReturn(new Configuration(Map.of("email", "test@example.invalid")));
        Storage<String> storage = mock();
        handler = new RoborockAccountHandler(bridge, storage, mock(HttpClient.class),
                mock(MqttInboundLivenessWatchdog.class));
    }

    @AfterEach
    void tearDown() {
        webTargets.close();
        threadPools.close();
    }

    @Test
    void messageForConfiguredDeviceIsDispatchedToItsHandler() throws Exception {
        RoborockVacuumHandler vacuum = mock(RoborockVacuumHandler.class);
        handler.childDevices.put("configuredDuid", vacuum);
        byte[] payload = new byte[] { 1, 2, 3 };

        handler.messageArrived(TOPIC_PREFIX + "configuredDuid", new MqttMessage(payload));

        verify(vacuum).handleMessage(payload);
    }

    @Test
    void repeatedMessagesForUnconfiguredDeviceAreIgnored() throws Exception {
        RoborockVacuumHandler vacuum = mock(RoborockVacuumHandler.class);
        handler.childDevices.put("configuredDuid", vacuum);

        for (int i = 0; i < 3; i++) {
            assertDoesNotThrow(
                    () -> handler.messageArrived(TOPIC_PREFIX + "otherDuid", new MqttMessage(new byte[] { 1 })));
        }

        verify(vacuum, never()).handleMessage(any());
    }
}
