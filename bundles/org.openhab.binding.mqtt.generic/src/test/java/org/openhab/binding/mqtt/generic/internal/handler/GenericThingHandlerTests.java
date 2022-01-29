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
package org.openhab.binding.mqtt.generic.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mqtt.generic.internal.handler.ThingChannelConstants.*;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.ChannelConfig;
import org.openhab.binding.mqtt.generic.ChannelConfigBuilder;
import org.openhab.binding.mqtt.generic.ChannelState;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.ThingHandlerHelper;
import org.openhab.binding.mqtt.generic.TransformationServiceProvider;
import org.openhab.binding.mqtt.generic.values.OnOffValue;
import org.openhab.binding.mqtt.generic.values.TextValue;
import org.openhab.binding.mqtt.generic.values.ValueFactory;
import org.openhab.binding.mqtt.handler.AbstractBrokerHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.transport.mqtt.MqttBrokerConnection;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;

/**
 * Tests cases for {@link GenericMQTTThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
public class GenericThingHandlerTests {

    private @Mock ThingHandlerCallback callback;
    private @Mock Thing thing;
    private @Mock AbstractBrokerHandler bridgeHandler;
    private @Mock MqttBrokerConnection connection;

    private GenericMQTTThingHandler thingHandler;

    @BeforeEach
    public void setUp() {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Mock the thing: We need the thingUID and the bridgeUID
        when(thing.getUID()).thenReturn(testGenericThing);
        when(thing.getChannels()).thenReturn(thingChannelList);
        when(thing.getStatusInfo()).thenReturn(thingStatus);
        when(thing.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandler.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connection));

        CompletableFuture<Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connection).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connection).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connection).unsubscribe(any(), any());
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

    @Test
    public void initializeWithUnknownThingUID() {
        ChannelConfig config = textConfiguration().as(ChannelConfig.class);
        assertThrows(IllegalArgumentException.class,
                () -> thingHandler.createChannelState(config, new ChannelUID(testGenericThing, "test"),
                        ValueFactory.createValueState(config, unknownChannel.getId())));
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

        verify(callback, atLeastOnce()).statusUpdated(eq(thing),
                argThat(arg -> arg.getStatus().equals(ThingStatus.ONLINE)));

        verify(callback).stateUpdated(eq(textChannelUID), argThat(arg -> "UPDATE".equals(arg.toString())));
        assertThat(textValue.getChannelState().toString(), is("UPDATE"));
    }

    @Test
    public void handleBridgeStatusChange() {
        Configuration config = new Configuration();
        config.put("availabilityTopic", "test/LWT");
        when(thing.getConfiguration()).thenReturn(config);
        thingHandler.initialize();
        thingHandler
                .bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null));
        thingHandler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verify(connection, times(2)).subscribe(eq("test/LWT"), any());
    }
}
