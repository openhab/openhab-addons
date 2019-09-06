/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.mqtt;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.State;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.core.util.UIDUtils;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.mqtt.generic.ChannelStateUpdateListener;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.homeassistant.internal.AbstractComponent;
import org.openhab.binding.mqtt.homeassistant.internal.ChannelConfigurationTypeAdapterFactory;
import org.openhab.binding.mqtt.homeassistant.internal.ComponentSwitch;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents;
import org.openhab.binding.mqtt.homeassistant.internal.DiscoverComponents.ComponentDiscovered;
import org.openhab.binding.mqtt.homeassistant.internal.HaID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homeassistant MQTT discovery device
 * tree.
 *
 * @author David Graeff - Initial contribution
 */
public class HomeAssistantMQTTImplementationTest extends JavaOSGiTest {
    private MqttService mqttService;
    private MqttBrokerConnection embeddedConnection;
    private MqttBrokerConnection connection;
    private int registeredTopics = 100;
    private Throwable failure = null;

    @Mock
    ChannelStateUpdateListener channelStateUpdateListener;

    @Mock
    TransformationServiceProvider transformationServiceProvider;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    private MqttConnectionObserver failIfChange = (state, error) -> assertThat(state,
            is(MqttConnectionState.CONNECTED));
    private String testObjectTopic;

    @Before
    public void setUp() throws InterruptedException, ExecutionException, TimeoutException, IOException {
        registerVolatileStorageService();
        initMocks(this);
        mqttService = getService(MqttService.class);

        // Wait for the EmbeddedBrokerService internal connection to be connected
        embeddedConnection = new EmbeddedBrokerTools().waitForConnection(mqttService);
        embeddedConnection.setRetain(true);
        embeddedConnection.setQos(1);

        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "ha_mqtt");
        connection.setQos(1);
        connection.setRetain(true);
        connection.setPersistencePath(Paths.get("subconn"));
        connection.start().get(1000, TimeUnit.MILLISECONDS);
        assertThat(connection.connectionState(), is(MqttConnectionState.CONNECTED));

        // If the connection state changes in between -> fail
        connection.addConnectionObserver(failIfChange);

        // Create topic string and config for one example HA component (a Switch)
        testObjectTopic = "homeassistant/switch/node/" + ThingChannelConstants.testHomeAssistantThing.getId();
        final String config = "{'name':'testname','state_topic':'" + testObjectTopic + "/state','command_topic':'" + testObjectTopic + "/set'}";

        // Publish component configurations and component states to MQTT
        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(embeddedConnection.publish(testObjectTopic + "/config", config.getBytes()));
        futures.add(embeddedConnection.publish(testObjectTopic + "/state", "true".getBytes()));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(1000, TimeUnit.MILLISECONDS);

        failure = null;

        doReturn(null).when(transformationServiceProvider).getTransformationService(any());
    }

    @After
    public void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
        if (connection != null) {
            connection.removeConnectionObserver(failIfChange);
            connection.stop().get(1000, TimeUnit.MILLISECONDS);
        }
    }

    @Test
    public void reconnectTest() throws InterruptedException, ExecutionException, TimeoutException {
        connection.removeConnectionObserver(failIfChange);
        connection.stop().get(2000, TimeUnit.MILLISECONDS);
        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "ha_mqtt");
        connection.start().get(2000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void retrieveAllTopics() throws InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch c = new CountDownLatch(registeredTopics);
        connection.subscribe("homeassistant/+/+/" + ThingChannelConstants.testHomeAssistantThing.getId() + "/#",
                (topic, payload) -> c.countDown()).get(1000, TimeUnit.MILLISECONDS);
        assertTrue("Connection " + connection.getClientId() + " not retrieving all topics",
                c.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void parseHATree() throws InterruptedException, ExecutionException, TimeoutException {
        MqttChannelTypeProvider channelTypeProvider = mock(MqttChannelTypeProvider.class);

        final Map<String, AbstractComponent<?>> haComponents = new HashMap<String, AbstractComponent<?>>();
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ChannelConfigurationTypeAdapterFactory()).create();

        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);
        DiscoverComponents discover = spy(
                new DiscoverComponents(ThingChannelConstants.testHomeAssistantThing, scheduler,
                        channelStateUpdateListener, gson, transformationServiceProvider));

        // The DiscoverComponents object calls ComponentDiscovered callbacks.
        // In the following implementation we add the found component to the `haComponents` map
        // and add the types to the channelTypeProvider, like in the real Thing handler.
        final CountDownLatch latch = new CountDownLatch(1);
        ComponentDiscovered cd = (haID, c) -> {
            haComponents.put(c.uid().getId(), c);
            c.addChannelTypes(channelTypeProvider);
            channelTypeProvider.setChannelGroupType(c.groupTypeUID(), c.type());
            latch.countDown();
        };

        // Start the discovery for 100ms. Forced timeout after 300ms.
        HaID haID = new HaID(testObjectTopic + "/config");
        CompletableFuture<Void> future = discover.startDiscovery(connection, 500, Collections.singleton(haID), cd)
                .thenRun(() -> {
                }).exceptionally(e -> {
                    failure = e;
                    return null;
                });

        assertTrue(latch.await(1000, TimeUnit.MILLISECONDS));
        future.get(800, TimeUnit.MILLISECONDS);

        // No failure expected and one discovered result
        assertNull(failure);
        assertThat(haComponents.size(), is(1));

        // For the switch component we should have one channel group type and one channel type
        // setChannelGroupType is called once above
        verify(channelTypeProvider, times(2)).setChannelGroupType(any(), any());
        verify(channelTypeProvider, times(1)).setChannelType(any(), any());

        String channelGroupId = UIDUtils
                .encode("node_" + ThingChannelConstants.testHomeAssistantThing.getId() + "_switch");

        State value = haComponents.get(channelGroupId).channelTypes().get(ComponentSwitch.switchChannelID).getState()
                .getCache().getChannelState();
        assertThat(value, is(UnDefType.UNDEF));

        haComponents.values().stream().map(e -> e.start(connection, scheduler, 100))
                .reduce(CompletableFuture.completedFuture(null), (a, v) -> a.thenCompose(b -> v)).exceptionally(e -> {
            failure = e;
            return null;
        }).get();

        // We should have received the retained value, while subscribing to the channels MQTT state topic.
        verify(channelStateUpdateListener, timeout(1000).times(1)).updateChannelState(any(), any());

        // Value should be ON now.
        value = haComponents.get(channelGroupId).channelTypes().get(ComponentSwitch.switchChannelID).getState()
                .getCache().getChannelState();
        assertThat(value, is(OnOffType.ON));
    }
}
