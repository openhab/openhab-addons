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
package org.openhab.binding.mielecloud.internal.webservice.sse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.mielecloud.internal.util.ReflectionUtil.*;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Response.HeadersListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpFields;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.openhab.binding.mielecloud.internal.webservice.ConnectionError;
import org.openhab.binding.mielecloud.internal.webservice.exception.MieleWebserviceDisconnectSseException;
import org.openhab.binding.mielecloud.internal.webservice.retry.AuthorizationFailedRetryStrategy;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class SseConnectionTest {
    private final String URL = "https://openhab.org/";

    @Nullable
    private Request request;

    @Nullable
    private SseRequestFactory sseRequestFactory;

    @Nullable
    private ScheduledExecutorService scheduler;

    @Nullable
    private BackoffStrategy backoffStrategy;

    @Nullable
    private SseListener sseListener;

    @Nullable
    private SseConnection sseConnection;

    @Nullable
    private HeadersListener registeredHeadersListener;

    @Nullable
    private CompleteListener registeredCompleteListener;

    private SseRequestFactory mockSseRequestFactory(@Nullable Request request) {
        SseRequestFactory factory = mock(SseRequestFactory.class);
        when(factory.createSseRequest(URL)).thenReturn(request);
        return factory;
    }

    private ScheduledExecutorService mockScheduler() {
        return mock(ScheduledExecutorService.class);
    }

    private Request mockRequest() {
        Request request = mock(Request.class);
        when(request.onResponseHeaders(any())).thenAnswer(invocation -> {
            registeredHeadersListener = invocation.getArgument(0);
            return request;
        });
        when(request.onComplete(any())).thenAnswer(invocation -> {
            registeredCompleteListener = invocation.getArgument(0);
            return request;
        });
        when(request.idleTimeout(anyLong(), any())).thenReturn(request);
        when(request.timeout(anyLong(), any())).thenReturn(request);
        return request;
    }

    private BackoffStrategy mockBackoffStrategy() {
        BackoffStrategy backoffStrategy = mock(BackoffStrategy.class);
        when(backoffStrategy.getSecondsUntilRetry(anyInt())).thenReturn(10L);
        when(backoffStrategy.getMinimumSecondsUntilRetry()).thenReturn(5L);
        when(backoffStrategy.getMaximumSecondsUntilRetry()).thenReturn(3600L);
        return backoffStrategy;
    }

    private void setUpRunningConnection() {
        request = mockRequest();
        sseRequestFactory = mockSseRequestFactory(request);
        scheduler = mockScheduler();
        backoffStrategy = mockBackoffStrategy();
        sseConnection = new SseConnection(URL, getMockedSseRequestFactory(), getMockedScheduler(),
                getMockedBackoffStrategy());

        sseListener = mock(SseListener.class);
        getSseConnection().addSseListener(getMockedSseListener());
        getSseConnection().connect();

        getRegisteredHeadersListener().onHeaders(null);
    }

    private Request getMockedRequest() {
        Request request = this.request;
        assertNotNull(request);
        return Objects.requireNonNull(request);
    }

    private SseRequestFactory getMockedSseRequestFactory() {
        SseRequestFactory sseRequestFactory = this.sseRequestFactory;
        assertNotNull(sseRequestFactory);
        return Objects.requireNonNull(sseRequestFactory);
    }

    private ScheduledExecutorService getMockedScheduler() {
        ScheduledExecutorService scheduler = this.scheduler;
        assertNotNull(scheduler);
        return Objects.requireNonNull(scheduler);
    }

    private BackoffStrategy getMockedBackoffStrategy() {
        BackoffStrategy backoffStrategy = this.backoffStrategy;
        assertNotNull(backoffStrategy);
        return Objects.requireNonNull(backoffStrategy);
    }

    private SseListener getMockedSseListener() {
        SseListener sseListener = this.sseListener;
        assertNotNull(sseListener);
        return Objects.requireNonNull(sseListener);
    }

    private SseConnection getSseConnection() {
        SseConnection sseConnection = this.sseConnection;
        assertNotNull(sseConnection);
        return Objects.requireNonNull(sseConnection);
    }

    private HeadersListener getRegisteredHeadersListener() {
        HeadersListener headersListener = registeredHeadersListener;
        assertNotNull(headersListener);
        return Objects.requireNonNull(headersListener);
    }

    private CompleteListener getRegisteredCompleteListener() {
        CompleteListener completeListener = registeredCompleteListener;
        assertNotNull(completeListener);
        return Objects.requireNonNull(completeListener);
    }

    @Test
    public void whenSseConnectionIsConnectedThenTheConnectionRequestIsMade() throws Exception {
        // given:
        Request request = mockRequest();
        SseRequestFactory sseRequestFactory = mockSseRequestFactory(request);
        ScheduledExecutorService scheduler = mockScheduler();
        SseConnection sseConnection = new SseConnection(URL, sseRequestFactory, scheduler);

        // when:
        sseConnection.connect();

        // then:
        verify(request).send(any());
    }

    @Test
    public void whenSseConnectionIsConnectedButNoRequestIsCreatedThenOnlyTheDesiredConnectionStateChanges()
            throws Exception {
        // given:
        SseRequestFactory sseRequestFactory = mockSseRequestFactory(null);
        ScheduledExecutorService scheduler = mockScheduler();
        SseConnection sseConnection = new SseConnection(URL, sseRequestFactory, scheduler);

        // when:
        sseConnection.connect();

        // then:
        assertTrue(((Boolean) getPrivate(sseConnection, "active")).booleanValue());
    }

    @Test
    public void whenHeadersAreReceivedAfterTheSseConnectionWasConnectedThenTheEventStreamParserIsScheduled()
            throws Exception {
        // given:
        Request request = mockRequest();
        SseRequestFactory sseRequestFactory = mockSseRequestFactory(request);
        ScheduledExecutorService scheduler = mockScheduler();
        SseConnection sseConnection = new SseConnection(URL, sseRequestFactory, scheduler);
        sseConnection.connect();
        HeadersListener headersListener = registeredHeadersListener;
        assertNotNull(headersListener);

        // when:
        headersListener.onHeaders(null);

        // then:
        verify(scheduler).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
    }

    @Test
    public void whenTheSseStreamIsClosedWithATimeoutThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        // when:
        invokePrivate(getSseConnection(), "onSseStreamClosed", new Class[] { Throwable.class }, new TimeoutException());

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.TIMEOUT, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenTheSseStreamIsClosedDueToAJetty401ErrorThenNoReconnectIsScheduledAndATokenRefreshIsRequested()
            throws Exception {
        // given:
        setUpRunningConnection();

        // when:
        invokePrivate(getSseConnection(), "onSseStreamClosed", new Class[] { Throwable.class }, new RuntimeException(
                AuthorizationFailedRetryStrategy.JETTY_401_HEADER_BODY_MISMATCH_EXCEPTION_MESSAGE));

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verifyNoMoreInteractions(getMockedScheduler());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.AUTHORIZATION_FAILED, 0);
    }

    @Test
    public void whenTheSseStreamIsClosedWithADifferentExceptionThanATimeoutThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        // when:
        invokePrivate(getSseConnection(), "onSseStreamClosed", new Class[] { Throwable.class },
                new IllegalStateException());

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SSE_STREAM_ENDED, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenTheSseRequestCompletesWithoutResultThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        // when:
        getRegisteredCompleteListener().onComplete(null);

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SSE_STREAM_ENDED, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenTheSseRequestCompletesWithoutResponseThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Result result = mock(Result.class);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SSE_STREAM_ENDED, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenTheSseRequestCompletesWithASuccessfulResponseThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(200);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SSE_STREAM_ENDED, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenTheSseRequestCompletesWithAnAuthorizationFailedResponseThenTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(401);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.AUTHORIZATION_FAILED, 0);
    }

    @Test
    public void whenTheSseRequestCompletesWithATooManyRequestsResponseWithoutRetryAfterHeaderThenAReconnectIsScheduledAccordingToTheBackoffStrategyAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(429);
        when(response.getHeaders()).thenReturn(new HttpFields());

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), eq(10L), eq(TimeUnit.SECONDS));
        verify(getMockedSseListener()).onConnectionError(ConnectionError.TOO_MANY_RERQUESTS, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenTheSseRequestCompletesWithATooManyRequestsResponseWithRetryAfterHeaderThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(429);
        HttpFields httpFields = new HttpFields();
        httpFields.add("Retry-After", "3600");
        when(response.getHeaders()).thenReturn(httpFields);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), eq(3600L), eq(TimeUnit.SECONDS));
        verify(getMockedSseListener()).onConnectionError(ConnectionError.TOO_MANY_RERQUESTS, 0);
    }

    @Test
    public void whenTheSseRequestCompletesWithATooManyRequestsResponseWithRetryAfterHeaderWithTooLowValueThenAReconnectIsScheduledWithTheMinimumWaitTime()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(429);
        HttpFields httpFields = new HttpFields();
        httpFields.add("Retry-After", "1");
        when(response.getHeaders()).thenReturn(httpFields);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), eq(5L), eq(TimeUnit.SECONDS));
        verify(getMockedSseListener()).onConnectionError(ConnectionError.TOO_MANY_RERQUESTS, 0);
    }

    @Test
    public void whenTheSseRequestCompletesWithATooManyRequestsResponseWithRetryAfterHeaderWithTooHighValueThenAReconnectIsScheduledWithTheMaximumWaitTime()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(429);
        HttpFields httpFields = new HttpFields();
        httpFields.add("Retry-After", "3601");
        when(response.getHeaders()).thenReturn(httpFields);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), eq(3600L), eq(TimeUnit.SECONDS));
        verify(getMockedSseListener()).onConnectionError(ConnectionError.TOO_MANY_RERQUESTS, 0);
    }

    @Test
    public void whenTheSseRequestCompletesWithAnInternalServerErrorResponseThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SERVER_ERROR, 0);
    }

    @Test
    public void whenTheSseRequestCompletesWithAnInternalServerErrorResponseMultipleTimesThenTheConnectionFailedCounterIsIncrementedEachTime()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(500);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SERVER_ERROR, 0);
        verify(getMockedSseListener()).onConnectionError(ConnectionError.SERVER_ERROR, 1);
    }

    @Test
    public void whenTheSseRequestCompletesWithAnUnknownErrorResponseThenAReconnectIsScheduledAndTheListenersAreNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        Response response = mock(Response.class);
        when(response.getStatus()).thenReturn(600);

        Result result = mock(Result.class);
        when(result.getResponse()).thenReturn(response);

        // when:
        getRegisteredCompleteListener().onComplete(result);

        // then:
        verify(getMockedScheduler(), times(2)).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verify(getMockedSseListener()).onConnectionError(ConnectionError.OTHER_HTTP_ERROR, 0);
        verify(getMockedBackoffStrategy()).getSecondsUntilRetry(anyInt());
    }

    @Test
    public void whenAServerSentEventIsReceivedThenItIsForwardedToTheListenersAndTheFailedConnectionCounterIsReset()
            throws Exception {
        // given:
        Request request = mockRequest();
        SseRequestFactory sseRequestFactory = mockSseRequestFactory(request);
        ScheduledExecutorService scheduler = mockScheduler();

        BackoffStrategy backoffStrategy = mock(BackoffStrategy.class);
        when(backoffStrategy.getSecondsUntilRetry(anyInt())).thenReturn(10L);

        SseConnection sseConnection = new SseConnection(URL, sseRequestFactory, scheduler, backoffStrategy);
        SseListener sseListener = mock(SseListener.class);
        sseConnection.addSseListener(sseListener);
        setPrivate(sseConnection, "failedConnectionAttempts", 10);
        sseConnection.connect();

        HeadersListener headersListener = registeredHeadersListener;
        assertNotNull(headersListener);
        headersListener.onHeaders(null);

        ServerSentEvent serverSentEvent = new ServerSentEvent("ping", "ping");

        // when:
        invokePrivate(sseConnection, "onServerSentEvent", serverSentEvent);

        // then:
        verify(sseListener).onServerSentEvent(serverSentEvent);
        assertEquals(0, (int) getPrivate(sseConnection, "failedConnectionAttempts"));
    }

    @Test
    public void whenTheSseStreamIsDisconnectedThenTheRunningRequestIsAborted() throws Exception {
        // given:
        setUpRunningConnection();

        // when:
        getSseConnection().disconnect();

        // then:
        verify(getMockedRequest()).abort(any());
        assertNull(getPrivate(getSseConnection(), "sseRequest"));
    }

    @Test
    public void whenTheSseStreamIsDisconnectedThenTheConnectionIsClosedAndNoReconnectIsScheduledAndTheListenersAreNotNotified()
            throws Exception {
        // given:
        setUpRunningConnection();

        // when:
        getSseConnection().disconnect();
        invokePrivate(getSseConnection(), "onSseStreamClosed", new Class[] { Throwable.class },
                new MieleWebserviceDisconnectSseException());

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verifyNoMoreInteractions(getMockedScheduler());
        verifyNoInteractions(getMockedSseListener());
    }

    @Test
    public void whenAPendingReconnectAttemptIsPerformedAfterTheSseConnectionWasDisconnectedThenTheConnectionIsNotRestored()
            throws Exception {
        // given:
        setUpRunningConnection();
        getSseConnection().disconnect();

        // when:
        invokePrivate(getSseConnection(), "connectInternal");

        // then:
        verify(getMockedScheduler()).schedule(ArgumentMatchers.<Runnable> any(), anyLong(), any());
        verifyNoMoreInteractions(getMockedScheduler());
        verifyNoInteractions(getMockedSseListener());
    }

    @Test
    public void whenTheSseConnectionIsConnectedMultipleTimesWithoutDisconnectingThenOnlyTheFirstConnectResultsInAnConnectionAttempt()
            throws Exception {
        // given:
        Request request = mockRequest();
        SseRequestFactory sseRequestFactory = mockSseRequestFactory(request);
        ScheduledExecutorService scheduler = mockScheduler();
        SseConnection sseConnection = new SseConnection(URL, sseRequestFactory, scheduler);
        sseConnection.connect();

        // when:
        sseConnection.connect();

        // then:
        verify(request, times(1)).onResponseHeaders(any());
    }

    @Test
    public void whenTheSseConnectionIsDisconnectedMultipleTimesWithoutConnectingAgainThenOnlyTheFirstDisconnectIsPerformed()
            throws Exception {
        // given:
        Request request = mockRequest();
        SseRequestFactory sseRequestFactory = mockSseRequestFactory(request);
        ScheduledExecutorService scheduler = mockScheduler();
        SseConnection sseConnection = new SseConnection(URL, sseRequestFactory, scheduler);
        sseConnection.connect();
        sseConnection.disconnect();

        // when:
        sseConnection.disconnect();

        // then:
        verify(request, times(1)).abort(any());
    }
}
