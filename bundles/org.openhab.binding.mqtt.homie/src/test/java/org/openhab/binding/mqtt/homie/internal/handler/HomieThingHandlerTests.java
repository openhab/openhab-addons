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
package org.openhab.binding.mqtt.homie.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mqtt.homie.internal.handler.ThingChannelConstants.TEST_HOMIE_THING;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.binding.mqtt.generic.mapping.SubscribeFieldToMQTTtopic;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.binding.mqtt.generic.tools.DelayedBatchProcessing;
import org.openhab.binding.mqtt.generic.values.Value;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.binding.mqtt.homie.ChannelStateHelper;
import org.openhab.binding.mqtt.homie.ThingHandlerHelper;
import org.openhab.binding.mqtt.homie.generic.internal.MqttBindingConstants;
import org.openhab.binding.mqtt.homie.internal.homie300.Device;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceAttributes.ReadyState;
import org.openhab.binding.mqtt.homie.internal.homie300.Node;
import org.openhab.binding.mqtt.homie.internal.homie300.NodeAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.Property;
import org.openhab.binding.mqtt.homie.internal.homie300.PropertyAttributes;
import org.openhab.binding.mqtt.homie.internal.homie300.PropertyAttributes.DataTypeEnum;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.thing.type.ThingTypeRegistry;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.TypeParser;

/**
 * Tests cases for {@link HomieThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class HomieThingHandlerTests {

    private Thing thing;

    private @Mock AbstractBrokerHandler bridgeHandler;
    private @Mock ThingHandlerCallback callback;
    private @Mock MqttBrokerConnection connection;
    private @Mock ScheduledExecutorService scheduler;
    private @Mock ScheduledFuture<?> scheduledFuture;
    private @Mock ThingTypeRegistry thingTypeRegistry;

    private HomieThingHandler thingHandler;

    private final MqttChannelTypeProvider channelTypeProvider = new MqttChannelTypeProvider(thingTypeRegistry);

    private final String deviceID = ThingChannelConstants.TEST_HOMIE_THING.getId();
    private final String deviceTopic = "homie/" + deviceID;

    // A completed future is returned for a subscribe call to the attributes
    private CompletableFuture<@Nullable Void> future = CompletableFuture.completedFuture(null);

    @BeforeEach
    public void setUp() {
        final ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        final Configuration config = new Configuration();
        config.put("basetopic", "homie");
        config.put("deviceid", deviceID);

        thing = ThingBuilder.create(MqttBindingConstants.HOMIE300_MQTT_THING, TEST_HOMIE_THING.getId())
                .withConfiguration(config).build();
        thing.setStatusInfo(thingStatus);

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());

        doReturn(false).when(scheduledFuture).isDone();
        doReturn(scheduledFuture).when(scheduler).schedule(any(Runnable.class), anyLong(), any(TimeUnit.class));

        final HomieThingHandler handler = new HomieThingHandler(thing, channelTypeProvider, 1000, 30, 5);
        thingHandler = spy(handler);
        thingHandler.setCallback(callback);
        final Device device = new Device(thing.getUID(), thingHandler, spy(new DeviceAttributes()),
                spy(new ChildMap<>()));
        thingHandler.setInternalObjects(spy(device), spy(new DelayedBatchProcessing<>(500, thingHandler, scheduler)));

        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test
    public void initialize() {
        assertThat(thingHandler.device.isInitialized(), is(false));
        // // A completed future is returned for a subscribe call to the attributes
        doReturn(future).when(thingHandler.device.attributes).subscribeAndReceive(any(), any(), anyString(), any(),
                anyInt());
        doReturn(future).when(thingHandler.device.attributes).unsubscribe();
        // Prevent a call to accept, that would update our thing.
        doNothing().when(thingHandler).accept(any());
        // Pretend that a device state change arrived.
        thingHandler.device.attributes.state = ReadyState.ready;

        verify(callback, times(0)).statusUpdated(eq(thing), any());

        thingHandler.initialize();

        // Expect a call to the bridge status changed, the start, the propertiesChanged method
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler).start(any());
        verify(thingHandler).readyStateChanged(any());
        verify(thingHandler.device.attributes).subscribeAndReceive(any(), any(),
                argThat(arg -> deviceTopic.equals(arg)), any(), anyInt());

        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.ONLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.NONE)));
    }

    @Test
    public void initializeGeneralTimeout() throws InterruptedException {
        // A non completed future is returned for a subscribe call to the attributes
        doReturn(future).when(thingHandler.device.attributes).subscribeAndReceive(any(), any(), anyString(), any(),
                anyInt());
        doReturn(future).when(thingHandler.device.attributes).unsubscribe();

        // Prevent a call to accept, that would update our thing.
        doNothing().when(thingHandler).accept(any());

        thingHandler.initialize();

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.OFFLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    public void initializeNoStateReceived() throws InterruptedException {
        // A completed future is returned for a subscribe call to the attributes
        doReturn(future).when(thingHandler.device.attributes).subscribeAndReceive(any(), any(), anyString(), any(),
                anyInt());
        doReturn(future).when(thingHandler.device.attributes).unsubscribe();

        // Prevent a call to accept, that would update our thing.
        doNothing().when(thingHandler).accept(any());

        thingHandler.initialize();
        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.OFFLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.GONE)));
    }

    @SuppressWarnings("null")
    @Test
    public void handleCommandRefresh() {
        // Create mocked homie device tree with one node and one read-only property
        Node node = thingHandler.device.createNode("node", spy(new NodeAttributes()));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        node.attributes.name = "testnode";

        Property property = node.createProperty("property", spy(new PropertyAttributes()));
        doReturn(future).when(property.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(property.attributes).unsubscribe();
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributes.settable = false;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        ThingHandlerHelper.setConnection(thingHandler, connection);
        // we need to set a channel value first, undefined values ignored on REFRESH
        property.getChannelState().getCache().update(new StringType("testString"));

        thingHandler.handleCommand(property.channelUID, RefreshType.REFRESH);
        verify(callback).stateUpdated(argThat(arg -> property.channelUID.equals(arg)),
                argThat(arg -> property.getChannelState().getCache().getChannelState().equals(arg)));
    }

    @SuppressWarnings("null")
    @Test
    public void handleCommandUpdate() {
        // Create mocked homie device tree with one node and one writable property
        Node node = thingHandler.device.createNode("node", spy(new NodeAttributes()));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        node.attributes.name = "testnode";

        Property property = node.createProperty("property", spy(new PropertyAttributes()));
        doReturn(future).when(property.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(property.attributes).unsubscribe();
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;
        property.attributes.settable = true;
        property.attributesReceived();
        node.properties.put(property.propertyID, property);
        thingHandler.device.nodes.put(node.nodeID, node);

        ChannelState channelState = requireNonNull(property.getChannelState());
        assertNotNull(channelState);
        ChannelStateHelper.setConnection(channelState, connection);// Pretend we called start()
        ThingHandlerHelper.setConnection(thingHandler, connection);

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(property.channelUID, updateValue);

        assertThat(property.getChannelState().getCache().getChannelState().toString(), is("UPDATE"));
        verify(connection, times(1)).publish(any(), any(), anyInt(), anyBoolean());

        // Check non writable property
        property.attributes.settable = false;
        property.attributesReceived();
        // Assign old value
        Value value = property.getChannelState().getCache();
        Command command = TypeParser.parseCommand(value.getSupportedCommandTypes(), "OLDVALUE");
        if (command != null) {
            property.getChannelState().getCache().update(command);
            // Try to update with new value
            updateValue = new StringType("SOMETHINGNEW");
            thingHandler.handleCommand(property.channelUID, updateValue);
            // Expect old value and no MQTT publish
            assertThat(property.getChannelState().getCache().getChannelState().toString(), is("OLDVALUE"));
            verify(connection, times(1)).publish(any(), any(), anyInt(), anyBoolean());
        }
    }

    public Object createSubscriberAnswer(InvocationOnMock invocation) {
        final AbstractMqttAttributeClass attributes = (AbstractMqttAttributeClass) invocation.getMock();
        final ScheduledExecutorService scheduler = (ScheduledExecutorService) invocation.getArguments()[0];
        final Field field = (Field) invocation.getArguments()[1];
        final String topic = (String) invocation.getArguments()[2];
        final boolean mandatory = (boolean) invocation.getArguments()[3];
        final SubscribeFieldToMQTTtopic s = spy(
                new SubscribeFieldToMQTTtopic(scheduler, field, attributes, topic, mandatory));
        doReturn(CompletableFuture.completedFuture(true)).when(s).subscribeAndReceive(any(), anyInt());
        return s;
    }

    public Property createSpyProperty(String propertyID, Node node) {
        // Create a property with the same ID and insert it instead
        Property property = spy(node.createProperty(propertyID, spy(new PropertyAttributes())));
        doAnswer(this::createSubscriberAnswer).when(property.attributes).createSubscriber(any(), any(), any(),
                anyBoolean());
        property.attributes.name = "testprop";
        property.attributes.datatype = DataTypeEnum.string_;

        return property;
    }

    public Node createSpyNode(String propertyID, Device device) {
        // Create the node
        Node node = spy(device.createNode("node", spy(new NodeAttributes())));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        node.attributes.name = "testnode";
        node.attributes.properties = new String[] { "property" };
        doAnswer(this::createSubscriberAnswer).when(node.attributes).createSubscriber(any(), any(), any(),
                anyBoolean());

        // Intercept creating a property in the next call and inject a spy'ed property.
        doAnswer(i -> createSpyProperty("property", node)).when(node).createProperty(any());

        return node;
    }

    @Test
    public void propertiesChanged() throws InterruptedException, ExecutionException {
        thingHandler.device.initialize("homie", "device", new ArrayList<>());
        ThingHandlerHelper.setConnection(thingHandler, connection);

        // Create mocked homie device tree with one node and one property
        doAnswer(this::createSubscriberAnswer).when(thingHandler.device.attributes).createSubscriber(any(), any(),
                any(), anyBoolean());

        thingHandler.device.attributes.state = ReadyState.ready;
        thingHandler.device.attributes.name = "device";
        thingHandler.device.attributes.homie = "3.0";
        thingHandler.device.attributes.nodes = new String[] { "node" };

        // Intercept creating a node in initialize()->start() and inject a spy'ed node.
        doAnswer(i -> createSpyNode("node", thingHandler.device)).when(thingHandler.device).createNode(any());

        verify(thingHandler, times(0)).nodeAddedOrChanged(any());
        verify(thingHandler, times(0)).propertyAddedOrChanged(any());

        thingHandler.initialize();

        assertThat(thingHandler.device.isInitialized(), is(true));

        verify(thingHandler).propertyAddedOrChanged(any());
        verify(thingHandler).nodeAddedOrChanged(any());

        verify(thingHandler.device).subscribe(any(), any(), anyInt());
        verify(thingHandler.device).attributesReceived(any(), any(), anyInt());

        assertNotNull(thingHandler.device.nodes.get("node").properties.get("property"));

        assertTrue(thingHandler.delayedProcessing.isArmed());

        // Simulate waiting for the delayed processor
        thingHandler.delayedProcessing.forceProcessNow();

        // Called for the updated property + for the new channels
        verify(callback, atLeast(2)).thingUpdated(any());

        final List<@NonNull Channel> channels = thingHandler.getThing().getChannels();
        assertThat(channels.size(), is(1));
        assertThat(channels.get(0).getLabel(), is("testprop"));
        assertThat(channels.get(0).getKind(), is(ChannelKind.STATE));

        final Map<@NonNull String, @NonNull String> properties = thingHandler.getThing().getProperties();
        assertThat(properties.get(MqttBindingConstants.HOMIE_PROPERTY_VERSION), is("3.0"));
        assertThat(properties.size(), is(1));
    }
}
