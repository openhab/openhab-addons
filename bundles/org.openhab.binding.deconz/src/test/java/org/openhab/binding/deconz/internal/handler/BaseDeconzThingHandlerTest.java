/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.deconz.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.deconz.internal.BindingConstants.BINDING_ID;
import static org.openhab.binding.deconz.internal.BindingConstants.BRIDGE_TYPE;
import static org.openhab.binding.deconz.internal.BindingConstants.CONFIG_ID;
import static org.openhab.binding.deconz.internal.BindingConstants.THING_TYPE_THERMOSTAT;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openhab.binding.deconz.DeconzTest;
import org.openhab.binding.deconz.internal.dto.BridgeFullState;
import org.openhab.binding.deconz.internal.dto.DeconzBaseMessage;
import org.openhab.binding.deconz.internal.dto.SensorMessage;
import org.openhab.binding.deconz.internal.netutils.WebSocketConnection;
import org.openhab.binding.deconz.internal.types.LightType;
import org.openhab.binding.deconz.internal.types.LightTypeDeserializer;
import org.openhab.binding.deconz.internal.types.ResourceType;
import org.openhab.binding.deconz.internal.types.ThermostatMode;
import org.openhab.binding.deconz.internal.types.ThermostatModeGsonTypeAdapter;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * The {@link BaseDeconzThingHandlerTest} is the base class for test classes that are used to test subclasses of
 * {@link DeconzBaseThingHandler}
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BaseDeconzThingHandlerTest extends JavaTest {
    private static final ThingUID BRIDGE_UID = new ThingUID(BRIDGE_TYPE, "bridge");
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_THERMOSTAT, "thing");
    protected @NonNullByDefault({}) DeconzBaseMessage deconzMessage;
    private @Mock @NonNullByDefault({}) Bridge bridge;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @Mock @NonNullByDefault({}) DeconzBridgeHandler bridgeHandler;
    private @Mock @NonNullByDefault({}) WebSocketConnection webSocketConnection;
    private @Mock @NonNullByDefault({}) BridgeFullState bridgeFullState;
    private @NonNullByDefault({}) Gson gson;
    private @NonNullByDefault({}) Thing thing;
    private @NonNullByDefault({}) DeconzBaseThingHandler thingHandler;

    @BeforeEach
    public void setupMocks() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(LightType.class, new LightTypeDeserializer());
        gsonBuilder.registerTypeAdapter(ThermostatMode.class, new ThermostatModeGsonTypeAdapter());
        gson = gsonBuilder.create();

        when(callback.getBridge(BRIDGE_UID)).thenReturn(bridge);
        when(callback.createChannelBuilder(any(ChannelUID.class), any(ChannelTypeUID.class)))
                .thenAnswer(i -> ChannelBuilder.create((ChannelUID) i.getArgument(0)).withType(i.getArgument(1)));
        doAnswer(i -> {
            thing = i.getArgument(0);
            thingHandler.thingUpdated(thing);
            return null;
        }).when(callback).thingUpdated(any(Thing.class));

        when(bridge.getStatusInfo()).thenReturn(new ThingStatusInfo(ThingStatus.ONLINE, ThingStatusDetail.NONE, ""));
        when(bridge.getHandler()).thenReturn(bridgeHandler);

        when(bridgeHandler.getWebSocketConnection()).thenReturn(webSocketConnection);
        when(bridgeHandler.getBridgeFullState())
                .thenReturn(CompletableFuture.completedFuture(Optional.of(bridgeFullState)));

        when(bridgeFullState.getMessage(ResourceType.SENSORS, "1")).thenAnswer(i -> deconzMessage);
    }

    protected void createThing(ThingTypeUID thingTypeUID, List<String> channels,
            BiFunction<Thing, Gson, DeconzBaseThingHandler> handlerSupplier) {
        ThingBuilder thingBuilder = ThingBuilder.create(thingTypeUID, THING_UID);
        thingBuilder.withBridge(BRIDGE_UID);
        for (String channelId : channels) {
            Channel channel = ChannelBuilder.create(new ChannelUID(THING_UID, channelId))
                    .withType(new ChannelTypeUID(BINDING_ID, channelId)).build();
            thingBuilder.withChannel(channel);
        }
        thingBuilder.withConfiguration(new Configuration(Map.of(CONFIG_ID, "1")));
        thing = thingBuilder.build();

        thingHandler = handlerSupplier.apply(thing, gson);
        thingHandler.setCallback(callback);
    }

    protected void assertThing(String fileName, Set<TestParam> expected) throws IOException {
        deconzMessage = DeconzTest.getObjectFromJson(fileName, SensorMessage.class, gson);

        thingHandler.initialize();

        ArgumentCaptor<ThingStatusInfo> captor = ArgumentCaptor.forClass(ThingStatusInfo.class);
        verify(callback, atLeast(2).description("assertQuantityOfStatusUpdates")).statusUpdated(eq(thing),
                captor.capture());

        List<ThingStatusInfo> statusInfoList = captor.getAllValues();
        assertThat("assertFirstThingStatus", statusInfoList.get(0).getStatus(), is(ThingStatus.UNKNOWN));
        assertThat("assertLastThingStatus", statusInfoList.get(statusInfoList.size() - 1).getStatus(),
                is(ThingStatus.ONLINE));

        assertThat("assertChannelCount:" + getAllChannels(thing), thing.getChannels().size(), is(expected.size()));
        for (TestParam testParam : expected) {
            Channel channel = thing.getChannel(testParam.channelId());
            assertThat("assertNonNullChannel:" + testParam.channelId, channel, is(notNullValue()));

            State state = testParam.state;
            if (channel != null && state != null) {
                verify(callback, times(3).description(channel + " did not receive an update"))
                        .stateUpdated(eq(channel.getUID()), eq(state));
            }
        }
    }

    private String getAllChannels(Thing thing) {
        return thing.getChannels().stream().map(Channel::getUID).map(ChannelUID::getId)
                .collect(Collectors.joining(","));
    }

    protected record TestParam(String channelId, @Nullable State state) {
    }
}
