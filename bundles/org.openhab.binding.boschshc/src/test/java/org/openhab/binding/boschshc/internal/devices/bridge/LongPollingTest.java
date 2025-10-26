/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.exceptions.LongPollingFailedException;
import org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils;
import org.openhab.core.util.SameThreadExecutorService;

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

    private @NonNullByDefault({}) LongPolling fixture;

    private @NonNullByDefault({}) BoschHttpClient httpClient;

    private @NonNullByDefault({}) Consumer<@NonNull LongPollResult> longPollHandler;

    private @NonNullByDefault({}) Consumer<@NonNull Throwable> failureHandler;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void beforeEach() {
        httpClient = mock(BoschHttpClient.class);
        longPollHandler = mock(Consumer.class);
        failureHandler = mock(Consumer.class);
        fixture = new LongPolling(new SameThreadExecutorService(), longPollHandler, failureHandler);
    }

    @Test
    void start() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
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
