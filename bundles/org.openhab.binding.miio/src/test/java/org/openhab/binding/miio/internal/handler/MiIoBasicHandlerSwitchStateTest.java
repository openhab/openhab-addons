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
package org.openhab.binding.miio.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.State;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Tests the state of Switch and Contact channels for the values read from the device.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoBasicHandlerSwitchStateTest {

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) MiIoDatabaseWatchService miIoDatabaseWatchService;
    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private @Mock @NonNullByDefault({}) BasicChannelTypeProvider basicChannelTypeProvider;
    private @Mock @NonNullByDefault({}) TranslationProvider translationProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;

    private @NonNullByDefault({}) StateCapturingHandler handler;

    private static class StateCapturingHandler extends MiIoBasicHandler {
        private final Map<String, State> states = new HashMap<>();

        StateCapturingHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService,
                CloudConnector cloudConnector, ChannelTypeRegistry channelTypeRegistry,
                BasicChannelTypeProvider basicChannelTypeProvider, TranslationProvider translationProvider,
                LocaleProvider localeProvider) {
            super(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry, basicChannelTypeProvider,
                    translationProvider, localeProvider);
        }

        @Nullable
        State read(String type, String value) {
            MiIoBasicChannel channel = new Gson().fromJson(
                    "{\"property\": \"p\", \"channel\": \"ch\", \"type\": \"" + type + "\", \"actions\": []}",
                    MiIoBasicChannel.class);
            assertNotNull(channel);
            JsonElement json = JsonParser.parseString(value);
            states.clear();
            updateChannel(channel, "p", json);
            return states.get("ch");
        }

        @Override
        protected void updateState(String channelID, State state) {
            states.put(channelID, state);
        }
    }

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(new ThingUID(MiIoBindingConstants.THING_TYPE_BASIC, "TestThing"));
        handler = new StateCapturingHandler(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry,
                basicChannelTypeProvider, translationProvider, localeProvider);
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = { "2;ON", "0;OFF", "'\"2\"';ON", "'\"1\"';ON", "'\"0\"';OFF", "'\"-1\"';OFF",
            "'\"0.5\"';OFF", "'\"1e999999999\"';ON", "'\"on\"';ON", "'\"ON\"';ON", "true;ON", "'\"true\"';ON",
            "false;OFF", "'\"off\"';OFF", "'\"open\"';OFF", "'\"running\"';OFF" })
    public void switchState(String value, String expected) {
        assertEquals(OnOffType.valueOf(expected), handler.read("Switch", value));
    }

    @ParameterizedTest
    @CsvSource(delimiter = ';', value = { "2;OPEN", "0;CLOSED", "'\"2\"';OPEN", "'\"0\"';CLOSED", "'\"open\"';OPEN",
            "'\"on\"';OPEN", "true;OPEN", "'\"closed\"';CLOSED" })
    public void contactState(String value, String expected) {
        assertEquals(OpenClosedType.valueOf(expected), handler.read("Contact", value));
    }
}
