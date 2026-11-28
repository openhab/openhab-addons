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
package org.openhab.binding.matter.internal.controller.devices.converter;

import java.math.BigDecimal;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.builder.ChannelBuilder;

/**
 * Base class for Matter converter tests
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public abstract class BaseMatterConverterTest {

    @NonNullByDefault({})
    protected MatterBridgeClient mockBridgeClient = new MatterBridgeClient();
    @Mock
    @NonNullByDefault({})
    protected BaseThingHandlerFactory mockThingHandlerFactory;
    @Mock
    @NonNullByDefault({})
    protected MatterStateDescriptionOptionProvider mockStateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    protected MatterChannelTypeProvider mockChannelTypeProvider;
    @Mock
    @NonNullByDefault({})
    protected MatterConfigDescriptionProvider mockConfigDescriptionProvider;
    @NonNullByDefault({})
    protected TestMatterBaseThingHandler mockHandler;
    @NonNullByDefault({})
    protected TemperatureMeasurementConverter converter;
    @Mock
    @NonNullByDefault({})
    protected TranslationService mockTranslationService;

    @SuppressWarnings("null")
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHandler = Mockito.spy(
                new TestMatterBaseThingHandler(mockBridgeClient, mockThingHandlerFactory, mockStateDescriptionProvider,
                        mockChannelTypeProvider, mockConfigDescriptionProvider, mockTranslationService));
    }

    protected void setChannelTransitionTime(ChannelUID channelUID, String seconds) {
        setChannelConfiguration(channelUID, Map.of("transitionTime", new BigDecimal(seconds)));
    }

    protected void setChannelConfiguration(ChannelUID channelUID, Map<String, Object> configuration) {
        Thing thing = spyThing();
        Mockito.doReturn(ChannelBuilder.create(channelUID).withConfiguration(new Configuration(configuration)).build())
                .when(thing).getChannel(channelUID);
    }

    protected void setThingStatus(ThingStatus status) {
        Thing thing = spyThing();
        Mockito.doReturn(status).when(thing).getStatus();
    }

    private Thing spyThing() {
        Thing current = mockHandler.getThing();
        Thing thing = Mockito.mockingDetails(current).isSpy() ? current : Mockito.spy(current);
        Mockito.doReturn(thing).when(mockHandler).getThing();
        return thing;
    }
}
