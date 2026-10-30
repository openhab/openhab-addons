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
package org.openhab.binding.matter.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterConfigDescriptionProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.controller.devices.converter.TestMatterBaseThingHandler;
import org.openhab.binding.matter.internal.util.TranslationService;
import org.openhab.core.config.core.ConfigDescription;
import org.openhab.core.config.core.ConfigDescriptionBuilder;
import org.openhab.core.config.core.ConfigDescriptionParameter;
import org.openhab.core.config.core.ConfigDescriptionParameter.Type;
import org.openhab.core.config.core.ConfigDescriptionParameterBuilder;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;

/**
 * Tests for {@link MatterBaseThingHandler#addConfigDescription}.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ConfigDescriptionPartsTest {
    @Mock
    @NonNullByDefault({})
    private BaseThingHandlerFactory thingHandlerFactory;
    @Mock
    @NonNullByDefault({})
    private MatterStateDescriptionOptionProvider stateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private MatterChannelTypeProvider channelTypeProvider;
    @Mock
    @NonNullByDefault({})
    private MatterConfigDescriptionProvider configDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private TranslationService translationService;
    @NonNullByDefault({})
    private TestMatterBaseThingHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new TestMatterBaseThingHandler(new MatterBridgeClient(), thingHandlerFactory,
                stateDescriptionProvider, channelTypeProvider, configDescriptionProvider, translationService);
    }

    @Test
    void testPartsWithDifferentKeysAreMergedInOrder() {
        handler.addConfigDescription("a", part("first"));
        handler.addConfigDescription("b", part("second"));
        assertEquals(List.of("first", "second"), publishedParameterNames());
    }

    @Test
    void testPartWithTheSameKeyReplacesItAndKeepsItsPlace() {
        handler.addConfigDescription("a", part("first"));
        handler.addConfigDescription("b", part("second"));
        handler.addConfigDescription("a", part("replaced"));
        assertEquals(List.of("replaced", "second"), publishedParameterNames());
    }

    private ConfigDescription part(String parameterName) {
        return ConfigDescriptionBuilder.create(handler.getConfigDescriptionURI())
                .withParameter(ConfigDescriptionParameterBuilder.create(parameterName, Type.TEXT).build()).build();
    }

    private List<String> publishedParameterNames() {
        ArgumentCaptor<ConfigDescription> captor = ArgumentCaptor.forClass(ConfigDescription.class);
        verify(configDescriptionProvider, atLeastOnce()).addConfigDescription(captor.capture());
        return captor.getValue().getParameters().stream().map(ConfigDescriptionParameter::getName).toList();
    }
}
