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
package org.openhab.binding.mqtt.homie.generic.internal.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.mqtt.homie.internal.handler.ThingChannelConstants;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceCallback;
import org.openhab.binding.mqtt.homie.internal.homie300.Node;
import org.openhab.binding.mqtt.homie.internal.homie300.NodeAttributes;

/**
 * Tests cases for {@link HomieChildMap}.
 *
 * @author David Graeff - Initial contribution
 */
public class HomieChildMapTests {
    private @Mock DeviceCallback callback;

    private final String deviceID = ThingChannelConstants.testHomieThing.getId();
    private final String deviceTopic = "homie/" + deviceID;

    // A completed future is returned for a subscribe call to the attributes
    final CompletableFuture<@Nullable Void> future = CompletableFuture.completedFuture(null);

    ChildMap<Node> subject = new ChildMap<>();

    private Node createNode(String id) {
        Node node = new Node(deviceTopic, id, ThingChannelConstants.testHomieThing, callback,
                spy(new NodeAttributes()));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        return node;
    }

    private void removedNode(Node node) {
        callback.nodeRemoved(node);
    }

    @Before
    public void setUp() {
        initMocks(this);
    }

    public static class AddedAction implements Function<Node, CompletableFuture<Void>> {
        @Override
        public CompletableFuture<Void> apply(Node t) {
            return CompletableFuture.completedFuture(null);
        }
    };

    @Test
    public void testArrayToSubtopicCreateAndRemove() {
        AddedAction addedAction = spy(new AddedAction());

        // Assign "abc,def" to the
        subject.apply(new String[] { "abc", "def" }, addedAction, this::createNode, this::removedNode);

        assertThat(future.isDone(), is(true));
        assertThat(subject.get("abc").nodeID, is("abc"));
        assertThat(subject.get("def").nodeID, is("def"));

        verify(addedAction, times(2)).apply(any());

        Node soonToBeRemoved = subject.get("def");
        subject.apply(new String[] { "abc" }, addedAction, this::createNode, this::removedNode);
        verify(callback).nodeRemoved(eq(soonToBeRemoved));
    }

}
