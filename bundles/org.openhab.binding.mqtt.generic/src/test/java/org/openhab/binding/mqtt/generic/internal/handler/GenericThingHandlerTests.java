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
package org.openhab.binding.mqtt.generic.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mqtt.generic.internal.handler.ThingChannelConstants.*;

import java.util.concurrent.CompletableFuture;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.types.RefreshType;
import org.eclipse.smarthome.io.transport.mqtt.MqttBrokerConnection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.ThingHandlerHelper;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.internal.handler.GenericMQTTThingHandler;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.ValueFactory;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;

/**
 * Tests cases for {@link GenericMQTTThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
public class GenericThingHandlerTests {
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
    public void setUp() {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        MockitoAnnotations.initMocks(this);
        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testGenericThing);
        when(thing.getChannels()).thenReturn(thingChannelList);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<Void>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).publish(any(), any(), anyInt(),
                anyBoolean());

        thingHandler = spy(new GenericMQTTThingHandler(thing, mock(MqttChannelStateDescriptionProvider.class),
                mock(TransformationServiceProvider.class), 1500));
        thingHandler.setCallback(callback);

        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandler).when(thingHandler).getBridgeHandler();

        // The broker connection bridge is by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test(expected = IllegalArgumentException.class)
    public void initializeWithUnknownThingUID() {
        ChannelConfig config = textConfiguration().as(ChannelConfig.class);
        thingHandler.createChannelState(config, new ChannelUID(testGenericThing, "test"),
                ValueFactory.createValueState(config, unknownChannel.getId()));
    }

    @Test
    public void initialize() {
        thingHandler.initialize();
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler).start(any());
        assertThat(thingHandler.getConnection(), is(connection));

        ChannelState channelConfig = thingHandler.channelStateByChannelUID.get(textChannelUID);
        assertThat(channelConfig.getStateTopic(), is("test/state"));
        assertThat(channelConfig.getCommandTopic(), is("test/command"));

        verify(connection).subscribe(eq(channelConfig.getStateTopic()), eq(channelConfig));

        verify(callback).statusUpdated(eq(thing), argThat((arg) -> arg.getStatus().equals(ThingStatus.ONLINE)
                && arg.getStatusDetail().equals(ThingStatusDetail.NONE)));
    }

    @Test
    public void handleCommandRefresh() {
        TextValue value = spy(new TextValue());
        value.update(new StringType("DEMOVALUE"));

        ChannelState channelConfig = mock(ChannelState.class);
        doReturn(CompletableFuture.completedFuture(true)).when(channelConfig).start(any(), any(), anyInt());
        doReturn(CompletableFuture.completedFuture(true)).when(channelConfig).stop();
        doReturn(value).when(channelConfig).getCache();
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();

        ThingHandlerHelper.setConnection(thingHandler, connection);

        thingHandler.handleCommand(textChannelUID, RefreshType.REFRESH);
        verify(callback).stateUpdated(eq(textChannelUID), argThat(arg -> "DEMOVALUE".equals(arg.toString())));
    }

    @Test
    public void handleCommandUpdateString() {
        TextValue value = spy(new TextValue());
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("stateTopic", "commandTopic").build(), textChannelUID,
                        value, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        ThingHandlerHelper.setConnection(thingHandler, connection);

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(textChannelUID, updateValue);
        verify(value).update(eq(updateValue));
        assertThat(channelConfig.getCache().getChannelState().toString(), is("UPDATE"));
    }

    @Test
    public void handleCommandUpdateBoolean() {
        OnOffValue value = spy(new OnOffValue("ON", "OFF"));
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("stateTopic", "commandTopic").build(), textChannelUID,
                        value, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        ThingHandlerHelper.setConnection(thingHandler, connection);

        StringType updateValue = new StringType("ON");
        thingHandler.handleCommand(textChannelUID, updateValue);

        verify(value).update(eq(updateValue));
        assertThat(channelConfig.getCache().getChannelState(), is(OnOffType.ON));
    }

    @Test
    public void processMessage() {
        TextValue textValue = new TextValue();
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("test/state", "test/state/set").build(), textChannelUID,
                        textValue, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        byte payload[] = "UPDATE".getBytes();
        // Test process message
        channelConfig.processMessage("test/state", payload);

        verify(callback).statusUpdated(eq(thing), argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        verify(callback).stateUpdated(eq(textChannelUID), argThat(arg -> "UPDATE".equals(arg.toString())));
        assertThat(textValue.getChannelState().toString(), is("UPDATE"));
    }
}
