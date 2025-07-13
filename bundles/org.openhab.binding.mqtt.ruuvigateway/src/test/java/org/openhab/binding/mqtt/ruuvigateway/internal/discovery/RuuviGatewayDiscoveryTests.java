/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.ruuvigateway.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mqtt.MqttBindingConstants;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link RuuviGatewayDiscoveryService}
 *
 * @author Anton Kharuzhy - Initial contribution
 * @author Sami Salonen - Adapted from Home Assistant to Ruuvi Gateway tests
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class RuuviGatewayDiscoveryTests {
    private @NonNullByDefault({}) RuuviGatewayDiscoveryService discovery;
    private static final ThingUID MQTT_BRIDGE_UID = new ThingUID(MqttBindingConstants.BRIDGE_TYPE_BROKER, "broker");

    private @Mock @NonNullByDefault({}) MQTTTopicDiscoveryService mqttTopicDiscoveryService;
    private @Mock @NonNullByDefault({}) MqttBrokerConnection mqttConnection;

    @BeforeEach
    public void beforeEach() {
        discovery = new RuuviGatewayDiscoveryService(mqttTopicDiscoveryService);
    }

    @ParameterizedTest
    @ValueSource(strings = { "de:ea:DB:be:ff:00", "de:ea:DB:be:ff-00", "de-ea-DB-be-ff-00" })
    public void testDiscoveryMacFormatPermutations(String leafTopic) throws Exception {
        var discoveryListener = new LatchDiscoveryListener();
        var latch = discoveryListener.createWaitForThingsDiscoveredLatch(1);

        // When discover one thing with two channels
        discovery.addDiscoveryListener(discoveryListener);
        discovery.receivedMessage(MQTT_BRIDGE_UID, mqttConnection, "ruuvi/foo/bar/" + leafTopic, "{}".getBytes());

        // Then one thing found
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        var discoveryResults = discoveryListener.getDiscoveryResults();
        assertThat(discoveryResults.size(), is(1));
        @Nullable
        DiscoveryResult result = discoveryResults.get(0);
        Objects.requireNonNull(result); // Make compiler happy
        assertThat(result.getBridgeUID(), is(MQTT_BRIDGE_UID));
        assertThat(result.getProperties().get(Thing.PROPERTY_VENDOR), is("Ruuvi Innovations Ltd (Oy)"));
        assertThat(result.getProperties().get(RuuviGatewayBindingConstants.PROPERTY_TAG_ID), is("DE:EA:DB:BE:FF:00"));
        assertThat(result.getProperties().get(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC),
                is("ruuvi/foo/bar/" + leafTopic));
    }

    @Test
    public void testDiscoveryMultipleThings() throws Exception {
        var discoveryListener = new LatchDiscoveryListener();
        var latch = discoveryListener.createWaitForThingsDiscoveredLatch(2);

        discovery.addDiscoveryListener(discoveryListener);
        discovery.receivedMessage(MQTT_BRIDGE_UID, mqttConnection, "something/to/ignore/ruuvi/foo/bar/invalid:mac",
                "{}".getBytes());
        discovery.receivedMessage(MQTT_BRIDGE_UID, mqttConnection, "ruuvi/foo/bar/invalid:mac", "{}".getBytes());
        discovery.receivedMessage(MQTT_BRIDGE_UID, mqttConnection, "ruuvi/foo/bar/aa:bb", "{}".getBytes()); // too short
                                                                                                            // mac
        discovery.receivedMessage(MQTT_BRIDGE_UID, mqttConnection, "ruuvi/foo/bar/de:ea:DB:be:ff:00", "{}".getBytes());
        discovery.receivedMessage(MQTT_BRIDGE_UID, mqttConnection, "ruuvi/foo/bar/de:ea:DB:be:ff:01", "{}".getBytes());

        // Then one thing found
        assertTrue(latch.await(3, TimeUnit.SECONDS));
        var discoveryResults = discoveryListener.getDiscoveryResults();
        assertThat(discoveryResults.size(), is(2));

        assertTrue(discoveryResults.stream().allMatch(result -> {
            assertThat(result.getBridgeUID(), is(MQTT_BRIDGE_UID));
            assertThat(result.getProperties().get(Thing.PROPERTY_VENDOR), is("Ruuvi Innovations Ltd (Oy)"));
            return true;
        }));

        assertTrue(//
                discoveryResults.stream().anyMatch(result -> "DE:EA:DB:BE:FF:00"
                        .equals(result.getProperties().get(RuuviGatewayBindingConstants.PROPERTY_TAG_ID))
                        && "ruuvi/foo/bar/de:ea:DB:be:ff:00".equals(
                                result.getProperties().get(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC)))
                        && //
                        discoveryResults.stream()
                                .anyMatch(result -> "DE:EA:DB:BE:FF:01".equals(
                                        result.getProperties().get(RuuviGatewayBindingConstants.PROPERTY_TAG_ID))
                                        && "ruuvi/foo/bar/de:ea:DB:be:ff:01".equals(result.getProperties()
                                                .get(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC)))

                , "Failed to match: " + discoveryResults.toString());
    }

    private static class LatchDiscoveryListener implements DiscoveryListener {
        private final CopyOnWriteArrayList<DiscoveryResult> discoveryResults = new CopyOnWriteArrayList<>();
        private @Nullable CountDownLatch latch;

        @Override
        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            discoveryResults.add(result);
            CountDownLatch localLatch = latch;
            if (localLatch != null) {
                localLatch.countDown();
            }
        }

        @Override
        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        }

        @Override
        public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, Instant timestamp,
                @Nullable Collection<ThingTypeUID> thingTypeUIDs, @Nullable ThingUID bridgeUID) {
            return Collections.emptyList();
        }

        public CopyOnWriteArrayList<DiscoveryResult> getDiscoveryResults() {
            return discoveryResults;
        }

        public CountDownLatch createWaitForThingsDiscoveredLatch(int count) {
            final var newLatch = new CountDownLatch(count);
            latch = newLatch;
            return newLatch;
        }
    }
}
