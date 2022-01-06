/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mielecloud.internal.webservice;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openhab.binding.mielecloud.internal.util.MockUtil;
import org.openhab.binding.mielecloud.internal.webservice.api.DeviceState;
import org.openhab.binding.mielecloud.internal.webservice.api.json.StateType;
import org.openhab.binding.mielecloud.internal.webservice.exception.AuthorizationFailedException;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceException;
import org.openhab.binding.mielecloud.internal.webservice.exception.TooManyRequestsException;

/**
 * @author Björn Lange - Initial Contribution
 */
@NonNullByDefault
public class ActionStateFetcherTest {
    private ScheduledExecutorService mockImmediatelyExecutingExecutorService() {
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        when(scheduler.submit(ArgumentMatchers.<Runnable> any()))
                .thenAnswer(new Answer<@Nullable ScheduledFuture<?>>() {
                    @Override
                    @Nullable
                    public ScheduledFuture<?> answer(@Nullable InvocationOnMock invocation) throws Throwable {
                        ((Runnable) MockUtil.requireNonNull(invocation).getArgument(0)).run();
                        return null;
                    }
                });
        return scheduler;
    }

    @Test
    public void testFetchActionsIsInvokedWhenInitialDeviceStateIsSet() {
        // given:
        ScheduledExecutorService scheduler = mockImmediatelyExecutingExecutorService();

        MieleWebservice webservice = mock(MieleWebservice.class);
        DeviceState deviceState = mock(DeviceState.class);
        DeviceState newDeviceState = mock(DeviceState.class);
        ActionStateFetcher actionsfetcher = new ActionStateFetcher(() -> webservice, scheduler);

        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(newDeviceState.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));

        // when:
        actionsfetcher.onDeviceStateUpdated(deviceState);

        // then:
        verify(webservice).fetchActions(any());
    }

    @Test
    public void testFetchActionsIsInvokedOnStateTransition() {
        // given:
        ScheduledExecutorService scheduler = mockImmediatelyExecutingExecutorService();

        MieleWebservice webservice = mock(MieleWebservice.class);
        DeviceState deviceState = mock(DeviceState.class);
        DeviceState newDeviceState = mock(DeviceState.class);
        ActionStateFetcher actionsfetcher = new ActionStateFetcher(() -> webservice, scheduler);

        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(newDeviceState.getStateType()).thenReturn(Optional.of(StateType.END_PROGRAMMED));

        actionsfetcher.onDeviceStateUpdated(deviceState);

        // when:
        actionsfetcher.onDeviceStateUpdated(newDeviceState);

        // then:
        verify(webservice, times(2)).fetchActions(any());
    }

    @Test
    public void testFetchActionsIsNotInvokedWhenNoStateTransitionOccurrs() {
        // given:
        ScheduledExecutorService scheduler = mockImmediatelyExecutingExecutorService();

        MieleWebservice webservice = mock(MieleWebservice.class);
        DeviceState deviceState = mock(DeviceState.class);
        DeviceState newDeviceState = mock(DeviceState.class);
        ActionStateFetcher actionsfetcher = new ActionStateFetcher(() -> webservice, scheduler);

        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));
        when(newDeviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        actionsfetcher.onDeviceStateUpdated(deviceState);

        // when:
        actionsfetcher.onDeviceStateUpdated(newDeviceState);

        // then:
        verify(webservice, times(1)).fetchActions(any());
    }

    @Test
    public void whenFetchActionsFailsWithAMieleWebserviceExceptionThenNoExceptionIsThrown() {
        // given:
        ScheduledExecutorService scheduler = mockImmediatelyExecutingExecutorService();

        MieleWebservice webservice = mock(MieleWebservice.class);
        doThrow(new MieleWebserviceException("It went wrong", ConnectionError.REQUEST_EXECUTION_FAILED))
                .when(webservice).fetchActions(any());

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        ActionStateFetcher actionsfetcher = new ActionStateFetcher(() -> webservice, scheduler);

        // when:
        actionsfetcher.onDeviceStateUpdated(deviceState);

        // then:
        verify(webservice, times(1)).fetchActions(any());
    }

    @Test
    public void whenFetchActionsFailsWithAnAuthorizationFailedExceptionThenNoExceptionIsThrown() {
        // given:
        ScheduledExecutorService scheduler = mockImmediatelyExecutingExecutorService();

        MieleWebservice webservice = mock(MieleWebservice.class);
        doThrow(new AuthorizationFailedException("Authorization failed")).when(webservice).fetchActions(any());

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        ActionStateFetcher actionsfetcher = new ActionStateFetcher(() -> webservice, scheduler);

        // when:
        actionsfetcher.onDeviceStateUpdated(deviceState);

        // then:
        verify(webservice, times(1)).fetchActions(any());
    }

    @Test
    public void whenFetchActionsFailsWithATooManyRequestsExceptionThenNoExceptionIsThrown() {
        // given:
        ScheduledExecutorService scheduler = mockImmediatelyExecutingExecutorService();

        MieleWebservice webservice = mock(MieleWebservice.class);
        doThrow(new TooManyRequestsException("Too many requests", null)).when(webservice).fetchActions(any());

        DeviceState deviceState = mock(DeviceState.class);
        when(deviceState.getStateType()).thenReturn(Optional.of(StateType.RUNNING));

        ActionStateFetcher actionsfetcher = new ActionStateFetcher(() -> webservice, scheduler);

        // when:
        actionsfetcher.onDeviceStateUpdated(deviceState);

        // then:
        verify(webservice, times(1)).fetchActions(any());
    }
}
