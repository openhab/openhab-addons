/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.blink.internal.service;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.openhab.binding.blink.internal.BlinkTestUtil;
import org.openhab.binding.blink.internal.dto.BlinkAccount;
import org.openhab.binding.blink.internal.dto.BlinkCommandResponse;

import com.google.gson.Gson;

/**
 * Test class.
 *
 * @author Matthias Oesterheld - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.WARN)
@NonNullByDefault
class BaseBlinkApiServiceTest {

    @Mock
    @NonNullByDefault({})
    HttpClient httpClient;
    @Spy
    Gson gson = new Gson();
    @Mock
    @NonNullByDefault({})
    Request request;
    @Mock
    @NonNullByDefault({})
    ContentResponse response;

    @NonNullByDefault({})
    BaseBlinkApiService apiService;

    @BeforeEach
    void setup() {
        apiService = spy(new BaseBlinkApiService(httpClient, gson));
        doReturn(request).when(httpClient).newRequest(anyString());
        doReturn(request).when(request).method(anyString());
        doReturn(request).when(request).header(ArgumentMatchers.any(HttpHeader.class), anyString());
    }

    @Test
    void testUrlConstructionAndReturn() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(200);
        String expected = "resultString";
        when(response.getContentAsString()).thenReturn(expected);
        doReturn(response).when(request).send();
        String result = apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null);
        verify(httpClient).newRequest("https://rest-abc.immedia-semi.com/api/v1/hurz");
        assertThat(result, is(expected));
    }

    @Test
    void testRawReturn() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(200);
        byte[] expected = "resultString".getBytes(StandardCharsets.UTF_8);
        when(response.getContent()).thenReturn(expected);
        doReturn(response).when(request).send();
        byte[] result = apiService.rawRequest("abc", "/api/v1/hurz", HttpMethod.GET, null, null);
        assertThat(result, is(expected));
    }

    @Test
    void testHeaderAndParamsSet() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        Map<String, String> params = Map.of("p1", "v1", "p2", "v2", "p3", "v3");
        doReturn(request).when(request).header(anyString(), anyString());
        doReturn(request).when(request).param(anyString(), anyString());
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("");
        doReturn(response).when(request).send();
        apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, "mytoken", params);
        verify(request).header(HttpHeader.ACCEPT, "application/json; charset=UTF-8");
        verify(request).header("token-auth", "mytoken");
        verify(request, times(params.size())).param(anyString(), anyString());
        IntStream.range(1, 4).forEach(i -> verify(request).param("p" + i, "v" + i));
    }

    @Test
    @SuppressWarnings("null")
    void testExceptionOnStatusNonEqual200() throws ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(500);
        doReturn(response).when(request).send();
        IOException exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getMessage(), is("Blink API Call unsuccessful <Status 500>"));
    }

    @Test
    @SuppressWarnings("null")
    void testExceptionOnErrorInSend() throws ExecutionException, InterruptedException, TimeoutException {
        IOException exception;
        doThrow(InterruptedException.class).when(request).send();
        exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getCause(), is(notNullValue()));
        // assertThat(exception.getCause().getClass(), is(InterruptedException.class));
        // would like to check if cause is a InterruptedException.class, but I'm in @NotNull @Nullable hell because of
        // assertThrows
        doThrow(TimeoutException.class).when(request).send();
        exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getCause(), is(notNullValue()));
        doThrow(ExecutionException.class).when(request).send();
        exception = assertThrows(IOException.class,
                () -> apiService.request("abc", "/api/v1/hurz", HttpMethod.GET, null, null));
        assertThat(exception.getCause(), is(notNullValue()));
    }

    static class SimpleClass {
        @Nullable
        public String iam;
        public int age;
    }

    @Test
    void testJsonDeserialization() throws IOException, ExecutionException, InterruptedException, TimeoutException {
        when(response.getStatus()).thenReturn(200);
        String jsonString = "{ 'iam' : 'old', 'age' : 90 }";
        when(response.getContentAsString()).thenReturn(jsonString);
        doReturn(response).when(request).send();
        SimpleClass result = apiService.apiRequest("abc", "api/v1/hurz", HttpMethod.GET, null, null, SimpleClass.class);
        verify(gson).fromJson(jsonString, SimpleClass.class);
        // noinspection ConstantConditions
        assertThat(result.iam, is(notNullValue()));
        assertThat(result.iam, is("old"));
        assertThat(result.age, is(90));
    }

    @Test
    void testWatchCommandStatusSuccess() throws IOException {
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        long networkId = 1L;
        long commandId = 2L;
        String expectedUri = "/network/" + networkId + "/command/" + commandId;
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        Future<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).schedule(ArgumentMatchers.any(Runnable.class), anyLong(), any());
        BlinkCommandResponse cmdResponse = new BlinkCommandResponse();
        cmdResponse.complete = true;
        doReturn(cmdResponse).when(apiService).apiRequest(same(account.account.tier), eq(expectedUri),
                eq(HttpMethod.GET), same(account.auth.token), isNull(), eq(BlinkCommandResponse.class));
        @SuppressWarnings("unchecked")
        Consumer<Boolean> handler = mock(Consumer.class);
        apiService.watchCommandStatus(scheduler, account, networkId, commandId, handler);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(runnableCaptor.capture(), eq(1L), eq(TimeUnit.SECONDS));
        assertThat(apiService.cmdStatusJobs, is(equalTo(Map.of(expectedUri, future))));
        runnableCaptor.getValue().run();
        verify(handler).accept(true);
        assertThat(apiService.cmdStatusJobs, is(anEmptyMap()));
    }

    @Test
    void testWatchCommandStatusOnException() throws IOException {
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        long networkId = 1L;
        long commandId = 2L;
        String expectedUri = "/network/" + networkId + "/command/" + commandId;
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        Future<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).schedule(ArgumentMatchers.any(Runnable.class), anyLong(), any());
        doThrow(new IOException()).when(apiService).apiRequest(same(account.account.tier), eq(expectedUri),
                eq(HttpMethod.GET), same(account.auth.token), isNull(), eq(BlinkCommandResponse.class));
        @SuppressWarnings("unchecked")
        Consumer<Boolean> handler = mock(Consumer.class);
        apiService.watchCommandStatus(scheduler, account, networkId, commandId, handler);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(runnableCaptor.capture(), eq(1L), eq(TimeUnit.SECONDS));
        assertThat(apiService.cmdStatusJobs, is(equalTo(Map.of(expectedUri, future))));
        runnableCaptor.getValue().run();
        verify(handler).accept(false);
        assertThat(apiService.cmdStatusJobs, is(anEmptyMap()));
    }

    @Test
    void testWatchCommandStatusOnRetry() throws IOException {
        BlinkAccount account = BlinkTestUtil.testBlinkAccount();
        long networkId = 1L;
        long commandId = 2L;
        String expectedUri = "/network/" + networkId + "/command/" + commandId;
        ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
        Future<?> future = mock(ScheduledFuture.class);
        doReturn(future).when(scheduler).schedule(ArgumentMatchers.any(Runnable.class), anyLong(), any());
        BlinkCommandResponse cmdResponse = new BlinkCommandResponse();
        cmdResponse.complete = false;
        doReturn(cmdResponse).when(apiService).apiRequest(same(account.account.tier), eq(expectedUri),
                eq(HttpMethod.GET), same(account.auth.token), isNull(), eq(BlinkCommandResponse.class));
        @SuppressWarnings("unchecked")
        Consumer<Boolean> handler = mock(Consumer.class);
        apiService.watchCommandStatus(scheduler, account, networkId, commandId, handler);
        ArgumentCaptor<Runnable> runnableCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(scheduler).schedule(runnableCaptor.capture(), eq(1L), eq(TimeUnit.SECONDS));
        assertThat(apiService.cmdStatusJobs, is(equalTo(Map.of(expectedUri, future))));
        runnableCaptor.getValue().run();
        verify(handler, times(0)).accept(anyBoolean());
        verify(apiService).watchCommandStatus(same(scheduler), same(account), eq(networkId), eq(commandId),
                same(handler));
        assertThat(apiService.cmdStatusJobs, is(equalTo(Map.of(expectedUri, future))));
    }

    @Test
    void testDispose() {
        Future<?> future = mock(Future.class);
        apiService.cmdStatusJobs.put("uri", future);
        apiService.dispose();
        verify(future).cancel(true);
        assertThat(apiService.cmdStatusJobs, is(anEmptyMap()));
    }
}
