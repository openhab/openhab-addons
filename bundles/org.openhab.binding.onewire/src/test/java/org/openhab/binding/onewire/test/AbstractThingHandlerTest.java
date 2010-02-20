/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import static org.mockito.ArgumentMatchers.any;
import static org.openhab.binding.onewire.internal.OwBindingConstants.THING_TYPE_OWSERVER;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Channel;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.eclipse.smarthome.core.thing.binding.builder.BridgeBuilder;
import org.eclipse.smarthome.test.java.JavaTest;
import org.junit.After;
import org.junit.Assert;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.OwDynamicStateDescriptionProvider;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.internal.handler.OwserverBridgeHandler;

/**
 * Base class for thing handler tests.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public abstract class AbstractThingHandlerTest extends JavaTest {

    protected Map<String, Object> bridgeProperties = new HashMap<>();
    protected Map<String, String> thingProperties = new HashMap<>();
    protected Map<String, Object> thingConfiguration = new HashMap<>();
    protected Map<String, Object> channelProperties = new HashMap<>();

    @Mock
    @NonNullByDefault({})
    protected ThingHandlerCallback thingHandlerCallback;

    @Mock
    @NonNullByDefault({})
    protected OwDynamicStateDescriptionProvider stateProvider;

    @Mock
    @NonNullByDefault({})
    protected ThingHandlerCallback bridgeHandlerCallback;

    @Mock
    @NonNullByDefault({})
    protected OwserverBridgeHandler bridgeHandler;

    @Mock
    @NonNullByDefault({})
    protected OwserverBridgeHandler secondBridgeHandler;

    protected List<Channel> channels = new ArrayList<>();

    protected @Nullable Bridge bridge;
    protected @Nullable Thing thing;
    protected @Nullable OwBaseThingHandler thingHandler;

    protected @Nullable InOrder inOrder;

    @After
    public void tearDown() {
        final ThingHandler thingHandler = this.thingHandler;
        if (thingHandler != null) {
            thingHandler.dispose();
        }
    }

    protected void initializeHandlerMocks() {
        final ThingHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            Assert.fail("thingHandler is null");
            return;
        }

        thingHandler.getThing().setHandler(thingHandler);
        thingHandler.setCallback(thingHandlerCallback);

        Mockito.doAnswer(answer -> {
            ((Thing) answer.getArgument(0)).setStatusInfo(answer.getArgument(1));
            return null;
        }).when(thingHandlerCallback).statusUpdated(any(), any());

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
                Assert.fail("thingHandler is null");
                return null;
            }

            thingHandler.updateSensorProperties(secondBridgeHandler);
            thingHandler.initialize();
            return null;
        }).when(bridgeHandler).scheduleForPropertiesUpdate(any());
    }
}
