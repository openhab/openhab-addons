/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants.*;
import static org.openhab.core.library.unit.MetricPrefix.HECTO;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.measure.quantity.Acceleration;
import javax.measure.quantity.Dimensionless;
import javax.measure.quantity.ElectricPotential;
import javax.measure.quantity.Power;
import javax.measure.quantity.Pressure;
import javax.measure.quantity.Temperature;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.discovery.MQTTTopicDiscoveryService;
import org.openhab.binding.mqtt.ruuvigateway.internal.RuuviGatewayBindingConstants;
import org.openhab.binding.mqtt.ruuvigateway.internal.discovery.RuuviGatewayDiscoveryService;
import org.openhab.binding.mqtt.ruuvigateway.internal.handler.RuuviTagHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.items.GenericItem;
import org.openhab.core.library.CoreItemFactory;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.link.ItemChannelLink;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes test data
 *
 * @author David Graeff - Initial contribution
 * @author Sami Salonen - Adapted and extended to Ruuvi Gateway tests
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RuuviGatewayTest extends MqttOSGiTest {
    protected @Mock @NonNullByDefault({}) UnitProvider mockedUnitProvider;
    private static final String BASE_TOPIC_RUUVI = "ruuvi";
    private static final Map<String, String> CHANNEL_TO_ITEM_TYPE = new HashMap<>();
    static {
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_ACCELERATIONX, "Number:Acceleration");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_ACCELERATIONY, "Number:Acceleration");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_ACCELERATIONZ, "Number:Acceleration");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_BATTERY, "Number:ElectricPotential");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_DATA_FORMAT, "Number");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_HUMIDITY, "Number:Dimensionless");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER, "Number");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_MOVEMENT_COUNTER, "Number");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_PRESSURE, "Number:Pressure");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_TEMPERATURE, "Number:Temperature");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_TX_POWER, "Number:Power");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_RSSI, "Number:Power");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_TS, "DateTime");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_GWTS, "DateTime");
        CHANNEL_TO_ITEM_TYPE.put(CHANNEL_ID_GWMAC, "String");
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

    @SuppressWarnings("unused") // used indirectly with Inbox
    private @NonNullByDefault({}) RuuviGatewayDiscoveryService ruuviDiscoveryService;
    private Set<Thing> things = new HashSet<>();

    @BeforeEach
    public void setup() {
        when(mockedUnitProvider.getUnit(any())).then(i -> {
            Class clazz = i.getArgument(0);
            if (Temperature.class.equals(clazz)) {
                return SIUnits.CELSIUS;
            } else if (Acceleration.class.equals(clazz)) {
                return Units.METRE_PER_SQUARE_SECOND;
            } else if (Dimensionless.class.equals(clazz)) {
                return Units.ONE;
            } else if (ElectricPotential.class.equals(clazz)) {
                return Units.VOLT;
            } else if (Pressure.class.equals(clazz)) {
                return HECTO(SIUnits.PASCAL);
            } else if (Power.class.equals(clazz)) {
                return Units.WATT;
            }
            return null;
        });
    }

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

    private Thing createRuuviThing(String brokerPrefix, String topic, @Nullable Integer timeoutMillisecs) {
        Configuration configuration = new Configuration();
        configuration.put(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TOPIC, topic);
        if (timeoutMillisecs != null) {
            configuration.put(RuuviGatewayBindingConstants.CONFIGURATION_PROPERTY_TIMEOUT, timeoutMillisecs);
        }
        ThingUID bridgeThingUID = new ThingUID("mqtt", "broker", "mybroker");
        ThingUID thingUID = new ThingUID(RuuviGatewayBindingConstants.THING_TYPE_BEACON,
                topic.replaceAll("[:_/]", "_"));
        ThingBuilder thingBuilder = ThingBuilder.create(RuuviGatewayBindingConstants.THING_TYPE_BEACON, thingUID)
                .withBridge(bridgeThingUID).withLabel("Ruuvi " + topic).withConfiguration(configuration);

        CHANNEL_TO_ITEM_TYPE.forEach((channelId, _itemType) -> {
            thingBuilder.withChannel(ChannelBuilder.create(new ChannelUID(thingUID, channelId)).build());
        });

        Thing thing = thingBuilder.build();
        thingProvider.add(thing);
        waitForAssert(() -> assertNotNull(thing.getHandler()));
        assertNotNull(thing.getConfiguration());
        things.add(thing);
        return thing;
    }

    private void triggerTimeoutHandling(Thing ruuviThing) {
        // Simulate some time passing, so that RuuviTagHandler.heartbeat() is called twice
        // Two heartbeat calls happens to trigger timeout handling in handler, one is not enough.
        // (this is really implementation detail of RuuviTagHandler, making this test slightly
        // error prone to possible changes in RuuviTagHandler implementation)
        //
        // 0. Assume some data received already, RuuviTagHandler.receivedData is true
        // 1. First heartbeat sets receivedData=false; no further action is taken yet
        // 2. Second heartbeat acts on false receivedData, e.g. updating Thing Status
        for (int i = 0; i < 2; i++) {
            callInternalHeartbeat(ruuviThing);
        }
    }

    private void callInternalHeartbeat(Thing ruuviThing) {
        ThingHandler handler = ruuviThing.getHandler();
        Objects.requireNonNull(handler);
        assertInstanceOf(RuuviTagHandler.class, handler);
        RuuviTagHandler ruuviHandler = (RuuviTagHandler) handler;
        try {
            Method heartbeatMethod = RuuviTagHandler.class.getDeclaredMethod("heartbeat");
            Objects.requireNonNull(heartbeatMethod);
            heartbeatMethod.setAccessible(true);
            heartbeatMethod.invoke(ruuviHandler);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            fail("Failed to call heartbeat method of thing handler via reflection. Bug in test? Details: "
                    + e.getClass().getSimpleName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private String itemName(ChannelUID channelUID) {
        return channelUID.getAsString().replace(":", "_");
    }

    private String linkChannelToAutogeneratedItem(ChannelUID channelUID) {
        String itemName = itemName(channelUID);
        String itemType = CHANNEL_TO_ITEM_TYPE.get(channelUID.getId());
        GenericItem item = new CoreItemFactory(mockedUnitProvider).createItem(itemType, itemName);
        assertNotNull(item, itemType);
        itemProvider.add(item);
        itemChannelLinkProvider.add(new ItemChannelLink(itemName, channelUID));
        return itemName;
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
        unregisterService(statusSubscriber);

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
        assertTrue(statusUpdates.size() > index,
                String.format("Not enough status updates. Expected %d, but only had %d. Status updates received: %s",
                        index + 1, statusUpdates.size(),
                        statusUpdates.stream().map(ThingStatusInfo::getStatus).collect(Collectors.toList())));
        assertEquals(status, statusUpdates.get(index).getStatus(), statusUpdates.get(index).toString());
        assertEquals(detail, statusUpdates.get(index).getStatusDetail(), statusUpdates.get(index).toString());
        assertEquals(description, statusUpdates.get(index).getDescription(), statusUpdates.get(index).toString());
    }

    @SuppressWarnings("null")
    private void assertThingStatusWithDescriptionPattern(List<ThingStatusInfo> statusUpdates, int index,
            ThingStatus status, ThingStatusDetail detail, String descriptionPattern) {
        assertTrue(statusUpdates.size() > index, "assert " + statusUpdates.size() + " > " + index + " failed");
        assertEquals(status, statusUpdates.get(index).getStatus(), statusUpdates.get(index).toString());
        assertEquals(detail, statusUpdates.get(index).getStatusDetail(), statusUpdates.get(index).toString());
        assertTrue(statusUpdates.get(index).getDescription().matches(descriptionPattern),
                statusUpdates.get(index).toString());
    }

    private void assertThingStatus(List<ThingStatusInfo> statusUpdates, int index, ThingStatus status) {
        assertThingStatus(statusUpdates, index, status, ThingStatusDetail.NONE, null);
    }

    private void assertItems(Function<String, State> channelStateGetter, String temperatureCelsius,
            String accelerationXStandardGravity, String accelerationYStandardGravity,
            String accelerationZStandardGravity, String batteryVolt, int dataFormat, String humidityPercent,
            int measurementSequenceNumber, int movementCounter, String pressurePascal, String txPowerDecibelMilliwatts,
            String rssiDecibelMilliwatts, Instant ts, Instant gwts, String gwMac) {
        assertEquals(new QuantityType<>(new BigDecimal(temperatureCelsius), SIUnits.CELSIUS),
                channelStateGetter.apply(CHANNEL_ID_TEMPERATURE));
        assertEquals(new QuantityType<>(new BigDecimal(accelerationXStandardGravity), Units.STANDARD_GRAVITY),
                channelStateGetter.apply(CHANNEL_ID_ACCELERATIONX));
        assertEquals(new QuantityType<>(new BigDecimal(accelerationYStandardGravity), Units.STANDARD_GRAVITY),
                channelStateGetter.apply(CHANNEL_ID_ACCELERATIONY));
        assertEquals(new QuantityType<>(new BigDecimal(accelerationZStandardGravity), Units.STANDARD_GRAVITY),
                channelStateGetter.apply(CHANNEL_ID_ACCELERATIONZ));
        assertEquals(new QuantityType<>(new BigDecimal(batteryVolt), Units.VOLT),
                channelStateGetter.apply(CHANNEL_ID_BATTERY));
        assertEquals(new DecimalType(dataFormat), channelStateGetter.apply(CHANNEL_ID_DATA_FORMAT));
        assertEquals(new QuantityType<>(new BigDecimal(humidityPercent), Units.PERCENT),
                channelStateGetter.apply(CHANNEL_ID_HUMIDITY));
        assertEquals(new DecimalType(new BigDecimal(measurementSequenceNumber)),
                channelStateGetter.apply(CHANNEL_ID_MEASUREMENT_SEQUENCE_NUMBER));
        assertEquals(new DecimalType(new BigDecimal(movementCounter)),
                channelStateGetter.apply(CHANNEL_ID_MOVEMENT_COUNTER));
        assertEquals(new QuantityType<>(new BigDecimal(pressurePascal), SIUnits.PASCAL),
                channelStateGetter.apply(CHANNEL_ID_PRESSURE));
        assertEquals(new QuantityType<>(new BigDecimal(txPowerDecibelMilliwatts), Units.DECIBEL_MILLIWATTS),
                channelStateGetter.apply(CHANNEL_ID_TX_POWER));

        assertEquals(new QuantityType<>(new BigDecimal(rssiDecibelMilliwatts), Units.DECIBEL_MILLIWATTS),
                channelStateGetter.apply(CHANNEL_ID_RSSI));
        assertEquals(new DateTimeType(ts.atZone(ZoneId.of("UTC"))), channelStateGetter.apply(CHANNEL_ID_TS));
        assertEquals(new DateTimeType(gwts.atZone(ZoneId.of("UTC"))), channelStateGetter.apply(CHANNEL_ID_GWTS));
        assertEquals(new StringType(gwMac), channelStateGetter.apply(CHANNEL_ID_GWMAC));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '@', value = { //
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:AA:01 @" + "{}", // empty json
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:03 @" + "invalid json", // invalid json
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:04 @" + "0201061BFF990405", // payload too short
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:05 @"
                    + "0201061BFF99050512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F", // wrong manufacturer id (the
                                                                                        // two bytes after FF do not
                                                                                        // match 99 04)
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:06 @"
                    + "0201061BFA99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F", // unexpected advertisement (no
                                                                                        // FF to indicate 'manufacturer
                                                                                        // specific' advertisement)
            BASE_TOPIC_RUUVI + "mygwid/DE:AD:BE:EF:BB:07 @" + "{" + "  \"gw_mac\": \"DE:AD:BE:EF:00\","
                    + "  \"rssi\": -82," + "  \"aoa\": [],"
                    // data field is number, not a string
                    + "  \"gwts\": \"1659365432\"," + "  \"ts\": \"1659365222\"," + "  \"data\": 999,"
                    + "  \"coords\": \"\" }", // wrong json data types
    })
    public void testInvalidCases(String topic, String val) throws Exception {
        final String jsonPayload;
        if (val.contains("{")) {
            // test argument is specifiying the whole json payload
            jsonPayload = val;
        } else {
            // test argument is only specifiying the data field in the json payload
            // Fill rest of the fields with some valid values
            jsonPayload = """
                    {\
                      "gw_mac": "DE:AD:BE:EF:00",\
                      "rssi": -82,\
                      "aoa": [],\
                      "gwts": "1659365432",\
                      "ts": "1659365222",\
                      "data": "\
                    """ + val + "\"," + "  \"coords\": \"\" }";
        }

        Thing ruuviThing = createRuuviThing("mygwid", topic, 100);
        waitForAssert(() -> {
            List<ThingStatusInfo> statusUpdates = statusSubscriber.statusUpdates.get(ruuviThing.getUID());
            assertNotNull(statusUpdates);
            int statusUpdateIndex = 0;
            assertThingStatus(statusUpdates, statusUpdateIndex++, ThingStatus.INITIALIZING);
            assertThingStatus(statusUpdates, statusUpdateIndex++, ThingStatus.UNKNOWN);
            assertThingStatus(statusUpdates, statusUpdateIndex++, ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    "Waiting for initial data");
            assertThingStatus(statusUpdates, statusUpdateIndex++, ThingStatus.OFFLINE,
                    ThingStatusDetail.COMMUNICATION_ERROR, "No valid data received for some time");
            scheduler.execute(() -> publish(topic, jsonPayload));
            assertThingStatusWithDescriptionPattern(statusUpdates, statusUpdateIndex++, ThingStatus.OFFLINE,
                    ThingStatusDetail.COMMUNICATION_ERROR, ".*could not be parsed.*");
            assertEquals(statusUpdateIndex, statusUpdates.size());
        });
    }

    @SuppressWarnings("null")
    @Test
    public void testDiscovery() {
        scheduler.execute(() -> publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02", """
                {\
                 "gw_mac": "DE:AD:BE:EF:00",\
                 "rssi": -82,\
                 "aoa": [],\
                 "gwts": "1659365432",\
                 "ts": "1659365222",\
                 "data": "0201061BFF99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",\
                 "coords": "" }"""));
        waitForAssert(() -> {
            assertEquals(2, inbox.getAll().size(), inbox.getAll().toString());
            var discovered = new HashSet<>(inbox.getAll());
            for (var result : discovered) {
                assertEquals(THING_TYPE_BEACON, result.getThingTypeUID());
                assertEquals("topic", result.getRepresentationProperty());
                Object topic = result.getProperties().get("topic");
                assertNotNull(topic);
                assertTrue(
                        // published in this test
                        (BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02").equals(topic)
                                // published in beforeEach
                                || (BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:AA:00")
                                        .equals(result.getProperties().get("topic")));
            }
        });
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testHappyFlow(boolean quickTimeout) {
        // with quickTimeout=false, heartbeat is effectively disabled. Thing will not "timeout" and go OFFLINE
        // with quickTimeout=true, timeout happens very fast. In CI we use infinite timeout and trigger timeout manually

        Thing ruuviThing = createRuuviThing("mygwid", BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02", 9_000_000);
        // Link all channels to freshly created items
        ruuviThing.getChannels().stream().map(Channel::getUID).forEach(this::linkChannelToAutogeneratedItem);

        @SuppressWarnings("null")
        Function<String, State> getItemState = channelId -> itemRegistry
                .get(itemName(ruuviThing.getChannel(channelId).getUID())).getState();

        AtomicInteger statusUpdateIndex = new AtomicInteger();
        waitForAssert(() -> {
            List<ThingStatusInfo> statusUpdates = statusSubscriber.statusUpdates.get(ruuviThing.getUID());
            assertNotNull(statusUpdates);

            assertThingStatus(statusUpdates, statusUpdateIndex.get(), ThingStatus.INITIALIZING);
            assertThingStatus(statusUpdates, statusUpdateIndex.get() + 1, ThingStatus.UNKNOWN);
            assertThingStatus(statusUpdates, statusUpdateIndex.get() + 2, ThingStatus.ONLINE, ThingStatusDetail.NONE,
                    "Waiting for initial data");

            statusUpdateIndex.set(statusUpdateIndex.get() + 3);
        });

        List<ThingStatusInfo> statusUpdates = statusSubscriber.statusUpdates.get(ruuviThing.getUID());
        assertNotNull(statusUpdates);
        if (quickTimeout) {
            triggerTimeoutHandling(ruuviThing);
            waitForAssert(() -> {
                assertThingStatus(statusUpdates, statusUpdateIndex.get(), ThingStatus.OFFLINE,
                        ThingStatusDetail.COMMUNICATION_ERROR, "No valid data received for some time");

                CHANNEL_TO_ITEM_TYPE.keySet()
                        .forEach(channelId -> assertEquals(UnDefType.UNDEF, getItemState.apply(channelId)));
                statusUpdateIndex.incrementAndGet();
            });
        }

        // publish some valid data ("valid case" test vector from
        // https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-5-rawv2)
        scheduler.execute(() -> publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02", """
                {\
                 "gw_mac": "DE:AD:BE:EF:00",\
                 "rssi": -82,\
                 "aoa": [],\
                 "gwts": "1659365432",\
                 "ts": "1659365222",\
                 "data": "0201061BFF99040512FC5394C37C0004FFFC040CAC364200CDCBB8334C884F",\
                 "coords": "" }"""));

        waitForAssert(() -> {
            assertThingStatus(statusUpdates, statusUpdateIndex.get(), ThingStatus.ONLINE);
            statusUpdateIndex.incrementAndGet();
        });

        waitForAssert(() -> {
            assertItems(getItemState, //
                    "24.3", // temperature, Celsius
                    "0.004", // acc X, g
                    "-0.004", // acc Y, g
                    "1.036", // acc Z, g
                    "2.9770000000000003", // battery, volt
                    5, // data format
                    "53.49", // humidity %
                    205, // measurement seq
                    66, // movement
                    "100044", // pressure, pascal
                    "4", // tx power, dBm
                    "-82", // RSSI, dBm
                    Instant.ofEpochSecond(1659365222), // ts
                    Instant.ofEpochSecond(1659365432), // gwts
                    "DE:AD:BE:EF:00" // gw mac
            );
        });

        if (quickTimeout) {
            triggerTimeoutHandling(ruuviThing);
            waitForAssert(() -> {
                assertThingStatus(statusUpdates, statusUpdateIndex.get(), ThingStatus.OFFLINE,
                        ThingStatusDetail.COMMUNICATION_ERROR, "No valid data received for some time");
                CHANNEL_TO_ITEM_TYPE.keySet()
                        .forEach(channelId -> assertEquals(UnDefType.UNDEF, getItemState.apply(channelId)));
                statusUpdateIndex.incrementAndGet();
            });
        }

        // Another mqtt update (("minimum values" test vector from
        // https://docs.ruuvi.com/communication/bluetooth-advertisements/data-format-5-rawv2)
        scheduler.execute(() -> publish(BASE_TOPIC_RUUVI + "/mygwid/DE:AD:BE:EF:BB:02", """
                {\
                 "gw_mac": "DE:AD:BE:EF:00",\
                 "rssi": -66,\
                 "aoa": [],\
                 "gwts": "1659365431",\
                 "ts": "1659365221",\
                 "data": "0201061BFF9904058001000000008001800180010000000000CBB8334C884F",\
                 "coords": "" }"""));
        if (quickTimeout) {
            // With quick timeout we were previously offline, so now we should be back online
            // with valid channels.
            waitForAssert(() -> {
                assertThingStatus(statusUpdates, statusUpdateIndex.get(), ThingStatus.ONLINE);
                statusUpdateIndex.getAndIncrement();
            });

            // ...after a while all items are updated
            waitForAssert(() -> {
                assertItems(getItemState, //
                        "-163.835", // temperature, Celsius
                        "-32.767", // acc X, g
                        "-32.767", // acc Y, g
                        "-32.767", // acc Z, g
                        "1.6", // battery, volt
                        5, // data format
                        "0.0", // humidity %
                        0, // measurement seq
                        0, // movement
                        "50000", // pressure, pascal
                        "-40", // tx power, dBm
                        "-66", // RSSI, dBm
                        Instant.ofEpochSecond(1659365221), // ts
                        Instant.ofEpochSecond(1659365431), // gwts
                        "DE:AD:BE:EF:00" // gw mac
                );
            });

            triggerTimeoutHandling(ruuviThing);
            waitForAssert(() -> {
                assertThingStatus(statusUpdates, statusUpdateIndex.get(), ThingStatus.OFFLINE,
                        ThingStatusDetail.COMMUNICATION_ERROR, "No valid data received for some time");
                CHANNEL_TO_ITEM_TYPE.keySet()
                        .forEach(channelId -> assertEquals(UnDefType.UNDEF, getItemState.apply(channelId)));
                statusUpdateIndex.getAndIncrement();
            });
        } else {
            // with non-quick timeout we are still online, and items are updated
            waitForAssert(() -> {
                assertItems(getItemState, //
                        "-163.835", // temperature, Celsius
                        "-32.767", // acc X, g
                        "-32.767", // acc Y, g
                        "-32.767", // acc Z, g
                        "1.6", // battery, volt
                        5, // data format
                        "0.0", // humidity %
                        0, // measurement seq
                        0, // movement
                        "50000", // pressure, pascal
                        "-40", // tx power, dBm
                        "-66", // RSSI, dBm
                        Instant.ofEpochSecond(1659365221), // ts
                        Instant.ofEpochSecond(1659365431), // gwts
                        "DE:AD:BE:EF:00" // gw mac
                );
            });
        }

        // assert that we have processed all status updates
        assertEquals(statusUpdateIndex.get(), statusUpdates.size());
    }
}
