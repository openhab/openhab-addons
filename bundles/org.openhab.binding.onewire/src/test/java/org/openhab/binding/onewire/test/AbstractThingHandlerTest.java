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
package org.openhab.binding.onewire.test;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.openhab.binding.onewire.internal.OwBindingConstants.THING_TYPE_OWSERVER;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.test.java.JavaTest;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.type.ChannelTypeUID;

/**
 * Base class for thing handler tests.
 *
 * @author Jan N. Klug - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public abstract class AbstractThingHandlerTest extends JavaTest {

    protected Map<String, Object> bridgeProperties = new HashMap<>();
    protected Map<String, String> thingProperties = new HashMap<>();
    protected Map<String, Object> thingConfiguration = new HashMap<>();
    protected Map<String, Object> channelProperties = new HashMap<>();

    protected @Mock @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;
    protected @Mock @NonNullByDefault({}) OwDynamicStateDescriptionProvider stateProvider;
    protected @Mock @NonNullByDefault({}) ThingHandlerCallback bridgeHandlerCallback;
    protected @Mock @NonNullByDefault({}) OwserverBridgeHandler bridgeHandler;
    protected @Mock @NonNullByDefault({}) OwserverBridgeHandler secondBridgeHandler;

    protected List<Channel> channels = new ArrayList<>();

    protected @Nullable Bridge bridge;
    protected @Nullable Thing thing;
    protected @Nullable OwBaseThingHandler thingHandler;

    protected @Nullable InOrder inOrder;

    @AfterEach
    public void tearDown() {
        final ThingHandler thingHandler = this.thingHandler;
        if (thingHandler != null) {
            thingHandler.dispose();
        }
    }

    protected void initializeHandlerMocks() {
        final ThingHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            fail("thingHandler is null");
            return;
        }

        thingHandler.getThing().setHandler(thingHandler);
        thingHandler.setCallback(thingHandlerCallback);

        Mockito.doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());

        Mockito.when(thingHandlerCallback.createChannelBuilder(any(), any())).thenAnswer(invocation -> {
            ChannelUID channelUID = (ChannelUID) invocation.getArguments()[0];
            ChannelTypeUID channelTypeUID = (ChannelTypeUID) invocation.getArguments()[1];

            return ChannelBuilder.create(channelUID).withType(channelTypeUID);
        });

        inOrder = Mockito.inOrder(bridgeHandler);
    }

    public void initializeBridge() throws OwException {
        bridgeProperties = new HashMap<>();
        final Bridge bridge = BridgeBuilder.create(THING_TYPE_OWSERVER, "testbridge").withLabel("Test Bridge")
                .withConfiguration(new Configuration(bridgeProperties)).build();
        bridge.setHandler(bridgeHandler);
        this.bridge = bridge;

        Mockito.doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(bridgeHandlerCallback).statusUpdated(any(), any());

        Mockito.doAnswer(answer -> OnOffType.ON).when(bridgeHandler).checkPresence(any());

        Mockito.doAnswer(answer -> new DecimalType(10)).when(bridgeHandler).readDecimalType(any(), any());

        Mockito.doAnswer(answer -> new BitSet(8)).when(bridgeHandler).readBitSet(any(), any());

        Mockito.doAnswer(answer -> {
            final OwBaseThingHandler thingHandler = this.thingHandler;
            if (thingHandler == null) {
                fail("thingHandler is null");
                return null;
            }

            thingHandler.updateSensorProperties(secondBridgeHandler);
            thingHandler.initialize();
            return null;
        }).when(bridgeHandler).scheduleForPropertiesUpdate(any());
    }
}
