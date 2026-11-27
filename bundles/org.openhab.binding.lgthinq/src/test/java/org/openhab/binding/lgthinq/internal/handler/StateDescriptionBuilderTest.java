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
package org.openhab.binding.lgthinq.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.lgthinq.internal.LGThinQStateDescriptionProvider;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.StateOption;

/**
 * @author Nemer Daud - Initial contribution
 */
@NonNullByDefault
class StateDescriptionBuilderTest {

    private LGThinQStateDescriptionProvider provider = mock(LGThinQStateDescriptionProvider.class);
    private ChannelUID channelUID = new ChannelUID(new ThingUID("lgthinq", "ac", "test"), "mode");

    @BeforeEach
    void setUp() {
        provider = mock(LGThinQStateDescriptionProvider.class);
        channelUID = new ChannelUID(new ThingUID("lgthinq", "ac", "test"), "mode");
    }

    @Test
    void setStateOptionsEmptyMappingRegistersEmptyList() {
        StateDescriptionBuilder.setStateOptions(provider, channelUID, Map.of());
        ArgumentCaptor<List<StateOption>> captor = ArgumentCaptor.forClass(List.class);
        verify(provider).setStateOptions(eq(channelUID), captor.capture());
        assertTrue(captor.getValue().isEmpty());
    }

    @Test
    void setStateOptionsStringMappingUsesValueAsLabel() {
        StateDescriptionBuilder.setStateOptions(provider, channelUID, Map.of("COOL", "Cooling"));
        ArgumentCaptor<List<StateOption>> captor = ArgumentCaptor.forClass(List.class);
        verify(provider).setStateOptions(eq(channelUID), captor.capture());
        List<StateOption> opts = captor.getValue();
        assertEquals(1, opts.size());
        assertEquals("COOL", opts.get(0).getValue());
        assertEquals("Cooling", opts.get(0).getLabel());
    }

    @Test
    void setStateOptionsI18nMappingTranslatesViaFallbackMap() {
        Map<String, String> rawMapping = Map.of("KEY1", "RAW_VALUE");
        Map<String, String> i18n = Map.of("RAW_VALUE", "Translated Label");
        StateDescriptionBuilder.setStateOptions(provider, channelUID, rawMapping, i18n);
        ArgumentCaptor<List<StateOption>> captor = ArgumentCaptor.forClass(List.class);
        verify(provider).setStateOptions(eq(channelUID), captor.capture());
        assertEquals("Translated Label", captor.getValue().get(0).getLabel());
    }

    @Test
    void setStateOptionsI18nMappingFallsBackToRawValueWhenKeyNotFound() {
        Map<String, String> rawMapping = Map.of("KEY1", "UNKNOWN_VALUE");
        Map<String, String> i18n = Map.of("KNOWN_VALUE", "Translated");
        StateDescriptionBuilder.setStateOptions(provider, channelUID, rawMapping, i18n);
        ArgumentCaptor<List<StateOption>> captor = ArgumentCaptor.forClass(List.class);
        verify(provider).setStateOptions(eq(channelUID), captor.capture());
        assertEquals("UNKNOWN_VALUE", captor.getValue().get(0).getLabel());
    }

    @Test
    void setStateOptionsLambdaMappingAppliesCustomLabelFunction() {
        Map<String, String> mapping = Map.of("K", "value");
        StateDescriptionBuilder.setStateOptions(provider, channelUID, mapping, e -> "custom-" + e.getValue());
        ArgumentCaptor<List<StateOption>> captor = ArgumentCaptor.forClass(List.class);
        verify(provider).setStateOptions(eq(channelUID), captor.capture());
        assertEquals("custom-value", captor.getValue().get(0).getLabel());
    }

    @Test
    void setStateOptionsAlwaysForwardsToProvider() {
        StateDescriptionBuilder.setStateOptions(provider, channelUID, Map.of("A", "B"));
        verify(provider, times(1)).setStateOptions(eq(channelUID), any());
    }
}
