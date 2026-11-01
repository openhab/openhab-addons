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

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;
import org.openhab.binding.atv.internal.AtvDynamicCommandDescriptionProvider;
import org.openhab.binding.atv.internal.AtvDynamicStateDescriptionProvider;
import org.openhab.binding.atv.internal.client.AppleTV;
import org.openhab.binding.atv.internal.client.capability.Audio;
import org.openhab.binding.atv.internal.client.capability.Power;
import org.openhab.binding.atv.internal.client.capability.RemoteControl;
import org.openhab.binding.atv.internal.client.conf.AtvConfig;
import org.openhab.binding.atv.internal.client.conf.Service;
import org.openhab.binding.atv.internal.client.dto.PowerState;
import org.openhab.binding.atv.internal.client.dto.Protocol;
import org.openhab.binding.atv.internal.config.AtvConfiguration;
import org.openhab.core.OpenHAB;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.NextPreviousType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.PlayPauseType;
import org.openhab.core.library.types.RewindFastforwardType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
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

        handler = new AtvHandler(thing, null, mock(AtvDynamicStateDescriptionProvider.class),
                mock(AtvDynamicCommandDescriptionProvider.class), null, mock(HttpClient.class));

        // The handler creates its dedicated connect executor in initialize(), which these tests skip;
        // inject a mock so the reconnect-scheduling paths behave as they would at runtime.
        ScheduledExecutorService connectExecutor = mock(ScheduledExecutorService.class);
        lenient().when(connectExecutor.schedule(Mockito.<Runnable> any(), Mockito.anyLong(), Mockito.any()))
                .thenReturn(mock(ScheduledFuture.class));
        Field executorField = AtvHandler.class.getDeclaredField("connectExecutor");
        executorField.setAccessible(true);
        executorField.set(handler, connectExecutor);

        appleTV = mock(AppleTV.class);
        remoteControl = mock(RemoteControl.class);
        power = mock(Power.class);
        audio = mock(Audio.class);
        when(appleTV.remoteControl()).thenReturn(remoteControl);
        when(appleTV.power()).thenReturn(power);
        when(appleTV.audio()).thenReturn(audio);

        injectAppleTV(appleTV);
    }

    @SuppressWarnings("unchecked")
    private void injectAppleTV(@org.eclipse.jdt.annotation.Nullable AppleTV atv) throws Exception {
        Field field = AtvHandler.class.getDeclaredField("appleTV");
        field.setAccessible(true);
        ((AtomicReference<@org.eclipse.jdt.annotation.Nullable AppleTV>) field.get(handler)).set(atv);
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
    public void testPowerOnWhileOfflineRequestsWake() throws Exception {
        injectAppleTV(null);
        handler.handleCommand(channel(CHANNEL_POWER), OnOffType.ON);
        try {
            assertThat(supervisor().isWaking(), is(true));
            assertThat(field("reconnectJob"), is(notNullValue()));
        } finally {
            cancelReconnectJob();
        }
    }

    @Test
    public void testPowerOffWhileOfflineDoesNotRequestWake() throws Exception {
        injectAppleTV(null);
        handler.handleCommand(channel(CHANNEL_POWER), OnOffType.OFF);
        assertThat(supervisor().isWaking(), is(false));
        assertThat(field("reconnectJob"), is(nullValue()));
    }

    @Test
    public void testNonPowerCommandWhileOfflineDoesNotRequestWake() throws Exception {
        injectAppleTV(null);
        handler.handleCommand(channel(CHANNEL_REMOTE_KEY), new StringType("up"));
        assertThat(supervisor().isWaking(), is(false));
        assertThat(field("reconnectJob"), is(nullValue()));
    }

    @Test
    public void testDegradedConnectionDetection() throws Exception {
        when(appleTV.connectedProtocols()).thenReturn(Set.of(Protocol.Companion));
        assertThat(isDegradedConnection(), is(true));

        when(appleTV.connectedProtocols()).thenReturn(Set.of(Protocol.Companion, Protocol.MRP));
        assertThat(isDegradedConnection(), is(false));

        // AirPlay and RAOP register without connecting anything, so on an Apple TV their presence alone
        // still leaves the connection without metadata, push updates or remote control.
        when(appleTV.connectedProtocols()).thenReturn(Set.of(Protocol.AirPlay, Protocol.RAOP));
        assertThat(isDegradedConnection(), is(true));
    }

    @Test
    public void testSpeakerConnectionIsNeverDegraded() throws Exception {
        Thing speaker = mock(Thing.class);
        when(speaker.getUID()).thenReturn(new ThingUID(THING_TYPE_SPEAKER, "speaker"));
        when(speaker.getThingTypeUID()).thenReturn(THING_TYPE_SPEAKER);
        AtvHandler speakerHandler = new AtvHandler(speaker, null, mock(AtvDynamicStateDescriptionProvider.class),
                mock(AtvDynamicCommandDescriptionProvider.class), null, mock(HttpClient.class));
        Field field = AtvHandler.class.getDeclaredField("appleTV");
        field.setAccessible(true);
        @SuppressWarnings("unchecked")
        AtomicReference<@org.eclipse.jdt.annotation.Nullable AppleTV> reference = (AtomicReference<@org.eclipse.jdt.annotation.Nullable AppleTV>) field
                .get(speakerHandler);
        reference.set(appleTV);
        when(appleTV.connectedProtocols()).thenReturn(Set.of(Protocol.AirPlay, Protocol.RAOP));

        Method method = AtvHandler.class.getDeclaredMethod("isDegradedConnection");
        method.setAccessible(true);
        assertThat(method.invoke(speakerHandler), is(false));
    }

    @Test
    public void testDegradedConnectionFalseWhenNotConnected() throws Exception {
        injectAppleTV(null);
        assertThat(isDegradedConnection(), is(false));
    }

    @Test
    public void testPowerToStateMapping() throws Exception {
        assertThat(powerToState(PowerState.On), is(OnOffType.ON));
        assertThat(powerToState(PowerState.Off), is(OnOffType.OFF));
        assertThat(powerToState(PowerState.Unknown), is(UnDefType.UNDEF));
    }

    @Test
    public void invalidShuffleValueDoesNotThrow() {
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_SHUFFLE), new StringType("Sideways"));
        verifyNoInteractions(remoteControl);
    }

    @Test
    public void commandRejectedByDeviceDoesNotThrow() {
        doThrow(new IllegalStateException("not now")).when(remoteControl).play();
        handler.handleCommand(new ChannelUID(thingUID, CHANNEL_MEDIA_CONTROL), PlayPauseType.PLAY);
        verify(remoteControl).play();
    }

    @Test
    public void mediaSourceAcceptsHttpUrls() {
        assertThat(handler.resolveMediaSource(" https://example.com/a.mp3 "),
                is(Optional.of("https://example.com/a.mp3")));
        assertThat(handler.resolveMediaSource("HTTP://example.com/a.mp3"), is(Optional.of("HTTP://example.com/a.mp3")));
    }

    @Test
    public void mediaSourceResolvesSoundsFolderFiles(@TempDir Path conf) throws IOException {
        Path sounds = Files.createDirectories(conf.resolve("sounds"));
        Path chime = Files.writeString(sounds.resolve("chime.wav"), "x");
        Files.writeString(conf.resolve("secret.txt"), "x");
        System.setProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT, conf.toString());

        assertThat(handler.resolveMediaSource("chime.wav"), is(Optional.of(chime.toAbsolutePath().toString())));
        assertThat(handler.resolveMediaSource("missing.wav"), is(Optional.empty()));
        assertThat(handler.resolveMediaSource("../secret.txt"), is(Optional.empty()));
        assertThat(handler.resolveMediaSource(".."), is(Optional.empty()));
        assertThat(handler.resolveMediaSource(chime.toString()), is(Optional.empty()));
        assertThat(handler.resolveMediaSource("file:///etc/passwd"), is(Optional.empty()));
        assertThat(handler.resolveMediaSource("/etc/passwd"), is(Optional.empty()));
    }

    @Test
    public void ownDeviceMatchesAnyIdentifierIgnoringCase() throws Exception {
        configure("192.168.1.10", "6e:18:62:3e:d2:29");
        AtvConfig device = device("6E:18:62:3E:D2:29", "6E18623ED229");
        assertThat(handler.ownDevice(List.of(device)), is(device));
    }

    @Test
    public void ownDeviceRejectsAnotherDevice() throws Exception {
        configure("192.168.1.10", "c8:d0:83:da:35:37");
        assertThat(handler.ownDevice(List.of(device("6E:18:62:3E:D2:29", "6E18623ED229"))), is(nullValue()));
    }

    @Test
    public void notFoundNamesTheDeviceAtTheHost() throws Exception {
        configure("192.168.1.10", "c8:d0:83:da:35:37");
        assertThat(handler.notFoundMessage(List.of(device("6E:18:62:3E:D2:29", "6E18623ED229"))),
                is("@text/offline.wrong-device [\"6E:18:62:3E:D2:29\", \"192.168.1.10\", \"c8:d0:83:da:35:37\"]"));
    }

    @Test
    public void notFoundIsPlainWhenNothingAnswers() throws Exception {
        configure("192.168.1.10", "c8:d0:83:da:35:37");
        assertThat(handler.notFoundMessage(List.of()), is("@text/offline.not-found"));
        configure("", "c8:d0:83:da:35:37");
        assertThat(handler.notFoundMessage(List.of(device("6E:18:62:3E:D2:29", "6E18623ED229"))),
                is("@text/offline.not-found"));
    }

    @Test
    public void legacyDeviceIdIsMoved() {
        Thing legacy = thingWith(Map.of(CONFIG_LEGACY_DEVICE_ID, "AA:5D:F4:05:37:04"));
        handlerFor(legacy).migrateLegacyDeviceId();
        assertThat(legacy.getConfiguration().get(CONFIG_DEVICE_ID), is("AA:5D:F4:05:37:04"));
        assertThat(legacy.getConfiguration().containsKey(CONFIG_LEGACY_DEVICE_ID), is(false));
    }

    @Test
    public void currentDeviceIdIsKept() {
        Thing current = thingWith(
                Map.of(CONFIG_DEVICE_ID, "6E:18:62:3E:D2:29", CONFIG_LEGACY_DEVICE_ID, "c8:d0:83:da:35:37"));
        handlerFor(current).migrateLegacyDeviceId();
        assertThat(current.getConfiguration().get(CONFIG_DEVICE_ID), is("6E:18:62:3E:D2:29"));
    }

    @AfterEach
    public void clearConfigDir() {
        System.clearProperty(OpenHAB.CONFIG_DIR_PROG_ARGUMENT);
    }

    private static Thing thingWith(Map<String, Object> configuration) {
        return ThingBuilder.create(THING_TYPE_APPLETV, "legacy").withConfiguration(new Configuration(configuration))
                .build();
    }

    private static AtvHandler handlerFor(Thing thing) {
        AtvHandler handler = new AtvHandler(thing, null, mock(AtvDynamicStateDescriptionProvider.class),
                mock(AtvDynamicCommandDescriptionProvider.class), null, mock(HttpClient.class));
        handler.setCallback(mock(ThingHandlerCallback.class));
        return handler;
    }

    private void configure(String host, String deviceId) throws Exception {
        AtvConfiguration config = new AtvConfiguration();
        config.host = host;
        config.deviceId = deviceId;
        Field field = AtvHandler.class.getDeclaredField("config");
        field.setAccessible(true);
        field.set(handler, config);
    }

    private static AtvConfig device(String airplayId, String raopId) throws Exception {
        AtvConfig device = new AtvConfig(InetAddress.getByName("192.168.1.10"), "Living Room");
        device.addService(new Service(raopId, Protocol.RAOP, 7000, Map.of()));
        device.addService(new Service(airplayId, Protocol.AirPlay, 7000, Map.of()));
        return device;
    }

    private State powerToState(PowerState state) throws Exception {
        Method method = AtvHandler.class.getDeclaredMethod("powerToState", PowerState.class);
        method.setAccessible(true);
        return Objects.requireNonNull((State) method.invoke(handler, state));
    }

    private boolean isDegradedConnection() throws Exception {
        Method method = AtvHandler.class.getDeclaredMethod("isDegradedConnection");
        method.setAccessible(true);
        return (boolean) Objects.requireNonNull(method.invoke(handler));
    }

    private ConnectionSupervisor supervisor() throws Exception {
        return (ConnectionSupervisor) Objects.requireNonNull(field("supervisor"));
    }

    private @org.eclipse.jdt.annotation.Nullable Object field(String name) throws Exception {
        Field field = AtvHandler.class.getDeclaredField(name);
        field.setAccessible(true);
        return field.get(handler);
    }

    private void cancelReconnectJob() throws Exception {
        Object job = field("reconnectJob");
        if (job instanceof ScheduledFuture) {
            ((ScheduledFuture<?>) job).cancel(true);
        }
    }
}
