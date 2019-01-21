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
package org.openhab.binding.mqtt.generic;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.types.UnDefType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionObserver;
import org.eclipse.smarthome.io.transport.mqtt.MqttConnectionState;
import org.eclipse.smarthome.io.transport.mqtt.MqttService;
import org.eclipse.smarthome.test.java.JavaOSGiTest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.Device;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.DeviceCallback;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.Node;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.NodeAttributes;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.Property;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.PropertyHelper;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.DeviceAttributes.ReadyState;
import org.openhab.binding.mqtt.generic.internal.convention.homie300.PropertyAttributes.DataTypeEnum;
import org.openhab.binding.mqtt.generic.internal.generic.ChannelState;
import org.openhab.binding.mqtt.generic.internal.handler.HomieThingHandler;
import org.openhab.binding.mqtt.generic.internal.handler.ThingChannelConstants;
import org.openhab.binding.mqtt.generic.internal.tools.ChildMap;
import org.openhab.binding.mqtt.generic.internal.tools.WaitForTopicValue;
import org.osgi.service.cm.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A full implementation test, that starts the embedded MQTT broker and publishes a homie device tree.
 *
 * @author David Graeff - Initial contribution
 */
public class HomieImplementationTests extends JavaOSGiTest {
    final Logger logger = LoggerFactory.getLogger(HomieImplementationTests.class);
    private MqttService mqttService;
    private MqttBrokerConnection embeddedConnection;
    private MqttBrokerConnection connection;
    private int registeredTopics = 100;

    // The handler is not tested here, so just mock the callback
    @Mock
    DeviceCallback callback;

    // A handler mock is required to verify that channel value changes have been received
    @Mock
    HomieThingHandler handler;

    ScheduledExecutorService scheduler;

    /**
     * Create an observer that fails the test as soon as the broker client connection changes its connection state
     * to something else then CONNECTED.
     */
    MqttConnectionObserver failIfChange = new MqttConnectionObserver() {
        @Override
        public void connectionStateChanged(@NonNull MqttConnectionState state, @Nullable Throwable error) {
            assertThat(state, is(MqttConnectionState.CONNECTED));
        }
    };

    private final String baseTopic = "homie";
    private final String deviceID = ThingChannelConstants.testHomieThing.getId();
    private final String deviceTopic = baseTopic + "/" + deviceID;
    String propertyTestTopic;

    @Before
    public void setUp() throws InterruptedException, ConfigurationException, ExecutionException, TimeoutException {
        registerVolatileStorageService();
        initMocks(this);
        mqttService = getService(MqttService.class);

        // Wait for the EmbeddedBrokerService internal connection to be connected
        embeddedConnection = new EmbeddedBrokerTools().waitForConnection(mqttService);

        connection = new MqttBrokerConnection(embeddedConnection.getHost(), embeddedConnection.getPort(),
                embeddedConnection.isSecure(), "homie");
        connection.setQos(1);
        connection.start().get(200, TimeUnit.MILLISECONDS);
        assertThat(connection.connectionState(), is(MqttConnectionState.CONNECTED));
        // If the connection state changes in between -> fail
        connection.addConnectionObserver(failIfChange);

        embeddedConnection.setRetain(true);
        embeddedConnection.setQos(1);

        List<CompletableFuture<Boolean>> futures = new ArrayList<>();
        futures.add(embeddedConnection.publish(deviceTopic + "/$homie", "3.0".getBytes()));
        futures.add(embeddedConnection.publish(deviceTopic + "/$name", "Name".getBytes()));
        futures.add(embeddedConnection.publish(deviceTopic + "/$state", "ready".getBytes()));
        futures.add(embeddedConnection.publish(deviceTopic + "/$nodes", "testnode".getBytes()));

        // Add homie node topics
        final String testNode = deviceTopic + "/testnode";
        futures.add(embeddedConnection.publish(testNode + "/$name", "Testnode".getBytes()));
        futures.add(embeddedConnection.publish(testNode + "/$type", "Type".getBytes()));
        futures.add(
                embeddedConnection.publish(testNode + "/$properties", "temperature,doorbell,testRetain".getBytes()));

        // Add homie property topics
        final String property = testNode + "/temperature";
        futures.add(embeddedConnection.publish(property, "10".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$name", "Testprop".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$settable", "true".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$unit", "°C".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$datatype", "float".getBytes()));
        futures.add(embeddedConnection.publish(property + "/$format", "-100:100".getBytes()));

        final String propertyBellTopic = testNode + "/doorbell";
        futures.add(embeddedConnection.publish(propertyBellTopic + "/$name", "Doorbell".getBytes()));
        futures.add(embeddedConnection.publish(propertyBellTopic + "/$settable", "false".getBytes()));
        futures.add(embeddedConnection.publish(propertyBellTopic + "/$retained", "false".getBytes()));
        futures.add(embeddedConnection.publish(propertyBellTopic + "/$datatype", "boolean".getBytes()));

        this.propertyTestTopic = testNode + "/testRetain";
        futures.add(embeddedConnection.publish(propertyTestTopic + "/$name", "Test".getBytes()));
        futures.add(embeddedConnection.publish(propertyTestTopic + "/$settable", "true".getBytes()));
        futures.add(embeddedConnection.publish(propertyTestTopic + "/$retained", "false".getBytes()));
        futures.add(embeddedConnection.publish(propertyTestTopic + "/$datatype", "boolean".getBytes()));

        registeredTopics = futures.size();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()])).get(200, TimeUnit.MILLISECONDS);

        scheduler = new ScheduledThreadPoolExecutor(4);
    }

    @After
    public void tearDown() throws InterruptedException, ExecutionException, TimeoutException {
        if (connection != null) {
            connection.removeConnectionObserver(failIfChange);
            connection.stop().get(500, TimeUnit.MILLISECONDS);
        }
        scheduler.shutdownNow();
    }

    @Test
    public void retrieveAllTopics() throws InterruptedException, ExecutionException, TimeoutException {
        CountDownLatch c = new CountDownLatch(registeredTopics);
        connection.subscribe(deviceTopic + "/#", (topic, payload) -> c.countDown()).get(200, TimeUnit.MILLISECONDS);
        assertTrue("Connection " + connection.getClientId() + " not retrieving all topics",
                c.await(1000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void retrieveOneAttribute() throws InterruptedException, ExecutionException {
        WaitForTopicValue watcher = new WaitForTopicValue(connection, deviceTopic + "/$homie");
        assertThat(watcher.waitForTopicValue(100), is("3.0"));
    }

    @SuppressWarnings("null")
    @Test
    public void retrieveAttributes() throws InterruptedException, ExecutionException {
        assertThat(connection.hasSubscribers(), is(false));

        Node node = new Node(deviceTopic, "testnode", ThingChannelConstants.testHomieThing, callback,
                new NodeAttributes());
        Property property = spy(
                new Property(deviceTopic + "/testnode", node, "temperature", callback, new PropertyAttributes()));

        // Create a scheduler
        ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor(4);

        property.subscribe(connection, scheduler, 100).get();

        assertThat(property.attributes.settable, is(true));
        assertThat(property.attributes.retained, is(true));
        assertThat(property.attributes.name, is("Testprop"));
        assertThat(property.attributes.unit, is("°C"));
        assertThat(property.attributes.datatype, is(DataTypeEnum.float_));
        assertThat(property.attributes.format, is("-100:100"));
        verify(property).attributesReceived();

        // Receive property value
        ChannelState channelState = spy(property.getChannelState());
        PropertyHelper.setChannelState(property, channelState);

        property.startChannel(connection, scheduler, 200).get();
        verify(channelState).start(any(), any(), anyInt());
        verify(channelState).processMessage(any(), any());
        verify(callback).updateChannelState(any(), any());

        assertThat(property.getChannelState().getCache().getChannelState(), is(new DecimalType(10)));

        property.stop().get();
        assertThat(connection.hasSubscribers(), is(false));
    }

    // Inject a spy'ed property
    public Property createSpyProperty(InvocationOnMock invocation) {
        final Node node = (Node) invocation.getMock();
        final String id = (String) invocation.getArguments()[0];
        Property property = spy(node.createProperty(id, spy(new PropertyAttributes())));
        return property;
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
    @Test
    public void parseHomieTree() throws InterruptedException, ExecutionException, TimeoutException {
        // Create a Homie Device object. Because spied Nodes are required for call verification,
        // the full Device constructor need to be used and a ChildMap object need to be created manually.
        ChildMap<Node> nodeMap = new ChildMap<>();
        Device device = spy(
                new Device(ThingChannelConstants.testHomieThing, callback, new DeviceAttributes(), nodeMap));

        // Intercept creating a node in initialize()->start() and inject a spy'ed node.
        doAnswer(this::createSpyNode).when(device).createNode(any());

        // initialize the device, subscribe and wait.
        device.initialize(baseTopic, deviceID, Collections.emptyList());
        device.subscribe(connection, scheduler, 200).get();

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
        device.startChannels(connection, scheduler, 50, handler).get();
        assertThat(propertyBell.getChannelState().isStateful(), is(false));
        assertThat(propertyBell.getChannelState().getCache().getChannelState(), is(UnDefType.UNDEF));
        assertThat(property.getChannelState().getCache().getChannelState(), is(new DecimalType(10)));

        property = node.properties.get("testRetain");
        WaitForTopicValue watcher = new WaitForTopicValue(embeddedConnection, propertyTestTopic + "/set");
        // Watch the topic. Publish a retain=false value to MQTT
        property.getChannelState().publishValue(OnOffType.OFF).get();
        assertThat(watcher.waitForTopicValue(50), is("false"));

        // Publish a retain=false value to MQTT.
        property.getChannelState().publishValue(OnOffType.ON).get();
        // This test is flaky if the MQTT broker does not get a time to "forget" this non-retained value
        Thread.sleep(50);
        // No value is expected to be retained on this MQTT topic
        watcher = new WaitForTopicValue(embeddedConnection, propertyTestTopic + "/set");
        assertNull(watcher.waitForTopicValue(50));
    }
}
