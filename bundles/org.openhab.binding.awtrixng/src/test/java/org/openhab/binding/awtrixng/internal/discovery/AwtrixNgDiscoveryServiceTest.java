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
package org.openhab.binding.awtrixng.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.awtrixng.internal.AwtrixNgBindingConstants.*;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.ThingUID;

/**
 * Test cases for the {@link AwtrixNgDiscoveryService}.
 *
 * @author Keith T. Garner - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
class AwtrixNgDiscoveryServiceTest {

    private static final int WAIT_MS = 2000;
    private static final ThingUID BROKER = new ThingUID(MqttBindingConstants.BRIDGE_TYPE_BROKER, "broker");

    private @Mock MQTTTopicDiscoveryService topicDiscoveryService;
    private @Mock MqttBrokerConnection connection;
    private @Mock DiscoveryListener listener;

    private AwtrixNgDiscoveryService discovery;

    @BeforeEach
    void setUp() {
        discovery = new AwtrixNgDiscoveryService(topicDiscoveryService);
        discovery.addDiscoveryListener(listener);
    }

    private void receive(String topic, String payload) {
        discovery.receivedMessage(BROKER, connection, topic, payload.getBytes(StandardCharsets.UTF_8));
    }

    // The discovery service reports results from its own scheduler, so wait for them
    private DiscoveryResult discovered() {
        ArgumentCaptor<DiscoveryResult> result = ArgumentCaptor.forClass(DiscoveryResult.class);
        verify(listener, timeout(WAIT_MS)).thingDiscovered(any(), result.capture());
        return result.getValue();
    }

    @Test
    void discoversAClockFromItsDeviceState() {
        receive("a4cf12ab34cd/state/device", """
                {"version":"1.0.12","uid":"a4cf12ab34cd","boardType":"awtrixng","soc":"esp32",
                 "macAddress":"A4:CF:12:0B:3C:7D","hostname":"awtrixng-ab34cd","currentApp":"Time"}
                """);

        DiscoveryResult result = discovered();
        assertEquals(new ThingUID(THING_TYPE_CLOCK, BROKER, "a4cf12ab34cd"), result.getThingUID());
        assertEquals(BROKER, result.getBridgeUID());
        assertEquals("AWTRIX NG Clock awtrixng-ab34cd", result.getLabel());
        assertEquals(PROP_UNIQUEID, result.getRepresentationProperty());
        assertEquals("a4cf12ab34cd", result.getProperties().get(PROP_BASETOPIC));
        assertEquals("a4cf12ab34cd", result.getProperties().get(PROP_UNIQUEID));
        assertEquals("1.0.12", result.getProperties().get(PROP_FIRMWARE));
        assertEquals("awtrixng", result.getProperties().get(PROP_BOARD_TYPE));
        assertEquals("A4:CF:12:0B:3C:7D", result.getProperties().get(PROP_MAC_ADDRESS));
        assertEquals("awtrixng-ab34cd", result.getProperties().get(PROP_HOSTNAME));
    }

    @Test
    void customPrefixBecomesTheBaseTopic() {
        receive("awtrixNG/state/device", "{\"uid\":\"a4cf12ab34cd\",\"boardType\":\"awtrixng\"}");

        DiscoveryResult result = discovered();
        assertEquals("awtrixNG", result.getProperties().get(PROP_BASETOPIC));
        assertEquals("AWTRIX NG Clock a4cf12ab34cd", result.getLabel());
        assertNull(result.getProperties().get(PROP_FIRMWARE));
    }

    @Test
    void ignoresDevicesThatAreNotAwtrixNg() {
        receive("other/state/device", "{\"temperature\":21.5}");
        receive("other/state/device", "{\"uid\":\"a4cf12ab34cd\"}");
        receive("other/state/device", "{\"uid\":\"nonsense\",\"boardType\":\"x\"}");
        receive("other/state/device", "{\"uid\":\"A4CF12AB34CD\",\"boardType\":\"x\"}");
        receive("other/state/device", "{not json");
        receive("other/state/device", "");
        receive("/state/device", "{\"uid\":\"a4cf12ab34cd\",\"boardType\":\"awtrixng\"}");
        receive("a4cf12ab34cd/state/settings", "{\"uid\":\"a4cf12ab34cd\",\"boardType\":\"awtrixng\"}");

        // Nothing is reported, which can only be told by waiting; the wait is generous for slow build machines
        verify(listener, after(WAIT_MS).never()).thingDiscovered(any(), any());
    }
}
