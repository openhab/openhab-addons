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
package org.openhab.binding.modbus.hager.internal.modbus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Access;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.binding.modbus.hager.internal.modbus.RegisterPoller.PollStatus;
import org.openhab.core.io.transport.modbus.AsyncModbusFailure;
import org.openhab.core.io.transport.modbus.AsyncModbusReadResult;
import org.openhab.core.io.transport.modbus.ModbusCommunicationInterface;
import org.openhab.core.io.transport.modbus.ModbusFailureCallback;
import org.openhab.core.io.transport.modbus.ModbusReadCallback;
import org.openhab.core.io.transport.modbus.ModbusReadRequestBlueprint;
import org.openhab.core.io.transport.modbus.ModbusRegisterArray;
import org.openhab.core.io.transport.modbus.PollTask;

/**
 * The {@link RegisterPollerTest} verifies the lifecycle of the regular polls and of the one-time requests. In
 * particular a poller which was stopped must not register polls afterwards, must not publish any further values and
 * must cancel the requests it is tracking.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null", "unchecked" })
public class RegisterPollerTest {

    private static final String THING_UID = "modbus:hager-flow-evse:flow:ems:evse1";
    private static final int SLAVE_ID = 1;
    private static final long REFRESH_MS = 5000;
    private static final Register READ_ONLY_REGISTER = new Register("evcs-name", "identity", 4098, 1, DataType.U16,
            Access.READ_ONLY, null, Map.of(), List.of());
    private static final Register WRITABLE_REGISTER = new Register("evcs-boost-mode", "configuration", 4400, 1,
            DataType.U16, Access.READ_WRITE, null, Map.of(), List.of());

    private final ModbusCommunicationInterface comms = mock(ModbusCommunicationInterface.class);
    private final RegisterPoller.Listener listener = mock(RegisterPoller.Listener.class);
    private final List<PollTask> pollTasks = new ArrayList<>();
    private final List<ModbusReadRequestBlueprint> requests = new ArrayList<>();
    private final List<ModbusReadCallback> resultCallbacks = new ArrayList<>();
    private final List<ModbusFailureCallback<ModbusReadRequestBlueprint>> failureCallbacks = new ArrayList<>();

    private RegisterPoller poller = new RegisterPoller(THING_UID, SLAVE_ID, REFRESH_MS,
            List.of(READ_ONLY_REGISTER, WRITABLE_REGISTER), listener);

    @BeforeEach
    public void setUpComms() {
        when(comms.registerRegularPoll(any(), anyLong(), anyLong(), any(), any())).thenAnswer(invocation -> {
            requests.add(invocation.getArgument(0));
            resultCallbacks.add(invocation.getArgument(3));
            failureCallbacks.add(invocation.getArgument(4));
            PollTask task = mock(PollTask.class);
            pollTasks.add(task);
            return task;
        });
        Future<Object> pending = mockFuture();
        doReturn(pending).when(comms).submitOneTimePoll(any(), any(), any());
        doReturn(mockFuture()).when(comms).submitOneTimeWrite(any(), any(), any());
        poller = new RegisterPoller(THING_UID, SLAVE_ID, REFRESH_MS, List.of(READ_ONLY_REGISTER, WRITABLE_REGISTER),
                listener);
    }

    @Test
    public void testStartRegistersOnePollPerReadBlock() {
        poller.start(comms);

        assertEquals(2, pollTasks.size(), "one poll per read block");
        verify(comms, times(2)).registerRegularPoll(any(), eq(REFRESH_MS), eq(0L), any(), any());
        assertEquals(SLAVE_ID, requests.get(0).getUnitID());
    }

    @Test
    public void testStartDoesNotRegisterTwice() {
        poller.start(comms);
        poller.start(comms);

        assertEquals(2, pollTasks.size(), "a second start must not register additional polls");
    }

    @Test
    public void testStopUnregistersEveryRegisteredPoll() {
        poller.start(comms);

        poller.stop();

        for (PollTask task : pollTasks) {
            verify(comms).unregisterRegularPoll(task);
        }
    }

    @Test
    public void testStartAfterStopRegistersNoPoll() {
        poller.stop();

        poller.start(comms);

        verify(comms, never()).registerRegularPoll(any(), anyLong(), anyLong(), any(), any());
        assertTrue(pollTasks.isEmpty(), "a stopped poller must not register polls");
    }

    @Test
    public void testPendingRequestIsCancelledOnStop() {
        Future<Object> pending = mockFuture();
        doReturn(pending).when(comms).submitOneTimePoll(any(), any(), any());
        poller.start(comms);

        poller.refresh(READ_ONLY_REGISTER);
        poller.stop();

        verify(pending).cancel(true);
    }

    @Test
    public void testPendingWriteIsCancelledOnStop() {
        Future<Object> pending = mockFuture();
        doReturn(pending).when(comms).submitOneTimeWrite(any(), any(), any());
        poller.start(comms);

        poller.write(WRITABLE_REGISTER, 1);
        poller.stop();

        verify(pending).cancel(true);
    }

    @Test
    public void testRefreshAfterStopIsNotSubmitted() {
        poller.start(comms);
        poller.stop();

        poller.refresh(READ_ONLY_REGISTER);
        poller.write(WRITABLE_REGISTER, 1);

        verify(comms, never()).submitOneTimePoll(any(), any(), any());
        verify(comms, never()).submitOneTimeWrite(any(), any(), any());
    }

    @Test
    public void testResponseAfterStopIsNotPublished() {
        poller.start(comms);
        poller.stop();

        deliverResult(0, new ModbusRegisterArray(1));

        verifyNoInteractions(listener);
    }

    @Test
    public void testFailureAfterStopIsNotPublished() {
        poller.start(comms);
        poller.stop();

        deliverFailure(0, new IllegalStateException("connection closed"));

        verifyNoInteractions(listener);
    }

    @Test
    public void testSuccessfulResponseIsPublished() {
        poller.start(comms);

        deliverResult(0, new ModbusRegisterArray(1));
        // the poll status is only reported when every read block answered
        verify(listener, never()).onStatus(any());
        deliverResult(1, new ModbusRegisterArray(1));

        verify(listener, times(2)).onUpdates(any());
        verify(listener).onStatus(PollStatus.OK);
    }

    @Test
    public void testFailedResponseIsPublished() {
        poller.start(comms);

        deliverFailure(0, new IllegalStateException("connection closed"));
        deliverFailure(1, new IllegalStateException("connection closed"));

        verify(listener).onStatus(PollStatus.FAILED);
    }

    private void deliverResult(int index, ModbusRegisterArray registers) {
        resultCallbacks.get(index).handle(new AsyncModbusReadResult(requests.get(index), registers));
    }

    private void deliverFailure(int index, Exception cause) {
        failureCallbacks.get(index).handle(new AsyncModbusFailure<>(requests.get(index), cause));
    }

    private static Future<Object> mockFuture() {
        return mock(Future.class);
    }
}
