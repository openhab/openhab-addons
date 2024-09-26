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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.openhab.binding.androidtv.internal.AndroidTVBindingConstants.CHANNEL_TV_CHANNEL;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.androidtv.internal.protocol.philipstv.ConnectionManager;
import org.openhab.binding.androidtv.internal.protocol.philipstv.PhilipsTVConnectionManager;
import org.openhab.core.library.types.StringType;
import org.openhab.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PhilipsTvChannelTest} is responsible for testing {@linkTvChannelService}
 *
 * @author Marcel Verpaalen - Initial contribution
 *
 */

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class PhilipsTvChannelTest {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private @Mock @NonNullByDefault({}) PhilipsTVConnectionManager handler;
    private @Mock @NonNullByDefault({}) ConnectionManager connectionManager;

    static final String NEW_RESPONSE = "{\"version\":\"84384_293_1\",\"id\":\"all\",\"listType\":\"MixedSources\",\"medium\":\"mixed\",\"operator\":\"Ziggo\",\"installCountry\":\"Netherlands\",\"Channel\":[{\"ccid\":142,\"preset\":\"1\",\"name\":\"NPO 1\",\"onid\":1536,\"tsid\":2098,\"sid\":19401,\"serviceType\":\"audio_video\",\"type\":\"DVB_C\",\"logoVersion\":142},{\"ccid\":143,\"preset\":\"2\",\"name\":\"NPO 2\",\"onid\":1536,\"tsid\":2098,\"sid\":19402,\"serviceType\":\"audio_video\",\"type\":\"DVB_C\",\"logoVersion\":143}]}";
    static final String OLD_RESPONSE = "{\"version\":123,\"id\":\"all\",\"listType\":\"MixedSources\",\"medium\":\"mixed\",\"operator\":\"Ziggo\",\"installCountry\":\"Netherlands\",\"Channel\":[{\"ccid\":142,\"preset\":\"1\",\"name\":\"NPO 1\",\"onid\":1536,\"tsid\":2098,\"sid\":19401,\"serviceType\":\"audio_video\",\"type\":\"DVB_C\",\"logoVersion\":142},{\"ccid\":143,\"preset\":\"2\",\"name\":\"NPO 2\",\"onid\":1536,\"tsid\":2098,\"sid\":19402,\"serviceType\":\"audio_video\",\"type\":\"DVB_C\",\"logoVersion\":143}]}";
    static final String TEST_CHANNEL = "NPO 2";

    private void testPhilipsTVChannelParsing(String channel, Command command, String ccid) throws IOException {
        Map<String, String> availableTvChannels = Collections.emptyMap();

        TvChannelService tvChannelService = new TvChannelService(handler, connectionManager);
        tvChannelService.handleCommand(channel, command);
        ArgumentCaptor<Map<String, String>> captor = ArgumentCaptor.forClass(Map.class);
        verify(handler, atLeastOnce()).updateChannelStateDescription(ArgumentMatchers.eq(CHANNEL_TV_CHANNEL),
                captor.capture());
        availableTvChannels = captor.getValue();
        logger.info("Channels found: {}", availableTvChannels);
        assertEquals(2, availableTvChannels.size());
        assertEquals(TEST_CHANNEL, availableTvChannels.get(TEST_CHANNEL));

        ArgumentCaptor<String> captorSwitchChannel = ArgumentCaptor.forClass(String.class);
        verify(connectionManager, atLeastOnce()).doHttpsPost(any(), captorSwitchChannel.capture());
        String changeChannelJson = captorSwitchChannel.getValue();
        logger.info("Channel change command: {}", changeChannelJson);
        assertEquals("{\"channel\":{\"serviceType\":\"\",\"logoVersion\":0,\"ccid\":\"" + ccid
                + "\",\"name\":\"\",\"preset\":\"\",\"tsid\":0,\"type\":\"\",\"onid\":0,\"sid\":0},\"channelList\":{\"id\":\"allter\",\"version\":\"30\"}}",
                changeChannelJson);
    }

    @Test
    public void testNewerPhilipsTVChannelParsing() {
        try {
            // test compatibility with newer Philips TV's that provide the version as string
            when(connectionManager.doHttpsGet(any())).thenReturn(NEW_RESPONSE);
            testPhilipsTVChannelParsing(TEST_CHANNEL, new StringType(TEST_CHANNEL), "143");
        } catch (IOException e) {
            logger.warn("Test Failed with", e);
        }
    }

    @Test
    public void testOlderPhilipsTVChannelParsing() {
        try {
            // test compatibility with older Philips TV's that provide the version as int
            when(connectionManager.doHttpsGet(any())).thenReturn(OLD_RESPONSE);
            testPhilipsTVChannelParsing("NPO 1", new StringType("NPO 1"), "142");
        } catch (IOException e) {
            logger.warn("Test Failed with", e);
        }
    }
}
