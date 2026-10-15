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
package org.openhab.binding.shelly.internal.handler.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSWALLDISPLAY;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_MEDIA_PLAYORPAUSE;

import java.util.Map;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiRpc;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia.Shelly2DeviceStatusMediaPlayback;
import org.openhab.binding.shelly.internal.api2.dto.ShellyMediaJsonDTO.Shelly2DeviceStatusMedia.Shelly2DeviceStatusMediaPlayback.Shelly2DeviceStatusMediaMeta;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.UnDefType;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null", "unchecked" })
public class ShellyComponentMediaTest {

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    private ShellyThingInterface newHandler() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_SHELLYPLUSWALLDISPLAY, "test"));
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getThing()).thenReturn(thing);
        return handler;
    }

    private ShellySettingsStatus newStatus(@Nullable Shelly2DeviceStatusMediaMeta meta) {
        Shelly2DeviceStatusMediaPlayback playback = new Shelly2DeviceStatusMediaPlayback();
        playback.enable = true;
        playback.volume = 5;
        playback.mediaType = "RADIO";
        playback.mediaMeta = meta;
        ShellySettingsStatus status = new ShellySettingsStatus();
        status.media = new Shelly2DeviceStatusMedia();
        status.media.playback = playback;
        return status;
    }

    @Test
    void playbackCreatesChannelsAndPublishesState() {
        ShellyThingInterface handler = newHandler();
        Shelly2DeviceStatusMediaMeta meta = new Shelly2DeviceStatusMediaMeta();
        meta.title = "Title";
        meta.artist = "Artist";
        meta.album = "Album";

        ShellyComponentMedia.updateChannels(handler, newStatus(meta));

        ArgumentCaptor<Map<String, Channel>> channels = ArgumentCaptor.forClass(Map.class);
        verify(handler).updateThingChannels(eq(Map.of()), channels.capture());
        assertEquals(8, channels.getValue().size());
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_CONTROL, PlayPauseType.PLAY);
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_VOLUME, new PercentType(50));
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_TYPE, new StringType("RADIO"));
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_TITLE, new StringType("Title"));
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_ARTIST, new StringType("Artist"));
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_ALBUM, new StringType("Album"));
    }

    @Test
    void playbackWithoutMetaClearsMetadata() {
        ShellyThingInterface handler = newHandler();

        ShellyComponentMedia.updateChannels(handler, newStatus(null));

        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_TITLE, UnDefType.UNDEF);
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_ARTIST, UnDefType.UNDEF);
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_ALBUM, UnDefType.UNDEF);
    }

    @ParameterizedTest
    @MethodSource("provideStatusWithoutPlayback")
    void missingPlaybackRemovesChannels(ShellySettingsStatus status) {
        ShellyThingInterface handler = newHandler();

        ShellyComponentMedia.updateChannels(handler, status);

        verify(handler).removeChannels(argThat(ids -> ids.size() == 8));
        verify(handler, never()).updateThingChannels(any(), any());
    }

    private static Stream<Arguments> provideStatusWithoutPlayback() {
        ShellySettingsStatus withoutPlayback = new ShellySettingsStatus();
        withoutPlayback.media = new Shelly2DeviceStatusMedia();
        return Stream.of(Arguments.of(new ShellySettingsStatus()), Arguments.of(withoutPlayback));
    }

    @Test
    void playPauseOnlyTogglesWhenRequestedStateDiffers() throws Exception {
        Shelly2ApiRpc api = mock(Shelly2ApiRpc.class);
        ShellyThingInterface handler = newHandler();
        when(handler.getApi()).thenReturn(api);
        when(handler.getChannelValue(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_CONTROL)).thenReturn(PlayPauseType.PLAY);

        ShellyComponentMedia.handleCommand(handler, CHANNEL_MEDIA_CONTROL, PlayPauseType.PLAY);
        verify(api, never()).apiRequest(anyString(), any(), any());

        ShellyComponentMedia.handleCommand(handler, CHANNEL_MEDIA_CONTROL, PlayPauseType.PAUSE);
        verify(api).apiRequest(SHELLYRPC_METHOD_MEDIA_PLAYORPAUSE, null, String.class);
        verify(handler).updateChannel(CHANNEL_GROUP_MEDIA, CHANNEL_MEDIA_CONTROL, PlayPauseType.PAUSE);
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForPercentToVolume")
    void percentToVolumeRoundsToNearestDeviceStep(int percent, int expectedVolume) {
        assertEquals(expectedVolume, ShellyComponentMedia.percentToVolume(percent));
    }

    private static Stream<Arguments> provideTestCasesForPercentToVolume() {
        return Stream.of(Arguments.of(4, 0), Arguments.of(5, 1), Arguments.of(55, 6), Arguments.of(-5, 0),
                Arguments.of(120, 10));
    }

    @ParameterizedTest
    @MethodSource("provideTestCasesForVolumeStep")
    void volumeStepChangesTheDeviceVolumeInBothDirections(int volume) {
        int percent = ShellyComponentMedia.volumeToPercent(volume);

        assertEquals(volume + 1, ShellyComponentMedia
                .percentToVolume(ShellyComponentMedia.commandToVolumePercent(IncreaseDecreaseType.INCREASE, percent)));
        assertEquals(volume - 1, ShellyComponentMedia
                .percentToVolume(ShellyComponentMedia.commandToVolumePercent(IncreaseDecreaseType.DECREASE, percent)));
    }

    private static Stream<Arguments> provideTestCasesForVolumeStep() {
        return Stream.of(Arguments.of(1), Arguments.of(9));
    }
}
