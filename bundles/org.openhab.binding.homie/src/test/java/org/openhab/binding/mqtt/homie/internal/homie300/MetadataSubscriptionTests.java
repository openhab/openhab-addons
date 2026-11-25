/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.homie.internal.homie300;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;
import org.openhab.binding.homie.internal.handler.HomieThingHandler;
import org.openhab.binding.homie.internal.handler.ThingChannelConstants;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.io.transport.mqtt.MqttMessageSubscriber;

/**
 * Tests metadata changes and shutdown while child subscriptions are pending.
 *
 * @author Cody Cutrer - Initial contribution
 */
@NonNullByDefault
class MetadataSubscriptionTests {
    private final DeviceCallback callback = mock(DeviceCallback.class);
    private final MqttBrokerConnection connection = mock(MqttBrokerConnection.class);
    private final ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
    private final DeviceAttributes deviceAttributes = spy(new DeviceAttributes());
    private final Device device = spy(new Device(ThingChannelConstants.TEST_HOMIE_THING, callback, deviceAttributes));
    private final NodeAttributes nodeAttributes = spy(new NodeAttributes());
    private final Node node = spy(
            new Node("homie/device", "node", ThingChannelConstants.TEST_HOMIE_THING, callback, nodeAttributes));
    private final CompletableFuture<@Nullable Void> pending = new CompletableFuture<>();

    @BeforeEach
    void setUp() {
        device.initialize("homie", "device", List.of());
        deviceAttributes.nodes = new String[] { "restored", "new" };
        deviceAttributes.state = DeviceAttributes.ReadyState.init;
        nodeAttributes.properties = new String[] { "restored", "new" };
        stubAttributes(deviceAttributes);
        stubAttributes(nodeAttributes);
    }

    private void stubAttributes(AbstractMqttAttributeClass attributes) {
        doReturn(CompletableFuture.completedFuture(null)).when(attributes).subscribeAndReceive(any(), any(),
                anyString(), any(), anyInt());
        doReturn(CompletableFuture.completedFuture(null)).when(attributes).unsubscribe();
        doReturn(true).when(attributes).isComplete();
    }

    private Node childNode(String id, CompletableFuture<@Nullable Void> subscription) {
        Node child = spy(
                new Node("homie/device", id, ThingChannelConstants.TEST_HOMIE_THING, callback, new NodeAttributes()));
        doReturn(subscription).when(child).subscribe(any(), any(), anyInt());
        doReturn(CompletableFuture.completedFuture(null)).when(child).stop();
        return child;
    }

    private Property childProperty(String id, CompletableFuture<@Nullable Void> subscription) {
        Property child = spy(node.createProperty(id, new PropertyAttributes()));
        doReturn(subscription).when(child).subscribe(any(), any(), anyInt());
        doReturn(CompletableFuture.completedFuture(null)).when(child).stop();
        return child;
    }

    @Test
    void deviceDoesNotSubscribeRestoredNodesAfterStop() throws Exception {
        Node restored = childNode("restored", CompletableFuture.completedFuture(null));
        Node added = childNode("new", pending);
        device.nodes.put("restored", restored);
        doReturn(added).when(device).createNode("new");
        CompletableFuture<@Nullable Void> subscription = device.subscribe(connection, scheduler, 500);
        assertFalse(subscription.isDone());
        device.stop().get(5, TimeUnit.SECONDS);
        clearInvocations(restored, callback);
        pending.complete(null);
        subscription.handle((v, e) -> null).get(5, TimeUnit.SECONDS);
        verify(restored, never()).subscribe(any(), any(), anyInt());
        verifyNoInteractions(callback);
        assertFalse(device.isInitialized());
    }

    @Test
    void nodeDoesNotSubscribeRestoredPropertiesAfterStop() throws Exception {
        Property restored = childProperty("restored", CompletableFuture.completedFuture(null));
        Property added = childProperty("new", pending);
        node.properties.put("restored", restored);
        doReturn(added).when(node).createProperty("new");
        CompletableFuture<@Nullable Void> subscription = node.subscribe(connection, scheduler, 500);
        assertFalse(subscription.isDone());
        node.stop().get(5, TimeUnit.SECONDS);
        clearInvocations(restored, callback);
        pending.complete(null);
        subscription.handle((v, e) -> null).get(5, TimeUnit.SECONDS);
        verify(restored, never()).subscribe(any(), any(), anyInt());
        verifyNoInteractions(callback);
    }

    @Test
    void deviceProcessesNodesAndReadyStateWhileChildMetadataIsPending() throws Exception {
        deviceAttributes.nodes = new String[] { "new" };
        Node added = childNode("new", pending);
        Node later = childNode("later", CompletableFuture.completedFuture(null));
        doReturn(added).when(device).createNode("new");
        doReturn(later).when(device).createNode("later");
        CompletableFuture<@Nullable Void> subscription = device.subscribe(connection, scheduler, 500);
        assertFalse(subscription.isDone());
        deviceAttributes.nodes = new String[] { "new", "later" };
        device.attributeChanged("nodes", new String[] { "new", "later" }, connection, scheduler, true);
        deviceAttributes.state = DeviceAttributes.ReadyState.ready;
        device.attributeChanged("state", DeviceAttributes.ReadyState.ready, connection, scheduler, true);
        pending.complete(null);
        subscription.get(5, TimeUnit.SECONDS);
        verify(later).subscribe(connection, scheduler, 500);
        verify(added, times(1)).subscribe(connection, scheduler, 500);
        verify(callback).readyStateChanged(DeviceAttributes.ReadyState.ready);
        device.stop().get(5, TimeUnit.SECONDS);
    }

    @Test
    void nodeProcessesPropertiesWhileChildMetadataIsPending() throws Exception {
        nodeAttributes.properties = new String[] { "new" };
        Property added = childProperty("new", pending);
        Property later = childProperty("later", CompletableFuture.completedFuture(null));
        doReturn(added).when(node).createProperty("new");
        doReturn(later).when(node).createProperty("later");
        CompletableFuture<@Nullable Void> subscription = node.subscribe(connection, scheduler, 500);
        assertFalse(subscription.isDone());
        nodeAttributes.properties = new String[] { "new", "later" };
        node.attributeChanged("properties", new String[] { "new", "later" }, connection, scheduler, true);
        pending.complete(null);
        subscription.get(5, TimeUnit.SECONDS);
        verify(later).subscribe(connection, scheduler, 500);
        verify(added, times(1)).subscribe(connection, scheduler, 500);
        node.stop().get(5, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void stopWaitsForSubscriptionSetup(boolean deviceLevel) throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean listening = new AtomicBoolean();
        Node restoredNode = childNode("restored", CompletableFuture.completedFuture(null));
        Property restoredProperty = childProperty("restored", CompletableFuture.completedFuture(null));
        Object restored = deviceLevel ? restoredNode : restoredProperty;
        Answer<CompletableFuture<@Nullable Void>> start = invocation -> {
            entered.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS), "Subscription setup was not released");
            listening.set(true);
            return CompletableFuture.completedFuture(null);
        };
        Answer<CompletableFuture<@Nullable Void>> stop = invocation -> {
            listening.set(false);
            return CompletableFuture.completedFuture(null);
        };
        CompletableFuture<@Nullable Void> subscription;
        if (deviceLevel) {
            device.nodes.put("restored", restoredNode);
            doReturn(childNode("new", pending)).when(device).createNode("new");
            doAnswer(start).when(restoredNode).subscribe(any(), any(), anyInt());
            doAnswer(stop).when(restoredNode).stop();
            subscription = device.subscribe(connection, scheduler, 500);
        } else {
            node.properties.put("restored", restoredProperty);
            doReturn(childProperty("new", pending)).when(node).createProperty("new");
            doAnswer(start).when(restoredProperty).subscribe(any(), any(), anyInt());
            doAnswer(stop).when(restoredProperty).stop();
            subscription = node.subscribe(connection, scheduler, 500);
        }
        clearInvocations(restored);
        CompletableFuture<Void> receiveMetadata = CompletableFuture.runAsync(() -> pending.complete(null));
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS), "Restored subscription setup did not start");
            CompletableFuture<@Nullable Void> stopped = deviceLevel ? device.stop() : node.stop();
            assertFalse(stopped.isDone(), "Shutdown must wait for subscription setup before removing listeners");
            release.countDown();
            stopped.get(5, TimeUnit.SECONDS);
            receiveMetadata.get(5, TimeUnit.SECONDS);
            subscription.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
            assertFalse(listening.get(), "Shutdown left a listener active");
        } finally {
            release.countDown();
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void stopInvalidatesPendingParentMetadata(boolean deviceLevel) throws Exception {
        AbstractMqttAttributeClass attributes = deviceLevel ? deviceAttributes : nodeAttributes;
        doReturn(pending).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        CompletableFuture<@Nullable Void> subscription = deviceLevel ? device.subscribe(connection, scheduler, 500)
                : node.subscribe(connection, scheduler, 500);
        (deviceLevel ? device.stop() : node.stop()).get(5, TimeUnit.SECONDS);
        clearInvocations(callback);
        pending.complete(null);
        subscription.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
        verifyNoInteractions(callback);
        assertTrue(subscription.isCompletedExceptionally());
        assertFalse(device.isInitialized());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void restartWaitsForCleanupAndIgnoresOldMetadata(boolean deviceLevel) throws Exception {
        deviceAttributes.nodes = new String[0];
        nodeAttributes.properties = new String[0];
        AbstractMqttAttributeClass attributes = deviceLevel ? deviceAttributes : nodeAttributes;
        doReturn(pending).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        CompletableFuture<@Nullable Void> oldSubscription = deviceLevel ? device.subscribe(connection, scheduler, 500)
                : node.subscribe(connection, scheduler, 500);
        CompletableFuture<@Nullable Void> cleanup = new CompletableFuture<>();
        doReturn(cleanup).when(attributes).unsubscribe();
        CompletableFuture<@Nullable Void> stopped = deviceLevel ? device.stop() : node.stop();
        doReturn(CompletableFuture.completedFuture(null)).when(attributes).subscribeAndReceive(any(), any(),
                anyString(), any(), anyInt());
        CompletableFuture<@Nullable Void> restarted = deviceLevel ? device.subscribe(connection, scheduler, 500)
                : node.subscribe(connection, scheduler, 500);
        assertFalse(restarted.isDone());
        verify(attributes, times(1)).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        pending.complete(null);
        oldSubscription.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
        verifyNoInteractions(callback);
        cleanup.complete(null);
        stopped.get(5, TimeUnit.SECONDS);
        restarted.get(5, TimeUnit.SECONDS);
        verify(attributes, times(2)).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        if (deviceLevel) {
            assertTrue(device.isInitialized());
            verify(callback).readyStateChanged(DeviceAttributes.ReadyState.init);
        } else {
            verify(callback).nodeAddedOrChanged(node);
        }
        (deviceLevel ? device.stop() : node.stop()).get(5, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void lateMandatoryMetadataRecoversAfterTimeout(boolean deviceLevel) throws Exception {
        AbstractMqttAttributeClass attributes = deviceLevel ? deviceAttributes : nodeAttributes;
        AtomicReference<AbstractMqttAttributeClass.AttributeChanged> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(3));
            return pending;
        }).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        CompletableFuture<@Nullable Void> subscription = deviceLevel ? device.subscribe(connection, scheduler, 500)
                : node.subscribe(connection, scheduler, 500);
        TimeoutException timeout = new TimeoutException("Mandatory metadata did not arrive");
        pending.completeExceptionally(timeout);
        assertSame(timeout,
                assertThrows(ExecutionException.class, () -> subscription.get(5, TimeUnit.SECONDS)).getCause());
        Node addedNode = childNode("new", CompletableFuture.completedFuture(null));
        Property addedProperty = childProperty("new", CompletableFuture.completedFuture(null));
        deviceAttributes.nodes = new String[] { "new" };
        deviceAttributes.state = DeviceAttributes.ReadyState.ready;
        nodeAttributes.properties = new String[] { "new" };
        doReturn(addedNode).when(device).createNode("new");
        doReturn(addedProperty).when(node).createProperty("new");
        // The field callback can be queued behind owner work, so completeness may already be updated.
        doReturn(true).when(attributes).isComplete();
        listener.get().attributeChanged(deviceLevel ? "nodes" : "properties", new String[] { "new" }, connection,
                scheduler, true);
        if (deviceLevel) {
            verify(addedNode).subscribe(connection, scheduler, 500);
            verify(callback).readyStateChanged(DeviceAttributes.ReadyState.ready);
            assertTrue(device.isInitialized());
        } else {
            verify(addedProperty).subscribe(connection, scheduler, 500);
            verify(callback, atLeastOnce()).nodeAddedOrChanged(node);
        }
        (deviceLevel ? device.stop() : node.stop()).get(5, TimeUnit.SECONDS);
        clearInvocations(callback, addedNode, addedProperty);
        listener.get().attributeChanged(deviceLevel ? "nodes" : "properties", new String[] { "new" }, connection,
                scheduler, true);
        verifyNoInteractions(callback, addedNode, addedProperty);
    }

    @Test
    void reinitializationKeepsOutgoingNodesForCleanup() throws Exception {
        Node restored = childNode("restored", CompletableFuture.completedFuture(null));
        device.nodes.put("restored", restored);
        doReturn(childNode("new", pending)).when(device).createNode("new");
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        AtomicBoolean listening = new AtomicBoolean();
        doAnswer(invocation -> {
            entered.countDown();
            assertTrue(release.await(5, TimeUnit.SECONDS), "Subscription setup was not released");
            listening.set(true);
            return CompletableFuture.completedFuture(null);
        }).when(restored).subscribe(any(), any(), anyInt());
        doAnswer(invocation -> {
            listening.set(false);
            return CompletableFuture.completedFuture(null);
        }).when(restored).stop();
        CompletableFuture<@Nullable Void> oldSubscription = device.subscribe(connection, scheduler, 500);
        clearInvocations(restored, callback);
        CompletableFuture<Void> receiveMetadata = CompletableFuture.runAsync(() -> pending.complete(null));
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS), "Restored subscription setup did not start");
            CompletableFuture<@Nullable Void> stopped = device.stop();
            assertFalse(stopped.isDone());
            device.initialize("homie", "replacement", List.of());
            assertEquals(0, device.nodes.size());
            release.countDown();
            stopped.get(5, TimeUnit.SECONDS);
            receiveMetadata.get(5, TimeUnit.SECONDS);
            oldSubscription.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
            assertFalse(listening.get(), "Reinitialization leaked the outgoing node's listeners");
            verify(restored).stop();
            verifyNoInteractions(callback);
            deviceAttributes.nodes = new String[0];
            device.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
            assertTrue(device.isInitialized());
            device.stop().get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
        }
    }

    @Test
    void propertyIgnoresMetadataCompletionAfterStop() throws Exception {
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        doReturn(pending).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Property property = node.createProperty("property", attributes);
        CompletableFuture<@Nullable Void> subscription = property.subscribe(connection, scheduler, 500);
        property.stop().get(5, TimeUnit.SECONDS);
        clearInvocations(callback);
        pending.complete(null);
        subscription.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
        verifyNoInteractions(callback);
        assertNull(property.getChannelState());
        assertTrue(subscription.isCompletedExceptionally());
    }

    private Set<MqttMessageSubscriber> trackValueListeners() {
        Set<MqttMessageSubscriber> listeners = new HashSet<>();
        doAnswer(invocation -> {
            listeners.add(invocation.getArgument(1));
            return CompletableFuture.completedFuture(true);
        }).when(connection).subscribe(anyString(), any());
        doAnswer(invocation -> {
            listeners.remove(invocation.getArgument(1));
            return CompletableFuture.completedFuture(true);
        }).when(connection).unsubscribe(anyString(), any());
        return listeners;
    }

    @Test
    void propertyRestartIgnoresOldMetadataAndRemovesValueListeners() throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        AtomicReference<AbstractMqttAttributeClass.AttributeChanged> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(3));
            return pending;
        }).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Property property = node.createProperty("property", attributes);
        CompletableFuture<@Nullable Void> oldSubscription = property.subscribe(connection, scheduler, 500);
        AbstractMqttAttributeClass.AttributeChanged oldListener = listener.get();
        property.stop().get(5, TimeUnit.SECONDS);
        doReturn(CompletableFuture.completedFuture(null)).when(attributes).subscribeAndReceive(any(), any(),
                anyString(), any(), anyInt());
        property.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        ChannelState active = property.getChannelState();
        assertNotNull(active);
        assertEquals(Set.of(active), listeners);
        clearInvocations(callback);
        pending.complete(null);
        oldSubscription.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
        oldListener.attributeChanged("name", "Old name", connection, scheduler, true);
        assertSame(active, property.getChannelState());
        verifyNoInteractions(callback);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        assertEquals(Set.of(active), listeners);
        property.stop().get(5, TimeUnit.SECONDS);
        assertTrue(listeners.isEmpty(), "Property shutdown left a value listener registered");
    }

    @Test
    void propertyMetadataUpdateStopsPreviousValueListener() throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        AtomicReference<AbstractMqttAttributeClass.AttributeChanged> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(3));
            return CompletableFuture.completedFuture(null);
        }).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Property property = node.createProperty("property", attributes);
        property.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        ChannelState previous = property.getChannelState();
        assertNotNull(previous);
        attributes.name = "Updated name";
        listener.get().attributeChanged("name", "Updated name", connection, scheduler, true);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        ChannelState updated = property.getChannelState();
        assertNotNull(updated);
        assertNotSame(previous, updated);
        assertEquals(Set.of(updated), listeners);
        property.stop().get(5, TimeUnit.SECONDS);
        assertTrue(listeners.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void childMetadataTimeoutDoesNotPreventStartingCachedChannels(boolean restored) throws Exception {
        trackValueListeners();
        deviceAttributes.nodes = new String[] { "incomplete" };
        NodeAttributes attributes = spy(new NodeAttributes());
        stubAttributes(attributes);
        doReturn(CompletableFuture.failedFuture(new TimeoutException("Missing mandatory node metadata")))
                .when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Node incomplete = device.createNode("incomplete", attributes);
        Property cached = incomplete.createProperty("cached", new PropertyAttributes());
        cached.attributesReceived();
        incomplete.properties.put("cached", cached);
        if (restored) {
            device.nodes.put("incomplete", incomplete);
        } else {
            doReturn(incomplete).when(device).createNode("incomplete");
        }
        device.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        device.startChannels(connection, scheduler, 0, mock(HomieThingHandler.class)).get(5, TimeUnit.SECONDS);
        verify(connection).subscribe(eq("homie/device/incomplete/cached"), same(cached.getChannelState()));
        assertTrue(device.isInitialized());
        device.stop().get(5, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void propertySerializesMetadataChangesAndRestartWithUnsubscription(boolean restart) throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        AtomicReference<AbstractMqttAttributeClass.AttributeChanged> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(3));
            return CompletableFuture.completedFuture(null);
        }).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Property property = node.createProperty("property", attributes);
        property.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        ChannelState previous = property.getChannelState();
        assertNotNull(previous);
        CompletableFuture<Boolean> unsubscribed = new CompletableFuture<>();
        AtomicBoolean firstUnsubscribe = new AtomicBoolean(true);
        doAnswer(invocation -> {
            listeners.remove(previous);
            return firstUnsubscribe.getAndSet(false) ? unsubscribed : CompletableFuture.completedFuture(true);
        }).when(connection).unsubscribe(anyString(), same(previous));
        attributes.name = "First update";
        listener.get().attributeChanged("name", "First update", connection, scheduler, true);
        attributes.name = "Second update";
        listener.get().attributeChanged("name", "Second update", connection, scheduler, true);
        CompletableFuture<@Nullable Void> start = property.startChannel(connection, scheduler, 0);
        assertFalse(start.isDone(), "Value subscription must wait for metadata replacement");
        CompletableFuture<@Nullable Void> restarted = CompletableFuture.completedFuture(null);
        if (restart) {
            property.stop();
            restarted = property.subscribe(connection, scheduler, 500);
            assertFalse(restarted.isDone(), "Restart must wait for outgoing value unsubscription");
        }
        unsubscribed.complete(true);
        if (restart) {
            restarted.get(5, TimeUnit.SECONDS);
            start.handle((v, error) -> null).get(5, TimeUnit.SECONDS);
            assertTrue(start.isCompletedExceptionally(), "A stopped subscription must not be restarted by queued work");
            property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        } else {
            start.get(5, TimeUnit.SECONDS);
        }
        ChannelState current = property.getChannelState();
        assertNotNull(current);
        assertEquals(Set.of(current), listeners);
        assertEquals("Second update", property.getChannelDefinition().getLabel());
        property.stop().get(5, TimeUnit.SECONDS);
        assertTrue(listeners.isEmpty());
    }

    @Test
    void restoredPropertyMetadataFailureDoesNotPreventStartingCachedChannel() throws Exception {
        trackValueListeners();
        deviceAttributes.nodes = new String[] { "restored" };
        NodeAttributes attributes = spy(new NodeAttributes());
        stubAttributes(attributes);
        attributes.properties = new String[] { "cached" };
        Node restored = device.createNode("restored", attributes);
        PropertyAttributes propertyAttributes = spy(new PropertyAttributes());
        stubAttributes(propertyAttributes);
        doReturn(CompletableFuture.failedFuture(new TimeoutException("Metadata subscription failed")))
                .when(propertyAttributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Property cached = restored.createProperty("cached", propertyAttributes);
        cached.attributesReceived();
        restored.properties.put("cached", cached);
        device.nodes.put("restored", restored);
        device.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        device.startChannels(connection, scheduler, 0, mock(HomieThingHandler.class)).get(5, TimeUnit.SECONDS);
        verify(connection).subscribe(eq("homie/device/restored/cached"), same(cached.getChannelState()));
        device.stop().get(5, TimeUnit.SECONDS);
    }

    @Test
    void propertyShutdownDrainsPendingStopBeforeRestartingCachedChannel() throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        AtomicReference<AbstractMqttAttributeClass.AttributeChanged> listener = new AtomicReference<>();
        doAnswer(invocation -> {
            listener.set(invocation.getArgument(3));
            return CompletableFuture.completedFuture(null);
        }).when(attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        Property property = node.createProperty("property", attributes);
        property.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        ChannelState cached = property.getChannelState();
        assertNotNull(cached);
        CompletableFuture<Boolean> firstStop = new CompletableFuture<>();
        AtomicBoolean firstUnsubscribe = new AtomicBoolean(true);
        // The connection removes the consumer before the wire unsubscribe completes;
        // a repeated call returns immediately.
        doAnswer(invocation -> {
            listeners.remove(cached);
            return firstUnsubscribe.getAndSet(false) ? firstStop : CompletableFuture.completedFuture(true);
        }).when(connection).unsubscribe(anyString(), same(cached));
        attributes.name = "Updated name";
        listener.get().attributeChanged("name", "Updated name", connection, scheduler, true);
        CompletableFuture<@Nullable Void> stopped = property.stop();
        TimeoutException metadataFailure = new TimeoutException("Metadata refresh failed");
        doReturn(CompletableFuture.failedFuture(metadataFailure)).when(attributes).subscribeAndReceive(any(), any(),
                anyString(), any(), anyInt());
        CompletableFuture<@Nullable Void> restarted = property.subscribe(connection, scheduler, 500);
        assertFalse(stopped.isDone(), "Shutdown must wait for the pending ChannelState.stop()");
        assertFalse(restarted.isDone(), "Restart must not reuse the cached channel before its old stop finishes");
        verify(connection, times(1)).unsubscribe(anyString(), same(cached));
        firstStop.complete(true);
        stopped.get(5, TimeUnit.SECONDS);
        assertSame(metadataFailure,
                assertThrows(ExecutionException.class, () -> restarted.get(5, TimeUnit.SECONDS)).getCause());
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        assertSame(cached, property.getChannelState());
        assertTrue(cached.hasSubscribed());
        assertSame(callback, cached.getChannelStateUpdateListener());
        assertEquals(Set.of(cached), listeners);
        property.stop().get(5, TimeUnit.SECONDS);
        assertTrue(listeners.isEmpty());
        assertNull(cached.getChannelStateUpdateListener());
    }

    @ParameterizedTest
    @ValueSource(strings = { "device", "node", "property" })
    void staleFieldSubscribersCannotMutateNewMetadata(String kind) throws Exception {
        AbstractMqttAttributeClass attributes = switch (kind) {
            case "device" -> new DeviceAttributes();
            case "node" -> new NodeAttributes();
            default -> new PropertyAttributes();
        };
        List<MqttMessageSubscriber> names = new ArrayList<>();
        doAnswer(invocation -> {
            if ("homie/device/$name".equals(invocation.getArgument(0))) {
                names.add(invocation.getArgument(1));
            }
            return CompletableFuture.completedFuture(true);
        }).when(connection).subscribe(anyString(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(anyString(), any());
        AbstractMqttAttributeClass.AttributeChanged oldListener = mock(
                AbstractMqttAttributeClass.AttributeChanged.class);
        AbstractMqttAttributeClass.AttributeChanged newListener = mock(
                AbstractMqttAttributeClass.AttributeChanged.class);
        attributes.subscribeAndReceive(connection, scheduler, "homie/device", oldListener, 500);
        attributes.unsubscribe().get(5, TimeUnit.SECONDS);
        attributes.subscribeAndReceive(connection, scheduler, "homie/device", newListener, 500);
        names.get(1).processMessage("homie/device/$name", "Current".getBytes(StandardCharsets.UTF_8));
        clearInvocations(oldListener, newListener);
        names.get(0).processMessage("homie/device/$name", "Obsolete".getBytes(StandardCharsets.UTF_8));
        assertEquals("Current", attributes.getClass().getField("name").get(attributes));
        verifyNoInteractions(oldListener, newListener);
        attributes.unsubscribe().get(5, TimeUnit.SECONDS);
    }

    @ParameterizedTest
    @ValueSource(strings = { "device", "node", "property" })
    void cleanupFailureDoesNotPreventMetadataResubscription(String kind) throws Exception {
        deviceAttributes.nodes = new String[0];
        nodeAttributes.properties = new String[0];
        PropertyAttributes propertyAttributes = spy(new PropertyAttributes());
        stubAttributes(propertyAttributes);
        Property property = node.createProperty("property", propertyAttributes);
        AbstractMqttAttributeClass attributes = switch (kind) {
            case "device" -> deviceAttributes;
            case "node" -> nodeAttributes;
            default -> propertyAttributes;
        };
        Supplier<CompletableFuture<@Nullable Void>> subscribe = switch (kind) {
            case "device" -> () -> device.subscribe(connection, scheduler, 500);
            case "node" -> () -> node.subscribe(connection, scheduler, 500);
            default -> () -> property.subscribe(connection, scheduler, 500);
        };
        Supplier<CompletableFuture<@Nullable Void>> stop = switch (kind) {
            case "device" -> device::stop;
            case "node" -> node::stop;
            default -> property::stop;
        };
        subscribe.get().get(5, TimeUnit.SECONDS);
        doReturn(CompletableFuture.failedFuture(new TimeoutException("Old session unsubscribe failed")))
                .when(attributes).unsubscribe();
        assertThrows(ExecutionException.class, () -> stop.get().get(5, TimeUnit.SECONDS));
        doReturn(CompletableFuture.completedFuture(null)).when(attributes).unsubscribe();
        subscribe.get().get(5, TimeUnit.SECONDS);
        verify(attributes, times(2)).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        stop.get().get(5, TimeUnit.SECONDS);
    }

    private Property addCachedDeviceProperty() {
        deviceAttributes.nodes = new String[] { "node" };
        nodeAttributes.properties = new String[] { "property" };
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        Property property = node.createProperty("property", attributes);
        property.attributesReceived();
        node.properties.put("property", property);
        device.nodes.put("node", node);
        return property;
    }

    @Test
    void shutdownStopsPropertiesReopenedAfterFailedDeviceMetadata() throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        addCachedDeviceProperty();
        device.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        device.startChannels(connection, scheduler, 0, mock(HomieThingHandler.class)).get(5, TimeUnit.SECONDS);
        device.stop().get(5, TimeUnit.SECONDS);
        assertTrue(listeners.isEmpty());
        doReturn(CompletableFuture.failedFuture(new TimeoutException("Device metadata unavailable")))
                .when(deviceAttributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        assertThrows(ExecutionException.class,
                () -> device.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS));
        device.startChannels(connection, scheduler, 0, mock(HomieThingHandler.class)).get(5, TimeUnit.SECONDS);
        assertEquals(1, listeners.size());
        device.stop().get(5, TimeUnit.SECONDS);
        assertTrue(listeners.isEmpty(), "A cached node stop must also stop reopened property contexts");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void deviceStartupDistinguishesChildSupersessionFromDeviceShutdown(boolean shutdown) throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        Property property = addCachedDeviceProperty();
        device.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        CompletableFuture<Boolean> acknowledgement = new CompletableFuture<>();
        doAnswer(invocation -> {
            listeners.add(invocation.getArgument(1));
            return acknowledgement;
        }).when(connection).subscribe(anyString(), any());
        CompletableFuture<@Nullable Void> values = device.startChannels(connection, scheduler, 0,
                mock(HomieThingHandler.class));
        CompletableFuture<@Nullable Void> transition = shutdown ? device.stop()
                : property.subscribe(connection, scheduler, 500);
        acknowledgement.complete(true);
        transition.get(5, TimeUnit.SECONDS);
        if (shutdown) {
            assertThrows(ExecutionException.class, () -> values.get(5, TimeUnit.SECONDS));
            assertTrue(listeners.isEmpty());
        } else {
            values.get(5, TimeUnit.SECONDS);
            device.startChannels(connection, scheduler, 0, mock(HomieThingHandler.class)).get(5, TimeUnit.SECONDS);
            assertEquals(1, listeners.size());
            device.stop().get(5, TimeUnit.SECONDS);
        }
    }

    @Test
    void replacingFieldSubscriptionsWaitsForInFlightMutationAndListener() throws Exception {
        DeviceAttributes attributes = new DeviceAttributes();
        List<MqttMessageSubscriber> names = new ArrayList<>();
        doAnswer(invocation -> {
            if ("homie/device/$name".equals(invocation.getArgument(0))) {
                names.add(invocation.getArgument(1));
            }
            return CompletableFuture.completedFuture(true);
        }).when(connection).subscribe(anyString(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(anyString(), any());
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        attributes.subscribeAndReceive(connection, scheduler, "homie/device",
                (name, value, broker, executor, complete) -> {
                    entered.countDown();
                    try {
                        assertTrue(release.await(5, TimeUnit.SECONDS), "Old metadata listener was not released");
                    } catch (InterruptedException error) {
                        Thread.currentThread().interrupt();
                        throw new AssertionError(error);
                    }
                }, 500);
        CompletableFuture<Void> oldMessage = CompletableFuture.runAsync(
                () -> names.getFirst().processMessage("homie/device/$name", "Old".getBytes(StandardCharsets.UTF_8)));
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            attributes.subscribeAndReceive(connection, scheduler, "homie/device", null, 500);
            assertEquals(1, names.size(), "New metadata listeners must wait for the outgoing mutation to finish");
            release.countDown();
            oldMessage.get(5, TimeUnit.SECONDS);
            assertEquals(2, names.size());
            names.get(1).processMessage("homie/device/$name", "Current".getBytes(StandardCharsets.UTF_8));
            names.getFirst().processMessage("homie/device/$name", "Obsolete".getBytes(StandardCharsets.UTF_8));
            assertEquals("Current", attributes.name);
            attributes.unsubscribe().get(5, TimeUnit.SECONDS);
        } finally {
            release.countDown();
        }
    }

    @Test
    void failedValueStopDoesNotLeaveCachedChannelMarkedSubscribed() throws Exception {
        Set<MqttMessageSubscriber> listeners = trackValueListeners();
        PropertyAttributes attributes = spy(new PropertyAttributes());
        stubAttributes(attributes);
        Property property = node.createProperty("property", attributes);
        property.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS);
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        ChannelState cached = property.getChannelState();
        assertNotNull(cached);
        AtomicBoolean first = new AtomicBoolean(true);
        doAnswer(invocation -> {
            listeners.remove(cached);
            return first.getAndSet(false) ? CompletableFuture.failedFuture(new TimeoutException("Unsubscribe failed"))
                    : CompletableFuture.completedFuture(true);
        }).when(connection).unsubscribe(anyString(), same(cached));
        assertThrows(ExecutionException.class, () -> property.stop().get(5, TimeUnit.SECONDS));
        doReturn(CompletableFuture.failedFuture(new TimeoutException("Metadata unavailable"))).when(attributes)
                .subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        assertThrows(ExecutionException.class,
                () -> property.subscribe(connection, scheduler, 500).get(5, TimeUnit.SECONDS));
        property.startChannel(connection, scheduler, 0).get(5, TimeUnit.SECONDS);
        assertEquals(Set.of(cached), listeners, "Cached value listener must be registered again after a failed stop");
        property.stop().get(5, TimeUnit.SECONDS);
    }
}
