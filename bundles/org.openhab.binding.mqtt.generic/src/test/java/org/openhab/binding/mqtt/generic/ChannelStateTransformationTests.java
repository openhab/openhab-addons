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
package org.openhab.binding.mqtt.generic;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mqtt.generic.internal.handler.ThingChannelConstants.*;

import java.util.concurrent.CompletableFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.internal.handler.GenericMQTTThingHandler;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.transform.TransformationService;

/**
 * Tests cases for {@link ThingHandler} to test the json transformation.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class ChannelStateTransformationTests {

    private @Mock @NonNullByDefault({}) TransformationService jsonPathServiceMock;
    private @Mock @NonNullByDefault({}) TransformationServiceProvider transformationServiceProviderMock;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @Mock @NonNullByDefault({}) Thing thingMock;
    private @Mock @NonNullByDefault({}) AbstractBrokerHandler bridgeHandlerMock;
    private @Mock @NonNullByDefault({}) MqttBrokerConnection connectionMock;

    private @NonNullByDefault({}) GenericMQTTThingHandler thingHandler;

    @BeforeEach
    public void setUp() throws Exception {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Mock the thing: We need the thingUID and the bridgeUID
        when(thingMock.getUID()).thenReturn(TEST_GENERIC_THING);
        when(thingMock.getChannels()).thenReturn(THING_CHANNEL_LIST_WITH_JSON);
        when(thingMock.getStatusInfo()).thenReturn(thingStatus);
        when(thingMock.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandlerMock.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connectionMock));

        CompletableFuture<@Nullable Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connectionMock).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).unsubscribe(any(), any());

        thingHandler = spy(new GenericMQTTThingHandler(thingMock, mock(MqttChannelStateDescriptionProvider.class),
                transformationServiceProviderMock, 1500));
        when(transformationServiceProviderMock.getTransformationService(anyString())).thenReturn(jsonPathServiceMock);

        thingHandler.setCallback(callbackMock);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandlerMock).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @SuppressWarnings("null")
    @Test
    public void initialize() throws Exception {
        when(thingMock.getChannels()).thenReturn(THING_CHANNEL_LIST_WITH_JSON);

        thingHandler.initialize();
        ChannelState channelConfig = thingHandler.getChannelState(TEXT_CHANNEL_UID);
        assertThat(channelConfig.transformationsIn.get(0).pattern, is(JSON_PATH_PATTERN));
    }

    @SuppressWarnings("null")
    @Test
    public void processMessageWithJSONPath() throws Exception {
        when(jsonPathServiceMock.transform(JSON_PATH_PATTERN, JSON_PATH_JSON)).thenReturn("23.2");

        thingHandler.initialize();
        ChannelState channelConfig = thingHandler.getChannelState(TEXT_CHANNEL_UID);
        channelConfig.setChannelStateUpdateListener(thingHandler);

        ChannelStateTransformation transformation = channelConfig.transformationsIn.get(0);

        byte payload[] = JSON_PATH_JSON.getBytes();
        assertThat(transformation.pattern, is(JSON_PATH_PATTERN));
        // Test process message
        channelConfig.processMessage(channelConfig.getStateTopic(), payload);

        verify(callbackMock).stateUpdated(eq(TEXT_CHANNEL_UID), argThat(arg -> "23.2".equals(arg.toString())));
        assertThat(channelConfig.getCache().getChannelState().toString(), is("23.2"));
    }
}
