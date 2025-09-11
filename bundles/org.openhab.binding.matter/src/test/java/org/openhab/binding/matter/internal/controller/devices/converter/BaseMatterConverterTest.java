/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;

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
}
