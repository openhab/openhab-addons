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
package org.openhab.binding.hasslink.internal.handler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.openhab.binding.hasslink.internal.HassLinkBindingConstants;
import org.openhab.binding.hasslink.internal.HassLinkDynamicCommandDescriptionProvider;
import org.openhab.binding.hasslink.internal.HassLinkDynamicStateDescriptionProvider;
import org.openhab.binding.hasslink.internal.api.dto.EntityState;
import org.openhab.binding.hasslink.internal.entity.impl.EventEntity;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ChannelKind;
import org.openhab.core.types.State;

import com.google.gson.JsonPrimitive;

class HassLinkDeviceHandlerTest {

    private static final String ENTITY_ID = "event.test_button";

    @Test
    void dispatchesTriggerChannelWithoutItemLink() throws ReflectiveOperationException {
        HandlerFixture fixture = new HandlerFixture(ChannelKind.TRIGGER);

        fixture.handler.onEntityStateUpdate(fixture.entityState);

        verify(fixture.callback, times(1)).channelTriggered(fixture.thing, fixture.channelUID, "button_pressed");
        verify(fixture.callback, never()).isChannelLinked(fixture.channelUID);
    }

    @Test
    void doesNotDispatchUnlinkedStateChannel() throws ReflectiveOperationException {
        HandlerFixture fixture = new HandlerFixture(ChannelKind.STATE);

        fixture.handler.onEntityStateUpdate(fixture.entityState);

        verify(fixture.callback, never()).stateUpdated(any(ChannelUID.class), any(State.class));
        verify(fixture.callback, never()).channelTriggered(fixture.thing, fixture.channelUID, "button_pressed");
    }

    private static final class HandlerFixture {
        private final Thing thing;
        private final ChannelUID channelUID;
        private final EntityState entityState = new EntityState(ENTITY_ID, "event",
                Map.of("event_type", new JsonPrimitive("button_pressed")));
        private final ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        private final HassLinkDeviceHandler handler;

        HandlerFixture(ChannelKind channelKind) throws ReflectiveOperationException {
            ThingUID thingUID = new ThingUID(HassLinkBindingConstants.THING_TYPE_DEVICE, "test_device");
            ThingUID bridgeUID = new ThingUID(HassLinkBindingConstants.THING_TYPE_BRIDGE, "test_server");
            channelUID = new ChannelUID(thingUID, "event");
            Channel channel = ChannelBuilder.create(channelUID).withKind(channelKind).build();
            thing = ThingBuilder.create(HassLinkBindingConstants.THING_TYPE_DEVICE, thingUID).withBridge(bridgeUID)
                    .withChannel(channel).build();

            HassLinkBridgeHandler bridgeHandler = mock(HassLinkBridgeHandler.class);
            Bridge bridge = mock(Bridge.class);
            when(callback.getBridge(bridgeUID)).thenReturn(bridge);
            when(bridge.getHandler()).thenReturn(bridgeHandler);
            when(callback.isChannelLinked(channelUID)).thenReturn(false);
            when(bridgeHandler.getEntityType(entityState)).thenReturn(new EventEntity());

            handler = new HassLinkDeviceHandler(thing, mock(HassLinkDynamicStateDescriptionProvider.class),
                    mock(HassLinkDynamicCommandDescriptionProvider.class));
            handler.setCallback(callback);

            Field channelsInitialized = HassLinkDeviceHandler.class.getDeclaredField("channelsInitialized");
            channelsInitialized.setAccessible(true);
            channelsInitialized.setBoolean(handler, true);

            Field entityToChannelsMap = HassLinkDeviceHandler.class.getDeclaredField("entityToChannelsMap");
            entityToChannelsMap.setAccessible(true);
            @SuppressWarnings("unchecked")
            Map<String, Set<ChannelUID>> mappings = (Map<String, Set<ChannelUID>>) entityToChannelsMap.get(handler);
            mappings.put(ENTITY_ID, Set.of(channelUID));
        }
    }
}
