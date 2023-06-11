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
package org.openhab.binding.mqtt.homeassistant;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.AvailabilityTracker;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.binding.mqtt.homeassistant.internal.component.Switch;
import org.openhab.binding.mqtt.homeassistant.internal.config.ChannelConfigurationTypeAdapterFactory;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.openhab.core.util.UIDUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homeassistant MQTT discovery device
 * tree.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class HomeAssistantMQTTImplementationTest extends MqttOSGiTest {

    private @NonNullByDefault({}) MqttBrokerConnection haConnection;
    private int registeredTopics = 100;
    private @Nullable Throwable failure;

    private @Mock @NonNullByDefault({}) ChannelStateUpdateListener channelStateUpdateListener;
    private @Mock @NonNullByDefault({}) AvailabilityTracker availabilityTracker;
    private @Mock @NonNullByDefault({}) TransformationServiceProvider transformationServiceProvider;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    private final MqttConnectionObserver failIfChange = (state, error) -> assertThat(state,
            is(MqttConnectionState.CONNECTED));
    private final String testObjectTopic = "homeassistant/switch/node/"
            + ThingChannelConstants.TEST_HOME_ASSISTANT_THING.getId();

    @Override
    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();

        haConnection = createBrokerConnection("ha_mqtt");

        // If the connection state changes in between -> fail
        haConnection.addConnectionObserver(failIfChange);

        // Create topic string and config for one example HA component (a Switch)
        final String config = "{'name':'testname','state_topic':'" + testObjectTopic + "/state','command_topic':'"
                + testObjectTopic + "/set'}";

        // Publish component configurations and component states to MQTT
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(publish(testObjectTopic + "/config", config));
        futures.add(publish(testObjectTopic + "/state", "ON"));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(2, TimeUnit.SECONDS);

        failure = null;

        doReturn(null).when(transformationServiceProvider).getTransformationService(any());
    }

    @Override
    @AfterEach
    public void afterEach() throws Exception {
        if (haConnection != null) {
            haConnection.removeConnectionObserver(failIfChange);
            haConnection.stop().get(5, TimeUnit.SECONDS);
        }

        super.afterEach();
    }

    @Test
    public void reconnectTest() throws Exception {
        haConnection.removeConnectionObserver(failIfChange);
        haConnection.stop().get(5, TimeUnit.SECONDS);
        haConnection = createBrokerConnection("ha_mqtt");
    }

    @Test
    public void retrieveAllTopics() throws Exception {
        CountDownLatch c = new CountDownLatch(registeredTopics);
        haConnection.subscribe("homeassistant/+/+/" + ThingChannelConstants.TEST_HOME_ASSISTANT_THING.getId() + "/#",
                (topic, payload) -> c.countDown()).get(5, TimeUnit.SECONDS);
        assertTrue(c.await(2, TimeUnit.SECONDS),
                "Connection " + haConnection.getClientId() + " not retrieving all topics");
    }

    @Test
    public void parseHATree() throws Exception {
        MqttChannelTypeProvider channelTypeProvider = mock(MqttChannelTypeProvider.class);

        final Map<String, AbstractComponent<?>> haComponents = new HashMap<>();
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();

        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
        DiscoverComponents discover = spy(new DiscoverComponents(ThingChannelConstants.TEST_HOME_ASSISTANT_THING,
                scheduler, channelStateUpdateListener, availabilityTracker, gson, transformationServiceProvider));

        // The DiscoverComponents object calls ComponentDiscovered callbacks.
        // In the following implementation we add the found component to the `haComponents` map
        // and add the types to the channelTypeProvider, like in the real Thing handler.
        final CountDownLatch latch = new CountDownLatch(1);
        ComponentDiscovered cd = (haID, c) -> {
            haComponents.put(c.getGroupUID().getId(), c);
            c.addChannelTypes(channelTypeProvider);
            channelTypeProvider.setChannelGroupType(c.getGroupTypeUID(), c.getType());
            latch.countDown();
        };

        // Start the discovery for 2000ms. Forced timeout after 4000ms.
        HaID haID = new HaID(testObjectTopic + "/config");
        CompletableFuture<Void> future = discover.startDiscovery(haConnection, 2000, Collections.singleton(haID), cd)
                .thenRun(() -> {
                }).exceptionally(e -> {
                    failure = e;
                    return null;
                });

        assertTrue(latch.await(4, TimeUnit.SECONDS));
        future.get(5, TimeUnit.SECONDS);

        // No failure expected and one discovered result
        assertNull(failure);
        assertThat(haComponents.size(), is(1));

        // For the switch component we should have one channel group type and one channel type
        // setChannelGroupType is called once above
        verify(channelTypeProvider, times(2)).setChannelGroupType(any(), any());
        verify(channelTypeProvider, times(1)).setChannelType(any(), any());

        String channelGroupId = UIDUtils
                .encode("node_" + ThingChannelConstants.TEST_HOME_ASSISTANT_THING.getId() + "_switch");

        State value = haComponents.get(channelGroupId).getChannel(Switch.SWITCH_CHANNEL_ID).getState().getCache()
                .getChannelState();
        assertThat(value, is(UnDefType.UNDEF));

        haComponents.values().stream().map(e -> e.start(haConnection, scheduler, 100))
                .reduce(CompletableFuture.completedFuture(null), (a, v) -> a.thenCompose(b -> v)).exceptionally(e -> {
                    failure = e;
                    return null;
                }).get();

        // We should have received the retained value, while subscribing to the channels MQTT state topic.
        verify(channelStateUpdateListener, timeout(4000).times(1)).updateChannelState(any(), any());

        // Value should be ON now.
        value = haComponents.get(channelGroupId).getChannel(Switch.SWITCH_CHANNEL_ID).getState().getCache()
                .getChannelState();
        assertThat(value, is(OnOffType.ON));
    }
}
