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
package org.openhab.binding.mqtt.homeassistant.internal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.handler.BrokerHandler;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttBindingConstants;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeBuilder;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.util.BundleResolver;

/**
 * Abstract class for HomeAssistant unit tests.
 *
 * @author Anton Kharuzhy - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public abstract class AbstractHomeAssistantTests extends JavaTest {
    public static final String BINDING_ID = "mqtt";

    public static final String BRIDGE_TYPE_ID = "broker";
    public static final String BRIDGE_TYPE_LABEL = "MQTT Broker";
    public static final ThingTypeUID BRIDGE_TYPE_UID = new ThingTypeUID(BINDING_ID, BRIDGE_TYPE_ID);
    public static final String BRIDGE_ID = UUID.randomUUID().toString();
    public static final ThingUID BRIDGE_UID = new ThingUID(BRIDGE_TYPE_UID, BRIDGE_ID);

    public static final String HA_TYPE_LABEL = "Home Assistant Thing";
    public static final ThingTypeUID HA_TYPE_UID = new ThingTypeUID(BINDING_ID, "homeassistant_dynamic_type");
    public static final String HA_ID = UUID.randomUUID().toString();
    public static final ThingUID HA_UID = new ThingUID(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, HA_ID);
    public static final ThingType HA_THING_TYPE = ThingTypeBuilder
            .instance(MqttBindingConstants.HOMEASSISTANT_MQTT_THING, HA_TYPE_LABEL).build();
    protected static final HomeAssistantPythonBridge PYTHON = new HomeAssistantPythonBridge();

    protected @Mock @NonNullByDefault({}) MqttBrokerConnection bridgeConnection;
    protected @Mock @NonNullByDefault({}) ThingTypeRegistry thingTypeRegistry;

    protected @NonNullByDefault({}) MqttChannelTypeProvider channelTypeProvider;
    protected @NonNullByDefault({}) MqttChannelStateDescriptionProvider stateDescriptionProvider;
    protected @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;

    protected final Bridge bridgeThing = BridgeBuilder.create(BRIDGE_TYPE_UID, BRIDGE_UID).build();
    protected final BrokerHandler bridgeHandler = spy(new BrokerHandler(bridgeThing));
    protected Thing haThing = ThingBuilder.create(HA_TYPE_UID, HA_UID).withBridge(BRIDGE_UID).build();
    protected final ConcurrentMap<String, Set<MqttMessageSubscriber>> subscriptions = new ConcurrentHashMap<>();

    private @Mock @NonNullByDefault({}) TranslationProvider translationProvider;
    private @Mock @NonNullByDefault({}) BundleResolver bundleResolver;

    @BeforeEach
    public void beforeEachAbstractHomeAssistantTests() {
        when(thingTypeRegistry.getThingType(BRIDGE_TYPE_UID))
                .thenReturn(ThingTypeBuilder.instance(BRIDGE_TYPE_UID, BRIDGE_TYPE_LABEL).build());
        when(thingTypeRegistry.getThingType(MqttBindingConstants.HOMEASSISTANT_MQTT_THING)).thenReturn(HA_THING_TYPE);

        channelTypeProvider = spy(new MqttChannelTypeProvider(thingTypeRegistry, new VolatileStorageService()));
        stateDescriptionProvider = spy(new MqttChannelStateDescriptionProvider(translationProvider, bundleResolver));
        channelTypeRegistry = spy(new ChannelTypeRegistry());

        setupConnection();

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(bridgeConnection));

        bridgeThing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.ONLINE.NONE, ""));
        bridgeThing.setHandler(bridgeHandler);

        haThing.setStatusInfo(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.ONLINE.NONE, ""));
    }

    protected void setupConnection() {
        doAnswer(invocation -> {
            final var topic = (String) invocation.getArgument(0);
            final var subscriber = (MqttMessageSubscriber) invocation.getArgument(1);

            subscriptions.putIfAbsent(topic, ConcurrentHashMap.newKeySet());
            Set<MqttMessageSubscriber> subscribers = subscriptions.get(topic);
            Objects.requireNonNull(subscribers); // Invariant, thanks to putIfAbsent above. To make compiler happy
            subscribers.add(subscriber);
            return CompletableFuture.completedFuture(true);
        }).when(bridgeConnection).subscribe(any(), any());

        doAnswer(invocation -> {
            final var topic = (String) invocation.getArgument(0);
            final var subscriber = (MqttMessageSubscriber) invocation.getArgument(1);
            final var topicSubscriptions = subscriptions.get(topic);

            if (topicSubscriptions != null) {
                topicSubscriptions.remove(subscriber);
            }
            return CompletableFuture.completedFuture(true);
        }).when(bridgeConnection).unsubscribe(any(), any());

        doAnswer(invocation -> {
            subscriptions.clear();
            return CompletableFuture.completedFuture(true);
        }).when(bridgeConnection).unsubscribeAll();

        doReturn(CompletableFuture.completedFuture(true)).when(bridgeConnection).publish(any(), any(), anyInt(),
                anyBoolean());
    }

    /**
     * @param relativePath path from src/test/java/org/openhab/binding/mqtt/homeassistant/internal
     * @return path
     */
    @SuppressWarnings("null")
    protected Path getResourcePath(String relativePath) {
        try {
            return Paths.get(AbstractHomeAssistantTests.class.getResource(relativePath).toURI());
        } catch (URISyntaxException e) {
            Assertions.fail(e);
        }
        throw new IllegalArgumentException();
    }

    protected String getResourceAsString(String relativePath) {
        try {
            return Files.readString(getResourcePath(relativePath));
        } catch (IOException e) {
            Assertions.fail(e);
        }
        throw new IllegalArgumentException();
    }

    protected byte[] getResourceAsByteArray(String relativePath) {
        try {
            return Files.readAllBytes(getResourcePath(relativePath));
        } catch (IOException e) {
            Assertions.fail(e);
        }
        throw new IllegalArgumentException();
    }

    protected static String configTopicToMqtt(String configTopic) {
        return HandlerConfiguration.DEFAULT_BASETOPIC + "/" + configTopic + "/config";
    }
}
