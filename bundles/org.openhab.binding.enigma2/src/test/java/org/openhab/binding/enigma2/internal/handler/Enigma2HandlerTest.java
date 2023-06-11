/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.enigma2.internal.handler;

import static org.eclipse.jdt.annotation.Checks.requireNonNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.enigma2.internal.Enigma2BindingConstants;
import org.openhab.binding.enigma2.internal.Enigma2Client;
import org.openhab.binding.enigma2.internal.Enigma2Configuration;
import org.openhab.binding.enigma2.internal.Enigma2RemoteKey;
import org.openhab.binding.enigma2.internal.actions.Enigma2Actions;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.types.RefreshType;

/**
 * The {@link Enigma2HandlerTest} class is responsible for testing {@link Enigma2Handler}.
 *
 * @author Guido Dolfen - Initial contribution
 */
@SuppressWarnings({ "null", "unchecked" })
@NonNullByDefault
public class Enigma2HandlerTest {
    public static final String CHANNEL_UID_PREFIX = "enigma2:device:192_168_0_3:";
    public static final String SOME_TEXT = "some Text";
    @Nullable
    private Enigma2Handler enigma2Handler;
    @Nullable
    private Enigma2Client enigma2Client;
    @Nullable
    private Thing thing;
    @Nullable
    private Configuration configuration;
    @Nullable
    private ThingHandlerCallback callback;

    @BeforeEach
    public void setUp() {
        enigma2Client = mock(Enigma2Client.class);
        thing = mock(Thing.class);
        callback = mock(ThingHandlerCallback.class);
        configuration = mock(Configuration.class);
        when(thing.getConfiguration()).thenReturn(requireNonNull(configuration));
        when(configuration.as(Enigma2Configuration.class)).thenReturn(new Enigma2Configuration());
        enigma2Handler = spy(new Enigma2Handler(requireNonNull(thing)));
        enigma2Handler.setCallback(callback);
        when(enigma2Handler.getEnigma2Client()).thenReturn(Optional.of(requireNonNull(enigma2Client)));
    }

    @Test
    public void testSendRcCommand() {
        enigma2Handler.sendRcCommand("KEY_1");
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.KEY_1.getValue());
    }

    @Test
    public void testSendInfo() {
        enigma2Handler.sendInfo(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
        verify(enigma2Client).sendInfo(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendWarning() {
        enigma2Handler.sendWarning(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
        verify(enigma2Client).sendWarning(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendError() {
        enigma2Handler.sendError(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
        verify(enigma2Client).sendError(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testSendQuestion() {
        enigma2Handler.sendQuestion(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
        verify(enigma2Client).sendQuestion(Enigma2BindingConstants.MESSAGE_TIMEOUT, SOME_TEXT);
    }

    @Test
    public void testGetEnigma2Client() {
        enigma2Handler = new Enigma2Handler(requireNonNull(thing));
        assertThat(enigma2Handler.getEnigma2Client(), is(Optional.empty()));
    }

    @Test
    public void testGetServices() {
        enigma2Handler = new Enigma2Handler(requireNonNull(thing));
        assertThat(enigma2Handler.getServices(), contains(Enigma2Actions.class));
    }

    @Test
    public void testSendRcCommandUnsupported() {
        enigma2Handler.sendRcCommand("KEY_X");
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandPowerRefreshFalse() {
        when(enigma2Client.isPower()).thenReturn(false);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_POWER);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshPower();
        verify(callback).stateUpdated(channelUID, OnOffType.OFF);
    }

    @Test
    public void testHandleCommandPowerRefreshTrue() {
        when(enigma2Client.isPower()).thenReturn(true);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_POWER);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshPower();
        verify(callback).stateUpdated(channelUID, OnOffType.ON);
    }

    @Test
    public void testHandleCommandPowerOn() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_POWER);
        enigma2Handler.handleCommand(channelUID, OnOffType.ON);
        verify(enigma2Client).setPower(true);
    }

    @Test
    public void testHandleCommandPowerOff() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_POWER);
        enigma2Handler.handleCommand(channelUID, OnOffType.OFF);
        verify(enigma2Client).setPower(false);
    }

    @Test
    public void testHandleCommandPowerUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_POWER);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandChannelRefresh() {
        when(enigma2Client.getChannel()).thenReturn(SOME_TEXT);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_CHANNEL);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshChannel();
        verify(callback).stateUpdated(channelUID, new StringType(SOME_TEXT));
    }

    @Test
    public void testHandleCommandMuteRefreshFalse() {
        when(enigma2Client.isMute()).thenReturn(false);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MUTE);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshVolume();
        verify(callback).stateUpdated(channelUID, OnOffType.OFF);
    }

    @Test
    public void testHandleCommandMuteRefreshTrue() {
        when(enigma2Client.isMute()).thenReturn(true);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MUTE);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshVolume();
        verify(callback).stateUpdated(channelUID, OnOffType.ON);
    }

    @Test
    public void testHandleCommandMuteOn() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MUTE);
        enigma2Handler.handleCommand(channelUID, OnOffType.ON);
        verify(enigma2Client).setMute(true);
    }

    @Test
    public void testHandleCommandMuteOff() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MUTE);
        enigma2Handler.handleCommand(channelUID, OnOffType.OFF);
        verify(enigma2Client).setMute(false);
    }

    @Test
    public void testHandleCommandMuteUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MUTE);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandChannelString() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_CHANNEL);
        enigma2Handler.handleCommand(channelUID, new StringType(SOME_TEXT));
        verify(enigma2Client).setChannel(SOME_TEXT);
    }

    @Test
    public void testHandleCommandChannelUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_CHANNEL);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandMediaPlayerRefresh() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_PLAYER);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandMediaPlay() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_PLAYER);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PLAY);
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.PLAY.getValue());
    }

    @Test
    public void testHandleCommandMediaPause() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_PLAYER);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.PAUSE.getValue());
    }

    @Test
    public void testHandleCommandMediaNext() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_PLAYER);
        enigma2Handler.handleCommand(channelUID, NextPreviousType.NEXT);
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.FAST_FORWARD.getValue());
    }

    @Test
    public void testHandleCommandMediaPrevious() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_PLAYER);
        enigma2Handler.handleCommand(channelUID, NextPreviousType.PREVIOUS);
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.FAST_BACKWARD.getValue());
    }

    @Test
    public void testHandleCommandMediaPlayerUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_PLAYER);
        enigma2Handler.handleCommand(channelUID, OnOffType.ON);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandMediaStopRefresh() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_STOP);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandMediaStopOn() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_STOP);
        enigma2Handler.handleCommand(channelUID, OnOffType.ON);
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.STOP.getValue());
    }

    @Test
    public void testHandleCommandMediaStopOff() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_STOP);
        enigma2Handler.handleCommand(channelUID, OnOffType.OFF);
        verify(enigma2Client).sendRcCommand(Enigma2RemoteKey.STOP.getValue());
    }

    @Test
    public void testHandleCommandMediaStopUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_MEDIA_STOP);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandTitleUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_TITLE);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandTitleRefresh() {
        when(enigma2Client.getTitle()).thenReturn(SOME_TEXT);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_TITLE);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshEpg();
        verify(callback).stateUpdated(channelUID, new StringType(SOME_TEXT));
    }

    @Test
    public void testHandleCommandAnswerUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_ANSWER);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandAnswerRefresh() {
        when(enigma2Client.getAnswer()).thenReturn(SOME_TEXT);
        when(enigma2Client.getLastAnswerTime()).thenReturn(LocalDateTime.now().plus(1, ChronoUnit.SECONDS));
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_ANSWER);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshAnswer();
        verify(callback).stateUpdated(channelUID, new StringType(SOME_TEXT));
    }

    @Test
    public void testHandleCommandAnswerRefreshFalse() {
        when(enigma2Client.getAnswer()).thenReturn(SOME_TEXT);
        when(enigma2Client.getLastAnswerTime()).thenReturn(LocalDateTime.of(2020, 1, 1, 0, 0));
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_ANSWER);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshAnswer();
        verifyNoInteractions(callback);
    }

    @Test
    public void testHandleCommandDescriptionUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_DESCRIPTION);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }

    @Test
    public void testHandleCommandDescriptionRefresh() {
        when(enigma2Client.getDescription()).thenReturn(SOME_TEXT);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_DESCRIPTION);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshEpg();
        verify(callback).stateUpdated(channelUID, new StringType(SOME_TEXT));
    }

    @Test
    public void testHandleCommandVolumeRefresh() {
        when(enigma2Client.getVolume()).thenReturn(35);
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_VOLUME);
        enigma2Handler.handleCommand(channelUID, RefreshType.REFRESH);
        verify(enigma2Client).refreshVolume();
        verify(callback).stateUpdated(channelUID, new PercentType(35));
    }

    @Test
    public void testHandleCommandVolumePercent() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_VOLUME);
        enigma2Handler.handleCommand(channelUID, new PercentType(30));
        verify(enigma2Client).setVolume(30);
    }

    @Test
    public void testHandleCommandVolumeDecimal() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_VOLUME);
        enigma2Handler.handleCommand(channelUID, new DecimalType(40));
        verify(enigma2Client).setVolume(40);
    }

    @Test
    public void testHandleCommandVolumeUnsupported() {
        ChannelUID channelUID = new ChannelUID(CHANNEL_UID_PREFIX + Enigma2BindingConstants.CHANNEL_VOLUME);
        enigma2Handler.handleCommand(channelUID, PlayPauseType.PAUSE);
        verifyNoInteractions(enigma2Client);
    }
}
