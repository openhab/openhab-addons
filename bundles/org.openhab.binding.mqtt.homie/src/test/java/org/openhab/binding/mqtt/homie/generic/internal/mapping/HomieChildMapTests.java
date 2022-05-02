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
package org.openhab.binding.mqtt.homie.generic.internal.mapping;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.tools.ChildMap;
import org.openhab.binding.mqtt.homie.internal.handler.ThingChannelConstants;
import org.openhab.binding.mqtt.homie.internal.homie300.DeviceCallback;
import org.openhab.binding.mqtt.homie.internal.homie300.Node;
import org.openhab.binding.mqtt.homie.internal.homie300.NodeAttributes;

/**
 * Tests cases for {@link HomieChildMap}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class HomieChildMapTests {
    private @Mock @NonNullByDefault({}) DeviceCallback callbackMock;

    private final String deviceID = ThingChannelConstants.TEST_HOMIE_THING.getId();
    private final String deviceTopic = "homie/" + deviceID;

    // A completed future is returned for a subscribe call to the attributes
    final CompletableFuture<@Nullable Void> future = CompletableFuture.completedFuture(null);

    ChildMap<Node> subject = new ChildMap<>();

    private Node createNode(String id) {
        Node node = new Node(deviceTopic, id, ThingChannelConstants.TEST_HOMIE_THING, callbackMock,
                spy(new NodeAttributes()));
        doReturn(future).when(node.attributes).subscribeAndReceive(any(), any(), anyString(), any(), anyInt());
        doReturn(future).when(node.attributes).unsubscribe();
        return node;
    }

    private void removedNode(Node node) {
        callbackMock.nodeRemoved(node);
    }

    public static class AddedAction implements Function<Node, CompletableFuture<Void>> {
        @Override
        public CompletableFuture<Void> apply(Node t) {
            return CompletableFuture.completedFuture(null);
        }
    }

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
        verify(callbackMock).nodeRemoved(eq(soonToBeRemoved));
    }
}
