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
package org.openhab.binding.iotawatt.internal.client;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.iotawatt.internal.model.StatusResponse;

import com.google.gson.Gson;

/**
 * @author Peter Rosenberg - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class IoTaWattClientTest {
    private static final String DEVICE_STATUS_RESPONSE_FILE = "apiResponses/device-status-response.json";

    @Mock
    @NonNullByDefault({})
    private HttpClient httpClient;
    private final Gson gson = new Gson();

    @Test
    void fetchStatus_whenValidJson_returnObject() throws IOException, ExecutionException, InterruptedException,
            TimeoutException, IoTaWattClientInterruptedException, IoTaWattClientCommunicationException,
            IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);
        when(contentResponse.getContentAsString()).thenReturn(readFile(DEVICE_STATUS_RESPONSE_FILE));

        // when
        Optional<StatusResponse> resultOptional = client.fetchStatus();

        // then
        // noinspection OptionalGetWithoutIsPresent
        StatusResponse result = resultOptional.get();
        assertThat(result.inputs().size(), is(2));
        StatusResponse.Input input0 = result.inputs().get(0);
        assertThat(input0.channel(), is(0));
        assertThat(input0.vrms(), is(254.2972F));
        assertThat(input0.hz(), is(50.02768F));
        assertThat(input0.phase(), is(0.92F));
        StatusResponse.Input input1 = result.inputs().get(1);
        assertThat(input1.channel(), is(1));
        assertThat(input1.watts(), is(1.42F));
        assertThat(input1.phase(), is(2.2F));
    }

    @Test
    void fetchStatus_whenWrongHost_throwException() {
        // given
        final IoTaWattClient client = new IoTaWattClient(" ", 10, httpClient, mock(Gson.class));

        // when
        assertThrows(IoTaWattClientConfigurationException.class, client::fetchStatus);
    }

    @Test
    void fetchStatus_whenInputsAndOutputsEmpty_returnEmpty()
            throws ExecutionException, InterruptedException, TimeoutException, IoTaWattClientInterruptedException,
            IoTaWattClientCommunicationException, IoTaWattClientConfigurationException, IoTaWattClientException {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getContentAsString()).thenReturn("{}");
        when(contentResponse.getStatus()).thenReturn(HttpStatus.OK_200);

        // when
        Optional<StatusResponse> resultOptional = client.fetchStatus();

        // then
        // noinspection OptionalGetWithoutIsPresent
        StatusResponse result = resultOptional.get();
        assertNull(result.inputs());
        assertNull(result.outputs());
    }

    @Test
    void fetchStatus_whenNot200Response_throwsException()
            throws ExecutionException, InterruptedException, TimeoutException {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(HttpStatus.BAD_REQUEST_400);

        // when/then
        assertThrows(IoTaWattClientCommunicationException.class, client::fetchStatus);
    }

    @ParameterizedTest
    @MethodSource("provideParamsForThrowCases")
    void fetchStatus_whenExceptions_throwsCustomException(Class<Throwable> thrownException,
            Class<Throwable> expectedException) throws ExecutionException, InterruptedException, TimeoutException {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        Request request = mock(Request.class);
        when(httpClient.newRequest(any(URI.class))).thenReturn(request);
        when(request.method(any(HttpMethod.class))).thenReturn(request);
        when(request.timeout(anyLong(), any(TimeUnit.class))).thenReturn(request);
        when(request.send()).thenThrow(thrownException);

        // when/then
        assertThrows(expectedException, client::fetchStatus);
    }

    @Test
    void start_whenSuccess_noException() {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        // when
        client.start();
        // then
        // doesn't throw an exception
    }

    @Test
    void start_whenError_throwException() throws Exception {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        doThrow(Exception.class).when(httpClient).start();
        // when/then
        assertThrows(IllegalStateException.class, client::start);
    }

    @Test
    void stop_whenSuccess_noException() {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        // when
        client.stop();
        // then
        // doesn't throw an exception
    }

    @Test
    void stop_whenError_noException() throws Exception {
        // given
        final IoTaWattClient client = new IoTaWattClient("hostname", 10, httpClient, gson);
        doThrow(Exception.class).when(httpClient).stop();
        // when
        client.stop();
        // then
        // doesn't throw an exception
    }

    private static Stream<Arguments> provideParamsForThrowCases() {
        return Stream.of(Arguments.of(InterruptedException.class, IoTaWattClientInterruptedException.class),
                Arguments.of(TimeoutException.class, IoTaWattClientCommunicationException.class),
                Arguments.of(ExecutionException.class, IoTaWattClientException.class));
    }

    private String readFile(String filename) throws IOException {
        final Path workingDir = Path.of("", "src/test/resources");
        final Path file = workingDir.resolve(filename);
        return Files.readString(file);
    }
}
