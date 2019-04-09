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
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.openhab.binding.mqtt.generic.internal.handler.ThingChannelConstants.*;

import java.util.concurrent.CompletableFuture;

import javax.naming.ConfigurationException;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.transform.TransformationService;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.eclipse.smarthome.io.transport.mqtt.MqttException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.ChannelStateTransformation;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.handler.GenericMQTTThingHandler;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;

/**
 * Tests cases for {@link ThingHandler} to test the json transformation.
 *
 * @author David Graeff - Initial contribution
 */
public class ChannelStateTransformationTests {

    @Mock
    private TransformationService jsonPathService;

    @Mock
    private TransformationServiceProvider transformationServiceProvider;

    @Mock
    private ThingHandlerCallback callback;

    @Mock
    private Thing thing;

    @Mock
    private AbstractBrokerHandler bridgeHandler;

    @Mock
    private MqttBrokerConnection connection;

    private GenericMQTTThingHandler thingHandler;

    @Before
    public void setUp() throws ConfigurationException, MqttException {
        initMocks(this);

        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testGenericThing);
        when(thing.getChannels()).thenReturn(thingChannelListWithJson);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<Void>();
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
