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
package org.openhab.binding.mqtt.homeassistant.internal;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.mqtt.generic.MqttChannelStateDescriptionProvider;
import org.openhab.binding.mqtt.generic.MqttChannelTypeProvider;
import org.openhab.binding.mqtt.homeassistant.generic.internal.MqttThingHandlerFactory;
import org.openhab.binding.mqtt.homeassistant.internal.component.AbstractComponent;
import org.openhab.core.i18n.UnitProvider;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.thing.type.ThingTypeRegistry;

/**
 * @author Jochen Klein - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class HomeAssistantChannelTransformationTests {
    protected @Mock @NonNullByDefault({}) ThingTypeRegistry thingTypeRegistry;
    protected @Mock @NonNullByDefault({}) UnitProvider unitProvider;

    protected @NonNullByDefault({}) HomeAssistantChannelTransformation transformation;

    @BeforeEach
    public void beforeEachChannelTransformationTest() {
        MqttChannelTypeProvider channelTypeProvider = new MqttChannelTypeProvider(thingTypeRegistry,
                new VolatileStorageService());
        MqttChannelStateDescriptionProvider stateDescriptionProvider = new MqttChannelStateDescriptionProvider();
        ChannelTypeRegistry channelTypeRegistry = new ChannelTypeRegistry();
        MqttThingHandlerFactory thingHandlerFactory = new MqttThingHandlerFactory(channelTypeProvider,
                stateDescriptionProvider, channelTypeRegistry, unitProvider);

        AbstractComponent component = Mockito.mock(AbstractComponent.class);
        HaID haID = new HaID("homeassistant/light/pool/light/config");
        when(component.getHaID()).thenReturn(haID);
        transformation = new HomeAssistantChannelTransformation(thingHandlerFactory.getJinjava(), component, "");
    }

    @Test
    public void testIif() {
        assertThat(transform("{{ iif(True) }}", ""), is("true"));
        assertThat(transform("{{ iif(False) }}", ""), is("false"));
        assertThat(transform("{{ iif(Null) }}", ""), is("false"));
        assertThat(transform("{{ iif(True, 'Yes') }}", ""), is("Yes"));
        assertThat(transform("{{ iif(False, 'Yes') }}", ""), is("false"));
        assertThat(transform("{{ iif(Null, 'Yes') }}", ""), is("false"));
        assertThat(transform("{{ iif(True, 'Yes', 'No') }}", ""), is("Yes"));
        assertThat(transform("{{ iif(False, 'Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ iif(Null, 'Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ iif(True, 'Yes', 'No', null) }}", ""), is("Yes"));
        assertThat(transform("{{ iif(False, 'Yes', 'No', null) }}", ""), is("No"));
        assertThat(transform("{{ iif(Null, 'Yes', 'No', 'NULL') }}", ""), is("NULL"));
        assertThat(transform("{{ iif(Null, 'Yes', 'No', null) }}", ""), is(""));
        assertThat(transform("{{ iif(True, 'Yes', 'No', null, null) }}", ""), is(nullValue()));

        assertThat(transform("{{ True | iif('Yes') }}", ""), is("Yes"));
        assertThat(transform("{{ False | iif('Yes') }}", ""), is("false"));
        assertThat(transform("{{ Null | iif('Yes') }}", ""), is("false"));
        assertThat(transform("{{ True | iif('Yes', 'No') }}", ""), is("Yes"));
        assertThat(transform("{{ False | iif('Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ Null | iif('Yes', 'No') }}", ""), is("No"));
        assertThat(transform("{{ True | iif('Yes', 'No', null) }}", ""), is("Yes"));
        assertThat(transform("{{ False | iif('Yes', 'No', null) }}", ""), is("No"));
        assertThat(transform("{{ Null | iif('Yes', 'No', 'NULL') }}", ""), is("NULL"));
        assertThat(transform("{{ Null | iif('Yes', 'No', null) }}", ""), is(""));
        assertThat(transform("{{ True | iif('Yes', 'No', null, null) }}", ""), is(nullValue()));
    }

    @Test
    public void testIsDefined() {
        assertThat(transform("{{ value_json.val | is_defined }}", "{}"), is(nullValue()));
        assertThat(transform("{{ 'hi' | is_defined }}", "{}"), is("hi"));
    }

    protected @Nullable String transform(String template, String value) {
        return transformation.apply(template, value).orElse(null);
    }
}
