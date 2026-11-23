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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.stubbing.Answer;
import org.openhab.binding.homie.internal.handler.ThingChannelConstants;
import org.openhab.binding.mqtt.generic.mapping.AbstractMqttAttributeClass;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;

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
}
