/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.UnDefType;

/**
 * Tests cases for {@link GenericMQTTThingHandler}.
 *
 * @author David Graeff - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class GenericThingHandlerTests {

    private @Mock @NonNullByDefault({}) ThingHandlerCallback callbackMock;
    private @Mock @NonNullByDefault({}) Thing thingMock;
    private @Mock @NonNullByDefault({}) AbstractBrokerHandler bridgeHandlerMock;
    private @Mock @NonNullByDefault({}) MqttBrokerConnection connectionMock;

    private @NonNullByDefault({}) GenericMQTTThingHandler thingHandler;

    @BeforeEach
    public void setUp() {
        ThingStatusInfo thingStatus = new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);

        // Mock the thing: We need the thingUID and the bridgeUID
        when(thingMock.getUID()).thenReturn(TEST_GENERIC_THING);
        when(thingMock.getChannels()).thenReturn(THING_CHANNEL_LIST);
        when(thingMock.getStatusInfo()).thenReturn(thingStatus);
        when(thingMock.getConfiguration()).thenReturn(new Configuration());

        // Return the mocked connection object if the bridge handler is asked for it
        when(bridgeHandlerMock.getConnectionAsync()).thenReturn(CompletableFuture.completedFuture(connectionMock));

        CompletableFuture<@Nullable Void> voidFutureComplete = new CompletableFuture<>();
        voidFutureComplete.complete(null);
        doReturn(voidFutureComplete).when(connectionMock).unsubscribeAll();
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).subscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).unsubscribe(any(), any());
        doReturn(CompletableFuture.completedFuture(true)).when(connectionMock).publish(any(), any(), anyInt(),
                anyBoolean());

        thingHandler = spy(new GenericMQTTThingHandler(thingMock, mock(MqttChannelStateDescriptionProvider.class),
                mock(TransformationServiceProvider.class), 1500));
        thingHandler.setCallback(callbackMock);

        // Return the bridge handler if the thing handler asks for it
        doReturn(bridgeHandlerMock).when(thingHandler).getBridgeHandler();

        // The broker connection bridge is by default online
        doReturn(thingStatus).when(thingHandler).getBridgeStatus();
    }

    @Test
    public void initializeWithUnknownThingUID() {
        ChannelConfig config = textConfiguration().as(ChannelConfig.class);
        assertThrows(IllegalArgumentException.class,
                () -> thingHandler.createChannelState(config, new ChannelUID(TEST_GENERIC_THING, "test"),
                        ValueFactory.createValueState(config, UNKNOWN_CHANNEL.getId())));
    }

    @Test
    public void initialize() {
        thingHandler.initialize();
        verify(thingHandler).bridgeStatusChanged(any());
        verify(thingHandler).start(any());
        assertThat(thingHandler.getConnection(), is(connectionMock));

        ChannelState channelConfig = thingHandler.channelStateByChannelUID.get(TEXT_CHANNEL_UID);
        assertThat(channelConfig.getStateTopic(), is("test/state"));
        assertThat(channelConfig.getCommandTopic(), is("test/command"));

        verify(connectionMock).subscribe(eq(channelConfig.getStateTopic()), eq(channelConfig));

        verify(callbackMock).statusUpdated(eq(thingMock), argThat(arg -> ThingStatus.ONLINE.equals(arg.getStatus())
                && ThingStatusDetail.NONE.equals(arg.getStatusDetail())));
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

        ThingHandlerHelper.setConnection(thingHandler, connectionMock);

        thingHandler.handleCommand(TEXT_CHANNEL_UID, RefreshType.REFRESH);
        verify(callbackMock).stateUpdated(eq(TEXT_CHANNEL_UID), argThat(arg -> "DEMOVALUE".equals(arg.toString())));
    }

    @Test
    public void handleCommandUpdateString() {
        TextValue value = spy(new TextValue());
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("stateTopic", "commandTopic").build(), TEXT_CHANNEL_UID,
                        value, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        ThingHandlerHelper.setConnection(thingHandler, connectionMock);

        StringType updateValue = new StringType("UPDATE");
        thingHandler.handleCommand(TEXT_CHANNEL_UID, updateValue);
        verify(value).parseCommand(eq(updateValue));
        // It didn't update the cached state
        assertThat(value.getChannelState(), is(UnDefType.UNDEF));
    }

    @Test
    public void handleCommandUpdateBoolean() {
        OnOffValue value = spy(new OnOffValue("ON", "OFF"));
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("stateTopic", "commandTopic").build(), TEXT_CHANNEL_UID,
                        value, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        ThingHandlerHelper.setConnection(thingHandler, connectionMock);

        StringType updateValue = new StringType("ON");
        thingHandler.handleCommand(TEXT_CHANNEL_UID, updateValue);

        verify(value).parseCommand(eq(updateValue));
    }

    @Test
    public void processMessage() {
        TextValue textValue = new TextValue();
        ChannelState channelConfig = spy(
                new ChannelState(ChannelConfigBuilder.create("test/state", "test/state/set").build(), TEXT_CHANNEL_UID,
                        textValue, thingHandler));
        doReturn(channelConfig).when(thingHandler).createChannelState(any(), any(), any());
        thingHandler.initialize();
        byte payload[] = "UPDATE".getBytes();
        // Test process message
        channelConfig.processMessage("test/state", payload);

        verify(callbackMock, atLeastOnce()).statusUpdated(eq(thingMock),
                argThat(arg -> ThingStatus.ONLINE.equals(arg.getStatus())));

        verify(callbackMock).stateUpdated(eq(TEXT_CHANNEL_UID), argThat(arg -> "UPDATE".equals(arg.toString())));
        assertThat(textValue.getChannelState().toString(), is("UPDATE"));
    }

    @Test
    public void handleBridgeStatusChange() {
        Configuration config = new Configuration();
        config.put("availabilityTopic", "test/LWT");
        when(thingMock.getConfiguration()).thenReturn(config);
        thingHandler.initialize();
        thingHandler
                .bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, null));
        thingHandler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, null));
        verify(connectionMock, times(2)).subscribe(eq("test/LWT"), any());
    }
}
