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
package org.openhab.binding.huesync.internal.handler.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDevice;
import org.openhab.binding.huesync.internal.api.dto.device.HueSyncDeviceDetailed;
import org.openhab.binding.huesync.internal.api.dto.execution.HueSyncExecution;
import org.openhab.binding.huesync.internal.api.dto.hdmi.HueSyncHdmi;
import org.openhab.binding.huesync.internal.connection.HueSyncDeviceConnection;
import org.openhab.binding.huesync.internal.types.HueSyncExceptionHandler;

/**
 * Regression tests for {@link HueSyncUpdateTask}.
 *
 * <p>
 * Verifies the exception-handling contract introduced by Issue #19079: the action consumer is
 * called only on complete success, and the exception handler is invoked on any fetch failure —
 * never the reverse.
 *
 * @author Patrik Gfeller - Initial contribution
 * @author Patrik Gfeller - Issue #19079, Regression tests
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class HueSyncUpdateTaskTest {

    @Mock
    private HueSyncDeviceConnection connection;
    @Mock
    private HueSyncExceptionHandler exceptionHandler;

    private HueSyncDevice deviceInfo = new HueSyncDevice();

    @BeforeEach
    void setup() {
        // Mocks are injected by MockitoExtension
        deviceInfo = new HueSyncDevice();
        deviceInfo.name = "TestDevice";
        deviceInfo.deviceType = "HSB1";
        deviceInfo.uniqueId = "AABBCCDDEEFF";
    }

    @Test
    void runCallsActionWithFullResultOnSuccess() throws Exception {
        var deviceStatus = new HueSyncDeviceDetailed();
        var hdmiStatus = new HueSyncHdmi();
        var execution = new HueSyncExecution();

        when(connection.getDetailedDeviceInfo()).thenReturn(deviceStatus);
        when(connection.getHdmiInfo()).thenReturn(hdmiStatus);
        when(connection.getExecutionInfo()).thenReturn(execution);

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertThat(received.size(), is(1));
        assertThat(received.get(0).deviceStatus, sameInstance(deviceStatus));
        assertThat(received.get(0).hdmiStatus, sameInstance(hdmiStatus));
        assertThat(received.get(0).execution, sameInstance(execution));
        verifyNoInteractions(exceptionHandler);
    }

    @Test
    void runDoesNotCallActionWhenFirstFetchFails() throws Exception {
        when(connection.getDetailedDeviceInfo()).thenThrow(new RuntimeException("timeout"));

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertTrue(received.isEmpty());
        verify(exceptionHandler).handle(any(Exception.class));
    }

    @Test
    void runDoesNotCallActionWhenSecondFetchFails() throws Exception {
        when(connection.getDetailedDeviceInfo()).thenReturn(new HueSyncDeviceDetailed());
        when(connection.getHdmiInfo()).thenThrow(new RuntimeException("network error"));

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertTrue(received.isEmpty());
        verify(exceptionHandler).handle(any(Exception.class));
    }

    @Test
    void runDoesNotCallActionWhenThirdFetchFails() throws Exception {
        when(connection.getDetailedDeviceInfo()).thenReturn(new HueSyncDeviceDetailed());
        when(connection.getHdmiInfo()).thenReturn(new HueSyncHdmi());
        when(connection.getExecutionInfo()).thenThrow(new RuntimeException("network error"));

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertTrue(received.isEmpty());
        verify(exceptionHandler).handle(any(Exception.class));
    }

    @Test
    void runCallsActionWithNullDeviceStatus() throws Exception {
        // getDetailedDeviceInfo() returns null — no exception, so the task still calls the consumer.
        // handleUpdate must then detect the null field and trigger the exception handler / recovery.
        when(connection.getDetailedDeviceInfo()).thenReturn(null);
        when(connection.getHdmiInfo()).thenReturn(new HueSyncHdmi());
        when(connection.getExecutionInfo()).thenReturn(new HueSyncExecution());

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertThat(received.size(), is(1));
        assertThat(received.get(0).deviceStatus, nullValue());
        assertThat(received.get(0).hdmiStatus, notNullValue());
        assertThat(received.get(0).execution, notNullValue());
        verifyNoInteractions(exceptionHandler);
    }

    @Test
    void runCallsActionWithNullHdmiStatus() throws Exception {
        when(connection.getDetailedDeviceInfo()).thenReturn(new HueSyncDeviceDetailed());
        when(connection.getHdmiInfo()).thenReturn(null);
        when(connection.getExecutionInfo()).thenReturn(new HueSyncExecution());

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertThat(received.size(), is(1));
        assertThat(received.get(0).deviceStatus, notNullValue());
        assertThat(received.get(0).hdmiStatus, nullValue());
        assertThat(received.get(0).execution, notNullValue());
        verifyNoInteractions(exceptionHandler);
    }

    @Test
    void runCallsActionWithNullExecution() throws Exception {
        when(connection.getDetailedDeviceInfo()).thenReturn(new HueSyncDeviceDetailed());
        when(connection.getHdmiInfo()).thenReturn(new HueSyncHdmi());
        when(connection.getExecutionInfo()).thenReturn(null);

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();

        assertThat(received.size(), is(1));
        assertThat(received.get(0).deviceStatus, notNullValue());
        assertThat(received.get(0).hdmiStatus, notNullValue());
        assertThat(received.get(0).execution, nullValue());
        verifyNoInteractions(exceptionHandler);
    }

    @Test
    void runCallsActionOnEachSuccessfulInvocation() throws Exception {
        when(connection.getDetailedDeviceInfo()).thenReturn(new HueSyncDeviceDetailed());
        when(connection.getHdmiInfo()).thenReturn(new HueSyncHdmi());
        when(connection.getExecutionInfo()).thenReturn(new HueSyncExecution());

        List<HueSyncUpdateTaskResult> received = new ArrayList<>();
        var task = new HueSyncUpdateTask(connection, deviceInfo, received::add, exceptionHandler);
        task.run();
        task.run();

        assertThat(received.size(), is(2));
        verifyNoInteractions(exceptionHandler);
    }
}
