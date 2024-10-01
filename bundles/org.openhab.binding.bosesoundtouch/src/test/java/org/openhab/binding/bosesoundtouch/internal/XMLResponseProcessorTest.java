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
package org.openhab.binding.bosesoundtouch.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_ALBUM;
import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_ARTIST;
import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_GENRE;
import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_STATIONLOCATION;
import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_STATIONNAME;
import static org.openhab.binding.bosesoundtouch.internal.BoseSoundTouchBindingConstants.CHANNEL_NOWPLAYING_TRACK;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.bosesoundtouch.internal.handler.BoseSoundTouchHandler;
import org.openhab.core.library.types.StringType;
import org.xml.sax.SAXException;

/**
 * Unit tests for {@link XMLResponseProcessor}.
 * 
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class XMLResponseProcessorTest {

    private @NonNullByDefault({}) XMLResponseProcessor fixture;
    private @NonNullByDefault({}) BoseSoundTouchHandler handler;

    @BeforeEach
    protected void setUp() throws Exception {
        handler = mock(BoseSoundTouchHandler.class);
        when(handler.getMacAddress()).thenReturn("5065834D198B");

        CommandExecutor commandExecutor = mock(CommandExecutor.class);
        when(handler.getCommandExecutor()).thenReturn(commandExecutor);

        fixture = new XMLResponseProcessor(handler);
    }

    @Test
    void testParseNowPlayingUpdate() throws SAXException, IOException, ParserConfigurationException {
        String updateXML = Files.readString(new File("src/test/resources/NowPlayingUpdate.xml").toPath());

        fixture.handleMessage(updateXML);

        verify(handler).updateState(CHANNEL_NOWPLAYING_ALBUM, new StringType("\"Appetite for Destruction\""));
        verify(handler).updateState(CHANNEL_NOWPLAYING_ARTIST, new StringType("Guns N' Roses"));
        verify(handler).updateState(CHANNEL_NOWPLAYING_TRACK, new StringType("Sweet Child O' Mine"));
        verify(handler).updateState(CHANNEL_NOWPLAYING_GENRE, new StringType("Rock 'n' Roll"));
        verify(handler).updateState(CHANNEL_NOWPLAYING_STATIONNAME, new StringType("Jammin'"));
        verify(handler).updateState(CHANNEL_NOWPLAYING_STATIONLOCATION, new StringType("All o'er the world"));
    }
}
