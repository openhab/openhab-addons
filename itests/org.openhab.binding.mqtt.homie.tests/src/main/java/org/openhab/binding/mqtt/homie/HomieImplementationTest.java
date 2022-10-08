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
package org.openhab.binding.mqtt.homie;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.binding.mqtt.generic.tools.WaitForTopicValue;
import org.openhab.binding.mqtt.homie.internal.handler.HomieThingHandler;
import org.openhab.binding.mqtt.homie.internal.homie300.Device;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes.ReadyState;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceCallback;
import org.openhab.binding.mqtt.homie.internal.homie300.Node;
import org.openhab.binding.mqtt.homie.internal.homie300.NodeAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.Property;
import org.openhab.binding.mqtt.homie.internal.homie300.PropertyAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.PropertyAttributes.DataTypeEnum;
import org.openhab.binding.mqtt.homie.internal.homie300.PropertyHelper;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttConnectionObserver;
import org.openhab.core.io.transport.mqtt.MqttConnectionState;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.types.UnDefType;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homie device tree.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class HomieImplementationTest extends MqttOSGiTest {
    private static final String BASE_TOPIC = "homie";
    private static final String DEVICE_ID = ThingChannelConstants.TEST_HOME_THING.getId();
    private static final String DEVICE_TOPIC = BASE_TOPIC + "/" + DEVICE_ID;

    private @NonNullByDefault({}) MqttBrokerConnection homieConnection;
    private int registeredTopics = 100;

    // The handler is not tested here, so just mock the callback
    private @Mock @NonNullByDefault({}) DeviceCallback callback;

    // A handler mock is required to verify that channel value changes have been received
    private @Mock @NonNullByDefault({}) HomieThingHandler handler;

    private @NonNullByDefault({}) ScheduledExecutorService scheduler;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    private MqttConnectionObserver failIfChange = (state, error) -> assertThat(state,
            is(MqttConnectionState.CONNECTED));

    private String propertyTestTopic = "";

    @Override
    @BeforeEach
    public void beforeEach() throws Exception {
        super.beforeEach();

        homieConnection = createBrokerConnection("homie");

        // If the connection state changes in between -> fail
        homieConnection.addConnectionObserver(failIfChange);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(publish(DEVICE_TOPIC + "/$homie", "3.0"));
        futures.add(publish(DEVICE_TOPIC + "/$name", "Name"));
        futures.add(publish(DEVICE_TOPIC + "/$state", "ready"));
        futures.add(publish(DEVICE_TOPIC + "/$nodes", "testnode"));

        // Add homie node topics
        final String testNode = DEVICE_TOPIC + "/testnode";
        futures.add(publish(testNode + "/$name", "Testnode"));
        futures.add(publish(testNode + "/$type", "Type"));
        futures.add(publish(testNode + "/$properties", "temperature,doorbell,testRetain"));

        // Add homie property topics
        final String property = testNode + "/temperature";
        futures.add(publish(property, "10"));
        futures.add(publish(property + "/$name", "Testprop"));
        futures.add(publish(property + "/$settable", "true"));
        futures.add(publish(property + "/$unit", "°C"));
        futures.add(publish(property + "/$datatype", "float"));
        futures.add(publish(property + "/$format", "-100:100"));

        final String propertyBellTopic = testNode + "/doorbell";
        futures.add(publish(propertyBellTopic + "/$name", "Doorbell"));
        futures.add(publish(propertyBellTopic + "/$settable", "false"));
        futures.add(publish(propertyBellTopic + "/$retained", "false"));
        futures.add(publish(propertyBellTopic + "/$datatype", "boolean"));

        this.propertyTestTopic = testNode + "/testRetain";
        futures.add(publish(propertyTestTopic + "/$name", "Test"));
        futures.add(publish(propertyTestTopic + "/$settable", "true"));
        futures.add(publish(propertyTestTopic + "/$retained", "false"));
        futures.add(publish(propertyTestTopic + "/$datatype", "boolean"));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).get(5, TimeUnit.SECONDS);

        scheduler = new ScheduledThreadPoolExecutor(6);
    }

    @Override
    @AfterEach
    public void afterEach() throws Exception {
        if (homieConnection != null) {
            homieConnection.removeConnectionObserver(failIfChange);
            homieConnection.stop().get(5, TimeUnit.SECONDS);
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        super.afterEach();
    }

    @Test
    public void retrieveAllTopics() throws Exception {
        // four topics are not under /testnode !
        CountDownLatch c = new CountDownLatch(registeredTopics - 4);
        homieConnection.subscribe(DEVICE_TOPIC + "/testnode/#", (topic, payload) -> c.countDown()).get(5,
                TimeUnit.SECONDS);
        assertTrue(c.await(5, TimeUnit.SECONDS),
                "Connection " + homieConnection.getClientId() + " not retrieving all topics ");
    }

    @Test
    public void retrieveOneAttribute() throws Exception {
        WaitForTopicValue watcher = new WaitForTopicValue(homieConnection, DEVICE_TOPIC + "/$homie");
        assertThat(watcher.waitForTopicValue(1000), is("3.0"));
    }

    @SuppressWarnings("null")
    @Disabled("Temporarily disabled: unstable")
    @Test
    public void retrieveAttributes() throws Exception {
        assertThat(homieConnection.hasSubscribers(), is(false));

        Node node = new Node(DEVICE_TOPIC, "testnode", ThingChannelConstants.TEST_HOME_THING, callback,
                new NodeAttributes());
        Property property = spy(
                new Property(DEVICE_TOPIC + "/testnode", node, "temperature", callback, new PropertyAttributes()));

        // Create a scheduler
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);

        property.subscribe(homieConnection, scheduler, 500).get();

        assertThat(property.attributes.settable, is(true));
        assertThat(property.attributes.retained, is(true));
        assertThat(property.attributes.name, is("Testprop"));
        assertThat(property.attributes.unit, is("°C"));
        assertThat(property.attributes.datatype, is(DataTypeEnum.float_));
        waitForAssert(() -> assertThat(property.attributes.format, is("-100:100")));
        verify(property, timeout(500).atLeastOnce()).attributesReceived();

        // Receive property value
        ChannelState channelState = spy(property.getChannelState());
        PropertyHelper.setChannelState(property, channelState);

        property.startChannel(homieConnection, scheduler, 500).get();
        verify(channelState).start(any(), any(), anyInt());
        verify(channelState, timeout(500)).processMessage(any(), any());
        verify(callback).updateChannelState(any(), any());

        assertThat(property.getChannelState().getCache().getChannelState(),
                is(new QuantityType<>(10, SIUnits.CELSIUS)));

        property.stop().get();
        assertThat(homieConnection.hasSubscribers(), is(false));
    }

    // Inject a spy'ed property
    public Property createSpyProperty(InvocationOnMock invocation) {
        final Node node = (Node) invocation.getMock();
        final String id = (String) invocation.getArguments()[0];
        return spy(node.createProperty(id, spy(new PropertyAttributes())));
    }

    // Inject a spy'ed node
    public Node createSpyNode(InvocationOnMock invocation) {
        final Device device = (Device) invocation.getMock();
        final String id = (String) invocation.getArguments()[0];
        // Create the node
        Node node = spy(device.createNode(id, spy(new NodeAttributes())));
        // Intercept creating a property in the next call and inject a spy'ed property.
        doAnswer(this::createSpyProperty).when(node).createProperty(any());
        return node;
    }

    @SuppressWarnings("null")
    @Disabled("Temporarily disabled: unstable")
    @Test
    public void parseHomieTree() throws Exception {
        // Create a Homie Device object. Because spied Nodes are required for call verification,
        // the full Device constructor need to be used and a ChildMap object need to be created manually.
        ChildMap<Node> nodeMap = new ChildMap<>();
        Device device = spy(
                new Device(ThingChannelConstants.TEST_HOME_THING, callback, new DeviceAttributes(), nodeMap));

        // Intercept creating a node in initialize()->start() and inject a spy'ed node.
        doAnswer(this::createSpyNode).when(device).createNode(any());

        // initialize the device, subscribe and wait.
        device.initialize(BASE_TOPIC, DEVICE_ID, Collections.emptyList());
        device.subscribe(homieConnection, scheduler, 1500).get();

        assertThat(device.isInitialized(), is(true));

        // Check device attributes
        assertThat(device.attributes.homie, is("3.0"));
        assertThat(device.attributes.name, is("Name"));
        assertThat(device.attributes.state, is(ReadyState.ready));
        assertThat(device.attributes.nodes.length, is(1));
        verify(device, times(4)).attributeChanged(any(), any(), any(), any(), anyBoolean());
        verify(callback).readyStateChanged(eq(ReadyState.ready));
        verify(device).attributesReceived(any(), any(), anyInt());

        // Expect 1 node
        assertThat(device.nodes.size(), is(1));

        // Check node and node attributes
        Node node = device.nodes.get("testnode");
        verify(node).subscribe(any(), any(), anyInt());
        verify(node).attributesReceived(any(), any(), anyInt());
        verify(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        assertThat(node.attributes.type, is("Type"));
        assertThat(node.attributes.name, is("Testnode"));

        // Expect 2 property
        assertThat(node.properties.size(), is(3));

        // Check property and property attributes
        Property property = node.properties.get("temperature");
        assertThat(property.attributes.settable, is(true));
        assertThat(property.attributes.retained, is(true));
        assertThat(property.attributes.name, is("Testprop"));
        assertThat(property.attributes.unit, is("°C"));
        assertThat(property.attributes.datatype, is(DataTypeEnum.float_));
        assertThat(property.attributes.format, is("-100:100"));
        verify(property).attributesReceived();
        assertNotNull(property.getChannelState());
        assertThat(property.getType().getState().getMinimum().intValue(), is(-100));
        assertThat(property.getType().getState().getMaximum().intValue(), is(100));

        // Check property and property attributes
        Property propertyBell = node.properties.get("doorbell");
        verify(propertyBell).attributesReceived();
        assertThat(propertyBell.attributes.settable, is(false));
        assertThat(propertyBell.attributes.retained, is(false));
        assertThat(propertyBell.attributes.name, is("Doorbell"));
        assertThat(propertyBell.attributes.datatype, is(DataTypeEnum.boolean_));

        // The device->node->property tree is ready. Now subscribe to property values.
        device.startChannels(homieConnection, scheduler, 50, handler).get();
        assertThat(propertyBell.getChannelState().isStateful(), is(false));
        assertThat(propertyBell.getChannelState().getCache().getChannelState(), is(UnDefType.UNDEF));
        assertThat(property.getChannelState().getCache().getChannelState(),
                is(new QuantityType<>(10, SIUnits.CELSIUS)));

        property = node.properties.get("testRetain");
        WaitForTopicValue watcher = new WaitForTopicValue(brokerConnection, propertyTestTopic + "/set");
        // Watch the topic. Publish a retain=false value to MQTT
        property.getChannelState().publishValue(OnOffType.OFF).get();
        assertThat(watcher.waitForTopicValue(10000), is("false"));

        // Publish a retain=false value to MQTT.
        property.getChannelState().publishValue(OnOffType.ON).get();
        // No value is expected to be retained on this MQTT topic
        waitForAssert(() -> {
            WaitForTopicValue w = new WaitForTopicValue(brokerConnection, propertyTestTopic + "/set");
            assertNull(w.waitForTopicValue(50));
        }, 500, 100);
    }
}
