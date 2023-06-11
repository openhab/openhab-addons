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
package org.openhab.binding.yamahareceiver.internal.protocol.xml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.yamahareceiver.internal.TestModels.*;
import static org.openhab.binding.yamahareceiver.internal.YamahaReceiverBindingConstants.Inputs.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.openhab.binding.yamahareceiver.internal.config.YamahaBridgeConfig;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoState;
import org.openhab.binding.yamahareceiver.internal.state.PlayInfoStateListener;

/**
 * Unit test for {@link InputWithPlayControlXML}.
 *
 * @author Tomasz Maruszak - Initial contribution
 */
public class InputWithPlayControlXMLTest extends AbstractZoneControlXMLTest {

    private InputWithPlayControlXML subject;

    private @Mock PlayInfoStateListener playInfoStateListener;
    private @Captor ArgumentCaptor<PlayInfoState> playInfoStateArg;
    private @Mock YamahaBridgeConfig bridgeConfig;

    private String albumUrl;

    private void given(String model, String input, Consumer<ModelContext> setup) throws Exception {
        ctx.prepareForModel(model);

        DeviceInformationXML deviceInformation = new DeviceInformationXML(con, deviceInformationState);
        deviceInformation.update();

        setup.accept(ctx);

        albumUrl = "http://some/url.jpg";
        when(bridgeConfig.getAlbumUrl()).thenReturn(albumUrl);

        subject = new InputWithPlayControlXML(input, con, playInfoStateListener, bridgeConfig, deviceInformationState);
    }

    @Test
    public void given_RX_S601D_and_Spotify_when_playStopPause_then_sendsProperCommand() throws Exception {
        given(RX_S601D, INPUT_SPOTIFY, ctx -> {
            ctx.respondWith("<Spotify><Play_Info>GetParam</Play_Info></Spotify>", "Spotify_Play_Info.xml");
        });

        // when
        subject.play();
        subject.stop();
        subject.pause();

        // then
        verify(con).send(eq("<Spotify><Play_Control><Playback>Play</Playback></Play_Control></Spotify>"));
        verify(con).send(eq("<Spotify><Play_Control><Playback>Stop</Playback></Play_Control></Spotify>"));
        verify(con).send(eq("<Spotify><Play_Control><Playback>Pause</Playback></Play_Control></Spotify>"));
    }

    @Test
    public void given_RX_S601D_and_Spotify_when_nextPrevious_then_sendsProperCommand() throws Exception {
        given(RX_S601D, INPUT_SPOTIFY, ctx -> {
            ctx.respondWith("<Spotify><Play_Info>GetParam</Play_Info></Spotify>", "Spotify_Play_Info.xml");
        });

        // when
        subject.nextTrack();
        subject.previousTrack();

        // then
        verify(con).send(eq("<Spotify><Play_Control><Playback>Skip Fwd</Playback></Play_Control></Spotify>"));
        verify(con).send(eq("<Spotify><Play_Control><Playback>Skip Rev</Playback></Play_Control></Spotify>"));
    }

    @Test
    public void given_RX_S601D_and_Bluetooth_when_playStopPause_then_sendsProperCommand() throws Exception {
        given(RX_S601D, INPUT_BLUETOOTH, ctx -> {
            ctx.respondWith("<Bluetooth><Play_Info>GetParam</Play_Info></Bluetooth>", "Bluetooth_Play_Info.xml");
        });

        // when
        subject.play();
        subject.stop();
        subject.pause();

        // then
        verify(con).send(eq("<Bluetooth><Play_Control><Playback>Play</Playback></Play_Control></Bluetooth>"));
        verify(con).send(eq("<Bluetooth><Play_Control><Playback>Stop</Playback></Play_Control></Bluetooth>"));
        verify(con).send(eq("<Bluetooth><Play_Control><Playback>Pause</Playback></Play_Control></Bluetooth>"));
    }

    @Test
    public void given_RX_S601D_and_Bluetooth_when_nextPrevious_then_sendsProperCommand() throws Exception {
        given(RX_S601D, INPUT_BLUETOOTH, ctx -> {
            ctx.respondWith("<Bluetooth><Play_Info>GetParam</Play_Info></Bluetooth>", "Bluetooth_Play_Info.xml");
        });

        // when
        subject.nextTrack();
        subject.previousTrack();

        // then
        verify(con).send(eq("<Bluetooth><Play_Control><Playback>Skip Fwd</Playback></Play_Control></Bluetooth>"));
        verify(con).send(eq("<Bluetooth><Play_Control><Playback>Skip Rev</Playback></Play_Control></Bluetooth>"));
    }

    @Test
    public void given_RX_S601D_and_NET_RADIO_when_nextPrevious_then_sendsProperCommand() throws Exception {
        given(RX_S601D, INPUT_NET_RADIO, ctx -> {
            ctx.respondWith("<NET_RADIO><Play_Info>GetParam</Play_Info></NET_RADIO>", "NET_RADIO_Play_Info.xml");
        });

        // when
        subject.nextTrack();
        subject.previousTrack();

        // then
        verify(con).send(eq("<NET_RADIO><Play_Control><Playback>Skip Fwd</Playback></Play_Control></NET_RADIO>"));
        verify(con).send(eq("<NET_RADIO><Play_Control><Playback>Skip Rev</Playback></Play_Control></NET_RADIO>"));
    }

    @Test
    public void given_RX_S601D_and_Spotify_when_update_then_stateIsProperlyRead() throws Exception {
        given(RX_S601D, INPUT_SPOTIFY, ctx -> {
            ctx.respondWith("<Spotify><Play_Info>GetParam</Play_Info></Spotify>", "Spotify_Play_Info.xml");
        });

        ArgumentCaptor<PlayInfoState> playInfoStateArg = ArgumentCaptor.forClass(PlayInfoState.class);

        // when
        subject.update();

        // then
        verify(playInfoStateListener).playInfoUpdated(playInfoStateArg.capture());
        PlayInfoState state = playInfoStateArg.getValue();

        assertEquals("Play", state.playbackMode);
        assertEquals("Above & Beyond", state.artist);
        assertEquals("Acoustic - Live At The Hollywood Bowl", state.album);
        assertEquals("No One On Earth - Live At The Hollywood Bowl", state.song);
        assertEquals("N/A", state.station);
        assertEquals("http://localhost/YamahaRemoteControl/AlbumART/AlbumART6585.jpg", state.songImageUrl);
    }

    @Test
    public void given_RX_S601D_and_NET_RADIO_when_update_then_stateIsProperlyRead() throws Exception {
        given(RX_S601D, INPUT_NET_RADIO, ctx -> {
            ctx.respondWith("<NET_RADIO><Play_Info>GetParam</Play_Info></NET_RADIO>", "NET_RADIO_Play_Info.xml");
        });

        // when
        subject.update();

        // then
        verify(playInfoStateListener).playInfoUpdated(playInfoStateArg.capture());
        PlayInfoState state = playInfoStateArg.getValue();

        assertEquals("Play", state.playbackMode);
        assertEquals("N/A", state.artist);
        assertEquals("Chilli ZET PL", state.station);
        assertEquals("", state.album);
        assertEquals("LESZEK MOZDZER - ZDROWY KOLATAJ", state.song);
        assertEquals("http://localhost/YamahaRemoteControl/AlbumART/AlbumART4626.jpg", state.songImageUrl);
    }

    @Test
    public void given_RX_S601D_and_Bluetooth_when_update_then_stateIsProperlyRead() throws Exception {
        given(RX_S601D, INPUT_BLUETOOTH, ctx -> {
            ctx.respondWith("<Bluetooth><Play_Info>GetParam</Play_Info></Bluetooth>", "Bluetooth_Play_Info.xml");
        });

        ArgumentCaptor<PlayInfoState> playInfoStateArg = ArgumentCaptor.forClass(PlayInfoState.class);

        // when
        subject.update();

        // then
        verify(playInfoStateListener).playInfoUpdated(playInfoStateArg.capture());
        PlayInfoState state = playInfoStateArg.getValue();

        assertEquals("Play", state.playbackMode);
        assertEquals("M.I.K.E.", state.artist);
        assertEquals("A State Of Trance Classics, Vol. 12 (The Full Unmixed Versions)", state.album);
        assertEquals("Voices From The Inside", state.song);
        assertEquals("N/A", state.station);
        assertEquals(albumUrl, state.songImageUrl);
    }

    @Test
    public void given_RX_V3900_and_NET_RADIO_when_playStopPause_then_sendsProperCommand() throws Exception {
        given(RX_V3900, INPUT_NET_RADIO, ctx -> {
            ctx.respondWith("<NET_USB><Play_Info>GetParam</Play_Info></NET_USB>", "NET_USB_Play_Info.xml");
        });

        // when
        subject.play();
        subject.stop();
        subject.pause();

        // then
        verify(con).send(eq("<NET_USB><Play_Control><Play>Play</Play></Play_Control></NET_USB>"));
        verify(con).send(eq("<NET_USB><Play_Control><Play>Stop</Play></Play_Control></NET_USB>"));
        verify(con).send(eq("<NET_USB><Play_Control><Play>Pause</Play></Play_Control></NET_USB>"));
    }

    @Test
    public void given_RX_V3900_and_NET_RADIO_when_nextPrevious_then_sendsProperCommand() throws Exception {
        given(RX_V3900, INPUT_NET_RADIO, ctx -> {
            ctx.respondWith("<NET_USB><Play_Info>GetParam</Play_Info></NET_USB>", "NET_USB_Play_Info.xml");
        });

        // when
        subject.nextTrack();
        subject.previousTrack();

        // then
        verify(con).send(eq("<NET_USB><Play_Control><Skip>Fwd</Skip></Play_Control></NET_USB>"));
        verify(con).send(eq("<NET_USB><Play_Control><Skip>Rev</Skip></Play_Control></NET_USB>"));
    }

    @Test
    public void given_RX_V3900_and_NET_RADIO_when_update_then_stateIsProperlyRead() throws Exception {
        given(RX_V3900, INPUT_NET_RADIO, ctx -> {
            ctx.respondWith("<NET_USB><Play_Info>GetParam</Play_Info></NET_USB>", "NET_USB_Play_Info.xml");
        });

        // when
        subject.update();

        // then
        verify(playInfoStateListener).playInfoUpdated(playInfoStateArg.capture());
        PlayInfoState state = playInfoStateArg.getValue();

        assertEquals("Play", state.playbackMode);
        assertEquals("Some Artist", state.artist);
        assertEquals("Some Album", state.album);
        assertEquals("SuomiPOP 98.1", state.song);
        assertEquals("N/A", state.station);
        assertEquals(albumUrl, state.songImageUrl);
    }

    @Test
    public void given_RX_V3900_and_TUNER_when_update_then_stateIsProperlyRead() throws Exception {
        given(RX_V3900, INPUT_TUNER, ctx -> {
            ctx.respondWith("<Tuner><Play_Info>GetParam</Play_Info></Tuner>", "Tuner_Play_Info.xml");
        });

        // when
        subject.update();

        // then
        verify(playInfoStateListener).playInfoUpdated(playInfoStateArg.capture());
        PlayInfoState state = playInfoStateArg.getValue();

        assertEquals("Stop", state.playbackMode);
        assertEquals("", state.artist);
        assertEquals("POP_M", state.album);
        assertEquals("", state.song);
        assertEquals("SUOMIPOP", state.station);
        assertEquals(albumUrl, state.songImageUrl);
    }
}
