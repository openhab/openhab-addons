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
package org.openhab.binding.motionblinds.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.motionblinds.internal.MessageSerializationTest.*;
import static org.openhab.binding.motionblinds.internal.MotionBlindsBindingConstants.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsMessage;
import org.openhab.binding.motionblinds.internal.dto.MotionBlindsRequest;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;

import com.google.gson.Gson;

/**
 * Tests for {@link MotionBlindsHandler} with a mocked {@link MotionBlindsCommunicationManager}.
 *
 * @author Oleksandr Mishchuk - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@NonNullByDefault
public class MotionBlindsHandlerTest {

    private static final Gson GSON = new Gson();
    private static final String IP = "192.168.0.21";
    private static final String OTHER_IP = "192.168.0.99";
    private static final String MAC = "244cab4e06b4";
    private static final String KEY = "12ab345c-d67e-8f";
    // covers the time the handler waits for the answer to a multicast GetDeviceList
    private static final long TIMEOUT_MS = 5000;
    /** First move of a tilt to 75% from fully lowered at 176°, see {@link #motorTiltsInTwoMoves} */
    private static final int FIRST_TILT_MOVE = 176 - 2 * 29 + SlatTracker.MOTOR_OVERSHOOT;

    private @Mock @NonNullByDefault({}) MotionBlindsCommunicationManager communicationManager;
    private @Mock @NonNullByDefault({}) ThingHandlerCallback callback;
    private @NonNullByDefault({}) MotionBlindsHandler handler;

    @BeforeEach
    public void setUp() {
        motorAnswersDiscoveryFrom(IP);
        // like the framework, apply status updates to the thing
        doAnswer(invocation -> {
            Thing thing = invocation.getArgument(0);
            thing.setStatusInfo(invocation.getArgument(1));
            return null;
        }).when(callback).statusUpdated(any(Thing.class), any(ThingStatusInfo.class));
    }

    @AfterEach
    public void tearDown() {
        if (handler != null) {
            handler.dispose();
        }
    }

    @Test
    public void initializeFetchesTokenAndReadsStatus() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).stateUpdated(channel(CHANNEL_POSITION), eq(new PercentType(98)));
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));
        // currentAngle 180° is 100% tilt
        verify(callback).stateUpdated(channel(CHANNEL_TILT), eq(PercentType.HUNDRED));

        List<MotionBlindsRequest> requests = captureRequests(2);
        assertEquals(MSG_GET_DEVICE_LIST, requests.get(0).msgType);
        MotionBlindsRequest read = requests.get(1);
        assertEquals(MSG_READ_DEVICE, read.msgType);
        assertEquals(MAC, read.mac);
        assertEquals(DEVICE_TYPE_WIFI_BLIND, read.deviceType);
        assertEquals("4D098FF506E81A40DB823D3F60333214", read.accessToken);
        verify(communicationManager).registerListener(MAC, handler);
    }

    @Test
    public void rejectedRequestIsRetriedWithTokenFromRejection() throws IOException {
        respond(GET_DEVICE_LIST_ACK, ACCESS_TOKEN_ERROR, READ_DEVICE_ACK);
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));
        List<MotionBlindsRequest> requests = captureRequests(3);
        assertEquals(List.of(MSG_GET_DEVICE_LIST, MSG_READ_DEVICE, MSG_READ_DEVICE),
                requests.stream().map(r -> r.msgType).toList());
        // the retry uses the access token calculated from the token in the rejection
        assertNotEquals(requests.get(1).accessToken, requests.get(2).accessToken);
        assertEquals(AccessTokenCalculator.calculate(KEY, "Zx8cVb7nMl6kJh5g"), requests.get(2).accessToken);
    }

    @Test
    public void rejectedRequestWithoutTokenFetchesNewToken() throws IOException {
        respond(GET_DEVICE_LIST_ACK, ACCESS_TOKEN_ERROR.replace(",\"token\":\"Zx8cVb7nMl6kJh5g\"", ""),
                GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));
        List<MotionBlindsRequest> requests = captureRequests(4);
        assertEquals(List.of(MSG_GET_DEVICE_LIST, MSG_READ_DEVICE, MSG_GET_DEVICE_LIST, MSG_READ_DEVICE),
                requests.stream().map(r -> r.msgType).toList());
    }

    @Test
    public void wrongKeyGoesOfflineWithConfigurationError() throws IOException {
        respond(GET_DEVICE_LIST_ACK, ACCESS_TOKEN_ERROR, ACCESS_TOKEN_ERROR);
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
    }

    @Test
    public void heartbeatDoesNotHideRejectedKey() throws IOException {
        respond(GET_DEVICE_LIST_ACK, ACCESS_TOKEN_ERROR, ACCESS_TOKEN_ERROR);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isAccessDenied()));

        // the motor is reachable and reports its state, but that does not prove that the key is accepted
        handler.onMessage(parse(HEARTBEAT), IP);
        verify(callback).stateUpdated(channel(CHANNEL_POSITION), eq(new PercentType(98)));
        assertTrue(isAccessDenied().matches(handler.getThing().getStatusInfo()));
        verify(callback, never()).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        // an accepted request clears it
        respond(READ_DEVICE_ACK);
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), RefreshType.REFRESH);
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));
    }

    @Test
    public void heartbeatDeliveredAfterDisposeIsIgnored() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        // the communication manager captured the listener before it was unregistered
        handler.dispose();
        clearInvocations(callback);
        handler.onMessage(parse(HEARTBEAT), IP);

        verifyNoInteractions(callback);
    }

    @Test
    public void disposeWaitsForRunningHeartbeat() throws IOException, InterruptedException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        CountDownLatch publishing = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        doAnswer(invocation -> {
            publishing.countDown();
            assertTrue(release.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
            return null;
        }).when(callback).stateUpdated(channel(CHANNEL_POSITION), any());
        Thread listener = new Thread(() -> handler.onMessage(parse(HEARTBEAT), IP), "listener");
        listener.setDaemon(true);
        listener.start();
        assertTrue(publishing.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));

        // the heartbeat is half published when the Thing is disabled
        Thread disposer = new Thread(() -> {
            handler.dispose();
            clearInvocations(callback);
        }, "disposer");
        disposer.setDaemon(true);
        disposer.start();
        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        while (disposer.isAlive() && disposer.getState() != Thread.State.BLOCKED
                && System.currentTimeMillis() < deadline) {
            Thread.onSpinWait();
        }
        release.countDown();
        disposer.join(TIMEOUT_MS);
        listener.join(TIMEOUT_MS);
        assertFalse(disposer.isAlive());
        assertFalse(listener.isAlive());

        // the heartbeat was completed before dispose returned, nothing followed it
        verifyNoInteractions(callback);
    }

    @Test
    public void noResponseGoesOfflineWithCommunicationError() throws IOException {
        when(communicationManager.sendRequest(anyString(), any(), eq(MAC))).thenThrow(new IOException("No response"));
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
    }

    @Test
    public void invalidKeyIsRejectedWithoutCommunication() throws IOException {
        createHandler("too-short").initialize();

        verify(callback).statusUpdated(any(Thing.class), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        verify(communicationManager, never()).sendRequest(anyString(), any(), anyString());
        verify(communicationManager, never()).registerListener(anyString(), any());
    }

    @Test
    public void nonAsciiKeyIsRejectedWithoutCommunication() throws IOException {
        // 16 characters, but not the 16 bytes of an AES key
        createHandler("12ab345c-d67e-8ä").initialize();

        verify(callback).statusUpdated(any(Thing.class), argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR));
        verify(communicationManager, never()).sendRequest(anyString(), any(), anyString());
        verify(communicationManager, never()).registerListener(anyString(), any());
    }

    @Test
    public void commandsAreSentAsWriteDevice() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK, READ_DEVICE_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), UpDownType.UP);
        captureRequests(3);
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), new PercentType(50));

        List<MotionBlindsRequest> writes = captureRequests(4).subList(2, 4);
        assertTrue(writes.stream().allMatch(r -> MSG_WRITE_DEVICE.equals(r.msgType)));
        assertEquals(List.of(Map.of("operation", OPERATION_OPEN), Map.of("targetPosition", 50)),
                writes.stream().map(r -> r.data).toList());
    }

    @Test
    public void olderMovementIsNotSentAfterStop() throws IOException, InterruptedException {
        AtomicBoolean blockNextRead = new AtomicBoolean();
        CountDownLatch readStarted = new CountDownLatch(1);
        CountDownLatch releaseRead = new CountDownLatch(1);
        doAnswer(invocation -> {
            MotionBlindsRequest request = invocation.getArgument(1);
            if (MSG_GET_DEVICE_LIST.equals(request.msgType)) {
                return parse(GET_DEVICE_LIST_ACK);
            }
            if (MSG_READ_DEVICE.equals(request.msgType) && blockNextRead.getAndSet(false)) {
                readStarted.countDown();
                assertTrue(releaseRead.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
            }
            return parse(READ_DEVICE_ACK);
        }).when(communicationManager).sendRequest(eq(IP), any(), eq(MAC));
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        // a slow request holds the motor, so both commands are still queued when it ends and may run in any order
        blockNextRead.set(true);
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), RefreshType.REFRESH);
        assertTrue(readStarted.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), UpDownType.UP);
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), StopMoveType.STOP);
        releaseRead.countDown();

        verify(communicationManager, timeout(TIMEOUT_MS)).sendRequest(eq(IP),
                argThat(r -> Map.of("operation", OPERATION_STOP).equals(r.data)), eq(MAC));
        // the older UP must not be sent, neither before nor after the STOP
        verify(communicationManager, after(1000).never()).sendRequest(eq(IP),
                argThat(r -> Map.of("operation", OPERATION_OPEN).equals(r.data)), eq(MAC));
        assertEquals(List.of(Map.of("operation", OPERATION_STOP)), writes());
    }

    @Test
    public void angleCommandIsSentAsTargetAngle() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_ANGLE),
                new QuantityType<>(135, Units.DEGREE_ANGLE));

        assertEquals(Map.of("targetAngle", 135), captureRequests(3).get(2).data);
    }

    @Test
    public void noSecondTiltMoveWhenMotorStopsAnswering() throws IOException, InterruptedException {
        when(communicationManager.sendRequest(eq(IP), any(), eq(MAC)))
                .thenReturn(parse(GET_DEVICE_LIST_ACK), parse(status(100, 176)), parse(status(100, 176)))
                .thenThrow(new IOException("No response"));
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_TILT), new PercentType(75));

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        // give a second move the time it would need; only the first one may have been sent
        Thread.sleep(3000);
        ArgumentCaptor<MotionBlindsRequest> captor = ArgumentCaptor.forClass(MotionBlindsRequest.class);
        verify(communicationManager, atLeastOnce()).sendRequest(eq(IP), captor.capture(), eq(MAC));
        assertEquals(1, captor.getAllValues().stream().filter(r -> MSG_WRITE_DEVICE.equals(r.msgType)).count());
    }

    @Test
    public void noSecondTiltMoveWhileBlindKeepsMoving() throws IOException, InterruptedException {
        // after the first tilt move the blind keeps travelling up, e.g. started with the remote
        respond(GET_DEVICE_LIST_ACK, status(100, 176), status(100, 176), status(99, 160), status(98, 140),
                status(97, 120), status(96, 100), status(95, 80), status(94, 60), status(93, 40));
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_TILT), new PercentType(75));

        // all stop checks see a different angle; give a second move the time it would need afterwards
        Thread.sleep(8000);
        ArgumentCaptor<MotionBlindsRequest> captor = ArgumentCaptor.forClass(MotionBlindsRequest.class);
        verify(communicationManager, atLeastOnce()).sendRequest(eq(IP), captor.capture(), eq(MAC));
        assertEquals(1, captor.getAllValues().stream().filter(r -> MSG_WRITE_DEVICE.equals(r.msgType)).count());
    }

    @Test
    public void tiltAboveOpenFromFullyLoweredTakesTwoMoves() throws IOException {
        // after the first command the motor is still moving on the first check; it overshoots the target by only 1°
        // and stops 6° before the end of the slack
        respond(GET_DEVICE_LIST_ACK, status(100, 176), status(100, 176), status(99, 150), status(98, 124),
                status(98, 124));
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));
        verify(callback).stateUpdated(channel(CHANNEL_TILT), eq(PercentType.HUNDRED));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_TILT), new PercentType(75));

        List<MotionBlindsRequest> requests = captureRequests(7);
        // up through the default slack of 2% to open, minus the overshoot of the motor
        assertEquals(Map.of("targetAngle", 176 - 2 * 29 + SlatTracker.MOTOR_OVERSHOOT), requests.get(2).data);
        // wait until two checks agree
        assertEquals(List.of(MSG_READ_DEVICE, MSG_READ_DEVICE, MSG_READ_DEVICE),
                requests.subList(3, 6).stream().map(r -> r.msgType).toList());
        verify(callback, timeout(TIMEOUT_MS).atLeastOnce()).stateUpdated(channel(CHANNEL_TILT),
                eq(new PercentType(50)));
        // then a quarter swing down, at half the rate while the bottom rail sets down on the sill
        Map<String, Object> second = Objects.requireNonNull(requests.get(6).data);
        Object targetAngle = second.get("targetAngle");
        assertTrue(
                targetAngle instanceof Integer angle && Math.abs(angle - (124 + 44 - SlatTracker.MOTOR_OVERSHOOT)) <= 2,
                "second move: " + second);
    }

    @Test
    public void stopDuringTiltCancelsSecondMove() throws IOException, InterruptedException {
        // the user stops the blind while the tilt waits for the first move to end
        motorTiltsInTwoMoves(() -> handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION),
                StopMoveType.STOP));
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_TILT), new PercentType(75));

        verify(communicationManager, timeout(TIMEOUT_MS)).sendRequest(eq(IP),
                argThat(r -> Map.of("operation", OPERATION_STOP).equals(r.data)), eq(MAC));
        // give a second move the time it would need; it must not restart the motor after the STOP
        Thread.sleep(4000);
        assertEquals(List.of(Map.of("targetAngle", FIRST_TILT_MOVE), Map.of("operation", OPERATION_STOP)), writes());
    }

    @Test
    public void disposeDuringTiltCancelsSecondMove() throws IOException, InterruptedException {
        CountDownLatch firstMove = new CountDownLatch(1);
        motorTiltsInTwoMoves(firstMove::countDown);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_TILT), new PercentType(75));
        assertTrue(firstMove.await(TIMEOUT_MS, TimeUnit.MILLISECONDS));
        handler.dispose();
        clearInvocations(callback);
        int requestsBeforeDispose = mockingDetails(communicationManager).getInvocations().size();

        // give a second move the time it would need; nothing may be sent or published anymore
        Thread.sleep(4000);
        assertEquals(requestsBeforeDispose, mockingDetails(communicationManager).getInvocations().size());
        assertEquals(List.of(Map.of("targetAngle", FIRST_TILT_MOVE)), writes());
        verifyNoInteractions(callback);
    }

    /**
     * The motor answers like in {@link #tiltAboveOpenFromFullyLoweredTakesTwoMoves}, so a tilt to 75% takes two
     * moves.
     *
     * @param onFirstTiltMove run when the first tilt move is sent
     */
    private void motorTiltsInTwoMoves(Runnable onFirstTiltMove) throws IOException {
        Queue<String> reads = new ConcurrentLinkedQueue<>(
                List.of(status(100, 176), status(99, 150), status(98, 124), status(98, 124)));
        AtomicBoolean firstTiltMoveSent = new AtomicBoolean();
        doAnswer(invocation -> {
            MotionBlindsRequest request = invocation.getArgument(1);
            if (MSG_GET_DEVICE_LIST.equals(request.msgType)) {
                return parse(GET_DEVICE_LIST_ACK);
            } else if (MSG_READ_DEVICE.equals(request.msgType)) {
                String read = reads.poll();
                return parse(read != null ? read : status(98, 124));
            }
            Map<String, Object> data = request.data;
            if (data != null && data.containsKey("targetAngle") && !firstTiltMoveSent.getAndSet(true)) {
                onFirstTiltMove.run();
            }
            return parse(status(100, 176));
        }).when(communicationManager).sendRequest(eq(IP), any(), eq(MAC));
    }

    private List<@Nullable Map<String, Object>> writes() throws IOException {
        ArgumentCaptor<MotionBlindsRequest> captor = ArgumentCaptor.forClass(MotionBlindsRequest.class);
        verify(communicationManager, atLeastOnce()).sendRequest(eq(IP), captor.capture(), eq(MAC));
        return captor.getAllValues().stream().filter(r -> MSG_WRITE_DEVICE.equals(r.msgType)).map(r -> r.data).toList();
    }

    @Test
    public void pushedReportUpdatesStateAndIpAddress() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        handler.onMessage(parse(REPORT_MOVING), OTHER_IP);
        verify(callback).stateUpdated(channel(CHANNEL_POSITION), eq(new PercentType(90)));

        // further requests go to the address the motor reported from
        when(communicationManager.sendRequest(eq(OTHER_IP), any(), eq(MAC))).thenReturn(parse(READ_DEVICE_ACK));
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), RefreshType.REFRESH);
        verify(communicationManager, timeout(TIMEOUT_MS)).sendRequest(eq(OTHER_IP), any(), eq(MAC));
    }

    @Test
    public void motorIsFoundByMac() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));
        verify(communicationManager).sendDiscoveryRequest();
        captureRequests(2);
    }

    @Test
    public void motorWithNewIpAddressIsFoundAgain() throws IOException {
        respond(GET_DEVICE_LIST_ACK, READ_DEVICE_ACK);
        createHandler(KEY).initialize();
        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class), argThat(isStatus(ThingStatus.ONLINE)));

        // the motor got a new IP address and does not answer on the old one anymore
        when(communicationManager.sendRequest(eq(IP), any(), eq(MAC))).thenThrow(new IOException("No response"));
        when(communicationManager.sendRequest(eq(OTHER_IP), any(), eq(MAC))).thenReturn(parse(READ_DEVICE_ACK));
        motorAnswersDiscoveryFrom(OTHER_IP);
        handler.handleCommand(new ChannelUID(handler.getThing().getUID(), CHANNEL_POSITION), RefreshType.REFRESH);

        verify(communicationManager, timeout(TIMEOUT_MS)).sendRequest(eq(OTHER_IP), any(), eq(MAC));
        verify(communicationManager, times(2)).sendDiscoveryRequest();
    }

    @Test
    public void motorNotFoundGoesOfflineWithCommunicationError() throws IOException {
        doNothing().when(communicationManager).sendDiscoveryRequest();
        createHandler(KEY).initialize();

        verify(callback, timeout(TIMEOUT_MS)).statusUpdated(any(Thing.class),
                argThat(info -> info.getStatus() == ThingStatus.OFFLINE
                        && info.getStatusDetail() == ThingStatusDetail.COMMUNICATION_ERROR));
        verify(communicationManager, never()).sendRequest(anyString(), any(), anyString());
    }

    private MotionBlindsHandler createHandler(String key) {
        Thing thing = ThingBuilder.create(THING_TYPE_WIFI_MOTOR, MAC)
                .withConfiguration(
                        new Configuration(Map.of(CONFIG_MAC_ADDRESS, MAC, "key", key, "refreshInterval", 3600)))
                .build();
        handler = new MotionBlindsHandler(thing, communicationManager);
        handler.setCallback(callback);
        return handler;
    }

    /**
     * The motor answers a multicast GetDeviceList from the given address.
     */
    private void motorAnswersDiscoveryFrom(String ipAddress) {
        doAnswer(invocation -> {
            handler.onMessage(parse(GET_DEVICE_LIST_ACK), ipAddress);
            return null;
        }).when(communicationManager).sendDiscoveryRequest();
    }

    private void respond(String first, String... next) throws IOException {
        MotionBlindsMessage[] messages = new MotionBlindsMessage[next.length];
        for (int i = 0; i < next.length; i++) {
            messages[i] = parse(next[i]);
        }
        when(communicationManager.sendRequest(eq(IP), any(), eq(MAC))).thenReturn(parse(first), messages);
    }

    private static String status(int position, int angle) {
        return READ_DEVICE_ACK.replace("\"currentPosition\":98", "\"currentPosition\":" + position)
                .replace("\"currentAngle\":180", "\"currentAngle\":" + angle);
    }

    private static MotionBlindsMessage parse(String json) {
        return Objects.requireNonNull(GSON.fromJson(json, MotionBlindsMessage.class));
    }

    private List<MotionBlindsRequest> captureRequests(int count) throws IOException {
        ArgumentCaptor<MotionBlindsRequest> captor = ArgumentCaptor.forClass(MotionBlindsRequest.class);
        verify(communicationManager, timeout(TIMEOUT_MS).times(count)).sendRequest(eq(IP), captor.capture(), eq(MAC));
        return captor.getAllValues();
    }

    private ChannelUID channel(String id) {
        return eq(new ChannelUID(handler.getThing().getUID(), id));
    }

    private static org.mockito.ArgumentMatcher<ThingStatusInfo> isStatus(ThingStatus status) {
        return info -> info.getStatus() == status;
    }

    private static org.mockito.ArgumentMatcher<ThingStatusInfo> isAccessDenied() {
        return info -> info.getStatus() == ThingStatus.OFFLINE
                && info.getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR
                && "@text/offline.config-error-access-denied".equals(info.getDescription());
    }
}
