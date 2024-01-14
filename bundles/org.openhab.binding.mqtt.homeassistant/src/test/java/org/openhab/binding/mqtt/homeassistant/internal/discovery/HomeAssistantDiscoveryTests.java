/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.homeassistant.internal.discovery;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractHomeAssistantTests;
import org.openhab.binding.mqtt.homeassistant.internal.HandlerConfiguration;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for {@link HomeAssistantDiscovery}
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@SuppressWarnings({ "unchecked" })
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class HomeAssistantDiscoveryTests extends AbstractHomeAssistantTests {
    private @NonNullByDefault({}) HomeAssistantDiscovery discovery;

    @BeforeEach
    public void beforeEach() {
        discovery = new TestHomeAssistantDiscovery(channelTypeProvider);
    }

    @Test
    public void testComponentNameSummary() {
        assertThat(
                HomeAssistantDiscovery.getComponentNamesSummary(
                        Stream.of("Sensor", "Switch", "Sensor", "Foobar", "Foobar", "Foobar")), //
                is("3x Foobar, 2x Sensor, Switch"));
    }

    @Test
    public void testOneThingDiscovery() throws Exception {
        var discoveryListener = new LatchDiscoveryListener();
        var latch = discoveryListener.createWaitForThingsDiscoveredLatch(1);

        // When discover one thing with two channels
        discovery.addDiscoveryListener(discoveryListener);
        discovery.receivedMessage(HA_UID, bridgeConnection,
                "homeassistant/climate/0x847127fffe11dd6a_climate_zigbee2mqtt/config",
                getResourceAsByteArray("component/configTS0601ClimateThermostat.json"));
        discovery.receivedMessage(HA_UID, bridgeConnection,
                "homeassistant/switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt/config",
                getResourceAsByteArray("component/configTS0601AutoLock.json"));

        // Then one thing found
        assert latch.await(3, TimeUnit.SECONDS);
        var discoveryResults = discoveryListener.getDiscoveryResults();
        assertThat(discoveryResults.size(), is(1));
        var result = discoveryResults.get(0);
        assertThat(result.getBridgeUID(), is(HA_UID));
        assertThat(result.getProperties().get(Thing.PROPERTY_MODEL_ID),
                is("Radiator valve with thermostat (TS0601_thermostat)"));
        assertThat(result.getProperties().get(Thing.PROPERTY_VENDOR), is("TuYa"));
        assertThat(result.getProperties().get(Thing.PROPERTY_FIRMWARE_VERSION), is("Zigbee2MQTT 1.18.2"));
        assertThat(result.getProperties().get(HandlerConfiguration.PROPERTY_BASETOPIC), is("homeassistant"));
        assertThat(result.getLabel(), is("th1 (Climate Control, Switch)"));
        assertThat((List<String>) result.getProperties().get(HandlerConfiguration.PROPERTY_TOPICS), hasItems(
                "climate/0x847127fffe11dd6a_climate_zigbee2mqtt", "switch/0x847127fffe11dd6a_auto_lock_zigbee2mqtt"));
    }

    private static class TestHomeAssistantDiscovery extends HomeAssistantDiscovery {
        public TestHomeAssistantDiscovery(MqttChannelTypeProvider typeProvider) {
            this.typeProvider = typeProvider;
        }
    }

    private static class LatchDiscoveryListener implements DiscoveryListener {
        private final CopyOnWriteArrayList<DiscoveryResult> discoveryResults = new CopyOnWriteArrayList<>();
        private @Nullable CountDownLatch latch;

        @Override
        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            discoveryResults.add(result);
            if (latch != null) {
                latch.countDown();
            }
        }

        @Override
        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
        }

        @Override
        public @Nullable Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
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
