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
package org.openhab.binding.atv.internal.handler;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.atv.internal.AtvBindingConstants.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.AppleTV;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Tests for the pure command-mapping and state-mapping logic in {@link AtvHandler}.
 *
 * <p>
 * Only the reachable, socket-free logic is exercised: the private {@code appleTV} field is
 * injected with a Mockito mock so {@link AtvHandler#handleCommand} can be driven directly, and the
 * private mapping helpers are reached via reflection.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class AtvHandlerTest {

    private @NonNullByDefault({}) AtvHandler handler;
    private @NonNullByDefault({}) AppleTV appleTV;
    private @NonNullByDefault({}) RemoteControl remoteControl;
    private @NonNullByDefault({}) Power power;
    private @NonNullByDefault({}) Audio audio;
    private @NonNullByDefault({}) ThingUID thingUID;

    @BeforeEach
    public void setUp() throws Exception {
        Thing thing = mock(Thing.class);
        thingUID = new ThingUID(THING_TYPE_APPLETV, "test");
        when(thing.getUID()).thenReturn(thingUID);
        when(thing.getThingTypeUID()).thenReturn(THING_TYPE_APPLETV);

        handler = new AtvHandler(thing, null);

        appleTV = mock(AppleTV.class);
        remoteControl = mock(RemoteControl.class);
        power = mock(Power.class);
        audio = mock(Audio.class);
        when(appleTV.remoteControl()).thenReturn(remoteControl);
        when(appleTV.power()).thenReturn(power);
        when(appleTV.audio()).thenReturn(audio);

        injectAppleTV(appleTV);
    }

    private void injectAppleTV(@org.eclipse.jdt.annotation.Nullable AppleTV atv) throws Exception {
        Field field = AtvHandler.class.getDeclaredField("appleTV");
        field.setAccessible(true);
        field.set(handler, atv);
    }

    private ChannelUID channel(String id) {
        return new ChannelUID(thingUID, id);
    }

    @Test
    public void testRemoteKeyUp() {
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("up"));
        verify(remoteControl).up();
    }

    @Test
    public void testRemoteKeyIsCaseInsensitive() {
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("MENU"));
        verify(remoteControl).menu();
    }

    @Test
    public void testRemoteKeyMixedCaseCompound() {
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("PlayPause"));
        verify(remoteControl).playPause();
    }

    @Test
    public void testRemoteKeySelect() {
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("select"));
        verify(remoteControl).select();
    }

    @Test
    public void testUnknownRemoteKeyIsIgnored() {
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("bogus"));
        verifyNoInteractions(remoteControl);
    }

    @Test
    public void testMediaControlPlay() {
        handler.handleCommand(channel(CHANNEL_MEDIA_CONTROL), PlayPauseType.PLAY);
        verify(remoteControl).play();
    }

    @Test
    public void testMediaControlPause() {
        handler.handleCommand(channel(CHANNEL_MEDIA_CONTROL), PlayPauseType.PAUSE);
        verify(remoteControl).pause();
    }

    @Test
    public void testMediaControlNext() {
        handler.handleCommand(channel(CHANNEL_MEDIA_CONTROL), NextPreviousType.NEXT);
        verify(remoteControl).next();
    }

    @Test
    public void testMediaControlPrevious() {
        handler.handleCommand(channel(CHANNEL_MEDIA_CONTROL), NextPreviousType.PREVIOUS);
        verify(remoteControl).previous();
    }

    @Test
    public void testMediaControlFastForward() {
        handler.handleCommand(channel(CHANNEL_MEDIA_CONTROL), RewindFastforwardType.FASTFORWARD);
        verify(remoteControl).skipForward();
    }

    @Test
    public void testMediaControlRewind() {
        handler.handleCommand(channel(CHANNEL_MEDIA_CONTROL), RewindFastforwardType.REWIND);
        verify(remoteControl).skipBackward();
    }

    @Test
    public void testPowerOn() {
        handler.handleCommand(channel(CHANNEL_POWER), OnOffType.ON);
        verify(power).turnOn();
    }

    @Test
    public void testPowerOff() {
        handler.handleCommand(channel(CHANNEL_POWER), OnOffType.OFF);
        verify(power).turnOff();
    }

    @Test
    public void testVolumePercent() {
        handler.handleCommand(channel(CHANNEL_VOLUME), new PercentType(42));
        verify(audio).setVolume(42.0);
    }

    @Test
    public void testVolumeOnOffMapsToFullOrMute() {
        handler.handleCommand(channel(CHANNEL_VOLUME), OnOffType.ON);
        verify(audio).setVolume(100.0);
        handler.handleCommand(channel(CHANNEL_VOLUME), OnOffType.OFF);
        verify(audio).setVolume(0.0);
    }

    @Test
    public void testCommandIgnoredWhenNotConnected() throws Exception {
        injectAppleTV(null);
        // must not throw and must not touch the (now detached) device mock
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("up"));
        verifyNoInteractions(remoteControl);
    }

    @Test
    public void testPowerToStateMapping() throws Exception {
        assertThat(powerToState(PowerState.On), is(OnOffType.ON));
        assertThat(powerToState(PowerState.Off), is(OnOffType.OFF));
        assertThat(powerToState(PowerState.Unknown), is(UnDefType.UNDEF));
    }

    private State powerToState(PowerState state) throws Exception {
        Method method = AtvHandler.class.getDeclaredMethod("powerToState", PowerState.class);
        method.setAccessible(true);
        return Objects.requireNonNull((State) method.invoke(handler, state));
    }
}
