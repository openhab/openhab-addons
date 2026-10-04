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
package org.openhab.binding.modbus.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.modbus.internal.ModbusBindingConstantsInternal;
import org.openhab.binding.modbus.internal.ModbusChannelTypeProvider;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;
import org.openhab.core.types.RefreshType;

/**
 * @author Sami Salonen - Initial contribution
 */
@NonNullByDefault
public class ModbusPoller2ThingHandlerTest {

    @Test
    public void reusesTheGeneratedChannelTypeWhenTheBridgeIsInitializedAgain() {
        Bridge bridge = BridgeBuilder.create(ModbusBindingConstantsInternal.THING_TYPE_MODBUS_POLLER2, "poller2")
                .withConfiguration(new Configuration()).withChannel(ChannelBuilder
                        .create(new ChannelUID("modbus:poller2:poller2:value")).withAcceptedItemType("Number").build())
                .build();
        ModbusChannelTypeProvider provider = new ModbusChannelTypeProvider();
        ModbusPoller2ThingHandler handler = new ModbusPoller2ThingHandler(bridge, provider);
        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        handler.setCallback(callback);

        handler.initialize();

        ArgumentCaptor<Thing> updatedThing = ArgumentCaptor.forClass(Thing.class);
        verify(callback).thingUpdated(updatedThing.capture());
        Channel generatedChannel = updatedThing.getValue().getChannels().getFirst();
        assertEquals(new ChannelTypeUID("modbus", "poller2-generated-number"), generatedChannel.getChannelTypeUID());
        assertEquals(1, provider.getChannelTypes(null).size());

        handler.thingUpdated(updatedThing.getValue());
        handler.initialize();

        verify(callback, times(1)).thingUpdated(any(Thing.class));
        assertEquals(1, provider.getChannelTypes(null).size());
    }

    @Test
    public void routesRefreshCommandsThroughThePollerRefreshLifecycle() {
        ModbusPoller2ThingHandler handler = spy(
                new ModbusPoller2ThingHandler(mock(Bridge.class), new ModbusChannelTypeProvider()));

        handler.handleCommand(mock(ChannelUID.class), RefreshType.REFRESH);

        verify(handler).refresh();
    }
}
