/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.Callable;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Response.CompleteListener;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.LongPollingFailedException;
import org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * Unit tests for {@link LongPolling}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
class LongPollingTest {

    /**
     * A dummy implementation of {@link ScheduledFuture}.
     * <p>
     * This is required because we can not return <code>null</code> in the executor service test implementation (see
     * below).
     *
     * @author David Pace - Initial contribution
     *
     * @param <T> The result type returned by this Future
     */
    private static class NullScheduledFuture<T> implements ScheduledFuture<T> {

        @Override
        public long getDelay(@Nullable TimeUnit unit) {
            return 0;
        }

        @Override
        public int compareTo(@Nullable Delayed o) {
            return 0;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return false;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return null;
        }

        @Override
        public T get(long timeout, @Nullable TimeUnit unit)
                throws InterruptedException, ExecutionException, TimeoutException {
            return null;
        }
    }

    /**
     * Executor service implementation that runs all runnables in the same thread in order to enable deterministic
     * testing.
     *
     * @author David Pace - Initial contribution
     *
     */
    private static class SameThreadExecutorService extends AbstractExecutorService implements ScheduledExecutorService {

        private volatile boolean terminated;

        @Override
        public void shutdown() {
            terminated = true;
        }

        @NonNullByDefault({})
        @Override
        public List<Runnable> shutdownNow() {
            return Collections.emptyList();
        }

        @Override
        public boolean isShutdown() {
            return terminated;
        }

        @Override
        public boolean isTerminated() {
            return terminated;
        }

        @Override
        public boolean awaitTermination(long timeout, @Nullable TimeUnit unit) throws InterruptedException {
            shutdown();
            return terminated;
        }

        @Override
        public void execute(@Nullable Runnable command) {
            if (command != null) {
                // execute in the same thread in unit tests
                command.run();
            }
        }

        @Override
        public ScheduledFuture<?> schedule(@Nullable Runnable command, long delay, @Nullable TimeUnit unit) {
            // not used in this tests
            return new NullScheduledFuture<>();
        }

        @Override
        public <V> ScheduledFuture<V> schedule(@Nullable Callable<V> callable, long delay, @Nullable TimeUnit unit) {
            return new NullScheduledFuture<>();
        }

        @Override
        public ScheduledFuture<?> scheduleAtFixedRate(@Nullable Runnable command, long initialDelay, long period,
                @Nullable TimeUnit unit) {
            if (command != null) {
                command.run();
            }
            return new NullScheduledFuture<>();
        }

        @Override
        public ScheduledFuture<?> scheduleWithFixedDelay(@Nullable Runnable command, long initialDelay, long delay,
                @Nullable TimeUnit unit) {
            if (command != null) {
                command.run();
            }
            return new NullScheduledFuture<>();
        }
    }

    private @NonNullByDefault({}) LongPolling fixture;

    private @NonNullByDefault({}) BoschHttpClient httpClient;

    private @Mock @NonNullByDefault({}) Consumer<@NonNull LongPollResult> longPollHandler;

    private @Mock @NonNullByDefault({}) Consumer<@NonNull Throwable> failureHandler;

    @BeforeEach
    void beforeEach() {
        fixture = new LongPolling(new SameThreadExecutorService(), longPollHandler, failureHandler);
        httpClient = mock(BoschHttpClient.class);
    }

    @Test
    void start() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        // when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request subscribeRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/subscribe".equals(r.method)))).thenReturn(subscribeRequest);
        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        fixture.start(httpClient);

        ArgumentCaptor<CompleteListener> completeListener = ArgumentCaptor.forClass(CompleteListener.class);
        verify(longPollRequest).send(completeListener.capture());

        BufferingResponseListener bufferingResponseListener = (BufferingResponseListener) completeListener.getValue();

        String longPollResultJSON = "{\"result\":[{\"path\":\"/devices/hdm:HomeMaticIP:3014F711A0001916D859A8A9/services/PowerSwitch\",\"@type\":\"DeviceServiceData\",\"id\":\"PowerSwitch\",\"state\":{\"@type\":\"powerSwitchState\",\"switchState\":\"ON\"},\"deviceId\":\"hdm:HomeMaticIP:3014F711A0001916D859A8A9\"}],\"jsonrpc\":\"2.0\"}\n";
        Response response = mock(Response.class);
        bufferingResponseListener.onContent(response,
                ByteBuffer.wrap(longPollResultJSON.getBytes(StandardCharsets.UTF_8)));

        Result result = mock(Result.class);
        bufferingResponseListener.onComplete(result);

        ArgumentCaptor<LongPollResult> longPollResultCaptor = ArgumentCaptor.forClass(LongPollResult.class);
        verify(longPollHandler).accept(longPollResultCaptor.capture());
        LongPollResult longPollResult = longPollResultCaptor.getValue();
        assertEquals(1, longPollResult.result.size());
        assertEquals(longPollResult.result.get(0).getClass(), DeviceServiceData.class);
        DeviceServiceData longPollResultItem = (DeviceServiceData) longPollResult.result.get(0);
        assertEquals("hdm:HomeMaticIP:3014F711A0001916D859A8A9", longPollResultItem.deviceId);
        assertEquals("/devices/hdm:HomeMaticIP:3014F711A0001916D859A8A9/services/PowerSwitch", longPollResultItem.path);
        assertEquals("PowerSwitch", longPollResultItem.id);
        JsonObject stateObject = (JsonObject) longPollResultItem.state;
        assertNotNull(stateObject);
        assertEquals("ON", stateObject.get("switchState").getAsString());
    }

    @Test
    void startLongPollingReceiveScenario()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request subscribeRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/subscribe".equals(r.method)))).thenReturn(subscribeRequest);
        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        fixture.start(httpClient);

        ArgumentCaptor<CompleteListener> completeListener = ArgumentCaptor.forClass(CompleteListener.class);
        verify(longPollRequest).send(completeListener.capture());

        BufferingResponseListener bufferingResponseListener = (BufferingResponseListener) completeListener.getValue();

        String longPollResultJSON = "{\"result\":[{\"@type\": \"scenarioTriggered\",\"name\": \"My scenario\",\"id\": \"509bd737-eed0-40b7-8caa-e8686a714399\",\"lastTimeTriggered\": \"1693758693032\"}],\"jsonrpc\":\"2.0\"}\n";
        Response response = mock(Response.class);
        bufferingResponseListener.onContent(response,
                ByteBuffer.wrap(longPollResultJSON.getBytes(StandardCharsets.UTF_8)));

        Result result = mock(Result.class);
        bufferingResponseListener.onComplete(result);

        ArgumentCaptor<LongPollResult> longPollResultCaptor = ArgumentCaptor.forClass(LongPollResult.class);
        verify(longPollHandler).accept(longPollResultCaptor.capture());
        LongPollResult longPollResult = longPollResultCaptor.getValue();
        assertEquals(1, longPollResult.result.size());
        assertEquals(longPollResult.result.get(0).getClass(), Scenario.class);
        Scenario longPollResultItem = (Scenario) longPollResult.result.get(0);
        assertEquals("509bd737-eed0-40b7-8caa-e8686a714399", longPollResultItem.id);
        assertEquals("My scenario", longPollResultItem.name);
        assertEquals("1693758693032", longPollResultItem.lastTimeTriggered);
    }

    @Test
    void startLongPollingReceiveUserDefinedState()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request subscribeRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/subscribe".equals(r.method)))).thenReturn(subscribeRequest);
        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        fixture.start(httpClient);

        ArgumentCaptor<CompleteListener> completeListener = ArgumentCaptor.forClass(CompleteListener.class);
        verify(longPollRequest).send(completeListener.capture());

        BufferingResponseListener bufferingResponseListener = (BufferingResponseListener) completeListener.getValue();

        String longPollResultJSON = "{\"result\":[{\"deleted\":false,\"@type\":\"userDefinedState\",\"name\":\"My User state\",\"id\":\"23d34fa6-382a-444d-8aae-89c706e22155\",\"state\":true}],\"jsonrpc\":\"2.0\"}\n";
        Response response = mock(Response.class);
        bufferingResponseListener.onContent(response,
                ByteBuffer.wrap(longPollResultJSON.getBytes(StandardCharsets.UTF_8)));

        Result result = mock(Result.class);
        bufferingResponseListener.onComplete(result);

        ArgumentCaptor<LongPollResult> longPollResultCaptor = ArgumentCaptor.forClass(LongPollResult.class);
        verify(longPollHandler).accept(longPollResultCaptor.capture());
        LongPollResult longPollResult = longPollResultCaptor.getValue();
        assertEquals(1, longPollResult.result.size());
        assertEquals(longPollResult.result.get(0).getClass(), UserDefinedState.class);
        UserDefinedState longPollResultItem = (UserDefinedState) longPollResult.result.get(0);
        assertEquals("23d34fa6-382a-444d-8aae-89c706e22155", longPollResultItem.getId());
        assertEquals("My User state", longPollResultItem.getName());
        assertTrue(longPollResultItem.isState());
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getBoschShcAndExecutionAndTimeoutAndInterruptedExceptionArguments()")
    void startSubscriptionFailureHandleExceptions(Exception exception)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenThrow(exception);

        LongPollingFailedException e = assertThrows(LongPollingFailedException.class, () -> fixture.start(httpClient));
        assertThat(e.getCause(), instanceOf(exception.getClass()));
        assertThat(e.getMessage(), containsString(CommonTestUtils.TEST_EXCEPTION_MESSAGE));
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExceutionExceptionAndRuntimeExceptionArguments()")
    void startLongPollFailure(Exception exception)
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST), any(JsonRpcRequest.class)))
                .thenReturn(request);
        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        fixture.start(httpClient);

        ArgumentCaptor<CompleteListener> completeListener = ArgumentCaptor.forClass(CompleteListener.class);
        verify(longPollRequest).send(completeListener.capture());

        BufferingResponseListener bufferingResponseListener = (BufferingResponseListener) completeListener.getValue();

        Result result = mock(Result.class);
        when(result.getFailure()).thenReturn(exception);
        bufferingResponseListener.onComplete(result);

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(failureHandler).accept(throwableCaptor.capture());
        Throwable t = throwableCaptor.getValue();
        assertEquals("Unexpected exception during long polling request", t.getMessage());
        assertSame(exception, t.getCause());
    }

    @Test
    void startSubscriptionInvalid()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request subscribeRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/subscribe".equals(r.method)))).thenReturn(subscribeRequest);
        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        fixture.start(httpClient);

        ArgumentCaptor<CompleteListener> completeListener = ArgumentCaptor.forClass(CompleteListener.class);
        verify(longPollRequest).send(completeListener.capture());

        BufferingResponseListener bufferingResponseListener = (BufferingResponseListener) completeListener.getValue();

        String longPollResultJSON = "{\"jsonrpc\":\"2.0\",\"error\": {\"code\":-32001,\"message\":\"No subscription with id: e8fei62b0-0\"}}\n";
        Response response = mock(Response.class);
        bufferingResponseListener.onContent(response,
                ByteBuffer.wrap(longPollResultJSON.getBytes(StandardCharsets.UTF_8)));

        Result result = mock(Result.class);
        bufferingResponseListener.onComplete(result);
    }

    /**
     * Tests a case in which the Smart Home Controller returns a HTML error response that is not parsable as JSON.
     * <p>
     * See <a href="https://github.com/openhab/openhab-addons/issues/15912">Issue 15912</a>
     */
    @Test
    void startLongPollingInvalidLongPollResponse()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request subscribeRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/subscribe".equals(r.method)))).thenReturn(subscribeRequest);
        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        fixture.start(httpClient);

        ArgumentCaptor<CompleteListener> completeListener = ArgumentCaptor.forClass(CompleteListener.class);
        verify(longPollRequest).send(completeListener.capture());

        BufferingResponseListener bufferingResponseListener = (BufferingResponseListener) completeListener.getValue();

        String longPollResultContent = "<HTML><HEAD><TITLE>400</TITLE></HEAD><BODY><H1>400 Unsupported HTTP Protocol Version: /remote/json-rpcHTTP/1.1</H1></BODY></HTML>";
        Response response = mock(Response.class);
        bufferingResponseListener.onContent(response,
                ByteBuffer.wrap(longPollResultContent.getBytes(StandardCharsets.UTF_8)));

        Result result = mock(Result.class);
        bufferingResponseListener.onComplete(result);

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(failureHandler).accept(throwableCaptor.capture());
        Throwable t = throwableCaptor.getValue();
        assertThat(t.getMessage(), is(
                "Could not deserialize long poll response: '<HTML><HEAD><TITLE>400</TITLE></HEAD><BODY><H1>400 Unsupported HTTP Protocol Version: /remote/json-rpcHTTP/1.1</H1></BODY></HTML>'"));
        assertThat(t.getCause(), instanceOf(JsonSyntaxException.class));
    }

    @Test
    void testHandleLongPollResponseNPE() {
        doThrow(NullPointerException.class).when(longPollHandler).accept(any());

        var resultJson = """
                {
                    "result": [
                        {
                            "@type": "DeviceServiceData",
                            "deleted": true,
                            "id": "CommunicationQuality",
                            "deviceId": "hdm:ZigBee:30fb10fffe46d732"
                        }
                    ],
                    "jsonrpc":"2.0"
                }
                """;
        fixture.handleLongPollResponse(httpClient, "subscriptionId", resultJson);

        ArgumentCaptor<Throwable> throwableCaptor = ArgumentCaptor.forClass(Throwable.class);
        verify(failureHandler).accept(throwableCaptor.capture());
        Throwable t = throwableCaptor.getValue();
        assertThat(t.getMessage(), is("Error while handling long poll response: '" + resultJson + "'"));
        assertThat(t.getCause(), instanceOf(NullPointerException.class));
    }

    @AfterEach
    void afterEach() {
        fixture.stop();
    }
}
