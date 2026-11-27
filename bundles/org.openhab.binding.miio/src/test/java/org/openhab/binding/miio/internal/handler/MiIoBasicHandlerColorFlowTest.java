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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.miio.internal.MiIoBindingConstants;
import org.openhab.binding.miio.internal.basic.BasicChannelTypeProvider;
import org.openhab.binding.miio.internal.basic.MiIoBasicChannel;
import org.openhab.binding.miio.internal.basic.MiIoBasicDevice;
import org.openhab.binding.miio.internal.basic.MiIoDatabaseWatchService;
import org.openhab.binding.miio.internal.cloud.CloudConnector;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.type.ChannelTypeRegistry;
import org.openhab.core.types.Command;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

/**
 * Tests the commands sent for the Yeelight color flow channels defined in the device database.
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MiIoBasicHandlerColorFlowTest {

    private static final String SUNRISE_FLOW = "\"50,1,16731392,1,360000,2,1700,10,540000,2,2700,100\"";
    private static final String DEFAULT_FLOW = "\"500,1,255,100,500,1,5292678,100,500,1,11673869,100,500,1,16776960,100,500,1,7733248,100\"";

    private @Mock @NonNullByDefault({}) Thing thing;
    private @Mock @NonNullByDefault({}) MiIoDatabaseWatchService miIoDatabaseWatchService;
    private @Mock @NonNullByDefault({}) CloudConnector cloudConnector;
    private @Mock @NonNullByDefault({}) ChannelTypeRegistry channelTypeRegistry;
    private @Mock @NonNullByDefault({}) BasicChannelTypeProvider basicChannelTypeProvider;
    private @Mock @NonNullByDefault({}) TranslationProvider translationProvider;
    private @Mock @NonNullByDefault({}) LocaleProvider localeProvider;

    private final ThingUID thingUID = new ThingUID(MiIoBindingConstants.THING_TYPE_BASIC, "TestThing");
    private @NonNullByDefault({}) CapturingHandler handler;

    private class CapturingHandler extends MiIoBasicHandler {
        private final List<String> sentCommands = new ArrayList<>();

        CapturingHandler(Thing thing, MiIoDatabaseWatchService miIoDatabaseWatchService, CloudConnector cloudConnector,
                ChannelTypeRegistry channelTypeRegistry, BasicChannelTypeProvider basicChannelTypeProvider,
                TranslationProvider translationProvider, LocaleProvider localeProvider) {
            super(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry, basicChannelTypeProvider,
                    translationProvider, localeProvider);
        }

        void loadDatabase(String fileName) throws IOException {
            try (InputStream is = getClass().getResourceAsStream("/database/" + fileName)) {
                assertNotNull(is, "Database file not found: " + fileName);
                MiIoBasicDevice device = new Gson().fromJson(
                        JsonParser.parseReader(new InputStreamReader(is, StandardCharsets.UTF_8)),
                        MiIoBasicDevice.class);
                for (MiIoBasicChannel channel : device.getDevice().getChannels()) {
                    actions.put(new ChannelUID(thingUID, channel.getChannel()), channel);
                }
            }
        }

        List<String> send(String channel, Command command) {
            sentCommands.clear();
            handleCommand(new ChannelUID(thingUID, channel), command);
            return new ArrayList<>(sentCommands);
        }

        @Override
        protected int sendCommand(String command, String params, String cloudServer, String sender) {
            sentCommands.add(command + params);
            return 1;
        }
    }

    @BeforeEach
    public void setUp() {
        when(thing.getUID()).thenReturn(thingUID);
        handler = new CapturingHandler(thing, miIoDatabaseWatchService, cloudConnector, channelTypeRegistry,
                basicChannelTypeProvider, translationProvider, localeProvider);
    }

    @Test
    public void colorFlowSwitchStartsAndStopsFlow() throws IOException {
        handler.loadDatabase("yeelink.light.color1.json");

        assertEquals(List.of("start_cf[0,0," + DEFAULT_FLOW + "]"), handler.send("colorflow", OnOffType.ON));
        assertEquals(List.of("stop_cf[]"), handler.send("colorflow", OnOffType.OFF));
    }

    @Test
    public void colorFlowSceneStartsSelectedFlow() throws IOException {
        handler.loadDatabase("yeelink.light.color1.json");

        assertEquals(List.of("start_cf[3,1," + SUNRISE_FLOW + "]"),
                handler.send("colorflowScene", new StringType("sunrise")));
        assertEquals(List.of("start_cf[0,0,\"300,1,16711680,100,300,1,255,100\"]"),
                handler.send("colorflowScene", new StringType("police")));
        assertEquals(List.of(), handler.send("colorflowScene", new StringType("unknown")));
    }

    @Test
    public void ambientColorFlowUsesBackgroundCommands() throws IOException {
        for (String fileName : List.of("yeelink.light.ceiling4.json", "yeelink.light.light15.json")) {
            handler.loadDatabase(fileName);

            assertEquals(List.of("bg_start_cf[0,0," + DEFAULT_FLOW + "]"),
                    handler.send("ambientColorflow", OnOffType.ON), fileName);
            assertEquals(List.of("bg_stop_cf[]"), handler.send("ambientColorflow", OnOffType.OFF), fileName);
            assertEquals(List.of("bg_start_cf[3,1," + SUNRISE_FLOW + "]"),
                    handler.send("ambientColorflowScene", new StringType("sunrise")), fileName);
        }
    }
}
