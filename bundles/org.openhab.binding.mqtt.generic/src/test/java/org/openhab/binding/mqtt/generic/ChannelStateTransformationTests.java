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

import javax.naming.ConfigurationException;

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
import org.openhab.core.io.transport.mqtt.MqttException;
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
@MockitoSettings(strictness = Strictness.WARN)
public class ChannelStateTransformationTests {

    private @Mock TransformationService jsonPathService;
    private @Mock TransformationServiceProvider transformationServiceProvider;
    private @Mock ThingHandlerCallback callback;
    private @Mock Thing thing;
    private @Mock AbstractBrokerHandler bridgeHandler;
    private @Mock MqttBrokerConnection connection;

    private GenericMQTTThingHandler thingHandler;

    @BeforeEach
    public void setUp() throws ConfigurationException, MqttException {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testGenericThing);
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());

        thingHandler = spy(new GenericMQTTThingHandler(thing, mock(MqttChannelStateDescriptionProvider.class),
                transformationServiceProvider, 1500));
        when(transformationServiceProvider.getTransformationService(anyString())).thenReturn(jsonPathService);

        thingHandler.setCallback(callback);
        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // We are by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @SuppressWarnings("null")
    @Test
    public void initialize() throws MqttException {
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);

        thingHandler.initialize();
        ChannelState channelConfig = thingHandler.getChannelState(textChannelUID);
        assertThat(channelConfig.transformationsIn.get(0).pattern, is(jsonPathPattern));
    }

    @SuppressWarnings("null")
    @Test
    public void processMessageWithJSONPath() throws Exception {
        when(jsonPathService.transform(jsonPathPattern, jsonPathJSON)).thenReturn("23.2");

        thingHandler.initialize();
        ChannelState channelConfig = thingHandler.getChannelState(textChannelUID);
        channelConfig.setChannelStateUpdateListener(thingHandler);

        ChannelStateTransformation transformation = channelConfig.transformationsIn.get(0);

        byte payload[] = jsonPathJSON.getBytes();
        assertThat(transformation.pattern, is(jsonPathPattern));
        // Test process message
        channelConfig.processMessage(channelConfig.getStateTopic(), payload);

        verify(callback).stateUpdated(eq(textChannelUID), argThat(arg -> "23.2".equals(arg.toString())));
        assertThat(channelConfig.getCache().getChannelState().toString(), is("23.2"));
    }
}
