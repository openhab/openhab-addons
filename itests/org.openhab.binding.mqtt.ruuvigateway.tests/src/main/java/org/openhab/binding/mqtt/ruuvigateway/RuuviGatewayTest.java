/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mqtt.ruuvigateway;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants;
import org.openhab.binding.mqtt.ruuvigateway.internal.discovery.RuuviGatewayDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.events.Event;
import org.openhab.core.events.EventFilter;
import org.openhab.core.events.EventSubscriber;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.events.ThingStatusInfoChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes test data
 *
 * @author David Graeff - Initial contribution
 * @author Sami Salonen - Adapted and extended to Ruuvi Gateway tests
 */
// @ExtendWith(MockitoExtension.class)
// @MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class RuuviGatewayTest extends MqttOSGiTest {
    private static final String BASE_TOPIC_RUUVI = "ruuvi";
    // private static final String BASE_TOPIC_CUSTOM = "mycustomprefix";

    private static class ThingStatusInfoChangedSubscriber implements EventSubscriber {

        private final Logger logger = LoggerFactory.getLogger(ThingStatusInfoChangedSubscriber.class);

        public Map<ThingUID, List<ThingStatusInfo>> statusUpdates = new HashMap<>();

        @Override
        public Set<@NonNull String> getSubscribedEventTypes() {
            return Collections.singleton(ThingStatusInfoChangedEvent.TYPE);
        }

        @Override
        public @Nullable EventFilter getEventFilter() {
            return null;
        }

        @Override
        public void receive(Event event) {
            // Expecting only state updates in the tests
            assertInstanceOf(ThingStatusInfoChangedEvent.class, event);
            ThingStatusInfoChangedEvent statusEvent = (ThingStatusInfoChangedEvent) event;
            logger.trace("Captured event: {} ", event);
            List<ThingStatusInfo> updates = statusUpdates.computeIfAbsent(statusEvent.getThingUID(),
                    item -> new ArrayList<>());
            assert updates != null; // To make compiler happy
            updates.add(statusEvent.getStatusInfo());
        }
    }

    private ThingStatusInfoChangedSubscriber statusSubscriber = new ThingStatusInfoChangedSubscriber();
    private @NonNullByDefault({}) MqttBrokerConnection mqttConnection;
    private int registeredTopics = 100;

    private @NonNullByDefault({}) ScheduledExecutorService scheduler;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    private MqttConnectionObserver failIfChange = (state, error) -> assertThat(state,
            is(MqttConnectionState.CONNECTED));

    private @NonNullByDefault({}) RuuviGatewayDiscoveryService ruuviDiscoveryService;
    private Set<Thing> things = new HashSet<>();

    private Bridge createMqttBrokerBridge() {
        Configuration configuration = new Configuration();
        configuration.put("host", "127.0.0.1");
        configuration.put("port", brokerConnection.getPort());
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("mqtt", "broker"), "mybroker").withLabel("MQTT Broker")
                .withConfiguration(configuration).build();
        thingProvider.add(bridge);
        waitForAssert(() -> assertNotNull(bridge.getHandler()));
        assertNotNull(bridge.getConfiguration());
        things.add(bridge);
        return bridge;
    }

    private Thing createRuuviThing(String brokerPrefix, String topic) {
        return createRuuviThing(brokerPrefix, topic, null);
    }

    private Thing createRuuviThing(String brokerPrefix, String topic, @Nullable Integer timeoutMillisecs) {
        Configuration configuration = new Configuration();
        configuration.put(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC, topic);
        if (timeoutMillisecs != null) {
            configuration.put(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TIMEOUT, timeoutMillisecs);
        }
        Thing thing = ThingBuilder
                .create(RuuviGatewayBindingConstants.THING_TYPE_BEACON, topic.replaceAll("[:_/]", "_"))
                .withBridge(new ThingUID("mqtt", "broker", "mybroker")).withLabel("Ruuvi " + topic)
                .withConfiguration(configuration).build();
        thingProvider.add(thing);
        waitForAssert(() -> assertNotNull(thing.getHandler()));
        assertNotNull(thing.getConfiguration());
        things.add(thing);
        return thing;
    }

    @Override
    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();

        statusSubscriber.statusUpdates.clear();
        registerService(statusSubscriber);

        MQTTTopicDiscoveryService mqttTopicDiscoveryService = getService(MQTTTopicDiscoveryService.class);
        assertNotNull(mqttTopicDiscoveryService);
        ruuviDiscoveryService = new RuuviGatewayDiscoveryService(mqttTopicDiscoveryService);

        createMqttBrokerBridge();

        mqttConnection = createBrokerConnection("myclientid");

        // If the connection state changes in between -> fail
        mqttConnection.addConnectionObserver(failIfChange);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:AA:00", "{}"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02",
                "{" + "  \"gw_mac\": \"DE:AD:BE:EF:00\"," + "  \"rssi\": -82," + "  \"aoa\": [],"
                        + "  \"gwts\": \"1659365432\"," + "  \"ts\": \"1659365222\","
                        + "  \"data\": \"0201061BFF99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F\","
                        + "  \"coords\": \"\" }"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:03", "invalid json"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:04",
                "{" + "  \"gw_mac\": \"DE:AD:BE:EF:00:00\"," + "  \"rssi\": -82," + "  \"aoa\": [],"
                        + "  \"gwts\": \"1659365432\"," + "  \"ts\": \"1659365222\","
                        + "  \"data\": \"0201061BFF990405\"," // payload too short
                        + "  \"coords\": \"\" }"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:05", "{" + "  \"gw_mac\": \"DE:AD:BE:EF:00:00\","
                + "  \"rssi\": -82," + "  \"aoa\": []," + "  \"gwts\": \"1659365432\"," + "  \"ts\": \"1659365222\","
                // non-matching manufacturer (the two bytes after FF do not match 99 04)
                + "  \"data\": \"0201061BFF99050512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F\","
                + "  \"coords\": \"\" }"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:06", "{" + "  \"gw_mac\": \"DE:AD:BE:EF:00:00\","
                + "  \"rssi\": -82," + "  \"aoa\": []," + "  \"gwts\": \"1659365432\"," + "  \"ts\": \"1659365222\","
                // advertisement is not for "manufacturer specific" data
                + "  \"data\": \"0201061BFA99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F\","
                + "  \"coords\": \"\" }"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:07",
                "{" + "  \"gw_mac\": \"DE:AD:BE:EF:00\"," + "  \"rssi\": -82," + "  \"aoa\": [],"
                // data field is number, not a string
                        + "  \"gwts\": \"1659365432\"," + "  \"ts\": \"1659365222\"," + "  \"data\": 999,"
                        + "  \"coords\": \"\" }"));
        futures.add(publish(BASE_TOPIC_RUUVI + "/mygwid/invalid:mac2", "{}"));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);

        scheduler = new ScheduledThreadPoolExecutor(6);
    }

    @Override
    @AfterEach
    public void afterEach() throws Exception {
        if (mqttConnection != null) {
            mqttConnection.removeConnectionObserver(failIfChange);
            mqttConnection.stop().get(5, TimeUnit.SECONDS);
        }
        things.stream().map(thing -> thingProvider.remove(thing.getUID()));

        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        super.afterEach();
    }

    @Test
    public void retrieveAllRuuviPrefixedTopics() throws Exception {
        CountDownLatch c = new CountDownLatch(registeredTopics);
        mqttConnection.subscribe(BASE_TOPIC_RUUVI + "/#", (topic, payload) -> c.countDown()).get(5, TimeUnit.SECONDS);
        assertTrue(c.await(5, TimeUnit.SECONDS),
                "Connection " + mqttConnection.getClientId() + " not retrieving all topics ");
    }

    private void assertThingStatus(List<ThingStatusInfo> statusUpdates, int index, ThingStatus status,
            @Nullable ThingStatusDetail detail, @Nullable String description) {
        assertEquals(status, statusUpdates.get(index).getStatus());
        assertEquals(detail, statusUpdates.get(index).getStatusDetail());
        assertEquals(description, statusUpdates.get(index).getDescription());
    }

    private void assertThingStatus(List<ThingStatusInfo> statusUpdates, int index, ThingStatus status) {
        assertThingStatus(statusUpdates, index, status, ThingStatusDetail.NONE, null);
    }

    @ParameterizedTest
    @ValueSource(strings = { //
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:AA:00", // empty json
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:03", // invalid json
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:04", // payload too short
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:05", // wrong manufacturer id
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:06", // unexpected advertisement
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:07", // wrong json data types
    })
    public void testInvalidCases(String topic) throws Exception {
        Thing ruuviThing = createRuuviThing("mygwid", topic, 100);
        waitForAssert(() -> {
            List<ThingStatusInfo> statusUpdates = statusSubscriber.statusUpdates.get(ruuviThing.getUID());
            assertNotNull(statusUpdates);
            assertEquals(4, statusUpdates.size(), statusUpdates.toString());
            int i = 0;
            assertThingStatus(statusUpdates, i++, ThingStatus.INITIALIZING);
            assertThingStatus(statusUpdates, i++, ThingStatus.UNKNOWN);
            assertThingStatus(statusUpdates, i++, ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    "Waiting for initial data");
            assertThingStatus(statusUpdates, i++, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No valid data received for some time");
        });
    }

    public void testHappyFlow() {
        Thing ruuviThing = createRuuviThing("mygwid", BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02", 100);
        waitForAssert(() -> {
            List<ThingStatusInfo> statusUpdates = statusSubscriber.statusUpdates.get(ruuviThing.getUID());
            assertNotNull(statusUpdates);
            assertEquals(4, statusUpdates.size(), statusUpdates.toString());
            int i = 0;
            assertThingStatus(statusUpdates, i++, ThingStatus.INITIALIZING);
            assertThingStatus(statusUpdates, i++, ThingStatus.UNKNOWN);
            assertThingStatus(statusUpdates, i++, ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    "Waiting for initial data");
            assertThingStatus(statusUpdates, i++, ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "No valid data received for some time");
        });
    }
}
