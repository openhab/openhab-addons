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
package org.openhab.binding.tibber.internal.pulse.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.tibber.internal.TibberPulseBindingConstants.*;
import static org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.*;
import static org.openhab.binding.tibber.internal.pulse.sml.SmlFrameBuilder.*;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.openhab.binding.tibber.internal.pulse.handler.BridgeHandler.StreamConnection;
import org.openhab.binding.tibber.internal.pulse.handler.HandlerSupport.Recorder;
import org.openhab.binding.tibber.internal.pulse.sml.SmlFrameBuilder;
import org.openhab.binding.tibber.internal.pulse.stream.StreamFailure;
import org.openhab.binding.tibber.internal.pulse.stream.StreamFailure.Kind;
import org.openhab.binding.tibber.internal.pulse.stream.StreamListener;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.internal.BridgeImpl;
import org.openhab.core.types.UnDefType;

/**
 * Tests the bridge handler against a double of the stream client: configuration validation, the status mapping of
 * design section 4.6, the host rule, routing of readings by device id, disposal, and the notification for discovery.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class BridgeHandlerTest {

    private static final long WAIT_MILLIS = 10_000;

    /** A stream client double that only records and lets the test play the callbacks. */
    private static final class FakeStream implements StreamConnection {
        final String host;
        final String password;
        final String threadName;
        final StreamListener listener;
        int starts;
        int stops;

        FakeStream(String host, String password, String threadName, StreamListener listener) {
            this.host = host;
            this.password = password;
            this.threadName = threadName;
            this.listener = listener;
        }

        @Override
        public void start() {
            starts++;
        }

        @Override
        public void stop() {
            stops++;
        }
    }

    private final List<FakeStream> streams = new CopyOnWriteArrayList<>();
    private final List<BridgeHandler> handlers = new ArrayList<>();
    private final List<MeterHandler> meters = new ArrayList<>();

    @AfterEach
    void tearDown() {
        meters.forEach(MeterHandler::dispose);
        handlers.forEach(BridgeHandler::dispose);
    }

    private BridgeHandler newHandler(Bridge bridge, Recorder recorder) {
        BridgeHandler handler = new BridgeHandler(bridge, (host, password, name, listener) -> {
            FakeStream stream = new FakeStream(host, password, name, listener);
            streams.add(stream);
            return stream;
        });
        handler.setCallback(recorder.callback);
        bridge.setHandler(handler);
        handlers.add(handler);
        return handler;
    }

    private final class Running {
        final Bridge bridge;
        final Recorder recorder;
        final BridgeHandler handler;

        Running(String id, String hostname, String password) {
            bridge = bridge(id, hostname, password);
            recorder = new Recorder(bridge, null);
            handler = newHandler(bridge, recorder);
            handler.initialize();
        }

        FakeStream stream() {
            return streams.get(streams.size() - 1);
        }

        StreamListener listener() {
            return stream().listener;
        }

        void assertStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            assertEquals(status, bridge.getStatus());
            assertEquals(detail, bridge.getStatusInfo().getStatusDetail());
            assertEquals(description, bridge.getStatusInfo().getDescription());
        }

        /** Adds an initialized meter thing for the device, as the framework would after the bridge is online. */
        MeterHandler addMeter(String id, String deviceId) {
            Thing thing = meter(bridge, id, deviceId, null);
            Recorder meterRecorder = new Recorder(thing, bridge);
            MeterHandler meterHandler = new MeterHandler(thing);
            meterHandler.setCallback(meterRecorder.callback);
            thing.setHandler(meterHandler);
            ((BridgeImpl) bridge).addThing(thing);
            meterRecorders.put(meterHandler, meterRecorder);
            meters.add(meterHandler);
            meterHandler.initialize();
            return meterHandler;
        }
    }

    private final java.util.Map<MeterHandler, Recorder> meterRecorders = new java.util.IdentityHashMap<>();

    private Recorder recorderOf(MeterHandler meter) {
        Recorder recorder = meterRecorders.get(meter);
        assertNotNull(recorder);
        return recorder;
    }

    private static byte[] validFrame(long watts) {
        return new SmlFrameBuilder().signed(POWER, UNIT_W, 0, watts, 8)
                .unsigned(ENERGY_IMPORT, UNIT_WH, -1, BigInteger.valueOf(12345678), 8)
                .octets(SmlFrameBuilder.METER_ID, new byte[] { 0x0a, 0x01, 0x45 }).build();
    }

    // thing-lifecycle: Configuration parameters

    @ParameterizedTest
    @ValueSource(strings = { "", "   ", "http://192.168.1.x/ws", "bridge.example/ws", "a b",
            "bridge\r\nX-Injected: 1" })
    void whenTheHostnameIsInvalidThenTheBridgeIsOfflineWithConfigurationErrorAndNoStreamIsOpened(String hostname) {
        Running r = new Running("b1", hostname, PASSWORD);

        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, TEXT_INVALID_HOSTNAME);
        assertTrue(streams.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = { "bridge.example", "bridge-a1b2c3d4.local", "192.168.1.20", "::1", "2001:db8::10",
            "tibber_bridge", "bridge.example:80" })
    void whenTheHostnameIsAHostNameOrAddressThenTheBridgeStartsConnecting(String hostname) {
        Running r = new Running("b1", hostname, PASSWORD);

        r.assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
        assertEquals(1, streams.size());
        assertEquals(hostname, r.stream().host);
    }

    @Test
    void whenThePasswordIsEmptyThenTheBridgeIsOfflineWithConfigurationErrorAndNoStreamIsOpened() {
        Running r = new Running("b1", "bridge.example", "");

        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, TEXT_EMPTY_PASSWORD);
        assertTrue(streams.isEmpty());
    }

    // discovery: Entry accepted without a password (the two forms of hostname that bridge discovery offers)

    @ParameterizedTest
    @ValueSource(strings = { "192.0.2.20", "Bridge01234567.local" })
    void whenAnEntryIsAcceptedWithoutAPasswordThenTheBridgeIsOfflineWithConfigurationErrorNamingThePassword(
            String offeredHostname) {
        Running r = new Running("bridge01234567", offeredHostname, "");

        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, TEXT_EMPTY_PASSWORD);
        assertTrue(TEXT_EMPTY_PASSWORD.contains("password"));
        assertTrue(streams.isEmpty(), "no connection attempt is made");
    }

    // discovery: Thing created from an entry follows the address (the framework replaces the hostname of the thing and
    // calls thingUpdated)

    @Test
    void whenTheFrameworkReplacesTheHostnameOfAnOnlineThingThenTheHandlerReconnectsToTheNewHostWithTheSamePassword() {
        Running r = new Running("bridge01234567", "192.0.2.20", PASSWORD);
        FakeStream old = r.stream();
        old.listener.onStreamActive();
        r.assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
        Bridge updated = bridge("bridge01234567", "192.0.2.21", PASSWORD);

        r.handler.thingUpdated(updated);

        assertEquals(1, old.stops);
        assertEquals(2, streams.size());
        FakeStream renewed = r.stream();
        assertEquals("192.0.2.21", renewed.host);
        assertEquals(PASSWORD, renewed.password);
        assertEquals(1, renewed.starts);
        assertEquals(ThingStatus.UNKNOWN, r.recorder.lastStatus().getStatus());
        old.listener.onStreamActive();
        assertEquals(ThingStatus.UNKNOWN, r.recorder.lastStatus().getStatus(), "the old stream is ignored");
        renewed.listener.onStreamActive();
        assertEquals(ThingStatus.ONLINE, r.recorder.lastStatus().getStatus());
    }

    @Test
    void whenTheConfigurationIsValidThenInitializationReturnsWithUnknownAndStartsOneClient() {
        Running r = new Running("b1", " bridge.example ", PASSWORD);

        r.assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
        assertEquals(1, streams.size());
        FakeStream stream = r.stream();
        assertEquals(1, stream.starts);
        assertEquals("bridge.example", stream.host);
        assertEquals(PASSWORD, stream.password);
        assertEquals("OH-binding-tibber-" + r.bridge.getUID(), stream.threadName);
    }

    @Test
    void whenTheBridgeHasNoChannelsThenCommandsAreIgnored() {
        Running r = new Running("b1", "bridge.example", PASSWORD);

        r.handler.handleCommand(new org.openhab.core.thing.ChannelUID(r.bridge.getUID(), "x"),
                org.openhab.core.types.RefreshType.REFRESH);

        assertTrue(r.bridge.getChannels().isEmpty());
        assertEquals(ThingStatus.UNKNOWN, r.bridge.getStatus());
    }

    // thing-lifecycle: Status model

    @Test
    void whenTheFirstFrameArrivesThenTheBridgeIsOnline() {
        Running r = new Running("b1", "bridge.example", PASSWORD);

        r.listener().onStreamActive();

        r.assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    @Test
    void whenThePasswordIsRejectedThenTheBridgeIsOfflineWithConfigurationErrorStatingIt() {
        Running r = new Running("b1", "bridge.example", PASSWORD);

        r.listener().onStreamLost(new StreamFailure(Kind.REJECTED));

        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/offline.conf-error.password-rejected");
    }

    @ParameterizedTest
    @EnumSource(value = Kind.class, names = { "ENDPOINT_MISSING", "UNREACHABLE", "CLOSED_BY_BRIDGE", "SILENT",
            "PROTOCOL" })
    void whenTheStreamEndsForARecoverableReasonThenTheBridgeIsOfflineWithCommunicationError(Kind kind) {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();

        r.listener().onStreamLost(new StreamFailure(kind));

        assertEquals(ThingStatus.OFFLINE, r.bridge.getStatus());
        assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, r.bridge.getStatusInfo().getStatusDetail());
        String description = r.bridge.getStatusInfo().getDescription();
        assertNotNull(description);
        assertTrue(description.startsWith("@text/offline.comm-error."), description);
    }

    @Test
    void whenTheStreamEndsThenEveryReasonHasItsOwnDescription() {
        List<@Nullable String> descriptions = new ArrayList<>();
        for (Kind kind : Kind.values()) {
            if (kind == Kind.HTTP_STATUS) {
                continue;
            }
            Running r = new Running("b" + kind.ordinal(), "bridge" + kind.ordinal() + ".example", PASSWORD);
            r.listener().onStreamLost(new StreamFailure(kind));
            descriptions.add(r.bridge.getStatusInfo().getDescription());
        }

        assertEquals(descriptions.size(), descriptions.stream().distinct().count());
    }

    @Test
    void whenTheBridgeAnswersWithAnotherHttpStatusThenTheStatusNumberIsPartOfTheDescription() {
        Running r = new Running("b1", "bridge.example", PASSWORD);

        r.listener().onStreamLost(new StreamFailure(503));

        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.comm-error.http-status [\"503\"]");
    }

    @Test
    void whenTheStreamIsLostAndReturnsThenTheBridgeGoesOfflineAndOnlineAgainWithoutTheOldDescription() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        r.listener().onStreamLost(new StreamFailure(Kind.CLOSED_BY_BRIDGE));
        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/offline.comm-error.closed-by-bridge");

        r.listener().onStreamActive();

        r.assertStatus(ThingStatus.ONLINE, ThingStatusDetail.NONE, null);
    }

    @Test
    void whenTwoBridgesUseDifferentHostsThenBothRun() {
        new Running("b1", "bridge-a.example", PASSWORD);
        Running second = new Running("b2", "bridge-b.example", PASSWORD);

        second.assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
        assertEquals(2, streams.size());
    }

    // thing-lifecycle: Disposal and runtime reconfiguration

    @Test
    void whenTheBridgeIsDisposedThenTheStreamIsStoppedOnceAndLateCallbacksPublishNothing() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        FakeStream stream = r.stream();
        r.recorder.clear();

        r.handler.dispose();
        stream.listener.onStreamActive();
        stream.listener.onStreamLost(new StreamFailure(Kind.CLOSED_BY_BRIDGE));
        boolean valid = stream.listener.onMessage(message(DEVICE_A, validFrame(5)));

        assertEquals(1, stream.stops);
        assertFalse(valid);
        assertTrue(r.recorder.statuses.isEmpty());
    }

    @Test
    void whenTheBridgeIsInitializedAgainThenANewClientRunsAndCallbacksOfTheOldOneAreIgnored() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        FakeStream old = r.stream();
        r.handler.dispose();
        r.handler.initialize();
        r.recorder.clear();

        old.listener.onStreamLost(new StreamFailure(Kind.REJECTED));
        old.listener.onStreamActive();

        assertEquals(2, streams.size());
        assertNotSame(old, r.stream());
        assertEquals(1, r.stream().starts);
        assertTrue(r.recorder.statuses.isEmpty());
        r.assertStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, null);
    }

    @Test
    void whenTheHostnameIsChangedThenTheOldStreamIsStoppedBeforeTheNewOneStarts() {
        Running r = new Running("b1", "bridge-a.example", PASSWORD);
        FakeStream old = r.stream();

        r.handler.handleConfigurationUpdate(java.util.Map.of("hostname", "bridge-b.example"));

        assertEquals(1, old.stops);
        assertEquals(2, streams.size());
        assertEquals("bridge-b.example", r.stream().host);
        assertEquals(1, r.stream().starts);
    }

    @Test
    void whenThePasswordIsChangedToAnEmptyOneThenTheOldStreamIsStoppedAndTheBridgeIsMisconfigured() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        FakeStream old = r.stream();

        r.handler.handleConfigurationUpdate(java.util.Map.of("password", ""));

        assertEquals(1, old.stops);
        assertEquals(1, streams.size());
        r.assertStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, TEXT_EMPTY_PASSWORD);
    }

    // channels and thing-lifecycle: routing by device id

    @Test
    void whenAReadingArrivesForTheConfiguredDeviceThenOnlyThatMeterIsUpdated() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        MeterHandler a = r.addMeter("a", DEVICE_A);
        MeterHandler b = r.addMeter("b", DEVICE_B);

        boolean valid = r.listener().onMessage(message(DEVICE_A, validFrame(1523)));

        assertTrue(valid);
        assertEquals(ThingStatus.ONLINE, a.getThing().getStatus());
        assertEquals(watts("1523"), recorderOf(a).lastState(CHANNEL_POWER));
        assertEquals(kilowattHours("1234.5678"), recorderOf(a).lastState(CHANNEL_ENERGY_IMPORT));
        assertEquals("0a0145", a.getThing().getProperties().get(PROPERTY_METER_ID));
        assertEquals(ThingStatus.UNKNOWN, b.getThing().getStatus());
        assertEquals(UnDefType.UNDEF, recorderOf(b).lastState(CHANNEL_POWER));
    }

    @Test
    void whenAReadingArrivesForAnotherDeviceThenNoChannelOfTheMeterChanges() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        MeterHandler a = r.addMeter("a", DEVICE_A);
        r.listener().onMessage(message(DEVICE_A, validFrame(100)));
        Recorder rec = recorderOf(a);
        assertNotNull(rec.lastState(CHANNEL_POWER));
        rec.clear();

        r.listener().onMessage(message(DEVICE_B, validFrame(5000)));

        assertTrue(rec.updates.isEmpty());
        assertEquals(ThingStatus.ONLINE, a.getThing().getStatus());
    }

    @Test
    void whenTwoMetersShareOneStreamThenEachShowsOnlyTheValuesOfItsOwnDevice() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        MeterHandler a = r.addMeter("a", DEVICE_A);
        MeterHandler b = r.addMeter("b", "BBBBBBBBBBBBBBBB");

        r.listener().onMessage(message(DEVICE_A, validFrame(100)));
        r.listener().onMessage(message(DEVICE_B, validFrame(200)));

        assertEquals(watts("100"), recorderOf(a).lastState(CHANNEL_POWER));
        assertEquals(watts("200"), recorderOf(b).lastState(CHANNEL_POWER));
    }

    @Test
    void whenAnUpperCaseDeviceIdIsInTheHeaderThenItIsRoutedToTheMeter() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        MeterHandler a = r.addMeter("a", DEVICE_A);

        r.listener().onMessage(message(DEVICE_A.toUpperCase(java.util.Locale.ROOT), validFrame(7)));

        assertEquals(watts("7"), recorderOf(a).lastState(CHANNEL_POWER));
    }

    @Test
    void whenAMeterThingIsRemovedThenTheOtherMeterAndTheStreamAreUnaffected() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        MeterHandler a = r.addMeter("a", DEVICE_A);
        MeterHandler b = r.addMeter("b", DEVICE_B);

        a.dispose();
        ((BridgeImpl) r.bridge).removeThing(a.getThing());
        boolean valid = r.listener().onMessage(message(DEVICE_B, validFrame(9)));

        assertTrue(valid);
        assertEquals(0, r.stream().stops);
        assertEquals(ThingStatus.ONLINE, b.getThing().getStatus());
        assertEquals(ThingStatus.ONLINE, r.bridge.getStatus());
    }

    @Test
    void whenTheBridgeIsDisposedThenItsMeterThingsKeepTheirConfiguration() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        MeterHandler a = r.addMeter("a", DEVICE_A);

        r.handler.dispose();

        assertEquals(DEVICE_A, a.getThing().getConfiguration().get("deviceId"));
        assertEquals(1, r.bridge.getThings().size());
    }

    // channels: SML frame validity as seen by the stream callback

    @Test
    void whenTheMessageCannotBeUsedThenItIsDroppedWithoutEffectOnStatusOrChannels() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        MeterHandler a = r.addMeter("a", DEVICE_A);
        r.listener().onMessage(message(DEVICE_A, validFrame(100)));
        Recorder rec = recorderOf(a);
        rec.clear();
        r.recorder.clear();
        byte[] frame = validFrame(200);
        byte[] truncated = java.util.Arrays.copyOf(frame, frame.length / 2);
        byte[] flipped = frame.clone();
        flipped[frame.length - 8] ^= 0x55;
        byte[] firmwareOnly = new SmlFrameBuilder().octets(FIRMWARE, new byte[] { 1, 2, 3 }).build();
        byte[] otherTopic = concat("<device:aaaaaaaaaaaaaaaa topic:\"reading\">".getBytes(StandardCharsets.US_ASCII),
                frame);

        assertFalse(r.listener().onMessage(new byte[0]));
        assertFalse(r.listener().onMessage("garbage".getBytes(StandardCharsets.US_ASCII)));
        assertFalse(r.listener().onMessage(message(DEVICE_A, new byte[0])));
        assertFalse(r.listener().onMessage(message(DEVICE_A, truncated)));
        assertFalse(r.listener().onMessage(message(DEVICE_A, flipped)));
        assertFalse(r.listener().onMessage(message(DEVICE_A, firmwareOnly)));
        assertFalse(r.listener().onMessage(otherTopic));

        assertTrue(rec.updates.isEmpty());
        assertTrue(rec.statuses.isEmpty());
        assertTrue(r.recorder.statuses.isEmpty());
        assertEquals(ThingStatus.ONLINE, a.getThing().getStatus());
    }

    @Test
    void whenAValidReadingBelongsToNoMeterThenTheStreamStillCountsItAsValid() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();

        assertTrue(r.listener().onMessage(message(DEVICE_A, validFrame(1))));
    }

    // discovery: meter discovery from the stream

    private BlockingQueue<String> listenForDevices(BridgeHandler handler) {
        BlockingQueue<String> seen = new LinkedBlockingQueue<>();
        handler.addDeviceSeenListener(seen::add);
        return seen;
    }

    private static @Nullable String poll(BlockingQueue<String> queue, long millis) throws InterruptedException {
        return queue.poll(millis, TimeUnit.MILLISECONDS);
    }

    @Test
    void whenANewDeviceSendsValidReadingsThenTheListenerHearsOfItExactlyOnce() throws Exception {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        BlockingQueue<String> seen = listenForDevices(r.handler);

        for (int i = 0; i < 100; i++) {
            r.listener().onMessage(message(DEVICE_A, validFrame(i)));
        }

        assertEquals(DEVICE_A, poll(seen, WAIT_MILLIS));
        assertNull(poll(seen, 200));
        assertEquals(List.of(DEVICE_A), List.copyOf(r.handler.getSeenDeviceIds()));
    }

    @Test
    void whenAMeterThingExistsForTheDeviceThenItIsNotOffered() throws Exception {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        r.addMeter("a", DEVICE_A);
        BlockingQueue<String> seen = listenForDevices(r.handler);

        r.listener().onMessage(message(DEVICE_A, validFrame(1)));
        r.listener().onMessage(message(DEVICE_B, validFrame(1)));

        assertEquals(DEVICE_B, poll(seen, WAIT_MILLIS));
        assertNull(poll(seen, 200));
        assertTrue(r.handler.getSeenDeviceIds().contains(DEVICE_B));
        assertFalse(r.handler.getSeenDeviceIds().contains(DEVICE_A));
    }

    @Test
    void whenMessagesAreIgnoredOrDroppedThenNoDeviceIsOffered() throws Exception {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        BlockingQueue<String> seen = listenForDevices(r.handler);
        byte[] frame = validFrame(1);

        r.listener().onMessage("<device:zzzz topic:\"sml\">".getBytes(StandardCharsets.US_ASCII));
        r.listener().onMessage(message(DEVICE_A, java.util.Arrays.copyOf(frame, 100)));
        r.listener().onMessage(
                concat("<device:aaaaaaaaaaaaaaaa topic:\"reading\">".getBytes(StandardCharsets.US_ASCII), frame));

        assertNull(poll(seen, 300));
        assertTrue(r.handler.getSeenDeviceIds().isEmpty());
    }

    @Test
    void whenAListenerRegistersLateThenItHearsOfTheDevicesSeenBefore() throws Exception {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        r.listener().onMessage(message(DEVICE_A, validFrame(1)));
        r.listener().onMessage(message(DEVICE_B, validFrame(1)));

        BlockingQueue<String> seen = listenForDevices(r.handler);

        List<@Nullable String> heard = new ArrayList<>();
        heard.add(poll(seen, WAIT_MILLIS));
        heard.add(poll(seen, WAIT_MILLIS));
        assertTrue(heard.containsAll(List.of(DEVICE_A, DEVICE_B)), heard.toString());
    }

    @Test
    void whenTooManyDevicesAppearThenOnlyTheFirstOnesAreRemembered() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        for (int i = 0; i < BridgeHandler.MAX_SEEN_DEVICES + 10; i++) {
            r.listener().onMessage(message(String.format("%016x", i + 1), validFrame(1)));
        }

        assertEquals(BridgeHandler.MAX_SEEN_DEVICES, r.handler.getSeenDeviceIds().size());
    }

    @Test
    void whenAListenerIsRemovedThenItHearsNothingMore() throws Exception {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        BlockingQueue<String> seen = new LinkedBlockingQueue<>();
        BridgeHandler.DeviceSeenListener listener = seen::add;
        r.handler.addDeviceSeenListener(listener);
        r.handler.removeDeviceSeenListener(listener);

        r.listener().onMessage(message(DEVICE_A, validFrame(1)));

        assertNull(poll(seen, 300));
    }

    @Test
    void whenTheBridgeIsDisposedThenTheSeenDevicesAreForgotten() {
        Running r = new Running("b1", "bridge.example", PASSWORD);
        r.listener().onStreamActive();
        r.listener().onMessage(message(DEVICE_A, validFrame(1)));

        r.handler.dispose();

        assertTrue(r.handler.getSeenDeviceIds().isEmpty());
    }

    private static byte[] concat(byte[]... parts) {
        return SmlFrameBuilder.concat(parts);
    }
}
